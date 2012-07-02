#! /usr/local/bin/perl

=head1 NAME

Hmmer.pm - a HMMER parsing object

Copyright (C)) 2005 Applied Biosystems. 
This file may be copied and redistributed freely, without advance permission,
provided that this Copyright statement is reproduced with each copy. 

LIMITATION OF WARRANTY
NOTHING IN THIS AGREEMENT WILL BE CONSTRUED AS A REPRESENTATION MADE OR
WARRANTY GIVEN BY APPLIED BIOSYSTEMS OR ANY THIRD PARTY THAT THE USE OF
DATA PROVIDED HEREUNDER WILL NOT INFRINGE ANY PATENT, COPYRIGHT, TRADEMARK
OR OTHER RIGHTS OF ANY THIRD PARTY. DATA IS PROVIDED "AS IS" WITHOUT
WARRANTY OF ANY KIND WHATSOEVER, EXPRESS OR IMPLIED, INCLUDING IMPLIED
WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. APPLIED
BIOSYSTEMS MAKES NO WARRANTY THAT ITS DATA DOES NOT CONTAIN ERRORS.

=head1 SYNOPSIS

synopsis

=head1 DESCRIPTION

=head1 VERSIONS

v 1.0 - 10/15/2004
v 1.1 - 01/20/2005  make it defaut to only use 1 cpu per process

=head1 AUTHORS / ACKNOWLEDGEMENTS

Anish Kejariwal kejarian@applera.com

=cut

package Hmmer;
use strict;
use Algs::HmmerHit;

# CLASS VARIABLES 
my $tmpDir = "/usr/tmp";  # where to store itermediate results

#does tmp dir work for interpro

=head2 new

 Description: Constructor creates a new HMMER object from a file
 Arguments:   $file    The name of a HMMER output file
 Returns:     $self object

=cut

sub new { 
  my $class = shift; 
  my $file = shift; 
  my $self = {}; 
  bless $self, $class; 
  $self->init($file); 
  return $self; 
} 

=head2 new

 Description: create a new hmmer object by running hmmsearch
 Arguments:   $db  The database to search
              $model  The filename of HMMER model file
              $args  Argument string to pass
              $hmmesearchFile If passed,results file will be kept on filesystem
              $errFile  File to catch errors from the run...undef for /dev/null
 Returns:     $self object

=cut

sub run {
    my $class = shift;
    my $db = shift;
    my $model = shift;
    my $args = shift;
    my $hmmsearchFile = shift;
    my $errFile = shift;

    $tmpDir = $args->{'tmpDir'} if (defined($args->{'tmpDir'})); 


    # set up and run hmmer
    my $hmmsearchResFile;
    return undef unless ($hmmsearchResFile = &runHmmer($db,$model,$args,$hmmsearchFile,$errFile));

    # create ourselves from this output
    my $self = {};
    bless $self, $class;
    $self->init($hmmsearchResFile);

    # cleanup
    unlink($hmmsearchResFile) unless ($hmmsearchFile);
    return $self;
}


###
# initialize the hmmer object
# not usually called by outside program
# new() does this
#
# params: $file, the name of a sam file
# return: this hmmer object or undef on error
###

#may just go to part with alignments, since mayb all info is there????

