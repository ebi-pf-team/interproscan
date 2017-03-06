#parse the hmmer3 tblout file
#run the pfsearch binary
#gift nuka
#jan 2015, mar 2015


import subprocess
import sys
import os.path
import os
import re
import time

from tempfile import NamedTemporaryFile

#reformat pfsearch alignment output to appear on one line
def clean_output(lines):
  line_list = iter(lines.splitlines())
  formatted_lines  = ''

  for line in line_list:
    if not line.strip():
      continue
    if 'match_nb' in line:
      line = re.sub('match_nb=\S+\s+match_type=\S+\s+', '', line)
      if 'match_nb' in line:
        #print 'regex failed ... ', line
        line = re.sub('match_nb=\S+\s+', '', line)
        line = re.sub('match_type=\S+\s+', '', line)
        #print 'regex may hav failed, print line again: ', line
	#else print 'regex worked ... ', line
    if formatted_lines and len(line) > 0 and line[0] == '>':
      formatted_lines += '\n'
      formatted_lines += line
    else:
      formatted_lines += line
  #print formatted_lines
  if not formatted_lines.strip():
    formatted_lines = ''
  else:
    formatted_lines += '\n'
  #print formatted_lines
  return formatted_lines

##get the hamap profiles from the
def get_hamap_profile(profiles_list_filename):
    profiles = {}
    lines = []
    if not os.path.isfile(profiles_list_filename):
        return profiles

    temp_err_file = profiles_list_filename + '-filtered'
    with open(profiles_list_filename, "r") as profile_list:
        for line in profile_list:
            line = line.strip()
            if not line.startswith('#'):
                lines.append(line)
                m = re.search('^(\S+)\s+(\S+)\s+(\S+)\s+(.*)', line)
                hit_line = ''
                if m:
                    seq_id = m.group(1)
                    profile = m.group(3)
                    profile_path = model_dir + '/' + profile + ".prf"
                    #print line
                    hit_line = 'ok - ' + line
                    if profile in profiles:
                       profiles[profile].extend([seq_id])
                    else:
                       profiles[profile] = [profile_path, seq_id]
                       #print profiles[profile]
                else:
                    print ('something wrong ' + line)
                    sys.stderr.write('something wrong ' + line)
                    hit_line = 'not ok - ' + line
                #append_to_file(temp_err_file, hit_line+'\n') 
    key_count = len(profiles.keys())
    append_to_file(temp_err_file, 'profile hits: ' + str(key_count) + '\n')
    return profiles

def get_sequences(fasta_file):
    fasta = open(fasta_file, 'r')
    fasta_dict = {}
    for line in fasta:
        line = line.strip()
        if line == '':
            continue
        if line.startswith('>'):
            seq_id = line.lstrip('>')
            seq_id = re.sub('\..*', '', seq_id)
            fasta_dict[seq_id] = ''
        else:
            fasta_dict[seq_id] += line + '\n'
            if len(line) > 80:
                raise ValueError('Input fasta file format problem for pfsearch, line length greater than 80 ')
    fasta.close()
    return fasta_dict

def get_sequences_for_profile(key_list, seqs_dict):
    sequences = ''
    #print len(key_list),':', key_list
    for key in key_list:
        if key in seqs_dict:
            #print "key found : ", key
            value = seqs_dict[key]
            sequences += '>' + key + '\n' + value
            if not value.endswith('\n'):
                sequences += '\n'
	    #print 'ok', sequences
        else:
            print ("key not found : " + key)

    #print 'ok', sequences
    return sequences

def create_temp_file(filename, temp_dir):
    file_prefix = filename + '.'
    #temp_dir = '/nfs/nobackup/interpro/nuka/i5/build/release-5.18-hamap/may06-test/temp/tmp'
    f = NamedTemporaryFile(delete=False, prefix=file_prefix, dir=temp_dir)
    return f.name

def append_to_file(filename, output):
    with open(filename, 'a') as out_file:
        out_file.write(output)

def write_to_file(filename, output):
    with open(filename, 'w') as seq_file:
        seq_file.write(output)  

