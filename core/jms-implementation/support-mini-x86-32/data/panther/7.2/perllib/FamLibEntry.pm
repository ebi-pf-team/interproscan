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
## FamLibEntry.pm - a class to handle a book entry in a library.
##      FamLibEntry.pm acts as a superclass to FamLibEntry_build and 
##      FamLibEntry_prod.  This allows the user to easily access the directory
##      structure of either the "build" (book-building/development) or "prod"
##      (production/released/published) versions of the library by simply 
##      specifying "build" or "prod" for the $flbType when instanciating the
##      FamLibBuilder object.
##          
#####
#####

package FamLibEntry;
use FastaFile;
use FamLibEntry_build;
use FamLibEntry_prod;
#use strict;

####
# constructor - creates and returns a FamLibEntry object. Operates via 
# FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#  - NOT TO BE CALLED DIRECTLY - use FamLibBuilder::getLibEntry()!
#
# @params: $flb - FamLibBuilder object
#          $bookName - name of book
#          $bookType - entry type (ex: nr, curated) - not used by prod instance
#                      of library
#          $fleType - FamLibEntry type (build or prod); build = default
# @return: $self - FamLibEntry object
#####
sub new {
    my $class = shift;
    my $flb = shift;
    my $bookName = shift;
    my $bookType = shift;
    my $fleType = shift;
    my $self = {};

    if ((! $fleType) || ($fleType eq "build")) {
        $self->{fle_instance} = new FamLibEntry_build($flb,$bookName,$bookType);
    } elsif ($fleType eq "prod") {
        $self->{fle_instance} = new FamLibEntry_prod($flb,$bookName,$bookType);
    }

    bless $self, $class;
    return $self;
}


# create a FamLibEntry on the filesystem
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
sub create {
    my $self = shift;
    return($self->{fle_instance}->create());
}


####
# check if our library exists on the filesystem
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @param none
# @returns 1 if exists, undef if doesn't
####
sub exists {
    my $self = shift;
    return($self->{fle_instance}->exists());
}


####
# create our library if it doesn't exist on the system
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @param none
# @returns non
####
sub assureExists {
    my $self = shift;
    return($self->{fle_instance}->assureExists());
}


####
# sub log() logs a message to our log file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params $message - what to write ... \n is not implied
#         $logFile - if passed, write to this file ... undef, write to deflt
# @return nothing
####
sub log {
    my $self = shift;
    my $message = shift;
    my $logFile = shift;
    return($self->{fle_instance}->log($message,$logFile));
}


####
# sub lock() locks an entry for a given process
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params $type - type of lock; should be calling process name
# @return 1 on success, 0 on failure or lock already exists
####
sub lock {
    my $self = shift;
    my $type = shift;

    return($self->{fle_instance}->lock($type));
}


####
# sub unlock() unlocks an entry for a given process
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params $type - type of lock; should be calling process name
#                 MUST MATCH TAG USED IN lock()!
# @return 1 on success, 0 on failure or no lock
####
sub unlock {
    my $self = shift;
    my $type = shift;

    return($self->{fle_instance}->unlock($type));
}


####
# sub isLocked() returns an entry's lock status
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params $type - type of lock; should be calling process name
#                 MUST MATCH TAG USED IN lock()!
# @return 1 on if locked, 0 on failure or no lock
####
sub isLocked {
    my $self = shift;
    my $type = shift;

    return($self->{fle_instance}->isLocked($type));
}


####
# sub bookDir() returns the path to the book
# (one book has many entries, each of a named type)
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# params: none
# returns: filepath to the book
####
sub bookDir {
    my $self = shift;
    return($self->{fle_instance}->bookDir());
}

####
# sub entryDir() returns the path to this entry
# (one book has many entries, each of a named type)
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# params: none
# returns: filepath to the entry
####
sub entryDir {
    my $self = shift;
    return($self->{fle_instance}->entryDir());
}


####
# sub bookName() returns book name
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# params: none
# returns: book name
#####
sub bookName {
    my $self = shift;
    return($self->{fle_instance}->bookName());
}

####
# sub bookType() returns book type (entry type)
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# params: none
# returns: book type
#####
sub bookType {
    my $self = shift;
    return($self->{fle_instance}->bookType());
}

####
# sub clustDir() returns the path to this entry's cluster directory
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# params: none
# returns: filepath to the cluster dir
####
sub clustDir {
    my $self = shift;
    return($self->{fle_instance}->clustDir());
}

