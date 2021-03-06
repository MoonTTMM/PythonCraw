import re

def enum(**enums):
    return type('Enum', (), enums)

def build_url(base_service, types, fields, querys):
	typestring = ""
	fieldstring = ""
	querystring = ""
	if len(types) > 0:
		typestring = "&types=" + "/".join(types)

	if len(fields) > 0:
		fieldstring = "&fetch=" + ",".join(fields)

	query_count = len(querys)
	if query_count == 1:
		querystring = "(" + querys[0] + ")" 
	else:
		i = 0
		while i < query_count:
			if i == 0:
				querystring = "(" + querys[i] + ")"
			else:
				querystring = "(" + querystring + " AND (" + querys[i] + "))"
			i = i + 1
	querystring = "&query=" + querystring
	url = base_service + "?pagesize=200" + typestring + fieldstring + querystring
	return url

def parse_todo_hour_from_iteration(content):
	return re.search(r'TASK REMAINING TOTAL changed from \[\d+\.\d\] to \[(\d+)\.\d\]', content).group(1)

def parse_actual_hour_from_iteration(content):
	return re.search(r'TASK ACTUAL TOTAL changed from \[\d+\.\d\] to \[(\d+)\.\d\]', content).group(1)

def parse_id_from_url(url):
	return re.search(r'\d+', re.search(r'/\d+', url).group()).group()

def parse_iteration_from_revision(content):
	return re.search(r'ITERATION changed from \[(.*)\] to \[(.*)\]', content).groups()

def get_project_id_from_url(url):
	return re.search(r'Project/(\d+)', url).group(1)
		

