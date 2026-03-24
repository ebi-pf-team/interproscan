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
    "AntiFam": "hmmer3-match",
    "Pfam": "hmmer3-match",
    "SMART": "hmmer2-match",
    "SUPERFAMILY": "superfamilyhmmer3-match",
    "CATH-Gene3D": "hmmer3-match",
    "CATH-FunFam": "funfamhmmer3-match",
    "CDD": "rpsblast-match",
    "HAMAP": "profilescan-match",
    "NCBIFAM": "hmmer3-match",
    "PANTHER": "panther-match",
    "PIRSF": "hmmer3-match",
    "PIRSR": "hmmer3-match-with-sites",
    "PRINTS": "fingerprints-match",
    "PROSITE patterns": "patternscan-match",
    "PROSITE profiles": "profilescan-match",
    "SFLD": "hmmer3-match-with-sites",
    "COILS": "coils-match",
    "MobiDB-lite": "mobidblite-match",
    "PHOBIUS": "phobius-match",
    "TMHMM": "tmhmm-match",
    "SIGNALP_EUK": "signalp-match",
    "SIGNALP_GRAM_NEGATIVE": "signalp-match",
    "SIGNALP_GRAM_POSITIVE": "signalp-match"
}

MATCH_ORDER = [
    "coils-match",
    "fingerprints-match",
    "funfamhmmer3-match",
    "hmmer2-match",
    "hmmer3-match",
    "hmmer3-match-with-sites",
    "mobidblite-match",
    "panther-match",
    "patternscan-match",
    "phobius-match",
    "blastprodom-match",
    "profilescan-match",
    "rpsblast-match",
    "signalp-match",
    "superfamilyhmmer3-match",
    "tmhmm-match"
]

LIBRARY_NAME_MAP = {
    "AntiFam": "ANTIFAM",
    "Pfam": "PFAM",
    "SMART": "SMART",
    "SUPERFAMILY": "SUPERFAMILY",
    "CATH-Gene3D": "GENE3D",
    "CATH-FunFam": "FUNFAM",
    "CDD": "CDD",
    "HAMAP": "HAMAP",
    "NCBIFAM": "NCBIFAM",
    "PANTHER": "PANTHER",
    "PIRSF": "PIRSF",
    "PIRSR": "PIRSR",
    "PRINTS": "PRINTS",
    "PROSITE patterns": "PROSITE_PATTERNS",
    "PROSITE profiles": "PROSITE_PROFILES",
    "SFLD": "SFLD",
    "COILS": "COILS",
    "MobiDB-lite": "MOBIDB_LITE",
    "PHOBIUS": "PHOBIUS",
    "TMHMM": "TMHMM",
    "SIGNALP_EUK": "SIGNALP_EUK",
    "SIGNALP_GRAM_NEGATIVE": "SIGNALP_GRAM_NEGATIVE",
    "SIGNALP_GRAM_POSITIVE": "SIGNALP_GRAM_POSITIVE"
}

# XML conversion functions
def make_location_tag(match_tag):

    if match_tag.endswith("-match-with-sites"):
        return match_tag.replace("-match-with-sites", "-location-with-sites")

    return match_tag.replace("-match", "-location")


def make_fragment_tag(match_tag):

    # FunFam special case (IPS5 quirk)
    if match_tag == "funfamhmmer3-match":
        return "hmmer3-location-fragment"

    if match_tag.endswith("-match-with-sites"):
        return match_tag.replace("-match-with-sites", "-location-fragment-with-sites")

    return match_tag.replace("-match", "-location-fragment")


def make_site_tag(match_tag):

    if match_tag.endswith("-match-with-sites"):
        return match_tag.replace("-match-with-sites", "-site")

    return match_tag.replace("-match", "-site")


def copy_element(elem):
    if elem.tag == "cigar-alignment":
        return None

    new_elem = ET.Element(elem.tag, dict(elem.attrib))
    new_elem.text = elem.text
    new_elem.tail = elem.tail

    if "type" in new_elem.attrib:
        new_elem.attrib["type"] = new_elem.attrib["type"].upper()

    if elem.tag == "signature-library-release":
        lib = new_elem.attrib.get("library")
        if lib in LIBRARY_NAME_MAP:
            new_elem.attrib["library"] = LIBRARY_NAME_MAP[lib]

    for child in elem:
        copied = copy_element(child)
        if copied is not None:
            new_elem.append(copied)

    return new_elem


