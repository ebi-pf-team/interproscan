#!/usr/bin/env python

import argparse
import json
import os
import re
import shutil
import subprocess as sp
import sys
import tempfile

from Bio import Phylo
from Bio.Phylo import NewickIO


class ReMatcher:
    def __init__(self, matchstring):
        self.matchstring = matchstring

    def match(self, regexp):
        self.rematch = re.match(regexp, self.matchstring)
        return bool(self.rematch)

    def group(self, i):
        return self.rematch.group(i)


def process_matches_epang(matches, datadir, tempdir, binary=None, threads=1):
    results = {}

    for pthr in matches:
        query_fasta_file = generate_fasta_for_panthr(pthr, matches[pthr],
                                                     datadir, tempdir)
        if not query_fasta_file:
            # No sequence to graft
            continue

        # print out the pthr Family matches
        for query_id in matches[pthr]:
            results[query_id] = (
                    query_id + "\t"
                    + pthr + "\t"
                    + matches[pthr][query_id]['score'][0] + "\t"
                    + matches[pthr][query_id]['evalue'][0] + "\t"
                    + matches[pthr][query_id]['domscore'][0] + "\t"
                    + matches[pthr][query_id]['domevalue'][0] + "\t"
                    + matches[pthr][query_id]['hmmstart'][0] + "\t"
                    + matches[pthr][query_id]['hmmend'][0] + "\t"
                    + matches[pthr][query_id]['alifrom'][0] + "\t"
                    + matches[pthr][query_id]['alito'][0] + "\t"
                    + matches[pthr][query_id]['envfrom'][0] + "\t"
                    + matches[pthr][query_id]['envto'][0] + "\t"
                    + "-" + "\n"
            )

        result_tree = _run_epang(pthr, query_fasta_file, datadir, tempdir,
                                 binary=binary, threads=threads)
        if not result_tree:
            # EPA-ng error (e.g tree cannot be converted to unrooted)
            continue

        for result in process_tree(pthr, result_tree, matches[pthr], datadir):
            query_id = result[0]
            results[query_id] = result

    return list(results.values())


def generate_fasta_for_panthr(pthr, matches, datadir, tempdir):

    pthr_align_length = align_length(pthr, datadir)

    query_fasta = ''

    for query_id in matches:
        querymsf = _querymsf(matches[query_id], pthr_align_length)

        # print fasta header
        query_fasta += '>' + query_id + '\n'

        # and the body lines
        for i in range(0, len(querymsf), 80):
            query_fasta += re.sub(r"[UO]", r"X", querymsf[i:i + 80],
                                  flags=re.I)
            query_fasta += '\n'

    if not query_fasta:
        return None

    fasta_out_file = os.path.join(tempdir, pthr + '_query.fasta')
    with open(fasta_out_file, 'wt') as outfile:
        outfile.write(query_fasta)

    return fasta_out_file


def stringify(query_id):
    # stringify query_id

    query_id = re.sub(r'[^\w]', '_', query_id)

    return query_id


def _querymsf(match_data, pthr_align_length):
    # matchdata contains: hmmstart, hmmend, hmmalign and matchalign,
    # as arrays (multiple modules possible)

    # N-terminaly padd the sequence
    # position 1 until start is filled with '-'

    querymsf = ((int(match_data['hmmstart'][0]) - 1) * '-')

    # loop the elements/domains
    for i in range(0, len(match_data['matchalign'])):

        # if this is not the first element, fill in the gap between the hits
        if i > 0:
            start = int(match_data['hmmstart'][i])
            end = int(match_data['hmmend'][i-1])
            # This bridges the query_id gap between the hits
            querymsf += (start - end - 1) * '-'

        # extract the query string
        matchalign = match_data['matchalign'][i]
        hmmalign = match_data['hmmalign'][i]

        # loop the sequence
        for j in range(0, len(hmmalign)):
            # hmm insert state
            if hmmalign[j:j+1] == ".":
                continue

            querymsf += matchalign[j:j+1]

    # C-terminaly padd the sequence
    # get the end of the last element/domain
    last_end = int(match_data['hmmend'][-1])
    # and padd out to fill the msf length
    querymsf += (pthr_align_length - last_end) * '-'

    # error check (is this required?)
    if len(querymsf) != pthr_align_length:
        # then something is wrong
        sys.stderr.write("Error: length of query MSF longer than expected "
                         "PANTHER alignment length: expected {}, "
                         "got {}.".format(pthr_align_length, len(querymsf)))
        sys.exit(1)

    return querymsf.upper()


