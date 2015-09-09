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
## FamLibEntry_build.pm - a class to handle a book entry in the build
##       (book-building/development) version of the library. FamLibEntry_build
##       is a subclass of FamLibEntry.
##       Do not access FamLibEntry_build directly - use FamLibEntry
##
##
#####
#####

package FamLibEntry_build;
use FastaFile;
#use strict;


####
# constructor - creates and returns a build instance of the  FamLibEntry 
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
    my $bookType = shift;
    my $self = {};
    $self->{flb} = $flb;
    $self->{bookName} = $bookName;
    $self->{bookType} = $bookType;

    bless $self, $class;
    return $self;
}

# create a FamLibEntry on the filesystem
# -Do not access directly - access through corresponding method in FamLibEntry
sub create {
    my $self = shift;

    # if we exist, don't recreate and return warning code
    return -1 if (-d $self->bookDir() && -d $self->entryDir());

    # create ourselves
    return undef unless (&makeDir($self->bookDir()));
    return undef unless (&makeDir($self->entryDir()));
    return undef unless (&makeDir($self->clustDir()));
    return undef unless (&makeDir($self->seqwtDir()));
    return undef unless (&makeDir($self->hmmDir()));
    return undef unless (&makeDir($self->beteDir()));
    return undef unless (&makeDir($self->threadDir()));
    return undef unless (&makeDir($self->attributeDir()));
    return undef unless (&makeDir($self->logDir()));

    # log our internal book creation info
    my $logString = "BOOK CREATED: " . `date`;
    $self->log($logString);
    $self->log("VERSION: ".$self->{flb}->{FamLibVersion}."\n");

    # register ourselves with the library
    $self->{flb}->register($bookName, $bookType);

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
    return 1 if (-d $self->bookDir() && -d $self->entryDir());
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
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params $message - what to write ... \n is not implied
#         $logFile - if passed, write to this file ... undef, write to deflt
# @return nothing
####
sub log {
    my $self = shift;
    my $message = shift;
    my $logFile = shift;
    $logFile = "general.log" unless ($logFile);
    $logFile = $self->logDir() . $logFile;
    
    open (LOG, ">>$logFile");
    print LOG $message;
    close LOG;
}


####
# sub lock() locks an entry for a given process
# -Do not access directly - access through corresponding method in FamLibEntry
# 
# @params $type - type of lock; should be calling process name
# @return 1 on success, 0 on failure or lock already exists
####
sub lock {
    my $self = shift;
    my $type = shift;

    my $lockFile = "$type.lock";
    $lockFile = $self->logDir() . $lockFile;

    return 0 if (-f $lockFile);

    #hopefully sleeping a random amount of time will stagger the process
    my $randNum = int(rand 30) +1;
    sleep $randNum;
    
    #now check again to see if file has been created
    return 0 if (-f $lockFile);

    
    open (LOCK, ">>$lockFile");
    print LOCK `date`, "\n";
    close LOCK;

    return 1 if (-s $lockFile);
    return 0;
}


####
# sub unlock() unlocks an entry for a given process
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params $type - type of lock; should be calling process name
#                 MUST MATCH TAG USED IN lock()!
# @return 1 on success, 0 on failure or no lock
####
sub unlock {
    my $self = shift;
    my $type = shift;

    my $lockFile = "$type.lock";
    $lockFile = $self->logDir() . $lockFile;
    
    if (-f $lockFile) {
	unlink($lockFile);
        return 1;
    } 
    return 0;
}


####
# sub isLocked() returns an entry's lock status
# -Do not access directly - access through corresponding method in FamLibEntry
# 
# @params $type - type of lock; should be calling process name
#                 MUST MATCH TAG USED IN lock()!
# @return 1 on if locked, 0 on failure or no lock
####
sub isLocked {
    my $self = shift;
    my $type = shift;

    my $lockFile = "$type.lock";
    $lockFile = $self->logDir() . $lockFile;
    
    return 1 if (-f $lockFile);
    return 0;
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
    return ($self->bookDir() . $self->{bookType}. "/");
}

####
# sub bookName() returns book name
# (book name comes from $fle object data structure)
# -Do not access directly - access through corresponding method in FamLibEntry
#
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
# -Do not access directly - access through corresponding method in FamLibEntry
#
# params: none
# returns: book type
#####
sub bookType {
    my $self = shift;
    return($self->{bookType});
}

####
# sub clustDir() returns the path to this entry's cluster directory
# -Do not access directly - access through corresponding method in FamLibEntry
#
# params: none
# returns: filepath to the cluster dir
####
sub clustDir {
    my $self = shift;
    return ($self->entryDir() . "clusts/");
}

####
# sub seqwtDir() returns the path to this entry's sequence weighting directory
# -Do not access directly - access through corresponding method in FamLibEntry
#
# params: none
# returns: filepath to the seqwt dir
####
sub seqwtDir {
    my $self = shift;
    return ($self->entryDir() . "seqwt/");
}

####
# sub hmmDir() returns the path to this entry's hmm directory
# -Do not access directly - access through corresponding method in FamLibEntry
#
# params: none
# returns: filepath to the hmm dir
####
sub hmmDir {
    my $self = shift;
    return ($self->entryDir() . "hmm/");
}


####
# sub beteDir() returns the path to this entry's bete directory
# -Do not access directly - access through corresponding method in FamLibEntry
#
# params: none
# returns: filepath to the bete dir
####
sub beteDir {
    my $self = shift;
    return ($self->entryDir() . "bete/");
}


####
# sub threadDir() returns the path to this entry's thread directory
# -Do not access directly - access through corresponding method in FamLibEntry
#
# params: none
# returns: filepath to the thread dir
####
sub threadDir {
    my $self = shift;
    return ($self->entryDir() . "thread/");
}


####
# sub attributeDir() returns the path to this entry's attr directory
# -Do not access directly - access through corresponding method in FamLibEntry
#
# params: none
# returns: filepath to the attr dir
####
sub attributeDir {
    my $self = shift;
    return ($self->entryDir() . "attr/");
}


####
# sub logDir() returns the path to this entry's log directory
# -Do not access directly - access through corresponding method in FamLibEntry
#
# params: none
# returns: filepath to the log dir
####
sub logDir {
    my $self = shift;
    return ($self->entryDir() . "log/");
}

####
# sub sfDir() returns the path to this SF[0-99] directory
# -does not exist in the build version of the library
#
# params: none
# returns: filepath to the SF dir
####
sub sfDir {
    my $self = shift;
    my $sfType = uc(shift);

    return undef;
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
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub seedFile {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->entryDir() . "$bn.$bt.fasta");
}

####
# sub seed [gs]ets this entry's seed sequence
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: the seed sequence in fasta format if setting it
# @return: the seed sequence
###
sub seed {
    my $self = shift;
    my $seedSeq = shift;

    # set the seed sequence if passed
    if ($seedSeq) {
	open(SEED, ">".$self->seedFile());
	print SEED $seedSeq;
	close SEED;
    }

    # return the seed sequence
    open(SEED, "<".$self->seedFile());
    my @seed = <SEED>;
    close SEED;
    return join("", @seed);
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
    my @clust = @_;
    my @newclust;

    # set the cluster if passed
    if (@clust) {
      open(SEED, ">".$self->clustFile());
      print SEED @clust;
      close SEED;
    }

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
    my $bt = $self->{bookType};
    return($self->clustDir() . "$bn.$bt.cluster.fasta");
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
    my $bt = $self->{bookType};
    return($self->clustDir() . "$bn.$bt.cluster.pir");
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
    my @align = @_;
    my @newalign;

    # set the cluster if passed
    if (@align) {
	    open(ALN, ">".$self->alignFile());
	    print ALN @align;
	    close ALN;
    }

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
    my $weightType = lc(shift);
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    
    return unless ((grep(/^$weightType$/, ("orig", "mag", "hkoff", "kplus"))));

    return($self->seqwtDir() . "$bn.$bt.$weightType.wts");
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
    my @weights = @_;
    my @newweights;

    return unless ((grep(/^$type$/, ("orig", "mag", "hkoff", "kplus"))));

    # set the weights if passed
    if (@weights) {
      open(WTS, ">".$self->weightFile($type));
      print WTS @weights;
      close WTS;
    }

    # return the weights 
    open (WTS, $self->weightFile($type)) || return;
    @newweights = <WTS>;
    close WTS;

    return(@newweights);
}


####
# sub hmmRoot returns this entry's HMM model name 
# (not the file name, ie, no .mod at end)
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: the type of model file to return
#          (ORIG | MAG | HKOFF | KPLUS | BEST)
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

    $hmmType= lc($hmmType) unless ($hmmType=~/SF/);
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return unless ((grep(/^$hmmType$/, ("orig", "mag", "hkoff", "kplus"))) || ($hmmType=~/^SF\d+$/));
    return($self->hmmDir() . "$bn.$bt.$hmmType");
}

