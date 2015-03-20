Copyright (C) 2007 Paul Thomas
This file may be copied and redistributed freely, without advance permission,
provided that this Copyright statement is reproduced with each copy. 

LIMITATION OF WARRANTY
NOTHING IN THIS AGREEMENT WILL BE CONSTRUED AS A REPRESENTATION MADE OR
WARRANTY GIVEN BY PAUL THOMAS OR ANY THIRD PARTY THAT THE USE OF
DATA PROVIDED HEREUNDER WILL NOT INFRINGE ANY PATENT, COPYRIGHT, TRADEMARK
OR OTHER RIGHTS OF ANY THIRD PARTY. DATA IS PROVIDED "AS IS" WITHOUT
WARRANTY OF ANY KIND WHATSOEVER, EXPRESS OR IMPLIED, INCLUDING IMPLIED
WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. PAUL
THOMAS MAKES NO WARRANTY THAT ITS DATA DOES NOT CONTAIN ERRORS.

############################################################################

PANTHER HMM scoring tools - Version 1.03
http://www.pantherdb.org/downloads/
4/10/2011

##########
Introduction: 

This tool is used to score protein sequences against the PANTHER HMM library, to help infer protein function.

If you have any questions, please contact us at: feedback@pantherdb.org
##########
Requirements:

1. PANTHER HMM library (ftp://ftp.pantherdb.org/panther_library/current_release/)
2. UNIX
3. Perl
4. HMMER - Download from   
ftp://selab.janelia.org/pub/software/hmmer/2.3.2/hmmer-2.3.2.tar.gz
Please note that this is an archived version of HMMER2.  The current release is HGMMER3.  The panther scoring script does not support HMMER3.
5. Blast - Downloaded from
ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/LATEST/ncbi-blast-2.2.24+-x64-linux.tar.gz
Please note that the output of this release has slightly different format, so the blast.pm module has been modified modified.

the location to HMMER, Perl, and BLAST binaries must be defined in your $PATH variable.  If you have any questions on how to set up $PATH, please contact your UNIX system administrator.

##########
Usage:

% cd pantherScore1.03
% source panther.cshrc
% ./pantherScore.pl -l <panther_hmm_library> -D B -V -i <fasta file> -o <output file> -n

-panther.cshrc is a sample cshrc file.  You just need to make sure the lib directory is in your PERL5LIB variable path 
-alternatively, for the -D option, use A, to get back additional hits, rather than just the best hit
-an example fasta file was included called test.fasta

##########
Sample output:
test    PTHR19264:SF179 G-PROTEIN COUPLED RECEPTOR HOMOLOG      3e-246  828.9
1-370,

-tab delimited file in the following format:
  col 1 - sequence ID
  col 2 - PANTHER accession if (containts :SF, is a subfamily HMM)
  col 3 - PANTHER family or subfamily name
  col 4 - HMM evalue score, as reported by HMMER
  col 5 - HMM score, as reported by HMMER (not used by PANTHER)
  col 6 - alignment range of protein for this particular HMM

##########
Algorithm Information:

-the protein sequence is blasted against the consensus sequences for each of the HMMs in the PANTHER library.
-based on the blast results, a set of heuristics are used to determine which HMMs the protein needs to be scored against.  The details for this can be seen in the getHmmsToScoreAgainst subroutine of pantherScore.pl.  Here are the basics:
   -if the best hit has an eval of zero, take the top 2 hits with eval > 0
   -if the best score is nonzero, but really small (1e-70), take the top 5 hits
   -if the top hit is a litte "weak" (worse than 1e-70), get all hits until 
    hit an evalue of 0.1...or...until the evalue drop off (the top evalue
    divided by the evalue of the hit being examined) is greater than 1e-15 and 
    we have found at least 5 hits
 These heuristics greatly reduce the number of HMMs that the protein needs to
 be scored against.  Because we do not have to score the protein against
 all ~70,000 PANTHER HMMs, the process is extremely fast.
-the protein is then scored against each of these HMMs, as determined from the 
 step above
-the best hit is determined by the HMM with the best HMMER e-value.  If multiple HMMs for that protein have the same HMMER e-value, then the HMMER (raw) score is used to determine what the best hit is


Notes: 
-because the tools only score the proteins against a subset of the HMMs, we do not use hmmpfam, and do not bundle all of our hmm files into a single file.
-a subset of the PANTHER HMMs have been released to the InterPro.  If you have obtained this README file through the InterProScan download and would like to download all the PANTHER HMMs to score your proteins against, please visit: http://panther.appliedbiosystems.com/downloads/

########## 
Interpretation of scores:

-closely related: if the score is better than E-23 (very likely to be a correct functional assignment)  
-related : if the score is better than E-11, but worse than E-23 (molecular function likely to be the correct but biological process/pathway less certain) 
-distantly related : if the score is better than E-3, but worse than E-11 (protein is evolutionarily related but function may have diverged) 

##########
Generation of consensus sequences for each HMM:

For each match state in the HMM, we take the emission probabilities and normalize them by the background probabilities.  If the maximal normalized emission probability is greater than 1.5, the corresponding amino acid becomes the consensus amino acid. If the maximal normalized probability is below 1.5, the consensus at this position will be "X". 

##########

Troubleshooting:

Ultimately, if you have any problems, please contact us at: feedback@pantherdb.org

but, before you do that it would be helpful if you can try each of the the following commands:
% hmmsearch PANTHER6.1/books/PTHR19264/hmmer.hmm test.fasta
% hmmsearch -Z 10000 PANTHER6.1/books/PTHR19264/hmmer.hmm test.fasta
% hmmsearch --cpu 1 PANTHER6.1/books/PTHR19264/hmmer.hmm test.fasta

All of these commands should run properly and generate results.  If they do not, this means that you have a problem with the way you installed hmmsearch, or more likely, you have a problem with how you compiled HMMER.

In particular, if you have problems with the --cpu option (if you have problems with this option you might see a POSIX or threads error), you should download and recompile HMMER so that it properly works with threads.  HMMER can be downloaded from http://hmmer.wustl.edu/.

If you send an email to PANTHER feedback, please tell us the command you are using, and send us the fasta file you are using.

##########
Version History:

version 1.03 - update hmm.pm and blast.pm modules in the lib/ directory to support the changes in the HMMER2 and BLAST algorithms.
version 1.02 - updated document, so that users can better trouble shoot errors with running the program (specifically, the threads issue)  8/23/07
version 1.01 - minor change.  if gaps between alignment ranges are <= 15 positions, then the alignment ranges are joined.


If you have any questions, please contact us at: feedback@pantherdb.org