def _run_epang(pthr, query_fasta, datadir, tempdir, binary=None, threads=1):
    msfdir = os.path.join(datadir, "Tree_MSF")
    referece_fasta = os.path.join(msfdir, "{}.AN.fasta".format(pthr))
    bifurnewick_in = os.path.join(msfdir, "{}.bifurcate.newick".format(pthr))
    outdir = os.path.join(tempdir, "{}_epang".format(pthr))
    os.mkdir(outdir)

    args = [binary or "epa-ng",
            "-G", "0.05",
            "-m", "WAG",
            "-T", str(threads),
            "-t", bifurnewick_in,
            "-s", referece_fasta,
            "-q", query_fasta,
            "-w", outdir]

    exit_code = sp.call(args, stderr=sp.DEVNULL, stdout=sp.DEVNULL)
    jplace_file = os.path.join(outdir, "epa_result.jplace")

    if exit_code == 0 and os.path.isfile(jplace_file):
        return jplace_file

    return None


def process_tree(pthr, result_tree, pthr_matches, datadir):
    with open(result_tree, "rt") as classification:
        classification_json = json.load(classification)

    tree_string = classification_json['tree']
    matches = re.findall(r'AN(\d+):\d+\.\d+\{(\d+)\}', tree_string)

    an_label = {}
    for [an, r] in matches:
        an_label['AN' + an] = 'R' + r
        an_label['R' + r] = 'AN' + an

    newick_string = re.sub(
        r'(AN\d+)?\:\d+\.\d+{(\d+)}', r'R\g<2>', tree_string)

    newick_string = re.sub(
        r'AN\d+', r'', newick_string)
    newick_string = re.sub(
        r'BI\d+', r'', newick_string)
    mytree = Phylo.read(NewickIO.StringIO(newick_string), 'newick')
    results_pthr = []

    for placement in classification_json['placements']:
        query_id = placement['n'][0]
        child_ids = []
        ter = []

        for maploc in placement['p']:
            rloc = 'R' + str(maploc[0])
            clade_obj = mytree.find_clades(rloc)

            node = next(clade_obj)
            ter.extend(node.get_terminals())
            comonancestor = mytree.common_ancestor(ter)

            for leaf in comonancestor.get_terminals():
                child_ids.append(an_label[leaf.name])

        common_an = _commonancestor(pthr, child_ids, datadir)

        annot_file = os.path.join(datadir, 'PAINT_Annotations', pthr + '.json')
        with open(annot_file, 'rt') as fh:
            pthrsf, _, _, _ = json.load(fh)[common_an]

        results_pthr.append(
            query_id + "\t"
            + (pthrsf or pthr) + "\t"
            + pthr_matches[query_id]['score'][0] + "\t"
            + pthr_matches[query_id]['evalue'][0] + "\t"
            + pthr_matches[query_id]['domscore'][0] + "\t"
            + pthr_matches[query_id]['domevalue'][0] + "\t"
            + pthr_matches[query_id]['hmmstart'][0] + "\t"
            + pthr_matches[query_id]['hmmend'][0] + "\t"
            + pthr_matches[query_id]['alifrom'][0] + "\t"
            + pthr_matches[query_id]['alito'][0] + "\t"
            + pthr_matches[query_id]['envfrom'][0] + "\t"
            + pthr_matches[query_id]['envto'][0] + "\t"
            + common_an + "\n"
        )

    return results_pthr


