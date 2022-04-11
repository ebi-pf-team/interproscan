#!/usr/bin/env python3

import argparse
import os
import shutil
import subprocess as sp
import sys
import tempfile


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
    parser.add_argument("-T", metavar="DIR",
                        dest="tempdir", 
                        default=tempfile.gettempdir(),
                        help="create temporary files in DIR")
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
    tempdir = tempfile.mkdtemp(dir=args.tempdir)
   
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

    tmpoutput = os.path.join(tempdir, "output.tmp")
    tmpresolved = os.path.join(tempdir, "resolved.tmp")

    # Base command for hmmsearch
    hmmsearch_cmd = [hmmsearch_binary] + hmmsearch_flags
    hmmsearch_cmd += ["-o", tmpoutput]

    # Base command for cath-resolve-hits
    cath_resolve_cmd = [cath_resolve_binary] + cath_resolve_flags
    cath_resolve_cmd += ["--input-format=hmmsearch_out", 
                         "--hits-text-to-file", tmpresolved]

    # hmmsearch output
    fh1 = open(hmmsearch_output, "wt")

    # cath-resolve-hits output
    fh2 = open(cath_hits_output, "wt")

    try:
        for i, hmmfile in enumerate(families.values()):
            # sys.stderr.write(f"{i+1:>10} / {len(families)}\n")

            # Search
            cmd = hmmsearch_cmd + [hmmfile, seqfile]
            exit_code = sp.call(cmd, stderr=sp.DEVNULL, stdout=sp.DEVNULL)

            if exit_code != 0:
                raise RuntimeError("error while running hmmsearch: "
                                   "{}".format(' '.join(cmd)))

            # Resolve
            cmd = cath_resolve_cmd + [tmpoutput]
            exit_code = sp.call(cmd, stderr=sp.DEVNULL, stdout=sp.DEVNULL)

            if exit_code != 0:
                raise RuntimeError("error while running cath-resolve-hits: "
                                   "{}".format(' '.join(cmd)))

            with open(tmpoutput, "rt") as fh3:
                fh1.write(fh3.read())

            with open(tmpresolved, "rt") as fh3:
                for line in fh3:
                    if line[0] != '#':
                        # query-id match-id score boundaries resolved aligned-regions cond-evalue indp-evalue
                        fh2.write(line)
    finally:
        fh1.close()
        fh2.close()
        shutil.rmtree(tempdir)


if __name__ == '__main__':
    main()