####
# sub seqwtDir() returns the path to this entry's sequence weighting directory
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# params: none
# returns: filepath to the seqwt dir
####
sub seqwtDir {
    my $self = shift;
    return($self->{fle_instance}->seqwtDir());
}

####
# sub hmmDir() returns the path to this entry's hmm directory
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# params: none
# returns: filepath to the hmm dir
####
sub hmmDir {
    my $self = shift;
    return($self->{fle_instance}->hmmDir());
}


####
# sub beteDir() returns the path to this entry's bete directory
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# params: none
# returns: filepath to the bete dir
####
sub beteDir {
    my $self = shift;
    return($self->{fle_instance}->beteDir());
}


####
# sub threadDir() returns the path to this entry's thread directory
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# params: none
# returns: filepath to the thread dir
####
sub threadDir {
    my $self = shift;
    return($self->{fle_instance}->threadDir());
}


####
# sub attributeDir() returns the path to this entry's attr directory
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# params: none
# returns: filepath to the attr dir
####
sub attributeDir {
    my $self = shift;
    return($self->{fle_instance}->attributeDir());
}


####
# sub logDir() returns the path to this entry's log directory
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# params: none
# returns: filepath to the log dir
####
sub logDir {
    my $self = shift;
    return($self->{fle_instance}->logDir());
}

####
# sub sfDir() returns the path to this SF[0-99] directory
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# params: none
# returns: filepath to the SF dir
####
sub sfDir {
    my $self = shift;
    my $sfType = shift;
    
    return($self->{fle_instance}->sfDir($sfType));
}

####
# sub makeDir makes the specified directory
# (a wrapper around mkdir())
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
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
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub seedFile {
    my $self = shift;
    return($self->{fle_instance}->seedFile());
}

####
# sub seed [gs]ets this entry's seed sequence
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the seed sequence in fasta format if setting it
# @return: the seed sequence
###
sub seed {
    my $self = shift;
    my $seedSeq = shift;

    return($self->{fle_instance}->seed($seedSeq));
}

####
# sub cluster [gs]ets this entry's cluster
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the cluster in fasta format if setting it
# @return: the cluster
###
sub cluster {
    my $self = shift;
    my @clust = @_;
    return($self->{fle_instance}->cluster(@clust));
}

####
# sub clustFile returns this entry's cluster file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub clustFile {
    my $self = shift;
    return($self->{fle_instance}->clustFile());
}

####
# sub alignFile returns this entry's alignment file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub alignFile {
    my $self = shift;
    return($self->{fle_instance}->alignFile());
}


####
# sub alignment [gs]ets this entry's alignment
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the cluster's alignment 
# @return: the cluster's alignment
###
sub alignment {
    my $self = shift;
    my @align = @_;
    return($self->{fle_instance}->alignment(@align));
}

####
# sub weightFile returns this entry's alignment
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the type of sequence weigth file to return
#          (MAG | HKOFF | KPLUS | BEST)
# @return: the file name
###
sub weightFile {
    my $self = shift;
    my $weightType = lc(shift);

    return($self->{fle_instance}->weightFile($weightType));
}

####
# sub weights [gs]ets this entry's sequence weights
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the sequence weights, in seq_wt format
# @return: the sequence weights, in seq_wt format
##
sub weights {
    my $self = shift;
    my $type = shift;
    my @weights = @_;
    return($self->{fle_instance}->weights($type,@weights));
}


####
# sub hmmRoot returns this entry's HMM model name 
# (not the file name, ie, no .mod at end)
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the type of model file to return
#              (ORIG | MAG | HKOFF | KPLUS | BEST)
#          the format type of the hmm (sam | hmmer)
# @return: the file name
###
sub hmmRoot {
    my $self = shift;
    my $hmmType = shift;
    my $hmmFormat = shift;

    return($self->{fle_instance}->hmmRoot($hmmType,$hmmFormat));
}

####
# sub samHmmRoot returns this entry's SAM HMM model name 
# (not the file name, ie, no .mod at end)
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the type of model file to return
#              (ORIG | MAG | HKOFF | KPLUS | BEST)
# @return: the file name
###
sub samHmmRoot {
    my $self = shift;
    my $hmmType = shift;

    return($self->{fle_instance}->samHmmRoot($hmmType));
}

####
# sub hmmerHmmRoot returns this entry's HMMER HMM model name 
# (not the file name, ie, no .mod at end)
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the type of model file to return
#              (ORIG | MAG | HKOFF | KPLUS | BEST)
# @return: the file name
###
sub hmmerHmmRoot {
    my $self = shift;
    my $hmmType = shift;

    return($self->{fle_instance}->hmmerHmmRoot($hmmType));
}