def _commonancestor(pathr, map_ans, datadir):
    newick_in = os.path.join(datadir, "Tree_MSF", "{}.newick".format(pathr))
    newtree = Phylo.read(newick_in, "newick")
    commonancestor = newtree.common_ancestor(map_ans)
    return str(commonancestor) if commonancestor else "root"


def parsehmmsearch(hmmer_out):
    match_store = {}
    score_store = {}
    evalue_store = {}
    store_align = False
    store_domain = []
    matches = {}

    with open(hmmer_out, "rt") as fp:
        matchpthr = None
        query_id = None

        line = fp.readline()
        while line:
            m = ReMatcher(line)

            if line.startswith('#') or not line.strip():
                line = fp.readline()
                continue
            elif m.match(r'\AQuery:\s+(PTHR[0-9]+)'):
                matchpthr = m.group(1)

                fp.readline()
                fp.readline()
                fp.readline()
                fp.readline()
                line = fp.readline()

                inclusion = True
                while line.strip():
                    m = ReMatcher(line)
                    if m.match(r'\s+------\sinclusion\sthreshold'):
                        inclusion = False
                        line = fp.readline()
                        continue

                    if inclusion:
                        score_array = line.split()
                        score_store[stringify(score_array[8])] = float(score_array[1])
                        evalue_store[stringify(score_array[8])] = float(score_array[0])

                    line = fp.readline()

            elif m.match(r'\A>>\s+(\S+)'):
                query_id = m.group(1)
                query_id = stringify(query_id)
                store_domain = []

                if query_id not in score_store:
                    store_align = False
                    line = fp.readline()
                    continue

                stored_score = 0
                if query_id in match_store:
                    stored_score = match_store[query_id]['score']

                if float(score_store[query_id]) > float(stored_score):
                    store_align = True

                    line = fp.readline()

                    m = ReMatcher(line)
                    if m.match(r'\s+\[No individual domains that satisfy reporting thresholds'):
                        store_align = False
                        line = fp.readline()
                        continue

                    current_match = {
                        'panther_id': matchpthr,
                        'score': score_store[query_id],
                        'align': {
                            'hmmalign': [],
                            'matchalign': [],
                            'hmmstart': [],
                            'hmmend': [],
                            'score': [],
                            'evalue': [],
                            'domscore': [],
                            'domevalue': [],
                            'alifrom': [],
                            'alito': [],
                            'envfrom': [],
                            'envto': [],
                            'acc': []
                        }
                    }

                    fp.readline()
                    line = fp.readline()
                    while line.strip():
                        domain_info = line.split()

                        if len(domain_info) != 16:
                            sys.stderr.write(
                                "domain info length is {}, "
                                "expected 16 "
                                "for {}.\n".format(len(domain_info), query_id))
                            sys.exit(1)

                        dom_num = domain_info[0]
                        dom_state = domain_info[1]

                        if dom_state == '!':
                            current_match['align']['score'].append(str(score_store[query_id]))
                            current_match['align']['evalue'].append(str(evalue_store[query_id]))
                            current_match['align']['domscore'].append(domain_info[2])
                            current_match['align']['domevalue'].append(domain_info[5])
                            current_match['align']['hmmstart'].append(domain_info[6])
                            current_match['align']['hmmend'].append(domain_info[7])
                            current_match['align']['alifrom'].append(domain_info[9])
                            current_match['align']['alito'].append(domain_info[10])
                            current_match['align']['envfrom'].append(domain_info[12])
                            current_match['align']['envto'].append(domain_info[13])
                            current_match['align']['acc'].append(domain_info[15])
                            store_domain.append(dom_num)

                        line = fp.readline()

                    if len(current_match['align']['hmmend']) == 0:
                        store_align = False

            elif m.match(r'\s+==\sdomain\s(\d+)') and store_align:
                domain_num = m.group(1)

                if domain_num in store_domain:
                    line = fp.readline()
                    hmmalign_array = line.split()

                    hmmalign_model = hmmalign_array[0]
                    hmmalign_model = re.sub(r'\..+', '', hmmalign_model)
                    hmmalign_seq = hmmalign_array[2]

                    line = fp.readline()
                    line = fp.readline()

                    matchalign_array = line.split()

                    matchalign_query = matchalign_array[0]
                    matchalign_query = stringify(matchalign_query)

                    matchlign_seq = matchalign_array[2]

                    if matchpthr == hmmalign_model and query_id == matchalign_query:
                        if len(current_match['align']['hmmalign']) >= len(current_match['align']['hmmstart']):
                            sys.stderr.write("Trying to add alignment sequence"
                                             " without additional data.\n")
                            sys.exit(1)

                        current_match['align']['hmmalign'].append(hmmalign_seq)
                        current_match['align']['matchalign'].append(matchlign_seq)

                    match_store[query_id] = current_match

                line = fp.readline()

            elif m.match(r'\A\/\/'):
                score_store = {}
                evalue_store = {}
                store_domain = []
                store_align = False

            line = fp.readline()

    for query_id in match_store:
        panther_id = match_store[query_id]['panther_id']

        if panther_id not in matches:
            matches[panther_id] = {}

        if not match_store[query_id]['align']['hmmstart']:
            sys.exit(1)

        matches[panther_id][query_id] = match_store[query_id]['align']

    return matches


