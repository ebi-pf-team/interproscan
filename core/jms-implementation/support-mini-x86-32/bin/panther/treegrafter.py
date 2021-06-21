#!/usr/bin/env python

import os
import shutil
import tempfile
import re
import json
import argparse
import logging
from Bio import Phylo
from Bio.Phylo import NewickIO

# relative imports
from tglib.re_matcher import re_matcher



def process_matches_raxml(matches):
    
    results = results_header

    for pthr in matches:
        logger.info('Processing panther id ' + pthr)

        pthr_align_length = align_length(pthr)

        for query_id in matches[pthr]:
            # print(query_id)
            # query_id = stringify(query_id)
            # print(query_id_str)
            # print('<<<')

            # print(matches[pthr][query_id], pthr_align_length)
            query_msf = _querymsf(matches[pthr][query_id], pthr_align_length)
            # print(query_msf)

            if not query_msf:
                logger.warning('Skipping query ' + query_id + ' due to query_msf sequence error')
                continue

            # print('>>>')

            fasta_file = _generateFasta(pthr, query_id, query_msf)
            # print(fasta_file)

            query_result = _run_raxml(
                pthr, query_id, fasta_file, annotations, matches[pthr][query_id])
            results += query_result
            # print(result_string)

    return results


def process_matches_epang(matches):

    logger.debug('Started running on EPA-NG mode')
    results = results_header

    for pthr in matches:
        logger.info('Processing panther id ' + pthr)

        # print(matches[pthr])
        query_fasta_file = generate_fasta_for_panthr(pthr, matches[pthr])
        # print(query_fasta_file)

        if os.path.getsize(query_fasta_file) == 0:
            logger.warning('Skipping PANTHER id ' + pthr +
                  ' due to empty query fasta')
            continue

        result_tree = _run_epang(pthr, query_fasta_file, annotations)

        if not result_tree:
            logger.warning('Skipping PANTHER id ' + pthr +
                  ' due to error running EPA-ng')
            continue

        # print(result_tree)

        pthr_results = process_tree(pthr, result_tree, matches[pthr])

        results += pthr_results

    return results


def generate_fasta_for_panthr(pthr, matches):

    pthr_align_length = align_length(pthr)

    query_fasta = ''

    for query_id in matches:
        # print(query_id)
        # query_id = stringify(query_id)
        # print(pthr)
        # print(query_id)
        # print(json.dumps(matches[query_id], indent=4))
        querymsf = _querymsf(matches[query_id], pthr_align_length)
        # print(querymsf)

        if not querymsf:
            logger.warning('Skipping query ' + query_id + ' due to query_msf sequence error')
            continue

        # print fasta header
        query_fasta += '>' + query_id + '\n'
        # and the body lines
        for i in range(0, len(querymsf), 80):
            query_fasta += querymsf[i:i+80] + '\n'

    # print(query_fasta)

    fasta_out_file = os.path.join(options['tmp_folder'], f'{pthr}_query.fasta')

    with open(fasta_out_file, 'w') as outfile:
        outfile.write(query_fasta)

    return fasta_out_file


def stringify(query_id):
    # stringify query_id

    query_id = re.sub(r'[^\w]', '_', query_id)

    return query_id


def _generateFasta(pathr, query_id, querymsf):

    # use static dir paths for testing. these are provided
    fasta_in_file = os.path.join(options['msf_tree_folder'], f'{pathr}.AN.fasta')
    fasta_out_file = os.path.join(options['tmp_folder'], f'{query_id}.{pathr}.fasta')

    with open(fasta_out_file, 'w') as outfile:
        with open(fasta_in_file, 'r') as infile:
            # print first fasta header
            outfile.write('>query_' + query_id + '\n')
            # and the body lines
            for i in range(0, len(querymsf), 80):
                outfile.write(querymsf[i:i+80] + '\n')
            # copy over the other fasta file
            outfile.write(infile.read())

    return fasta_out_file


