#!/usr/bin/perl -w
use strict;
use Thread;
use Thread::Semaphore;
  
# ass3.pl
# http://supfam.org
# Copyright (c) 2010 Julian Gough; see http://supfam.org/SUPERFAMILY/license.html
#This program is supposed to: Create SUPERFAMILY assignments from the output of HMMER3 hmmscan
#Julian Gough 8.7.2010

#Files-----------------------------------------------
my $selfhits = "./self_hits.tab";
my $clafile      = "./dir.cla.scop.txt";
my $modeltab     = "./model.tab";
my $pdbj95d      = "./pdbj95d";
#----------------------------------------------------

#ASIGN-VARIABLES-------------------------------------
my $usage =
"ass3.pl [-arg argument ..] <database (fasta file)> <scores (ouput from hmmscan)> <outputfile>\n[-e Evaluecutoff]    -e 0.0001 is conservative default\n[-v y/n] print progress dots? y\n[-h y/n] calculate family level? y\n[-s filename] self_hits.tab, default working dir\n[-r filename] dir.cla.scop.txt file, default working dir\n[-m filename] model.tab file, default working dir\n[-p filename] pdbj95d file, default working dir\n[-t y/n] test input files? default is yes\n[-b Sequences per block] sequence block size per thread\n[-f Processors] number of parrallel threads to run\n\n\nN.B. This is written to work with HMMER3: hmmscan\n";

my $density       = 6;
my $insertdensity = 25;
my $percentsame = 0.35;
my $coresize = 15;
my $verbose;
my $scanfile;
my $database;
my $threads;
my $flvl;
my $outputfile :shared;
my $cutoff;
my $threadsize;
my $done=0;
my $seq;
my $seqid;
my $n_sfs;
my $totmods;
my $i;
my $ii;
my $it;
my $line;
my ($query, $model, $flag2, $domain, $l);
my @threadlist;
my $test;
my %modelhits :shared;
my %mod;
my %blosum;
my %pxlength;
my %px;
my %length;
my %familymembers;
my ( %fm, %sf, %pxsf, %pxfa, %pdb, %sf_size );
my @lines;
my @batch :shared;
my $last;
my $flag=-1;
my $semaphore = Thread::Semaphore->new(0);
my %pickup;
#----------------------------------------------------

#ARGS------------------------------------------------
die "Usage: $usage\n" unless ( @ARGV > 2 );
my %args = &Args();
unless ( defined( $cutoff = $args{"e"} ) ) {
  $cutoff = 0.0001;
}
unless ( defined( $verbose = $args{"v"} ) ) {
  $verbose = 'y';
}
unless ( defined( $flvl = $args{"h"} ) ) {
  $flvl = 'y';
}
unless ( defined( $threads = $args{"f"} ) ) {
  $threads = 1;
}
unless ( defined( $threadsize = $args{"b"} ) ) {
  $threadsize = 100;
}
if ( defined( $args{"s"} ) ) { $selfhits     = $args{"s"} }
if ( defined( $args{"r"} ) ) { $clafile      = $args{"r"} }
if ( defined( $args{"m"} ) ) { $modeltab     = $args{"m"} }
if ( defined( $args{"p"} ) ) { $pdbj95d      = $args{"p"} }

$database   = $args{"arg1"};
$scanfile   = $args{"arg2"};
$outputfile = $args{"arg3"};
die "Usage: $usage\n"  unless ( defined($database) and defined($scanfile) and defined($outputfile) );
unless ( defined( $test = $args{"t"} ) ) {
  $test = 'y';
}
print STDERR
  "\nRunning ass3.pl....\n\nhmmscan results file: $scanfile\noutputfile: $outputfile\nEvalue cut-off:  $cutoff\nSequences per block: $threadsize\nNo. of threads: $threads\nPrint progress dots  y/n:   $verbose\n\n";

#----------------------------------------------------

#TESTING-FILES---------------------------------------
unless ($test =~ /[Nn]/){
  unless ( -e $clafile ) {
    print
      "SCOP classification file not found. Looking for it here: $clafile\n";
    print
      "It can be downloaded from the SCOP website at this location: http://scop.mrc-lmb.cam.ac.uk/scop/parse/index.html\n";
    print
      "The file you are looking for has a filename of the form: dir.cla.scop.txt_1.75\n";
    exit;
  }
  unless ( -e $selfhits ) {
    print
      "SUPERFAMILY self_hits.tab file not found. Looking for it here: $selfhits\n";
    print
      "It can be downloaded from the SUPERFAMILY ftp site at this location using your login details which were supplied when the license was applied for online: /models/self_hits.tab.gz\n";
    exit;
  }
  unless ( -e $modeltab ) {
    print
      "SUPERFAMILY model.tab file not found. Looking for it here: $modeltab\n";
    print
      "It can be downloaded from the SUPERFAMILY ftp site at this location using your login details which were supplied when the license was applied for online: /models/model.tab\n";
    exit;
  }
  unless ( -e $pdbj95d ) {
    print
      "SUPERFAMILY pdbj95d file not found. Looking for it here: $pdbj95d\n";
    print
      "It can be downloaded from the SUPERFAMILY ftp site at this location using your login details which were supplied when the license was applied for online.\n";
    exit;
  }
  $test = 0;
  open CLA, ("$clafile");
  while (<CLA>) {
    unless (/^#/) {
      if (/^(\S+)\s.*fa=(\d+),.*px=(\d+)/) {
	$familymembers{$1} = 1;
	$familymembers{$3} = 1;
	$test++;
      }
      elsif (/\S/) {
	print STDERR
	  "WARNING: failed to parse this line from $clafile\n$_\n";
      }
    }
  }
  close CLA;
  if ( $test < 60000 ) {
    print
      "The SCOP classification file only has $test lines recognised in it. This seems like it might not be enough. Please check the file.\n";
    exit;
  }
  $test = 0;
  print STDERR "Checking self_hits.tab ...";
  open SELF, ("$selfhits");
  while (<SELF>) {
    if (/^(\d+)\t(\d+)\t\d+\t([a-zA-Z.-]+)$/) {
      unless ( exists( $familymembers{$2} ) ) {
	print
	  "The self hits file and the SCOP dir.cla.scop file do not match. Perhaps they are from different versions. Please check the versions of both files. The following SCOP 'px' identifier was found in the self-hits file but not in the SCOP classification file: $2\n";
	exit;
      }
      else {
	$test++;
      }
    }
    elsif (/\S/) {
      print STDERR
	"WARNING: failed to parse this line from $selfhits\n$_\n";
    }
  }
  close SELF;
  print STDERR "done\n";
  if ( $test < 250000 ) {
    print
      "The SUPERFAMILY self hits file only has $test lines recognised in it. This seems like it might not be enough. Please check the file.\n";
    exit;
  }
  $test = 0;
  open MODEL, ("$modeltab");
  while (<MODEL>) {
    if (/^\d+\t\d+\t\S+\t(\S+)\t/) {
      unless ( exists( $familymembers{$1} ) ) {
	print
	  "The model.tab file and the SCOP dir.cla.scop file do not match. Perhaps they are from different versions. Please check the versions of both files. The following SCOP identifier was found in the self-hits file but not in the SCOP classification file: $1\n";
	exit;
      }
      else {
	$test++;
      }
    }
    elsif (/\S/) {
      print STDERR
	"WARNING: failed to parse this line from $modeltab\n$_\n";
    }
  }
  close MODEL;
  if ( $test < 7000 ) {
    print
      "The SUPERFAMILY model.tab file only has $test lines recognised in it. This seems like it might not be enough. Please check the file.\n";
    exit;
  }
  %familymembers=();
  open PDB, ("$pdbj95d");
  while (<PDB>) {
    if (/^>(\S+)/) {
      $test--;
    }
  }
  close PDB;
  unless ($test == 0){
    print STDERR "Different number of entries in model file and pdbj95d file (differ by $test): Check versions of both.\n";
    exit;
  }
  else{
    $test=0;
  }
  open DB, ("$database");
  $seq='';
  while (<DB>){
    if (/^>(\S+)/){
      $test++;
    }
  }
  close DB;
  if ( $test < 100 ) {
    print
      "The FASTA database file only has $test sequences recognised in it. This seems like it might not be enough. Please check the file.\n";
    exit;
  }
}
#----------------------------------------------------

