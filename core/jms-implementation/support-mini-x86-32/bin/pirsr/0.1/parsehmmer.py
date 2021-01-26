import re
import os
import sys


class re_matcher(object):

    def __init__(self, matchstring):
        self.matchstring = matchstring

    def match(self, regexp):
        self.rematch = re.match(regexp, self.matchstring)
        return bool(self.rematch)

    def group(self, i):
        return self.rematch.group(i)

def parsehmmsearch(hmmer_out):

    matches = {}

    with open(hmmer_out) as fp:
        score_store = {}

        match_store = {}

        # match_store['POPTR|EnsemblGenome=POPTR_0018s04850|UniProtKB=B9INH6'] = {}
        # match_store['POPTR|EnsemblGenome=POPTR_0018s04850|UniProtKB=B9INH6']['score'] = 900
        # match_store['POPTR|EnsemblGenome=POPTR_0018s04850|UniProtKB=B9INH6']['panther_id'] = 'TEST_PNTR_ID'

        store_align = 0

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
            elif m.match(r'\AQuery:\s+(PIRSR[0-9]+\S+)'):
                # print("INSIDE IF 2: {}".format(line.strip()))
                matchpthr = m.group(1)
                # print(matchpthr)

                fp.readline()
                fp.readline()
                fp.readline()
                fp.readline()
                line = fp.readline()
                # print(line)

                while line.strip():
                    m = re_matcher(line)
                    if m.match(r'\s+------\sinclusion\sthreshold'):
                        # print("INCLUSION THRESHOLD:")
                        # print(line)
                        break
                    
                    # print("STRIP:")
                    # print(line)
                    
                    score_array = line.split()

                    # curr_query_id = score_array[8]
                    # # print(curr_query_id)
                    # curr_score = float(score_array[1])
                    # # print(curr_score)
                    if not '---'  in score_array[1]:
                        score_store[score_array[8]] = float(score_array[1])


                    line = fp.readline()

                # print("END IF 2:")
                # print(line)

            elif m.match(r'\A>>\s+(\S+)'):
                # print("INSIDE IF 3: {}".format(line.strip()))
                query_id = m.group(1)
                store_align = 0

                if not query_id in score_store:
                    # print("QUERY ID UNDER THRESHOLD")
                    line = fp.readline()
                    continue
    
                # print(query_id)
                if query_id not in match_store or score_store[query_id] > match_store[query_id]['score']:
                    # print("NEED TO STORE MATCH")
                    store_align = 1
                    # if query_id not in match_store:
                    match_store[query_id] = {
                            'panther_id': matchpthr, 'score': score_store[query_id], 'align': {
                                'hmmstart': [], 'hmmend': [], 'hmmalign': [], 'matchalign': []
                            } }


            elif m.match(r'\s+\d+\s+!'):
                hit_tokens = line.split()
                
                print(hit_tokens)
                match_store[query_id]['align']['alistart'] = hit_tokens[9]
                match_store[query_id]['align']['aliend'] = hit_tokens[10]
            
            elif m.match(r'\s+==') and store_align:
                # print("INSIDE IF 4: {}".format(line.strip()))

                line = fp.readline()
                hmmalign_array = line.split()

                match_store[query_id]['align']['hmmstart'].append(hmmalign_array[1])
                match_store[query_id]['align']['hmmend'].append(hmmalign_array[3])
                match_store[query_id]['align']['hmmalign'].append(hmmalign_array[2])

                line = fp.readline()

                line = fp.readline()

                match_store[query_id]['align']['matchalign'].append(line.split()[2])

                line = fp.readline()

            elif m.match(r'\A\/\/'):
                # print("END BLOCK")
                # print(match_store)
                score_store = {}
                # BREAK FOR DEBUG
                # break

            # else:
            #     print("NOT MATCHED: {}".format(line.strip()))

            line = fp.readline()

    fp.close()

    for query_id in match_store:
        panther_id = match_store[query_id]['panther_id']

        if panther_id not in matches:
            matches[panther_id] = {}

        matches[panther_id][query_id] = match_store[query_id]['align']


    # print(matches)
    return(matches)

