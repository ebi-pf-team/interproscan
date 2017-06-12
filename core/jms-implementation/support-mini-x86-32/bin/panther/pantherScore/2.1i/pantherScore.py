import sys
from operator import itemgetter

def get_queryName(hmma):
    hmma_list = hmma.split ('.')
    # print(hmma_list)
    if len(hmma_list) > 2:
        hmm_fam  = hmma_list[0]
        hmm_sf = hmma_list[1]
        something_else = hmma_list[2]
    elif len(hmma_list) == 2:
        hmm_fam  = hmma_list[0]
        something_else = hmma_list[1]
    hmmId = hmm_fam
    if hmm_sf.startswith("SF"):
        hmmId = hmm_fam + ':' + hmm_sf
        # hmmId = hmm_fam + '.' + hmm_sf
    return hmmId

def get_best_hits(seq_hits, cut_off):
    best_hmms = []
    best_evalue = None
    #a_hit_is = {'eval': eVal, 'score': score, 'ali_f': ali_f, 'ali_t': ali_t}
    #m[np.argmin(m[:, 0]), :]

    sorted(seq_hits, key=itemgetter(0))
    best_evalue = seq_hits[1][0]
    if best_evalue > cut_off:
        return best_hmms
    else:
        #check the scores
        with_scores = seq_hits[seq_hits[:,0]==best_evalue]
        sorted(with_scores, key=itemgetter(2))

        for row in seq_hits:
            if row[0] == 'TODO':
               print('code TODO')
        sorted(seq_hits, key=itemgetter(0))
    seq_hits = all_scores[seqId]
    for hmmId in seq_hits:
        if not best_evalue:
            best_evalue = seq_hits[hmmId]



def parseHmmscan(domtblout, hmmer_mode):
    all_scores = {}
    with open(domtblout, 'r') as infile:
        for line in infile:
            if line.startswith('#'):
                continue
            # print(line)
            if hmmer_mode == 'hmmsearch':
                #my @ reorder =   @row [0..5];
                #$row[0] = $reorder[3];
                #$row[1] = $reorder[4];
                #$row[2] = $reorder[5];
                #$row[3] = $reorder[0];
                #$row[4] = $reorder[1];
                #$row[5] = $reorder[2];
                seqid, acc1, tlen, hmma, acc2, qlen, eVal, score, bias, num, num_t, cEval, iEval, dscore, dbias, hmm_f, hmm_t, ali_f, ali_t, env_f, env_t, accuracy, desc = line.split()
            else:
                hmma, acc2, qlen, seqid, acc1, tlen, eVal, score, bias, num, num_t, cEval, iEval, dscore, dbias, hmm_f, hmm_t, ali_f, ali_t, env_f, env_t, accuracy, desc = line.split()

            #my ($target, $acc1, $tlen, $seq, $acc2, $qlen, $eVal, $score, $bias, $num, $num_t, $cEval, $iEval, $dscore, $dbias, $hmm_f, $hmm_t, $ali_f, $ali_t, $env_f, $env_t, @else) = split (/\s+/, $_);
            hmmId = get_queryName(hmma)

            hmmHit = {'eval': eVal, 'score': score, 'ali_f': ali_f, 'ali_t': ali_t}
            all_scores[seqid][hmmId] = hmmHit

        $seq =~ s/^\s+|\s+$//g;
 		$eVal =~ s/^\s+|\s+$//g;
 		$score =~ s/^\s+|\s+$//g;
 		#$allScores->{$seq}{$hmmId}{$num}{'eval'} = $eVal;
 		#$allScores->{$seq}{$hmmId}{$num}{'score'} = $score;
 		#$allScores->{$seq}{$hmmId}{$num}{'seqRange'} = "$ali_t\-$ali_f";
 		#print "$target\t$seq\t$eVal\t$score\t$ali_f\-$ali_t\n";
 		$allScores->{$seq}{$eVal}{$score}{$hmmId}{"$ali_f\-$ali_t"}=1;

            # $allScores->{$seq}{$eVal}{$score}{$hmmId}{"$ali_f\-$ali_t"} = 1
            """
                        hhmId = None
                        seqid, acc1, tlen, hmma, acc2, qlen, eVal, score, bias, num, num_t, cEval, iEval, dscore, dbias, hmm_f, hmm_t, ali_f, ali_t, env_f, env_t, accuracy, desc = line.split()
                        # hmm_hit = {}
                        hmmId = get_queryName(hmma)


                        if seqid in domains.keys():
                            seqHmmHits = domains[seqid]
                            # hmmHits = seqHmmHits[hmmId]
                            # print line
                            # print type(hmmHits), seqid, hmmId,  hmmHits
                            domains[seqid][hmmId] = hmmHit
                        else:
                            domains[seqid] = {}
                            # domains[seqid][hmmId]= []
                            domains[seqid][hmmId] = hmmHit

                            # domains.append({'seqid':seqid, 'acc1':acc1, 'tlen':tlen, 'hmma':hmma, 'acc2':acc2, 'qlen':qlen, 'eVal':eVal, 'score':score, 'bias':bias, 'num':num, 'num_t':num_t, 'cEval':cEval, 'iEval':iEval, 'dscore':dscore, 'dbias':dbias, 'hmm_f':hmm_f, 'hmm_t':hmm_t, 'ali_f':ali_f, 'ali_t':ali_t, '':env_f, '':env_t, 'accuracy':accuracy, 'desc':desc})

            """
    return all_scores

if __name__ == '__main__':
    domtblout = sys.argv[1]
    hmmscan = sys.argv[2]
    if domtblout and hmmscan:
        all_scores = parseHmmscan(domtblout, hmmscan)
        print ("hit count: " + str(len(all_scores)))
    print("=== end ===")
