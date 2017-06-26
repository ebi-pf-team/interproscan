#!/usr/bin/perl

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
## pantherHmmScoreBlast.pl - a script to score protein sequences against
##                   the PANTHER HMM library. First, uses blast to prefilter
##                   results and determine which HMMs to score against
##
## v1.0 - Anish Kejariwal 11/1/04
## v1.01 - added the option for combining sequences alignments together if
## breaks < 15 AA apart by default  use -J 0 to remove this option.
##                                         -- Rozina Loo 01/12/05
##v2.0 - modified to use hmmer3 program for scoring. added the option to choose hmmscan or hmmsearch
##       need to provide the path to the directory containing hmmer3 binary executables or put the
##       executables in the system path.
##                             --Xiaosong Huang 08/24/2016
##
#####
#####

# necessary libraries
use lib 'lib';
use FamLibBuilder;
use FastaFile;
use strict;
use FileHandle;

# default values
local $| = 1;
my $type = "orig";
my $wtType = "mag";
my $smallEvalCutoff = 1e-70;
my $smallEvalCntLimit = 5;
my $blastEvalCutoff = .1;
my $evalDropCutoff = 1e-15;
my $distantHmmCutoff = 1e-3;
my $closelyHmmCutoff = 1e-23;
my $iprScanFamCutForFamsWithSf = 0.1;
my $tmpDir = "/tmp/panther11";
my $famHmmCutoff = 0.1;
my $prod = 1;
my $Scan = 1;
my ($inFile,$outFile,$errFile,$library,$verbose,$userHmmEvalCut,$namesFlag);
my ($displayType,$pantherBestHit,$allHits,$iprScanBestHit);
my ($hmmsearchPath,$tmpdirPath,$gunzipPath,$hmmBinPath);
my $numCpu;
my $usehmmsearch = 0;

#add optins for paths to blast and hmmsearch

# get command-line arguments
use Getopt::Long;
$Getopt::Long::ignorecase=0; #case sensitive
&GetOptions
(
 "h" => sub {&usage()},     # -h for help
 "l=s" => \$library,        # -l for panther (l)ibrary of HMMs
 "D=s" => \$displayType,    # -D for (D)isplay type (I,B,A)
 "E=f" => \$userHmmEvalCut, # -E for HMM (e)value cutoff
 "s" => \$usehmmsearch,      # -s for using hmmsearch program
 "H=s" => \$hmmBinPath,  # -H for path to hmmer3 binary directory containg hmmscan and hmmsearch programs
 "z:s" => \$gunzipPath,     # -z for gun(z)ip path to uncompress HMM
 "T=s" => \$tmpdirPath,     # -T for path to (T)mp directory
 "o=s" => \$outFile,        # -o for (o)utput file (redirect STDOUT)
 "i=s" => \$inFile,         # -i for (i)nput fasta file to search
 "t=s" => \$type,           # -t for entry (t)ype
 "n" => \$namesFlag,        # -n for (n)amesFlag to display HMM annotations
 "P=i" => \$prod,           # -P for (P)roduction version of library
 "c=i" => \$numCpu,           # -c for num of (c)pu to use for blast/hmmsearch
 "e=s" => \$errFile,        # -e for (e)rror file (redirect STDERR)
 "V" => \$verbose           # -v for (v)erbose (debug info to STDERR)
);

print STDERR "$0 starts at ". localtime() . "\n";

&usage("Please specify library\n") unless (-d $library);
&usage("Please specify input fasta file\n") unless (-s $inFile);

#check to make sure that only one scoring method was specified
if ($displayType eq "I") {
  $iprScanBestHit = 1;
} elsif ($displayType eq "B") {
  $pantherBestHit = 1;
} elsif ($displayType eq "A") {
  $allHits = 1;
} else {
  &usage("Please specify I, B, or A for -D option\n");
}

my $hmmsearch = "hmmsearch";
my $hmmscan = "hmmscan";
if ($hmmBinPath && (! -e $hmmBinPath)) {
  &usage("User defined hmmsearch or hmmscan path $hmmBinPath cannot be accessed\n");
}
if ($hmmBinPath && ( -e $hmmBinPath) && ($usehmmsearch)) {
  $hmmsearch = "$hmmBinPath/hmmsearch";
}
if ($hmmBinPath && ( -e $hmmBinPath) && (! $usehmmsearch)){
   $hmmscan = "$hmmBinPath/hmmscan";
}
if ($gunzipPath && (! -s $gunzipPath)) {
  &usage("User defined gunzip path $gunzipPath cannot be accessed\n");
}
if ($tmpdirPath && (! -d $tmpdirPath)) {
  &usage("User defined tmp dir path $tmpdirPath cannot be accessed\n");
}

