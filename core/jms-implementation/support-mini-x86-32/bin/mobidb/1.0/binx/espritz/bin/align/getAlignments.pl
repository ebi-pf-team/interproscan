#!/usr/bin/perl

#use File::Basename;

# Author: Ian Walsh
# Date: 20 June 2011
#
# A simple script to get psi-blast based multiple sequence alignments. 

# user variables: 
# (1) $blastdir: the location of the blast binaries
# (2) $bigdir: Location of blast 
# (3) $database: The name of the sequence database used to build alignments. 


#blast directories
my $blastdir = "/local/kim/protein/bin/blast/bin";
my $bigdir = "/import/space-srv1/db/blastdb/";

#database
my $database = "$bigdir/nr90filt";


# protein file name (FASTA)
if ($#ARGV < 0) {
	print "usage: runit fasta_file \n";
	exit;
}

my $fname = shift @ARGV;
print "Working file $fname \n";


# Read the input data and replace "B-->C" and "OJUZ --> X"
open (fi, "<$fname");
my @text = <fi>;
close fi;
my $name = shift @text;
$name =~s/>//g;
$name =~s/\s//g;
my $seq;
while(@text){
	$seq  .= shift @text;
	chomp  $seq;
}
$seq =~ y/BOJUZ/CXXXX/;

if (length($name)>20) {$name = substr($name,0,20);$name.="\n";}

open(fi,">$fname.fasta");
print fi "\> $name\n";
print fi "$seq\n";
close fi;

#$JUNK .= " $fname.fasta";
print "$seq \n";

# We blast the protein

system("$blastdir/blastpgp -i $fname.fasta -o $fname.tmp -C $fname.chk -F T -b 3000 -j 2 -e 0.001 -h 1e-10 -d $database");
system("$blastdir/blastpgp -i $fname.fasta -R $fname.chk -o $fname.blastpgp -F T -b 3000 -j 1 -e 0.001 -h 1e-10 -d $database");


$JUNK .= " $fname.blastpgp";

##########################################################################
# Extract multiple sequence alignments from Blast output with "." for gap
##########################################################################

system("./bin/blastredo $fname.blastpgp $fname.flatblast $fname.fasta");

$JUNK .= " $fname.flatblast.app";



##########################################################################
# Remove junk
##########################################################################

system("rm $JUNK");


