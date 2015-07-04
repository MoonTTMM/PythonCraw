import scrapy
import json
import helper
import re
from RallyCrawl.items import UserStory

TYPES = helper.enum(FEATURE="portfolioitem/feature", USERSTORY="hierarchicalrequirement")
FIELDS = helper.enum(NAME="Name", OWNER="Owner", CHILDREN="Children")
BASE_SERVICE = "https://rally1.rallydev.com/slm/webservice/v2.x/iteration"
ITERATION = "I17"

project_query = "Project = \"Project/15468059055\""

def add_to_us_dict(userstory_dict, us_id, added_dict):
	if userstory_dict.has_key(us_id):
		userstory_dict[us_id]["dependencies"].update(added_dict)
	else:
		for value in userstory_dict.values():
			add_to_us_dict(value["dependencies"], us_id, added_dict)

def create_userstory(userstory_dict):
	return {"id" : userstory_dict["FormattedID"], "name" : userstory_dict["Name"], "dependencies" : {} }

def create_userstory_item(userstory_dict):
	return UserStory(objectID = userstory_dict["objectId"], formattedId = userstory_dict["FormattedID"], name = userstory_dict["Name"])

class IterationSpider(scrapy.Spider):
	name = "iteration"
	allowed_domain = ["rallydev.com"]
	start_urls = ["https://rally1.rallydev.com/slm/login.op"]

	# {objectId: {formattedId:"", name:"", dependencies:{objectId:{..} , ...} }}
	queried_userstories = {}

	def parse(self, response):
		return scrapy.FormRequest.from_response(
				response,
				formdata={'j_username' : 'man.shen@ni.com', 'j_password' : 'sm19890923'},
				callback = self.after_login
			)

	def after_login(self, response):
		project_iterations_url = helper.build_url(BASE_SERVICE, [], [], [project_query])
		return scrapy.Request(project_iterations_url, callback = self.parse_iteration)

	def parse_iteration(self, response):
		iteration_dict = json.loads(response.body)
		for iteration in iteration_dict["QueryResult"]["Results"]:
			if ITERATION in iteration["Name"]:
				iteration_query = "iteration = \"" +　iteration["_ref"] + "\""
				iteration_userstories_url = helper.build_url(BASE_SERVICE, [TYPES.USERSTORY], [], [project_query, iteration_query])
				yield scrapy.Request(iteration_userstories_url, callback = self.parse_userstory)

	# traverse all userstories and get those with given owner.
	def parse_userstory(self, response):
		userstory_dict = json.loads(response.body)
		# userstory is a dict
		for userstory in userstory_dict["QueryResult"]["Results"]:
			# recursion in user story tree. (only traverse search userstory)
			children = userstory["Children"]
			yield scrapy.Request(children["_ref"]+"?pagesize=10000", callback = self.parse_userstory)
			# recursion in predecessor tree.
			predecessors = userstory["Predecessors"]
			if predecessors["Count"] > 0 : 
				yield UserStory(name = userstory["Name"])
				UserstorySpider.queried_userstories[userstory["ObjectID"]] = create_userstory(userstory)
				yield scrapy.Request(predecessors["_ref"], callback = self.parse_predecessors)

	# traverse all predecessors of one userstory
	def parse_predecessors(self, response):
		userstory_dict = json.loads(response.body)
		# userstory is a dict
		for userstory in userstory_dict["QueryResult"]["Results"]:
			# according to the us's successor id, add us to the dependencies of the successor.
			successor_id = re.search(r'\d+', re.search(r'/\d+/', response.url).group()).group()
			dependency = create_userstory(userstory)	
			add_to_us_dict(UserstorySpider.queried_userstories, successor_id, dependency)
			# recursion in predecessor tree.
			predecessors = userstory["Predecessors"]
			if predecessors["Count"] > 0:
				yield scrapy.Request(predecessors["_ref"], callback = self.parse_predecessors)
			















	