def filter_best_domain(matches):
    for panther in matches:
        for query in matches[panther]:

            while len(matches[panther][query]['domscore']) > 1:
                if matches[panther][query]['domscore'][0] > matches[panther][query]['domscore'][1]:
                    for key in matches[panther][query]:
                        del matches[panther][query][key][1]
                else:
                    for key in matches[panther][query]:
                        del matches[panther][query][key][0]

    return matches


def filter_evalue_cutoff(matches, cutoff):
    for panther in matches:
        for query in dict(matches[panther]):
            # remove queries with e-value greater than the cutoff
            if float(matches[panther][query]['evalue'][0]) > cutoff:
                del matches[panther][query]

    return matches


def align_length(pthr, datadir):
    pthr_fasta_file = os.path.join(datadir,
                                   "Tree_MSF",
                                   "{}.AN.fasta".format(pthr))

    with open(pthr_fasta_file, "rt") as f:
        file = f.read().split('>')
        first_seq = file[1]
        first_seq = re.sub(r'\A[^\n]+', '', first_seq)
        first_seq = re.sub(r'\n', '', first_seq)
        seq_length = len(first_seq)

    return seq_length


def prepare(args):
    datadir = args.datadir

    sys.stderr.write("Loading PAINT annotations\n")
    paintdir = os.path.join(datadir, "PAINT_Annotations")
    paintfile = os.path.join(paintdir, "PAINT_Annotatations_TOTAL.txt")

    families = {}
    with open(paintfile, "rt") as fh:
        for i, line in enumerate(fh):
            fam_an_id, annotations, graft_point = line.rstrip().split("\t")
            fam_id, node_id = fam_an_id.split(':')

            try:
                fam = families[fam_id]
            except KeyError:
                fam = families[fam_id] = {}

            go_terms = []
            protein_class = subfam_id = None
            for annotation in re.split(r"\s+|;", annotations):
                if re.fullmatch(r"PTHR\d+:(SF\d+)", annotation):
                    subfam_id = annotation
                elif re.fullmatch(r"GO:\d{7}", annotation):
                    go_terms.append(annotation)
                elif re.fullmatch(r"PC\d{5}", annotation):
                    protein_class = annotation

            fam[node_id] = (subfam_id, go_terms, protein_class, graft_point)

            if (i + 1) % 100000 == 0:
                sys.stderr.write("\t{:,} lines processed\n".format(i+1))

    for fam_id, obj in families.items():
        with open(os.path.join(paintdir, fam_id + ".json"), "wt") as fh:
            json.dump(obj, fh)

    sys.stderr.write("Updating sequences\n")
    msfdir = os.path.join(datadir, "Tree_MSF")
    for name in os.listdir(msfdir):
        if name.endswith(".fasta"):
            src = os.path.join(msfdir, name)
            dst = src + ".tmp"
            with open(src, "rt") as fh1, open(dst, "wt") as fh2:
                for line in fh1:
                    if line[0] == ">":
                        fh2.write(line)
                    else:
                        # Replace Selenocysteine (U) and Pyrrolysine (O) AA
                        # by the undetermined AA character (X).
                        fh2.write(re.sub(r"[UO]", r"X", line.upper()))

            os.unlink(src)
            os.rename(dst, src)


