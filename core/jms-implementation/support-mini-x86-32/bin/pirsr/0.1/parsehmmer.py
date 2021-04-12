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

def stringify(query_id):
    # stringify query_id

    query_id = re.sub(r'[^\w]', '_', query_id)

    return query_id


def parsehmmsearch(hmmer_out):

    matches = {}
    with open(hmmer_out) as fp:
        score_store = {}
        match_store = {}
        forward_seek = False
        domain_hits = {}
        #sequence_hits =
        matchpthr = None
        query_id = None

        line = fp.readline()
        while line:
            m = re_matcher(line)
            print ('top:' + m.matchstring)
            if line.strip().startswith('>>'):
                print('then deal with the sequence number')
            if ('==' in line):
                print("processing  for sequence " + str(query_id) +  ' and hmm ' +   hmm_id )
                print(line)
            if line.startswith('#') or not line.strip():
                line = fp.readline()
                continue
            elif m.match(r'\AQuery:\s+(PIRSR[0-9]+\S+)'):
                hmm_id = m.group(1)
                print ('processing hmm accession : ' + hmm_id)
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

                    score_array = line.split()
                    if not '---'  in score_array[1]:
                        score_store[stringify(score_array[8])] = float(score_array[1])
                    else:
                        score_store[stringify(score_array[8])] = float(0.0)

                    line = fp.readline()

                # print("END IF 2:")
                # print(line)

            elif m.match(r'\A>>\s+(\S+)'):
                # print("INSIDE IF 3: {}".format(line.strip()))
                query_id = m.group(1)
                query_id = stringify(query_id)
                print ('processing hmm sequence query_id : ' + query_id)

                if not query_id in domain_hits:
                    domain_hits[query_id] = {}
                domain_hits[query_id][hmm_id] = {}

                store_align = 1

                line = fp.readline()

                # print(line)

                m = re_matcher(line)
                if m.match(r'\s+\[No individual domains that satisfy reporting thresholds'):
                    # print("RESPORTING THRESHOLDS")
                    #logger.warning('WARNING: No individual domains that satisfy reporting thresholds for ' + query_id)
                    print("Do we ever reach here ....")
                    # print(line)
                    break


                fp.readline()
                line = fp.readline()
                while line.strip():
                    # print(line)
                    domain_info = line.split()
                    #print (domain_info)
                    # print(domain_info)

                    if(len(domain_info) != 16):
                        logger.error('ERROR: domain info length is ' + str(len(domain_info)) + ', expected 16 for ' + query_id)
                        logger.debug(domain_info)
                        quit()

                    domain_id = domain_info[0]

                    if not domain_info[1] == '!':
                        print('this is a weak match')
                    else:
                        print('process this domain hit: ' + str(domain_id))

                    print(domain_info)
                    #domain_hit = {}
                    domain_hits[query_id][hmm_id][domain_id] = {}
                    print(domain_hits[query_id][hmm_id][domain_id])
                    domain_hits[query_id][hmm_id][domain_id]['score'] = domain_info[2]
                    domain_hits[query_id][hmm_id][domain_id]['bias'] = domain_info[3]
                    domain_hits[query_id][hmm_id][domain_id]['cEvalue'] = domain_info[4]
                    domain_hits[query_id][hmm_id][domain_id]['iEvalue'] = domain_info[5]
                    domain_hits[query_id][hmm_id][domain_id]['hmmstart'] = domain_info[6]
                    domain_hits[query_id][hmm_id][domain_id]['hmmend'] = domain_info[7]
                    domain_hits[query_id][hmm_id][domain_id]['alifrom'] = domain_info[9]
                    domain_hits[query_id][hmm_id][domain_id]['alito'] = domain_info[10]
                    domain_hits[query_id][hmm_id][domain_id]['envfrom'] = domain_info[12]
                    domain_hits[query_id][hmm_id][domain_id]['envto'] = domain_info[13]
                    domain_hits[query_id][hmm_id][domain_id]['acc'] = domain_info[15]
                    domain_hits[query_id][hmm_id][domain_id]['hmmalign'] = ''
                    domain_hits[query_id][hmm_id][domain_id]['matchalign'] = ''
                    if domain_hits[query_id][hmm_id][domain_id]:
                        print("Print the hits for this domain: " + str(domain_id))
                        print(domain_hits[query_id][hmm_id][domain_id])
                    # print(json.dumps(match_store[query_id], indent=4))

                    line = fp.readline()

            elif m.match(r'\s+=='):
                print("process alignments for sequence " + str(query_id) +  ' and hmm ' +   hmm_id )
                process_align = True
                print('How many hits: ' + str(len(domain_hits[query_id][hmm_id])))
                while process_align:
                    domain_hit_id = m.matchstring.split()[2]
                    #print(m.matchstring)
                    hmmalign = ''
                    matchalign = ''

                    #process the alignment for there is hit
                    print ('process alignment for domain ' +  str(domain_hit_id))
                    store_alignment = False
                    same_domain = True
                    line = fp.readline()
                    while same_domain:
                        #print('line: ' + line)
                        hmmalign_array = line.split()
                        hmmalign = hmmalign +  hmmalign_array[2]
                        print(hmmalign)

                        line = fp.readline()
                        line = fp.readline()
                        matchalign = matchalign + line.split()[2]

                        #print('align: ' + hmmalign +'==' + matchalign )

                        line = fp.readline()
                        #print(line)
                        line = fp.readline()
                        #print('check line: ' + line)
                        line = fp.readline()
                        exit_condition = 0
                        if line.strip():
                            if line.strip().startswith(hmm_id):
                                #print("the alignme spans more than two lines ")
                                store_alignment = False
                                dummy = 1
                                exit_condition = 0
                            elif line.strip().startswith('=='):
                                print('process different domain ...')
                                store_alignment = True
                                if not domain_hit_id in domain_hits[query_id][hmm_id]:
                                    print("1. ERRO with " + str(domain_hit_id))
                                same_domain = False
                                m = re_matcher(line)
                                exit_condition = 1
                            elif line.strip().startswith('>>'):
                                store_alignment = True
                                if not domain_hit_id in  domain_hits[query_id][hmm_id]:
                                    print("2. ERRO with " + str(domain_hit_id))
                                    #print(domain_hit_id)
                                m = re_matcher(line)
                                same_domain = False
                                process_align = False
                                exit_condition = 2
                            else:
                                store_alignment = True
                                if not domain_hit_id in domain_hits[query_id][hmm_id]:
                                    print("3. ERRO with " + str(domain_hit_id))
                                exit_condition = 3
                        else:
                            if not domain_hit_id in domain_hits[query_id][hmm_id]:
                                print("4. ERRO with " + str(domain_hit_id))
                                #print(domain_hit_id)
                            store_alignment = True
                            same_domain = False
                            process_align = False
                            exit_condition = 4

                    if store_alignment:
                        print('store_alignment: ' + str(store_alignment) + " exit_condition " + str(exit_condition) )
                        domain_hits[query_id][hmm_id][domain_hit_id]['hmmalign'] = hmmalign
                        domain_hits[query_id][hmm_id][domain_hit_id]['matchalign'] = matchalign
                        print ('store_alignment line: ' + line)
                        forward_seek = True

            elif m.match(r'\A\/\/'):
                # print("END BLOCK")
                # print(match_store)
                score_store = {}
                # BREAK FOR DEBUG
                # break

            # else:
            #     print("NOT MATCHED: {}".format(line.strip()))
            if not forward_seek: # we might have peeked forward
                line = fp.readline()
                forward_seek = False
            else:
                forward_seek = False

    fp.close()
    #process domain_hits

    return domain_hits

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
    matches = parsehmmsearch(output_file)
    raw_matches = []
    print ("hmmer search hits: --- ")
    for seq_id in matches:
        hits = matches[seq_id]
        for hmm_id  in hits:
            hit = hits[hmm_id]
            for hit_id in hit:
                start = hit[hit_id]['alifrom']
                end = hit[hit_id]['alito']
                matchalign = hit[hit_id]['matchalign']
                hmmalign = hit[hit_id]['hmmalign']
                hmmfrom = hit[hit_id]['hmmstart']
                hmmend = hit[hit_id]['hmmend']
                domscore = hit[hit_id]['score']
                domevalue = hit[hit_id]['iEvalue']
                hit_info = (seq_id, hmm_id, hmmfrom, hmmend, hmmalign, start, end, matchalign, domscore, domevalue)
                print (str(hit_info) + '\n')
                raw_matches.append(hit_info)

    return set(raw_matches)

def print2file(matches, output_file):
  with open(output_file,  "w") as outf:
    for match in matches:
        #only works in python 3.5 or later, so disable
        #hit_info = f"{match[0]}\t{match[1]}\t{match[2]}\t{match[3]}\t{match[4]}\t{match[5]}\t{match[6]}\t{match[7]}\n"
        hit_info = "{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\n".format(match[0], match[1], match[2], match[3], match[4], match[5], match[6], match[7])
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
        hmmstart = hit['hmmstart'][0]
        hmmend = hit['hmmend'][0]
        #hit_info = f"{seq_id}\t{hmm_id}\t{hmmfrom}\t{hmmend}\t{hmmalign}\t{start}\t{end}\t{matchalign}\n"
        hit_info = "{seq_id}\t{hmm_id}\t{hmmstart}\t{hmmend}\t{hmmalign}\t{start}\t{end}\t{matchalign}\n".format(seq_id,hmm_id,hmmstart,hmmend,hmmalign,start,end,matchalign)
        outf.write(hit_info)

