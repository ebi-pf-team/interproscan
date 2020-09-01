#!/usr/bin/env perl

use strict;
use warnings;
use Getopt::Long;
use Pod::Usage;

use Smart::Comments;

use JSON;
use FindBin qw($Bin);
use lib "$Bin";

use PIRSR;


my $data_folder;

my $preprocess = 0;

my $hmmscan = 'hmmscan';
my $cpus = 1;
my $hmmalign = 'hmmalign';

my $query_file;
my $out_file;

my $verbose = 0;

GetOptions(

  'help'        => sub { pod2usage( -verbose => 1 ) },
  'man'         => sub { pod2usage( -verbose => 2 ) },
  'verbose'     => \$verbose,
  'data=s'      => \$data_folder,
  'preprocess'  => \$preprocess,
  'hmmalign=s'  => \$hmmalign,
  'hmmscan=s'   => \$hmmscan,
  'cpus=i'      => \$cpus,
  'query=s'     => \$query_file,
  'out=s'       => \$out_file,

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
    hmmscan     => $hmmscan,
    cpus        => $cpus,
    verbose     => $verbose
);

if ($verbose) {
    print "PIRSR initiated...\n";
}


# Do preprocessing if required
if ($preprocess) {
    print "Preprocessing data...\n" if $verbose;
    my $process_result = $pirsr->process_data();

    if ($process_result) {
        print "Preprocessing completed successfully...\n" if $verbose;
    } else {
        print "Preprocessing failed...\n" if $verbose;
        exit;
    }
}


# Process query sequences if required
if ($query_file) {
    print "Querying sequences in '$query_file'...\n" if $verbose;
    my $search_output = $pirsr->run_query($query_file);

    my $json_out = to_json( $search_output, { pretty => 1 } );
    my $out_fh;

    print "Outputting results " if $verbose;
    if ($out_file) {
        print "to $out_file...\n" if $verbose;
        open ($out_fh, '>', $out_file) or die "Failed top open $out_file file: $!\n";
        select($out_fh);
    } else{
        print "to STDOUT...\n" if $verbose;
    }

    # Output the results data
    print $json_out;

    if ($out_file) {
        close($out_fh) or die "Failed to close $out_file file: $!\n";
        select STDOUT;
    }
}


print "Done...\n" if $verbose;




1;

__END__



=head1 NAME

pirsr.pl - PIRSR scan program

=head1 SYNOPSIS

  pirsr.pl -data SR-InterPro-2020_08/data/ [OPTIONS]

  -help                   : Prints brief help message.
  -man                    : Prints full documentation.
  -verbose                : Report warnings to STDOUT, default true.
  -data <folder>          : Folder with PIRSF data to use or preprocess, required.
  -preprocess             : If set will do the data processing and persist it, default assumes data has been preprocessed previously.
  -query <file>           : FASTA file with sequences to analyse with PIRSR.
  -out <file>             : File name to write the JSON results from query data, default STDOUT.
  -hmmalign               : Path to hmmalign binaries, default system wide install.
  -hmmscan                : Path to hmmscan binaries, default system wide install.
  -cpus <#>               : Number of cpus to using for hmmscan, default 1.
  -verbose                : Report warnings to STDOUT, default true.
  -help                   : Prints brief help message.
  -man                    : Prints full documentation.

=head1 DESCRIPTION

PIRSR processes PIR Site Rules data and reports matches for the query fasta sequence file.

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