#coding=utf-8
import htmlDownloader

url = raw_input("Input your url: ")
html = htmlDownloader.download(url)
print html