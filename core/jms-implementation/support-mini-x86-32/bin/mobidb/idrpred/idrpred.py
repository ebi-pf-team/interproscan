import os
import sys
from argparse import ArgumentParser, FileType
from tempfile import gettempdir
from concurrent.futures import ThreadPoolExecutor, as_completed

from . import predict


def parse_fasta(file):
    seq_id = sequence = ""

    with file as fh:
        for line in map(str.rstrip, fh):
            if line[0] == ">":
                if seq_id and sequence:
                    yield seq_id, sequence.upper()
                seq_id = line[1:].split()[0]
                sequence = ""
            elif line:
                sequence += line

    if seq_id and sequence:
        yield seq_id, sequence.upper()


def run(file: str, bindir: str, threads: int, **kwargs):
    if threads > 1:
        with ThreadPoolExecutor(max_workers=threads) as executor:
            fs = {}
            for seq_id, sequence in parse_fasta(file):
                f = executor.submit(predict, seq_id, sequence, bindir,
                                    **kwargs)
                fs[f] = seq_id

                if len(fs) == 1000:
                    for f in as_completed(fs):
                        seq_id = fs[f]
                        regions = f.result()
                        yield seq_id, regions

                    fs.clear()

            for f in as_completed(fs):
                seq_id = fs[f]
                regions = f.result()
                yield seq_id, regions
    else:
        for seq_id, sequence in parse_fasta(file):
            regions = predict(seq_id, sequence, bindir, **kwargs)
            yield seq_id, regions


def main():
    script = os.path.relpath(__file__)

    description = "A consensus-based predictor of intrinsically " \
                  "disordered regions in proteins."
    parser = ArgumentParser(prog=f"python {os.path.basename(script)}",
                            description=description)
    parser.add_argument("infile", nargs="?", default="-",
                        type=FileType("rt", encoding="UTF-8"),
                        help="A file of sequences in FASTA format.")
    parser.add_argument("outfile", nargs="?", default="-",
                        type=FileType("wt", encoding="UTF-8"),
                        help="Write the output of infile to outfile.")
    parser.add_argument("--force", action="store_true", default=False,
                        help="Generate consensus as long as at least "
                             "one predictor did not fail.")
    parser.add_argument("--skip-features", dest="find_features",
                        action="store_false", default=True,
                        help="Do not indentify sequence features, "
                             "such as domains of low complexity.")
    parser.add_argument("--round", action="store_true", default=False,
                        help="Round scores before threshold checks, "
                             "like MobiDB-lite.")
    parser.add_argument("--tempdir", metavar="DIRECTORY", default=gettempdir(),
                        help=(f"Directory to use for temporary files, "
                              f"default: {gettempdir()}."))
    parser.add_argument("--threads", type=int, default=1,
                        help="Number of parallel threads, default: 1.")
    args = parser.parse_args()

    root = os.path.abspath(os.path.dirname(script))
    bindir = os.path.join(root, "bin")

    with args.outfile as outfile:
        for seq_id, regions in run(args.infile, bindir, args.threads,
                                   force=args.force,
                                   round=args.round,
                                   find_features=args.find_features,
                                   merge_features=True,
                                   keep_non_idr_features=False,
                                   tempdir=args.tempdir):
            if regions is None:
                sys.stderr.write(f"error in {seq_id}\n")
                continue

            for start, end, feature in regions:
                outfile.write(f"{seq_id}\t{start}\t{end}\t{feature}\n")


if __name__ == "__main__":
    main()