&pukeSetup() if ($verbose);

if ($outFile) {
    open (STDOUT, ">$outFile") || &usage("Cannot open $outFile");
}
if ($errFile) {
    open (STDERR, ">$errFile") || &usage("Cannot open $errFile");
}

#######################################################
###  MAIN BEGINS HERE
#######################################################

$tmpDir = $tmpdirPath if ($tmpdirPath);

my $libType = ($prod ? "prod" : "build");
my $flb = new FamLibBuilder($library,$libType);
&usage("Cannot find library specified as $library.\n") unless ($flb->exists());


#determine evalue cutoff, depending on format for output results
my $hmmEvalCut;
if ($userHmmEvalCut) {
  $hmmEvalCut = $userHmmEvalCut;
} elsif ($allHits || $pantherBestHit) {
  $hmmEvalCut = $distantHmmCutoff;
} elsif ($iprScanBestHit) {
  $hmmEvalCut = $closelyHmmCutoff;
}

my  $hmmNames = &getHmmNames($flb->namesTab()) if ($namesFlag);

my $allScores = {};

if (! $usehmmsearch){
    print STDERR "RUNNING parseHmmscan\n";

    my $hmmModel = $flb->binHmm;
	$allScores = &parseHmmscan($inFile,$hmmModel,$allScores);
}
else{
    print STDERR "RUNNING parseHmmsearch2\n";
    my $hmmModel = $flb->binHmm;
	$allScores = &parseHmmsearch2($inFile,$hmmModel,$allScores);
}

