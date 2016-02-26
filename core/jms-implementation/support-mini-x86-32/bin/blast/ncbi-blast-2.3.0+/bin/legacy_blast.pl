#! /usr/bin/perl -w
# $Id: legacy_blast.pl 195935 2010-06-28 20:32:08Z camacho $
# ===========================================================================
#
#                            PUBLIC DOMAIN NOTICE
#               National Center for Biotechnology Information
#
#  This software/database is a "United States Government Work" under the
#  terms of the United States Copyright Act.  It was written as part of
#  the author's official duties as a United States Government employee and
#  thus cannot be copyrighted.  This software/database is freely available
#  to the public for use. The National Library of Medicine and the U.S.
#  Government have not placed any restriction on its use or reproduction.
#
#  Although all reasonable efforts have been taken to ensure the accuracy
#  and reliability of the software and data, the NLM and the U.S.
#  Government do not and cannot warrant the performance or results that
#  may be obtained by using this software or data. The NLM and the U.S.
#  Government disclaim all warranties, express or implied, including
#  warranties of performance, merchantability or fitness for any particular
#  purpose.
#
#  Please cite the author in any work or product based on this material.
#
# ===========================================================================
#
# Author:  Christiam Camacho
#
# File Description:
#   Script to convert NCBI C toolkit command line program and arguments into 
#   NCBI C++ toolkit command line program and arguments for the BLAST suite of
#   programs
#
# ===========================================================================

use strict;
use warnings;
use Getopt::Long qw(:config no_ignore_case bundling no_auto_abbrev);
use Pod::Usage;

use constant DEBUG => 0;
# Default PATH where binaries will be found
use constant DEFAULT_PATH => "/usr/bin";

pod2usage({-exitval => 1, -verbose => 2}) if (@ARGV == 0);

my $application = shift;
my $print_only = "0"; # Determines whether script prints or runs the command
# This array will contain file names to delete that are created with bl2seq's
# -A option
my @files2delete;

my $cmd;
if ($application eq "blastall") {
    $cmd = &handle_blastall(\$print_only);
} elsif ($application eq "megablast") {
    $cmd = &handle_megablast(\$print_only);
} elsif ($application eq "blastpgp") {
    $cmd = &handle_blastpgp(\$print_only);
} elsif ($application eq "bl2seq") {
    $cmd = &handle_bl2seq(\$print_only);
} elsif ($application eq "rpsblast") {
    $cmd = &handle_rpsblast(\$print_only);
} elsif ($application eq "fastacmd") {
    $cmd = &handle_fastacmd(\$print_only);
} elsif ($application eq "formatdb") {
    $cmd = &handle_formatdb(\$print_only);
} elsif ($application eq "seedtop") {
    $cmd = &handle_seedtop(\$print_only);
} elsif ($application =~ /version/) {
    my $revision = '$Revision: 195935 $';
    $revision =~ s/\$Revision: | \$//g;
    print "$0 version $revision\n";
    goto CLEAN_UP;
} elsif ($application =~ /help/) {
    pod2usage({-exitval => 1, -verbose => 2});
} else {
    die "Application: '$application' is not supported\n";
}

if ($print_only) {
    print "$cmd\n";
} else {
    print STDERR "$cmd\n" if (DEBUG);
    my $rv = system($cmd);
    unless ($rv == 0) {
        die "Program failed, try executing the command manually.\n"; 
    }
}

CLEAN_UP:
unlink foreach (@files2delete);

# Only add quotation marks in case there are spaces in the database argument
sub create_db_argument($)
{
    my $arg = shift;
    my $retval = "-db ";
    $retval .= ( ($arg =~ /\s/) ? "\"$arg\" " : "$arg ");
    return $retval;
}

# Converts floating point numbers to integers
sub convert_float_to_int($)
{
    my $float_arg = shift;
    my $retval = 0;
    if ($float_arg =~ /(\d+)e([+-])(\d+)/) {
        $retval = $1;
        if ($2 eq "+") {
            $retval *= 10**$3
        } else {
            $retval /= 10**$3
        }
    } else {
        $retval = int($float_arg);
    }
    return $retval;
}

# Add the .exe extension for binaries if necessary on windows
sub add_exe_extension()
{
    return ($^O =~ /mswin|cygwin/i) ? ".exe " : " ";
}

sub convert_sequence_locations($$)
{
    my $arg = shift;
    my $target = shift;
    my $retval;
    if (defined $arg) {
        if ($target eq "query") {
            $retval .= "-query_loc ";
        } elsif ($target eq "range") {
            $retval .= "-range ";
        } else {
            $retval .= "-subject_loc ";
        }
        my @fields = split(/[ ;,]/, $arg);
        $retval .= "$fields[0]-$fields[1] ";
    }
    return $retval;
}

sub convert_filter_string($$)
{
    my $filter_string = shift;
    my $program = shift;

    #print STDERR "Parsing '$filter_string'\n";

    if ($filter_string =~ /F/) {
        if ($program eq "blastp" or $program eq "tblastn" or 
            $program eq "blastx" or $program eq "tblastx") {
            return "-seg no ";
        } else {
            return "-dust no ";
        }
    }

    my $retval = "";
    if ($filter_string =~ /S (\d+) (\S+) (\S+)/) {
        $retval .= "-seg '$1 $2 $3' ";
    }
    if ($filter_string =~ /D (\d+) (\d+) (\d+)/) {
        $retval .= "-dust '$1 $2 $3' ";
    }
    if ($filter_string =~ /R -d (\S+)/) {
        $retval .= "-filtering_db $1 ";
    } elsif ($filter_string =~ /R\s*;/) {
        $retval .= "-filtering_db repeat/repeat_9606 ";
    }

    if ($filter_string =~ /L|T|S|D/ and not ($retval =~ /seg|dust/)) {
        if ($program eq "blastp" or $program eq "tblastn" or 
            $program eq "blastx") {
            $retval .= "-seg yes ";
        } else {
            $retval .= "-dust yes ";
        }
    }

    if ($filter_string =~ /m/) {
        $retval .= "-soft_masking true ";
    }
    #print STDERR "returning '$retval'\n";
    return $retval;
}

sub convert_strand($)
{
    my $old_strand_arg = shift;
    my $retval = "-strand ";
    if ($old_strand_arg == 1) {
        $retval .= "plus ";
    } elsif ($old_strand_arg == 2) {
        $retval .= "minus ";
    } else {
        $retval .= "both ";
    }
    return $retval;
}

