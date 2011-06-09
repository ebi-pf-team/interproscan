#!/usr/local/bin/perl

# Copyright (C)) 2005 Applied Biosystems.
# This file may be copied and redistributed freely, without advance permission,
# provided that this Copyright statement is reproduced with each copy.

# LIMITATION OF WARRANTY
# NOTHING IN THIS AGREEMENT WILL BE CONSTRUED AS A REPRESENTATION MADE OR
# WARRANTY GIVEN BY APPLIED BIOSYSTEMS OR ANY THIRD PARTY THAT THE USE OF
# DATA PROVIDED HEREUNDER WILL NOT INFRINGE ANY PATENT, COPYRIGHT, TRADEMARK
# OR OTHER RIGHTS OF ANY THIRD PARTY. DATA IS PROVIDED "AS IS" WITHOUT
# WARRANTY OF ANY KIND WHATSOEVER, EXPRESS OR IMPLIED, INCLUDING IMPLIED
# WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. APPLIED
# BIOSYSTEMS MAKES NO WARRANTY THAT ITS DATA DOES NOT CONTAIN ERRORS.

##### 
#####
##
## FamLibEntry.pm - a class to handle one book entry in the prod
##     (production/released) version of the library.  FamLibEntry_prod is a
##     subclass of FamLibEntry
##     Do not access FamLibEntry_prod directly - use FamLibEntry
#####
#####

package FamLibEntry_prod;
use FastaFile;
#use strict;

####
# constructor - creates and returns a prod instance of the  FamLibEntry 
# object. 
# - NOT TO BE CALLED DIRECTLY - use FamLibBuilder::getLibEntry()!
#
# @params: $flb - FamLibBuilder object
#          $bookName - name of book
#          $bookType - entry type (ex: nr, curated)
# @return: $self - FamLibEntry object
#####
sub new {
    my $class = shift;
    my $flb = shift;
    my $bookName = shift;
    my $self = {};
    $self->{flb} = $flb;
    $self->{bookName} = $bookName;

    bless $self, $class;
    return $self;
}

# create a FamLibEntry on the filesystem
# -Do not access directly - access through corresponding method in FamLibEntry
sub create {
    my $self = shift;

    # if we exist, don't recreate and return warning code
    return -1 if (-d $self->bookDir());

    # create ourselves
    return undef unless (&makeDir($self->bookDir()));
   
    return 1;
}

####
# check if our library exists on the filesystem
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @param none
# @returns 1 if exists, undef if doesn't
####
sub exists {
    my $self = shift;
    return 1 if (-d $self->bookDir());
    return undef;
}

####
# create our library if it doesn't exist on the system
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @param none
# @returns none
####
sub assureExists {
    my $self = shift;
    $self->create() unless ($self->exists());
}


####
# sub log() logs a message to our log file
# -log file does not exists in prod version of the library
# 
# @params $message - what to write ... \n is not implied
#         $logFile - if passed, write to this file ... undef, write to deflt
# @return nothing
####
sub log {
    my $self = shift;
    my $message = shift;
    my $logFile = shift;
    return undef;
}


####
# sub lock() locks an entry for a given process
# -lock mechanism does not work in the prod version of the library
#
# @params $type - type of lock; should be calling process name
# @return 1 on success, 0 on failure or lock already exists
####
sub lock {
    my $self = shift;
    my $type = shift;

    return undef;
}


####
# sub unlock() unlocks an entry for a given process
# -lock mechanism does not work in the prod version of the library
#
# @params $type - type of lock; should be calling process name
#                 MUST MATCH TAG USED IN lock()!
# @return 1 on success, 0 on failure or no lock
####
sub unlock {
    my $self = shift;
    my $type = shift;

    return undef;
}


####
# sub isLocked() returns an entry's lock status
# -lock mechanism does not work in the prod version of the library
#
# @params $type - type of lock; should be calling process name
#                 MUST MATCH TAG USED IN lock()!
# @return 1 on if locked, 0 on failure or no lock
####
sub isLocked {
    my $self = shift;
    my $type = shift;

    return undef;
}


####
# sub bookDir() returns the path to the book
# (one book has many entries, each of a named type)
# -Do not access directly - access through corresponding method in FamLibEntry
#
# params: none
# returns: filepath to the book
####
sub bookDir {
    my $self = shift;
    return ($self->{flb}->bookDir() . $self->{bookName} . "/");
}

####
# sub entryDir() returns the path to this entry
# (one book has many entries, each of a named type)
# -Do not access directly - access through corresponding method in FamLibEntry
#
# params: none
# returns: filepath to the entry
####
sub entryDir {
    my $self = shift;
    return undef;
}

