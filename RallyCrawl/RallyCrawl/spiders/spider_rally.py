import scrapy
import json

class RallySpider(scrapy.Spider):
	serviceCount = 0
	service_url = "https://rally1.rallydev.com/slm/webservice/"
	project_url = "https://rally1.rallydev.com/slm/webservice/v2.x/project/15468059055"
	workspace_url = "https://rally1.rallydev.com/slm/webservice/v2.x/workspace/3852683575"
	service_list = []

	name = "rally"
	allowed_domain = ["rallydev.com"]
	start_urls = ["https://rally1.rallydev.com/slm/login.op"]

	# def start_requests(self):
	# 	return [scrapy.FormRequest("http://rally1.rallydev.com/slm/j_spring_security_check",
	# 		formdata={'j_username' : 'man.shen@ni.com',
	# 				  'j_password' : 'sm19890923'},
	# 		callback=self.logged_in)]

	# def logged_in(self, response):
	# 	with open("rally", "wb") as f:
	# 		f.write(response.body)

	def parse(self, response):
		# with open ("rally", "wb") as f:
		# 	f.write(response.body)
		return scrapy.FormRequest.from_response(
				response,
				formdata={'j_username' : 'man.shen@ni.com', 'j_password' : 'sm19890923'},
				callback = self.after_login
			)

	def after_login(self, response):
		return scrapy.Request(RallySpider.workspace_url, callback = self.parse_service)
		# with open("rally", "wb") as f:
		# 	f.write(response.body)

	def parse_service(self, response):
		# with open("service", "wb") as f:
		# 	f.write(response.body)
		# print response.body
		service_json = json.loads(response.body)

		# print service_json.keys()
		# print service_json_values()
		with open("service", "ab") as f:
			print "This is Request +++++++++++++++++++++++++++++++++"
			f.write("Response: " + response.url + "\n")
			# self.get_info_from_dict(service_json)
			# for value in self.get_info_from_dict(service_json, f):
			# 	yield scrapy.Request(value, callback=self.parse_service)
			for service in self.get_info_from_dict(service_json):
				if self.is_us(service):
					f.write("UserStory: " + service + "\n")
				else:
					f.write(service + "\n")
					yield scrapy.Request(service, callback=self.parse_service)
		# print type(service_json)

	def get_info_from_dict(self, info):
		if isinstance(info, dict):
			print "This is a dict __________________________________________"
			print info
			for key, value in info.items():
				if self.is_us(key):
					yield key
				else:
					for service in self.get_info_from_dict(value):
						yield service
		elif isinstance(info, list):
			print "This is a list **************************************"
			print info
			for value in info:
				for service in self.get_info_from_dict(value):
					yield service
		elif isinstance(info, basestring) and RallySpider.service_url in info:
		# elif isinstance(value, basestring):
			print "This is a Service #############################################"
			print info
			# if len(value) > 0:
			# 	print value[0]
			# f.write(value[0])
			if RallySpider.service_list.count(info) == 0 and RallySpider.serviceCount <= 1000:
				RallySpider.serviceCount += 1
				RallySpider.service_list.append(info)
				print RallySpider.serviceCount
				yield info
		elif isinstance(info, basestring) and self.is_us(info):
			yield info

	def is_us(self, value):
		return "user story" in value.lower() or "user stories" in value.lower() or "US" in value or "userstory" in value.lower() or "userstories" in value.lower()