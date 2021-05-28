import  os, sys, time
import shelve
import random
from subprocess import call
import subprocess

#run command
def runSystemCall(command):
    returnStatus = call([command], shell=True)
    if returnStatus:
        print ("Problem running system call:", command)

def  absoluteFilePaths(directory):
  for  dirpath,_,filenames in  os.walk(directory):
        #print dirpath
        for  f in  filenames:
            yield  os.path.abspath(os.path.join(dirpath,  f))

def absoluteFilePaths2(directory):
  input_files = []
  for root, dirs, files in os.walk(directory):
    for file in files:
        p=os.path.join(root,file)
        #print p
        print (os.path.abspath(p))
        input_files.append(os.path.abspath(p))
  return input_files.sort()

if __name__ == "__main__":

    if sys.version_info<(3,0,0):
        sys.exit("Error: You need python 3.0 or later to run this script")

    #deal with arguments that are required
    if len(sys.argv) < 6:
        sys.exit("Error: expected more than 6 arguments, check your command again")

    try:
        arg_list = sys.argv
        models_dir = arg_list[1]
        fasta_file = arg_list[2]
        output_file = arg_list[3]
        binary_command = arg_list[4]
        binary_switches = arg_list[5:]

        pfsearch_cmd_switches = ' '.join(binary_switches)
        #print pfsearch_cmd

        #print (binary_switches)
        #print (binary_command + " " + pfsearch_cmd_switched)
        profiles =  absoluteFilePaths(models_dir)
        models = []
        for profile in profiles:
            models.append(profile)
        #print(models_dir)
        #print(len(models))
        try:
            for el in models:
                #print (el)
                #cmd = f"{binary_command} {pfsearch_cmd_switches} {el} {fasta_file} "
                cmd = "{} {} {} {} ".format(binary_command, pfsearch_cmd_switches, el, fasta_file)
                #print (cmd)
                #runSystemCall(cmd)
                cmd_els = cmd.split()
                output = subprocess.check_output(cmd_els, universal_newlines=True)
                if output.strip():
                   with open(output_file, 'a') as out_file:
                       out_file.write(output + '\n')
        except:
            print(sys.version)
            print("Error running pfsearchV3 - Unexpected error: " + cmd)
            print(sys.exc_info())
            sys.exit("Error running pfsearchV3 - Unexpected error: " + cmd) 
    except:
        print(sys.version)
        print("Unexpected error while running prosite binary: ")
        print(sys.exc_info())
        sys.exit("Error running prosite binary " + binary_command)
