#!/usr/bin/env perl

use strict;
use warnings;
use Getopt::Long;
use FindBin qw($Bin);
use lib "$Bin";
use PIRSF;

use Smart::Comments;

#------------------------------------------------------------------------------
# Deal with all of the options handling.

# Both the family and subfamily hmm library combined.
my $sf_hmm = 'sf_hmm_all';
# PIRSF data file
my $pirsf_dat = 'pirsf.dat';
# Other options with default values
my $mode = 'hmmscan';
my $verbose = 0;
my $output  = 'pirsf';
my $cpus = 1;
my $tmpdir = '/tmp';
# Other options with no default value
my $input;
my $hmmer_path;
my $dominput;

GetOptions(
  'help'     => sub { pod2usage( -verbose => 1 ) },
  'man'      => sub { pod2usage( -verbose => 2 ) },
  'fasta=s'  => \$input,
  'domtbl=s' => \$dominput,
  'hmmlib=s' => \$sf_hmm,
  'verbose'  => \$verbose,
  'mode=s'   => \$mode,
  'data=s'   => \$pirsf_dat,
  'outfmt=s' => \$output,
  'path=s'   => \$hmmer_path,
  'cpus=i'   => \$cpus,
  'tmpdir=s' => \$tmpdir,
) or pod2usage(2);

if (!$input) {
    print STDERR "\n *** FATAL, no input fasta file defined ***\n\n";
    pod2usage(2);
    exit;
}

#------------------------------------------------------------------------------
# Main body

# Check that the hmm files have been properly pressed
PIRSF::checkHmmFiles($sf_hmm, $hmmer_path);

# Read the PIRSF data file.
my ($pirsf_data, $children) = PIRSF::read_pirsf_dat($pirsf_dat);

# If no dominput provided, generate it
if (!$dominput) {
  $dominput = PIRSF::run_hmmer($hmmer_path, $mode, $cpus, $sf_hmm, $input, $tmpdir);
}

# Parse dominput into results array
my $results = PIRSF::read_dom_input($dominput);

# Now run the search.
my $matches = PIRSF::process_results($results, $pirsf_data, $children, $mode);

# Now determine the best matches and subfamily matches.
my $best_matches = PIRSF::post_process($matches, $pirsf_data);

# get the no matches for output printing
if ($verbose) {
  my $target_hash = PIRSF::read_fasta($input);
  foreach my $seq_id (keys %{$target_hash}) {
    if (!exists $best_matches->{$seq_id}) {
      $best_matches->{$seq_id} = $target_hash->{$seq_id};
    }
  }
}

# ASCII bestMatchesoutput - but we will want to directly load into the database.
PIRSF::print_output($best_matches, $pirsf_data, $output);

1;

__END__



=head1 NAME

pirsf.pl - Interpro version of PIR SF scan program for one sequence in fasta format

=head1 SYNOPSIS

  pirsf.pl -fasta myseqs.fa [OPTIONS]

  -fasta  <filename>          : Input fasta that you want to analyse, required.
  -hmmlib <filename>          : The PIRSF HMM library, containing both family and subfamily profiles, default sf_hmm_all .
  -dat    <filename>          : The PIRSF data file, listing family and subfamily metadata, default pirsf.dat.
  -mode   <hmmscan|hmmsearch> : [Experimental] Switch from hmmscan mode to hmmsearch.
  -verbose                    : Report No matches, default off.
  -outfmt <pirsf|i5>          : Print output in different formats. Default pirsf.
  -path                       : Path to HMMER binaries.
  -cpu    <#>                 : Number of cpus to using for hmmscan. Default 1
  -tmpdir                     : Directory for hmmer to use. Default temporary ./tmp/
  -domtbl                     : domtblout from hmmer run (if provided will skip using hmmer).
  -help                       : Prints this message.

=head1 DESCRIPTION



=head1 DEPENDENCIES

hmmer

=cut