# Handle the conversion from blastall arguments to the corresponding C++
# binaries
sub handle_blastall($)
{
    my $print_only = shift;
    my $path = DEFAULT_PATH;
    my ($opt_A, $opt_B, $opt_C, $opt_D, $opt_E, $opt_F, $opt_G, $opt_I, $opt_J, 
        $opt_K, $opt_L, $opt_M, $opt_O, $opt_P, $opt_Q, $opt_R, $opt_S, $opt_T, 
        $opt_U, $opt_V, $opt_W, $opt_X, $opt_Y, $opt_Z, $opt_a, $opt_b, $opt_d, 
        $opt_e, $opt_f, $opt_g, $opt_i, $opt_l, $opt_m, $opt_n, $opt_o, $opt_p, 
        $opt_q, $opt_r, $opt_s, $opt_t, $opt_v, $opt_w, $opt_y, $opt_z);

    GetOptions("<>"             => sub { $application = shift; },
               "print_only!"    => $print_only,
               "path=s"         => \$path,
               "A=i"            => \$opt_A,
               "B=i"            => \$opt_B, # not handled, not applicable
               "C=s"            => \$opt_C,
               "D=i"            => \$opt_D,
               "E=i"            => \$opt_E,
               "F=s"            => \$opt_F,
               "G=i"            => \$opt_G,
               "I:s"            => \$opt_I,
               "J:s"            => \$opt_J,
               "K=i"            => \$opt_K,
               "L=s"            => \$opt_L,
               "M=s"            => \$opt_M,
               "O=s"            => \$opt_O,
               "P=i"            => \$opt_P,
               "Q=i"            => \$opt_Q,
               "R=s"            => \$opt_R,
               "S=i"            => \$opt_S,
               "T:s"            => \$opt_T,
               "U:s"            => \$opt_U,
               "V:s"            => \$opt_V, # not handled, not applicable
               "W=i"            => \$opt_W,
               "X=i"            => \$opt_X,
               "Y=f"            => \$opt_Y,
               "Z=i"            => \$opt_Z,
               "a=i"            => \$opt_a,
               "b=i"            => \$opt_b,
               "d=s"            => \$opt_d,
               "e=f"            => \$opt_e,
               "f=i"            => \$opt_f,
               "g:s"            => \$opt_g,
               "i=s"            => \$opt_i,
               "l=s"            => \$opt_l,
               "m=i"            => \$opt_m,
               "n:s"            => \$opt_n,
               "o=s"            => \$opt_o,
               "p=s"            => \$opt_p,
               "q=i"            => \$opt_q,
               "r=i"            => \$opt_r,
               "s:s"            => \$opt_s,
               "t=i"            => \$opt_t,
               "v=i"            => \$opt_v,
               "w=i"            => \$opt_w,
               "y=f"            => \$opt_y,
               "z=f"            => \$opt_z,
               );

    unless (defined $opt_p) {
        die "-p must be provided\n";
    }

    my $retval = $path;
    if (defined $opt_p) {
        if (defined $opt_R) {
            $retval .= "/tblastn";
            $retval .= &add_exe_extension();
            $retval .= "-in_pssm $opt_R ";
        } elsif (defined $opt_n and $opt_n =~ /t/i) {
            $retval .= "/blastn";
            $retval .= &add_exe_extension();
            $retval .= "-task megablast ";
        } else {
            $retval .= "/$opt_p";
            $retval .= &add_exe_extension();
            $retval .= "-task blastn " if ($opt_p eq "blastn");
        }
    }
    $retval .= &create_db_argument($opt_d)  if (defined $opt_d);
    unless (($retval =~ /\/tblastn/) and defined $opt_R) {
        $retval .= "-query $opt_i "         if (defined $opt_i);
    }
    $retval .= "-gilist $opt_l "            if (defined $opt_l);
    $retval .= "-dbsize $opt_z "            if (defined $opt_z);
    $retval .= "-matrix $opt_M "            if (defined $opt_M);
    $retval .= "-evalue $opt_e "            if (defined $opt_e);
    $retval .= "-gapopen $opt_G "           if (defined $opt_G);
    $retval .= "-gapextend $opt_E "         if (defined $opt_E);
    $retval .= "-xdrop_ungap $opt_y "       if (defined $opt_y);
    $retval .= "-xdrop_gap $opt_X "         if (defined $opt_X);
    $retval .= "-xdrop_gap_final $opt_Z "   if (defined $opt_Z);
    $retval .= "-num_threads $opt_a "       if (defined $opt_a);
    if (defined $opt_A) {
        if (defined $opt_P and $opt_P ne "0") {
            print STDERR "Warning: ignoring -P because window size is set\n";
        }
        $retval .= "-window_size $opt_A "       
    }
    if (defined $opt_P and $opt_P eq "1" and (not defined $opt_A)) {
        $retval .= "-window_size 0 ";
    }
    $retval .= "-word_size $opt_W "         if (defined $opt_W);
    if (defined $opt_Y) {
        $retval .= "-searchsp " . &convert_float_to_int($opt_Y) . " ";
    }
    if (defined $opt_f) {
        unless ($opt_p eq "blastn") {
            $retval .= "-min_word_score $opt_f "    
        } else {
            print STDERR "Warning: -f is not supported for blastn\n";
        }
    }
    if (defined $opt_I and (length($opt_I) == 0 or $opt_I =~ /t/i)) {
        $retval .= "-show_gis ";
    }
    $retval .= "-num_descriptions $opt_v "  if (defined $opt_v);
    $retval .= "-num_alignments $opt_b "    if (defined $opt_b);
    $retval .= "-query_gencode $opt_Q "     if (defined $opt_Q);
    $retval .= "-db_gencode $opt_D "        if (defined $opt_D);
    $retval .= "-penalty $opt_q "           if (defined $opt_q);
    $retval .= "-reward $opt_r "            if (defined $opt_r);
    $retval .= "-culling_limit $opt_K "     if (defined $opt_K);
    $retval .= "-max_intron_length $opt_t " if (defined $opt_t);
    $retval .= "-frame_shift_penalty $opt_w " if (defined $opt_w);
    $retval .= "-comp_based_stats $opt_C "  if (defined $opt_C);
    $retval .= "-out $opt_o "               if (defined $opt_o);
    if (defined $opt_m) {
        if ($opt_m == 5 or $opt_m == 6) {
            print STDERR "Warning: -m5 or -m6 formatting options ";
            print STDERR "are not supported!\n";
        }
        $opt_m -= 2 if ($opt_m >= 7);
        $retval .= "-outfmt $opt_m "            
    }
    if (defined $opt_O) {
        unless ($retval =~ s/-out \S+ /-out $opt_O /) {
            $retval .= "-out $opt_O ";
        }
        unless ($retval =~ s/-outfmt \d+/-outfmt 8/) {
            $retval .= "-outfmt 8 ";
        } else {
            print STDERR "Warning: overriding output format\n";
        }
    }
    if (defined $opt_T and (length($opt_T) == 0 or $opt_T =~ /t/i)) {
        $retval .= "-html "                     
    }

    $retval .= &convert_sequence_locations($opt_L, "query") if ($opt_L);
    if (defined $opt_U and (length($opt_U) == 0 or $opt_U =~ /t/i)) {
        $retval .= "-lcase_masking ";
    }
    if (defined $opt_g and $opt_g =~ /f/i) {
        $retval .= "-ungapped ";
    }
    if (defined $opt_J and (length($opt_J) == 0 or $opt_J =~ /t/i)) {
        $retval .= "-parse_deflines ";
    }
    $retval .= &convert_strand($opt_S) if (defined $opt_S and not
                                           ($opt_p ne "blastp" or 
                                            $opt_p ne "tblastn"));
    if (defined $opt_s and (length($opt_s) == 0 or $opt_s =~ /t/i)) {
        $retval .= "-use_sw_tback ";
    }

    if (defined $opt_F) {
        $retval .= &convert_filter_string($opt_F, $opt_p);
    } elsif (not defined $opt_F and $opt_p eq "blastp") {
        $retval .= &convert_filter_string("T", $opt_p);
    }

    return $retval;
}

