#!/usr/bin/env perl

use strict;
use warnings;
use Getopt::Long;
use Pod::Usage;

use JSON;
use FindBin qw($Bin);
use lib "$Bin";

use PIRSR;


my $data_folder;
my $skipNCter = 0;
my $hmmalign = 'hmmalign';
my $verbose = 0;

GetOptions(

  'help'        => sub { pod2usage( -verbose => 1 ) },
  'man'         => sub { pod2usage( -verbose => 2 ) },
  'verbose'     => \$verbose,
  'data=s'      => \$data_folder,
  'hmmalign=s'  => \$hmmalign,
  'skipNCter'   => \$skipNCter,

) or pod2usage(2);


# set the default hmm_folder
if (!$data_folder) {
    print "\n *** data path is mandatory ***\n\n";
    pod2usage(2);
    exit;
}

# Start PIRSR
my $pirsr = PIRSR->new(
    data_folder => $data_folder,
    hmmalign    => $hmmalign,
    skipNCter   => $skipNCter,
    verbose     => $verbose
);

if ($verbose) {
    print "PIRSR initiated...\n";
}



print "Preprocessing data...\n" if $verbose;
my $process_result = $pirsr->process_data();

if ($process_result) {
    print "Preprocessing completed successfully...\n" if $verbose;
} else {
    print "Preprocessing failed...\n" if $verbose;
    exit;
}


print "Done...\n" if $verbose;




1;

__END__



=head1 NAME

pirsr-preprocess.pl - PIRSR data preprocessor program

=head1 SYNOPSIS

  pirsr-preprocess.pl -data SR-InterPro-2020_08/data/ [-skipNCter] [OPTIONS]

  -help                   : Prints brief help message.
  -man                    : Prints full documentation.
  -verbose                : Report warnings to STDOUT, default true.
  -data <folder>          : Folder with PIRSF data to preprocess, required.
  -skipNCter              : If set will ignore preprocessing of rules with a start/end position of Nter/Cter, default false.
  -hmmalign               : Path to hmmalign binaries, default system wide install.


=head1 DESCRIPTION

PIRSR processes PIR Site Rules data and reports matches for the query fasta sequence file.

This is the preprocessor part only of the complete PIRSR scan program.

PIR Site Rule is a series of HMMs and rules to match sites, manually created based on template sequence.
For a sequence to hit a PIRSR it needs to hit a HMM and conform to the crafted residue site rules for that HMM.
The runner script is B<pirsr.pl> and it uses the B<PIRSR.pm> package.
To get help you can run C<perl pirsr.pl -man>.

Protein Information Resource provides regular data updates for PIRSR. Those can be found at L<https://proteininformationresource.org/ura/pirsr/files_for_ebi/srhmm_for_interpro/>.
Data comes in a tarball with the name SR-InterPro-YYYY-MM.tar.gz and updates are released roughly monthly.
Inside the tarball there is a data/ folder, this is the folder that will be required for processing and building of the PIRSR system.






=head1 DEPENDENCIES

hmmer

=cut