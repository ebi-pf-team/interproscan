#!/usr/bin/perl -w
use strict;
use Thread;
use Thread::Semaphore;
  $| = 1;

# hmmscan.pl
# http://supfam.org
# Copyright (c) 2010 Julian Gough; see http://supfam.org/SUPERFAMILY/license.html
#This program is supposed to: run hmmscan using sequence threading (more efficient the native HMMER3 threading)
#Julian Gough 29.7.2010

#ASIGN-VARIABLES-------------------------------------
my $usage =
"hmmscan.pl  [-options] <hmm database> <FASTA seqfile>\n\nwhere options include all hmmscan options, plus these extra:\n  --threads  <n>  : number of sequence level threads\n  --blocksize <n>  : number of sequences to run per thread, default 100\n  --hmmscan <path> : location of hmmscan binary\n  --tempdir <path> : location of directory to be used for temporary FASTA files\n\n-h  print hmmscan options\n";

my $threads=1;
my $outputfile :shared;
my $blocksize=100;
my $semaphore = Thread::Semaphore->new(0);
my $tempdir      = "/tmp";
my $hmmscan     = "hmmscan";
my ($hmmlib,$seqfile,$it,$i,$tempfile,$tblout,$domtblout);
my (@args,@threadlist);
my @batch :shared;
#----------------------------------------------------

#ARGS------------------------------------------------
die "Usage: $usage\n" unless ( @ARGV >= 2 );
my %args = &Args();
$outputfile    = 'rumplestiltskin';

if ( defined( $args{"--cpu"} ) ) { delete( $args{"--cpu"});}
if ( defined( $args{"--tempdir"} ) ) { $tempdir     = $args{"--tempdir"}; delete( $args{"--tempdir"});}
if ( defined( $args{"--threads"} ) ) { $threads      = $args{"--threads"}; delete( $args{"--threads"}); }
if ( defined( $args{"--blocksize"} ) ) { $blocksize   = $args{"--blocksize"}; delete( $args{"--blocksize"}); }
if ( defined( $args{"--hmmscan"} ) ) { $hmmscan      = $args{"--hmmscan"}; delete( $args{"--hmmscan"}); }
if ( defined( $args{"-o"})) {$outputfile    = $args{"-o"}; delete( $args{"-o"}); 
			     if (-e $outputfile){system ("/bin/rm $outputfile");}
}
if ( defined( $args{"--tblout"})) {$tblout     = $args{"--tblout"};}
if ( defined( $args{"--domtblout"})) {$tblout     = $args{"--domtblout"};}

$hmmlib    = $args{"arg1"}; delete $args{"arg1"};
$seqfile   = $args{"arg2"}; delete $args{"arg2"};
$tempfile  = "$tempdir/hmmscan".int(rand(99999999999));

die "Usage: $usage\n"  unless ( defined($hmmlib) and defined($seqfile) );
#----------------------------------------------------

#MULTI-THREAD----------------------------------------
{lock(@batch);
 for $i (1 .. $threads){
   $it= new Thread \&Threading, $i;
   push @threadlist,$it;
 }
#----------------------------------------------------
 
#FASTA-LOOP------------------------------------------
 $i=0;
 open FASTA, ("$seqfile");
 while (<FASTA>) {
   if (/^>/){
     if  ($i > 0 and ($i/$blocksize) == int($i/$blocksize)){
       $semaphore->up();cond_wait(@batch);
     }
     $i++;
   }
   push @batch, $_;
 }
 close FASTA;
 $semaphore->up();cond_wait(@batch);
}

$semaphore->up($threads);
foreach $it (@threadlist){
  $it->join;
  if ( defined( $args{"--tblout"})) {system ("/bin/cat $tempfile\_tblout.$it >> $tblout ; /bin/rm $tempfile\_tblout.$it"); }
  if ( defined( $args{"--domtblout"})) {system ("/bin/cat $tempfile\_domtblout.$it >> $tblout ; /bin/rm $tempfile\_domtblout.$it"); }
}
#----------------------------------------------------

#SUB-ROUTINE-----------------------------------------------
sub Threading{
  my $it=$_[0];
  my @lines=('togetstarted');
  my @output;
  my $scanargs;
  
  if ( defined( $args{"--tblout"})) { $args{"--tblout"}      = "$tempfile\_tblout.$it" ;}
  if ( defined( $args{"--domtblout"})) { $args{"--domtblout"}      = "$tempfile\_domtblout.$it" ;}
  $tempfile  = "$tempfile\_thread\_$it.fa";
  
  while (scalar(@lines) > 0){
    {
      $semaphore->down();
      lock(@batch);
      @lines=@batch;@batch=();
      cond_signal(@batch);
    }	 
    if (scalar(@lines) > 0){
      open TEMP,(">$tempfile");
      print TEMP @lines;
      close TEMP;
      $scanargs=join ' ',%args;
      @output=();
      system ("$hmmscan --cpu 1 $scanargs $hmmlib $tempfile > $tempfile.res");
      system ("/bin/rm $tempfile");
      {
	lock($outputfile);
	unless ($outputfile eq 'rumplestiltskin'){
	system ("/bin/cat $tempfile.res >> $outputfile");
	}
	else{
	system ("/bin/cat $tempfile.res");
	}
      }
      system ("/bin/rm $tempfile.res");
    }
  }
}
#----------------------------------------------------

#SUB-ROUTINE-----------------------------------------------
sub Args {
  
  # This reads in ARGV and sets all -n  type options and sets
  # the global hash array %args
  my $arguement;
  my $type = "rumplestiltskin";
  my $i    = 0;
  my @args;
  
  foreach $arguement (@ARGV) {
    if ( $arguement =~ /^(-\S+)$/ ) {
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
