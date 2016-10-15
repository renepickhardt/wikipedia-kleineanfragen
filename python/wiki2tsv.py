"""
Author: Rene Pickhardt (rene@rene-pickhardt.de)
License: GPLv3

this transforms the output of  Wikipedia-Extractor: https://github.com/bwbaugh/wikipedia-extractor
to a tab seperated file of the format: 

ID\tTitle\tArticle

Each article is in one line meaning all line breaks have been removed

mkdir data
cd data

clone wikiextractor:
git clone https://github.com/bwbaugh/wikipedia-extractor.git
cd wikipedia-extractor

download wikidump:
wget https://dumps.wikimedia.org/dewiki/20161001/dewikie-20161001-pages-articles.xm.bz2

extract dump and run Wikiextractor
bzcat dewikie-20161001-pages-articles.xm.bz2 >  ./WikiExtractor.py -b 5000M -o extracted-articles -ns 0,0 -o extracted-articles -

cd ../..

then run this script
python wiki2tsv.py

"""
import re
import lxml.etree as et


def extractIdAndTitle(line):
    doc=et.fromstring(line[:-1]+"</doc>")
    key = doc.attrib["id"]
    url = doc.attrib["url"]
    title = doc.attrib["title"]

    
    return key,title

def isStandardArticle(text):
    secondHalf = text.split(" title=")[1]
    if ":" in secondHalf:
        return False
    return True


cnt = 0
endcnt = 0
beginToken = "<doc id="
endToken = "</doc>"
newarticle = False
totalText = ""
shallParse = False
key = 0
title = ""
tabbedText = ""
w2 = open("data/de-20161001-1-tabbed-article-per-line","w")

# change the range depending on the output of how many files have been generated by wikipedia-extractor
for i in range(0,1):
    f = open ("data/wikipedia-extractor/extracted-articles/AA/wiki_0"+str(i))
    for line in f:
        line = line.decode('utf-8', 'ignore')
        if line.startswith(beginToken):
            cnt = cnt +1
            newarticle=True
            shallParse = isStandardArticle(line)
            key,title = extractIdAndTitle(line)
            if shallParse:
                tabbedText = str(key) + "\t" + title + "\t"
                w2.write(tabbedText.encode("utf-8"))
            continue
        if newarticle:
            newarticle = False
            continue
        if len(line)<3:
            continue
        if line.startswith(endToken):
            endcnt=endcnt+1
            if shallParse:
                w2.write("\n")
            continue
        if shallParse:
            words = re.findall(r'(?u)(\w+)', line[:-1].lower())
            wstring= " ".join(words) + " "
            w2.write(wstring.encode("utf-8"))
        if cnt%10000==0:
            print cnt
        
print cnt, endcnt
print "transformed wiki articles to tsv file"
w2.close()