sub init {
  my $self = shift;
  my $file = shift;
  
  if (open(TMP, "<$file")) {
    $self->{fileName} = $file;
    
    #need 'parsed for domains' section, as well as 'Alignments' section
    my $reachedAlignments = 0;
    my @scoreLines;
    my @alignments;
    my $alignmentText;
    while(my $line =<TMP>) {
      return undef if ($line=~/\[no hits above thresholds\]/);

      if ($line=~/^Parsed for domains:/) {
	$line = <TMP>; # 'Sequence' line
	$line = <TMP>; # '----' line
	while(my $line = <TMP>) {
	  last if ($line=~/^\s+/);
	  push(@scoreLines,$line);
	}
      }

      if ($line =~/^Alignments of top-scoring domains:/) {	   
	$reachedAlignments = 1;
	next;
      }

      next unless ($reachedAlignments);
      if ($line=~/^\S/) {
	push(@alignments,$alignmentText) if ($alignmentText);
	$alignmentText = "";
      }
      last if ($line=~/^Histogram of all scores:/);

      $alignmentText .= $line;
    }
    close(TMP);
    
    my $numAlignments = @alignments;
    my $numScores = @scoreLines;
    if ($numScores != $numAlignments) {
      die "Error parsing hmmsearch output file\t$file\t$numScores\$numAlignments\n";  
    }
    $self->{numHits} = $numScores;
    $self->{scores} = \@scoreLines;
    $self->{alignments} = \@alignments;
    $self->{curHit} = 1;
    $self->{noMoreHits} = undef;
    return $self;
  } else {
    return undef;
  }
 
}


sub nextHit {
    my $self = shift;
    my $start;
    my $end;
    my $isLast;

    # return nothing if we are past EOF
    return undef if ($self->{noMoreHits});

    # return nothing if there are no hits
    return undef unless ($self->{numHits});

    my $alignmentText = $self->{alignments}[$self->{curHit}-1];
    my $scoreLine = $self->{scores}[$self->{curHit}-1];

    # check that this isn't the last hit
    $self->{curHit}++;
    $self->{noMoreHits} = 1 if ($self->{curHit} > $self->{numHits});

    return new HmmerHit($scoreLine,$alignmentText);
}


###
# run hmmer
#
# params: $db, the database to search
#         $model, the model to search with
#
# return: the name of the file with the results
###
sub runHmmer {
    my $db = shift;
    my $model = shift;
    my $args = shift;
    my $hmmsearchFile = shift;
    my $errFile = shift;

    my $alg = "hmmsearch";
    $alg = $args->{'hmmsearchBinary'} if (defined($args->{'hmmsearchBinary'}));

    # set up files
    $hmmsearchFile = "$tmpDir/hmmer.$$" unless ($hmmsearchFile);
    $errFile = "/dev/null" unless ($errFile);

    my $numCpu = 1;
    $numCpu = $args->{'numCpu'} if ($args->{'numCpu'});

    #HMM file may or may not be compressed.  If it is compress, will have
    #gz extension.  Check, and if so, get compressed model, and do scoring
    my $compressedFlag;
    unless (-s $model) {
      if (-s "$model.gz") {
	$compressedFlag = 1;
	my $gunzip = (defined($args->{'gunzipBinary'}) ? $args->{'gunzipBinary'} : "gunzip");
	my $tmpunzipped = $tmpDir . "/hmm.unzipped.$$";
	if (system("$gunzip -c $model.gz > $tmpunzipped")) {
	  print STDERR "FATAL ERROR : unable to uncompress $model : ($!)\n";  
	  return undef;  
	}
	$model = $tmpunzipped;
      } else {
	print STDERR "FATAL ERROR : $model can not be accesed\n";
	return undef;
      }
    }

    #call hmmsearch
    my $cmd = "$alg -Z 10000 --cpu $numCpu $model $db";
    $cmd .= " 1> $hmmsearchFile";
    $cmd .= " 2>> $errFile";
    if (system($cmd)) { 
      unlink $model if ($compressedFlag); #remove tmp uncompressed HMM 
      print STDERR "FATAL ERROR : $cmd \nSystem command returned error status: ($!)\n"; 
      return undef; 
    } 
      
    unlink $model if ($compressedFlag); #remove tmp uncompressed HMM

    # check ourselves out
    if (! -s $hmmsearchFile) {
      print STDERR "Missing output hmmsearch file: $hmmsearchFile\n";
      return undef;
    }
      
    
    # return the file
    return $hmmsearchFile;
}


1;  # so the require or use succeeds  