def convert_signature(sig_elem):

    # copy attributes
    new_sig = ET.Element("signature", dict(sig_elem.attrib))

    if "type" in new_sig.attrib:
        new_sig.attrib["type"] = new_sig.attrib["type"].upper()

    entries = []
    models = None
    release = None

    for child in sig_elem:

        tag = child.tag

        if tag == "entry":
            copied = copy_element(child)
            if copied is not None:
                entries.append(copied)

        elif tag == "models":
            models = copy_element(child)

        elif tag == "signature-library-release":
            copied = copy_element(child)
            if copied is not None:
                release = copied

    # enforce XSD order

    for e in entries:
        new_sig.append(e)

    if models is not None:
        new_sig.append(models)

    if release is not None:
        new_sig.append(release)

    return new_sig


def convert_match_xml(match_elem):
    source = match_elem.get("source")
    match_tag = MATCH_TAG_MAP.get(source, "hmmer3-match")

    match_attrib = {}
    if "evalue" in match_elem.attrib:
        match_attrib["evalue"] = match_elem.attrib["evalue"]
    if "score" in match_elem.attrib:
        match_attrib["score"] = match_elem.attrib["score"]
    if "name" in match_elem.attrib:
        match_attrib["name"] = match_elem.attrib["name"]
    if "ac" in match_elem.attrib:
        match_attrib["ac"] = match_elem.attrib["ac"]
    if "graft-point" in match_elem.attrib:
        match_attrib["graft-point"] = match_elem.attrib["graft-point"]

    new_match = ET.Element(match_tag, match_attrib)

    # propagate PRINTS graphscan attribute
    if match_tag == "fingerprints-match":
        graphscan = match_elem.attrib.get("graphscan")
        if graphscan:
            new_match.attrib["graphscan"] = graphscan

    for child in match_elem:

        if child.tag == "signature":
            new_match.append(convert_signature(child))

        if match_tag == "panther-match" and "ac" not in new_match.attrib:
            sig = match_elem.find("signature")
            if sig is not None and "ac" in sig.attrib:
                new_match.attrib["ac"] = sig.attrib["ac"]

        elif child.tag == "model-ac":
            copied = copy_element(child)
            if copied is not None:
                new_match.append(copied)

        elif child.tag == "locations":
            new_locations = ET.Element("locations")

            if match_tag == "superfamilyhmmer3-match":
                first_loc = child.find("location")
                if first_loc is not None and "evalue" in first_loc.attrib:
                    new_match.attrib["evalue"] = first_loc.attrib["evalue"]

            for loc in child:
                loc_attrib = dict(loc.attrib)

                # Remove evalue from location for SUPERFAMILY
                if match_tag == "superfamilyhmmer3-match" and "evalue" in loc_attrib:
                    loc_attrib.pop("evalue")
                if match_tag == "patternscan-match":
                    loc_attrib.setdefault("level", "NONE")
                if match_tag == "panther-match":
                    loc_attrib.pop("evalue", None)
                    loc_attrib.pop("score", None)
                if source == "PIRSR":
                    loc_attrib.setdefault("hmm-bounds", "COMPLETE")
                if match_tag in ["hmmer3-match", "funfamhmmer3-match"]:
                    if source == "NCBIFAM":
                        loc_attrib.setdefault("post-processed", "false")
                    else:
                        loc_attrib.setdefault("post-processed", "true")
                if source == "SFLD":
                    loc_attrib.setdefault("hmm-bounds", "INCOMPLETE")

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

                            site_locations = None
                            for site_child in site:
                                if site_child.tag == "site-locations":
                                    site_locations = copy_element(site_child)

                            if site_locations is not None:
                                new_site.append(site_locations)

                            site_container.append(new_site)

                        new_loc.append(site_container)

                    else:
                        copied = copy_element(sub)
                        if copied is not None:
                            new_loc.append(copied)

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
        converted_matches = []

        if matches is not None:
            for match in matches.findall("match"):
                new_match = convert_match_xml(match)
                converted_matches.append(new_match)

        # sort according to IPS5 schema order
        order_index = {name: i for i, name in enumerate(MATCH_ORDER)}

        converted_matches.sort(
            key=lambda m: order_index.get(m.tag, 999)
        )

        for m in converted_matches:
            matches_container.append(m)

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