my $forceUsehmmsearch = 0;
if ($forceUsehmmsearch) {
	my $hmmsearch_help = "$hmmsearch -h > $tmpDir/hmmsearch_help";
    system("$hmmsearch_help");
    open (HH, "$tmpDir/hmmsearch_help");
    my $l1 = <HH>;
    my $line2 = <HH>;
    print STDERR "HMMER3 hmmsearch binary executable is not found, HMMER2 program does not work for this script.\n" and exit unless ($line2 =~ m/^#\sHMMER\s3/);
	my $ff = new FastaFile($inFile);
	$ff->open();
	my @books = $flb->bookNames();
	# loop over all models in the library
	foreach my $fam (@books) {
	  my $fle = $flb->getLibEntry($fam,$type);

	  #maybe should check that hmm file exits
	  my $hmmFile = $fle->hmmFile($wtType,"hmmer");
	  my @seqsForFam = $ff->getIds();
	  my $numSeqsForFam = @seqsForFam;

	  print STDERR "RUNNING $numSeqsForFam on $fam\n" if ($verbose);

	  #score seqs aginast family HMM, and store scores in our hash
	  $allScores = &runHmmsearch($inFile,$hmmFile,$allScores,$fam);

	  #if there is only one subfamiliy, only scoe against the family
	  my @sfs = $fle->hmmerSfHmmFiles();
	  my $numSfs = @sfs;
	  next if ($numSfs == 1);

	  #now score against all SFs
	  my @sfHmmFiles = $fle->sfHmmFiles("hmmer");
	  my @sfIDs = map {/\/(SF\d+)\//} @sfHmmFiles;
	  foreach my $sfId (@sfIDs) {
		my @seqsForSf = $ff->getIds();
		my $numSeqsForSf = @seqsForSf;
		print STDERR "RUNNING $numSeqsForSf on $fam:$sfId\n" if ($verbose);

		my $sfHmmFile = $fle->hmmFile($sfId,"hmmer");

		$allScores = &runHmmsearch($inFile,$sfHmmFile,$allScores,"$fam:$sfId");
	  }
	}
}


#now deterimine best hit, and print out scores
foreach my $seqId (keys %$allScores) {
  if ($allHits) {
    foreach my $hmm (keys %{$allScores->{$seqId}}) {
      my $eval = $allScores->{$seqId}{$hmm}{'eval'};
      next if ($eval > $hmmEvalCut);
      &printRes($seqId,$hmm,$eval,$allScores->{$seqId}{$hmm}{'score'},$allScores->{$seqId}{$hmm}{'seqRange'},$hmmNames->{$hmm});
    }

  } elsif ($pantherBestHit) {
    my $hmm = &getBestHit($allScores,$seqId,$hmmEvalCut);
    next unless ($hmm);
    &printRes($seqId,$hmm,$allScores->{$seqId}{$hmm}{'eval'},$allScores->{$seqId}{$hmm}{'score'},$allScores->{$seqId}{$hmm}{'seqRange'},$hmmNames->{$hmm});

  } elsif ($iprScanBestHit) {
    my $hmm = &getBestHit($allScores,$seqId,$hmmEvalCut);
    next unless ($hmm);
    &printRes($seqId,$hmm,$allScores->{$seqId}{$hmm}{'eval'},$allScores->{$seqId}{$hmm}{'score'},$allScores->{$seqId}{$hmm}{'seqRange'},$hmmNames->{$hmm},1);
    my ($fam,$sf) = split(/:/,$hmm);
    if ($sf) {
      #if doing ipr scan and have SF hit, get fam info (if fam meet threshold)
      #get sf score info, and use sf seq range
      &printRes($seqId,$fam,$allScores->{$seqId}{$hmm}{'eval'},$allScores->{$seqId}{$hmm}{'score'},$allScores->{$seqId}{$hmm}{'seqRange'},$hmmNames->{$fam},1);
    }
  }
}

print STDERR "$0 ends at ". localtime() . "\n";





#######################################################
###  SUBROUTINES BEGIN HERE
#######################################################

sub printRes {
  my $seqId = shift;
  my $hmm = shift;
  my $eval = shift;
  my $score = shift;
  my $seqRange = shift;
  my $name = shift;
  my $splitRanges = shift;

  return unless ($seqRange);
  my $outStr = "$seqId\t$hmm\t$name\t$eval\t$score";
  if ($splitRanges) {
    my @ranges = split(/,/,$seqRange);
    foreach my $range (@ranges) {
      print $outStr . "\t$range\n";
    }
  } else {
    print $outStr . "\t$seqRange\n";
  }
}


sub getHmmNames {
  my $namesFile = shift;

  my %namesMap;
  open(NAMES,$namesFile) || print STDERR "Cannot read names.tab file\n";
  while(my $line = <NAMES>) {
    chomp $line;
    my ($hmm,$def) = split(/\t/,$line);
    my ($book,$sf) = split(/\./,$line);
    my $acc = $book;
    $acc .= ":$sf" if ($sf && ($sf ne "mag"));
    $namesMap{$acc}=$def;
  }
  close(NAMES);
  return(\%namesMap);
}






sub getBestHit {
  my $allScores = shift;
  my $seqId = shift;
  my $cutoff = shift;

  my ($bestHmm,$bestEval,$bestScore);
  foreach my $hmm (keys %{$allScores->{$seqId}}) {
    my $eval = $allScores->{$seqId}{$hmm}{'eval'};
    my $score = $allScores->{$seqId}{$hmm}{'score'};
    next if ($eval > $cutoff);

    #define best hit, if: 1st time; this is the best eval; this eval equal
    #to best eval, AND this score is better define as best hit
    if ( (! defined($bestEval)) || ($eval < $bestEval) ||
	 (($eval == $bestEval) && ($score > $bestScore))) {
      $bestHmm = $hmm;
      $bestEval = $eval;
      $bestScore = $score;
    }
  }
  return $bestHmm;
}



sub runHmmsearch {
  my $inFile = shift; # input fasta file
  my $hmmFile = shift;
  my $allScores = shift;
  my $hmmId = shift;
  my $domtbl="$tmpDir/$hmmId\.domtblout";

  my $hmmsearch_run = "$hmmsearch --domtblout $domtbl $hmmFile $inFile > /dev/null";

  system("$hmmsearch_run");
  open TB, "$domtbl" or die $!;
	while (<TB>){
		next if (/^#/);
		my ($seq, $acc1, $tlen, $hmma, $acc2, $qlen, $eVal, $score, $bias, $num, $num_t, $cEval, $iEval, $dscore, $dbias, $hmm_f, $hmm_t, $ali_f, $ali_t, $env_f, $env_t, @else) = split (/\s+/, $_);
		$seq =~ s/^\s+|\s+$//g;
		$eVal =~ s/^\s+|\s+$//g;
		$score =~ s/^\s+|\s+$//g;
		$allScores->{$seq}{$hmmId}{'eval'} = $eVal;
		$allScores->{$seq}{$hmmId}{'score'} = $score;
		$allScores->{$seq}{$hmmId}{'seqRange'} = "$ali_f-$ali_t"; # need to figure out


	}
	close TB;
	unlink $domtbl;
  return $allScores;
}


sub runHmmsearch2 {
  my $inFile = shift; # input fasta file
  my $hmmModel = shift;
  my $allScores = shift;
  my $domtbl="$tmpDir/temp\.domtblout";
  my $hmmsearch_run = "$hmmsearch --domtblout $domtbl $hmmModel $inFile > /dev/null";

  system("$hmmsearch_run");
  open TB, "$domtbl" or die $!;
  	while (<TB>){
  		next if (/^#/);
  		my ($seq, $acc1, $tlen, $hmma, $acc2, $qlen, $eVal, $score, $bias, $num, $num_t, $cEval, $iEval, $dscore, $dbias, $hmm_f, $hmm_t, $ali_f, $ali_t, $env_f, $env_t, @else) = split (/\s+/, $_);

  		my ($hmm_fam, $hmm_sf, @else2) = split (/\./, $hmma);
        my $hmmId = $hmm_fam;
        $hmmId = "$hmmId:$hmm_sf" if $hmm_sf =~ /^SF\d+/;

  		$seq =~ s/^\s+|\s+$//g;
  		$eVal =~ s/^\s+|\s+$//g;
  		$score =~ s/^\s+|\s+$//g;
  		$allScores->{$seq}{$hmmId}{'eval'} = $eVal;
  		$allScores->{$seq}{$hmmId}{'score'} = $score;
  		$allScores->{$seq}{$hmmId}{'seqRange'} = "$ali_f-$ali_t"; # need to figure out


  	}
	close TB;
	unlink $domtbl;
  return $allScores;
}

sub parseHmmsearch2 {
  my $inFile = shift; # input fasta file
  my $hmmModel = shift;
  my $allScores = shift;
  my $domtbl= $inFile;
  #my $hmmsearch_run = "$hmmsearch --domtblout $domtbl $hmmModel $inFile > /dev/null";

  print STDERR "running parseHmmsearch2";
  #system("$hmmsearch_run");
  open TB, "$domtbl" or die $!;
  	while (<TB>){
  		next if (/^#/);
  		my ($seq, $acc1, $tlen, $hmma, $acc2, $qlen, $eVal, $score, $bias, $num, $num_t, $cEval, $iEval, $dscore, $dbias, $hmm_f, $hmm_t, $ali_f, $ali_t, $env_f, $env_t, @else) = split (/\s+/, $_);

  		my ($hmm_fam, $hmm_sf, @else2) = split (/\./, $hmma);
        my $hmmId = $hmm_fam;
        $hmmId = "$hmmId:$hmm_sf" if $hmm_sf =~ /^SF\d+/;

  		$seq =~ s/^\s+|\s+$//g;
  		$eVal =~ s/^\s+|\s+$//g;
  		$score =~ s/^\s+|\s+$//g;
  		$allScores->{$seq}{$hmmId}{'eval'} = $eVal;
  		$allScores->{$seq}{$hmmId}{'score'} = $score;
  		$allScores->{$seq}{$hmmId}{'seqRange'} = "$ali_f-$ali_t"; # need to figure out

  	}
	close TB;
	#unlink $domtbl;
  return $allScores;
}

sub parseHmmscan {
  my $inFile = shift; # input fasta file
  my $hmmModel = shift;
  my $allScores = shift;
  my $domtbl=$inFile;
  #my $hmmscan_run = "$hmmscan --domtblout $domtbl $hmmModel $inFile > /dev/null";

  #system("$hmmscan_run");
  open TB, "$domtbl" or die $!;
	while (<TB>){
		next if (/^#/);
		my ($target, $acc1, $tlen, $seq, $acc2, $qlen, $eVal, $score, $bias, $num, $num_t, $cEval, $iEval, $dscore, $dbias, $hmm_f, $hmm_t, $ali_f, $ali_t, $env_f, $env_t, @else) = split (/\s+/, $_);
		$target =~ s/^\s+|\s+$//g;
		my ($hmm_fam, $hmm_sf, @else2) = split (/\./, $target);
		my $hmmId = $hmm_fam;
		$hmmId = "$hmmId:$hmm_sf" if $hmm_sf =~ /^SF\d+/;
		$seq =~ s/^\s+|\s+$//g;
		$eVal =~ s/^\s+|\s+$//g;
		$score =~ s/^\s+|\s+$//g;
		$allScores->{$seq}{$hmmId}{'eval'} = $eVal;
		$allScores->{$seq}{$hmmId}{'score'} = $score;
		$allScores->{$seq}{$hmmId}{'seqRange'} = "$ali_f-$ali_t"; # need to figure out


	}
	close TB;
	#unlink $domtbl;
  return $allScores;
}





sub runHmmscan {
  my $inFile = shift; # input fasta file
  my $hmmModel = shift;
  my $allScores = shift;
  my $domtbl="$tmpDir/temp\.domtblout";
  my $hmmscan_run = "$hmmscan --domtblout $domtbl $hmmModel $inFile > /dev/null";

  system("$hmmscan_run");
  open TB, "$domtbl" or die $!;
	while (<TB>){
		next if (/^#/);
		my ($target, $acc1, $tlen, $seq, $acc2, $qlen, $eVal, $score, $bias, $num, $num_t, $cEval, $iEval, $dscore, $dbias, $hmm_f, $hmm_t, $ali_f, $ali_t, $env_f, $env_t, @else) = split (/\s+/, $_);
		$target =~ s/^\s+|\s+$//g;
		my ($hmm_fam, $hmm_sf, @else2) = split (/\./, $target);
		my $hmmId = $hmm_fam;
		$hmmId = "$hmmId:$hmm_sf" if $hmm_sf =~ /^SF\d+/;
		$seq =~ s/^\s+|\s+$//g;
		$eVal =~ s/^\s+|\s+$//g;
		$score =~ s/^\s+|\s+$//g;
		$allScores->{$seq}{$hmmId}{'eval'} = $eVal;
		$allScores->{$seq}{$hmmId}{'score'} = $score;
		$allScores->{$seq}{$hmmId}{'seqRange'} = "$ali_f-$ali_t"; # need to figure out


	}
	close TB;
	unlink $domtbl;
  return $allScores;
}





# pukeSetup pukes back global setup variables, parsed from @AR
sub pukeSetup {
  print STDERR "_"x50, "\n";
  print STDERR "Verbose level is high.\n";
  print STDERR "Input fasta file is: $inFile\n";
  print STDERR "Display Type: $displayType\n";
  if ($userHmmEvalCut) {
    print STDERR "User defined hmm score cutoff of $userHmmEvalCut\n";
  }
  if ($hmmBinPath) {
     print STDERR "User defined path to HMMER3 binary folder: $hmmBinPath\n";
  }
  if ($gunzipPath) {
     print STDERR "User defined path to gunzip: $gunzipPath\n";
  }
  if ($tmpdirPath) {
    print STDERR "User defined path to tmp dir: $tmpdirPath\n";
  }
  print STDERR "library: $library\n";
  print STDERR "Output file is: $outFile\n" if ($outFile);
  print STDERR "Error file is: $errFile\n" if ($errFile);
  print STDERR "_"x50, "\n";
}



# output for help and errors
sub usage {
    my $error = shift;

    print <<__EOT;

pantherScore2.0.pl - a script to score protein sequences against the
hmmer3 PANTHER HMM library using  hmmer3 programs


Usage:
    pantherScore2.0.pl -l <PANTHER library> -D B -V
                    -i <fasta file> -o <output file>

Example:


use hmmscan:
./pantherScore2.0.pl -l PANTHER11.1 -D B -V -H ./hmmer-3.1b2/binaries/ -i homo_sapiens.fasta -o homo_sapiens.out -n

use hmmsearch:
./pantherScore2.0.pl -l PANTHER11.1 -D B -V -H ./hmmer-3.1b2/binaries/ -i homo_sapiens.fasta -o homo_sapiens.out -n -s

Where args are:
\t-h for help (this message)
\t-l PANTER (l)ibrary with HMMs when using hmmsearch, binary PANTHER HMM model when using hmmscan
\t-D display type for results
\t\toptions: I (interproscan), B (best hit), A (all hits)
\t-E user defined hmm (E)value cutoff to over ride default
\t-s to use hmmsearch instead of hmmscan for scoring sequence againt hmm library
\t-H user defined path to direcgtory contating hmmer3 binary executables (including hmmscan or hmmsearch)
\t\tdefault: hmmer3 binary in \$PATH
\t-z user defined path to gun(z)ip path
\t\tdefault: gunzip binary in \$PATH
\t-n to display family and subfamily names in the output
\t-T user defined path for tmp directory
\t\tdefault: /tmp/
\t-o (o)utput file (redirect STDOUT)
\t-i (i)nput fasta file to score
\t-t for entry (t)ype
\t-n to display family and subfamily names in the output
\t-P P=1 to for (P)roduction format of lib; P=0 for build version
\t\tdefault: 1
\t-c for number of (c)pu on machine to use for blast/hmmsearch
\t\tdefault: 1
\t-e (e)rror file (redirect STDERR)
\t-v (v)erbose (debug info to STDERR)
\t-V (V)ery verbose (debug info to STDERR)
__EOT

  print "Error: $error\n\n" if ($error);

  exit(-1);
}
