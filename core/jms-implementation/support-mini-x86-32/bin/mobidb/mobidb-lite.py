#!/usr/bin/env python

'''
MobiDB-lite, version 1.0, March 2016
By Damiano Piovesan, Marco Necci & Silvio C.E. Tosatto
BiocomputingUP lab, Padua, Italy
'''

import sys, os, subprocess, shlex, re, argparse
from multiprocessing import Pool
from tempfile import NamedTemporaryFile
#from multiprocessing.pool import ThreadPool
import json


########################################################################
#	Predictors calls
########################################################################


def iupred_pred(binDirectory, seq, flavour, architecture):
	args = ['{0}/iupred/bin{1}/iupred_string'.format(binDirectory, architecture),str(seq),flavour]
	proc = subprocess.Popen(args,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
	
	output = proc.communicate()[0]
	if proc.returncode == 0:
		
		probs = []
		for r in output.strip().split("\n"):
			if(r[0] != "#"):
				r = r.strip().split()
				probs.append(round(float(r[2]),4))

		return probs
		
	return None

def run_iupred(flatFile, binDirectory, architecture):

	seq = ""
	with open(flatFile) as f:
		for line in f:
			seq += line

	probss = iupred_pred(binDirectory, seq, "short", architecture)
	probsl = iupred_pred(binDirectory, seq, "long", architecture)
    
	all_probs = []
	if probss:
		all_probs.append({ "pred" : "iups", "p" : probss })
	if probsl:
		all_probs.append({ "pred" : "iupl", "p" : probsl })
	
	return all_probs if all_probs else None
		


def run_espritz(disbinFile, binDirectory, flavour, architecture):
	
	ESPRITZPATH = "{0}/espritz/bin".format(binDirectory)
	command = '{0}/espritz/bin/bin{3}/disbin{2} {0}/espritz/bin/model_definition/ensemble{2} {1} /dev/null'.format(binDirectory,disbinFile,flavour,architecture)
	args = shlex.split(command)
	proc = subprocess.Popen(args,stdout=subprocess.PIPE,cwd=ESPRITZPATH)#,stderr=subprocess.PIPE)
	output = proc.communicate()[0]
		
	if proc.returncode == 0:
		
		probs = []
		
		for r in output.split("\n"):
			if len(r) == 0:
				continue

			if r[0] in ["O","D"]:
				probs.append(round(float(r.split()[1]),4))
		
		return [{ "pred" : "esp"+flavour, "p" : probs }]
	return None
    
    

def run_globplot(flatFile, binDirectory, architecture):
	command = "python {0}/globplot/GlobPipe.py 10 15 74 4 5 {1} {0}/globplot/bin{2}".format(binDirectory, flatFile, architecture)
	#print command
	args = shlex.split(command)
	proc = subprocess.Popen(args,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
	output = proc.communicate()[0]
	if proc.returncode == 0:
		
		return eval(output)
	return None

def run_disembl(flatFile, binDirectory, architecture):
	command = 'python {0}/disembl/DisEMBL.mobidb.py 8 8 4 1.2 1.4 1.2 {0}/disembl/bin{2} {1}'.format(binDirectory, flatFile, architecture)
	args = shlex.split(command)
	proc = subprocess.Popen(args,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
	output = proc.communicate()[0].rstrip("\n")
	if proc.returncode == 0:
		
		return eval(output)
	return None


'''
def get_jronn_probs(output):
    probs = []
    for row in output.split("\n"):
        if len(row) > 0 and row[0] != ">":
            probs.append(row.split()[1])

    return probs
'''


def get_vsl_probs(output):
	processlines = False

	probs = []
	for row in output.split("\n"):
		if row == "----------------------------------------":
			processlines = True
		elif row == "========================================":
			processlines = False
		elif processlines and "-" not in row:
			probs.append(round(float(row.split()[2].replace(',','.')),4))

	return(probs)

def run_vsl2b(flatFile, binDirectory):

	command = "java -XX:+UseSerialGC -jar {0}/vsl2/VSL2.jar -s:{1}".format(binDirectory,flatFile)
	args = shlex.split(command)
	proc = subprocess.Popen(args,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
	output = proc.communicate()[0]
	if proc.returncode == 0:
		
		probs = get_vsl_probs(output)
		
		if probs: #may not get results if sequence has non-standard residues (X,etc)outfile
			return [{ "pred" : "vsl", "p" : probs }]
	return None    


def generate_files(acc, seq):
	
	f_disbin = NamedTemporaryFile(delete=False, prefix="{0}_disbin".format(acc))		#
	f_flat = NamedTemporaryFile(delete=False, prefix="{0}_flat".format(acc))			#
	
	f_disbin.write("1\n{0}\n{1}".format(len(seq),seq))
	f_flat.write(seq)
	
	return f_disbin.name, f_flat.name
	

def delete_files(disbinFile, flatFile):
	
	os.remove(disbinFile)
	os.remove(flatFile)
	
	return


'''
Parallel call to predictors
'''
def run_mobidb(binDirectory, threads, extendedOutput, acc, seq, architecture, verbose = False ):		

	#	Write input files
	disbinFile, flatFile = generate_files(acc, seq)

	#print threads
	pool = Pool(threads)
	#pool = ThreadPool(threads)

	result = []
		
	result.append(pool.apply_async(run_espritz,  [disbinFile, binDirectory, "X", architecture]) )
	result.append(pool.apply_async(run_espritz,  [disbinFile, binDirectory, "N", architecture]) )
	result.append(pool.apply_async(run_espritz,  [disbinFile, binDirectory, "D", architecture]) )
	result.append(pool.apply_async(run_globplot, [flatFile, binDirectory, architecture]) )   
	#result.append(pool.apply_async(run_vsl2b,    [flatFile, binDirectory]) )
	result.append(pool.apply_async(run_iupred,   [flatFile, binDirectory, architecture]) )	# x2
	result.append(pool.apply_async(run_disembl,  [flatFile, binDirectory, architecture]) )	# x2
	
	##pool.apply_async(run_jronn) # Slow !!!

	pool.close()
	pool.join()

	#	Delete input files
	delete_files(disbinFile, flatFile)
	
	#	Calculate consensus
	
	data = {}
	if result:
		for ele in result:
			if ele.get():
				for ele2 in ele.get():
					#print ele2["pred"], len(ele2["p"])
					if ele2:
						data[ele2["pred"]] = ele2["p"]
	
	if data:
		return calcConsensus(data, acc, extendedOutput = extendedOutput, verbose = verbose)
	
	return None

########################################################################
#	Consensus calculation
########################################################################

'''
Mathematical Morphology (MM) Erasion/Dilation implementation of Order/Disorder states
'''
def matMorphology(seq, rmax=3):
	# Morphological operation to eliminate spurious order inside disorder
	# and vice versa
	try:
		rmax = int(rmax)
	except:
		print "Wrong Matematical Morphology length parameter:", rmax
		sys.exit(1)

	# Disorder expansion
	seq = rmax*"D"+seq+rmax*"D"

	for r in range(1,rmax +1):
		pattern = 'D'*r + 'S'*r + 'D'*r
		new_pattern = 'D'*r + 'D'*r + 'D'*r

		for i in range(0, r +1):
			seq = seq.replace(pattern,new_pattern)

	# Disorder contraction
	seq = rmax*"S"+seq[rmax:-rmax]+rmax*"S"

	for r in range(1,rmax +1):
		pattern = 'S'*r + 'D'*r + 'S'*r
		new_pattern = 'S'*r + 'S'*r + 'S'*r

		for i in range(0, r +1):
			seq = seq.replace(pattern,new_pattern)

	return seq[rmax:-rmax]


'''
Tranforms disorder/order string into a list of disorder regions (start/end)
'''
def getRegions(state,letter = 'D'):
	
	regions = []
	start = 0
	end = 0
	for i in range(len(state) -1):
		if state[i] != state[i + 1]:
			if state[i] == "D":
				regions.append((start,end))				
			start = i + 1
		end += 1
	if state[-1] == "D":
		regions.append((start,end))
		
	return regions

'''
Filter regions shorter than cutoff
'''
def filterRegions(regions,cutoff):
	
	try:
		cutoff = int(cutoff)
	except:
		print "Wrong Disorder region length cutoff:", cutoff
		sys.exit(1)
		
	filteredRegions = []
	for start,end in regions:
		if (end-start+1) >= cutoff:
			filteredRegions.append((start,end))
	
	return filteredRegions


def regions2set(regions):
	positions = set()	
	for start,end in regions:
		positions.update(range(start,end + 1))	
	return positions
    
def regions2state(regions,length):
	state = ""
	
	positions = regions2set(regions)
	
	for i in range(0,length):
		if i in positions:
			state += "D"
		else:
			state += "S"
	
	return state			


'''
Calculate long disorder ModiDB consensus
'''
def calcConsensus(data, accession, agreement_th = (5.0/8.0), mm = 3, mergeRegions = True, regionLenCutoff = 20, extendedOutput = False, verbose = False):
    
	predCutoff = {
		'dis465':0.5,
		'disHL':0.086,
		'espD':0.5072,
		'espN':0.3089,
		'espX':0.1434,
		'iupl':0.5,
		'iups':0.5,
		'glo':0.0,
		'vsl':0.5,
		'jronn':0.5 
	}
    
	start = True
	
	l = len(data[data.keys()[0]])	# sequence length
	for m in data: 
		if len(data[m]) != l:			
			start = False
			break
	
	if start:
		n_m = len(data)			# NUmber of methods
		
		#	Cont disorder predictions for each position
		agreement = [0.0] * l   # Number of methods agree (sequence length)
		for m in data: 
			cutoff = predCutoff[m]
			for index,p in enumerate(data[m]):
				if p >= cutoff:
					agreement[index] += 1.0
		
		#	Calculate majority
		for i in range(len(agreement)):
			agreement[i] = round(float(agreement[i]) / n_m,2)
		
		consensus = ""
		for i in range(len(agreement)):    
			if agreement[i] > agreement_th:
				consensus += 'D'                    
			else:
				consensus += 'S'
		 
		#	Apply Mathematical Morphology
		if mm:
			consensus = matMorphology(consensus, rmax=mm)
			
		#	Merge close disorder regions
		if mergeRegions:
			sentinel=True
			
			while sentinel:
				m = re.search("D{21,}S{1,10}D{21,}", consensus)
				if m:
					matchLength = m.end(0) - m.start(0)
					consensus = consensus[:m.start(0)] + "D" * matchLength + consensus[m.end(0):]
				else: 
					sentinel = False
			
		regions = getRegions(consensus,letter = 'D')			#	Transform consensus string into intervals
		regions = filterRegions(regions, regionLenCutoff)		#	Remove short regions
		consensus = regions2state(regions, l)					#	Regions back to string
		regions = [ (r[0]+1, r[1]+1) for r in regions]			#	Shift positions (1 = first residue)
		
		if regions:
			if extendedOutput:		
				return json.dumps({ "acc" : acc, "pred":"mobidb-lite", "n_pred" : n_m,  "consensus" : consensus, "regions" : regions, "p" : agreement }	, indent=2, sort_keys=True)	
			else:			
				return "\n".join(["{0}\t{1}\t{2}".format(acc,r[0],r[1]) for r in regions])
	else:
		if verbose:	
			return "{0}\tERROR: Different output size ({1})".format(acc, ",".join(["{0}:{1}".format(m,len(data[m])) for m in data]))
			
	return None



########################################################################
#	Main
########################################################################

'''
Command line arguments parser
'''
def argParser():
	
	parser = argparse.ArgumentParser(prog='mobidb-lite.py',description="MobiDB-lite: long disorder consensus predictor",
					formatter_class=argparse.ArgumentDefaultsHelpFormatter)
	
	parser.add_argument('fastaFile',
						help='Fasta input file. Multi-fasta is allowed')
	parser.add_argument('-bin','--binDirectory',default=None,
						help='Directory of binary executables')
	parser.add_argument('-o','--outFile',default='',
						help='Output file name')
	parser.add_argument('-a','--architecture',default='64', choices=['64','32'],
						help='System architecture')					
	parser.add_argument('-t','--threads',default=7, type=thread_type,
						help='Number of parallel threads')
	parser.add_argument('-l','--longOutput',action='store_true',default=False)
	parser.add_argument('-v','--verbose',action='store_true',default=False)
						
	# Initialize the parser object
	args=parser.parse_args()

	return args

def thread_type(x):
    x = int(x)
    if x > 7:
        raise argparse.ArgumentTypeError("Maximum parallel threads: 7")
    return x

if __name__ == "__main__":
	
	args = argParser()
	
	#	Assign bin directory
	binDirectory = None
	if not args.binDirectory:
		binDirectory = os.getcwd() + "/binx"
	else:
		binDirectory = args.binDirectory
	
	#	Set "IUPred_PATH" environment variable 
	os.environ["IUPred_PATH"] = "{0}/iupred/bin{1}".format(binDirectory, args.architecture)
	
	#	Open output file
	if args.outFile:
		fout = open(args.outFile, "w")
		
	#	Parse fasta input
	acc = None
	seq = ""
	with open(args.fastaFile) as f:
		for line in f:
			line = line.strip()
			if line:
				if line[0] == ">":
					
					#	Skip first empty cycle
					if acc:

						#	Run programs
						out = run_mobidb(binDirectory, args.threads, args.longOutput, acc, seq, args.architecture, verbose = args.verbose)
						if out:
							if args.outFile:
								fout.write(out + "\n")
							else:
								print out
						else:
							if args.verbose:
								print "{0}\tWARNING: Disorder regions not found".format(acc)
						
											
					acc = line.split()[0][1:]
					seq = ""
				else:
					seq += line
	
	#	Run programs (last entry)
	out = run_mobidb(binDirectory, args.threads, args.longOutput, acc, seq, args.architecture, verbose = args.verbose)			
	if out:
		if args.outFile:
			fout.write(out + "\n")
			fout.close()
		else:
			print out
	else:
		if args.verbose:
			if args.verbose:
				print "{0}\tWARNING: Disorder regions not found".format(acc)


	
