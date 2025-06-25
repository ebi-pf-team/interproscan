#!/usr/bin/env python3

import argparse
import os
import subprocess as sp
import sys


def main():
    parser = argparse.ArgumentParser(description="""\
Filter and search FunFams with hmmsearch.
    """)
    parser.add_argument("seqfile", 
                        help="fasta file")
    parser.add_argument("tabfile",
                        help="file of assigned CATH superfamilies")
    parser.add_argument("hmmdir",
                        help="FunFam data directory")
    parser.add_argument("hmmsearch_output",
                        metavar="hmmsearch-output",
                        help="hmmsearch output file")
    parser.add_argument("cath_hits_output", metavar="cath-hits-output",
                        help="cath-resolve-hits output file")
    parser.add_argument("--hmmsearch", metavar="FILE",
                        default="hmmsearch",
                        help="location of the hmmsearch binary")
    parser.add_argument("--hmmsearch-flags", metavar="FLAGS",
                        default="",
                        help="additional hmmsearch options")
    parser.add_argument("--cath-resolve-hits", metavar="FILE",
                        default="cath-resolve-hits",
                        help="location of the cath-resolve-hits binary")
    parser.add_argument("--cath-resolve-hits-flags", metavar="FLAGS",
                        default="",
                        help="additional cath-resolve-hits options")
    args = parser.parse_args()

    seqfile = args.seqfile
    tabfile = args.tabfile
    hmmdir = args.hmmdir
    hmmsearch_output = args.hmmsearch_output
    cath_hits_output = args.cath_hits_output
    hmmsearch_binary = args.hmmsearch
    hmmsearch_flags = args.hmmsearch_flags.strip('"\'').split()
    cath_resolve_binary = args.cath_resolve_hits
    cath_resolve_flags = args.cath_resolve_hits_flags.strip('"\'').split()
   
    families = {}
    with open(tabfile, "rt") as fh:
        for line in fh:
            if line[0] != "#":
                cath_family_id = line.split()[1]

                if cath_family_id in families:
                    continue

                parts = cath_family_id.split('.')
                hmmfile = os.path.join(hmmdir, *parts) + ".hmm"

                if os.path.isfile(hmmfile):
                    families[cath_family_id] = hmmfile

    # Run hmmsearch
    hmmsearch_cmd = [hmmsearch_binary] + hmmsearch_flags
    with open(hmmsearch_output, "wt") as fh:
        for i, hmmfile in enumerate(families.values()):
            # print(f"{i+1:>10} / {len(families)}", file=sys.stderr)

            cmd = hmmsearch_cmd + [hmmfile, seqfile]
            exit_code = sp.call(cmd, stderr=sp.DEVNULL, stdout=fh)

            if exit_code != 0:
                raise RuntimeError("error while running hmmsearch: "
                                   "{}".format(' '.join(cmd)))

    # Run cath-resolve-hits
    cath_resolve_cmd = [cath_resolve_binary] + cath_resolve_flags
    cath_resolve_cmd += ["--input-format=hmmsearch_out", hmmsearch_output]
    
    with open(cath_hits_output, "wt") as fh:
        exit_code = sp.call(cmd, stderr=sp.DEVNULL, stdout=fh)
        if exit_code != 0:
            raise RuntimeError("error while running cath-resolve-hits: "
                                "{}".format(' '.join(cath_resolve_cmd)))


if __name__ == '__main__':
    main()