####  
# sub hmmerHmmRoot returns this entry's HMMER HMM model name   
# (not the file name, ie, no .mod at end)  
# -Do not access directly - access through corresponding method in FamLibEntry #  
# @params: the type of model file to return  (SF[0-99] | mag)  
# @return: the file name  
###  
sub hmmerHmmRoot {  
    my $self = shift;  
    my $hmmType = shift;  
    return undef;
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
    $hmmType = "mag" unless $hmmType;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->hmmDir()."$bn.$bt.$hmmType.evd");
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
    my $mu = shift;
    my $lambda = shift;

    # set the params if passed
    if ($mu && $lambda) {
	open(EVD, ">".$self->evdParamFile());
	print EVD "MU = $mu\n";
	print EVD "LAMBDA = $lambda\n";
	close EVD;
    }

    # return the evd params
    open(EVD, $self->evdParamFile()) || return;
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
#          the format type of the hmm (sam | hmmer)  - default is sam
# @return: the file name
###
sub hmmFile {
    my $self = shift;
    my $hmmType = shift;    
    my $hmmFormat = shift;

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
#          (ORIG | MAG | HKOFF | KPLUS | BEST | SF[0..n])
# @return: the file name
###
sub samHmmFile {
    my $self = shift;
    my $hmmType = shift;
     
    $hmmType = lc($hmmType) unless ($hmmType=~/SF/);
   
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return unless ((grep(/^$hmmType$/, ("orig", "mag", "hkoff", "kplus"))) || ($hmmType=~/^SF\d+$/));
    
    return($self->samHmmRoot($hmmType).".mod");
}

####
# sub hmmerHmmFile returns this entry's HMMER HMM model file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: the type of model file to return
#          (ORIG | MAG | HKOFF | KPLUS | BEST | SF[0..n])
# @return: the file name
###
sub hmmerHmmFile {
    my $self = shift;
    my $hmmType = shift;
    return undef;
}

####
# sub hmm [gs]ets this entry's hmm (SAM HMM)
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: the hmm, in SAM format 
# @return: the hmm, in SAM format
###
sub hmm {
    my $self = shift;
    my $type = shift;
    my @hmm = @_;
    return $self->samHmm($type,@hmm);
}

#### 
# sub samHmm [gs]ets this entry's hmm (SAM HMM) 
# -Do not access directly - access through corresponding method in FamLibEntry 
# 
# @params: the hmm, in SAM format  
# @return: the hmm, in SAM format 
### 
sub samHmm { 
    my $self = shift; 
    my $type = shift; 
    my @hmm = @_; 
    my @newhmm; 
 
    # set the hmm if passed 
    if (@hmm) { 
      open(HMM, ">".$self->samHmmFile($type)); 
      print HMM @hmm; 
      close HMM; 
    } 
 
    # return the hmm  
    open(HMM, $self->samHmmFile($type)) || return; 
    @newhmm = <HMM>; 
    close HMM; 
 
    return(@newhmm); 
} 

#### 
# sub hmmerHmm [gs]ets this entry's hmm
# -Do not access directly - access through corresponding method in FamLibEntry 
# 
# @params: the hmm, in HMMER format  
# @return: the hmm, in HMMER format 
### 
sub hmmerHmm { 
    my $self = shift; 
    return undef;
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
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return unless ((grep(/^$llmType$/, ("orig", "mag", "hkoff", "kplus", "SF"))) || ($llmType=~/^SF\d+$/));

    return($self->hmmRoot($llmType).".llm");
}


####
# sub distRoot returns this entry's Sam Score File root
# (without the .dist extension)
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @return: the file name
###
sub distRoot {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->logDir() . "$bn.$bt");
}