def parsehmmscan(hmmer_out):

    matches = {}
    

    with open(hmmer_out) as fp:
        align_found_n = 0
        matchpthr = None
        query_id = None

        line = fp.readline()
        while line:
            m = re_matcher(line)
            # print("Line {}: {}".format(cnt, line.strip()))

            if line.startswith('#') or not line.strip():
                # print("INSIDE IF 1: {}".format(line.strip()))
                line = fp.readline()
                continue
            elif m.match(r'\AQuery:\s+(\S+)'):
                # print("INSIDE IF 2: {}".format(line.strip()))
                query_id = m.group(1)
                # print(query_id)
            elif m.match(r'\A>> (PIRSR[0-9]+\S+)'):
                align_found_n += 1
                # print("INSIDE IF 3: {}".format(line.strip()))
                matchpthr = m.group(1)
                # print(matchpthr)
                # print(align_found_n)
            elif m.match(r'\s+\d+\s+!') and align_found_n == 1:
               # print("INSIDE IF 4: {}".format(line.strip()))
               # print(line)

                mark = line.split()
                # print(mark)

                if matchpthr not in matches:
                    matches[matchpthr] = {}
                if query_id not in matches[matchpthr]:
                    matches[matchpthr][query_id] = {}
                if 'hmmstart' not in matches[matchpthr][query_id]:
                    matches[matchpthr][query_id]['hmmstart'] = []
                    #matches[matchpthr][query_id]['alistart'] = []

                if 'hmmend' not in matches[matchpthr][query_id]:
                    matches[matchpthr][query_id]['hmmend'] = []
                    #matches[matchpthr][query_id]['aliend'] = []

                matches[matchpthr][query_id]['domscore'] = mark[2]
                matches[matchpthr][query_id]['domevalue'] = mark[5]

                matches[matchpthr][query_id]['hmmstart'].append(mark[6])
                matches[matchpthr][query_id]['hmmend'].append(mark[7])

                matches[matchpthr][query_id]['alistart'] = mark[9]
                matches[matchpthr][query_id]['aliend'] = mark[10]

                matches[matchpthr][query_id]['envstart'] = mark[12]
                matches[matchpthr][query_id]['envend'] = mark[13]

                # print(matches)

            elif m.match(r'\s+==') and align_found_n == 1:
                # print("INSIDE IF 5: {}".format(line.strip()))
                # query_id = m.group(1)
                # print(query_id)
                # print(align_found_n)

                line = fp.readline()

                if 'hmmalign' not in matches[matchpthr][query_id]:
                    matches[matchpthr][query_id]['hmmalign'] = []

                matches[matchpthr][query_id]['hmmalign'].append(line.split()[2])

                line = fp.readline()

                line = fp.readline()
                if 'matchalign' not in matches[matchpthr][query_id]:
                    matches[matchpthr][query_id]['matchalign'] = []

                matches[matchpthr][query_id]['matchalign'].append(
                    line.split()[2])

                line = fp.readline()

            elif m.match(r'\A>> (\S+)'):
                align_found_n += 1
                # print("INSIDE IF 3: {}".format(line.strip()))
                matchpthr = m.group(1)
                # print(matchpthr)
                # print(align_found_n)


            elif m.match(r'\A\/\/'):
                # print("END BLOCK")
                align_found_n = 0
                # break
            # else:
            #     print("NOT MATCHED: {}".format(line.strip()))

            line = fp.readline()

    fp.close()
    return(matches)

def parse(output_file):
    matches = parsehmmscan(output_file)
    raw_matches = []
    for seq_id in matches:
        hits = matches[seq_id]
        for hmm_id  in hits:
            hit = hits[hmm_id]
            start = hit['alistart']
            end = hit['aliend']
            matchalign = hit['matchalign'][0]
            hmmalign = hit['hmmalign'][0]
            hmmfrom = hit['hmmstart'][0]
            hmmend = hit['hmmend'][0]
            domscore = hit['domscore']
            domevalue = hit['domevalue']
            hit_info = [seq_id, hmm_id, hmmfrom, hmmend, hmmalign, start, end, matchalign, domscore, domevalue]
            raw_matches.append(hit_info)
    return raw_matches

def print(matches, output_file):
  with open(output_file,  "w") as outf:
    for match in matches:
        hit_info = f"{match[0]}\t{match[1]}\t{match[2]}\t{match[3]}\t{match[4]}\t{match[5]}\t{match[6]}\t{match[7]}\n"
        outf.write(hit_info)


def print_matches(matches, hmmmode, output_file):
  with open(output_file,  "w") as outf:
    for seq_id in matches:
      hits = matches[seq_id]
      for hmm_id  in hits:
        hit = hits[hmm_id]
        start = hit['alistart']
        end = hit['aliend']
        matchalign = hit['matchalign'][0]
        hmmalign = hit['hmmalign'][0]
        hmmfrom = hit['hmmstart'][0]
        hmmend = hit['hmmend'][0]
        hit_info = f"{seq_id}\t{hmm_id}\t{hmmfrom}\t{hmmend}\t{hmmalign}\t{start}\t{end}\t{matchalign}\n"
        outf.write(hit_info)