####
# sub bookName() returns book name
# (book name comes from $fle object data structure)
# -Do not access directly - access through corresponding method in FamLibEntry
# params: none
# returns: book name
#####
sub bookName {
    my $self = shift;
    return($self->{bookName});
}

####
# sub bookType() returns book type (entry type)
# (book type comes from $fle object data structure)
# -does not exist in the prod version of the library
#
# params: none
# returns: book type
#####
sub bookType {
    my $self = shift;
    return undef;
}

####
# sub clustDir() returns the path to this entry's cluster directory
# -does not exist in the prod version of the library
#
# params: none
# returns: filepath to the cluster dir
####
sub clustDir {
    my $self = shift;
    return undef;
}

####
# sub seqwtDir() returns the path to this entry's sequence weighting directory
# -does not exist in the prod version of the library
#
# params: none
# returns: filepath to the seqwt dir
####
sub seqwtDir {
    my $self = shift;
    return undef;
}

####
# sub hmmDir() returns the path to this entry's hmm directory
# -does not exist in the prod version of the library
#
# params: none
# returns: filepath to the hmm dir
####
sub hmmDir {
    my $self = shift;
    return undef;
}


####
# sub beteDir() returns the path to this entry's bete directory
# -does not exist in the prod version of the library
#
# params: none
# returns: filepath to the bete dir
####
sub beteDir {
    my $self = shift;
    return undef;
}


####
# sub threadDir() returns the path to this entry's thread directory
# -does not exist in the prod version of the library
#
# params: none
# returns: filepath to the thread dir
####
sub threadDir {
    my $self = shift;
    return undef;
}


####
# sub attributeDir() returns the path to this entry's attr directory
# -does not exist in the prod version of the library
#
# params: none
# returns: filepath to the attr dir
####
sub attributeDir {
    my $self = shift;
    return undef;
}


####
# sub logDir() returns the path to this entry's log directory
# -does not exist in the prod version of the library
#
# params: none
# returns: filepath to the log dir
####
sub logDir {
    my $self = shift;
    return undef;
}


####
# sub sfDir() returns the path to this SF[0-99] directory
# -Do not access directly - access through corresponding method in FamLibEntry
#
# params: none
# returns: filepath to the SF dir
####
sub sfDir {
    my $self = shift;
    my $sfType = uc(shift);

    return undef unless ($sfType=~/^SF\d+$/);
    
    return($self->bookDir() . "$sfType/");
}


####
# sub makeDir makes the specified directory
# (a wrapper around mkdir())
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: 1 on success, undef on failure
###
sub makeDir {
    my $dir = shift;
    if (! -d $dir) {
	mkdir ($dir, 0777) || return undef;
    }
    return 1;
}


####
# sub seedFile returns this entry's seed sequence file
# -does not exist in the prod version of the library
#
# @params: none
# @return: the file name
###
sub seedFile {
    my $self = shift;
    return undef;
}

####
# sub seed [gs]ets this entry's seed sequence
# -does not exist in the prod version of the library
#
# @params: the seed sequence in fasta format if setting it
# @return: the seed sequence
###
sub seed {
    my $self = shift;
    my $seedSeq = shift;

    return undef;
}

####
# sub cluster [gs]ets this entry's cluster
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: the cluster in fasta format if setting it
# @return: the cluster
###
sub cluster {
    my $self = shift;
    my @newclust;

    # Make sure clustFile exists before opening it
    # to return the cluster 
    if ( -s $self->clustFile() ){
	my $ff = new FastaFile($self->clustFile());
	$ff->open();
	while (my $seq = $ff->nextSeq()) {
	    push(@newclust, $seq);
	}
	$ff->close();
    }

    
    return(@newclust);
}

####
# sub clustFile returns this entry's cluster file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub clustFile {
    my $self = shift;
    my $bn = $self->{bookName};
    return($self->bookDir() . "cluster.fasta");
}

####
# sub alignFile returns this entry's alignment file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub alignFile {
    my $self = shift;
    my $bn = $self->{bookName};
    return($self->bookDir() . "cluster.pir");
}


####
# sub alignment [gs]ets this entry's alignment
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: the cluster's alignment 
# @return: the cluster's alignment
###
sub alignment {
    my $self = shift;
    my @newalign;

    # return the cluster 
    my $aln = new FastaFile($self->alignFile());
    return undef unless ($aln);
    $aln->open();
    @newalign = $aln->getSeqs();

    return(@newalign);
}