####
# sub distFile returns this entry's Sam Score File
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @return: the file name
###
sub distFile {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->distRoot() . ".dist");
}


####
# sub beteRoot returns this entry's Bete basename
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @return: the file name
###
sub beteRoot {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->beteDir() . "$bn.$bt");
}

####
# sub treeFile returns this entry's tree filename
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @return: the file name
###
sub treeFile {
    my $self = shift;
    return($self->beteRoot() . ".nodes.tree");
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
    my @tree = @_;
    my @newtree;

    # set the tree if passed
    if (@tree) {
      open(TRE, ">".$self->treeFile());
      print TRE @tree,"\n";
      close TRE;
    }

    # return the tree 
    open(TRE, $self->treeFile()) || return;
    @newtree = <TRE>;
    close TRE;

    return(@newtree);
}

####
# sub beteProfiles returns the filenames for the
# profiles created by bete
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @return: the array of file name
###
sub beteProfiles {
    my $self = shift;
    my @files;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    opendir(BETE, $self->beteDir());
    my @tempfiles = grep(/profile$/, readdir(BETE));
    closedir(BETE);
    foreach my $file(@tempfiles){
      next unless($file=~/SF/);
      push(@files,$file);
    }

    foreach my $file (@files) {	
	$file = $self->beteDir()."/$file";
    }
    return(@files);
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
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    my $glob = $self->hmmDir() . "$bn.$bt.SF*.mod";
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
    return undef;
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
    my $profile = shift;
    $profile =~ s!/bete/!/hmm/!;
    $profile =~ s!\.profile$!.mod!;
    return($profile);
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
    my $bt = $self->{bookType};
    return($self->beteDir() . "$bn.$bt.tata");
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
    return($self->beteDir() . "$bn.$bt.attr");
}

