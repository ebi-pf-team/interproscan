#!/usr/bin/env python3

import argparse
import glob
import os
import subprocess as sp
import sys


PROPERTIES="interproscan.properties"


def load_properties(file, properties):
    with open(file, "rt") as fh:
        for line in map(str.strip, fh):
            if not line or line[0] == "#":
                continue

            fields = line.split("=", 1)
            if len(fields) == 2:
                key = fields[0].strip()
                value = fields[1].strip()
                properties[key] = value

    try:
        bin_dir = properties["bin.directory"]
        data_dir = properties["data.directory"]
    except KeyError:
        return

    for key, value in properties.items():
        if "${bin.directory}" in value:
            value = value.replace("${bin.directory}", bin_dir)

        if "${data.directory}" in value:
            value = value.replace("${data.directory}", data_dir)

        properties[key] = value


def is_ready(hmmfile):
    for ext in [".h3m", ".h3i", ".h3f", ".h3p"]:
        if not os.path.isfile(hmmfile + ext):
            return False

    return True


def hmmpress(binary, force, database):
    if force:
        cmd = [binary, "-f", database]
    else:
        cmd = [binary, database]

    proc = sp.Popen(cmd, stdout=sp.PIPE, stderr=sp.PIPE)
    outs, errs = proc.communicate()

    if proc.returncode != 0:
        sys.stderr.write(errs.decode())
        sys.exit(1)


def realpath(path):
    return os.path.realpath(os.path.expanduser(path))


def main():
    parser = argparse.ArgumentParser(description="index HMM databases")
    parser.add_argument("properties", help="InterProScan properties file")
    parser.add_argument("-f", "--force", action="store_true",
                        help="re-index all HMM databases (default: off)")
    args = parser.parse_args()

    properties = {}
    load_properties(PROPERTIES, properties)

    if realpath(args.properties) != realpath(PROPERTIES):
        load_properties(args.properties, properties)

    ignore = {"binary.tmhmm.path", "funfam.hmm.path", "smart.hmm.path"}
    for key, path in properties.items():
        if "hmm.path" in key and key not in ignore:
            bin_key = "binary.hmmer3.path"

            if os.path.isfile(path):
                hmmfiles = [path]
            else:
                hmmfiles = glob.iglob(os.path.join(path, "**", "*.hmm"),
                                      recursive=True)

            for hmmfile in hmmfiles:
                if args.force or not is_ready(hmmfile):
                    try:
                        bin_path = os.path.join(properties[bin_key], "hmmpress")
                    except KeyError:
                        continue
                    else:
                        hmmpress(bin_path, args.force, hmmfile)


if __name__ == '__main__':
    main()