####
# sub weightFile returns this entry's alignment
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: the type of sequence weigth file to return
#          (MAG | HKOFF | KPLUS | BEST)
# @return: the file name
###
sub weightFile {
    my $self = shift;

    return($self->bookDir() . "cluster.wts");
}

####
# sub weights [gs]ets this entry's sequence weights
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: the sequence weights, in seq_wt format
# @return: the sequence weights, in seq_wt format
##
sub weights {
    my $self = shift;
    my $type = shift;

    # return the weights 
    open (WTS, $self->weightFile()) || return;
    my @newweights = <WTS>;
    close WTS;

    return(@newweights);
}


####
# sub hmmRoot returns this entry's HMM model name 
# (not the file name, ie, no .mod at end)
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: the type of model file to return  (SF[0-99] | mag)
#          the format type of the hmm (sam | hmmer) - default is sam
# @return: the file name
###
sub hmmRoot {
    my $self = shift;
    my $hmmType = shift;
    my $hmmFormat = shift;
 
    if ( (! $hmmFormat) || ($hmmFormat eq "sam")) { 
      return $self->samHmmRoot($hmmType); 
    } elsif ($hmmFormat eq "hmmer") { 
      return $self->hmmerHmmRoot($hmmType); 
    } 
    return undef;
}

####  
# sub samHmmRoot returns this entry's SAM HMM model name   
# (not the file name, ie, no .mod at end)  
# -Do not access directly - access through corresponding method in FamLibEntry
#  
# @params: the type of model file to return  (SF[0-99] | mag)  
# @return: the file name  
###
sub samHmmRoot {  
    my $self = shift;  
    my $hmmType = shift;  
 
    if ($hmmType) {   
       return ($self->sfDir($hmmType) . "hmm") if ($hmmType=~/^SF\d+$/);  
       return unless (lc($hmmType) eq "mag");   
     }    
    return($self->bookDir() . "hmm");
}

#### 
# sub hmmerHmmRoot returns this entry's HMMER HMM model name  
# (not the file name, ie, no .mod at end) 
# -Do not access directly - access through corresponding method in FamLibEntry 
# 
# @params: the type of model file to return  (SF[0-99] | mag) 
# @return: the file name 
### 
sub hmmerHmmRoot { 
    my $self = shift; 
    my $hmmType = shift; 

    if ($hmmType) {  
       return ($self->sfDir($hmmType) . "hmmer") if ($hmmType=~/^SF\d+$/); 
       return unless (lc($hmmType) eq "mag");  
     }   
    return($self->bookDir() . "hmmer"); 
}

####
# sub evdParamFile returns the file containing this entry's HMM mu & lambda 
# for EVD P-value calculation
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: $hmmType - either "mag", or "SF[0-99]"
# @return: the file name
###
sub evdParamFile {
    my $self = shift;
    my $hmmType = shift;
    
    $hmmType = "mag" unless ($hmmType);
    
    if ($hmmType) {
       return unless (($hmmType eq "mag") || ($hmmType=~/^SF\d+$/));
    }
    
    return($self->hmmRoot($hmmType).".evd");
}

####
# sub evdParam [sg]ets this entry's HMM mu & lambda for EVD P-value
# calculation
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: $hmmType - eith "mag" or "SF[0-99]"
#          $mu - the mu value for the EVD fit
#          $lambda - the lambda value for the EVD fit
# @return: the file name
###
sub evdParam {
    my $self = shift;
    my $hmmType = shift;

    $hmmType = "mag" unless ($hmmType);

    # return the evd params
    open(EVD, $self->evdParamFile($hmmType)) || return;
    my @lines = <EVD>;
    close EVD;
    
    ($mu) = $lines[0] =~ /Mu = ([\d\.]+)/;
    ($lambda) = $lines[1] =~ /Lambda = ([\d\.]+)/;
    return($mu, $lambda);
}

####
# sub hmmFile returns this entry's HMM model file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: the type of model file to return
#              (ORIG | MAG | HKOFF | KPLUS | BEST | SF[0..n])
#          the format type of the hmm (sam | hmmer) - default is sam
# @return: the file name
###
sub hmmFile {
    my $self = shift;
    my $hmmType = shift;
    my $hmmFormat = shift;
    $hmmFormat = lc($hmmFormat);

    if ( (! $hmmFormat) || ($hmmFormat eq "sam")) {
      return $self->samHmmFile($hmmType);
    } elsif ($hmmFormat eq "hmmer") {
      return $self->hmmerHmmFile($hmmType);
    }
    return undef;
}