sub handle_seedtop($)
{
    my $print_only = shift;
    my $path = DEFAULT_PATH;
    my ($opt_C, $opt_D, $opt_E, $opt_F, $opt_G, $opt_I, $opt_J, $opt_K, $opt_M,
        $opt_O, $opt_S, $opt_X, $opt_d, $opt_e, $opt_f, $opt_i, $opt_k, $opt_o,
        $opt_p, $opt_q, $opt_r);

    GetOptions("<>"             => sub { $application = shift; },
               "print_only!"    => $print_only,
               "path=s"         => \$path,
               "C=i"            => \$opt_C,
               "D=i"            => \$opt_D,
               "E=i"            => \$opt_E,
               "F:s"            => \$opt_F,
               "G=i"            => \$opt_G,
               "I:s"            => \$opt_I,
               "J:s"            => \$opt_J,
               "K=i"            => \$opt_K,
               "M=s"            => \$opt_M,
               "O=s"            => \$opt_O,
               "S=i"            => \$opt_S,
               "X=i"            => \$opt_X,
               "d=s"            => \$opt_d,
               "e=f"            => \$opt_e,
               "f:s"            => \$opt_f,
               "i=s"            => \$opt_i,
               "k=s"            => \$opt_k,
               "o=s"            => \$opt_o,
               "p=s"            => \$opt_p,
               "q=i"            => \$opt_q,
               "r=i"            => \$opt_r
               );

    my $retval = $path;
    $retval .= "/psiblast";
    $retval .= &add_exe_extension();
    $retval .= "-query $opt_i "             if (defined $opt_i);
    $retval .= "-phi_pattern $opt_k "       if (defined $opt_k);
    $retval .= &create_db_argument($opt_d)  if (defined $opt_d);
    $retval .= "-gapopen $opt_G "           if (defined $opt_G);
    $retval .= "-gapextend $opt_E "         if (defined $opt_E);
    $retval .= "-out $opt_o "               if (defined $opt_o);
    if (defined $opt_O) {
        unless ($retval =~ s/-out \S+ /-out $opt_O /) {
            $retval .= "-out $opt_O ";
        }
        unless ($retval =~ s/-outfmt \d+/-outfmt 8/) {
            $retval .= "-outfmt 8 ";
        } else {
            print STDERR "Warning: overriding output format\n";
        }
    }
    if (defined $opt_I and (length($opt_I) == 0 or $opt_I =~ /t/i)) {
        $retval .= "-show_gis ";
    }
    if (defined $opt_J and (length($opt_J) == 0 or $opt_J =~ /t/i)) {
        $retval .= "-parse_deflines ";
    }
    $retval .= "-xdrop_gap $opt_X "         if (defined $opt_X);
    $retval .= "-evalue $opt_e "            if (defined $opt_e);
    $retval .= "-matrix $opt_M "            if (defined $opt_M);
    my $query_is_protein = "1";
    if (defined $opt_p) {
        unless ($opt_p eq "patseedp") {
            die "Only patseedp program is supported\n";
        }
        # Change query_is_protein if other programs are supported
    } else {
        die "Program must be specified\n";
    }
    if (defined $opt_F) {
        $retval .= &convert_filter_string($opt_F,
                                          ($query_is_protein eq "1")
                                          ? "blastp" : "blastn");
    }

    # Unsupported options
    if (defined $opt_D) {
        print STDERR "Warning: -D option is not supported!\n";
    }
    if (defined $opt_S) {
        print STDERR "Warning: -S option is not supported!\n";
    }
    if (defined $opt_C) {
        print STDERR "Warning: -C option is not supported!\n";
    }
    if (defined $opt_q) {
        print STDERR "Warning: -q option is not supported!\n";
    }
    if (defined $opt_r) {
        print STDERR "Warning: -r option is not supported!\n";
    }
    if (defined $opt_f) {
        print STDERR "Warning: -f option is not supported!\n";
    }
    if (defined $opt_K) {
        print STDERR "Warning: -K option is not supported!\n";
    }
    return $retval;
}

