import scrapy
from snowball.items import *

TYPE = "11,12"
loginUrl = "http://xueqiu.com/"
baseUrl = "http://xueqiu.com/stock/cata/stocklist.json?type="+TYPE
financialUrl = "http://xueqiu.com/stock/f10/finmainindex.json?page=1&size=4"

class FinancialSpicer(scrapy.Spider):
	name = "financial"
	allowed_domain = ["xueqiu.com"]
	start_urls = [loginUrl]

	page_count = 0
	size = 90

	stock_info = {}

	def parse(self, response):
		return scrapy.FormRequest.from_response(
				response,
				formdata={'username' : 'superttmm@163.com', 'password' : 'sm19890923'},
				callback = self.after_login
			)

	def after_login(self, response):
		yield scrapy.Request(baseUrl, callback = parse_stock_count)

	def parse_stock_count(self, response):
		count_dict = json.loads(response.body)
		page_count = count_dict["count"] / page_size + 1
		for x in xrange(1,page_count):
			yield scrapy.Request(baseUrl + "&size=" + size + "&page=" + x, callback = parse_stock)

	def parse_stock(self, response):
		stock_dict = json.loads(response.body)
		for stock in stock_dict["stocks"]:
			symbol = stock["symbol"]
			FinancialSpicer.stock_info[symbol] = stock["name"]
			yield scrapy.Request(financialUrl + "&symbol=" + symbol, callback = parse_financial)

	def parse_financial(self, response):
		finan_dict = json.loads(response.body)
		for f in finan_dict["list"]:
			symbol = get_symbol_from_url(response.url)
			yield FinancialItem(symbol = symbol, name = FinancialSpicer.stock_info[symbol], netincgrowrate = f["netincgrowrate"])

	def get_symbol_from_url(url):
		return re.search(r'&symbol=(.*)', content).group(1)




