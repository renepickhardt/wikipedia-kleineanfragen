"""
Author: Rene Pickhardt (rene@rene-pickhardt.de)
License: GPLv3

This script connects to a postgres database (called rpickhardt) containing the data dump of kleineanfragen.de
(c.f.: https://kleineanfragen.de/info/daten ) 

it extracts the text of kleine anfragen into 1 line (without improoving problems from pdf extraction that can be found in the data)

creates a tsv file of the format:

URL\t\TEXT

the tsv file is storred in data/ka.tsv


"""

import re
import psycopg2

# Try to connect

conn = None
try:
    conn=psycopg2.connect(database="rpickhardt")
except:
    print "I am unable to connect to the database."

cur = conn.cursor()

try:
    cur.execute("""SELECT body_id, legislative_term, slug, contents
FROM papers;
""")
except:
    print "I can't show all databases"
    
rows = cur.fetchall()
#https://kleineanfragen.de/bayern/17/7860-mobbing-an-schulen
print "\nRows: \n"
cnt = 0
anfragen= {}
for row in rows:
    cnt = cnt + 1
    kaURL = "https://kleineanfragen.de/" + bodies[row[0]]["slug"] + "/" + str(row[1]) + "/"+ row[2]
    content = row[3]
    if content == None:
        print "could not extract text of: ", row
        continue
    content = content.decode("utf-8")
    text = " ".join(re.findall(r'(?u)(\w+)',content.lower()))
    anfragen[kaURL]=text

f = open("data/ka.tsv","w")
for url in anfragen:
    text = anfragen[url]
    wstring = url+ "\t" + text + "\n"
    #wstring = unicode(wstring, 'utf-8')
    f.write(wstring.encode("utf-8")) 