sub handle_megablast($)
{
    my $print_only = shift;
    my $path = DEFAULT_PATH;
    my ($opt_A, $opt_D, $opt_E, $opt_F, $opt_G, $opt_H, $opt_I, $opt_J, 
        $opt_L, $opt_M, $opt_N, $opt_O, $opt_P, $opt_Q, $opt_R, $opt_S, 
        $opt_T, $opt_U, $opt_V, $opt_W, $opt_X, $opt_Y, $opt_Z, $opt_a, 
        $opt_b, $opt_d, $opt_e, $opt_f, $opt_g, $opt_i, $opt_l, $opt_m, 
        $opt_n, $opt_o, $opt_p, $opt_q, $opt_r, $opt_s, $opt_t, $opt_v, 
        $opt_y, $opt_z);

    GetOptions("<>"             => sub { $application = shift; },
               "print_only!"    => $print_only,
               "path=s"         => \$path,
               "A=i"            => \$opt_A,
               "D=i"            => \$opt_D,
               "E=i"            => \$opt_E,
               "F=s"            => \$opt_F,
               "G=i"            => \$opt_G,
               "H=i"            => \$opt_H,
               "I:s"            => \$opt_I,
               "J:s"            => \$opt_J,
               "L=s"            => \$opt_L,
               "M=i"            => \$opt_M,
               "N=i"            => \$opt_N,
               "O=s"            => \$opt_O,
               "P=i"            => \$opt_P, # no equivalent in new engine
               "Q=s"            => \$opt_Q,
               "R:s"            => \$opt_R,
               "S=i"            => \$opt_S,
               "T:s"            => \$opt_T,
               "U:s"            => \$opt_U,
               "V:s"            => \$opt_V, # not handled, not applicable
               "W=i"            => \$opt_W,
               "X=i"            => \$opt_X,
               "Y=f"            => \$opt_Y,
               "Z=i"            => \$opt_Z,
               "a=i"            => \$opt_a,
               "b=i"            => \$opt_b,
               "d=s"            => \$opt_d,
               "e=f"            => \$opt_e,
               "f:s"            => \$opt_f,
               "g:s"            => \$opt_g,
               "i=s"            => \$opt_i,
               "l=s"            => \$opt_l,
               "m=i"            => \$opt_m,
               "n:s"            => \$opt_n,
               "o=s"            => \$opt_o,
               "p=f"            => \$opt_p,
               "q=i"            => \$opt_q,
               "r=i"            => \$opt_r,
               "s=i"            => \$opt_s,
               "t=i"            => \$opt_t,
               "v=i"            => \$opt_v,
               "y=i"            => \$opt_y,
               "z=f"            => \$opt_z
               );
    my $retval = $path;

    $retval .= "/blastn";
    $retval .= &add_exe_extension();
    $retval .= "-query $opt_i "             if (defined $opt_i);
    $retval .= &create_db_argument($opt_d)  if (defined $opt_d);
    $retval .= "-evalue $opt_e "            if (defined $opt_e);
    $retval .= "-xdrop_gap $opt_X "         if (defined $opt_X);
    $retval .= "-gilist $opt_l "            if (defined $opt_l);
    $retval .= "-penalty $opt_q "           if (defined $opt_q);
    $retval .= "-reward $opt_r "            if (defined $opt_r);
    $retval .= "-gapopen $opt_G "           if (defined $opt_G);
    $retval .= "-gapextend $opt_E "         if (defined $opt_E);
    $retval .= "-out $opt_o "               if (defined $opt_o);
    if (defined $opt_m) {
        if ($opt_m == 5 or $opt_m == 6) {
            print STDERR "Warning: -m5 or -m6 formatting options ";
            print STDERR "are not supported!\n";
        }
        $opt_m -= 2 if ($opt_m >= 7);
        $retval .= "-outfmt $opt_m "            
    }
    if (defined $opt_O) {
        unless ($retval =~ s/-out \S+ /-out $opt_O /) {
            $retval .= "-out $opt_O ";
        }
        unless ($retval =~ s/-outfmt \d+/-outfmt 8/) {
            $retval .= "-outfmt 8 ";
        } else {
            print STDERR "Warning: overriding output format\n";
        }
    }
    if (defined $opt_T and (length($opt_T) == 0 or $opt_T =~ /t/i)) {
        $retval .= "-html "                     
    }
    $retval .= "-num_descriptions $opt_v "  if (defined $opt_v);
    $retval .= "-num_alignments $opt_b "    if (defined $opt_b);
    $retval .= "-num_threads $opt_a "       if (defined $opt_a);
    $retval .= "-word_size $opt_W "         if (defined $opt_W);
    $retval .= "-dbsize $opt_z "            if (defined $opt_z);
    if (defined $opt_Y) {
        $retval .= "-searchsp " . &convert_float_to_int($opt_Y) . " ";
    }
    $retval .= "-xdrop_ungap $opt_y "       if (defined $opt_y);
    $retval .= "-xdrop_gap_final $opt_Z "   if (defined $opt_Z);
    if (defined $opt_t) {
        $retval .= "-template_length $opt_t ";
        # Set the template type to the default value in megablast if not
        # provided, as blastn requires it
        $opt_N = 0 unless (defined $opt_N); 
    }
    $retval .= "-window_size $opt_A "       if (defined $opt_A);
    if (defined $opt_N) {
        $retval .= "-template_type coding " if ($opt_N == 0);
        $retval .= "-template_type optimal " if ($opt_N == 1);
        $retval .= "-template_type coding_and_optimal " if ($opt_N == 2);
    }
    if (defined $opt_F) {
        $retval .= &convert_filter_string($opt_F, "blastn");
    }
    if (defined $opt_J and (length($opt_J) == 0 or $opt_J =~ /t/i)) {
        $retval .= "-parse_deflines ";
    }


    $retval .= "-perc_identity $opt_p " if (defined $opt_p);
    $retval .= "-min_raw_gapped_score $opt_s " if (defined $opt_s);
    $retval .= &convert_strand($opt_S) if (defined $opt_S);
    $retval .= &convert_sequence_locations($opt_L, "query") if ($opt_L);
    if (defined $opt_U and (length($opt_U) == 0 or $opt_U =~ /t/i)) {
        $retval .= "-lcase_masking ";
    }
    if (defined $opt_n and (length($opt_n) == 0 or $opt_n =~ /t/i)) {
        $retval .= "-no_greedy ";
    }

    # Unsupported options
    # This option can be safely ignored
    #if (defined $opt_M) {
    #    print STDERR "Warning: -M option is ignored\n";
    #}

    my $tab_with_acc =
        "-outfmt \"7 qacc sseqid pident length mismatch gapopen qstart qend " .
        "sstart send evalue bitscore\" ";

    # Here are some combinations of options and their equivalent conversion to
    # the -outfmt option:
    # NOTE: only in the last case we use sgi as the user explicitely requests
    # the GIs to be shown (via -I), thus we assume the database/subjects will
    # have GIs. We don't do the same for accessions, because if these are not
    # available, an ordinal ID gets printed.
    # -J -D3 -R -fF = -outfmt "7 qacc sseqid pident length mismatch gapopen
    # qstart qend sstart send evalue bitscore"
    # -J -D3 -R -fT = -outfmt "7 qseqid sseqid pident length mismatch gapopen
    # qstart qend sstart send evalue bitscore"
    # -J -D3 -R -fT -I = -outfmt "7 qgi sgi pident length mismatch gapopen
    # qstart qend sstart send evalue bitscore"

    if (defined $opt_D) {
        if ($opt_D == 3) {  # tabular output
            unless ($retval =~ s/-outfmt \d+/$tab_with_acc/) {
                $retval .= "$tab_with_acc ";
            } else {
                print STDERR "Warning: overriding output format\n";
            }
        } elsif ($opt_D == 2) { # traditional BLAST output
            unless ($retval =~ s/-outfmt \d+/-outfmt 0/) {
                $retval .= "-outfmt 0 ";
            } else {
                print STDERR "Warning: overriding output format\n";
            }
        } elsif ($opt_D == 4) { # text ASN.1
            unless ($retval =~ s/-outfmt \d+/-outfmt 8/) {
                $retval .= "-outfmt 8 ";
            } else {
                print STDERR "Warning: overriding output format\n";
            }
        } elsif ($opt_D == 5) { # binary ASN.1
            unless ($retval =~ s/-outfmt \d+/-outfmt 9/) {
                $retval .= "-outfmt 9 ";
            } else {
                print STDERR "Warning: overriding output format\n";
            }
        } else {
            print STDERR "Warning: -D option with value $opt_D is not " .
                "supported!\n";
        }
    }

    if (defined $opt_I and (length($opt_I) == 0 or $opt_I =~ /t/i)) {
        $retval .= "-show_gis ";
        $retval =~ s/qacc/qgi/;
        $retval =~ s/sseqid/sgi/;
    }
    # -fF is the default, if -f or -fT is specified, we assume that's what's
    # desired and we apply a modification to the previously set output format
    # (we can safely assume this b/c -f only works with -D3)
    if (defined $opt_f and (length($opt_f) == 0 or $opt_f =~ /t/i)) {
        $retval =~ s/qacc/qseqid/;
    }
    if (defined $opt_R and not ($retval =~ /-outfmt.*7/)) {
        print STDERR "Warning: -R option is deprecated, please rely on the ".
            "application's exit code to determine its success or failure.\n" .
            "0 means success, non-zero means failure\n";
    }
    # Deprecated options
    if (defined $opt_g and $opt_g =~ /f/i) {
        print STDERR "Warning: -g option is not supported!\n";
    }
    if (defined $opt_H) {
        print STDERR "Warning -H option is not supported!\n";
    }
    if (defined $opt_Q) {
        print STDERR "Warning: -Q option is deprecated\n";
    }
    if (defined $opt_P) {
        print STDERR "Warning: -P option is deprecated\n";
    }

    return $retval;
}

