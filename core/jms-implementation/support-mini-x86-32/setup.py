#!/usr/bin/env python3

import argparse
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

    bin_dir = properties["bin.directory"]
    data_dir = properties["data.directory"]

    for key, value in properties.items():
        if "${bin.directory}" in value:
            value = value.replace("${bin.directory}", bin_dir)

        if "${data.directory}" in value:
            value = value.replace("${data.directory}", data_dir)

        properties[key] = value

    return properties


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

    ignore = {"binary.tmhmm.path", "smart.hmm.path"}
    for key, path in properties.items():
        if "hmm.path" in key and key not in ignore:
            if args.force or not is_ready(path):
                if key.startswith(("sfld", "superfamily")):
                    bin_key = "binary.hmmer3.path"
                else:
                    bin_key = "binary.hmmer33.path"

                bin_path = os.path.join(properties[bin_key], "hmmpress")
                hmmpress(bin_path, args.force, path)


if __name__ == '__main__':
    main()
