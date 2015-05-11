#!/usr/bin/perl
# a2m2selex.pl
# http://supfam.org
# Copyright (c) 2001 MRC and Julian Gough; see http://supfam.org/SUPERFAMILY/license.html
#This program is supposed to: convert from a2m to selex format
#specifying model architecture for use by HMMER using the
# '--hand' option
#Julian Gough 14.6.01

#ASIGN-VARIABLES-------------------------------------

my $prettyalign = "prettyalign";    #specify the path to the SAM binary here
my $a2mfile;
my $usage;
my %sequences;
my $name;
my $blank = 0;
my $i;
my @temp;

#----------------------------------------------------

#ARGUEMENTS-------------------------------------------
$usage = "a2m2selex.pl  <a2mfile>";
die "Usage: $usage\n" unless ( @ARGV > 0 );
$a2mfile = $ARGV[0];

#-----------------------------------------------------

#CONVERSION-------------------------------------------
open PRETTY, ("$prettyalign $a2mfile |");
while (<PRETTY>) {
    if (/^;(.*)$/) {
        $blank = 1;
        print "#$1\n";
        next;
    }
    unless (/\S/) {
        $blank = 1;
        print $_;
        next;
    }
    elsif ( $blank == 1 and /^(\S+\s+)(\S.*)$/ ) {
        print "#=RF";
        for $i ( 1 .. ( length($1) - 4 ) ) {
            print " ";
        }
        @temp = split //, $2;
        foreach $i (@temp) {
            if ( $i =~ /[A-Z]/ or $i =~ /\-/ ) {
                print 'x';
            }
            else {
                print '.';
            }
        }
        print "\n", $_;
        $blank = 0;
    }
    elsif ( /^\s\s\s\s(\s+\d[\s\d]*)$/ or /^\s\s\s\s(\s+\|[\s\|]*)$/ ) {
        print "#   $1";
    }
    else {
        print $_;
    }
}
close PRETTY;

#-----------------------------------------------------