sub handle_blastpgp($)
{
    my $print_only = shift;
    my $path = DEFAULT_PATH;
    my ($opt_A, $opt_B, $opt_C, $opt_E, $opt_F, $opt_G, $opt_H, $opt_I, 
        $opt_J, $opt_K, $opt_L, $opt_M, $opt_N, $opt_O, $opt_P, $opt_Q, 
        $opt_R, $opt_S, $opt_T, $opt_U, $opt_W, $opt_X, $opt_Y, $opt_Z, 
        $opt_a, $opt_b, $opt_c, $opt_d, $opt_e, $opt_f, $opt_h, $opt_i, 
        $opt_j, $opt_k, $opt_l, $opt_m, $opt_o, $opt_p, $opt_q, $opt_s, 
        $opt_t, $opt_u, $opt_v, $opt_y, $opt_z);

    GetOptions("<>"             => sub { $application = shift; },
               "print_only!"    => $print_only,
               "path=s"         => \$path,
               "A=i"            => \$opt_A,
               "B=s"            => \$opt_B,
               "C=s"            => \$opt_C,
               "E=i"            => \$opt_E,
               "F=s"            => \$opt_F,
               "G=i"            => \$opt_G,
               "H=i"            => \$opt_H,
               "I:s"            => \$opt_I,
               "J:s"            => \$opt_J,
               "K=i"            => \$opt_K,
               "L=i"            => \$opt_L,
               "M=s"            => \$opt_M,
               "N=f"            => \$opt_N,
               "O=s"            => \$opt_O,
               "P=i"            => \$opt_P,
               "Q=s"            => \$opt_Q,
               "R=s"            => \$opt_R,
               "S=i"            => \$opt_S,
               "T:s"            => \$opt_T,
               "U:s"            => \$opt_U,
               "W=i"            => \$opt_W,
               "X=i"            => \$opt_X,
               "Y=f"            => \$opt_Y,
               "Z=i"            => \$opt_Z,
               "a=i"            => \$opt_a,
               "b=i"            => \$opt_b,
               "c=i"            => \$opt_c,
               "d=s"            => \$opt_d,
               "e=f"            => \$opt_e,
               "f=i"            => \$opt_f,
               "h=f"            => \$opt_h,
               "i=s"            => \$opt_i,
               "j=i"            => \$opt_j,
               "k=s"            => \$opt_k,
               "l=s"            => \$opt_l,
               "m=i"            => \$opt_m,
               "o=s"            => \$opt_o,
               "p=s"            => \$opt_p,
               "q=i"            => \$opt_q,
               "s:s"            => \$opt_s,
               "t=s"            => \$opt_t,
               "u=i"            => \$opt_u,
               "v=i"            => \$opt_v,
               "y=f"            => \$opt_y,
               "z=f"            => \$opt_z
               );
    my $retval = $path . "/psiblast";
    $retval .= &add_exe_extension();

    my $query_is_protein = "1";

    if (defined $opt_p and not ($opt_p ne "blastpgp" or
                                $opt_p ne "patseedp")) {
        die "Program '$opt_p' not implemented\n";
    }

    $retval .= &create_db_argument($opt_d)  if (defined $opt_d);
    $retval .= "-query $opt_i "             if (defined $opt_i);
    $retval .= "-gilist $opt_l "            if (defined $opt_l);
    $retval .= "-gap_trigger $opt_N "       if (defined $opt_N);
    $retval .= "-matrix $opt_M "            if (defined $opt_M);
    $retval .= "-num_iterations $opt_j "    if (defined $opt_j);
    $retval .= "-min_word_score $opt_f "    if (defined $opt_f);
    $retval .= "-evalue $opt_e "            if (defined $opt_e);
    $retval .= "-gapopen $opt_G "           if (defined $opt_G);
    $retval .= "-gapextend $opt_E "         if (defined $opt_E);
    $retval .= "-num_threads $opt_a "       if (defined $opt_a);
    $retval .= "-dbsize $opt_z "            if (defined $opt_z);
    if (defined $opt_Y) {
        $retval .= "-searchsp " . &convert_float_to_int($opt_Y) . " ";
    }
    $retval .= "-pseudocount $opt_c "       if (defined $opt_c);
    $retval .= "-inclusion_ethresh $opt_h " if (defined $opt_h);
    if (defined $opt_A) {
        if (defined $opt_P and $opt_P ne "0") {
            print STDERR "Warning: ignoring -P because window size is set\n";
        }
        $retval .= "-window_size $opt_A "       
    }
    if (defined $opt_P and $opt_P eq "1" and (not defined $opt_A)) {
        $retval .= "-window_size 0 ";
    }
    $retval .= "-word_size $opt_W "         if (defined $opt_W);
    $retval .= "-xdrop_ungap $opt_y "       if (defined $opt_y);
    $retval .= "-xdrop_gap $opt_X "         if (defined $opt_X);
    $retval .= "-xdrop_gap_final $opt_Z "   if (defined $opt_Z);
    $retval .= "-num_descriptions $opt_v "  if (defined $opt_v);
    $retval .= "-num_alignments $opt_b "    if (defined $opt_b);
    $retval .= "-culling_limit $opt_K "     if (defined $opt_K);
    $retval .= "-comp_based_stats $opt_t "  if (defined $opt_t);
    $retval .= "-phi_pattern $opt_k "       if (defined $opt_k);
    $retval .= "-out $opt_o "               if (defined $opt_o);
    $retval .= "-out_ascii_pssm $opt_Q "    if (defined $opt_Q);
    $retval .= "-in_msa $opt_B "            if (defined $opt_B);

    if (defined $opt_m) {
        if ($opt_m == 5 or $opt_m == 6) {
            print STDERR "Warning: -m5 or -m6 formatting options ";
            print STDERR "are not supported!\n";
        }
        $opt_m -= 2 if ($opt_m >= 7);
        $retval .= "-outfmt $opt_m "            
    }
    if (defined $opt_O) {
        unless ($retval =~ s/-out \S+ /-out $opt_O /) {
            $retval .= "-out $opt_O ";
        }
        unless ($retval =~ s/-outfmt \d+/-outfmt 8/) {
            $retval .= "-outfmt 8 ";
        } else {
            print STDERR "Warning: overriding output format\n";
        }
    }
    if (defined $opt_T and (length($opt_T) == 0 or $opt_T =~ /t/i)) {
        $retval .= "-html "                     
    }
    if (defined $opt_I and (length($opt_I) == 0 or $opt_I =~ /t/i)) {
        $retval .= "-show_gis ";
    }
    if (defined $opt_J and (length($opt_J) == 0 or $opt_J =~ /t/i)) {
        $retval .= "-parse_deflines ";
    }
    if (defined $opt_s and (length($opt_s) == 0 or $opt_s =~ /t/i)) {
        $retval .= "-use_sw_tback ";
    }
    if (defined $opt_U and (length($opt_U) == 0 or $opt_U =~ /t/i)) {
        $retval .= "-lcase_masking ";
    }
    if (defined $opt_F) {
        $retval .= &convert_filter_string($opt_F, 
                                          ($query_is_protein eq "1")
                                          ? "blastp" : "blastn");
    }

    my $location = "";
    $location .= $opt_S if (defined $opt_S);
    if (defined $opt_H) {
        if ($location eq "") {
            $location = "0,$opt_H";
        } else {
            $location .= ",$opt_H";
        }
    }
    if ($location ne "") {
        $location .= ",-1" unless (defined $opt_H);
        $retval .= &convert_sequence_locations($location, "query");
    }

    # Checkpoint file recovery
    if (defined $opt_R) {
        if (defined $opt_q and $opt_q ne "0") {
            $retval .= "-in_pssm $opt_R " 
        } else {
            die "ERROR: recovery from C toolkit checkpoint " .
                "file format not supported\n";
        }
    }

    # Checkpoint file saving
    if (defined $opt_C) {
        if (defined $opt_C and $opt_u ne "0") {
            $retval .= "-out_pssm $opt_C "          
        } else {
            die "ERROR: saving PSSM to C toolkit checkpoint " .
                "file format not supported\n";
        }
    }

    return $retval;
}

