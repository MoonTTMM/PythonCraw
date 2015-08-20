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
PROJECTS = helper.enum(SEARCH="Project = \"Project/15468059055\"", \
	CONTROL="Project = \"Project/15467515126\"", \
	DIFF="Project = \"Project/15468575267\"", \
	DataStorage = "Project = \"Project/20232532972\"", \
	Login = "Project = \"Project/21328527954\"", \
	TestPanel = "Project = \"Project/22610876987\"", \
	Interative = "Project = \"Project/15467935361\"")

projects_se = [PROJECTS.SEARCH, PROJECTS.CONTROL, PROJECTS.DIFF]
projects_tdms = [PROJECTS.DataStorage, PROJECTS.Login, PROJECTS.TestPanel, PROJECTS.Interative]

owners_se = ["Man Shen", "Zhihao Zhang", "Tian Lan", "Yichao Jia", "Ruhao Gao", "Zhenfang Zhu", \
	"Jifan Wang", "Mengliang Li", "Lucy Wang", "Jinyong Zhang", "Wenbin Xie","Mark Dai", "Penar Zhu"]
owners_tdms = ["Jie Zheng", "Xiaowei Jiang", "Yi Su", "Tianbin Xu", "Zaizhou Ma"]

class BurndownSpider(scrapy.Spider):
	name = "burndown"
	allowed_domain = ["rallydev.com"]
	start_urls = ["https://rally1.rallydev.com/slm/login.op"]

	userstory_owner_map = {}
	userstory_project_map = {}
	userstory_iteration_map = {}

	def parse(self, response):
		return scrapy.FormRequest.from_response(
				response,
				formdata={'j_username' : 'man.shen@ni.com', 'j_password' : 'sm19890923'},
				callback = self.after_login
			)

	def after_login(self, response):
		for project in projects_se:
			search_iterations_url = helper.build_url(BASE_SERVICE + CATEGORY.ITERATION, [], [FIELDS.ENDDATE, FIELDS.STARTDATE], [project])
			yield scrapy.Request(search_iterations_url, callback = self.parse_iteration)			

	def parse_iteration(self, response):
		iteration_dict = json.loads(response.body)
		project_id = helper.get_project_id_from_url(response.url)
		for iteration in iteration_dict["QueryResult"]["Results"]:
			#if "I16" in iteration["_refObjectName"] or "I17" in iteration["_refObjectName"]:
			# yield IterationInfo(iteration = iteration["_refObjectName"], startDate = iteration["StartDate"], endDate = iteration["EndDate"])
			iteration_url = iteration["_ref"]
			iteration_query = "iteration = \"" + iteration_url + "\""
			project_query = "project = \"Project/" + project_id + "\""
			iteration_userstories_url = helper.build_url(BASE_SERVICE + CATEGORY.ARTIFACT, [TYPES.USERSTORY], \
				[FIELDS.OWNER, FIELDS.REVISION, FIELDS.ESTIMATE, FIELDS.ACTUAL, FIELDS.REMAINING, FIELDS.PROJECT, FIELDS.ITERATION], [project_query, iteration_query])
			yield scrapy.Request(iteration_userstories_url, callback = self.parse_userstory)

	# traverse all userstories and get those with given owner.
	def parse_userstory(self, response):
		userstory_dict = json.loads(response.body)
		# userstory is a dict
		for userstory in userstory_dict["QueryResult"]["Results"]:
			# recursion in user story tree. (only traverse search userstory)
			owner = userstory["Owner"]["_refObjectName"]
			project = userstory["Project"]["_refObjectName"]
			iteration = userstory["Iteration"]["_refObjectName"]
			if owner in owners_se:
				# use this as the identifier for project
				revisionHistory = userstory["RevisionHistory"]["_ref"]
				userstory = helper.parse_id_from_url(revisionHistory)
				BurndownSpider.userstory_owner_map[userstory] = owner
				BurndownSpider.userstory_project_map[userstory] = project
				BurndownSpider.userstory_iteration_map[userstory] = iteration
				yield scrapy.Request(revisionHistory + "/Revisions?pagesize=200", callback = self.parse_revisions)

	def parse_revisions(self, response):
		revision_dict = json.loads(response.body)
		for revision in revision_dict["QueryResult"]["Results"]:
			date = revision["_CreatedAt"]
			time = revision["CreationDate"]
			content = revision["Description"]
			revisionHistory = revision["RevisionHistory"]["_ref"]
			userstory = helper.parse_id_from_url(revisionHistory)
			owner = BurndownSpider.userstory_owner_map[userstory]
			project = BurndownSpider.userstory_project_map[userstory]
			iteration = BurndownSpider.userstory_iteration_map[userstory]
			if "TASK REMAINING TOTAL" in content:
				todo_hour = helper.parse_todo_hour_from_iteration(content)
				yield IterationBurnDown(time = time, todo = todo_hour, userstory = userstory, date = date, owner = owner, project = project, iteration = iteration)
			if "TASK ACTUAL TOTAL" in content:
				actual_hour = helper.parse_actual_hour_from_iteration(content)
				yield IterationBurnDown(time = time, actual = actual_hour, userstory = userstory, date = date, owner = owner, project = project, iteration = iteration)
			if "ITERATION changed from" in content:
				iterations = helper.parse_iteration_from_revision(content)
				yield UserstoryInfo(userstory = userstory, iteration = iterations[0], endTime = time)
				yield UserstoryInfo(userstory = userstory, iteration = iterations[1], startTime = time)
















	
