#!/usr/bin/env python3

# Very basic parser for Stockholm files to extract active site information for SFLD/i5
# N.B. makes some wild assumptions on the format/order of the file (which is officially
# more flexible than what's assumed here), including:
# * that RF comes before the rest of the column wise annotation
# * alignments are written on a single line

# Copyright (c) EMBL-EBI 2016.


from datetime import datetime, date, time
import sys
import re

def write_header(fn, f):
    f.write("## MSA feature annotation file\n")
    f.write("# Format version: 1.1\n")
    f.write("# MSA file: " + fn + "\n")
    now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    f.write("# Date " + now + "\n")

def parse_ft_line(s):
    ft_re = re.compile('#=GF FT\s+(\d+)\s*(.*)?')
    m = ft_re.match(s)
    if not m:
        return
    if len(m.groups()) > 1:
        return (int(m.group(1)), m.group(2))
    else:
        print(s)
        return (m.group(2), "")


def parse_msa(lines, f):
    if lines[0] != '# STOCKHOLM 1.0':
        return False
    sites = []
    gc_tags = set(['RF'])
    rf_line = None
    features = []
    ft_coords = []
    for line in lines:
        if line[:7] == '#=GF AC':
            ac = line.split()[-1]
        elif line[:7] == '#=GF FT':
            (pos, desc) = parse_ft_line(line)
            sites.append([pos, desc])
            ft_coords.append(pos)
        elif line[:7] == '#=GC RF':
            rf_line = line.split()[-1]
        elif line[:4] == '#=GC':
            if not rf_line:
                print("ERROR: found line " + line.rstrip() + " without #=GC RF")
                sys.exit(1)
            gc_line = line.split()[-1]
            if len(gc_line) == 0 or len(gc_line) != len(rf_line):
                print("ERROR #=GC lines disagree:\n\t" + gc_line + "\n\t" + rf_line)
                sys.exit(1)
            pos = 0
            gc_coords = []
            features.append("")
            for i in range(len(gc_line)):
                if rf_line[i] != '.':
                    pos += 1
                if gc_line[i] != '.':
                    gc_coords.append(pos)
                    features[-1] += gc_line[i]
            if sorted(gc_coords) != sorted(ft_coords):
                print("Coordinates in #=GF FT entries do not match with #=GC lines")
                sys.exit(1)
    f.write("ACC %s %d %d\n" % (ac, len(sites), len(features)))
    for site in sorted(sites, key = lambda x: int(x[0])):
        f.write("SITE %d %s\n" % (site[0], site[1]))
    for fs in features:
        f.write("FEATURE " + fs + "\n")
 
    return True

if len(sys.argv) != 3:
    print("Usage")
    sys.exit(1)

lines = []

with open(sys.argv[1], 'r') as msaf, open(sys.argv[2], 'w') as annot:
    write_header(sys.argv[1], annot)
    for line in msaf:
        line = line.rstrip()
        if len(line) == 0:
            continue
        if line == '//':
            parse_msa(lines, annot)
            lines = []
        else:
            lines.append(line)