####  
# sub samHmmFile returns this entry's SAM HMM model file  
# -Do not access directly - access through corresponding method in FamLibEntry  
#  
# @params: the type of model file to return  
#              (ORIG | MAG | HKOFF | KPLUS | BEST | SF[0..n])  
# @return: the file name  
###  
sub samHmmFile {
    my $self = shift;  
    my $hmmType = shift;  

    if ($hmmType) {     
      return unless ((lc($hmmType) eq "mag") || ($hmmType=~/^SF\d+$/));     
    }

    return($self->samHmmRoot($hmmType).".mod");  
}

#### 
# sub hmmerHmmFile returns this entry's HMMER HMM model file 
# -Do not access directly - access through corresponding method in FamLibEntry 
# 
# @params: the type of model file to return 
#              (ORIG | MAG | HKOFF | KPLUS | BEST | SF[0..n]) 
# @return: the file name 
### 
sub hmmerHmmFile {
    my $self = shift; 
    my $hmmType = shift; 

    if ($hmmType) {    
      return unless ((lc($hmmType) eq "mag") || ($hmmType=~/^SF\d+$/));    
    }  

    return($self->hmmerHmmRoot($hmmType).".hmm"); 
}

####
# sub hmm [gs]ets this entry's hmm (SAM HMM) 
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: the hmm, in SAM format 
#          the type of model file to return
# @return: the hmm, in SAM format
###
sub hmm {
    my $self = shift;
    my $type = shift;
    my @hmm = @_;
    return if (@hmm);

    return $self->samHmm($type);
}

####  
# sub samHmm [gs]ets this entry's hmm  
# -Do not access directly - access through corresponding method in FamLibEntry  
#  
# @params: the hmm, in HMMER format   
#          the type of model file to return  
#              (ORIG | MAG | HKOFF | KPLUS | BEST | SF[0..n])  
# @return: the hmm, in HMMER format  
###  
sub samHmm {  
    my $self = shift;  
    my $type = shift;
    my @hmm = @_;
    return if (@hmm);
    
    # return the hmm   
    open(HMM, $self->samHmmFile($type)) || return;  
    my @newhmm = <HMM>;  
    close HMM;  
  
    return(@newhmm);  
}

#### 
# sub hmmerHmm [gs]ets this entry's hmm 
# -Do not access directly - access through corresponding method in FamLibEntry 
# 
# @params: the hmm, in HMMER format  
#          the type of model file to return 
#              (ORIG | MAG | HKOFF | KPLUS | BEST | SF[0..n]) 
# @return: the hmm, in HMMER format 
### 
sub hmmerHmm { 
    my $self = shift; 
    my $type = shift; 
    my @hmm = @_;
    return if (@hmm);

    # return the hmm  
    open(HMM, $self->hmmerHmmFile($type)) || return; 
    my @newhmm = <HMM>; 
    close HMM; 
 
    return(@newhmm); 
} 

####
# sub llmFile returns this entry's LLM file for HMM alignment
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: the type of model file to return - should match HMM
#          (ORIG | MAG | HKOFF | KPLUS | BEST | SF[0..n])
# @return: the file name - same as HMM but with .llm extention
###
sub llmFile {
    my $self = shift;
    my $llmType = shift;
   
    $llmType = lc($llmType) unless ($llmType=~/SF/);

    if ($hmmType) {
	return unless (($llmType eq "mag") || ($llmType=~/^SF\d+$/));
    }

    return($self->hmmRoot($llmType).".llm");
}

####
# sub distRoot returns this entry's Sam Score File root
# (without the .dist extension)
# -does not exist in the prod version of the library
#
# @return: the file name
###
sub distRoot {
    my $self = shift;
    
    return undef;
}

####
# sub distFile returns this entry's Sam Score File
# -does not exist in the prod verison of the library
#
# @return: the file name
###
sub distFile {
    my $self = shift;

    return undef;
}


####
# sub beteRoot returns this entry's Bete basename
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @return: the file name
###
sub beteRoot {
    my $self = shift;
    return($self->bookDir . "tree");
}

####
# sub treeFile returns this entry's tree filename
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @return: the file name
###
sub treeFile {
    my $self = shift;
    return($self->beteRoot() . ".tree");
}

####
# sub tree [gs]ets this entry's tree
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: the tree, in Caley format
# @return: the tree, in Caley format
###
sub tree {
    my $self = shift;
    my @newtree;

    # return the tree 
    open(TRE, $self->treeFile()) || return;
    @newtree = <TRE>;
    close TRE;

    return(@newtree);
}