def run(args):
    if not os.path.isfile(args.fasta):
        sys.stderr.write("Error: {}: "
                         "no such file.\n".format(args.fasta))
        sys.exit(1)
    elif not os.path.isfile(args.hmmsearch):
        sys.stderr.write("Error: {}: "
                         "no such file.\n".format(args.hmmsearch))
        sys.exit(1)
    elif not os.path.isdir(args.datadir):
        sys.stderr.write("Error: {}: "
                         "no such directory.\n".format(args.datadir))
        sys.exit(1)

    matches = parsehmmsearch(args.hmmsearch)
    matches = filter_best_domain(matches)

    if args.evalue:
        matches = filter_evalue_cutoff(matches, args.evalue)

    os.makedirs(args.tempdir, exist_ok=True)
    tempdir = tempfile.mkdtemp(dir=args.tempdir)

    if args.output == "-":
        fh = sys.stdout
    else:
        fh = open(args.output, "wt")

    fh.write("query_id\tpanther_id\tscore\tevalue\tdom_score\tdom_evalue\t"
             "hmm_start\thmm_end\tali_start\tali_end\tenv_start\tenv_end\t"
             "node_id\n")

    try:
        results = process_matches_epang(matches, args.datadir, tempdir,
                                        binary=args.epang,
                                        threads=args.threads)

        for line in results:
            fh.write(line)
    finally:
        if fh is not sys.stdout:
            fh.close()

        if not args.keep:
            shutil.rmtree(tempdir)


def main():
    parser = argparse.ArgumentParser(description="""\
TreeGrafter is a tool for annotating uncharacterized
protein sequences, using annotated phylogenetic trees.
    """)

    subparsers = parser.add_subparsers()

    parser_pre = subparsers.add_parser("prepare")
    parser_pre.add_argument("datadir", help="PANTHER/TreeGrafter data directory")
    parser_pre.set_defaults(func=prepare)

    parser_run = subparsers.add_parser("run")
    parser_run.add_argument("fasta", help="fasta file")
    parser_run.add_argument("hmmsearch", help="hmmsearch output file")
    parser_run.add_argument("datadir", help="TreeGrafter data directory")
    parser_run.add_argument("-e", dest="evalue", type=float,
                            metavar="FLOAT", help="e-value cutoff")
    parser_run.add_argument("-o", dest="output", metavar="FILE",
                            help="write output to FILE",
                            default="-")
    parser_run.add_argument("--epa-ng", dest="epang",
                            help="location of the EPA-ng binary")
    parser_run.add_argument("-t", dest="threads", type=int, default=1,
                            help="number of threads to run EPA-ng with")
    parser_run.add_argument("-T", dest="tempdir", metavar="DIR",
                            help="create temporary files in DIR",
                            default=tempfile.gettempdir())
    parser_run.add_argument("--keep", action="store_true",
                            help="keep temporary directory")
    parser_run.set_defaults(func=run)

    args = parser.parse_args()
    args.func(args)


if __name__ == '__main__':
    main()
