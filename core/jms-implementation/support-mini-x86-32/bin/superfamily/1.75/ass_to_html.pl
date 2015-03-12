#!/usr/bin/perl  
# ass_to_html.pl
# http://supfam.org
# Copyright (c) 2001 MRC and Julian Gough; see http://supfam.org/SUPERFAMILY/license.html
# David Morais 11.11.10
use strict;
use CGI; 


my @array;
my $q = CGI->new; 
my %des;
my %sf;
print $q->header, 
$q->start_html('SUPERFAMILY Assignment Results'), 
$q->h1('SUPERFAMILY Assignment Results'),
$q->p('For full description of the columns visit the <a href=http://supfam.org/SUPERFAMILY/howto_use_models.html#Output_format>SUPERFAMILY website</a>'); 

open (DES,"$ARGV[0]"); #dir.des.scop.txt
while (<DES>){
if  (/^(\d+)\s+(\w+)\s+\S+\s+\S+\s+(\S.*)/){
$des{$1}=$3;

}
}
close DES;
 
open (MOD, "$ARGV[1]") || die "could not open $ARGV[1], $!\n"; # model.tab
while (<MOD>){
if (/^(\d+)\s+(\d+)/){
$sf{$1}=$2;
}
}
close MOD;


print $q->table({-border=>1});

print "<tr><th>Seq_ID</th><th>Match_region</th><th>Superfamily E_value</th><th>SCOP superfamily</th><th>Family E_value</th><th>SCOP family</th><th>Closest structure</th><th>Alignment</th></tr>";

open (IN,"$ARGV[2]") || die " could not open $ARGV[2], $!\n"; # .ass file
while (<IN>)
{
	chomp;
	@array=split;
	my $sequence= Split_Seq ($array[5]);
	$array[2]=~s/,/\<br\>/g;	
	print 	"<tr><th>$array[0]</th><th>$array[2]</th><th>$array[3]</th><th><a href=\"http://supfam.org/SUPERFAMILY/cgi-bin/scop.cgi?sunid=$sf{$array[1]}\">$des{$sf{$array[1]}}</a></th><th>$array[6]</th><th><a href=\"http://supfam.org/SUPERFAMILY/cgi-bin/scop.cgi?sunid=$array[8]\">$des{$array[8]}</a></th><th><a href=\"http://supfam.org/SUPERFAMILY/cgi-bin/scop.cgi?sunid=$array[7]\">$des{$array[7]}</a></th><th><pre>$sequence</pre></th></tr>";
	
	
}


print $q->end_table;
print $q->end_html;


sub Split_Seq
{
my $seq = shift;
my $tam=length($seq);
my $off = 0;
my $string;
	while ($off <=$tam)
	{
	
	$string .= substr($seq,$off,50) . "<br>";
	$off += 50;	
	
	}

	
	return $string;
	
	
}
