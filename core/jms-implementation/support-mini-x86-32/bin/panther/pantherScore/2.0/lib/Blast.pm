#! /usr/local/bin/perl

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
## Blast.pm - a new, fast blast parsing object
## 
## v1.0 - BRK 4/28/99 ; from fastmatrix
##
#####
#####

use strict;
package Blast;
use Hit;

# CLASS VARIABLES
my $tempDir = "/tmp";  # where to store itermediate results

###
# create a new Blast object from a file
#
# params: $file, the name of a Blast output file
#         $blastRound, the iteration of Blast desired
#         (Optional - defaults to last round).
# return: the properly initialized blast object
###
sub new {
    my $class = shift;
    my $file = shift;
    my $blastRound = shift;
    my $self = {};
    bless $self, $class;
    $self->init($file, $blastRound);
    return $self;
}

### 
# create a new blast object by running blast
#
# params: $alg, the algorithm to use
#         $db, the database to search
#         $query, the query sequence 
#           (use concatenated fasta to run more than one blast serially)
#           (this can speed up execution significantly if running against)
#           (a small DB repeatedly since blast doesn't need to fork each time)
#         $args, an argument string
#         $mask, a flag - if set, mask the sequence before blasting it.
#         \%args - a hash reference of other arguments
#             {saveBlast} - if set, save Blast results to this file
#
# return: a new blast object with the run's results in it (single seq $query)
#         an array of initialized blast objects (multiseq $query)
###
sub run {
    my $class = shift;
    my $alg = shift;
    my $db = shift;
    my $query = shift;
    my $blastArgs = shift;
    my $mask = shift;
    my $args = shift;

    $tempDir = $args->{'tmpDir'} if (defined($args->{'tmpDir'}));

    # set up and run blast
    my $blastFile;
    return undef unless ($blastFile = &runBlast($alg,$db,$query,
						$blastArgs,$mask,$args));

    # check to see if we have multiple blast runs in one file
    my @blastFiles = &splitBlast($blastFile, "temp");

    # create ourselves from this output
    my @selves;
    foreach my $bFile (@blastFiles) {
        my $self = {};
        bless $self, $class;
        $self->init($bFile);
	push(@selves, $self);

        # cleanup
        unlink($bFile);
    }

    # final cleanup
    if ($args->{saveBlast}) {
        my $cmd = "cp $blastFile ".$args->{saveBlast};
	if (system($cmd)) {  
	  print STDERR "FATAL ERROR : $cmd \nSystem command returned error status: ($!)\n";  
	  return undef;  
	}  
    }
    unlink($blastFile);

    # either return the array of blast objects
    # or just the one ...
    if ($#selves > 0) {
        return @selves;
    } elsif ($#selves == 0) {
	return $selves[0];
    } else {
	return undef;
    }
}

###
# Initialize our blast object.
# Not usually called by outside program.
# new() does this
#
# params: $file, the name of a blast file
#         $round, the iteration desired
#         (defaults to last);
# return: this blast object or undef on error
###
sub init {
    my $self = shift;
    my $file = shift;
    my $round = shift;

    # slurp in the entire blast file
    if (open(TMP, "<$file")) {
        $self->{'fileName'} = $file;
	my @tmp = <TMP>;
	close TMP;
	$self->{'blastFile'} = \@tmp;
	$self->{'numLines'} = $#tmp;
	$self->{'curLine'} = $self->findLastPsiIter($round);
	$self->{'noMoreHits'} = undef;

	# find the line with the query name on it
	foreach my $line (@{$self->{'blastFile'}}) {
	    if ($line =~ /^Length\=[\d+]/) {
	      my $queryLen = (split(/\=/, $line))[1];
	      $queryLen=~s/\s//g;
	      unless ($queryLen=~/^\d+$/) {
		warn "Cannot parse query length: $line\n";
	      }

	      $self->{'queryLength'} = $queryLen;
	      last;
	    } 
	}
	unless ($self->{'queryLength'}) {
	    warn "Failed to set value for query length!!\n";
	}

	return $self;
     } else {
	return undef;
     }
}

