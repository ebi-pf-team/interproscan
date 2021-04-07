"""Takes the output from Cath Resolve Hits and assigns CATH superfamilies based on domain id's"""


import sys
import pickle as pkl
import os
import itertools
from collections import defaultdict
import csv
import gzip
import re

domain_to_family_map_file = sys.argv[1]
discontinuous_regs_file = sys.argv[2]
infile = sys.argv[3]
outfile = sys.argv[4]

evalue_coff = 0.001

discontinuous_regs = pkl.load(open(discontinuous_regs_file, 'rb'), encoding='utf-8')

mode = "with_family"
if len(sys.argv) > 2:
   mode = sys.argv[2]


ofh = None
# if infile.endswith(".gz"):
#     ofh = csv.writer(gzip.open(infile.replace(".gz", ".csv.gz"),"w"))
# else:
#     ofh = csv.writer(open(infile + ".csv","w"))

#output file
ofh = csv.writer(open(outfile,"w"), delimiter='\t')

dom_to_fam={}

for line in open(domain_to_family_map_file):
    if line.strip():
        vals = re.split(r'\s+', line)
        domain_id = vals[0].strip()
        domain_id = re.sub(r'^"|"$', '', domain_id)
        if '-' in domain_id:
            domain_id = domain_id[:-3]
        superfamily = vals[1].strip()
        superfamily = re.sub(r'^"|"$', '', superfamily)
        dom_to_fam[domain_id] = superfamily

# for line in open(domain_to_family_map_file):
#     vals = line.split()
#     dom_to_fam[vals[1].strip()] = vals[0]
#
# for line in open("./cath_release/CathDomainList"):
#
#     line =line.replace("\n","")
#
#     if line.startswith("#"):continue
#
#     vals = line.split()
#     superfamily= ".".join(vals[1:5])
#     domain_id =vals[0]
#     dom_to_fam[domain_id] = superfamily



def rangesAsList(i, merge_small_gaps=False):
    x=[]
    for start,stop in ranges(i):
        x.append([start, stop])

    return x

def getRegionsAsString(regions):

    a=[]
    for start_stop in regions:
        a.append("-".join(map(str,start_stop)))
    return ",".join(a)


def ranges(i):
    for a, b in itertools.groupby(enumerate(i), lambda x_y: x_y[1] - x_y[0]):
        b = list(b)
        yield b[0][1], b[-1][1]


"""open cath resolve hits file, add a CATH superfamily column and split up discontinous HMM's into component domains"""
ifh = None
if infile.endswith(".gz"): ifh = gzip.open(infile)
else: ifh = open(infile)

for line in ifh:
    line = line.rstrip()
    vals =line.split()

    if line.startswith("#"):
        if line.startswith("#FIELD"):
            ofh.writerow(["#domain_id","cath-superfamily"] + vals[1:])
        continue



    hmm_id = vals[1]
    if hmm_id.startswith("dc_") is False:
        dom = hmm_id.split("-")[0]
        sfam = dom_to_fam.get(dom, "-")

        if mode =="with_family":
            if len(sfam)==0: continue

            evalue = float(vals[-1])

            if evalue > evalue_coff: continue

        ofh.writerow([dom,sfam] + vals + [""])
        continue

    sequence_id, hmm_id, bit_score, start_stop, final_start_stop, alignment_regs,cond_eval, ind_eval = vals

    final_start_stop_list=[]
    for i in final_start_stop.split(","):
        final_start_stop_list.append(list(map(int,i.split("-"))))
    plup = discontinuous_regs[hmm_id]


    mda_resolved_aas = set()
    for start,stop in final_start_stop_list:
        mda_resolved_aas |= set(range(start, stop +1))


    dom_sequence_regs = defaultdict(list)

    resi_dom={}
    for areg in alignment_regs.split(";"):
        hmm_region, seq_region = areg.split(",")
        hmm_start, hmm_stop = list(map(int, hmm_region.split("-")))
        seq_start, seq_stop = list(map(int, seq_region.split("-")))
        seq_pos = list(range(seq_start, seq_stop +1))


        for c,i in enumerate(range(hmm_start, hmm_stop+1)):

            dom, resi, ostat = plup.get(i-1, [None,None,None]) #zero indexed
            aa_num = seq_pos[c]
            if aa_num not in mda_resolved_aas: continue

            resi_dom[seq_pos[c]]=[dom,ostat]
            if dom:
                dom_sequence_regs[dom].append(seq_pos[c])
    #fill
    for dom, regs in list(dom_sequence_regs.items()):
        
        sequence_regs = rangesAsList(regs)
        new_sequence_regs=[]
        for c,reg in enumerate(sequence_regs):
         
            if c==0:
                new_sequence_regs.append(reg)
                continue
            prev_reg = new_sequence_regs[-1]

            if reg[0] - prev_reg[1] <=20:
                conflict = False
               
                
                for resi in range(prev_reg[0]+1, reg[1]):
                    dom2,ostat = resi_dom.get(resi,[None, None])
                    if dom2 is None: continue
                    if dom2 != dom: 
                        conflict =True
                        break

                if conflict is False:
                    new_reg = [prev_reg[0], reg[1]]
                    new_sequence_regs[-1]= new_reg    

                else:new_sequence_regs.append(reg)
            else:
                new_sequence_regs.append(reg)
        
        reg_ostats=[]
        for reg in new_sequence_regs:
            reg_ostat = set([resi_dom[reg[0]][1],  resi_dom[reg[1]][1] ])
            if len(reg_ostat) > 1: reg_ostat = "*"
            else:  reg_ostat = "".join(list(reg_ostat))
            reg_ostats.append(reg_ostat)

        reg_ostats_string = "".join(reg_ostats)
        sequence_regs_string = getRegionsAsString(new_sequence_regs)
        tot_res = 0
        for start,stop in new_sequence_regs:
            tot_res += (stop - start) +1
        if tot_res < 10: continue
        vals[1] = hmm_id + "_" + dom
        vals[4] =sequence_regs_string


        sfam =  dom_to_fam.get(dom,"-")
        if mode =="with_family":   
            if len(sfam) ==0: continue
            evalue = float(vals[-1])
            if evalue > evalue_coff: continue

        ofh.writerow([dom, sfam] +  vals + [reg_ostats_string])

ifh.close()
