#!/usr/bin/env python

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
    parser.add_argument("-o", "--output", default="-", 
                        help="cath-resolve-hits output file")
    parser.add_argument("--domtblout", 
                        help="output hmmsearch domain hits file")
    parser.add_argument("--hmmsearch", default="hmmsearch",
                        help="location of the hmmsearch binary")
    parser.add_argument("--hmmsearch-flags", default="",
                        help="additional hmmsearch options")
    parser.add_argument("--cath-resolve-hits", default="cath-resolve-hits",
                        help="location of the cath-resolve-hits binary")
    parser.add_argument("--cath-resolve-hits-flags", default="",
                        help="additional cath-resolve-hits options")
    parser.add_argument("-T", dest="tempdir", metavar="DIR",
                        help="create temporary files in DIR",
                        default=tempfile.gettempdir())
    args = parser.parse_args()

    seqfile = args.seqfile
    tabfile = args.tabfile
    hmmdir = args.hmmdir
    output = args.output
    domtblout = args.domtblout
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
    tmpdomtblout = os.path.join(tempdir, "domtblout.tmp")
    tmpresolved = os.path.join(tempdir, "resolved.tmp")

    # Base command for hmmsearch
    hmmsearch_cmd = [hmmsearch_binary] + hmmsearch_flags
    hmmsearch_cmd += ["-o", tmpoutput]
    hmmsearch_cmd += ["--domtblout", tmpdomtblout]

    # Base command for cath-resolve-hits
    cath_resolve_cmd = [cath_resolve_binary] + cath_resolve_flags
    cath_resolve_cmd += ["--input-format=hmmsearch_out", 
                         "--hits-text-to-file", tmpresolved]

    # cath-resolve-hits output
    fh1 = sys.stdout if output == "-" else open(output, "wt")

    # Save the per-domain output from hmmsearch
    fh2 = open(domtblout, "wt") if domtblout else None

    try:
        for i, hmmfile in enumerate(families.values()):
            # sys.stderr.write(f"{i+1:>10} / {len(families)}\n")

            # Search
            cmd = hmmsearch_cmd + [hmmfile, seqfile]
            exit_code = sp.call(cmd, stderr=sp.DEVNULL, stdout=sp.DEVNULL)

            if exit_code != 0:
                raise RuntimeError("error while running hmmsearch: "
                                   "{}".format(' '.join(cmd)))

            if fh2:
                with open(tmpdomtblout, "rt") as fh3:
                    fh2.write(fh3.read())

            # Resolve
            cmd = cath_resolve_cmd + [tmpoutput]
            exit_code = sp.call(cmd, stderr=sp.DEVNULL, stdout=sp.DEVNULL)

            if exit_code != 0:
                raise RuntimeError("error while running cath-resolve-hits: "
                                   "{}".format(' '.join(cmd)))

            with open(tmpresolved, "rt") as fh3:
                for line in fh3:
                    if line[0] != '#':
                        # query-id match-id score boundaries resolved aligned-regions cond-evalue indp-evalue
                        fh1.write(line)
    finally:
        if fh1 is not sys.stdout:
            fh1.close()

        if fh2:
            fh2.close()

        shutil.rmtree(tempdir)


if __name__ == '__main__':
    main()