####
# sub subtabFile returns this entry's .sub.tab file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub subtabFile {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    return($self->beteDir() . "$bn.$bt.sub.tab");
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
    return($self->beteDir() . "$bn.$bt.png");
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
    return($self->beteDir() . "$bn.$bt.collapsedGrid.png");

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
    return($self->beteDir() . "$bn.$bt.collapsedGridInfo.txt");
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
    return($self->beteDir() . "$bn.$bt.collapsedTreeInfo.txt"); 
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
    return($self->beteDir() . "$bn.$bt.expandedGrid.png");
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
    return($self->beteDir() . "$bn.$bt.expandedGridInfo.txt");
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
    return($self->beteDir() . "$bn.$bt.expandedTree.png");
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
    return($self->beteDir() . "$bn.$bt.expandedTreeInfo.txt");
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
    return($self->beteDir() . "$bn.$bt.sfColors.tab");
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
    my $bt = $self->{bookType};
    return($self->beteDir() . "$bn.$bt.bete");
}


####
# sub createBeteFile creates this entry's .bete file
# -Do not access directly - access through corresponding method in FamLibEntry
#
# @params: none
# @return: the file name
###
sub createBeteFile {
    my $self = shift;
    my $bn = $self->{bookName};
    my $bt = $self->{bookType};
    
    my $beteDir = $self->beteDir();
    my $beteFile = $self->beteFile();
    my $attributeFile = $self->attributeFile();
    my $treeFile = $self->treeFile();

    $attributeFile=~s/^$beteDir(\S+)/$1/;
    $treeFile=~s/^$beteDir(\S+)/$1/;

    open(BETE,">$beteFile");
    print BETE $treeFile,"\n";
    print BETE $attributeFile,"\n";
    close(BETE);
    
}

# use FamLibBuilder for testing

# required for loading
1;