def _querymsf(matchdata, pthrAlignLength):

    # matchdata contains: hmmstart, hmmend, hmmalign and matchalign, as arrays (multiple modules possible)

    # N-terminaly padd the sequence
    # position 1 until start is filled with '-'

    # print(json.dumps(matchdata, indent=4))    

    querymsf = ((int(matchdata['hmmstart'][0]) - 1) * '-')

    # loop the elements/domains
    for i in range(0, len(matchdata['matchalign'])):

        # if this is not the first element, fill in the gap between the hits
        if i > 0:
            start = int(matchdata['hmmstart'][i])
            end = int(matchdata['hmmend'][i-1])
            # This bridges the query_id gap between the hits
            querymsf += (start - end - 1) * '-'

        # extract the query string
        matchalign = matchdata['matchalign'][i]
        hmmalign = matchdata['hmmalign'][i]

        # loop the sequence
        for j in range(0, len(hmmalign)):
            # hmm insert state
            if hmmalign[j:j+1] == ".":
                continue

            querymsf += matchalign[j:j+1]

    # C-terminaly padd the sequence
    # get the end of the last element/domain
    last_end = int(matchdata['hmmend'][-1])
    # and padd out to fill the msf length
    querymsf += (int(pthrAlignLength) - last_end) * '-'

    # error check (is this required?)
    if (len(querymsf) != pthrAlignLength):
        # then something is wrong
        # print("pthrAlignLength: " + str(pthrAlignLength))
        # print("length querymsf: " + str(len(querymsf)))
        # print("MSF: " + querymsf)
        # print("match data:")
        # print(matchdata)

        logger.error(
            'Length of query MSF longer than expected PANTHER alignment length: expected ' + str(pthrAlignLength) + ", got " + str(len(querymsf)))
        logger.debug(matchdata)
        # quit()
        return 0

    return querymsf.upper()


def _run_epang(pthr, query_fasta, annotations):

    # print(query_fasta)

    referece_fasta = os.path.join(options['msf_tree_folder'], f'{pthr}.AN.fasta')
    # print(referece_fasta)

    bifurnewick_in = os.path.join(options['msf_tree_folder'], f'{pthr}.bifurcate.newick')

    epang_dir = os.path.join(options['tmp_folder'], pthr + '_epang')

    os.mkdir(epang_dir)

    epang_cmd = 'epa-ng -G 0.05 -m WAG -T 4 -t ' + bifurnewick_in + \
                ' -s ' + referece_fasta + \
                ' -q ' + query_fasta + ' -w ' + epang_dir + ' >/dev/null'

    if options['algo_bin']:
        epang_cmd = options['algo_bin'] + '/' + epang_cmd

    logger.debug('running epa_ng cmd: ' + epang_cmd)

    exit_status = os.system(epang_cmd)

    if exit_status:
        logger.error('Error running EPA-ng command: ' + epang_cmd)
        return 0

    return epang_dir + '/epa_result.jplace'


def _run_raxml(pathr, query_id, fasta_file, annotations, pthr_matches):

    # print(fasta_file)

    bifurnewick_in = os.path.join(options['msf_tree_folder'], f'{pathr}.bifurcate.newick')

    raxml_dir = os.path.join(options['tmp_folder'], f'{pathr}_{query_id}_raxml')

    os.mkdir(raxml_dir)

    raxml_cmd = 'raxmlHPC-PTHREADS-SSE3 -f y -p 12345 -t ' + bifurnewick_in + \
                ' -G 0.05 -m PROTGAMMAWAG -T 4 -s ' + fasta_file + \
                ' -n ' + pathr + ' -w ' + raxml_dir + ' >/dev/null'

    if options['algo_bin']:
        raxml_cmd = options['algo_bin'] + '/' + raxml_cmd

    # print('RAXML_CMD: ' + raxml_cmd)

    exit_status = os.system(raxml_cmd)

    if exit_status:
        logger.error('Error running RAxML command: ' + raxml_cmd)
        return ''


    # print(exit_status)

    mapANs = _mapto(raxml_dir, pathr, query_id)
    # print(mapANs)

    commonAN = _commonancestor(pathr, mapANs)
    # print(commonAN)

    if commonAN is None:
        commonAN = 'root'

    annot = annotations[pathr + ':' + str(commonAN)]
    # print(annot)
    # result = query_id + "\t" + pathr + "\t" + annot + "\n"

    pthrsf_match = re.match('.*?PTHR\d+:(SF\d+)', annot)
    pthrsf = pthrsf_match.group(1)
    # print(pthrsf)

    # print(pthr_matches)
    # query_matches = pthr_matches[query_id]
    # print(query_matches)

    results = []

    for x in range(0, len(pthr_matches['hmmstart'])):
        # print(x)
        results.append(
            query_id + "\t" +
            pathr + "\t" +
            pthrsf + "\t" +
            pthr_matches['score'][x] + "\t" +
            pthr_matches['evalue'][x] + "\t" +
            pthr_matches['domscore'][x] + "\t" +
            pthr_matches['domevalue'][x] + "\t" +
            pthr_matches['hmmstart'][x] + "\t" +
            pthr_matches['hmmend'][x] + "\t" +
            pthr_matches['alifrom'][x] + "\t" +
            pthr_matches['alito'][x] + "\t" +
            pthr_matches['envfrom'][x] + "\t" +
            pthr_matches['envto'][x] + "\t" +
            annot + "\n")


    # print(results)
    return results


