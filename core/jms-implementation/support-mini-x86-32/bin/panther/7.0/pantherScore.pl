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
## pantherHmmScoreBlast.pl - a script to score protein sequences against 
##                   the PANTHER HMM library. First, uses blast to prefilter 
##                   results and determine which HMMs to score against
##
## v1.0 - Anish Kejariwal 11/1/04
## v1.01 - added the option for combining sequences alignments together if 
## breaks < 15 AA apart by default  use -J 0 to remove this option.
##                                         -- Rozina Loo 01/12/05
##
#####
#####

# necessary libraries
use FamLibBuilder;
use Algs::Hmmer;
use FastaFile;
use Blast;
use strict;
use FileHandle;

# default values
local $| = 1;
my $type = "orig";
my $wtType = "mag";
my $searchAlg = "blastp";
my $nonZeroBlastHitLimit = 2;
my $smallEvalCutoff = 1e-70;
my $smallEvalCntLimit = 5;
my $blastEvalCutoff = .1;
my $evalDropCutoff = 1e-15;
my $distantHmmCutoff = 1e-3;
my $closelyHmmCutoff = 1e-23;
my $iprScanFamCutForFamsWithSf = 0.1;
my $tmpDir = "/usr/tmp";
my $famHmmCutoff = 0.1;
my $prod = 1;
my ($inFile,$outFile,$errFile,$library,$verbose,$userHmmEvalCut,$namesFlag);
my ($displayType,$pantherBestHit,$allHits,$iprScanBestHit);
my ($blastPath,$hmmsearchPath,$tmpdirPath,$gunzipPath);
my $joinAlnFlag = 1;
my $numCpu;

#add optins for paths to blast and hmmsearch

# get command-line arguments
use Getopt::Long;
$Getopt::Long::ignorecase=0; #case sensitive
&GetOptions 
(
 "h" => sub {&usage()},     # -h for help
 "D=s" => \$displayType,    # -D for (D)isplay type (I,B,A)
 "E=f" => \$userHmmEvalCut, # -E for HMM (e)value cutoff
 "B=s" => \$blastPath,      # -B for path to (B)last binary
 "H=s" => \$hmmsearchPath,  # -H for path to (H)mmsearch binary
 "z:s" => \$gunzipPath,     # -z for gun(z)ip path to uncompress HMM
 "T=s" => \$tmpdirPath,     # -T for path to (T)mp directory
 "o=s" => \$outFile,        # -o for (o)utput file (redirect STDOUT)
 "l=s" => \$library,        # -l for panther (l)ibrary of HMMs
 "i=s" => \$inFile,         # -i for (i)nput fasta file to search
 "t=s" => \$type,           # -t for entry (t)ype
 "n" => \$namesFlag,        # -n for (n)amesFlag to display HMM annotations
 "P=i" => \$prod,           # -P for (P)roduction version of library
 "J=i" => \$joinAlnFlag,    # -J for (J)oin alignment flag
 "c=i" => \$numCpu,           # -c for num of (c)pu to use for blast/hmmsearch
 "e=s" => \$errFile,        # -e for (e)rror file (redirect STDERR)
 "V" => \$verbose           # -v for (v)erbose (debug info to STDERR)
);

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