#READ-IN-FILES---------------------------------------
open DB, ("$database");
$seq='';
$seqid='';
while (<DB>){
  if (/^>(\S+)/){
    $length{$seqid}=length($seq);
    $seqid=$1;
    $seq='';
  }
  elsif(/(\S+)/){
    $seq=$seq.$1;chomp($seq);
  }
}
$length{$seqid}=length($seq);
$seq='';
close DB;
open MODEL, ("$modeltab");
$totmods    = 0;
$n_sfs = 0;
while (<MODEL>) {
  if (/^(\d+)\t(\d+)\t\S+\t(\S+)\t/) {
    $totmods++;
    $mod{$3} = $1;
    if ( exists( $sf_size{$2} ) ) {
      $sf_size{$2}++;
    }
    else {
      $sf_size{$2} = 1;
      $n_sfs++;
    }
  }
}
close MODEL;

unless ( -e $clafile ) {
  print STDERR
    "SCOP classification file not found. Please run script in test mode first\n";
  die;
}
open CLA, ("$clafile");
while (<CLA>) {
  if (/^(\S+)\t(\S+\t\S*).*sf=(\d+),fa=(\d+),.*px=(\d+)/) {
    if ( exists( $mod{$1} ) ) {
      $fm{"$mod{$1}"} = $4;
      $sf{"$mod{$1}"} = $3;
      $pxfa{$5}       = $4;
      $pxsf{$5}       = $3;
      $px{$1}=$5;
    }
  }
}
close CLA;
open PDB, ("$pdbj95d");
$seq='';
$seqid='';$px{$seqid}='';
while (<PDB>){
  if (/^>(\S+)/){
    $pxlength{$px{$seqid}}=length($seq);
    $seqid=$1;
    $seq='';
  }
  elsif(/(\S+)/){
    $seq=$seq.$1;chomp($seq);
  }
}
$pxlength{$px{$seqid}}=length($seq);
$seq='';
close PDB;
&Blosum62;
if ($flvl eq 'y'){
  print STDERR "Reading self_hits.tab ...";
  open SELFHITS, ("$selfhits");
  while (<SELFHITS>) {
    if (/^(\d+)\t(\d+)\t(\d+)\t(\S+)$/) {
      if (exists($modelhits{$1})){$modelhits{$1}=$modelhits{$1}.",$2:$3$4";}else{$modelhits{$1}="$2:$3$4";}
    }
  }
  close SELFHITS;
  print STDERR "done\n";
}
#----------------------------------------------------

#CHECK-WHERE-LEFT-OFF-FROM---------------------------
if ( -e $outputfile ) {
  $last='';
  print STDERR "Picking up from where I left off:\n";
  open OUT, ("$outputfile");
  while (<OUT>) {
    push @lines, $_;
    if (/^(\S+)\t\S+\t/) {
      unless ($last eq $1){
	$done++;
      }
      $last = $1;
    }
    else {
      last;
    }
  }
  close OUT;
  open OUT, (">$outputfile");
  foreach $line (@lines) {
    if ( $line =~ /^(\S+)\t\S+\t/ ) {
      if ( $last eq $1 ) {
	last;
      }
      else {
	$pickup{$1}=1;
	print OUT $line;
      }
    }
  }
  close OUT;
  @lines = ();
  print STDERR "$done sequences with hits, now continuing\n\n";
}
else {
  $last = '';
}
#----------------------------------------------------

