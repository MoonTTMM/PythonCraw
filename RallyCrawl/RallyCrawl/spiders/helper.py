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

