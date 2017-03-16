#!/usr/bin/env perl

use strict;
use warnings;
use lib;
use Getopt::Long;
use FindBin qw($Bin);

#Our PIRSF module sits in the same directory as the executable.
#Add the path to the library path.
BEGIN {
  lib->import($Bin);
}

use PIRSF;

#------------------------------------------------------------------------------
#Deal with all of the options handling.

my($sf_hmm, $pirsf_dat, $input, $mode, $help, $verbose, $output, $hmmer_path, 
$cpus);

#Both the family and subfamily hmm library combined. 
$sf_hmm="sf_hmm_all";
#PIRSF data file
$pirsf_dat="pirsf.dat";
$mode = "hmmscan";
$verbose = 0;
$output  = 'pirsf';
$hmmer_path = '';
$cpus = '';

GetOptions(
  "h"        => \$help,
  "fasta=s"  => \$input,
  "hmmlib=s" => \$sf_hmm,
  "verbose"  => \$verbose,
  "mode=s"   => \$mode,
  "dat=s"    => \$pirsf_dat,
  "outfmt=s" => \$output,
  "path=s"   => \$hmmer_path,
  "cpus=i"   => \$cpus,
) or die("Error in command line arguments, run $0 -h\n");

help() if($help);

if(!$input){
  print "\n *** FATAL, no input fasta file defined ***\n\n";
  $help = 1;
}

help() if($help);

#sf.tb file
my $sftb="sf.tb";
#sf.seq file (should run formatdb first)
my $sfseq="sf.seq";

#------------------------------------------------------------------------------
#Main body

#Check that the hmm files have been properly pressed
PIRSF::checkHmmFiles($sf_hmm, $hmmer_path);

#Read the PIRSF data file.
my ($pirsf_data, $children) = PIRSF::read_pirsf_dat($pirsf_dat);
#Store all of the results here.
my $matches = {};

#This gets a list of sequences to be searched. No need
#to parse the length, as that is in the hmmer output.
PIRSF::read_fasta($input, $matches) if($verbose);

#Now run the search.
PIRSF::run_hmmer($input, $sf_hmm, $pirsf_data, $matches, $children, $hmmer_path, $cpus, $mode);

#Now determine the best matches and subfamily matches.
my $bestMatches = PIRSF::post_process($matches, $pirsf_data);

#ASCII output - but we will want to directly load ingto the database.
PIRSF::print_output($bestMatches, $pirsf_data, $output);

exit;

#------------------------------------------------------------------------------


sub help {

  print<<EOF;

usage: $0 -fasta myseqs.fa

Options - 
  -fasta  <filename>          : Input fasta that you want to analyse, required.
  -hmmlib <filename>          : The PIRSF HMM library, containing both family and subfamily profiles, default sf_hmm_all .
  -dat    <filename>          : The PIRSF data file, listing family and subfamily metadata, default pirsf.dat.
  -mode   <hmmscan|hmmsearch> : [Experimental] Switch from hmmscan mode to hmmsearch.
  -verbose                    : Report No matches, default off.
  -outfmt <pirsf|i5>          : Print output in different formats. Default prisf.
  -path                       : Path to HMMER binaries.
  -cpu    <#>                 : Number of cpus to using for hmmscan.
  -help                       : Prints this message.

EOF
exit;
}