# Tested: all conversions should work
sub handle_bl2seq
{
    use File::Temp qw(:POSIX);  # for tmpnam

    my $print_only = shift;
    my $path = DEFAULT_PATH;
    my ($opt_A, $opt_D, $opt_E, $opt_F, $opt_G, $opt_I, $opt_J, $opt_M, 
        $opt_S, $opt_T, $opt_U, $opt_V, $opt_W, $opt_X, $opt_Y, $opt_a, 
        $opt_d, $opt_e, $opt_g, $opt_i, $opt_j, $opt_m, $opt_o, $opt_p, 
        $opt_q, $opt_r, $opt_t);

    GetOptions("<>"             => sub { $application = shift; },
               "print_only!"    => $print_only,
               "path=s"         => \$path,
               "A:s"            => \$opt_A,
               "D=i"            => \$opt_D,
               "E=i"            => \$opt_E,
               "F=s"            => \$opt_F,
               "G=i"            => \$opt_G,
               "I=s"            => \$opt_I,
               "J=s"            => \$opt_J,
               "M=s"            => \$opt_M,
               "S=i"            => \$opt_S,
               "T:s"            => \$opt_T,
               "U:s"            => \$opt_U,
               "V:s"            => \$opt_V, # not handled, not applicable
               "W=i"            => \$opt_W,
               "X=i"            => \$opt_X,
               "Y=f"            => \$opt_Y,
               "a=s"            => \$opt_a,
               "d=f"            => \$opt_d,
               "e=f"            => \$opt_e,
               "g:s"            => \$opt_g,
               "i=s"            => \$opt_i,
               "j=s"            => \$opt_j,
               "m:s"            => \$opt_m,
               "o=s"            => \$opt_o,
               "p=s"            => \$opt_p,
               "q=i"            => \$opt_q,
               "r=i"            => \$opt_r,
               "t=i"            => \$opt_t
               );
    my $retval = $path;

    unless (defined $opt_i and defined $opt_j) {
        die "-i and -j are required in bl2seq\n";
    }

    if (defined $opt_p) {
        $retval .= "/$opt_p";
        $retval .= &add_exe_extension();
    } else {
        die "Program must be specified via the -p option\n";
    }
    unless (defined $opt_A) {
        $retval .= "-query $opt_i "             if (defined $opt_i);
        $retval .= "-subject $opt_j "           if (defined $opt_j);
    } else {
        # The -A option is not supported, so we create temporary files to
        # simulate it (example input: bl2seq -i129295 -j104501 -pblastp -A)
        my $query_fname = tmpnam();
        open(Q, ">$query_fname") or die "Failed to open $query_fname: $!\n";
        print Q "$opt_i" and close(Q);
        push @files2delete, $query_fname;

        my $subj_fname = tmpnam();
        open(S, ">$subj_fname") or die "Failed to open $subj_fname: $!\n";
        print S "$opt_j" and close(S);
        push @files2delete, $subj_fname;
        if (DEBUG) {
            print STDERR "Created temp. files $query_fname and $subj_fname\n";
        }

        $retval .= "-query $query_fname -subject $subj_fname ";
        if ($$print_only) {
            print STDERR "Warning: arguments to -query and -subject must be ";
            print STDERR "files containing the\narguments to bl2seq's -i and ";
            print STDERR "-j arguments respectively.\n";
        }
    }
    $retval .= "-out $opt_o "               if (defined $opt_o);
    if (defined $opt_a) {
        unless ($retval =~ s/-out \S+ /-out $opt_a /) {
            $retval .= "-out $opt_a ";
        }
        unless ($retval =~ s/-outfmt \d+/-outfmt 8/) {
            $retval .= "-outfmt 8 ";
        } else {
            print STDERR "Warning: overriding output format\n";
        }
    }
    if (defined $opt_D and $opt_D =~ /1/) {
        if ($retval =~ s/-outfmt \d+/-outfmt 7/) {
            print STDERR "Warning: overriding output format\n";
        } else {
            $retval .= "-outfmt 7 ";
        }
    }
    if (defined $opt_T and (length($opt_T) == 0 or $opt_T =~ /t/i)) {
        $retval .= "-html "                     
    }
    $retval .= "-evalue $opt_e "            if (defined $opt_e);
    $retval .= "-gapopen $opt_G "           if (defined $opt_G);
    $retval .= "-gapextend $opt_E "         if (defined $opt_E);
    $retval .= "-word_size $opt_W "         if (defined $opt_W);
    $retval .= "-matrix $opt_M "            if (defined $opt_M);
    $retval .= "-penalty $opt_q "           if (defined $opt_q);
    $retval .= "-reward $opt_r "            if (defined $opt_r);
    $retval .= &convert_strand($opt_S)      if (defined $opt_S);
    $retval .= "-max_intron_length $opt_t " if (defined $opt_t);
    $retval .= "-dbsize $opt_d "            if (defined $opt_d);
    $retval .= "-xdrop_gap $opt_X "         if (defined $opt_X);
    if (defined $opt_Y) {
        $retval .= "-searchsp " . &convert_float_to_int($opt_Y) . " ";
    }
    if (defined $opt_U and (length($opt_U) == 0 or $opt_U =~ /t/i)) {
        $retval .= "-lcase_masking ";
    }
    if (defined $opt_m and (length($opt_m) == 0 or $opt_m =~ /t/i)) {
        $retval .= "-task megablast ";
    }
    if (defined $opt_g and $opt_g =~ /f/i) {
        $retval .= "-ungapped ";
    }
    $retval .= &convert_sequence_locations($opt_I, "query") if ($opt_I);
    $retval .= &convert_sequence_locations($opt_J, "subject") if ($opt_J);

    if (defined $opt_F) {
        $retval .= &convert_filter_string($opt_F, $opt_p);
    }

    return $retval;
}

