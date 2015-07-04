# from RallyCrawl.spiders.spider_iteration import *
import sys
sys.path.append('..')

from spiders.helper import *

TYPES = enum(FEATURE="portfolioitem/feature", USERSTORY="hierarchicalrequirement")
FIELDS = enum(NAME="Name", OWNER="Owner", CHILDREN="Children", REVISION="RevisionHistory", REMAINING="TaskRemainingTotal", ACTUAL="TaskActualTotal", ESTIMATE="TaskEstimateTotal")
BASE_SERVICE = "https://rally1.rallydev.com/slm/webservice/v2.x/"
CATEGORY = enum(ITERATION="iteration", ARTIFACT="artifact", TASK="task")
ITERATION = "I17"
project_query = "Project = \"Project/15468059055\""
iteration_query = "iteration = \"iteration/24353200602\""

project_iterations_url = build_url(BASE_SERVICE + CATEGORY.ITERATION, [], [], [project_query])
print project_iterations_url

iteration_userstories_url = build_url(BASE_SERVICE + CATEGORY.ARTIFACT, [TYPES.USERSTORY], \
					[FIELDS.OWNER, FIELDS.REVISION, FIELDS.ESTIMATE, FIELDS.ACTUAL, FIELDS.REMAINING], [project_query, iteration_query])
print iteration_userstories_url

revision_des = "TASK ACTUAL TOTAL changed from [0.0] to [8.0]"
print parse_hour_from_iteration(revision_des)

history_url = "https://rally1.rallydev.com/slm/webservice/v2.x/revisionhistory/36076397475"
print parse_id_from_url(history_url)

iteration = {"_ref" : "abc"}
print "iteration = \"" + iteration["_ref"] + "\""



