#parse the hmmer3 tblout file
#run the pfsearch binary
#gift nuka
#jan 2015

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
  print lines

  for line in line_list:
    if not line.strip():
      continue
    if 'match_nb' in line:
      line = re.sub('match_nb=\S+\s+match_type=\S+\s+', '', line)
      print line
      if 'match_nb' in line:
        print 'regex failed ... ', line
        line = re.sub('match_nb=\S+\s+', '', line)
        line = re.sub('match_type=\S+\s+', '', line)
        print 'regex may hav failed, print line again: ', line
      else:
	print 'regex worked ... ', line
    #print line
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
  print formatted_lines
  return formatted_lines

##get the hamap profiles from the
def get_hamap_profile(profiles_list_filename):
    profiles = {}
    lines = []
    if not os.path.isfile(profiles_list_filename):
        return profiles

    with open(profiles_list_filename, "r") as profile_list:
        for line in profile_list:
            line = line.strip()
            if not line.startswith('#'):
                lines.append(line)
                m = re.search('^(\S+)\s+(\S+)\s+(\S+)\s+(.*)', line)
                seq_id = m.group(1)
                profile = m.group(3)
                profile_path = model_dir + '/' + profile + ".prf"
                #print line

                if profile in profiles:
                    profiles[profile].extend([seq_id])
                else:
                    profiles[profile] = [profile_path, seq_id]
                    #print profiles[profile]
    return profiles

def get_sequences(fasta_file):
    fasta= open(fasta_file, 'r')
    fasta_dict= {}
    for line in fasta:
        line= line.strip()
        if line == '':
            continue
        if line.startswith('>'):
            seq_id= line.lstrip('>')
            seq_id= re.sub('\..*', '', seq_id)
            fasta_dict[seq_id]= ''
        else:
            fasta_dict[seq_id] += line
    fasta.close()
    return fasta_dict

def get_sequences_for_profile(key_list, seqs_dict):
    sequences = ''
    print len(key_list),':', key_list
    for key in key_list:
        if key in seqs_dict:
            #print "key found : ", key
            value = seqs_dict[key]
            sequences += '>' + key + '\n' + value + '\n'
	    #print 'ok', sequences
        else:
            print "key not found .. "

    #print 'ok', sequences
    return sequences

def create_temp_file(filename):
    file_prefix = filename + '.'
    f = NamedTemporaryFile(delete=False, prefix=file_prefix)
    return f.name

def write_to_file(filename, output):
    with open(filename, 'w') as seq_file:
        seq_file.write(output)  

def run_pfsearch_binary():
    print "run actual pfsearch command"

def run_pfsearch_binary(arg_list, profiles, seqs_dict, stats_filename, command_index):
    count = 0
    temp_file_list = []
    prf_half = int(len(profiles) / 2)
    get_seq_time = 0
    for prf in profiles:
        sequence_ids = profiles[prf][1:]
        get_seq_start_time = time.time()
        input_fasta_sequences = get_sequences_for_profile(sequence_ids, seqs_dict)
        #print input_fasta_sequences
        temp_file = create_temp_file(prf)
        print 'temp file is ', temp_file
        write_to_file(temp_file, input_fasta_sequences)
        get_seq_end_time = time.time()
        time_to_get_seqences = get_seq_end_time - get_seq_start_time
        print 'time to get ', len(sequence_ids), 'sequences for ', prf, ': ', time_to_get_seqences
        get_seq_time += time_to_get_seqences
        temp_file_list.append(temp_file)

        comd_to_run = []
        comd_to_run = arg_list[command_index:]
        #comd_to_run.extend([profiles[prf][0],fasta_file])
        comd_to_run.extend([profiles[prf][0],temp_file])
        cmd_string = ' '.join(comd_to_run)
        print "command to run: ",  cmd_string
        count = count  + 1
        output = subprocess.check_output(comd_to_run)
        output = clean_output(output)
        if output.strip():
            with open(output_file, 'a') as out_file:
                out_file.write(output)
        #if count > prf_half:
        #    break

    for tempfile in temp_file_list:
        print tempfile
        os.unlink(tempfile)

    with open(stats_filename, 'w') as stats_file:
        stats_file.write('Total time to get and write ' + str(len(temp_file_list)) + ' seq files :' + str(get_seq_time * 1000) + " ms \n")
    return count

if __name__ == "__main__":

    #deal with arguments that are required
    if len(sys.argv) < 6:
        sys.exit("Error: expected more than 6 arguments, check your command again")

    arg_list = sys.argv
    profiles_list_filename = arg_list[1]
    fasta_file = arg_list[2]
    stats_filename = arg_list[3]
    output_file = arg_list[4]
    model_dir = arg_list[5]
    command_index = 6

    pfsreach_cmd = ' '.join(sys.argv)
    print pfsreach_cmd

    #create the output file in case we dont have any matches
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
	
	##print some interesting stats
        stats_filename = fasta_file + ".stats"
        #run the pfsearch binary
        pfsearch_cmd_run_count = run_pfsearch_binary(arg_list, profiles,seqs_dict, stats_filename, command_index)

        # end_time = time.time()
        #
        # pfsearch_time = end_time - start_time
        #
        # if pfsearch_cmd_run_count:
        #     avg_per_cmd = int(pfsearch_time * 1000 / pfsearch_cmd_run_count )
        # else:
        #     avg_per_cmd = 0
        # with open(stats_filename, 'a') as stats_file:
        #     stats_file.write("pfsearch cmd run count = " + str(pfsearch_cmd_run_count) + "\n")
        #     stats_file.write("read file time =  " + str(read_file_time * 1000) + " ms \n")
        #     stats_file.write("pfsearch_time = " +  str(pfsearch_time) + "\n")
        #     stats_file.write("pfsearch time avg = " + str(avg_per_cmd) + " ms \n")