###
# find the line at which the next blast hit starts
# if $start is passed, start the search at that line
# otherwise start at the current line
#
# params: $start, the line number to start the search at (optional)
# return: ($line, $isLast), $line = the line starting the next hit
#                           $isLast = 1 if this is the last hit in the file
####
sub seekNextHit {
    my $self = shift;
    my $start = shift;
    $start = $self->{curLine} unless ($start);

    my $i;
    my $line;
    my $isLast = undef;

    # scan lines until we find the start of the next hit
    # this is offset with an initial '>'
    # if we hit EOF or "Database:" we're done
    for ($i=$start+1 ; $i < $self->{numLines} ; $i++) {
	$line = $self->{blastFile}[$i];
        last if ($line =~ /^>/);
        if (($line =~ /^\s+Database:/) or ($line =~ /^Searching/)) {
	### if ($line =~ /^Sequences used in model/) {
	    $isLast = 1;
	    last;
	}
    }

    # if we didn't go past EOF, return correct line
    # otehrwise return 0,0
    if ($i < $self->{numLines}) {
        return ($i, $isLast);
    } else {
	return (0, 0);
    }
}

###
# return the next hit in the blast file
#
# params: none
# return: a Hit object containing the next hit
#         undef if no more hits
#
# usage: 
#   while ($hit = $blast->nextHit()) {...}
#
###
sub nextHit {
    my $self = shift;
    my $start;
    my $end;
    my $isLast;

    return undef if ($self->{noMoreHits});
    
    # find the boundaries of this hit
    ($start, $isLast) = $self->seekNextHit();
    ($end, $isLast) = $self->seekNextHit($start);

    # if we found boundaries, create a hit object with those lines
    # and return it
    if ($start && $end) {
	$end--;
        my @hit = @{$self->{blastFile}}[$start..$end];
	$self->{curLine} = $end;
	$self->{noMoreHits} = 1 if ($isLast);
	return new Hit($self, @hit);
	### return new Hit($self, $self->{'queryLength'}, @hit);
    } else {
        return undef;
    }
}


