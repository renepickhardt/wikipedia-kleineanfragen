This tool creates a mapping from requests and responds to the German (state) governments ( published at https://www.kleineanfragen.de ) to the the most relevant German Wikipedia articles covering this topic

It works by building a lucene search index with a probabilistic query model (BM25) over the German Wikipedia and taking the documents from kleineanfragen.de as a query to retrieve the moste likely (id est similar) articles.

This means that you can forke the code and hang in any other data set to build similar applications. 

## Get going

### you need:
* python 2.7
* lxml (python lib)
* java 7 (JDK)
* maven2

### extracting wikipedia articles
coming soon

### extracting kleine Anfragen
coming soon

### index wikipedia articles
coming soon

## data sets
following data sets are used:
* the database of https://kleineanfragen.de/info/daten 
* a current wikipedia dump from: https://dumps.wikimedia.org/dewiki/ 