def process_tree(pthr, result_tree, pthr_matches):

    # print(raxml_dir, pathr, query_id)

    with open(result_tree) as classification:
        classification_json = json.load(classification)

    # print(classification_json)

    tree_string = classification_json['tree']
    # print(tree_string)


    matches = re.findall(r'AN(\d+):\d+\.\d+\{(\d+)\}', tree_string)
    # print(matches)

    AN_label = {}
    for [an, r] in matches:
        AN_label['AN' + an] = 'R' + r
        AN_label['R' + r] = 'AN' + an

    # print(AN_label)

    newick_string = re.sub(
        r'(AN\d+)?\:\d+\.\d+{(\d+)}', r'R\g<2>', tree_string)

    newick_string = re.sub(
        r'AN\d+', r'', newick_string)
    newick_string = re.sub(
        r'BI\d+', r'', newick_string)


    # print(newick_string)

    mytree = Phylo.read(NewickIO.StringIO(newick_string), 'newick')
    # print(mytree)
    # Phylo.draw_ascii(mytree)

    results_pthr = []

    for placement in classification_json['placements']:
        # print(placement['n'])
        # print(placement['p'])
        query_id = placement['n'][0]
        # print('query: ' + str(query_id))

        child_ids = []
        ter = []

        for maploc in placement['p']:
            # print(maploc)
            # print(placement['n'])
            # print(placement['p'])
            rloc = 'R' + str(maploc[0])

            # rloc = 'R145'

            # print('RLOC: ' + rloc)

            # node_obj = mytree.find_clades(rloc)
            # print('n_obj: ' + str(node_obj))

            # node = node_obj.__next__()
            # print('next: ' + str(node))

            clade_obj = mytree.find_clades(rloc)

            node = None

            try:
                node = next(clade_obj)
            except:
                logger.error("Error grafting node " +
                             rloc + " on " + pthr + " tree")
                # print(mytree)
                continue


            # print('node: ' + str(node))

            ter.extend(node.get_terminals())

            # print(ter)

            comonancestor = mytree.common_ancestor(ter)

            # print(comonancestor)

            for leaf in comonancestor.get_terminals():
                child_ids.append(AN_label[leaf.name])

            # print(child_ids)
            # return child_ids

        commonAN = _commonancestor(pthr, child_ids)
        # print(commonAN)

        if commonAN is None:
            commonAN = 'root'

        annot = annotations[pthr + ':' + str(commonAN)]
        # print(annot)

        pthrsf_match = re.match('.*?PTHR\d+:(SF\d+)', annot)

        pthrsf = ''
        if pthrsf_match:
            pthrsf = pthrsf_match.group(1)
        else:
            logger.warning("parsing annotations, could not get SF family for " + str(commonAN))
            
        # print(pthrsf)

        # print(pthr_matches)
        # query_matches = pthr_matches[query_id]
        # print(query_matches)

        for x in range(0, len(pthr_matches[query_id]['hmmstart'])):
            # print(x)
            results_pthr.append(
                query_id + "\t" + 
                pthr + "\t" + 
                pthrsf + "\t" +
                pthr_matches[query_id]['score'][x] + "\t" +
                pthr_matches[query_id]['evalue'][x] + "\t" +
                pthr_matches[query_id]['domscore'][x] + "\t" +
                pthr_matches[query_id]['domevalue'][x] + "\t" +
                pthr_matches[query_id]['hmmstart'][x] + "\t" +
                pthr_matches[query_id]['hmmend'][x] + "\t" +
                pthr_matches[query_id]['alifrom'][x] + "\t" +
                pthr_matches[query_id]['alito'][x] + "\t" +
                pthr_matches[query_id]['envfrom'][x] + "\t" +
                pthr_matches[query_id]['envto'][x] + "\t" +
                annot + "\n")

        # results_pthr.append(query_id + "\t" + pthr + "\t" + annot + "\n")

    # print(results_pthr)
    return results_pthr