sub handle_rpsblast
{
    my $print_only = shift;
    my $path = DEFAULT_PATH;
    my ($opt_F, $opt_I, $opt_J, $opt_L, $opt_N, $opt_O, $opt_P, $opt_T, 
        $opt_U, $opt_V, $opt_X, $opt_Y, $opt_Z, $opt_a, $opt_b, $opt_d, 
        $opt_e, $opt_i, $opt_l, $opt_m, $opt_o, $opt_p, $opt_v, $opt_y, 
        $opt_z);

    GetOptions("<>"             => sub { $application = shift; },
               "print_only!"    => $print_only,
               "path=s"         => \$path,
               "F=s"            => \$opt_F,
               "I:s"            => \$opt_I,
               "J:s"            => \$opt_J,
               "L=s"            => \$opt_L,
               "N=f"            => \$opt_N,
               "O=s"            => \$opt_O,
               "P=i"            => \$opt_P,
               "T:s"            => \$opt_T,
               "U:s"            => \$opt_U,
               "V=s"            => \$opt_V,
               "X=i"            => \$opt_X,
               "Y=f"            => \$opt_Y,
               "Z=i"            => \$opt_Z,
               "a=i"            => \$opt_a,
               "b=i"            => \$opt_b,
               "d=s"            => \$opt_d,
               "e=f"            => \$opt_e,
               "i=s"            => \$opt_i,
               "l=s"            => \$opt_l,
               "m=i"            => \$opt_m,
               "o=s"            => \$opt_o,
               "p:s"            => \$opt_p,
               "v=i"            => \$opt_v,
               "y=f"            => \$opt_y,
               "z=f"            => \$opt_z
               );
    my $retval = $path;

    if (defined $opt_p and $opt_p =~ /f/i) {
        $retval .= "/rpstblastn";
    } else {
        $retval .= "/rpsblast";
    }
    $retval .= &add_exe_extension();

    $retval .= "-query $opt_i "             if (defined $opt_i);
    $retval .= &create_db_argument($opt_d)  if (defined $opt_d);
    $retval .= "-evalue $opt_e "            if (defined $opt_e);
    $retval .= "-out $opt_o "               if (defined $opt_o);
    $retval .= "-xdrop_ungap $opt_y "       if (defined $opt_y);
    $retval .= "-xdrop_gap $opt_X "         if (defined $opt_X);
    $retval .= "-min_raw_gapped_score $opt_N " if (defined $opt_N);
    $retval .= "-num_threads $opt_a "       if (defined $opt_a);
    $retval .= "-num_descriptions $opt_v "  if (defined $opt_v);
    $retval .= "-num_alignments $opt_b "    if (defined $opt_b);
    $retval .= "-dbsize $opt_z "            if (defined $opt_z);
    if (defined $opt_Y) {
        $retval .= "-searchsp " . &convert_float_to_int($opt_Y) . " ";
    }
    $retval .= "-xdrop_gap_final $opt_Z "   if (defined $opt_Z);
    if (defined $opt_m) {
        if ($opt_m == 5 or $opt_m == 6) {
            print STDERR "Warning: -m5 or -m6 formatting options ";
            print STDERR "are not supported!\n";
        }
        $opt_m -= 2 if ($opt_m >= 7);
        $retval .= "-outfmt $opt_m "            
    }
    if (defined $opt_O) {
        unless ($retval =~ s/-out \S+ /-out $opt_O /) {
            $retval .= "-out $opt_O ";
        }
        unless ($retval =~ s/-outfmt \d+/-outfmt 8/) {
            $retval .= "-outfmt 8 ";
        } else {
            print STDERR "Warning: overriding output format\n";
        }
    }
    if (defined $opt_T and (length($opt_T) == 0 or $opt_T =~ /t/i)) {
        $retval .= "-html "                     
    }
    if (defined $opt_P and $opt_P eq "1") {
        $retval .= "-window_size 0 ";
    }
    if (defined $opt_F) {
        $retval .= &convert_filter_string($opt_F, "blastp");
    }
    if (defined $opt_I and (length($opt_I) == 0 or $opt_I =~ /t/i)) {
        $retval .= "-show_gis ";
    }
    if (defined $opt_J and (length($opt_J) == 0 or $opt_J =~ /t/i)) {
        $retval .= "-parse_deflines ";
    }
    if (defined $opt_U and (length($opt_U) == 0 or $opt_U =~ /t/i)) {
        $retval .= "-lcase_masking ";
    }
    $retval .= &convert_sequence_locations($opt_L, "query") if ($opt_L);

    return $retval;
}

