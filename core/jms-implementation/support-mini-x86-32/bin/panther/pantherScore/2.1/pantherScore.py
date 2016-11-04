import sys

def getBestHit (domains, seqId, hmmEvalueCutOff):
  bestHmm = None
  bestEval = None
  bestScore = None

  hmmHits = domains[seqId]
  #print hmmHits
  for key in hmmHits:
    ##get key

    hmmId = key
    eval = hmmHits[hmmId]['eval']
    score = hmmHits[hmmId]['score']
    #print key, eval, score, "eval:", float(eval), "cut off is ", float(hmmEvalueCutOff)
    #print "is eval within cutoff: ", float(eval) > float(hmmEvalueCutOff)
    if float(eval) > float(hmmEvalueCutOff):
      continue

    #define best hit, if: 1st time; this is the best eval; this eval equal
    #to best eval, AND this score is better define as best hit
    if  (not bestEval) or (eval < bestEval) or ((eval == bestEval) and (score > bestScore)):
      bestHmm = hmmId
      bestEval = eval
      bestScore = score

  return bestHmm

def get_queryName(hmma):
  hmma_list = hmma.split ('.')
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


#domfile = '/home/nuka/projects/git/github/interproscan/core/jms-implementation/target/interproscan-5-dist/temp/nuka-d90_20161102_140800110_eocm//jobPantherHmm3/000000000001_000000000003.raw.domtblout.out'
#panther_outputfile = '/home/nuka/projects/git/github/interproscan/core/jms-implementation/target/interproscan-5-dist/temp/nuka-d90_20161102_140800110_eocm//jobPantherHmm3/000000000001_000000000003.filtered.out'

if  len(sys.argv) == 3:
  domfile = sys.argv[1]
  panther_outputfile = sys.argv[2]
else:
  sys.exit(2)

hmmEvalCut = 0.001
domains = {}

open(panther_outputfile, 'w').close()

seqHmmHits = []
with open(domfile, 'r') as infile:
   for line in infile:
     if line.startswith('#'):
       continue
     hhmId = None
     seqid, acc1, tlen, hmma, acc2, qlen, eVal, score, bias, num, num_t, cEval, iEval, dscore, dbias, hmm_f, hmm_t, ali_f, ali_t, env_f, env_t, accuracy, desc  = line.split()
     #hmm_hit = {}
     hmmId = get_queryName(hmma)

     hmmHit = {'eval':eVal, 'score':score, 'ali_f':ali_f, 'ali_t':ali_t}
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

     #domains.append({'seqid':seqid, 'acc1':acc1, 'tlen':tlen, 'hmma':hmma, 'acc2':acc2, 'qlen':qlen, 'eVal':eVal, 'score':score, 'bias':bias, 'num':num, 'num_t':num_t, 'cEval':cEval, 'iEval':iEval, 'dscore':dscore, 'dbias':dbias, 'hmm_f':hmm_f, 'hmm_t':hmm_t, 'ali_f':ali_f, 'ali_t':ali_t, '':env_f, '':env_t, 'accuracy':accuracy, 'desc':desc})

with open(panther_outputfile, 'a') as outfile:
  for seqId in domains.keys():
    #print seqId
    bestHmmId = getBestHit(domains,seqId,hmmEvalCut)
    #$seqId,$hmm,$allScores->{$seqId}{$hmm}{'eval'},$allScores->{$seqId}{$hmm}{'score'},$allScores->{$seqId}{$hmm}{'seqRange'},$hmmNames->{$hmm}
    if bestHmmId:
      pantherFamily = bestHmmId
      pantherSubFamily = None
      if ':' in bestHmmId:
        pantherFamily, pantherSubFamily = bestHmmId.split(':')
      hmmHits = domains[seqId]
      hmmHit = hmmHits[bestHmmId]
      #if hmmHit.keys()[0] == pantherFamily:
      outfile.write(seqId + '\t' + bestHmmId + '\t' + pantherFamily +  '\t' + hmmHit['eval'] + '\t' + hmmHit['score'] + '\t' + hmmHit['ali_f'] + '-' + hmmHit['ali_t'] + '\n')
      if pantherSubFamily:
        outfile.write(seqId + '\t' + pantherFamily +  '\t' + pantherFamily +  '\t' + hmmHit['eval'] + '\t' + hmmHit['score'] + '\t' + hmmHit['ali_f'] + '-' + hmmHit['ali_t'] + '\n')