def _mapto(raxml_dir, pathr, query_id):

    # print(raxml_dir, pathr, query_id)

    classification_file = raxml_dir + '/RAxML_portableTree.' + pathr + '.jplace'
    # print(classification_file)

    with open(classification_file) as classification:
        classification_json = json.load(classification)

    # print(classification_json)

    tree_string = classification_json['tree']
    # print(tree_string)


    matches = re.findall(r'AN(\d+):\d+\.\d+\{(\d+)\}', tree_string)
    # print(matches)

    AN_label = {}
    for [an, r] in matches:
        AN_label['AN' + an] = 'R' + r
        AN_label['R' + r] = 'AN' + an

    # print(AN_label)

    newick_string = re.sub(
        r'(AN\d+)?\:\d+\.\d+{(\d+)}', r'R\g<2>', tree_string)

    newick_string = re.sub(
        r'AN\d+', r'', newick_string)
    newick_string = re.sub(
        r'BI\d+', r'', newick_string)
    # print(newick_string)

    mytree = Phylo.read(NewickIO.StringIO(newick_string), 'newick')
    # print(mytree)
    # Phylo.draw_ascii(mytree)

    locations_ref = classification_json['placements'][0]['p']
    # locations_ref = [[130, 13902], [238, 13902]]
    # print(locations_ref)

    child_ids = []

    ter = []

    for maploc in locations_ref:
        # print(maploc[0])

        rloc = 'R' + str(maploc[0])

        # print('RLOC: ' + rloc)

        node = mytree.find_clades(rloc).__next__()
        # print('node: ' + str(node))


        ter.extend(node.get_terminals())

    # print("maploc OUT")

    comonancestor = mytree.common_ancestor(ter)

    # print(comonancestor)

    for leaf in comonancestor.get_terminals():
        child_ids.append(AN_label[leaf.name])

    # print(child_ids)
    return child_ids


def _commonancestor(pathr, mapANs):

    pantherdir = options['msf_tree_folder']

    newick_in = pantherdir + pathr + '.newick'

    newtree = Phylo.read(newick_in, "newick")
    # Phylo.draw_ascii(newtree)

    commonancestor = newtree.common_ancestor(mapANs)
    # print(commonancestor)

    return commonancestor


def runhmmr():

    
    options['hmmr_out'] = os.path.join(options['hmmr_dir'],
        os.path.basename(options['fasta_input']) + \
        '.' + options['hmmr_mode'] + '.out')

    panther_hmm = os.path.join(options['data_folder'], 'famhmm/binHmm')


    # the binary
    hmmr_cmd = options['hmmr_mode']

    # path to binary
    if options['hmmr_bin']:
        hmmr_cmd = options['hmmr_bin'] + '/' + hmmr_cmd

    # all the rest
    hmmr_cmd = hmmr_cmd + ' --notextw --cpu ' + str(options['hmmr_cpus']) + \
        ' -Z ' + options['hmmr_Z'] + ' -E ' + options['hmmr_E'] + ' --domE ' + options['hmmr_domE'] + ' --incdomE ' + options['hmmr_incdomE'] + \
        ' -o ' + options['hmmr_out'] + \
        ' ' + panther_hmm + ' ' + options['fasta_input'] + ' > /dev/null'


    exit_status = os.system(hmmr_cmd)

    if exit_status != 0:
        logger.critical('Error running hmmer ' + hmmr_cmd)
        quit()

    return exit_status


def parsehmmr(hmmer_out):
    if options['hmmr_mode'] == 'hmmscan':
        return parsehmmscan(hmmer_out)
    elif options['hmmr_mode'] == 'hmmsearch':
        return parsehmmsearch(hmmer_out)


