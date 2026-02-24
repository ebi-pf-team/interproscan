#!/usr/bin/env python3

import argparse
import sys
import json
import xml.etree.ElementTree as ET


SCHEMAS = "https://ftp.ebi.ac.uk/pub/software/unix/iprscan/5/schemas"

# Register default namespace
ET.register_namespace("", SCHEMAS)

# interproscan 5 xml match tags 
MATCH_TAG_MAP = {
    "Pfam": "hmmer3-match",
    "SMART": "hmmer3-match",
    "SUPERFAMILY": "superfamilyhmmer3-match",
    "CATH-Gene3D": "hmmer3-match",
    "CATH-FunFam": "funfamhmmer3-match",
    "CDD": "rpsblast-match",
    "HAMAP": "hmmer3-match",
    "NCBIFAM": "hmmer3-match",
    "PANTHER": "panther-match",
    "PIRSF": "hmmer3-match",
    "PIRSR": "hmmer3-match-with-sites",
    "PRINTS": "fingerprintscan-match",
    "PROSITE patterns": "patternscan-match",
    "PROSITE profiles": "profilescan-match",
    "SFLD": "hmmer3-match",
    "COILS": "coils-match",
    "MobiDB-lite": "mobidblite-match",
    "PHOBIUS": "phobius-match",
    "TMHMM": "tmhmm-match",
    "SIGNALP_EUK": "signalp-match",
    "SIGNALP_GRAM_NEGATIVE": "signalp-match",
    "SIGNALP_GRAM_POSITIVE": "signalp-match"
}


# XML conversion functions
def make_location_tag(match_tag):
    if match_tag.endswith("-match-with-sites"):
        return match_tag.replace("-match-with-sites", "-location")
    return match_tag.replace("-match", "-location")


def make_fragment_tag(match_tag):
    if match_tag.endswith("-match-with-sites"):
        return match_tag.replace("-match-with-sites", "-location-fragment")
    return match_tag.replace("-match", "-location-fragment")


def make_site_tag(match_tag):
    if match_tag.endswith("-match-with-sites"):
        return match_tag.replace("-match-with-sites", "-site")
    return match_tag.replace("-match", "-site")


def copy_element(elem):
    new_elem = ET.Element(elem.tag, elem.attrib)
    new_elem.text = elem.text
    new_elem.tail = elem.tail

    if "type" in new_elem.attrib:
        new_elem.attrib["type"] = new_elem.attrib["type"].upper()

    for child in elem:
        new_elem.append(copy_element(child))
    return new_elem

def convert_match_xml(match_elem):
    source = match_elem.get("source")
    match_tag = MATCH_TAG_MAP.get(source, "hmmer3-match")

    match_attrib = {}
    if "evalue" in match_elem.attrib:
        match_attrib["evalue"] = match_elem.attrib["evalue"]
    if "score" in match_elem.attrib:
        match_attrib["score"] = match_elem.attrib["score"]

    new_match = ET.Element(match_tag, match_attrib)

    for child in match_elem:

        if child.tag in ["signature", "model-ac"]:
            new_match.append(copy_element(child))

        elif child.tag == "locations":
            new_locations = ET.Element("locations")

            for loc in child:
                loc_attrib = dict(loc.attrib)

                new_loc = ET.Element(
                    make_location_tag(match_tag),
                    loc_attrib
                )

                for sub in loc:

                    if sub.tag == "location-fragments":
                        frag_container = ET.Element("location-fragments")

                        for frag in sub:
                            new_frag = ET.Element(
                                make_fragment_tag(match_tag),
                                dict(frag.attrib)
                            )
                            frag_container.append(new_frag)

                        new_loc.append(frag_container)

                    elif sub.tag == "sites":
                        site_container = ET.Element("sites")

                        for site in sub:
                            new_site = ET.Element(
                                make_site_tag(match_tag),
                                dict(site.attrib)
                            )

                            for site_child in site:
                                new_site.append(copy_element(site_child))

                            site_container.append(new_site)

                        new_loc.append(site_container)

                    else:
                        new_loc.append(copy_element(sub))

                new_locations.append(new_loc)

            new_match.append(new_locations)

    return new_match

def convert_file_xml(input_xml, output_xml):
    tree = ET.parse(input_xml)
    root6 = tree.getroot()

    root5 = ET.Element(f"{{{SCHEMAS}}}protein-matches")
    root5.set(
        "interproscan-version",
        root6.get("interproscan-version", "") + "-" +
        root6.get("interpro-version", "")
    )

    for protein in root6.findall("protein"):
        new_protein = ET.SubElement(root5, "protein")

        seq = protein.find("sequence")
        if seq is not None:
            new_protein.append(copy_element(seq))

        xref = protein.find("xref")
        if xref is not None:
            new_protein.append(copy_element(xref))

        matches_container = ET.SubElement(new_protein, "matches")

        matches = protein.find("matches")
        if matches is not None:
            for match in matches.findall("match"):
                new_match = convert_match_xml(match)
                matches_container.append(new_match)

    try:
        ET.indent(root5, space="  ")
    except AttributeError:
        pass

    ET.ElementTree(root5).write(
        output_xml,
        encoding="UTF-8",
        xml_declaration=True
    )


# JSON conversion functions
def convert_match_json(match):
    new_match = {}

    for key, value in match.items():
        # skip source
        if key == "source":
            continue
        new_match[key] = value

    # uppercase signature.type and entry.type
    signature = new_match.get("signature")
    if isinstance(signature, dict):

        if "type" in signature and signature["type"]:
            signature["type"] = signature["type"].upper()

        entry = signature.get("entry")
        if isinstance(entry, dict) and "type" in entry and entry["type"]:
            entry["type"] = entry["type"].upper()

    return new_match

def convert_file_json(input_json, output_json):
    with open(input_json) as f:
        data = json.load(f)

    version = data.get("interproscan-version", '') + '-' + data.get("interpro-version", '')

    new_data = {
        "interproscan-version": version,
        "results": []
    }

    for protein in data.get("results", []):
        new_protein = {}

        if "xref" in protein:
            new_protein["xref"] = protein["xref"]

        if "md5" in protein:
            new_protein["md5"] = protein["md5"]

        if "sequence" in protein:
            new_protein["sequence"] = protein["sequence"]

        new_protein["matches"] = []

        for match in protein.get("matches", []):
            new_protein["matches"].append(convert_match_json(match))

        new_data["results"].append(new_protein)

    with open(output_json, "w") as f:
        json.dump(new_data, f, indent=2)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Convert InterProScan 6 to InterProScan 5 XML or JSON output"
    )

    parser.add_argument("-i", "--input", required=True)
    parser.add_argument("-o", "--output", required=True)

    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("-xml", "--xml", action="store_true", help="Convert XML")
    group.add_argument("-json", "--json", action="store_true", help="Convert JSON")

    args = parser.parse_args()

    if args.xml:
        convert_file_xml(args.input, args.output)
    elif args.json:
        convert_file_json(args.input, args.output)
