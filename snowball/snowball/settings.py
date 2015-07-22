# -*- coding: utf-8 -*-

# Scrapy settings for snowball project
#
# For simplicity, this file contains only the most important settings by
# default. All the other settings are documented here:
#
#     http://doc.scrapy.org/en/latest/topics/settings.html
#

BOT_NAME = 'snowball'

SPIDER_MODULES = ['snowball.spiders']
NEWSPIDER_MODULE = 'snowball.spiders'

# Crawl responsibly by identifying yourself (and your website) on the user-agent
#USER_AGENT = 'snowball (+http://www.yourdomain.com)'

FEED_FORMAT = 'json'
FEED_URI = 'file: ../../../%(name)s.json'
