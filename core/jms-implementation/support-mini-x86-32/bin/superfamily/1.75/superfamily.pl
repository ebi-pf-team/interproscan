#!/usr/bin/perl -w
# superfamily.pl
# http://supfam.org
# Copyright (c) 2001 MRC and Julian Gough; see http://supfam.org/SUPERFAMILY/license.html
# David Morais 28.09.10

use strict;

my $usage="USAGE:\nsuperfamily.pl <genome.fa>\n\noptionally\nnohup superfamily.pl <genome.fa> & \n";

unless (@ARGV==1)
{
print $usage;	
	
}   
$ARGV[0]=~/^(\w+)\.fa$/;
my $file=$1;

print "Running fasta checker\n";
system "fasta_checker.pl $ARGV[0] >scratch/$file\_torun.fa";


print "Running hmmscan\n";
system "hmmscan.pl -o scratch/$file.res -E 10 -Z 15438 hmmlib scratch/$file\_torun.fa --hmmscan hmmscan --threads 4 --tempdir scratch ";


print "Running assingments\n";
system "ass3.pl -t n -f 4 -e 0.01 scratch/$file\_torun.fa scratch/$file.res $file.ass ";


print "Running ass_to_html\n";
system "ass_to_html.pl dir.des.scop.txt model.tab $file.ass > $file.html";

