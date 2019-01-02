import subprocess
import sys
import os.path
import os
import re
import time


def get_sequences(fasta_file):
    with open(fasta_file, 'r') as fasta:
        fasta_dict = {}
        for line in fasta:
            line = line.strip()
            if line == '':
                continue
            if line.startswith('>'):
                seq_id = line.lstrip('>')
                fasta_dict[seq_id] = ''
            else:
                fasta_dict[seq_id] += line + '\n'
    return fasta_dict

def updateSet(key, new_elem, seq_list):
    s_list = []
    if key in seq_list:
        s_list = seq_list[key]
        s_list.append(new_elem)
    else:
        s_list = [new_elem]
    seq_list[key] = s_list
    return seq_list

def get_all_seqs_dic(all_seqs):
    print("Seq count: " + str(len(all_seqs)))
    all_seqs_dic = {}

    for elem in all_seqs:
        key = elem
        new_key = key.split()[0]
        if '_' in new_key:
            new_key,index = new_key.rsplit('_',1)
        s_list = all_seqs[key]
        seq_length = len(s_list)
        s_pair = [seq_length,(key, s_list)]
        all_seqs_dic = updateSet(new_key, s_pair, all_seqs_dic)
    return all_seqs_dic

if len(sys.argv) >= 4:
    orfs_filter_size = sys.argv[1]
    input_file = sys.argv[2]
    output_file = sys.argv[3]
else:
    print("Error: missing parameters " + str(4 - len(sys.argv)))
    sys.exit(4)

all_seqs_dic = get_all_seqs_dic(get_sequences(input_file))
print("original nucleotide sequence count: " + str(len(all_seqs_dic)))
if orfs_filter_size:
    orfs_filter_size = int(orfs_filter_size)
else:
    orfs_filter_size = 6;

total_count = 0
with open (output_file, 'w') as outf:
    for key in all_seqs_dic:
        s_pair_list =  all_seqs_dic[key]
        s_pair_list = sorted(s_pair_list, reverse=True)
        for el in s_pair_list[:orfs_filter_size]:
            one_seq = el[1]
            outf.write('>' + one_seq[0] + '\n' + one_seq[1])
            total_count = total_count + 1

print("Seq count after filter: " + str(total_count))