sub handle_fastacmd
{
    my $print_only = shift;
    my $path = DEFAULT_PATH;
    my ($opt_d, $opt_p, $opt_s, $opt_i, $opt_a, $opt_l, $opt_t, $opt_o,
        $opt_c, $opt_D, $opt_L, $opt_S, $opt_T, $opt_I, $opt_P);

    GetOptions("<>"             => sub { $application = shift; },
               "print_only!"    => $print_only,
               "path=s"         => \$path,
               "D=i"            => \$opt_D,
               "I:s"            => \$opt_I,
               "L=s"            => \$opt_L,
               "P=i"            => \$opt_P,
               "S=i"            => \$opt_S,
               "T:s"            => \$opt_T,
               "a:s"            => \$opt_a,
               "c:s"            => \$opt_c,
               "d=s"            => \$opt_d,
               "i=s"            => \$opt_i,
               "l=i"            => \$opt_l,
               "o=s"            => \$opt_o,
               "p=s"            => \$opt_p,
               "s=s"            => \$opt_s,
               "t:s"            => \$opt_t
               );

    my $retval = $path . "/blastdbcmd";
    $retval .= &add_exe_extension();
    $retval .= &create_db_argument($opt_d)  if (defined $opt_d);
    if (defined $opt_p) {
        $retval .= "-dbtype ";
        if ($opt_p =~ /p/i) {
            $retval .= "prot ";
        } elsif ($opt_p =~ /f/i) {
            $retval .= "nucl ";
        } else {
            $retval .= "guess ";
        }
    }
    $retval .= "-entry $opt_s "             if (defined $opt_s);
    $retval .= "-entry_batch $opt_i "       if (defined $opt_i);
    $retval .= "-line_length $opt_l "       if (defined $opt_l);
    $retval .= "-out $opt_o "               if (defined $opt_o);
    $retval .= "-pig $opt_P "               if (defined $opt_P);
    if (defined $opt_D) {
        $retval .= "-entry all -outfmt ";
        if ($opt_D eq '1') {
            $retval .= "\%f ";
        } elsif ($opt_D eq '2') {
            $retval .= "\%g ";
        } elsif ($opt_D eq '3') {
            $retval .= "\%a ";
        } else {
            die "Invalid argument to -D\n";
        }
    }
    $retval .= &convert_sequence_locations($opt_L, "range") if ($opt_L);
    $retval .= &convert_strand($opt_S) if (defined $opt_S);
    if (defined $opt_T) {
        #print STDERR "Warning: -T option is not supported, please use " .
        #    "the -outfmt option to blastdbcmd with \%T, \%L, or \%S as an " .
        #    "argument\n";
        $retval .= "-outfmt \"NCBI Taxonomy id: \%T; Common name: \%L; ";
        $retval .= "Scientific name: \%S\" ";
    }
    if (defined $opt_I and (length($opt_I) == 0 or $opt_I =~ /t/i)) {
        $retval .= "-info ";
    }
    if (defined $opt_a and (length($opt_a) == 0 or $opt_a =~ /t/i)) {
        $retval .= "-get_dups ";
    }
    if (defined $opt_t and (length($opt_t) == 0 or $opt_t =~ /t/i)) {
        $retval .= "-target_only ";
    }
    if (defined $opt_c and (length($opt_c) == 0 or $opt_c =~ /t/i)) {
        $retval .= "-ctrl_a ";
    }
    return $retval;
}

sub handle_formatdb
{
    my $print_only = shift;
    my $path = DEFAULT_PATH;
    my ($opt_B, $opt_F, $opt_L, $opt_T, $opt_V, $opt_a, $opt_b, $opt_e, $opt_i,
        $opt_l, $opt_n, $opt_o, $opt_p, $opt_s, $opt_t, $opt_v);

    GetOptions("<>"             => sub { $application = shift; },
               "print_only!"    => $print_only,
               "path=s"         => \$path,
               "B=s"            => \$opt_B,
               "F=s"            => \$opt_F,
               "L=s"            => \$opt_L,
               "T=s"            => \$opt_T,
               "V:s"            => \$opt_V,
               "a:s"            => \$opt_a,
               "b:s"            => \$opt_b,
               "e:s"            => \$opt_e,
               "i=s"            => \$opt_i,
               "l=s"            => \$opt_l,
               "n=s"            => \$opt_n,
               "o:s"            => \$opt_o,
               "p:s"            => \$opt_p,
               "s:s"            => \$opt_s,
               "t=s"            => \$opt_t,
               "v=i"            => \$opt_v
               );

    my $retval = $path;
    if (defined $opt_L) {
        $retval .= "/blastdb_aliastool";
        die "-i is required\n" unless (defined $opt_i);
        die "-F is required\n" unless (defined $opt_F);
    } else {
        $retval .= "/makeblastdb";
    }
    $retval .= &add_exe_extension();

    if (defined $opt_B) {
        die "-F option must be specified with -B\n" unless (defined $opt_F);
        $retval = $path . "/blastdb_aliastool";
        $retval .= &add_exe_extension();
        $retval .= "-gi_file_in $opt_F -gi_file_out $opt_B";
        return $retval;
    }

    $retval .= "-title \"$opt_t\" "         if (defined $opt_t);
    if (defined $opt_p) {
        $retval .= "-dbtype ";
        if ((length($opt_p) == 0 or $opt_p =~ /t/i)) {
            $retval .= "prot ";
        } else {
            $retval .= "nucl ";
        }
    }
    if ($retval =~ /blastdb_aliastool/) {
        $retval .= "-out $opt_L "               if (defined $opt_L);
        if (defined $opt_i and not defined $opt_n) {
            $retval .= &create_db_argument($opt_i);
        }
        # there's no -n in blastdb_aliastool, as we copy the argument value
        # verbatim into the DBLIST field of the alias file, so we make
        # formatdb's -n option tool override -i
        $retval .= &create_db_argument($opt_n)  if (defined $opt_n);
    } else {
        $retval .= "-out $opt_n "               if (defined $opt_n);
        $retval .= "-in $opt_i "                if (defined $opt_i);
    }
    $retval .= "-gilist $opt_F "            if (defined $opt_F);
    $retval .= "-logfile $opt_l "           if (defined $opt_l);
    $retval .= "-taxid-map $opt_T "           if (defined $opt_T);

    if (defined $opt_o and (length($opt_o) == 0 or $opt_o =~ /t/i)) {
        $retval .= "-parse_seqids ";
    }
    if (defined $opt_a) {
        print STDERR "Warning: -a option is not supported\n";
    }
    if (defined $opt_b) {
        print STDERR "Warning: -b option is not supported\n";
    }
    if (defined $opt_e) {
        print STDERR "Warning: -e option is not supported\n";
    }
    if (defined $opt_s) {
        print STDERR "Warning: -s option is not supported\n";
    }
    if (defined $opt_V) {
        print STDERR "Warning: -V option is not supported\n";
    }
    if (defined $opt_v) {
        print STDERR "Warning: -v option is not supported, please use " .
            "the -max_file_sz option to makeblastdb\n";
    }
    return $retval;
}
__END__

=head1 NAME

B<legacy_blast.pl> - Convert BLAST command line invocations from NCBI C 
toolkit's implementation to NCBI C++ toolkit's implementation.

=head1 SYNOPSIS

legacy_blast.pl <C toolkit command line program and arguments> [--print_only] 
[--path /path/to/binaries] 
legacy_blast.pl [--version]
legacy_blast.pl [--help]

=head1 OPTIONS

=over 2

=item B<--path>

Use the provided path as the location of the BLAST binaries to execute/print
(default: /usr/bin).

=item B<--print_only>

Print the equivalent command line option instead of running the command
(default: false).

=item B<--version>

Prints this script's version. Must be invoked as the first and only argument to
this script.

=back

=head1 DESCRIPTION

This script converts and runs the equivalent NCBI C toolkit command line BLAST 
program and arguments provided to it (whenever possible) to NCBI C++ tookit 
BLAST programs. Note that to specify options to this script they B<MUST> use 2
dashes to prefix the options B<AND> be listed at the end of the command line
invocation to convert.

=head1 EXIT CODES

This script returns 0 on success and a non-zero value on errors.

=head1 BUGS

Please report them to <blast-help@ncbi.nlm.nih.gov>

=head1 COPYRIGHT

See PUBLIC DOMAIN NOTICE included at the top of this script.

=cut
