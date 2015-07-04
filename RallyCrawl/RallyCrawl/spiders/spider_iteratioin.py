import scrapy
import json
import helper
import re
from RallyCrawl.items import IterationBurnDown

TYPES = helper.enum(FEATURE="portfolioitem/feature", USERSTORY="hierarchicalrequirement")
FIELDS = helper.enum(NAME="Name", OWNER="Owner", CHILDREN="Children", REVISION="RevisionHistory", REMAINING="TaskRemainingTotal", ACTUAL="TaskActualTotal", ESTIMATE="TaskEstimateTotal")
BASE_SERVICE = "https://rally1.rallydev.com/slm/webservice/v2.x/"
CATEGORY = helper.enum(ITERATION="iteration", ARTIFACT="artifact")
ITERATION = "I17"

owners = ["Man Shen"]
project_query = "Project = \"Project/15468059055\""
time_change = ["TASK REMAINING TOTAL", "TASK ACTUAL TOTAL"]

class IterationSpider(scrapy.Spider):
	name = "iteration"
	allowed_domain = ["rallydev.com"]
	start_urls = ["https://rally1.rallydev.com/slm/login.op"]

	def parse(self, response):
		return scrapy.FormRequest.from_response(
				response,
				formdata={'j_username' : 'man.shen@ni.com', 'j_password' : 'sm19890923'},
				callback = self.after_login
			)

	def after_login(self, response):
		project_iterations_url = helper.build_url(BASE_SERVICE + CATEGORY.ITERATION, [], [], [project_query])
		return scrapy.Request(project_iterations_url, callback = self.parse_iteration)

	def parse_iteration(self, response):
		iteration_dict = json.loads(response.body)
		for iteration in iteration_dict["QueryResult"]["Results"]:
			if ITERATION in iteration["Name"]:
				iteration_query = "iteration = \"" +　iteration["_ref"] + "\""
				iteration_userstories_url = helper.build_url(BASE_SERVICE + CATEGORY.ARTIFACT, [TYPES.USERSTORY], \
					[FIELDS.OWNER, FIELDS.REVISION, FIELDS.ESTIMATE, FIELDS.ACTUAL, FIELDS.REMAINING], [project_query, iteration_query])
				yield scrapy.Request(iteration_userstories_url, callback = self.parse_userstory)

	# traverse all userstories and get those with given owner.
	def parse_userstory(self, response):
		userstory_dict = json.loads(response.body)
		# userstory is a dict
		for userstory in userstory_dict["QueryResult"]["Results"]:
			# recursion in user story tree. (only traverse search userstory)
			owner = userstory["OWNER"]["_refObjectName"]
			if owner in owners:
				yield scrapy.Request(userstory["RevisionHistory"] + "/Revisions", callback = self.parse_revisions)

	def parse_revisions(self, response):
		revision_dict = json.loads(response.body)
		for revision in revision_dict["QueryResult"]["Results"]:
			date = revision["_CreatedAt"]
			time = revision["CreationDate"]
			content = revision["Description"]
			hour = helper.parse_hour_from_iteration(content)
			if "TASK REMAINING TOTAL" in content:
				yield IterationBurnDown(date = date, todo = hour)
			elif "TASK ACTUAL TOTAL" in content:
				yield IterationBurnDown(date = date, actual = hour)
			















	