if ($blastPath && (! -s $blastPath)) {
  &usage("User defined blast path $blastPath cannot be accessed\n");
}
if ($hmmsearchPath && (! -s $hmmsearchPath)) {
  &usage("User defined hmmsearch path $hmmsearchPath cannot be accessed\n");
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
&usage("Cannot find library $library.\n") unless ($flb->exists());

my $blastDb = $flb->consensusFasta();
&usage("Error with library - missing blastable $blastDb\n") unless (-s "$blastDb.psq");

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

my $fh = new FileHandle($inFile,"r") || die "Cannot create file handle for $inFile\n";
my $indexHash = &indexFasta($fh);

#blast sequence against consensus seqs of HMM library to
#determine which HMMs to score against
my ($selectedFams,$selectedSfs) = &getHmmsToScoreAgainst($inFile);

my $allScores = {};
# loop over all models in the library
foreach my $fam (keys %$selectedFams) {
  my $fle = $flb->getLibEntry($fam,$type);

  #maybe should check that hmm file exits
  my $hmmFile = $fle->hmmFile($wtType,"hmmer");
  my @seqsForFam = keys %{$selectedFams->{$fam}};
  my $numSeqsForFam = @seqsForFam;

  print STDERR "RUNNING $numSeqsForFam on $fam\n" if ($verbose);

  my $tmpFile = $tmpDir . "/scorehmm.$fam.$$.fasta";
  &writeTmpFileViaIndex($tmpFile,\@seqsForFam,$indexHash,$fh);

  #score seqs aginast family HMM, and store scores in our hash
  $allScores = &runHmmer($tmpFile,$hmmFile,$allScores,$fam);
  unlink $tmpFile;

  #if there is only one subfamiliy, only scoe against the family
  my @sfs = $fle->hmmerSfHmmFiles();
  my $numSfs = @sfs;
  next if ($numSfs == 1);

  #now score against all SFs
  foreach my $sfId (keys %{$selectedSfs->{$fam}}) {
    my @seqsForSf = keys %{$selectedSfs->{$fam}{$sfId}};
    my $numSeqsForSf = @seqsForSf;
    print STDERR "RUNNING $numSeqsForSf on $fam:$sfId\n" if ($verbose);

    my $sfHmmFile = $fle->hmmFile($sfId,"hmmer");
    my $tmpFile = $tmpDir . "/scorehmm.$fam.$sfId.$$.fasta";
    &writeTmpFileViaIndex($tmpFile,\@seqsForSf,$indexHash,$fh);

    $allScores = &runHmmer($tmpFile,$sfHmmFile,$allScores,"$fam:$sfId");
    unlink $tmpFile;
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


#######################################################
###  SUBROUTINES BEGIN HERE
#######################################################

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


sub getSeq {
  my $fh = shift; 
  my $indexHash = shift;
  my $id = shift;
 
 my $seekPos = $indexHash->{$id};
  print STDERR "$id does not exist in sequence index\n" unless (defined($seekPos));

  #go to sequence
  seek $fh, $seekPos, 0;
  
  #grab sequence
  my @seq;

  #get the first > line
  my $line = <$fh>;
  push (@seq, $line);
  # now read in the sequence
  while ($line = <$fh>) {
    last if ($line =~ /^>/);
    push (@seq, $line);
  }
  if (@seq && $seq[0]=~/[>\|]$id[\|\s]/) { 
    return (join("",@seq)); 
  } else {
    return undef;
  }

}

sub indexFasta {
  my $fh = shift;

  my %fastaIdx;
  my $curPos = 0;
  my $fastaPos = 0; 
   while (my $line = <$fh>) {
    $fastaPos = $curPos;
    $curPos = tell $fh;
    next unless ($line=~/^>/);
    my $id = FastaFile::id($line);
    $fastaIdx{$id} = $fastaPos;
  }
  return \%fastaIdx;
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

sub runHmmer {
  my $tmpFile = shift;
  my $hmmFile = shift;
  my $allScores = shift;
  my $hmmId = shift;

  my $args = {}; 
  $args->{'hmmsearchBinary'} = $hmmsearchPath if ($hmmsearchPath); 
  $args->{'gunzipBinary'} = $gunzipPath if ($gunzipPath); 
  $args->{'tmpDir'} = $tmpdirPath if ($tmpdirPath); 
  $args->{'numCpu'} = $numCpu if ($numCpu);

  my $hmmerObj = run Hmmer($tmpFile,$hmmFile,$args);

  if ($hmmerObj) {
    while (my $hit = $hmmerObj->nextHit()) {
      my $hitName = $hit->id();
      $allScores->{$hitName}{$hmmId}{'eval'} = $hit->evalue();
      $allScores->{$hitName}{$hmmId}{'score'} = $hit->score();
      $allScores->{$hitName}{$hmmId}{'seqRange'} = $hit->seqRange($joinAlnFlag);
    }
  }
  return $allScores;
}



sub getHmmsToScoreAgainst {
  my $file = shift;
  my $selectedFams = {};
  my $selectedSfs = {};


  my $ff = new FastaFile($file);
  $ff->open();
  while(my $seq = $ff->nextSeq()) {
    my $id = FastaFile::id($seq);
    print STDERR "Blasting on $id\n" if ($verbose);

    #instanciate blast class
    my $args = {};
    $args->{'blastBinary'} = $blastPath if ($blastPath);
    $args->{'tmpDir'} = $tmpdirPath if ($tmpdirPath);
    $args->{'numCpu'} = $numCpu if ($numCpu);
    my $blastObj = run Blast($searchAlg, $blastDb, $seq,undef,undef,$args);
    
    unless ($blastObj) {
      print STDERR "Problem with blast on $id\n";
      next;
    }

    my $bestEval;
    my $hitCnt = 0;
    my $numHitsOverZero;
    while (my $hit = $blastObj->nextHit()) {
      $hitCnt++;

      my $subject_id = $hit->id();  # the panther hmm
      my $eval = $hit->expect();
      $eval=~s/^e/1e/;

      #set first hit as best hit
      $bestEval = $eval if ($hitCnt == 1);

      #keep track of the number of hits greater than 0
      $numHitsOverZero++ if ($eval != 0);

      #depending on the best score, do the following heuristics
      if ($bestEval == 0){
        #if the best hit has an eval of zero, take the top 2 hits with eval>0
        last if ($numHitsOverZero > $nonZeroBlastHitLimit);
      } elsif ($bestEval < $smallEvalCutoff) {
        #if the best score is notzero, but really small, take the top 5 hits
        last if ($hitCnt > $smallEvalCntLimit);
      } else {
        #the top hit is a litte "weak"
        #end if evalue exceeds cutoff of 0.1
        last if ($eval > $blastEvalCutoff);

        #end if > 5 hits, and have gone far enough in terms of eval drop
        my $evalDrop = $bestEval/$eval;
        last if (($hitCnt>$smallEvalCntLimit) && ($evalDrop<$evalDropCutoff));
      }
      my ($famId,$sfId) = split(/:/,$subject_id);
      #always score against the famly
      $selectedFams->{$famId}{$id}=1;

      if ($sfId) {
        #if hit subfamily, score this seq against the subfamily also
        $selectedSfs->{$famId}{$sfId}{$id}=1;
      }
    }
  }
  $ff->close();
  return ($selectedFams,$selectedSfs);
}

sub writeTmpFileViaIndex {
  my $tmpFile = shift;
  my $seqIds = shift;
  my $indexHash = shift;
  my $fh = shift;

  open(TMP, ">$tmpFile") || die "Cannot create $tmpFile\n";
  foreach my $id (@$seqIds) {
    my $seq = &getSeq($fh,$indexHash,$id); 
    if ($seq) {
      print TMP $seq,"\n";
    } else {
      print STDERR "Missing sequence for $id\n";
    }
  }
  close TMP;
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
  if ($blastPath) {
    print STDERR "User defined path to blastall: $blastPath\n";
  }
  if ($hmmsearchPath) {
     print STDERR "User defined path to hmmsearch: $hmmsearchPath\n";
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

pantherHmmScoreBlast.pl - a script to score protein sequences against the
PANTHER HMM library. First, uses blast to prefilter results and determine 
which HMMs to score against 


score_hmm - a program score HMMs in a library against a DB

Usage:
    pantherScore.pl -l <PANTHER library> -D B -V 
                    -i <fasta file> -o <output file>


Where args are:
\t-h for help (this message)
\t-l PANTER (l)ibrary with HMMs
\t-D display type for results
\t\toptions: I (interproscan), B (best hit), A (all hits)
\t-E user defined hmm (E)value cutoff to over ride default
\t\tdefault: depends on display type; determined by PANTHER group
\t-B user defined path to (B)last binary
\t\tdefault: blastall binary in \$PATH
\t-H user defined path to (H)mmsearch binary
\t\tdefault: hmmsearch binary in \$PATH
\t-z user defined path to gun(z)ip path
\t\tdefault: gunzip binary in \$PATH
\t-n to display family and subfamily names in the output
\t-T user defined path for tmp directory
\t\tdefault: /usr/tmp/
\t-P P=1 to for (P)roduction format of lib; P=0 for build version
\t\tdefault: 1
\t-J J=1 to for (J)oin alignments in output; J=0 to leave unjoined
\tif J=1, if gaps between alignment ranges are <= 15 positions, then the 
\talignment ranges are joined
\t\tdefault: 1
\t-t for entry (t)ype
\t\tdefault: nr
\t-n to display family and subfamily names in the output
\t-c for number of (c)pu on machine to use for blast/hmmsearch
\t\tdefault: 1
\t-i (i)nput fasta file to score
\t-o (o)utput file (redirect STDOUT)
\t-e (e)rror file (redirect STDERR)
\t-v (v)erbose (debug info to STDERR)
\t-V (V)ery verbose (debug info to STDERR)
__EOT

  print "Error: $error\n\n" if ($error);

  exit(-1);
}
