import scrapy
import json
import helper
import re
from RallyCrawl.items import *

TYPES = helper.enum(FEATURE="portfolioitem/feature", USERSTORY="hierarchicalrequirement")
FIELDS = helper.enum(NAME="Name", OWNER="Owner", CHILDREN="Children", REVISION="RevisionHistory", \
	REMAINING="TaskRemainingTotal", ACTUAL="TaskActualTotal", ESTIMATE="TaskEstimateTotal", \
	ENDDATE="EndDate", STARTDATE="StartDate", PROJECT="Project", ITERATION="Iteration")
BASE_SERVICE = "https://rally1.rallydev.com/slm/webservice/v2.x/"
CATEGORY = helper.enum(ITERATION="iteration", ARTIFACT="artifact")
PROJECTS = helper.enum(SEARCH="Project = \"Project/15468059055\"", CONTROL="Project = \"Project/15467515126\"", DIFF="Project = \"Project/15468575267\"")

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
		search_iterations_url = helper.build_url(BASE_SERVICE + CATEGORY.ITERATION, [], [FIELDS.ENDDATE, FIELDS.STARTDATE, \
			FIELDS.ESTIMATE, FIELDS.ACTUAL, FIELDS.REMAINING, FIELDS.PROJECT], [PROJECTS.SEARCH])
		yield scrapy.Request(search_iterations_url, callback = self.parse_iteration)
		control_iterations_url = helper.build_url(BASE_SERVICE + CATEGORY.ITERATION, [], [FIELDS.ENDDATE, FIELDS.STARTDATE, \
			FIELDS.ESTIMATE, FIELDS.ACTUAL, FIELDS.REMAINING, FIELDS.PROJECT], [PROJECTS.CONTROL])
		yield scrapy.Request(control_iterations_url, callback = self.parse_iteration)
		diff_iterations_url = helper.build_url(BASE_SERVICE + CATEGORY.ITERATION, [], [FIELDS.ENDDATE, FIELDS.STARTDATE, \
			FIELDS.ESTIMATE, FIELDS.ACTUAL, FIELDS.REMAINING, FIELDS.PROJECT], [PROJECTS.DIFF])
		yield scrapy.Request(diff_iterations_url, callback = self.parse_iteration)

	def parse_iteration(self, response):
		iteration_dict = json.loads(response.body)
		project_id = helper.get_project_id_from_url(response.url)
		for iteration in iteration_dict["QueryResult"]["Results"]:
			# if ITERATION in iteration["_refObjectName"]:
			yield IterationInfo(iteration = iteration["_refObjectName"], startDate = iteration["StartDate"], \
				endDate = iteration["EndDate"], actualTotal = iteration["TaskActualTotal"], todoTotal = iteration["TaskRemainingTotal"], \
				estimateTotal = iteration["TaskEstimateTotal"], project = iteration["Project"]["_refObjectName"])














	
