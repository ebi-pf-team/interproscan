import subprocess
import os, sys

data_dir = sys.argv[1]
size_args = len(sys.argv)

if len(sys.argv) > 2:
    hmmer3_root = sys.argv[2]
else:
    hmmer3_root = '../hmmer/hmmer3/3.1b1'

hmm_log_dir = data_dir + '/log'
if not os.path.exists(hmm_log_dir):
    os.makedirs(hmm_log_dir)

hhm_output_dir = data_dir + '/hamap_hmms'
if not os.path.exists(hhm_output_dir):
    os.makedirs(hhm_output_dir)

seed_alignments = os.listdir(data_dir + '/hamap_seed_alignments')

hmmbuild = hmmer3_root + '/hmmbuild'
hmmbuild_options = ' '
hmmbuild_cmd = hmmbuild + hmmbuild_options
for sa_file in seed_alignments:
    hmm_file_root = sa_file.replace('.msa','.hmm')
    hmm_file_path = hhm_output_dir + '/'+ hmm_file_root
    hmm_file_log = hmm_log_dir + '/' + hmm_file_root + '.log'
    comd_to_run = hmmbuild_cmd + ' -o ' + hmm_file_log + ' ' + hmm_file_path + ' ' + data_dir + '/hamap_seed_alignments/' + sa_file
    #print(comd_to_run)
    output = subprocess.check_output(comd_to_run,shell=True)