def parsehmmscan(hmmer_out):

    # logger.critical("hmmscan mode is temporarily deactivated, please use hmmscaner")
    # quit()

    store_domain = []
    score_store = {}
    evalue_store = {}
    matches = {}

    with open(hmmer_out) as fp:
        align_found_n = 0
        matchpthr = None
        query_id = None

        line = fp.readline()
        while line:
            m = re_matcher(line)
            # print("Line: {}".format(line.strip()))

            if line.startswith('#') or not line.strip():
                # print("INSIDE IF 1: {}".format(line.strip()))
                line = fp.readline()
                continue
            elif m.match(r'\AQuery:\s+(\S+)'):
                # print("INSIDE IF 2: {}".format(line.strip()))
                query_id = m.group(1)
                query_id = stringify(query_id)
                # print(query_id)

                line = fp.readline()

                m = re_matcher(line)
                if m.match(r'Description:'):
                    fp.readline()

                fp.readline()
                fp.readline()
                fp.readline()
                line = fp.readline()

                while line.strip():
                    m = re_matcher(line)
                    if m.match(r'\s+------\sinclusion\sthreshold'):
                        # print("INCLUSION THRESHOLD: " + line)
                        break

                    # print("STRIP:")
                    # print(line)

                    m = re_matcher(line)
                    if m.match(r'\s+\[No hits detected that satisfy reporting thresholds'):
                        # print("RESPORTING THRESHOLDS")
                        logger.warning(
                            'No hits detected that satisfy reporting thresholds for ' + query_id)
                        # print(line)
                        line = fp.readline()
                        # quit()
                        continue


                    score_array = line.split()

                    matchid = re.sub('\..*', '', score_array[8])

                    score_store[matchid] = float(score_array[1])
                    # print(score_store)
                    evalue_store[matchid] = float(score_array[0])

                    line = fp.readline()

                # print("END IF 2:")
                # print(score_store)


            elif m.match(r'\A>>\s+(\w+)'):
                # print("INSIDE IF 3: {}".format(line.strip()))
                matchpthr = m.group(1)
                # print(matchpthr)
                store_domain = []

                if not matchpthr in score_store:
                    # print("MODEL ID UNDER THRESHOLD")
                    line = fp.readline()
                    continue

                align_found_n += 1
                # print(align_found_n)


            elif m.match(r'\s+\d+\s+[!?]') and align_found_n == 1:
                # print("INSIDE IF 4: {}".format(line.strip()))
                # if m.match(r'\s+\d+\s+\?'):
                #     print(line)
                #     # quit()
                #     fp.readline()
                #     fp.readline()
                #     fp.readline()
                #     fp.readline()
                #     fp.readline()
                #     line = fp.readline()
                #     continue
                mark = line.split()
                # print(mark)

                dom_num = mark[0]
                dom_state = mark[1]

                # print("LOOKING AT DOMAIN: " + dom_num + dom_state)

                if dom_state == '!':
                    # print(mark)
                    if matchpthr not in matches:
                        matches[matchpthr] = {}
                    if query_id not in matches[matchpthr]:
                        matches[matchpthr][query_id] = {
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

                    matches[matchpthr][query_id]['score'].append(str(score_store[matchpthr]))
                    matches[matchpthr][query_id]['evalue'].append(str(evalue_store[matchpthr]))
                    # matches[matchpthr][query_id]['bias'].append(mark[3])
                    matches[matchpthr][query_id]['domscore'].append(mark[2])
                    matches[matchpthr][query_id]['domevalue'].append(mark[5])
                    matches[matchpthr][query_id]['hmmstart'].append(mark[6])
                    matches[matchpthr][query_id]['hmmend'].append(mark[7])
                    matches[matchpthr][query_id]['alifrom'].append(mark[9])
                    matches[matchpthr][query_id]['alito'].append(mark[10])
                    matches[matchpthr][query_id]['envfrom'].append(mark[12])
                    matches[matchpthr][query_id]['envto'].append(mark[13])
                    matches[matchpthr][query_id]['acc'].append(mark[15])

                    store_domain.append(dom_num)

                # print(json.dumps(matches[matchpthr][query_id], indent=4))
                # print(matches)

            elif m.match(r'\s+==\sdomain\s(\d+)') and align_found_n == 1:
                # print("INSIDE IF 5: {}".format(line.strip()))
                # print(matchpthr)
                # print(query_id)
                # print(align_found_n)

                domain_num = m.group(1)
                # print("DOMAIN " + domain_num)

                if domain_num in store_domain:

                    line = fp.readline()

                    matches[matchpthr][query_id]['hmmalign'].append(line.split()[2])

                    line = fp.readline()

                    line = fp.readline()

                    matches[matchpthr][query_id]['matchalign'].append(line.split()[2])
    
                    line = fp.readline()

                    # print(json.dumps(matches[matchpthr][query_id], indent=4))

            elif m.match(r'\A\/\/'):
                # print("END BLOCK")
                align_found_n = 0
                score_store = {}
                evalue_store = {}

                # print(json.dumps(matches, indent=4))

                # if query_id == 'UPI0004FABBC5':
                #     print("BREAK: " + query_id)
                #     quit()


                # print(json.dumps(matches, indent=4))
                # break
            # else:
            #     print("NOT MATCHED: {}".format(line.strip()))

            line = fp.readline()

    fp.close()
    return matches


def parsehmmsearch(hmmer_out):

    match_store = {}
    score_store = {}
    evalue_store = {}
    store_align = 0
    store_domain = []

    matches = {}


    with open(hmmer_out) as fp:
        matchpthr = None
        query_id = None

        line = fp.readline()
        while line:
            m = re_matcher(line)
            # print("Line: {}".format(line.strip()))

            if line.startswith('#') or not line.strip():
                # print("INSIDE IF 1: {}".format(line.strip()))
                line = fp.readline()
                continue
            elif m.match(r'\AQuery:\s+(PTHR[0-9]+)'):
                # print("INSIDE IF 2: {}".format(line.strip()))
                matchpthr = m.group(1)
                # print("MODEL_ID: " + matchpthr)

                fp.readline()
                fp.readline()
                fp.readline()
                fp.readline()
                line = fp.readline()
                # print(line)

                inclusion = 1
                while line.strip():
                    m = re_matcher(line)
                    if m.match(r'\s+------\sinclusion\sthreshold'):
                        # print("INCLUSION THRESHOLD: " + line)
                        inclusion = 0
                        line = fp.readline()
                        # print(line)
                        continue

                    # print("STRIP:")
                    # print(line)
                    if inclusion:
                        score_array = line.split()
                        # print(score_array)

                        score_store[stringify(score_array[8])] = float(score_array[1])
                        evalue_store[stringify(score_array[8])] = float(score_array[0])

                    line = fp.readline()

                # print(score_store)

            elif m.match(r'\A>>\s+(\S+)'):
                # print("INSIDE IF 3: {}".format(line.strip()))
                query_id = m.group(1)
                query_id = stringify(query_id)

                store_domain = []


                if not query_id in score_store:
                    # print("QUERY ID UNDER THRESHOLD")
                    store_align = 0
                    line = fp.readline()
                    # quit()
                    continue

                stored_score = 0
                if query_id in match_store:
                    stored_score = match_store[query_id]['score']

                if float(score_store[query_id]) > float(stored_score):
                    # print(">>> NEED TO STORE MATCH")
                    # print("new score   : " + str(score_store[query_id]))
                    # print("stored score: " + str(stored_score))

                    store_align = 1

                    line = fp.readline()

                    m = re_matcher(line)
                    if m.match(r'\s+\[No individual domains that satisfy reporting thresholds'):
                        # print("RESPORTING THRESHOLDS")
                        logger.warning('No individual domains that satisfy reporting thresholds for ' + query_id)
                        store_align = 0
                        # print(line)
                        line = fp.readline()
                        # quit()
                        continue

                    # match_store[query_id] = {
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
                        # print(line)
                        domain_info = line.split()
                        # print(domain_info)

                        if(len(domain_info) != 16):
                            logger.critical('domain info length is ' + str(len(domain_info)) + ', expected 16 for ' + query_id)
                            logger.debug(domain_info)
                            quit()

                        dom_num = domain_info[0]
                        dom_state = domain_info[1]


                        # print("LOOKING AT DOMAIN: " + dom_num + dom_state)


                        if dom_state == '!':

                            current_match['align']['score'].append(str(score_store[query_id]))
                            current_match['align']['evalue'].append(str(evalue_store[query_id]))
                            current_match['align']['domscore'].append(domain_info[2])
                            # current_match['align']['bias'].append(domain_info[3])
                            # current_match['align']['cEvalue'].append(domain_info[4])
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
                        store_align = 0
                        logger.debug('No ! domains to store, skipping query ' + query_id + ' on model ' + matchpthr )
                        # print(json.dumps(current_match, indent=4))
                        # quit()

                    # print(json.dumps(current_match, indent=4))
                    # print(store_align)

            elif m.match(r'\s+==\sdomain\s(\d+)') and store_align:
                # print("INSIDE IF 4: {}".format(line.strip()))
                # print(matchpthr)
                # print(query_id)
                domain_num = m.group(1)
                # print("DOMAIN " + domain_num)

                if domain_num in store_domain:
                    # print(store_domain)
                    line = fp.readline()
                    hmmalign_array = line.split()

                    hmmalign_model = hmmalign_array[0]
                    hmmalign_model = re.sub(r'\..+', '', hmmalign_model)
                    # print('hmmalign_model ' + hmmalign_model)
                    hmmalign_seq = hmmalign_array[2]


                    
                    # match_store[query_id]['align']['hmmalign'].append(hmmalign_seq)

                    line = fp.readline()

                    line = fp.readline()

                    # print(query_id)
                    matchalign_array = line.split()

                    matchalign_query = matchalign_array[0]
                    matchalign_query = stringify(matchalign_query)

                    # print('matchalign_query ' + matchalign_query)
                    matchlign_seq = matchalign_array[2]
                    # match_store[query_id]['align']['matchalign'].append(matchlign_seq)

                    if (matchpthr == hmmalign_model) and (query_id == matchalign_query):
                        if len(current_match['align']['hmmalign']) >= len(current_match['align']['hmmstart']):
                            # print("SIZE MISMATCH")
                            logger.critical("Trying to add alignment sequence without additional data")
                            quit()
                        # match_store[query_id]['align']['hmmalign'].append(hmmalign_seq)
                        # match_store[query_id]['align']['matchalign'].append(matchlign_seq)

                        current_match['align']['hmmalign'].append(hmmalign_seq)
                        current_match['align']['matchalign'].append(matchlign_seq)

                    # print("store the match")
                    match_store[query_id] = current_match
                    # print(json.dumps(match_store[query_id], indent=4))
                    # else:
                    #     print("ID MISMATCH")
                    #     quit()




                line = fp.readline()

                # print(json.dumps(match_store[query_id], indent=4))

            elif m.match(r'\A\/\/'):
                # print("END BLOCK")
                # print(match_store)
                # print(json.dumps(match_store, indent=4))

                score_store = {}
                evalue_store = {}
                store_domain = []
                store_align = 0

                # BREAK FOR DEBUG
                # break



            line = fp.readline()


    fp.close()

    for query_id in match_store:
        panther_id = match_store[query_id]['panther_id']

        if panther_id not in matches:
            matches[panther_id] = {}

        if not match_store[query_id]['align']['hmmstart']:
            print(query_id)
            print(match_store[query_id])
            quit()

        matches[panther_id][query_id] = match_store[query_id]['align']

    # print(json.dumps(matches, indent=4))
    return matches


def filter_best_domain(matches):
    # print(json.dumps(matches, indent=4))

    for panther in matches:
        # print(json.dumps(panther, indent=4))
        for query in matches[panther]:
            # print(json.dumps(query, indent=4))
            # print(json.dumps(matches[panther][query], indent=4))

            while len(matches[panther][query]['domscore']) > 1:
                # print("score 1 " + matches[panther][query]['score'][0])
                # print("score 2 " + matches[panther][query]['score'][1])
                if matches[panther][query]['domscore'][0] > matches[panther][query]['domscore'][1]:
                    # print("DELETE 2")
                    for key in matches[panther][query]:
                        del matches[panther][query][key][1]
                else:
                    # print("DELETE 1")
                    for key in matches[panther][query]:
                        del matches[panther][query][key][0]

    # print(json.dumps(matches, indent=4))
    return matches


def filter_evalue_cutoff(matches):

    for panther in matches:
        # print(json.dumps(panther, indent=4))
        for query in dict(matches[panther]):
            # print(json.dumps(query, indent=4))

            # remove queries with evalou greater than the cutoff
            if float(matches[panther][query]['evalue'][0]) > float(options['evalue_cutoff']):
                del matches[panther][query]

    # print(json.dumps(matches, indent=4))

    return matches


def get_args():
    """
    Command line arguments parser.
    """

    ap = argparse.ArgumentParser(
        prog='treegrafter.py', description="TreeGrafter",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter)

    ap.add_argument('-f', '--fasta', required=True,
                    help="input fasta file")

    ap.add_argument(
        '-hb', '--hbin', default=None,
        help='path to hmmer bin directory, PATH if None')

    ap.add_argument(
        '-hm', '--hmode', default='hmmsearch', choices=['hmmscan', 'hmmsearch'],
        help='hmmer mode to use')

    ap.add_argument(
        '-am', '--amode', default='epang', choices=['raxml', 'epang'],
        help='tree placing algorithm to use')

    ap.add_argument(
        '-ab', '--abin',
        help='path to RAxML/EPA-ng bin directory, PATH if None')

    ap.add_argument(
        '-hc', '--hcpus', default=1, type=int,
        help="number of hmmer cpus")

    ap.add_argument(
        '-hZ', default=65000000, type=int,
        help="hmmer set # of comparisons done, for E-value calculation")

    ap.add_argument(
        '-hE', default=0.001, type=float,
        help="hmmer report sequences <= this E-value threshold in output")

    ap.add_argument(
        '-hdomE', default=0.000000001, type=float,
        help="hmmer report domains <= this E-value threshold in output")

    ap.add_argument(
        '-hincdomE', default=0.000000001, type=float,
        help="hmmer consider domains <= this E-value threshold as significant")

    ap.add_argument(
        '-ho', '--hout', default=None,
        help="existing hmmer output file")

    ap.add_argument(
        '-hd', '--hdir', default=None,
        help="directory to store hmmer output files, None defaults to the tmp dir")

    ap.add_argument(
        '-o', '--out', required=True,
        help='output file name')

    ap.add_argument(
        '-d', '--data', required=True,
        help='panther data directory')

    ap.add_argument(
        '-t', '--tmp',
        help='tmp work directory, None usues system default')

    ap.add_argument(
        '-k', '--keep', action='store_true',
        help='if set, does not clear tmp folder')

    ap.add_argument(
        '-e', "--evalue-cutoff", default=None, type=float,
        help="consider only matches from hmmer output with evalue <= provided cutoff")

    ap.add_argument(
        '-v', '--verbose', default='ERROR', choices=['DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL'], type=str.upper,
        help='if set, logs debug info at the provided level')

    ap.add_argument(
        '-l', '--legacy', action='store_true',
        help='if set, uses legacy behaviour of using multiple domains of a match. Default uses best domain only.')


    args = vars(ap.parse_args())

    return args


def align_length(pthr):
    pthr_fasta_file = os.path.join(options['msf_tree_folder'], f'{pthr}.AN.fasta')

    try:
        with open(pthr_fasta_file) as f:
            file = f.read().split('>')
            first_seq = file[1]
            first_seq = re.sub(r'\A[^\n]+', '', first_seq)
            first_seq = re.sub(r'\n', '', first_seq)
            seq_length = len(first_seq)
    except IOError:
        logger.error("Could not find alignment for " + pthr)
        return 0

    return seq_length


def get_annotations():
    annot_file = os.path.join(options['data_folder'], 'PAINT_Annotations/PAINT_Annotatations_TOTAL.txt')
    # print(annot_file)

    annot = {}
    with open(annot_file) as f:
        for line in f:
            line = line.strip()
            # print(line)
            line_array = line.split("\t")
            # print(line_array)
            annot[line_array[0]] = line_array[1]

    return annot


if __name__ == '__main__':

    args = get_args()

    # global options
    options = {}
    options['data_folder'] = os.path.abspath(args['data'])
    options['fasta_input'] = args['fasta']
    options['out_file'] = args['out']
    options['hmmr_mode'] = args['hmode']
    options['hmmr_bin'] = args['hbin']
    options['algo_mode'] = args['amode']
    options['algo_bin'] = args['abin']
    options['hmmr_cpus'] = args['hcpus']
    options['hmmr_Z'] = args['hZ']
    options['hmmr_E'] = args['hE']
    options['hmmr_domE'] = args['hdomE']
    options['hmmr_incdomE'] = args['hincdomE']
    options['hmmr_dir'] = args['hdir']
    options['hmmr_out'] = args['hout']
    options['keep_tmp'] = args['keep']
    options['evalue_cutoff'] = args['evalue_cutoff']
    options['legacy'] = args['legacy']
    options['msf_tree_folder'] = os.path.join(options['data_folder'], 'Tree_MSF/')
    if args['tmp'] is None:
        options['tmp_folder'] = tempfile.mkdtemp()
    else:
        options['tmp_folder'] = tempfile.mkdtemp(dir=args['tmp'])
    if options['hmmr_dir'] is None:
        options['hmmr_dir'] = options['tmp_folder']

    log_formatter_str = '%(asctime)s | %(levelname)-8s | %(message)s'
    log_formatter = logging.Formatter(log_formatter_str)

    handlers = list()
    console_handler = logging.StreamHandler()
    console_handler.setFormatter(log_formatter)
    handlers.append(console_handler)

    logging.basicConfig(level=args['verbose'],
                        format=log_formatter_str,
                        handlers=handlers)


    logger = logging.getLogger('treegrafter')

    if not os.path.isdir(options['data_folder']):
        logger.critical('PANTHER data folder does not exist')
        quit()

    if not os.path.isdir(options['tmp_folder']):
        logger.critical('Cannot write to tmp folder ' + options['tmp_folder'])
        quit()


    results_header = ["query_id\tpanther_id\tpanther_sf\tscore\tevalue\tdom_score\tdom_evalue\thmm_start\thmm_end\tali_start\tali_end\tenv_start\tenv_end\tannotations\n"]


    # print(json.dumps(options, indent=4))    

    if options['hmmr_out'] is None:
        logger.info('Running ' + options['hmmr_mode'])
        runhmmr()

    logger.info('Parsing hmmr output file')
    matches = parsehmmr(options['hmmr_out'])

    # print(json.dumps(matches, indent=4))

    if not options['legacy']:
        # get the best domain only
        logger.info('Filtering best domains')
        matches = filter_best_domain(matches)

    if options['evalue_cutoff']:
        logger.info('Checking cutoff evalue')
        matches = filter_evalue_cutoff(matches)


    logger.info('Loading annotations')
    annotations = get_annotations()
    # print(annotations)

    results = []

    if options['algo_mode'] == 'raxml':
        results = process_matches_raxml(matches)
    elif options['algo_mode'] == 'epang':
        results = process_matches_epang(matches)

    file = open(options['out_file'], 'w')
    logger.info('Writing results to output file')
    for line in results:
        file.write(line)
    file.close()

    if not options['keep_tmp']:
        # remove tmp folder
        logger.info('Removing tmp data folder ' + options['tmp_folder'])
        shutil.rmtree(options['tmp_folder'])
        os.mkdir(options['tmp_folder'])
    else:
        logger.info('Keeping tmp data folder ' + options['tmp_folder'])

    logger.info('Done')

