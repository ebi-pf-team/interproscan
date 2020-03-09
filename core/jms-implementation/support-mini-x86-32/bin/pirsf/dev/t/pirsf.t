use strict;
use warnings;
use Test::More;
use Test::Exception;
use Test::Warnings;
use Test::Differences;

use FindBin '$Bin';
use lib "$Bin/../";

use Smart::Comments;


use_ok 'PIRSF';



my $dat_file = "$Bin/../../../../data/pirsf/3.02/pirsf.dat";

my ($pirsf_data, $children) = PIRSF::read_pirsf_dat($dat_file);
## $pirsf_data
## $children
my $pirsf_count = scalar keys %{$pirsf_data};
## $pirsf_count

is(scalar keys %{$pirsf_data} , 4,  "4 PIRSF ids found on pirsf_dat");

my $example_pirsf_dat->{'PIRSF500175'} = $pirsf_data->{'PIRSF500175'};

my $expected_ex_pirsf_dat = {
    blast => 0,
    meanL => '427.5',
    meanS => '570.346666666667',
    minS => '442.9',
    name => 'Glutamyl-tRNA(Gln) amidotransferase, subunit D',
    stdL => '16.8926631697177',
    stdS => '46.7185242248408'
};

is_deeply($example_pirsf_dat->{'PIRSF500175'}, $expected_ex_pirsf_dat, "Random PIRSF id data is correct");
is($pirsf_data->{'PIRSF500175'}->{'children'}, undef, "No children for PIRSF500175");

my $expected_children = {
    PIRSF500175 => 1,
    PIRSF500176 => 1
};

is_deeply($pirsf_data->{'PIRSF001220'}->{'children'}, $expected_children, "Correct children for PIRSF001220");



my $georgetown_testing = `for file in data/test/*
                          do
                            perl pirsf.2017.pl "\$file"
                          done`;

## $georgetown_testing





my $input = "$Bin/data/test.fasta";
# my $dominput = "data/P10751.hmmscan.domtblout.out";
my $sf_hmm = "$Bin/../../../../data/pirsf/3.02/sf_hmm_all";
my $matches;
my $hmmer_path = "$Bin/../../../hmmer/hmmer3/3.1b1";
# my $cpus = 1;
my $mode = "hmmscan";
my $i5_tmpdir = '/tmp';



my $pirsf_bin = "$Bin/../";

my $test_prot_cmd = "perl ${pirsf_bin}/pirsf.pl --fasta $input -path $hmmer_path --hmmlib $sf_hmm -dat $dat_file --mode hmmscan --outfmt pirsf";

my $test_prot_pirsf = qx/$test_prot_cmd/;



eq_or_diff($test_prot_pirsf, $georgetown_testing, "results of test.fasta match those of georgetown pirsf.2017.pl");




# my $single_prot_i5_cmd = "perl ${pirsf_bin}/pirsf.pl --fasta data/P10751.fasta --domtbl data/P10751.hmmscan.domtblout.out -path bin/hmmer3.1b1 --hmmlib data/pirsf_data/sf_hmm_all -dat data/pirsf_data/pirsf.dat --tmpdir . --mode hmmscan --outfmt i5 -cpu 1";
# my $single_prot_pirsf_cmd = "perl ${pirsf_bin}/pirsf.pl --fasta data/P10751.fasta --domtbl data/P10751.hmmscan.domtblout.out -path bin/hmmer3.1b1 --hmmlib data/pirsf_data/sf_hmm_all -dat data/pirsf_data/pirsf.dat --tmpdir . --mode hmmscan --outfmt pirsf -cpu 1";

# my $single_prot_i5 = qx/$single_prot_i5_cmd/;
# my $single_prot_pirsf = qx/$single_prot_pirsf_cmd/;

# is($single_prot_i5, $P10751_i5_expected, "expected i5 output for P10751");
# is($single_prot_pirsf, $P10751_pirsf_expected, "expected pirsf output for P10751");



# my $selected_prot_i5_cmd = "perl ${pirsf_bin}/pirsf.pl --fasta data/selected.fasta --domtbl data/selected.hmmscan.domtblout.out -path bin/hmmer3.1b1 --hmmlib data/pirsf_data/sf_hmm_all -dat data/pirsf_data/pirsf.dat --tmpdir . --mode hmmscan --outfmt i5 -cpu 1";
# ## $selected_prot_i5_cmd

# my $selected_prot_i5 = qx/$selected_prot_i5_cmd/;
# ## $selected_prot_i5


# is(scalar split('\n', $selected_prot_i5), 251,  "251 lines in output for the selected prots list");
# cannot test actual data, protein order is random
# is($selected_prot_i5, $selected_i5_expected, "and all match the expected output");







# my $matches_found = PIRSF::run_hmmer($input, $dominput, $sf_hmm, $pirsf_data, $matches, $children, $hmmer_path, $cpus, $mode, $i5_tmpdir);
# is($matches_found, 1, "Matches found for protein P10751");



# my $P10751_i5_expected = <<P10751_i5;
# 84	2	PIRSF037162	sp|P10751|ZFP11_MOUSE	3.3e-193	..	84	9	24.8	641.2	4.5	3.5e-09	1.7e-06	84	2	0.76	186.0
# 331	59	PIRSF037162	sp|P10751|ZFP11_MOUSE	3.3e-193	..	331	59	206.3	641.2	43.8	3.9e-64	1.8e-61	331	59	0.99	186.0
# 415	143	PIRSF037162	sp|P10751|ZFP11_MOUSE	3.3e-193	..	415	143	199.4	641.2	42.5	4.5e-62	2.1e-59	415	143	0.99	186.0
# 444	309	PIRSF037162	sp|P10751|ZFP11_MOUSE	3.3e-193	..	443	309	95.5	641.2	17.1	1.4e-30	6.8e-28	444	309	0.99	186.0
# 647	393	PIRSF037162	sp|P10751|ZFP11_MOUSE	3.3e-193	..	644	393	173.0	641.2	46.2	4.8e-54	2.3e-51	647	393	0.98	186.0
# P10751_i5

# my $P10751_pirsf_expected = <<P10751_pirsf;
# Query Sequence: sp|P10751|ZFP11_MOUSE matches PIRSF037162: PR-domain zinc finger protein PRDM5
#    #    score  bias  c-Evalue  i-Evalue hmmfrom  hmm to    alifrom  ali to    envfrom  env to     acc
#    1 !   24.8   4.5   3.5e-09   1.7e-06     454     535 ..       9      84 ..       2      84 .. 0.76
#    2 !  206.3  43.8   3.9e-64   1.8e-61     257     530 ..      59     331 ..      59     331 .. 0.99
#    3 !  199.4  42.5   4.5e-62   2.1e-59     257     530 ..     143     415 ..     143     415 .. 0.99
#    4 !   95.5  17.1   1.4e-30   6.8e-28     396     530 ..     309     443 ..     309     444 .. 0.99
#    5 !  173.0  46.2   4.8e-54   2.3e-51     255     507 ..     393     644 ..     393     647 .] 0.98
# P10751_pirsf

done_testing();