####
# sub beteProfiles returns the filenames for the
# profiles created by bete
# -does not exist in the prod version of the library
#
# @return: the array of file name
###
sub beteProfiles {
    my $self = shift;

    return undef;
}


####
# sub sfHmmFiles returns the filenames for all the
# subfamilly HMMs created from bete profiles
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: the format type of the hmm (sam | hmmer) - default is sam
# @return: the array of file roots
###
sub sfHmmFiles {
    my $self = shift;
    my $hmmFormat = shift;
    $hmmFormat = lc($hmmFormat);

    if ( (! $hmmFormat) || ($hmmFormat eq "sam")) { 
      return $self->samSfHmmFiles();
    } elsif ($hmmFormat eq "hmmer") { 
      return $self->hmmerSfHmmFiles();
    } 
    return undef; 
}

####  
# sub samSfHmmFiles returns the filenames for all the  
# subfamilly HMMs created from bete profiles  
# -Do not access directly - access through corresponding method in FamLibEntry #  
# @return: the array of file roots  
### 
sub samSfHmmFiles {
    my $self = shift;  
    my $glob = $self->bookDir() . "SF*/hmm.mod";  
    return(<${glob}>);  
}

#### 
# sub hmmerSfHmmFiles returns the filenames for all the 
# subfamilly HMMs created from bete profiles 
# -Do not access directly - access through corresponding method in FamLibEntry 
# 
# @return: the array of file roots 
###
sub hmmerSfHmmFiles { 
    my $self = shift; 
    my $glob = $self->bookDir() . "SF*/hmmer.hmm"; 
    return(<${glob}>); 
}

####
# sub sfHmmForProfile returns the filename for the
# subfamilly HMMs associated with a given profile
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @return: the array of file roots
###
sub sfHmmForProfile {
    my $self = shift;
    return undef;
}

####
# sub tataFile returns this entry's .tata file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub tataFile {
    my $self = shift;
    my $bn = $self->{bookName};

    return($self->beteRoot() . ".tata");
}

####
# sub attributeFile returns this entry's .attr file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub attributeFile {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->bookDir() . "attr.tab");
}

####
# sub subtabFile returns this entry's .sub.tab file
# -does not exist in the prod verison of the library
#
# @params: none
# @return: the file name
###
sub subtabFile {
    my $self = shift;
    return undef;
}

####
# sub collapsedTreeFile returns this entry's collapsed tree .png file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub collapsedTreeFile {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->bookDir() . "tree.png");
}

###
# sub collapsedGridFile returns the collapsedGrid.png file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub collapsedGridFile {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->bookDir() . "collapsedGrid.png");

}

###
# sub collapsedGridInfoFile returns the collapsedGridInfo.txt file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub collapsedGridInfoFile {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->bookDir() . "collapsedGridInfo.txt");
}

###
# sub collapsedTreeInfoFile returns the collapsedTreeInfo.txt file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub collapsedTreeInfoFile {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->bookDir() . "collapsedTreeInfo.txt"); 
}

###
# sub expandedGridFile returns the collapsedGridInfo.png file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub expandedGridFile {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->bookDir() . "expandedGrid.png");
}

###
# sub expandedGridInfo returns the expandedGridInfo.txt file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub expandedGridInfoFile {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->bookDir() . "expandedGridInfo.txt");
}

###
# sub expandedTreeFile returns the expandedTree.png file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub expandedTreeFile {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->bookDir() . "expandedTree.png");
}

###
# sub expandedTreeInfoFile returns the expandedTreeInfo.txt file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub expandedTreeInfoFile {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->bookDir() . "expandedTreeInfo.txt");
}

###
# sub sfColorsFile returns the sfColors.tab file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub sfColorsFile {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->bookDir() . "sfColors.tab");
}

####
# sub beteFile returns this entry's .bete file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub beteFile {
    my $self = shift;
    my $bn = $self->{bookName};
    return($self->beteRoot() . ".bete");
}


####
# sub createBeteFile creates this entry's .bete file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
####
sub createBeteFile {
    my $self = shift;

    my $bookDir = $self->bookDir();
    my $beteFile = $self->beteFile();
    my $attributeFile = $self->attributeFile();
    my $treeFile = $self->treeFile();

    $attributeFile=~s/^$bookDir(\S+)/$1/;
    $treeFile=~s/^$bookDir(\S+)/$1/;

    open(BETE,">$beteFile");
    print BETE $treeFile,"\n";
    print BETE $attributeFile,"\n";
    close(BETE);
}

# use FamLibBuilder for testing

# required for loading
1;
