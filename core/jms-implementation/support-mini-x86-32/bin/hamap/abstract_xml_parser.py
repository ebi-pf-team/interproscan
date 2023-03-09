# To change this template, choose Tools | Templates
# and open the template in the editor.

__author__="craigm"
__date__ ="$15-Oct-2010 14:16:27$"

import xml.dom.minidom
import os
import re
import sys
dbcodes = {'sfld':'B', 'smart':'R',  'hamap': 'Q', 'prosite-pt': 'P','prosite-pf': 'M', 'prints':'F', 'pfam': 'H', 'pirsf': 'U', 'cdd': 'J'}
typeconversion = {"family": "F", "domain": "D", "repeat": "R", "conserved_site": "C", "motif": "C", "coiled-coil": "U", "disordered": "U"}
bracketPattern = re.compile("(MID\s*:\d+\s*)\]\s*\[(P)")
newline = re.compile("\n+")
spaces = re.compile("\s+")
# changes references in adajacent square brakets into one square braket
# eg [PMID:1] [PMID:2] to [PMID:1, PMID:2]
# does member db-specific stuff as well
def processAbstract(analysis, abstract):
    abstract = abstract.strip()
    abstract = re.sub(bracketPattern, "\\1,\\2", abstract)
    if analysis == "prints" or analysis == "pirsf":
        abstract = re.sub(newline, "</p><p>", abstract)
    #elif analysis.startswith("prosite"):
        #abstract = re.sub(newline, " ", abstract)
        abstract = re.sub(spaces, " ", abstract)
    return "".join(["<p>",abstract,"</p>"])


def allDataPresent(ac, name, shortName, type, abstract):
    return ac and name and shortName and type and abstract

# gets abstract data into the format (currently) required for loading into interpro
def formatData(ac, dbcode, name, shortName, type, abstract, handle):
    type = processType(type, dbcode)
    if len(abstract) > 4000:
        abstract = abstract[:3995] + '</p>'
    output = "::".join([ac, dbcode, shortName, name, type, abstract + "|:\n"])
    return output

def processType(type, dbcode):
    if dbcodes['hamap'] == dbcode or dbcodes['sfld'] == dbcode:
        return typeconversion['family']
    else:
        return typeconversion[type.lower()]


# main program starts here
analysis = sys.argv[1]
if analysis not in dbcodes.keys():
    print("Script needs to be updated to parse this member database. Current member dbs supported are:", dbcodes.keys())
    sys.exit(1)
dbcode = dbcodes[analysis]
infile = sys.argv[2]
outfile = sys.argv[3]
doc = xml.dom.minidom.parse(infile)
handle = open(outfile, "w")
for docChildNode in doc.childNodes:
    if docChildNode.nodeName == "signature-library-release":
        libraryNode = docChildNode
        for libraryChildNode in libraryNode.childNodes:
            if libraryChildNode.nodeName == "signature":
                signatureNode = libraryChildNode
                ac, name, shortName, type, abstract = "", "", "", "", ""
                rawAbstract = ""
                ac, name, shortName, type = signatureNode.getAttribute("ac"), signatureNode.getAttribute("desc"), signatureNode.getAttribute("name"), signatureNode.getAttribute("type")
                for node in signatureNode.childNodes:
                    if node.nodeName == "abstract":
                        abstractNode = node
                        for childNode in abstractNode.childNodes:
                            if childNode.nodeType == childNode.CDATA_SECTION_NODE:
                                rawAbstract = childNode.nodeValue
                            elif childNode.nodeType == childNode.TEXT_NODE:
                                # if there is a CDATA section eg
                                # <abstract>
                                # <![CDATA[
                                # ........
                                # ]]>
                                # </abstract>
                                # newlines can mean that mindom interprets this as 3 abstract child nodes
                                # 1 of which is the CDATA section
                                # the other two are text nodes (with newlines and empty spaces
                                # the test below means that a text node will only be used
                                # if it has actual text!
                                if not rawAbstract:
                                    rawAbstract = childNode.nodeValue
                        abstract = processAbstract(analysis, rawAbstract)
                try:
                    handle.write(formatData(ac, dbcode, name, shortName, type, abstract, handle))
                except UnicodeEncodeError:
                    print(ac)
                    sys.exit(1)