####
# sub evdParamFile returns the file containing this entry's HMM mu & lambda 
# for EVD P-value calculation
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: $hmmType - either "mag", or "SF[0-99]"
# @return: the file name
###
sub evdParamFile {
    my $self = shift;
    my $hmmType = shift;
    return($self->{fle_instance}->evdParamFile($hmmType));
}

####
# sub evdParam [sg]ets this entry's HMM mu & lambda for EVD P-value
# calculation
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
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
    my $mu = shift;
    my $lambda = shift;

    return($self->{fle_instance}->evdParam($hmmType,$mu,$lambda));
}

####
# sub hmmFile returns this entry's HMM model file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the type of model file to return
#              (ORIG | MAG | HKOFF | KPLUS | BEST | SF[0..n])
#          the format type of the hmm (sam | hmmer)
# @return: the file name
###
sub hmmFile {
    my $self = shift;
    my $hmmType = shift;    
    my $hmmFormat = shift;

    $hmmType = lc($hmmType) unless ($hmmType=~/SF/);
    $hmmFormat = lc($hmmFormat);

    return($self->{fle_instance}->hmmFile($hmmType,$hmmFormat));
}

####
# sub samHmmFile returns this entry's SAM HMM model file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the type of model file to return
#              (ORIG | MAG | HKOFF | KPLUS | BEST | SF[0..n])
# @return: the file name
###
sub samHmmFile {
    my $self = shift;
    my $hmmType = shift;    

    $hmmType = lc($hmmType) unless ($hmmType=~/SF/);

    return($self->{fle_instance}->samHmmFile($hmmType));
}

####
# sub hmmerHmmFile returns this entry's HMMER HMM model file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the type of model file to return
#              (ORIG | MAG | HKOFF | KPLUS | BEST | SF[0..n])
# @return: the file name
###
sub hmmerHmmFile {
    my $self = shift;
    my $hmmType = shift;    

    $hmmType = lc($hmmType) unless ($hmmType=~/SF/);

    return($self->{fle_instance}->hmmerHmmFile($hmmType));
}

####
# sub hmm [gs]ets this entry's hmm (SAM HMM)
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the hmm, in SAM format 
# @return: the hmm, in SAM format
###
sub hmm {
    my $self = shift;
    my $type = shift;
    my @hmm = @_;

    if (@hmm) {
      my $firstLine = lc($hmm[0]);
      die "Incorect use of hmm method - do not defined hmmer or the default sam type\n" if (($firstLine eq "hmmer") || ($firstLine eq "sam"));
    }

    return($self->{fle_instance}->hmm($type,@hmm)); 
}

#### 
# sub samHmm [gs]ets this entry's hmm 
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType 
# specified when instanciating FamLibEntry object 
# 
# @params: the hmm, in SAM format  
# @return: the hmm, in SAM format 
### 
sub samHmm { 
    my $self = shift; 
    my $type = shift; 
    my @hmm = @_;
 
    return($self->{fle_instance}->samHmm($type,@hmm)); 
} 

#### 
# sub hmmerHmm [gs]ets this entry's hmm 
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType 
# specified when instanciating FamLibEntry object 
# 
# @params: the hmm, in HMMER format  
# @return: the hmm, in HMMER format 
### 
sub hmmerHmm { 
    my $self = shift; 
    my $type = shift; 
    my @hmm = @_; 
 
    return($self->{fle_instance}->hmmerHmm($type,@hmm)); 
} 

####
# sub llmFile returns this entry's LLM file for HMM alignment
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the type of model file to return - should match HMM
#          (ORIG | MAG | HKOFF | KPLUS | BEST | SF[0..n])
# @return: the file name - same as HMM but with .llm extention
###
sub llmFile {
    my $self = shift;
    my $llmType = shift;
    
    return($self->{fle_instance}->llmFile($llmType));
}


####
# sub distRoot returns this entry's Sam Score File root
# (without the .dist extension)
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @return: the file name
###
sub distRoot {
    my $self = shift;
    return($self->{fle_instance}->distRoot());
}

####
# sub distFile returns this entry's Sam Score File
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @return: the file name
###
sub distFile {
    my $self = shift;
    return($self->{fle_instance}->distFile());
}


####
# sub beteRoot returns this entry's Bete basename
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @return: the file name
###
sub beteRoot {
    my $self = shift;
    return($self->{fle_instance}->beteRoot());
}