#MULTI-THREAD----------------------------------------
{lock(@batch);
 if ($threads == 1){
   $it= new Thread \&Threading, 0;
   push @threadlist,$it;
 }
 else{
   for $i (1 .. $threads){
     $it= new Thread \&Threading, $i;
     push @threadlist,$it;
   }
 }
 #----------------------------------------------------

 #PARSE-LOOP-------------------------------------------
 print STDERR "Progress ($threadsize sequences per dot):";
 open DATA, ("$scanfile");
 while (<DATA>) {
   if (/^Query:\s+(\S+)/){
     $flag=0;
     $query=$1;
     $ii++;
   }
   elsif (/^\s+\[No\stargets\sdetected\sthat\ssat/ and ($flag == 0)){
     $flag=5;
     $flag2=1;
   }
   elsif (/^\>\>\s+(\S+)/ and ($flag == 0 or $flag == 5)){
     $seq=$1;
     $model=$1;
     $flag=1;
     $i=scalar(@lines);
   }
   elsif($flag == 1){
     if (/^\s+#\s+score\s\sbias/){
       $flag=2;
     }
     elsif($flag2 == 1 and /^\s+\[No\sindividual\sdomains/){
       $flag=5;
     }
     else{
       print STDERR "Unexpected line: $_";
     }
   }
   elsif($flag == 2){
     if(/^\s---\s\s\s------\s-----/){
       $flag=3;
     }
     else{
       print STDERR "Did not expect this line: $_";
     }
   }
   elsif($flag == 3){
     if (/\S/){
       if (/^\s+(\d+)\s\S\s+\S+\s+\S+\s+\S+\s+(\S+)\s+(\d+)\s+(\d+)\s+\S\S\s+(\d+)\s+(\d+)/){
	 $lines[($1-1+$i)]="$model\t$2\t$5\t$6\t$3\t";
       }
       else{
	 print STDERR "Failed to parse line: $_";
       }
     }
     else{
       $flag=4;
     }
   }
   elsif ($flag == 4){
     if (/^\s+Alignments\sfor\seach\sdomain/){
       $flag=5;$flag2=0;
     }
     else{
       print STDERR "Expecting a different line here: $_";
     }
   }
   elsif ($flag ==5){
     if ($flag2 == 0){
       if (/^\s+\=\=\s+domain\s(\d+)/){
	 $domain=$1;$flag2=1;
       }
       else{
	 print STDERR "This line should be different: $_";
       }
     }
     elsif ($flag2 == 1 and /\S/){
       if(/^Internal\spipeline\sstatistics/){
	 $flag=0;
	 unless (exists($pickup{$query})){
	   $last='';
	   foreach $l (@lines){push @batch, $l;}push @batch, $query;
	   if  (($ii/$threadsize) == int($ii/$threadsize)){
	     $semaphore->up();cond_wait(@batch);
	     if ($verbose eq 'y'){print STDERR ".";}
	   }
	 }
	 @lines=();
       }
       elsif (/^\s+\=\=\s+domain\s(\d+)/){
	 $domain=$1;
       }
       elsif (/^\s+\S+\s+[0-9-]+\s+\S+\s+[0-9-]+/){
	 $flag2=2;
       }
       else{
	 print STDERR "One of three possible lines not found here: $_";
       }
     }
     elsif($flag2 == 2){
       $flag2=3;
     }
     elsif($flag2 == 3){
       if (/^\s+(\S+)\s+([0-9-]+)\s+(\S+)\s+([0-9-]+)/){
	 if ($1 eq $query){
	   $flag2=4;
	   if (defined($lines[($domain-1+$i)])){$lines[($domain-1+$i)]=$lines[($domain-1+$i)].$3;}else{$lines[($domain-1+$i)]=$3;}
	 }
	 else{
	   print STDERR "@lines Query $query not matching line: $_";
	 }
       }
       else{
	 print STDERR "Important line failed parse: $_";
       }
     }
     elsif($flag2 == 4){
       $flag2=5;
     }
     elsif($flag2 == 5){
       if (/\S/){
	 print STDERR "This line should be blank: $_";
       }
       else{
	 $flag2=1;
       }
     }
   }
 }
 close DATA;
 $semaphore->up();cond_wait(@batch);
}

$semaphore->up($threads);
foreach $it (@threadlist){
  $it->join;
}
print STDERR "\nFinished assigning ($ii sequences processed)\n";
close OUT;
#----------------------------------------------------

#SUB-ROUTINE-----------------------------------------------
sub Threading{
  my @ls=();
  my $l;
  my $it=$_[0];
  my @lins=('togetstarted');
  my @output;
  while (scalar(@lins) > 0){
    {
      $semaphore->down();
      lock(@batch);
      @lins=@batch;@batch=();
      cond_signal(@batch);
    }	 
    foreach $l (@lins){
      if ($l =~ /\t/){
	push @ls, $l;
      }
      else{
	push @output,	&ProcessSequence(\@ls,$l);@ls=();
      }
    }
    {lock($outputfile);open OUT, (">>$outputfile");print OUT join "\n",@output;if (scalar(@output ) > 0){print OUT "\n";}close OUT;@output=();}
  }
}
#----------------------------------------------------

#SUB-ROUTINE-----------------------------------------------
sub ProcessSequence {
  my $pointer = $_[0];
  my @lines = @$pointer;
  my @out;
  my $query = $_[1];
  my $i = 0;
  my ($flag,$offset,$startpos,$endpos, $seq2, $l, $count);
  my (
      $topk,   $flag2,   $tempseq, $bigoverlap, 
      $overlap, $supfam, $ij,  $j, $k, $hit, $conflict, $alloverlap
     );
  my @regions;
  my @models     = ();
  my @scores     = ();
  my @regionfrom = ();
  my @regionto   = (); 
  my @modstart   = ();
  my @align      = ();
  my @match1;
  my @match2;
  
  foreach $hit (@lines) {
    if      ($hit =~ /^(\S+)\t(\S+)\t(\S+)\t(\S+)\t(\S+)\t(\S+)$/){
      $models[$i]     = $1;
      $regionfrom[$i] = $3;
      $regionto[$i]   = $4;
      $scores[$i]     = $2;
      $modstart[$i]   = $5;
      $align[$i]      = $6;
      $i++;
    }
    else{
      print STDERR "Can't parse: $hit\n";die;
    }
  }
  
  my  @order        = &OrderArray(@scores);
  my  @trues;
  my  (%donefam,%donefalse);
  my  $tempcutoff=$cutoff/($n_sfs/$totmods); #to make sure those adjusted are included
  my  @assignfrom   = ();
  my  @assignto     = ();
  my  @assignscore  = ();
  my  @assignmod    = ();
  my  @assignmodstart    = ();
  my  @assignseq    = ();
  my  @assignfammod = ();
  my  @assignfammodstart = ();
  my  @assignfamseq = ();
  my  $leftover     = $length{$query};
  
  foreach $i (@order) {
    $conflict = 0;
    if ( scalar(@assignmod) == 0 and $scores[$i] <= $tempcutoff ) {
      $assignfrom[0]                   = $regionfrom[$i];
      $assignto[0]                     = $regionto[$i];
      $assignscore[0]                  = $scores[$i];
      $assignmod[0]                    = $models[$i];
      $assignmodstart[0]               = $modstart[$i];
      $assignseq[0] = $align[$i];
      $trues[0] = 1;
      $tempseq = $assignseq[0];
      $tempseq =~ s/\-//g;
      $leftover = $leftover - ( $tempseq =~ tr/[A-Z]// );
      next;
    }
    else {
      $alloverlap = 0;
      $bigoverlap = 0;
      $topk       = '';
      $seq2 = $align[$i];
      $tempseq = $seq2;
      $tempseq =~ s/\-//g;
      @match2 = split //, $tempseq;
      $count =  ( $tempseq =~ tr/[A-Z]// );
      for $k ( 0 .. scalar(@assignmod) - 1 ) {
	unless ( $assignto[$k] < $regionfrom[$i]
		 or $assignfrom[$k] > $regionto[$i] )
	  {
	    $tempseq = $assignseq[$k];
	    $tempseq =~ s/\-//g;
	    @match1 = split //, $tempseq;
	    $overlap = 0;
	    $startpos=$regionfrom[$i]-$assignfrom[$k]; if ($startpos < 0){$startpos=0;}
	    $endpos=(scalar(@match1) - 1) - ($assignto[$k]-$regionto[$i]);if ($endpos > (scalar(@match1) - 1)){$endpos=scalar(@match1) - 1}
	    $offset=$assignfrom[$k]-$regionfrom[$i];	    
	    for $l ( $startpos .. $endpos ) {
	      if (    uc( $match2[($l+$offset)] ) eq $match2[($l+$offset)]
		      and uc( $match1[$l] ) eq $match1[$l] )
		{
		  $alloverlap++;
		  $overlap++;
		}
	    }
	    if ( $overlap > $bigoverlap ) {
	      $topk       = $k;#the one with the biggest overlap
	      $bigoverlap = $overlap;
	    }
	  }
      }
      if ( $alloverlap > $count * $percentsame ) {
	$conflict = 1;
	#--works-out-top-scoring-model-of-differemt-family-to-absolute-top-scoring-model
	#--and-how-many-true-hits-before-first-assumed-bad-for-E-value-adjustment-------
	$flag = 0;
	if ( $sf{ $models[$i] } == $sf{ $assignmod[$topk] } )
	  {
unless (exists($donefalse{$topk})){$trues[$topk]++;}
	    unless ( $fm{ $assignmod[$topk] } == $fm{ $models[$i] } or exists($donefam{$topk}))
	      {
		$assignfammod[$topk] = $models[$i];
		$assignfamseq[$topk] = $align[$i];
		$assignfammodstart[$topk] = $modstart[$i];
		$donefam{$topk}=1;
	      }
	  }
	else{
	 $donefalse{$topk}=1; 
	}
  	#-------------------------------------------------------------------------------	
	#-------------------------------------------------------------------------------	
      }
      if ( $conflict == 0 and $scores[$i] <= $tempcutoff ) {
	push @assignfrom,  $regionfrom[$i];
	push @assignto,    $regionto[$i];
	push @assignscore, $scores[$i];
	push @assignmod,   $models[$i];
	push @assignmodstart,   $modstart[$i];
	push @assignseq,   $seq2;
	push @trues, 1;
	$tempseq = $seq2;
	$tempseq =~ s/-//g;
	$leftover =
	  $leftover - ( $tempseq =~ tr/[A-Z]// ) + $alloverlap;
      }
    }
    if ( $leftover < $coresize
	 and scalar(@assignmod) == scalar(keys(%donefam)) and scalar(@assignmod) == scalar(keys(%donefalse)))
      {
	last;
      }
  }
  #------------
  for $j ( 0 .. scalar(@assignmod) - 1 ) {
    $assignscore[$j] = sprintf( "%.2e", ($assignscore[$j]  * $sf_size{ $sf{$assignmod[$j]} } * ($n_sfs/($totmods-$trues[$j]+1)) / $trues[$j]) );
    if ($assignscore[$j] <= $cutoff){
      #regions-----
      @regions =
	&Regions( $assignfrom[$j], $assignseq[$j], $density, $insertdensity );
      for $ij ( 0 .. scalar(@regions) - 2 ) {
	if ( $ij / 2 == int( $ij / 2 ) ) {
	  $regions[$ij] = $regions[$ij] . '-';
	}
	else {
	  $regions[$ij] = $regions[$ij] . ',';
	}
      }
      #-------------
      unless ( defined( $assignfammod[$j] ) ) {
	$assignfammod[$j] = '-';
      }
      unless ( defined( $assignfammodstart[$j] ) ) {
	$assignfammodstart[$j] = '-';
      }
      unless ( defined( $assignfamseq[$j] ) ) {
	$assignfamseq[$j] = '-';
      }
      $line= join '',@regions;
      $line= "$query\t$assignmod[$j]\t".$line."\t$assignscore[$j]\t$assignmodstart[$j]\t$assignseq[$j]";
      if ($flvl eq 'y'){
	$line=$line."\t";
	$line=$line	. &FamilyLevel( $assignmod[$j],  $assignmodstart[$j], $assignseq[$j], $assignfammod[$j], $assignfammodstart[$j], $assignfamseq[$j]);
      }
      push @out,$line;
    }
  }
  unless (scalar(@out) > 0){@out=("$query\t-\t-\t-\t-\t-\t-\t-\t-");}
  return (@out);
}
#----------------------------------------------------------

#SUB-ROUTINE-----------------------------------------------
sub Args {
  
  # This reads in ARGV and sets all -n  type options and sets
  # the global hash array %args
  my $arguement;
  my $type = "rumplestiltskin";
  my $i    = 0;
  my @args;
  
  foreach $arguement (@ARGV) {
    if ( $arguement =~ /^-(\S+)$/ ) {
      $type = $1;
    }
    else {
      if ( $type eq "rumplestiltskin" ) {
	$i++;
	push @args, "arg$i", $arguement;
      }
      else {
	push @args, $type, $arguement;
	$type = "rumplestiltskin";
      }
    }
  }
  return (@args);
}

#----------------------------------------------------------

#SUB-ROUTINE-----------------------------------------------
sub OrderArray {
  
  #  This reads in an array of values and orders them
  #returning the list of the order

  my @values = @_;
  my %map;
  my $i;
  my @sorted;
  my @listout;
  my @temp;
  my $flag;
  my $j   = 0;
  my $old = 'rumplestiltsin';
  
  for $i ( 0 .. scalar(@values) - 1 ) {
    if ( exists( $map{ $values[$i] } ) ) {
      $map{ $values[$i] } = join ',', $map{ $values[$i] }, $i;
    }
    else {
      $map{ $values[$i] } = $i;
    }
  }
  @sorted = sort NumericallySort @values;
  for $i ( 0 .. scalar(@sorted) - 1 ) {
    if ( $map{ $sorted[$i] } =~ /,/ ) {
      unless ( $old eq $map{ $sorted[$i] } ) {
	$j   = 0;
	$old = $map{ $sorted[$i] };
      }
      @temp = split /,/, $map{ $sorted[$i] };
      $listout[$i] = $temp[$j];
      $j++;
    }
    else {
      $j = 0;
      $listout[$i] = $map{ $sorted[$i] };
    }
  }
  return @listout;
}

#----------------------------------------------------------

#SUB-ROUTINE-----------------------------------------
sub NumericallySort { $a <=> $b; }
#----------------------------------------------------

#SUB-ROUTINE-----------------------------------------------
sub FamilyLevel {
  #  calculates the family level stuff
  #PARMS
  my $gapopen   = 3;
  my $gapextend = 0.8;
  my $K         = 0.2;
  my $lambda    = 0.12;
  my $tau       = 0.6;
  my $maxval    = 180;
  my $minval    = 1.0e-236;
  
  #-----
  my (
      $i,         $align,   $model,     $nextalign,
      $nextmodel, $score,   $nextscore, $bits,
      $eval, $px,        $fa, $modstart, $nextmodstart
     );
  my (@fams);
  
  $model     = $_[0];
  $modstart  = $_[1];
  $align     = $_[2];
  $nextmodel = $_[3];
  $nextmodstart = $_[4];
  $nextalign = $_[5];
  $score     = $maxval;
  $nextscore = $maxval;
  $px        = '-';
  $fa        = '-', $eval = '-';
  
 #HYBRID-SCORE-TOP-MODEL-------------------------------
  foreach $i (split /,/, $modelhits{$model} ) {
    $i =~/^(\d+):(\d+)(\S+)/;
    $i=$1;
    $bits = &OptScoreGapPenalty( $modstart, $align, $2, $3,
				 $pxlength{$i},
				 $gapopen, $gapextend, $lambda );
    if ( $score > $bits or $score == $maxval ) {
      if ( $fa ne $pxfa{$i} ) {
	$nextscore = $score;
      }
      $score = $bits;
      $px    = $i;
      $fa    = $pxfa{$i};
    }
    elsif ( ( $nextscore > $bits or $nextscore == $maxval )
	    and $pxfa{$i} ne $fa )
      {
	$nextscore = $bits;
      }
  }
  #-----------------------------------------------------
  
  #HYBRID-SCORE-NEXT-MODEL------------------------------
  if ( $nextmodel =~ /\d{7}/ ) {
    foreach $i (split /,/, $modelhits{$nextmodel} ) {
      $i =~/^(\d+):(\d+)(\S+)/;
      $i=$1;
      $bits = &OptScoreGapPenalty( $nextmodstart, $nextalign, $2, $3,
				   $pxlength{$i},
				   $gapopen, $gapextend, $lambda );
      if ( $score > $bits or $score == $maxval ) {
	if ( $fa ne $pxfa{$i} ) {
	  $nextscore = $score;
	}
	$score = $bits;
	$px    = $i;
	$fa    = $pxfa{$i};
      }
      elsif ( ( $nextscore > $bits or $nextscore == $maxval )
	      and $pxfa{$i} ne $fa )
	{
	  $nextscore = $bits;
	}
    }
  }
  #-----------------------------------------------------
  
  #EVALUE-----------------------------------------------
  if ( $score < $minval )     { $score     = $minval }
  if ( $nextscore < $minval ) { $nextscore = $minval }
  # Because perl can't handle raising a -ve number to a fractional
  # power with an odd denominator
  my $log_scores_diff = log($nextscore) - log($score);
  if (  $log_scores_diff < 0 ) {
    $eval = $K / ( 1 + exp( -( -$log_scores_diff )**$tau ) );
  }
  else {
    $eval = $K / ( 1 + exp( $log_scores_diff**$tau ) );
  }
  if ( $eval =~ /^(\d\.\d?\d?)\d*(e\S+)$/ ) {
    $eval = "$1$2";
  }
  elsif ( $eval =~ /^(\d+\.?0*\d\d?)\d*$/ ) {
    $eval = $1;
  }
  else {
    print STDERR "Failed to parse Evalue: $eval\n";
  }
  
  #-----------------------------------------------------
  return ("$eval\t$px\t$fa");
}
#----------------------------------------------------------

#SUB-ROUTINE-----------------------------------------------
sub OptScoreGapPenalty {
  my $s1 = $_[0];
  my $a1 = $_[1];
  my $s2 = $_[2];
  my $a2 = $_[3];
  my $n  = $_[4];
  my @align1;
  my @align2;
  my ( $i, $j );
  my $evalue;
  my ( $S, $bitscore, $optS );
  my $gapopen    = $_[5];
  my $gapextend  = $_[6];
  my $gappenalty = 0;
  my $gapstatus  = 'closed';
  my $lambda = $_[7];
  
  #add-up-raw-score-------------------
  @align1 = split //, $a1;
  @align2 = split //, $a2;
  $S      = 0;
  $optS   = 0;
  $i      = 0;
  $j      = 0;
  if ($s1 < $s2){
    $s1=($s2-$s1);
    until ($s1 == 0 or $i >= scalar(@align1)){
      if ($align1[$i] =~ /[-A-Z]/){
	$s1--;
      }
      $i++;
    }
  }
  else{
    $s2=($s1-$s2);
    until ($s2 == 0 or $j >= scalar(@align2)){
      if ($align2[$j] =~ /[-A-Z]/){
	$s2--;
      }
      $j++;
    }
  }
  if ($i < scalar(@align1) and $j < scalar(@align2)){
    until ( $i == scalar(@align1) or $j == scalar(@align2) ) {
      if (   "$align1[$i]$align2[$j]" eq "--"
	     or "$align1[$i]$align2[$j]" =~ /[a-z][a-z]/ )
	{
	  $i++;
	  $j++;
	  unless ( $gapstatus eq 'closed' or $gapstatus eq 'both' ) {
	    $gappenalty = $gappenalty - $gapopen + $gapextend;
	  }
	  $gapstatus = 'both';
	}
      else {
	unless ( "$align1[$i]$align2[$j]" =~ /[-a-z]/ ) {
	  $S          = $S + $blosum{ "$align1[$i]$align2[$j]" };
	  $S          = $S - $gappenalty;
	  $gappenalty = 0;
	  if ( $S < 0 )     { $S    = 0; }
	  if ( $S > $optS ) { $optS = $S; }
	  $gapstatus = 'closed';
	  $i++;
	  $j++;
	}
	elsif ( $gapstatus eq 'closed' ) {
	  $gappenalty = $gappenalty + $gapopen;
	  if ( $align1[$i] =~ /[a-z]/ ) {
	    $gapstatus = 2;
	    $i++;
	  }
	  elsif ( $align2[$j] =~ /[a-z]/ ) {
	    $gapstatus = 1;
	    $j++;
	  }
	  elsif ( $align1[$i] =~ /[A-Z]/ ) {
	    $gapstatus = 2;
	    $i++;
	    $j++;
	  }
	  else {
	    $gapstatus = 1;
	    $i++;
	    $j++;
	  }
	}
	else {
	  if ( $align1[$i] =~ /[a-z]/ ) {
	    if ( $gapstatus eq '1' ) {
	      $gappenalty = $gappenalty - $gapopen;
	    }
	    else {
	      $gappenalty = $gappenalty + $gapextend;
	    }
	    $gapstatus = 2;
	    $i++;
	  }
	  elsif ( $align2[$j] =~ /[a-z]/ ) {
	    if ( $gapstatus eq '2' ) {
	      $gappenalty = $gappenalty - $gapopen;
	    }
	    else {
	      $gappenalty = $gappenalty + $gapextend;
	    }
	    $gapstatus = 1;
	    $j++;
	  }
	  elsif ( $align1[$i] =~ /[A-Z]/ ) {
	    if ( $gapstatus eq '1' ) {
	      $gappenalty = $gappenalty - $gapopen;
	    }
	    else {
	      $gappenalty = $gappenalty + $gapextend;
	    }
	    $gapstatus = 2;
	    $i++;
	    $j++;
	  }
	  else {
	    if ( $gapstatus eq '2' ) {
	      $gappenalty = $gappenalty - $gapopen;
	    }
	    else {
	      $gappenalty = $gappenalty + $gapextend;
	    }
	    $gapstatus = 1;
	    $i++;
	    $j++;
	  }
	}
      }
    }
  }
  #-----------------------------------
  
  #bit-score--------------------------
  $bitscore = ( $lambda * $optS ) / log(2);
  
  #-----------------------------------
  
  #evalue-----------------------------
  $evalue = $n * 2**( -$bitscore );
  
  #-----------------------------------
  
  return ($evalue);
}
#----------------------------------------------------------

#SUB-ROUTINE-----------------------------------------------
#This Subroutine takes an alignment of upper case and lower case letters
#and returns the proper domain boundaries
#Takes as arguements the alignment, and two residue densities required for
#boundary recognition of outside and inside boundaries
sub Regions {
  my $start = $_[0];
  my $alignment = $_[1];
  $alignment =~ s/\-//g;
  $alignment =~ s/\.//g;
  my @res      = split //, $alignment;
  my $density  = $_[2];
  my $density2 = $_[3];
  my $count    = 0;
  my $r;
  my $begin;
  my $end;
  my $altbegin = -1;
  my $altend   = -1;
  my $done     = 0;
  my $status   = 'out';
  my @output;
  my $lowest = 0;
  my $low    = 0;
  my $count2 = -$density;
  
  $count = 0;
  for $r ( 0 .. scalar(@res) - 1 ) {
    if ( $res[$r] =~ /[A-Z]/ ) {
      $low++;
      if ( $count2 <= -$density ) {
	$count2 = 0;
	$begin  = $r;
      }
      unless ( $count2 == 0 ) {
	$count2++;
      }
      $end    = $r;
      $altend = $r;
      unless ( $altbegin > -1 ) {
	$altbegin = $r;
      }
      unless ( $count == $density ) {
	$count++;
      }
    }
    else {
      unless ( $count2 == -$density ) {
	$count2--;
      }
      $low--;
      unless ( $count == 0 ) {
	$count--;
      }
    }
    if ( $lowest > $low ) {
      $lowest = $low;
    }
    if ( $count == $density and $status eq 'out' ) {
      if ( scalar(@output) > 1
	   and $lowest > ( $density - $density2 - 1 ) )
	{
	  pop(@output);
	  $status = 'in';
	}
      else {
	push @output, ( $begin + $start );
	$status = 'in';
      }
      $low    = $density;
      $lowest = $density;
    }
    elsif ( $count == 0 and $status eq 'in' ) {
      push @output, ( $end + $start );
      $status = 'out';
      $low    = 0;
      $lowest = 0;
    }
  }
  if ( $status eq 'in' ) {
    push @output, ( $end + $start );
  }
  
  unless ( 2 * int( scalar(@output) / 2 ) == scalar(@output) ) {
    print STDERR "Error in defining regions\n";
  }
  unless ( scalar(@output) >= 1 ) {
    $output[0] = $altbegin + $start;
    $output[1] = $altend + $start;
  }
  return (@output);
}
#-----------------------------------------------------------

#SUB-ROUTINE-----------------------------------------------
sub Blosum62 {
    $blosum{"AA"} = 4;
    $blosum{"AR"} = -1;
    $blosum{"AN"} = -2;
    $blosum{"AD"} = -2;
    $blosum{"AC"} = 0;
    $blosum{"AQ"} = -1;
    $blosum{"AE"} = -1;
    $blosum{"AG"} = 0;
    $blosum{"AH"} = -2;
    $blosum{"AI"} = -1;
    $blosum{"AL"} = -1;
    $blosum{"AK"} = -1;
    $blosum{"AM"} = -1;
    $blosum{"AF"} = -2;
    $blosum{"AP"} = -1;
    $blosum{"AS"} = 1;
    $blosum{"AT"} = 0;
    $blosum{"AW"} = -3;
    $blosum{"AY"} = -2;
    $blosum{"AV"} = 0;
    $blosum{"AB"} = -2;
    $blosum{"AZ"} = -1;
    $blosum{"AX"} = 0;
    $blosum{"A*"} = -4;
    $blosum{"RA"} = -1;
    $blosum{"RR"} = 5;
    $blosum{"RN"} = 0;
    $blosum{"RD"} = -2;
    $blosum{"RC"} = -3;
    $blosum{"RQ"} = 1;
    $blosum{"RE"} = 0;
    $blosum{"RG"} = -2;
    $blosum{"RH"} = 0;
    $blosum{"RI"} = -3;
    $blosum{"RL"} = -2;
    $blosum{"RK"} = 2;
    $blosum{"RM"} = -1;
    $blosum{"RF"} = -3;
    $blosum{"RP"} = -2;
    $blosum{"RS"} = -1;
    $blosum{"RT"} = -1;
    $blosum{"RW"} = -3;
    $blosum{"RY"} = -2;
    $blosum{"RV"} = -3;
    $blosum{"RB"} = -1;
    $blosum{"RZ"} = 0;
    $blosum{"RX"} = -1;
    $blosum{"R*"} = -4;
    $blosum{"NA"} = -2;
    $blosum{"NR"} = 0;
    $blosum{"NN"} = 6;
    $blosum{"ND"} = 1;
    $blosum{"NC"} = -3;
    $blosum{"NQ"} = 0;
    $blosum{"NE"} = 0;
    $blosum{"NG"} = 0;
    $blosum{"NH"} = 1;
    $blosum{"NI"} = -3;
    $blosum{"NL"} = -3;
    $blosum{"NK"} = 0;
    $blosum{"NM"} = -2;
    $blosum{"NF"} = -3;
    $blosum{"NP"} = -2;
    $blosum{"NS"} = 1;
    $blosum{"NT"} = 0;
    $blosum{"NW"} = -4;
    $blosum{"NY"} = -2;
    $blosum{"NV"} = -3;
    $blosum{"NB"} = 3;
    $blosum{"NZ"} = 0;
    $blosum{"NX"} = -1;
    $blosum{"N*"} = -4;
    $blosum{"DA"} = -2;
    $blosum{"DR"} = -2;
    $blosum{"DN"} = 1;
    $blosum{"DD"} = 6;
    $blosum{"DC"} = -3;
    $blosum{"DQ"} = 0;
    $blosum{"DE"} = 2;
    $blosum{"DG"} = -1;
    $blosum{"DH"} = -1;
    $blosum{"DI"} = -3;
    $blosum{"DL"} = -4;
    $blosum{"DK"} = -1;
    $blosum{"DM"} = -3;
    $blosum{"DF"} = -3;
    $blosum{"DP"} = -1;
    $blosum{"DS"} = 0;
    $blosum{"DT"} = -1;
    $blosum{"DW"} = -4;
    $blosum{"DY"} = -3;
    $blosum{"DV"} = -3;
    $blosum{"DB"} = 4;
    $blosum{"DZ"} = 1;
    $blosum{"DX"} = -1;
    $blosum{"D*"} = -4;
    $blosum{"CA"} = 0;
    $blosum{"CR"} = -3;
    $blosum{"CN"} = -3;
    $blosum{"CD"} = -3;
    $blosum{"CC"} = 9;
    $blosum{"CQ"} = -3;
    $blosum{"CE"} = -4;
    $blosum{"CG"} = -3;
    $blosum{"CH"} = -3;
    $blosum{"CI"} = -1;
    $blosum{"CL"} = -1;
    $blosum{"CK"} = -3;
    $blosum{"CM"} = -1;
    $blosum{"CF"} = -2;
    $blosum{"CP"} = -3;
    $blosum{"CS"} = -1;
    $blosum{"CT"} = -1;
    $blosum{"CW"} = -2;
    $blosum{"CY"} = -2;
    $blosum{"CV"} = -1;
    $blosum{"CB"} = -3;
    $blosum{"CZ"} = -3;
    $blosum{"CX"} = -2;
    $blosum{"C*"} = -4;
    $blosum{"QA"} = -1;
    $blosum{"QR"} = 1;
    $blosum{"QN"} = 0;
    $blosum{"QD"} = 0;
    $blosum{"QC"} = -3;
    $blosum{"QQ"} = 5;
    $blosum{"QE"} = 2;
    $blosum{"QG"} = -2;
    $blosum{"QH"} = 0;
    $blosum{"QI"} = -3;
    $blosum{"QL"} = -2;
    $blosum{"QK"} = 1;
    $blosum{"QM"} = 0;
    $blosum{"QF"} = -3;
    $blosum{"QP"} = -1;
    $blosum{"QS"} = 0;
    $blosum{"QT"} = -1;
    $blosum{"QW"} = -2;
    $blosum{"QY"} = -1;
    $blosum{"QV"} = -2;
    $blosum{"QB"} = 0;
    $blosum{"QZ"} = 3;
    $blosum{"QX"} = -2;
    $blosum{"Q*"} = -4;
    $blosum{"EA"} = -1;
    $blosum{"ER"} = 0;
    $blosum{"EN"} = 0;
    $blosum{"ED"} = 2;
    $blosum{"EC"} = -4;
    $blosum{"EQ"} = 2;
    $blosum{"EE"} = 5;
    $blosum{"EG"} = -2;
    $blosum{"EH"} = 0;
    $blosum{"EI"} = -3;
    $blosum{"EL"} = -3;
    $blosum{"EK"} = 1;
    $blosum{"EM"} = -2;
    $blosum{"EF"} = -3;
    $blosum{"EP"} = -1;
    $blosum{"ES"} = 0;
    $blosum{"ET"} = -1;
    $blosum{"EW"} = -3;
    $blosum{"EY"} = -2;
    $blosum{"EV"} = -2;
    $blosum{"EB"} = 1;
    $blosum{"EZ"} = 4;
    $blosum{"EX"} = -1;
    $blosum{"E*"} = -4;
    $blosum{"GA"} = 0;
    $blosum{"GR"} = -2;
    $blosum{"GN"} = 0;
    $blosum{"GD"} = -1;
    $blosum{"GC"} = -3;
    $blosum{"GQ"} = -2;
    $blosum{"GE"} = -2;
    $blosum{"GG"} = 6;
    $blosum{"GH"} = -2;
    $blosum{"GI"} = -4;
    $blosum{"GL"} = -4;
    $blosum{"GK"} = -2;
    $blosum{"GM"} = -3;
    $blosum{"GF"} = -3;
    $blosum{"GP"} = -2;
    $blosum{"GS"} = 0;
    $blosum{"GT"} = -2;
    $blosum{"GW"} = -2;
    $blosum{"GY"} = -3;
    $blosum{"GV"} = -3;
    $blosum{"GB"} = -1;
    $blosum{"GZ"} = -2;
    $blosum{"GX"} = -1;
    $blosum{"G*"} = -4;
    $blosum{"HA"} = -2;
    $blosum{"HR"} = 0;
    $blosum{"HN"} = 1;
    $blosum{"HD"} = -1;
    $blosum{"HC"} = -3;
    $blosum{"HQ"} = 0;
    $blosum{"HE"} = 0;
    $blosum{"HG"} = -2;
    $blosum{"HH"} = 8;
    $blosum{"HI"} = -3;
    $blosum{"HL"} = -3;
    $blosum{"HK"} = -1;
    $blosum{"HM"} = -2;
    $blosum{"HF"} = -1;
    $blosum{"HP"} = -2;
    $blosum{"HS"} = -1;
    $blosum{"HT"} = -2;
    $blosum{"HW"} = -2;
    $blosum{"HY"} = 2;
    $blosum{"HV"} = -3;
    $blosum{"HB"} = 0;
    $blosum{"HZ"} = 0;
    $blosum{"HX"} = -1;
    $blosum{"H*"} = -4;
    $blosum{"IA"} = -1;
    $blosum{"IR"} = -3;
    $blosum{"IN"} = -3;
    $blosum{"ID"} = -3;
    $blosum{"IC"} = -1;
    $blosum{"IQ"} = -3;
    $blosum{"IE"} = -3;
    $blosum{"IG"} = -4;
    $blosum{"IH"} = -3;
    $blosum{"II"} = 4;
    $blosum{"IL"} = 2;
    $blosum{"IK"} = -3;
    $blosum{"IM"} = 1;
    $blosum{"IF"} = 0;
    $blosum{"IP"} = -3;
    $blosum{"IS"} = -2;
    $blosum{"IT"} = -1;
    $blosum{"IW"} = -3;
    $blosum{"IY"} = -1;
    $blosum{"IV"} = 3;
    $blosum{"IB"} = -3;
    $blosum{"IZ"} = -3;
    $blosum{"IX"} = -1;
    $blosum{"I*"} = -4;
    $blosum{"LA"} = -1;
    $blosum{"LR"} = -2;
    $blosum{"LN"} = -3;
    $blosum{"LD"} = -4;
    $blosum{"LC"} = -1;
    $blosum{"LQ"} = -2;
    $blosum{"LE"} = -3;
    $blosum{"LG"} = -4;
    $blosum{"LH"} = -3;
    $blosum{"LI"} = 2;
    $blosum{"LL"} = 4;
    $blosum{"LK"} = -2;
    $blosum{"LM"} = 2;
    $blosum{"LF"} = 0;
    $blosum{"LP"} = -3;
    $blosum{"LS"} = -2;
    $blosum{"LT"} = -1;
    $blosum{"LW"} = -2;
    $blosum{"LY"} = -1;
    $blosum{"LV"} = 1;
    $blosum{"LB"} = -4;
    $blosum{"LZ"} = -3;
    $blosum{"LX"} = -1;
    $blosum{"L*"} = -4;
    $blosum{"KA"} = -1;
    $blosum{"KR"} = 2;
    $blosum{"KN"} = 0;
    $blosum{"KD"} = -1;
    $blosum{"KC"} = -3;
    $blosum{"KQ"} = 1;
    $blosum{"KE"} = 1;
    $blosum{"KG"} = -2;
    $blosum{"KH"} = -1;
    $blosum{"KI"} = -3;
    $blosum{"KL"} = -2;
    $blosum{"KK"} = 5;
    $blosum{"KM"} = -1;
    $blosum{"KF"} = -3;
    $blosum{"KP"} = -1;
    $blosum{"KS"} = 0;
    $blosum{"KT"} = -1;
    $blosum{"KW"} = -3;
    $blosum{"KY"} = -2;
    $blosum{"KV"} = -2;
    $blosum{"KB"} = 0;
    $blosum{"KZ"} = 1;
    $blosum{"KX"} = -1;
    $blosum{"K*"} = -4;
    $blosum{"MA"} = -1;
    $blosum{"MR"} = -1;
    $blosum{"MN"} = -2;
    $blosum{"MD"} = -3;
    $blosum{"MC"} = -1;
    $blosum{"MQ"} = 0;
    $blosum{"ME"} = -2;
    $blosum{"MG"} = -3;
    $blosum{"MH"} = -2;
    $blosum{"MI"} = 1;
    $blosum{"ML"} = 2;
    $blosum{"MK"} = -1;
    $blosum{"MM"} = 5;
    $blosum{"MF"} = 0;
    $blosum{"MP"} = -2;
    $blosum{"MS"} = -1;
    $blosum{"MT"} = -1;
    $blosum{"MW"} = -1;
    $blosum{"MY"} = -1;
    $blosum{"MV"} = 1;
    $blosum{"MB"} = -3;
    $blosum{"MZ"} = -1;
    $blosum{"MX"} = -1;
    $blosum{"M*"} = -4;
    $blosum{"FA"} = -2;
    $blosum{"FR"} = -3;
    $blosum{"FN"} = -3;
    $blosum{"FD"} = -3;
    $blosum{"FC"} = -2;
    $blosum{"FQ"} = -3;
    $blosum{"FE"} = -3;
    $blosum{"FG"} = -3;
    $blosum{"FH"} = -1;
    $blosum{"FI"} = 0;
    $blosum{"FL"} = 0;
    $blosum{"FK"} = -3;
    $blosum{"FM"} = 0;
    $blosum{"FF"} = 6;
    $blosum{"FP"} = -4;
    $blosum{"FS"} = -2;
    $blosum{"FT"} = -2;
    $blosum{"FW"} = 1;
    $blosum{"FY"} = 3;
    $blosum{"FV"} = -1;
    $blosum{"FB"} = -3;
    $blosum{"FZ"} = -3;
    $blosum{"FX"} = -1;
    $blosum{"F*"} = -4;
    $blosum{"PA"} = -1;
    $blosum{"PR"} = -2;
    $blosum{"PN"} = -2;
    $blosum{"PD"} = -1;
    $blosum{"PC"} = -3;
    $blosum{"PQ"} = -1;
    $blosum{"PE"} = -1;
    $blosum{"PG"} = -2;
    $blosum{"PH"} = -2;
    $blosum{"PI"} = -3;
    $blosum{"PL"} = -3;
    $blosum{"PK"} = -1;
    $blosum{"PM"} = -2;
    $blosum{"PF"} = -4;
    $blosum{"PP"} = 7;
    $blosum{"PS"} = -1;
    $blosum{"PT"} = -1;
    $blosum{"PW"} = -4;
    $blosum{"PY"} = -3;
    $blosum{"PV"} = -2;
    $blosum{"PB"} = -2;
    $blosum{"PZ"} = -1;
    $blosum{"PX"} = -2;
    $blosum{"P*"} = -4;
    $blosum{"SA"} = 1;
    $blosum{"SR"} = -1;
    $blosum{"SN"} = 1;
    $blosum{"SD"} = 0;
    $blosum{"SC"} = -1;
    $blosum{"SQ"} = 0;
    $blosum{"SE"} = 0;
    $blosum{"SG"} = 0;
    $blosum{"SH"} = -1;
    $blosum{"SI"} = -2;
    $blosum{"SL"} = -2;
    $blosum{"SK"} = 0;
    $blosum{"SM"} = -1;
    $blosum{"SF"} = -2;
    $blosum{"SP"} = -1;
    $blosum{"SS"} = 4;
    $blosum{"ST"} = 1;
    $blosum{"SW"} = -3;
    $blosum{"SY"} = -2;
    $blosum{"SV"} = -2;
    $blosum{"SB"} = 0;
    $blosum{"SZ"} = 0;
    $blosum{"SX"} = 0;
    $blosum{"S*"} = -4;
    $blosum{"TA"} = 0;
    $blosum{"TR"} = -1;
    $blosum{"TN"} = 0;
    $blosum{"TD"} = -1;
    $blosum{"TC"} = -1;
    $blosum{"TQ"} = -1;
    $blosum{"TE"} = -1;
    $blosum{"TG"} = -2;
    $blosum{"TH"} = -2;
    $blosum{"TI"} = -1;
    $blosum{"TL"} = -1;
    $blosum{"TK"} = -1;
    $blosum{"TM"} = -1;
    $blosum{"TF"} = -2;
    $blosum{"TP"} = -1;
    $blosum{"TS"} = 1;
    $blosum{"TT"} = 5;
    $blosum{"TW"} = -2;
    $blosum{"TY"} = -2;
    $blosum{"TV"} = 0;
    $blosum{"TB"} = -1;
    $blosum{"TZ"} = -1;
    $blosum{"TX"} = 0;
    $blosum{"T*"} = -4;
    $blosum{"WA"} = -3;
    $blosum{"WR"} = -3;
    $blosum{"WN"} = -4;
    $blosum{"WD"} = -4;
    $blosum{"WC"} = -2;
    $blosum{"WQ"} = -2;
    $blosum{"WE"} = -3;
    $blosum{"WG"} = -2;
    $blosum{"WH"} = -2;
    $blosum{"WI"} = -3;
    $blosum{"WL"} = -2;
    $blosum{"WK"} = -3;
    $blosum{"WM"} = -1;
    $blosum{"WF"} = 1;
    $blosum{"WP"} = -4;
    $blosum{"WS"} = -3;
    $blosum{"WT"} = -2;
    $blosum{"WW"} = 11;
    $blosum{"WY"} = 2;
    $blosum{"WV"} = -3;
    $blosum{"WB"} = -4;
    $blosum{"WZ"} = -3;
    $blosum{"WX"} = -2;
    $blosum{"W*"} = -4;
    $blosum{"YA"} = -2;
    $blosum{"YR"} = -2;
    $blosum{"YN"} = -2;
    $blosum{"YD"} = -3;
    $blosum{"YC"} = -2;
    $blosum{"YQ"} = -1;
    $blosum{"YE"} = -2;
    $blosum{"YG"} = -3;
    $blosum{"YH"} = 2;
    $blosum{"YI"} = -1;
    $blosum{"YL"} = -1;
    $blosum{"YK"} = -2;
    $blosum{"YM"} = -1;
    $blosum{"YF"} = 3;
    $blosum{"YP"} = -3;
    $blosum{"YS"} = -2;
    $blosum{"YT"} = -2;
    $blosum{"YW"} = 2;
    $blosum{"YY"} = 7;
    $blosum{"YV"} = -1;
    $blosum{"YB"} = -3;
    $blosum{"YZ"} = -2;
    $blosum{"YX"} = -1;
    $blosum{"Y*"} = -4;
    $blosum{"VA"} = 0;
    $blosum{"VR"} = -3;
    $blosum{"VN"} = -3;
    $blosum{"VD"} = -3;
    $blosum{"VC"} = -1;
    $blosum{"VQ"} = -2;
    $blosum{"VE"} = -2;
    $blosum{"VG"} = -3;
    $blosum{"VH"} = -3;
    $blosum{"VI"} = 3;
    $blosum{"VL"} = 1;
    $blosum{"VK"} = -2;
    $blosum{"VM"} = 1;
    $blosum{"VF"} = -1;
    $blosum{"VP"} = -2;
    $blosum{"VS"} = -2;
    $blosum{"VT"} = 0;
    $blosum{"VW"} = -3;
    $blosum{"VY"} = -1;
    $blosum{"VV"} = 4;
    $blosum{"VB"} = -3;
    $blosum{"VZ"} = -2;
    $blosum{"VX"} = -1;
    $blosum{"V*"} = -4;
    $blosum{"BA"} = -2;
    $blosum{"BR"} = -1;
    $blosum{"BN"} = 3;
    $blosum{"BD"} = 4;
    $blosum{"BC"} = -3;
    $blosum{"BQ"} = 0;
    $blosum{"BE"} = 1;
    $blosum{"BG"} = -1;
    $blosum{"BH"} = 0;
    $blosum{"BI"} = -3;
    $blosum{"BL"} = -4;
    $blosum{"BK"} = 0;
    $blosum{"BM"} = -3;
    $blosum{"BF"} = -3;
    $blosum{"BP"} = -2;
    $blosum{"BS"} = 0;
    $blosum{"BT"} = -1;
    $blosum{"BW"} = -4;
    $blosum{"BY"} = -3;
    $blosum{"BV"} = -3;
    $blosum{"BB"} = 4;
    $blosum{"BZ"} = 1;
    $blosum{"BX"} = -1;
    $blosum{"B*"} = -4;
    $blosum{"ZA"} = -1;
    $blosum{"ZR"} = 0;
    $blosum{"ZN"} = 0;
    $blosum{"ZD"} = 1;
    $blosum{"ZC"} = -3;
    $blosum{"ZQ"} = 3;
    $blosum{"ZE"} = 4;
    $blosum{"ZG"} = -2;
    $blosum{"ZH"} = 0;
    $blosum{"ZI"} = -3;
    $blosum{"ZL"} = -3;
    $blosum{"ZK"} = 1;
    $blosum{"ZM"} = -1;
    $blosum{"ZF"} = -3;
    $blosum{"ZP"} = -1;
    $blosum{"ZS"} = 0;
    $blosum{"ZT"} = -1;
    $blosum{"ZW"} = -3;
    $blosum{"ZY"} = -2;
    $blosum{"ZV"} = -2;
    $blosum{"ZB"} = 1;
    $blosum{"ZZ"} = 4;
    $blosum{"ZX"} = -1;
    $blosum{"Z*"} = -4;
    $blosum{"XA"} = 0;
    $blosum{"XR"} = -1;
    $blosum{"XN"} = -1;
    $blosum{"XD"} = -1;
    $blosum{"XC"} = -2;
    $blosum{"XQ"} = -1;
    $blosum{"XE"} = -1;
    $blosum{"XG"} = -1;
    $blosum{"XH"} = -1;
    $blosum{"XI"} = -1;
    $blosum{"XL"} = -1;
    $blosum{"XK"} = -1;
    $blosum{"XM"} = -1;
    $blosum{"XF"} = -1;
    $blosum{"XP"} = -2;
    $blosum{"XS"} = 0;
    $blosum{"XT"} = 0;
    $blosum{"XW"} = -2;
    $blosum{"XY"} = -1;
    $blosum{"XV"} = -1;
    $blosum{"XB"} = -1;
    $blosum{"XZ"} = -1;
    $blosum{"XX"} = -1;
    $blosum{"X*"} = -4;
#treated as Cysteine, but really is Selenocysteine
    $blosum{"UA"} = 0;
    $blosum{"UR"} = -3;
    $blosum{"UN"} = -3;
    $blosum{"UD"} = -3;
    $blosum{"UC"} = 9;
    $blosum{"UQ"} = -3;
    $blosum{"UE"} = -4;
    $blosum{"UG"} = -3;
    $blosum{"UH"} = -3;
    $blosum{"UI"} = -1;
    $blosum{"UL"} = -1;
    $blosum{"UK"} = -3;
    $blosum{"UM"} = -1;
    $blosum{"UF"} = -2;
    $blosum{"UP"} = -3;
    $blosum{"US"} = -1;
    $blosum{"UT"} = -1;
    $blosum{"UW"} = -2;
    $blosum{"UY"} = -2;
    $blosum{"UV"} = -1;
    $blosum{"UB"} = -3;
    $blosum{"UZ"} = -3;
    $blosum{"UX"} = -2;
    $blosum{"UU"} = -2;
    $blosum{"U*"} = -4;

    $blosum{"AU"} = 0;
    $blosum{"RU"} = -3;
    $blosum{"NU"} = -3;
    $blosum{"DU"} = -3;
    $blosum{"CU"} = 9;
    $blosum{"QU"} = -3;
    $blosum{"EU"} = -4;
    $blosum{"GU"} = -3;
    $blosum{"HU"} = -3;
    $blosum{"IU"} = -1;
    $blosum{"LU"} = -1;
    $blosum{"KU"} = -3;
    $blosum{"MU"} = -1;
    $blosum{"FU"} = -2;
    $blosum{"PU"} = -3;
    $blosum{"SU"} = -1;
    $blosum{"TU"} = -1;
    $blosum{"WU"} = -2;
    $blosum{"YU"} = -2;
    $blosum{"VU"} = -1;
    $blosum{"BU"} = -3;
    $blosum{"ZU"} = -3;
    $blosum{"XU"} = -2;
#treated as Lysine but really is Pyrrolysine
    $blosum{"OA"} = -1;
    $blosum{"OR"} = 2;
    $blosum{"ON"} = 0;
    $blosum{"OD"} = -1;
    $blosum{"OC"} = -3;
    $blosum{"OQ"} = 1;
    $blosum{"OE"} = 1;
    $blosum{"OG"} = -2;
    $blosum{"OH"} = -1;
    $blosum{"OI"} = -3;
    $blosum{"OL"} = -2;
    $blosum{"OK"} = 5;
    $blosum{"OM"} = -1;
    $blosum{"OF"} = -3;
    $blosum{"OP"} = -1;
    $blosum{"OS"} = 0;
    $blosum{"OT"} = -1;
    $blosum{"OW"} = -3;
    $blosum{"OY"} = -2;
    $blosum{"OV"} = -2;
    $blosum{"OB"} = 0;
    $blosum{"OZ"} = 1;
    $blosum{"OX"} = -1;
    $blosum{"OU"} = -1;
    $blosum{"O*"} = -4;
    $blosum{"AO"} = -1;
    $blosum{"RO"} = 2;
    $blosum{"NO"} = 0;
    $blosum{"DO"} = -1;
    $blosum{"CO"} = -3;
    $blosum{"QO"} = 1;
    $blosum{"EO"} = 1;
    $blosum{"GO"} = -2;
    $blosum{"HO"} = -1;
    $blosum{"IO"} = -3;
    $blosum{"LO"} = -2;
    $blosum{"KO"} = 5;
    $blosum{"MO"} = -1;
    $blosum{"FO"} = -3;
    $blosum{"PO"} = -1;
    $blosum{"SO"} = 0;
    $blosum{"TO"} = -1;
    $blosum{"WO"} = -3;
    $blosum{"YO"} = -2;
    $blosum{"VO"} = -2;
    $blosum{"BO"} = 0;
    $blosum{"ZO"} = 1;
    $blosum{"XO"} = -1;
    $blosum{"UO"} = -1;
    $blosum{"*O"} = -4;
    $blosum{"OO"} = 5;
#Treated as Isoleucine but actually is Isoleucine/Leucine except LJ and JL where it is average
    $blosum{"JA"} = -1;
    $blosum{"JR"} = -3;
    $blosum{"JN"} = -3;
    $blosum{"JD"} = -3;
    $blosum{"JC"} = -1;
    $blosum{"JQ"} = -3;
    $blosum{"JE"} = -3;
    $blosum{"JG"} = -4;
    $blosum{"JH"} = -3;
    $blosum{"JJ"} = 4;
    $blosum{"JL"} = 3;
    $blosum{"JI"} = 3;
    $blosum{"JK"} = -3;
    $blosum{"JO"} = -3;
    $blosum{"JM"} = 1;
    $blosum{"JF"} = 0;
    $blosum{"JP"} = -3;
    $blosum{"JS"} = -2;
    $blosum{"JT"} = -1;
    $blosum{"JW"} = -3;
    $blosum{"JY"} = -1;
    $blosum{"JV"} = 3;
    $blosum{"JB"} = -3;
    $blosum{"JZ"} = -3;
    $blosum{"JX"} = -1;
    $blosum{"JU"} = -1;
    $blosum{"J*"} = -4;
#Treated as Leucine but actually is Isoleucine/Leucine except LJ and JL where it is average
    $blosum{"AJ"} = -1;
    $blosum{"RJ"} = -2;
    $blosum{"NJ"} = -3;
    $blosum{"DJ"} = -4;
    $blosum{"CJ"} = -1;
    $blosum{"QJ"} = -2;
    $blosum{"EJ"} = -3;
    $blosum{"GJ"} = -4;
    $blosum{"HJ"} = -3;
    $blosum{"IJ"} = 3;
    $blosum{"LJ"} = 3;
    $blosum{"KJ"} = -2;
    $blosum{"OJ"} = -2;
    $blosum{"MJ"} = 2;
    $blosum{"FJ"} = 0;
    $blosum{"PJ"} = -3;
    $blosum{"SJ"} = -2;
    $blosum{"TJ"} = -1;
    $blosum{"WJ"} = -2;
    $blosum{"YJ"} = -1;
    $blosum{"VJ"} = 1;
    $blosum{"BJ"} = -4;
    $blosum{"ZJ"} = -3;
    $blosum{"XJ"} = -1;
    $blosum{"UJ"} = -1;
    $blosum{"*J"} = -4;

    $blosum{"*A"} = -4;
    $blosum{"*R"} = -4;
    $blosum{"*N"} = -4;
    $blosum{"*D"} = -4;
    $blosum{"*C"} = -4;
    $blosum{"*Q"} = -4;
    $blosum{"*E"} = -4;
    $blosum{"*G"} = -4;
    $blosum{"*H"} = -4;
    $blosum{"*I"} = -4;
    $blosum{"*L"} = -4;
    $blosum{"*K"} = -4;
    $blosum{"*M"} = -4;
    $blosum{"*F"} = -4;
    $blosum{"*P"} = -4;
    $blosum{"*S"} = -4;
    $blosum{"*T"} = -4;
    $blosum{"*W"} = -4;
    $blosum{"*Y"} = -4;
    $blosum{"*V"} = -4;
    $blosum{"*B"} = -4;
    $blosum{"*Z"} = -4;
    $blosum{"*X"} = -4;
    $blosum{"*U"} = -4;
    $blosum{"**"} = 1;
}
#----------------------------------------------------------
