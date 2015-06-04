TYPES = tuple("portfolioitem/feature", "hierarchicalrequirement")
FIELDS = tuple("Name", "Owner", "Children")
BASE_SERVICE = "https://rally1.rallydev.com/slm/webservice/v2.x/artifact"

def build_url(base_service, types, fields, query):
	url = base_service + "?" + 
	"types=" + "/".join(types) + 
	"query=(" + query + ")" + 
	"fields=" + ",".join(fields)


