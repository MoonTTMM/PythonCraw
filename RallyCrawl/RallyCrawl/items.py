# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class RallycrawlItem(scrapy.Item):
    # define the fields for your item here like:
    # name = scrapy.Field()
    service = scrapy.Field()

class UserStory(scrapy.Item):
	objectId = scrapy.Field()
	formattedId = scrapy.Field()
	name = scrapy.Field()
	dependencies = scrapy.Field()

class IterationBurnDown(scrapy.Item):
	date = scrapy.Field()
	time = scrapy.Field()
	userstory = scrapy.Field()
	todo = scrapy.Field()
	actual = scrapy.Field()
	accepted = scrapy.Field()

class IterationInfo(scrapy.Item):
	startDate = scrapy.Field()
	endDate = scrapy.Field()


