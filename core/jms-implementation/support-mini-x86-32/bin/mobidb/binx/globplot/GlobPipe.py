#!/usr/bin/env python
# Copyright (C) 2003 Rune Linding - EMBL
# GlobPlot TM
# GlobPlot is licensed under the Academic Free license


from string import *
from sys import argv
import fpformat
import sys
import os
from os import system,popen3
import math

try:
    smoothFrame = int(sys.argv[1])
    DOM_joinFrame = int(sys.argv[2])
    DOM_peakFrame = int(sys.argv[3])
    DIS_joinFrame = int(sys.argv[4])
    DIS_peakFrame = int(sys.argv[5])
    fileName = str(sys.argv[6])    
    BINXPATH = str(sys.argv[7])
    #outfilename = str(sys.argv[8])
    
except:
    print 'Usage:'
    print '         ./GlobPipe.py SmoothFrame DOMjoinFrame DOMpeakFrame DISjoinFrame DISpeakFrame FASTAfile'
    print '         Optimised for ELM: ./GlobPlot.py 10 8 75 8 8 sequence_file'
    print '         Webserver settings: ./GlobPlot.py 10 15 74 4 5 sequence_file'
    raise SystemExit

# Russell/Linding
RL = {'N':0.229885057471264,'P':0.552316012226663,'Q':-0.187676577424997,'A':-0.261538461538462,'R':-0.176592654077609, \
      'S':0.142883029808825,'C':-0.0151515151515152,'T':0.00887797506611258,'D':0.227629796839729,'E':-0.204684629516228, \
      'V':-0.386174834235195,'F':-0.225572305974316,'W':-0.243375458622095,'G':0.433225711769886,'H':-0.00121743364986608, \
      'Y':-0.20750516775322,'I':-0.422234699606962,'K':-0.100092289621613,'L':-0.337933495925287,'M':-0.225903614457831}

def Sum(seq,par_dict):
    sum = 0
    results = []
    raws = []
    sums = []
    p = 1
    for residue in seq:
        try:
            parameter = par_dict[residue]
        except:
            parameter = 0
        if p == 1:
            sum = parameter
        else:
            sum = sum + parameter#*math.log10(p)
        ssum = float(fpformat.fix(sum,10))
        sums.append(ssum)
        p +=1
    return sums

def getSlices(dydx_data, DOM_join_frame, DOM_peak_frame, DIS_join_frame, DIS_peak_frame):
    DOMslices = []
    DISslices = []
    in_DOMslice = 0
    in_DISslice = 0
    beginDOMslice = 0
    endDOMslice = 0
    beginDISslice = 0
    endDISslice = 0
    for i in range( len(dydx_data) ):
    #close dom slice
        if in_DOMslice and dydx_data[i] > 0:
            DOMslices.append([beginDOMslice, endDOMslice])
            in_DOMslice = 0
    #close dis slice
        elif in_DISslice and dydx_data[i] < 0:
            DISslices.append([beginDISslice, endDISslice])
            in_DISslice = 0
        # elseif inSlice expandslice
        elif in_DOMslice:
            endDOMslice += 1
        elif in_DISslice:
            endDISslice += 1
    # if not in slice and dydx !== 0 start slice
        if dydx_data[i] > 0 and not in_DISslice:
            beginDISslice = i
            endDISslice = i
            in_DISslice = 1
        elif dydx_data[i] < 0 and not in_DOMslice:
            beginDOMslice = i
            endDOMslice = i
            in_DOMslice = 1
    #last slice
    if in_DOMslice:
        DOMslices.append([beginDOMslice, endDOMslice])
    if in_DISslice:
        DISslices.append([beginDISslice,endDISslice])
    k = 0
    l = 0
    while k < len(DOMslices):
        if k+1 < len(DOMslices) and DOMslices[k+1][0]-DOMslices[k][1] < DOM_join_frame:
            DOMslices[k] = [ DOMslices[k][0], DOMslices[k+1][1] ]
            del DOMslices[k+1]
        elif DOMslices[k][1]-DOMslices[k][0]+1 < DOM_peak_frame:
            del DOMslices[k]
        else:
            k += 1
    while l < len(DISslices):
        if l+1 < len(DISslices) and DISslices[l+1][0]-DISslices[l][1] < DIS_join_frame:
            DISslices[l] = [ DISslices[l][0], DISslices[l+1][1] ]
            del DISslices[l+1]
        elif DISslices[l][1]-DISslices[l][0]+1 < DIS_peak_frame:
            del DISslices[l]
        else:
            l += 1
    return DOMslices, DISslices


