import helper
import re
from spider_iteration import *

TYPES = helper.enum(FEATURE="portfolioitem/feature", USERSTORY="hierarchicalrequirement")
FIELDS = helper.enum(NAME="Name", OWNER="Owner", CHILDREN="Children")
BASE_SERVICE = "https://rally1.rallydev.com/slm/webservice/v2.x/iteration"

#query = ["Project = \"Project/15468059055\"", "iteration = \"iteration/24353200602\"", "iteration = \"iteration/24353200602\""]
query = ["Project = \"Project/15468059055\""]
base_url = helper.build_url(BASE_SERVICE, [], [], query)
print base_url

# query = "https://rally1.rallydev.com/slm/webservice/v2.x/artifact/123456/test"
# print re.search(r'\d+', re.search(r'/\d+/', query).group()).group()


