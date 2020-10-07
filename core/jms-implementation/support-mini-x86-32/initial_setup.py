#!/usr/bin/env python
# """"
# check if there are hmm models that need indexing
# """"

import os, sys
import glob
#from subprocess import run
from subprocess import call

def load_properties(filepath, sep='=', comment_char='#'):
    """
    Read the file passed as parameter as a properties file.
    """
    ipr_properties = {}
    with open(filepath, "rt") as inf:
        for line in inf:
            stripped_line = line.strip()
            if stripped_line and not stripped_line.startswith(comment_char):
                key_value = stripped_line.split(sep)
                key = key_value[0].strip()
                value = sep.join(key_value[1:]).strip().strip('"') 
                ipr_properties[key] = value 
    data_directory  =  ipr_properties['data.directory']
    bin_directory = ipr_properties['bin.directory']
    ipr_properties_resolved_paths = {}
    for key, value in ipr_properties.items():
        if '${bin.directory}' in value:
            value = value.replace('${bin.directory}', bin_directory)
        if '${data.directory}' in value:
            value = value.replace('${data.directory}', data_directory)
        ipr_properties_resolved_paths [key] = value

    return ipr_properties_resolved_paths

def can_run_hmmscan(hmms_path):
    exts = ['.h3p', '.h3m', '.h3f', '.h3i']
    return all([os.path.isfile(hmms_path + ext) for ext in exts])

def delete_files(wildcard_file):
    file_list = glob.glob(wildcard_file, recursive=True)
    for file_path in file_list:
        try:
            os.remove(file_path)
        except OSError:
            print("Error while deleting file - " + file_path)

def get_hmm_models_props(ipr_properties):
    hmm_paths = []
    for key, value in ipr_properties.items():
        if 'hmm.path' in key and not 'smart' in key:
            if not os.path.isfile(value):
                continue
            if not can_run_hmmscan(value):
                hmm_paths.append(value)
    return hmm_paths

def run_hmmpress(hmmpress_path, hmms_path):
    #run([hmmpress_path,hmms_path])
    call([hmmpress_path,hmms_path])

if __name__ == "__main__":
    ipr_properties = load_properties('interproscan.properties')
    hmmer3_dir = ipr_properties['binary.hmmer3.path']
    hmmpress_path = hmmer3_dir + '/hmmpress'
    hmm_models_paths = get_hmm_models_props(ipr_properties)
    if (len(hmm_models_paths) > 0):
        print("Checking any hmm models that need indexing ... this may take a few minutes")
        sys.stdout.flush()
        for  hmms_path in  hmm_models_paths:
            if not can_run_hmmscan(hmms_path):
                run_hmmpress(hmmpress_path, hmms_path.strip())
        print('Completed indexing the hmm models.')