def SavitzkyGolay(window,derivative,datalist):
    SG_bin = '{0}/sav_gol -V0 '.format(BINXPATH)
    sys.path.append("")
    stdin, stdout, stderr = popen3(SG_bin + '-D' + str(derivative) + ' -n' + str(window)+','+str(window))
    for data in datalist:
        stdin.write(`data`+'\n')
    try:
        stdin.close()
    except:
        print stderr.readlines()
    results = stdout.readlines()
    stdout.close()
    SG_results = []
    for result in results:
        SG_results.append(float(fpformat.fix(result,6)))
    return SG_results


def reportSlicesTXT(slices, sequence, maskFlag):
    if maskFlag == 'DOM':
        coordstr = '|GlobDoms:'
    elif maskFlag == 'DIS':
        coordstr = '|Disorder:'
    else:
        raise SystemExit
    if slices == []:
        #by default the sequence is in uppercase which is our search space
        s = sequence
    else:
        # insert seq before first slide
        if slices[0][0] > 0:
            s = sequence[0:slices[0][0]]
        else:
            s = ''
        for i in range(len(slices)):
            #skip first slice
            if i > 0:
                coordstr = coordstr + ', '
            coordstr = coordstr + str(slices[i][0]+1) + '-' + str(slices[i][1]+1)
            #insert the actual slice
            if maskFlag == 'DOM':
                s = s + lower(sequence[slices[i][0]:(slices[i][1]+1)])
                if i < len(slices)-1:
                    s = s + upper(sequence[(slices[i][1]+1):(slices[i+1][0])])
                #last slice
                elif slices[i][1] < len(sequence)-1:
                    s = s + lower(sequence[(slices[i][1]+1):(len(sequence))])
            elif maskFlag == 'DIS':
                s = s + upper(sequence[slices[i][0]:(slices[i][1]+1)])
                #insert untouched seq between disorder segments, 2-run labelling
                if i < len(slices)-1:
                    s = s + sequence[(slices[i][1]+1):(slices[i+1][0])]
                #last slice
                elif slices[i][1] < len(sequence)-1:
                    s = s + sequence[(slices[i][1]+1):(len(sequence))]
    return s,coordstr


def runGlobPlot():
	
	# Read flat file (1 line sequence)
	seq = ""
	with open(fileName) as f:
		for line in f:
			seq += line.strip()

	seq = upper(seq)
	
	# sum function
	sum_vector = Sum(seq,RL)
	# Run Savitzky-Golay
	smooth = SavitzkyGolay(`smoothFrame`,0,sum_vector)
	dydx_vector = SavitzkyGolay(`smoothFrame`,1, sum_vector)

	#test
	sumHEAD = sum_vector[:smoothFrame]
	sumTAIL = sum_vector[len(sum_vector)-smoothFrame:]
	newHEAD = []
	newTAIL = []
	for i in range(len(sumHEAD)):
		try:
			dHEAD = (sumHEAD[i+1]-sumHEAD[i])/2
		except:
			dHEAD = (sumHEAD[i]-sumHEAD[i-1])/2
		try:
			dTAIL = (sumTAIL[i+1]-sumTAIL[i])/2
		except:
			dTAIL = (sumTAIL[i]-sumTAIL[i-1])/2
		newHEAD.append(dHEAD)
		newTAIL.append(dTAIL)
	dydx_vector[:smoothFrame] = newHEAD
	dydx_vector[len(dydx_vector)-smoothFrame:] = newTAIL
	globdoms, globdis = getSlices(dydx_vector, DOM_joinFrame, DOM_peakFrame, DIS_joinFrame, DIS_peakFrame)
	s_domMask, coordstrDOM = reportSlicesTXT(globdoms, seq, 'DOM')
	s_final, coordstrDIS = reportSlicesTXT(globdis, s_domMask, 'DIS')
	
                    
	return [{ "pred" : "glo" , "p" : [round(i,4) for i in dydx_vector] } ]

print runGlobPlot()