def run_pfsearch_binary(arg_list, profiles, seqs_dict, input_fasta_file, command_index):
    count = 0
    temp_file_list = []
    stats_filename = input_fasta_file + ".stats"
    #base_job_dir = os.path.dirname(stats_filename)
    temp_dir = input_fasta_file + '-tmp'
    os.makedirs(temp_dir)
    #prf_half = int(len(profiles) / 2)
    temp_err_file = profiles_list_filename + '-filtered'
        
    append_to_file(temp_err_file, 'profile hits in run_pfsearch: ' + str(len(profiles.keys())) + '\n')

    append_to_file(temp_err_file, 'sequence count in run_pfsearch: ' + str(len(seqs_dict.keys())) + '\n')
    get_seq_time = 0
    keys = profiles.keys()
    write_to_file(temp_err_file + '-keys', 'profile keys in run_pfsearch: ' + str(len(keys)) + '\n')
    for prf in profiles:
        prf_seqs = ' '.join(profiles[prf])
        prf_seqs_count = len(profiles[prf][1:])
        prf_out = 'processing profile #:' + str(count) + ' ' +  prf + ' seqs:' + str(prf_seqs_count) + ' - ' + prf_seqs + '  \n'
        append_to_file(temp_err_file, prf_out)
        if prf == 'MF_00005':
            #append_to_file(temp_err_file, 'processing MF_00005 \n')
            mf_output = ' '.join(profiles[prf])            
            #append_to_file(temp_err_file, mf_output)
            #append_to_file(temp_err_file, ' ok \n ')
        sequence_ids = profiles[prf][1:]
        get_seq_start_time = time.time()
        input_fasta_sequences = get_sequences_for_profile(sequence_ids, seqs_dict)
        #print input_fasta_sequences
        #temp_file = create_temp_file(prf, temp_dir)
        temp_file = temp_dir + '/' + prf
        temp_output_file = temp_file + '.raw.out'
        #print 'temp file is ', temp_file
        write_to_file(temp_file, input_fasta_sequences)
        get_seq_end_time = time.time()
        time_to_get_seqences = get_seq_end_time - get_seq_start_time
        #print 'time to get ', len(sequence_ids), 'sequences for ', prf, ': ', time_to_get_seqences
        get_seq_time += time_to_get_seqences
        temp_file_list.append(temp_file)

        comd_to_run = []
        comd_to_run = arg_list[command_index:]
        #comd_to_run.extend([profiles[prf][0],fasta_file])
        comd_to_run.extend([profiles[prf][0],temp_file])
        cmd_string = ' '.join(comd_to_run)
        #print "command to run: ",  cmd_string
        count = count  + 1
        #append_to_file(temp_err_file, "command to run: " +  cmd_string + ' \n')
        if not os.path.isfile(profiles[prf][0]):
            #profile not available
            continue
        output = subprocess.check_output(comd_to_run)
        #append_to_file(temp_err_file, "command to run: " +  cmd_string + ' \n')
        #if prf == 'MF_00005':
        #    append_to_file(temp_err_file, "command to run: " +  cmd_string + ' \n')
        #    with open(temp_output_file, 'a') as out_temp_file:
        #        out_temp_file.write(output)
        output = clean_output(output)
        if output.strip():
            with open(output_file, 'a') as out_file:
                out_file.write(output)
        #if count > prf_half:
        #    break

    for tempfile in temp_file_list:
        #print tempfile
        #todo remove comment
        #os.unlink(tempfile)
        testcount = 1

    append_to_file(temp_err_file, 'completed running thru ' + str(count) + ' profiles \n')
    with open(stats_filename, 'w') as stats_file:
        stats_file.write('Total time to get and write ' + str(len(temp_file_list)) + ' seq files :' + str(get_seq_time * 1000) + " ms \n")
    return count

if __name__ == "__main__":

    if sys.version_info<(2,7,0):
        sys.exit("Error: You need python 2.7 or later to run this script")

    #deal with arguments that are required
    if len(sys.argv) < 6:
        sys.exit("Error: expected more than 6 arguments, check your command again")

    try:
        arg_list = sys.argv
        profiles_list_filename = arg_list[1]
        fasta_file = arg_list[2]
        stats_filename = arg_list[3]
        output_file = arg_list[4]
        model_dir = arg_list[5]
        command_index = 6

        pfsearch_cmd = ' '.join(sys.argv)
        #print pfsearch_cmd

        #create the output file in case we don't have any matches
        open(output_file, 'a').close()

        start_time = time.time()

        #get the profiles
        profiles =  get_hamap_profile(profiles_list_filename)

        if len(profiles) > 1:
            #get the protein sequences
            seqs_dict = get_sequences(fasta_file)

            end_time = time.time()
            read_file_time = end_time - start_time
            start_time = time.time()

            #stats_filename = fasta_file + ".stats"
            #run the pfsearch binary
            pfsearch_cmd_run_count = run_pfsearch_binary(arg_list, profiles,seqs_dict, fasta_file, command_index)
            sys.stderr.write('prfs: ' + str(count))
    except:
        print (sys.version)
        print ("Unexpected error: ")
        print (sys.exc_info())