###
# seek to the final psi-blast iteration
#
# params: $round, the psi-blast round to find 
#          (defaults to last round if not passed)
# return: line number where the last iteration starts
###
sub findLastPsiIter {
    my $self = shift;
    my $round = shift;
    my @blastFile = @{$self->{blastFile}};

    # get all the psi-blast iteration lines
    my @resLines = grep (/Results from round/, @blastFile);

    ### return 0 unless (@resLines);
    # Above seems to make Blast header info the first "hit", 
    # messing up Hsps later (i.e. seqclust fails)
    # so use test below to point to actual start of blastp hit info.
    # (actually the line *before* the first hit, so that nextHit 
    # behaves well.

    # If not psi, return pointer to (just before) first hit of blastp.
    unless (@resLines) {
	my $i;
	for ($i=0 ; $i < $self->{numLines} ; $i++){
	    my $line = $self->{blastFile}[$i];
	    if ($line =~ /^>/ ) {
		return ($i-1);
	    }
	}
    }

    # get the $round number line or the last
    my $last;
    if (($round) && ($round <= $#resLines)) {
        $last = $resLines[$round-1];
    } else {
        $last = $resLines[$#resLines];
    }

    # scan for the last such line
    my $i;
    for ($i=0 ; $i < $self->{numLines} ; $i++) {
	my $line = $self->{blastFile}[$i];
        last if ($line eq $last);
    }
     
    return $i;
}


###
# return the name of the blast query sequence
###
sub query {
    my $self = shift;

    # find the line with the query name on it
    foreach my $line (@{$self->{blastFile}}) {
	if ($line =~ /^Query= ([\S]*)/) {
	    return $1;
	}
    }

    return undef ;
}


###
# return the length of the blast query sequence
###
sub length_query {
    my $self = shift;

    # find the line with the query name on it
    foreach my $line (@{$self->{blastFile}}) {
      if ($line =~ /^[\s]+\(([\S]+) letters\)/) { 
	my $queryLen = $1; 
	$queryLen=~s/,//; 
	unless ($queryLen=~/^\d+$/) { 
	  warn "Cannot parse query length: $line\n"; 
	} 
	return $queryLen;
      }  
    } 
    return undef ;
}

###
# run blast
#
# params: $alg, the algorithm to use
#         $db, the database to search
#         $query, the query sequence
#         $args, an argument string
#         $mask, a flag - if set, mask the sequence before running blast
#
# return: the name of the file with the results
###
sub runBlast {
    my $alg = shift;
    my $db = shift;
    my $query = shift;
    my $bargs = shift;
    my $mask = shift;
    my $args = shift;

    my $blastBinary;
    if ($args->{'blastBinary'}) {
      $blastBinary = $args->{'blastBinary'};
    } elsif ($alg eq "blastpgp") {
      $blastBinary = $alg;
    } else {
      $blastBinary = "legacy_blast.pl blastall";
    }

    my $numCpu = 1;
    $numCpu = $args->{'numCpu'} if ($args->{'numCpu'});

    # check ourselves out
    return undef unless ($alg =~ /blast/);

    # set up files
    my ($seqFile, $blastFile) = &makeBlastFiles($query);

    # call blast
    my $cmd = $blastBinary . " -I T -v 5001 -b 5001 ";
    $cmd .= "-i $seqFile ";
    $cmd .= "-d $db ";
    $cmd .= "-a $numCpu ";
    unless ($mask){
	$cmd .= "-F 0 ";
    }
    $cmd .= "-o $blastFile ";
    $cmd .= "-p $alg " unless ($alg eq "blastpgp");
    $cmd .= $bargs;

    if (system($cmd)) { 
      print STDERR "FATAL ERROR : $cmd \nSystem command returned error status: ($!)\n"; 
      unlink($seqFile);
      return undef; 
    } 

    # cleanup 
    unlink($seqFile);

    # check ourselves out
    return undef unless (-f $blastFile);

    my $blastFile1 = "$blastFile-1";

    open (FH, $blastFile);
    open (OUT, ">$blastFile1");
    while (my $line=<FH>){
	chomp $line;
	if ($line=~/lcl\|PTHR/){
	    $line=~s/lcl\|//;
	    print OUT "$line\n";
	}else{
	    print OUT "$line\n";
	}
    }
    close (OUT);
    close (FH);

    # return the file
    return $blastFile1;
}


###
#  mask a sequence
#
# params: $query, the query sequence
#
# return: the masked sequence in fasta format
###
sub runMask {
    my $query = shift;

    my ($seqFile, $blastFile) = &makeBlastFiles($query);
    my $maskCmd = "seg $seqFile -x";
    my $mask = `$maskCmd`;
    unlink $seqFile if (-f $seqFile);
    unlink $blastFile if (-f $blastFile);

    if ($mask) {
         return $mask;
    } else {
	return $query;
    }
}




###
# creates sequence & blast file names
# and writes out the seq info to the seq file
#
# params: $seq, the query sequence in fasta format
# return: the names of the query & result files
#         (does not run blast - result file is empty)
###
sub makeBlastFiles {
 
    my $seq = shift;

    my $seqName = $1 if ($seq =~ />[^\w\d_]*([\w\d_]+)/);

    my $seqFile = "$tempDir/seqclust.$seqName.$$.fasta";
    my $blastFile = "$tempDir/seqclust.$seqName.$$.blast";
    open(SEQ, ">$seqFile") || die ("Unable to write sequence for blast");
    print SEQ $seq;
    close SEQ;
    return ($seqFile, $blastFile);
}

####
# sub splitBlast takes a blast file and examines it to see if it 
# contains multiple runs.  If it does, it splits them into temp
# files and returns their filenames
#
# @params $blastFile - the filename of the original blast file
# @return @tempFiles - the split files.
####
sub splitBlast {
    my $blastFile = shift;
    my $seqName = shift;

    # get file
    open(BFILE, $blastFile);
    my @blast = <BFILE>;
    close BFILE;

    my $count = 0;
    my @files;
    open(TEMP, ">/tmp/dummy");
    foreach my $line (@blast) {
	if ($line =~ /^BLAST/) {
	    $count++;
	    close TEMP;
            my $tempFile = "$tempDir/seqclust.$seqName.$$.$count.blast";
	    push (@files, $tempFile);
            open(TEMP, ">$tempFile");
        }
        print TEMP $line;
    }
    close TEMP;
    unlink("/tmp/dummy");
    return @files;
}
 

###
# test harness for "new Blast($file)"
###
sub testFile {
    my $bo = new Blast("test.blast");

    while (my $hit = $bo->nextHit()) {
	print "HIT: ", $hit->name(), "\t", $hit->expect(), "\n";
	while (my $hsp = $hit->nextHsp()) {
	    print "HSP: ", $hsp->expect(), "\n";
	}
    }
}


###
# test harness for "run Blast(...)"
###
sub testRun {
    my $seq = ">test1\nGPGVTHLKVGDRVGIPWLYSACGHCDYCLSGQETLCERQQNAGYSVDGGYAE\n";
    my $bo = run Blast("blastpgp", "/mag/research/DB/blast/nr", $seq);

    while (my $hit = $bo->nextHit()) {
	print "HIT: ", $hit->name(), "\t", $hit->expect(), "\n";
	while (my $hsp = $hit->nextHsp()) {
	    print "HSP: ", $hsp->expect(), "\n";
	}
    }
}

# &testNew;
#&testRun;

1;  # required for library loading 
