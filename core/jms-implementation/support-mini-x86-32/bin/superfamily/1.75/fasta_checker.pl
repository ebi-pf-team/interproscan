#!/usr/bin/perl -w
use strict;
# fasta_checker.pl
# http://supfam.org
# Copyright (c) 2001 MRC and Julian Gough; see http://supfam.org/SUPERFAMILY/license.html
#This program is supposed to: Check the sanity of a fasta sequence file, outputting the
#corrected sequences to standard out,  and reporting to standard error.
#Julian Gough 22.1.01

#ASIGN-VARIABLES-------------------------------------

my $input;
my $usage;
my $line=0;
my %ids;
my $comment;
my $flag=0;
my $idflag=0;
my $thisline;
my $verbose;
my $one;
my $two;
my $seq;
my $err;

#----------------------------------------------------


#ARGUEMENTS-------------------------------------------
$usage="check_fasta.pl  <file.fa> <verbosity #>";
die "Usage: $usage\n" unless (@ARGV > 0);

$input=$ARGV[0];
$verbose=$ARGV[1];
unless (defined($verbose)){
$verbose=0;
}
#-----------------------------------------------------


#CHECK-IT---------------------------------------------
open FILE,("$input");
while (<FILE>){
$line++;
  if (/^>/){
#Is there any sequence for the ID?
if($flag == 0 and $line > 1){
print STDERR "No Sequence,  line:",($line-1),"\n";
if ($verbose == 1){
print STDERR $_;
}
}
      if (/^>\s+(\S+)$/ or /^>\s+(\S+)(\s.*)$/){
print STDERR "Space before identifier,  line:$line\n";
if ($verbose == 1){
print STDERR $_;
}
if (defined($2)){
$thisline=">$1$2\n";
}
else{
$thisline=">$1\n";
}
}
else{
$thisline=$_;
}
    if ($thisline =~ /^(>\S+)$/ or $thisline =~ /^(>\S+)(\s.*)$/){
#Is ID unique?
      if (exists($ids{"$1"})){
print STDERR "ID $1 not unique,  line:$line\n";
if ($verbose == 1){
print STDERR $_;
}
$idflag=1;
      }
else{
$idflag=0;
$ids{"$1"}=1;
#Does the ID end in a comma?
$one =$1;
$two=$2;
if ($one =~ /^(\S+)\,$/){
$comment = "$1$two\n";
print STDERR "ID $one ends in a comma,  line:$line\n";
}
else{
$comment = $thisline;
}
$flag=0;
}
    }
#Identifier present?
    else{
print STDERR "No identifier,  line:$line\n";
if ($verbose == 1){
print STDERR $_;
}
    }
  }
elsif ($idflag == 0){
  if (/\S/){
$seq=$_;
$err = $seq =~ s/\s+//g;
if ($verbose == 1 and $err > 0){
print STDERR "Line:$line contains $err blank characters\n";
}
if ($seq =~ /^(.*)\*$/){
$seq=$1;
print STDERR "Line:$line ends in '*' character. Removed (stop codon)\n";
}
$err = $seq =~ s/[^a-zA-Z]/X/g;
#$err = $err + $seq =~  s/[oj]/x/g;
#$err = $err + $seq =~ s/[OJ]/X/g;
if ($err > 0){
print STDERR "Line:$line contains $err non-amino acid characters\n";
if ($verbose == 1){
print STDERR $_;
}
}
#Is there any sequence for the ID?
if (length($seq) > 0){
if($flag == 0){
$flag=1;
print $comment;
}
print $seq,"\n";
}
  }
#Blank line?
elsif($verbose == 1){
print STDERR "Blank line,  line:$line\n";
}
}
}
close FILE;
#-----------------------------------------------------