####
# sub treeFile returns this entry's tree filename
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @return: the file name
###
sub treeFile {
    my $self = shift;
    return($self->{fle_instance}->treeFile());
}

####
# sub tree [gs]ets this entry's tree
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the tree, in Caley format
# @return: the tree, in Caley format
###
sub tree {
    my $self = shift;
    my @tree = @_;

    return($self->{fle_instance}->tree(@tree));
}

####
# sub beteProfiles returns the filenames for the
# profiles created by bete
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @return: the array of file name
###
sub beteProfiles {
    my $self = shift;
    return($self->{fle_instance}->beteProfiles());
}


####
# sub sfHmmFiles returns the filenames for all the
# subfamilly HMMs created from bete profiles
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: the format type of the hmm (sam | hmmer) 
# @return: the array of file roots
###
sub sfHmmFiles {
    my $self = shift;
    my $hmmFormat = shift;
    return($self->{fle_instance}->sfHmmFiles($hmmFormat));
}

####
# sub samSfHmmFiles returns the filenames for all the SAM
# subfamilly HMMs created from bete profiles
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @return: the array of file roots
###
sub samSfHmmFiles {
    my $self = shift;
    return($self->{fle_instance}->samSfHmmFiles());
}

####
# sub hmmerSfHmmFiles returns the filenames for all the HMMER
# subfamilly HMMs created from bete profiles
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @return: the array of file roots
###
sub hmmerSfHmmFiles {
    my $self = shift;
    return($self->{fle_instance}->hmmerSfHmmFiles());
}

####
# sub sfHmmForProfile returns the filename for the
# subfamilly HMMs associated with a given profile
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @return: the array of file roots
###
sub sfHmmForProfile {
    my $self = shift;
    my $profile = shift;
    return($self->{fle_instance}->sfHmmForProfile($profile));
}

####
# sub tataFile returns this entry's .tata file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub tataFile {
    my $self = shift;
    return($self->{fle_instance}->tataFile());
}


####
# sub attributeFile returns this entry's .attr file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub attributeFile {
    my $self = shift;
    return($self->{fle_instance}->attributeFile());
}

####
# sub subtabFile returns this entry's .sub.tab file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub subtabFile {
    my $self = shift;
    return($self->{fle_instance}->subtabFile());
}

###
# sub collapsedTreeFile returns the collapsed tree .png file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub collapsedTreeFile {
    my $self = shift;
    return($self->{fle_instance}->collapsedTreeFile());
}

###
# sub collapsedGridFile returns the collapsedGrid.png file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub collapsedGridFile {
    my $self = shift;
    return($self->{fle_instance}->collapsedGridFile());
}

###
# sub collapsedGridInfoFile returns the collapsedGridInfo.txt file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub collapsedGridInfoFile {
    my $self = shift;
    return($self->{fle_instance}->collapsedGridInfoFile());
}

###
# sub collapsedTreeInfoFile returns the collapsedTreeInfo.txt file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub collapsedTreeInfoFile {
    my $self = shift;
    return($self->{fle_instance}->collapsedTreeInfoFile());
}

###
# sub expandedGridFile returns the collapsedGridInfo.png file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub expandedGridFile {
    my $self = shift;
    return($self->{fle_instance}->expandedGridFile());
}

###
# sub expandedGridInfo returns the expandedGridInfo.txt file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub expandedGridInfoFile {
    my $self = shift;
    return($self->{fle_instance}->expandedGridInfoFile());
}

###
# sub expandedTreeFile returns the expandedTree.png file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub expandedTreeFile {
    my $self = shift;
    return($self->{fle_instance}->expandedTreeFile());
}

###
# sub expandedTreeInfoFile returns the expandedTreeInfo.txt file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub expandedTreeInfoFile {
    my $self = shift;
    return($self->{fle_instance}->expandedTreeInfoFile());
}

###
# sub sfColorsFile returns the sfColors.tab file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub sfColorsFile {
    my $self = shift;
    return($self->{fle_instance}->sfColorsFile());
}

####
# sub beteFile returns this entry's .bete file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub beteFile {
    my $self = shift;
    return($self->{fle_instance}->beteFile());
}

####
# sub createBeteFile creates this entry's .bete file
# -Operates via FamLibEntry_build or FamLibEntry_prod, depending on $fleType
# specified when instanciating FamLibEntry object
#
# @params: none
# @return: the file name
###
sub createBeteFile {
    my $self = shift;
    return($self->{fle_instance}->createBeteFile());
  
}

# use FamLibBuilder for testing

# required for loading
1;
