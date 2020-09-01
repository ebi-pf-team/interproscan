package PIRSR;

use strict;
use warnings;

use JSON;

use Moose;
use namespace::autoclean;

use Bio::Pfam::HMM::HMMResultsIO;
use File::Temp qw/tempfile/;
use File::Copy;


use Smart::Comments;



has 'data_folder' => (
    is       => 'rw',
    isa      => 'Str',
    required => 1
);

# has 'hmm_folder' => (
#     is       => 'rw',
#     isa      => 'Str',
#     required => 1
# );

# has 'rule_folder' => (
#     is       => 'rw',
#     isa      => 'Str',
#     required => 1
# );

has 'verbose' => (
    is       => 'rw',
    isa      => 'Bool',
    default  => 0
);

has 'hmmalign' => (
    is       => 'rw',
    isa      => 'Str',
    default  => 'hmmalign'
);

has 'hmmscan' => (
    is       => 'rw',
    isa      => 'Str',
    default  => 'hmmscan'
);

has 'cpus' => (
    is       => 'rw',
    isa      => 'Num',
    default  => 1
);




# # returns array of seq->hmm positions for a given alignment string
# sub prepare_data_paths {
#     my ($data_folder, $hmm_folder, $template_folder, $rule_folder) = @_;

#     $data_folder =~ s/\/$//;

#     # set the default hmm_folder
#     if (!$hmm_folder && $data_folder) {
#         $hmm_folder = "${data_folder}/sr_hmm";
#     }

#     # set the default template_folder
#     if (!$template_folder && $data_folder) {
#         $template_folder = "${data_folder}/sr_tp";
#     }

#     # set the default rule_folder and move the uru file there
#     if (!$rule_folder && $data_folder) {
#         $rule_folder = "${data_folder}/sr_uru";
#         if (!-d $rule_folder) {
#             mkdir($rule_folder);
#         }

#         if (!-e "${rule_folder}/PIRSR.uru") {
#             move("${data_folder}/PIRSR.uru", "${rule_folder}/PIRSR.uru");
#         }
#     }

#     return {
#         template_folder => $template_folder,
#         hmm_folder      => $hmm_folder,
#         rule_folder     => $rule_folder,
#     }
# }





sub process_data {
    my ($self) = @_;

    my $data_folder = $self->{data_folder};
#     $data_folder =~ s/\/$//;
# ### $data_folder

    # check provided data folder exists
    if (! -d $data_folder) {
        warn ("Could not find data folder '$data_folder'\n")  if ($self->verbose);
        return 0;
    }


    # check the hmm_folder
    if (! -d "${data_folder}/sr_hmm") {
        warn ("Could not find hmm data folder '${data_folder}/sr_hmm'\n")  if ($self->verbose);
        return 0;
    }

    # check the templates file
    if (! -e "${data_folder}/sr_tp/sr_tp.seq") {
        warn ("Could not find template data file '${data_folder}/sr_tp/sr_tp.seq'\n")  if ($self->verbose);
        return 0;
    }

    # check the rules file
    if (-e "${data_folder}/PIRSR.uru") {
        if (!-d "${data_folder}/sr_uru") {
            mkdir("${data_folder}/sr_uru");
        }
        move("${data_folder}/PIRSR.uru", "${data_folder}/sr_uru/PIRSR.uru")
    } else {
        if (! -e "${data_folder}/sr_uru/PIRSR.uru") {
            warn ("Could not find rules data file '${data_folder}/PIRSR.uru or ${data_folder}/sr_uru/PIRSR.uru'\n")  if ($self->verbose);
            return 0;
        }
    }

    my $template_ok = $self->process_templates();
    my $hmm_ok = $self->process_hmms();
    my $rule_ok = $self->process_rules();

    if ($template_ok && $hmm_ok && $rule_ok) {
        return 1;
    } else {
        warn ("Error preprocessing")  if ($self->verbose);
        return 0;
    }
}




sub process_templates {
    my ($self) = @_;

    my $template_folder = $self->{data_folder} . '/sr_tp';

    # parse the sr_tp.seq a seq block at a time
    do {
        local $/ = ">";
        open (my $in, '<', "$template_folder/sr_tp.seq") or die "Failed top open $template_folder/sr_tp.seq file: $!\n";
        while (my $block = <$in>) {

            $block =~ s/\n?>?\z//;
            next if !$block;


            # the first line (seq identifier) has the format: 'Q59771   PIRSR000188-2'
            if ($block =~ /\A((\w+)\s+\w+-\d+)\n/) {
                # the whole line is the $header, the first bit is the $prot_id
                my $header = $1;
                my $prot_it = $2;

                # now we have $header and $prot_it, get rid of the first line and grab the seq
                $block =~ s/\A(\w+)\s+(\w+-\d+)\n//;
                $block =~ s/\n//g;

                my $seq = $block;

                open (my $fa_out, '>', "${template_folder}/${prot_it}.fa" ) or die "Failed top open ${prot_it}.fa file: $!\n";

                print $fa_out ">${header}\n${seq}\n";

                close($fa_out) or die "Failed to close ${prot_it}.fa\n";


            } else {
                die "Failed to parse fasta block: \"$block\"\n";
            }
        }

        close($in) or die "Failed to close $template_folder/sr_tp.seq file\n";
    };



    return 1;
}


sub process_hmms {
    my ($self) = @_;

    my $hmm_folder = $self->{data_folder} . '/sr_hmm';

    open(my $hmm_out, '>', "$self->{data_folder}/sr_hmm_all") or die "Can't open '$hmm_folder/sr_hmm_all: $!\n";

    foreach my $hmm_file ( glob("$hmm_folder/PIRSR*.hmm") ) {

        open(my $hmm_in, '<', "$hmm_file") or die "Can't open '$hmm_file': $!\n";
        # slurp individual hmm file
        my $hmm = do { local $/; <$hmm_in> };
        close($hmm_in) or die "Can't close '$hmm_file' after reading: $!\n";

        # fix hmm name
        my $hmm_name = $hmm_file;
        # remove path
        $hmm_name =~ s/.*\///;
        # remove file type
        $hmm_name =~ s/\.hmm//;
        # replace with hmm/rule namehmm_folder
        $hmm =~ s/NAME\s+.*?\n/NAME  ${hmm_name}\nACC   ${hmm_name}\n/;

        # print it out to library file
        print $hmm_out $hmm;

    }

    close($hmm_out) or die "Can't close '$self->{data_folder}/sr_hmm_all' after writing: $!\n";

    # create auxfiles
    my $cmd = "hmmpress $self->{data_folder}/sr_hmm_all";

    foreach my $ext (qw(h3p h3m h3f h3i)) {
        if (!-e "$self->{data_folder}/sr_hmm_all.${ext}") {
            # Looks like the hmm database is not pressed
            warn "Running hmmpress on $self->{data_folder}/sr_hmm_all\n" if ($self->verbose);
            system("hmmpress $self->{data_folder}/sr_hmm_all") and die "Could not run hmmpress: $!\n";
            last;
        }
    }

    return 1;
}


sub process_rules {
    my ($self) = @_;

    my $rule_folder = $self->{data_folder} . '/sr_uru';

    my $rules = {};

    do {
        local $/ = "//\n";
        open (my $uru_in, '<', "${rule_folder}/PIRSR.uru") or die "Failed top open ${rule_folder}/PIRSR.uru file: $!\n";
        while (my $block = <$uru_in>) {
            my $rule_hash = _parse_rules($block);

            $rule_hash = $self->align_template($rule_hash);

            # my $rule_acc = $rule_hash->{'AC'};

            $rules->{$rule_hash->{'AC'}} = $rule_hash;


            # open (my $uru_out, '>', "${rule_folder}/${rule_acc}.json" ) or die "Failed to open ${rule_acc}.json file: $!\n";

            # my $json_uru = to_json( $rule_hash, { pretty => 1 } );
            # print $uru_out $json_uru;

            # close($uru_out) or die "Failed to close ${rule_acc}.json\n";
        }
        close($uru_in) or die "Failed to close ${rule_folder}/PIRSR.uru: $!\n";
    };

    open (my $uru_out, '>', "$self->{data_folder}/sr_uru.json" ) or die "Failed to open $self->{data_folder}/sr_uru.json file: $!\n";

    my $json_uru = to_json( $rules, { pretty => 1 } );
    print $uru_out $json_uru;

    close($uru_out) or die "Failed to close $self->{data_folder}/sr_uru.json\n";

    return 1;
}






sub _parse_rules {
    my ($block) = @_;

    my @rule = split(/\n/,$block);

    my $rule_hash = {};

    while (my $line = shift @rule) {

        if ($line =~ /\A(\*\*|Comments|XX|end case|\/\/)/ ) {
        } elsif ($line =~ /\AAC\s+(.+);/ ) {
            $rule_hash->{'AC'} = $1;
        } elsif ($line =~ /\ADC\s+(.+);/ ) {
            $rule_hash->{'DC'} = $1;
        } elsif ($line =~ /\ATR\s+(.+);/ ) {
            $rule_hash->{'TR'} = $1;
        } elsif ($line =~ /\ASize:\s+(.+);/ ) {
            $rule_hash->{'Size'} = $1;
        } elsif ($line =~ /\ARelated:\s+(.+);/ ) {
            $rule_hash->{'Related'} = $1;
        } elsif ($line =~ /\AScope:/ ) {
            while ($rule[0] =~ /\A\s+(.*)/ ) {
                push( @{$rule_hash->{'Scope'}}, $1);
                shift @rule;
            }
        } elsif ($line =~ /\Acase\s\<Feature:(\S+)\>\z/) {
            my $feature->{'model'} = $1;
            my $group;

            while ($rule[0] !~ /\Aend case/ ) {
                $line = shift @rule;

                if ($line =~ /\AFT\s+From\: (\S+)\z/) {
                    $feature->{'from'} = $1;

                } elsif ($line =~ /\AFT\s{3}(\S+)\s+(\w+)\s+(\w+)(\s+(.*))?\z/) {

                        undef $group;
                        $group->{'label'} = $1;
                        $group->{'start'} = $2;
                        $group->{'end'} = $3;
                        $group->{'desc'} = $5;

                } elsif ($line =~ /\AFT\s+Group\:\s+(\d+)\;\s+Condition\:\s+(.*)\z/) {

                    $group->{'group'} = $1;
                    $group->{'condition'} = $2;

                    push( @{$rule_hash->{'Groups'}->{$group->{'group'}}}, $group);

                } elsif ($line =~ /\AFT\s+(.*)\z/) {

                    $group->{'desc'} .= " $1";

                } else {
                    die "Failed to parse Group line: \"$line\"\n";
                }
            }
            $rule_hash->{'Feature'} = $feature;

        } elsif ($line =~ /\Acase\s\<Feature:(\S+)\>.+/){

            while ($rule[0] !~ /\Aend case/ ) {
                $line = shift @rule;
            }

        } else {
            die "Failed to parse rules line: \"$line\"\n";
        }
    }

    return $rule_hash;
}






sub align_template {
    my ($self, $rule) = @_;


    my $prot_id = $rule->{'Feature'}->{'from'};

    my $fasta_file = "$self->{data_folder}/sr_tp/${prot_id}.fa";

    open (my $in, '<', "$fasta_file") or die "Failed top open $fasta_file file: $!\n";

    my ($prot_model, $prot_seq);

    my $fasta = <$in>;

    if ($fasta =~ m/.*\t(.*)\n(.*)\n?/) {
        $prot_model = $1;
        $prot_seq = $2;
    }

    my $stockholm = `$self->{hmmalign} $self->{data_folder}/sr_hmm/${prot_model}.hmm ${fasta_file}`;

    my $alignment_str;
    while ($stockholm =~ /\n${prot_id}\s+([^\n]*)/g) {
        $alignment_str .= $1;
    }

    my $alignment = align_map($alignment_str);

    foreach my $grp (keys %{$rule->{'Groups'}}) {

        for my $pos (0 .. $#{$rule->{'Groups'}->{$grp}} ) {

            if ($rule->{'Groups'}->{$grp}->[$pos]->{'start'} eq 'Nter') {
                warn "rule $rule->{AC}, group $grp, pos $pos: Start is Nter.\n" if ($self->verbose);

                my $offset = get_ter_offset($rule->{'Groups'}->{$grp}->[$pos]->{condition});

                $rule->{'Groups'}->{$grp}->[$pos]->{hmmStart} = $alignment->[$rule->{'Groups'}->{$grp}->[$pos]->{'end'}] - $offset;

            } else {
                $rule->{'Groups'}->{$grp}->[$pos]->{hmmStart} = $alignment->[$rule->{'Groups'}->{$grp}->[$pos]->{'start'}];
            }

            # process end
            if ($rule->{'Groups'}->{$grp}->[$pos]->{'end'} eq 'Cter') {
                warn "rule $rule->{AC}, group $grp, pos $pos: End is Cter.\n" if ($self->verbose);

                my $offset = get_ter_offset($rule->{'Groups'}->{$grp}->[$pos]->{condition});

                $rule->{'Groups'}->{$grp}->[$pos]->{hmmEnd} = $alignment->[$rule->{'Groups'}->{$grp}->[$pos]->{'start'}] + $offset;

            } else {
                $rule->{'Groups'}->{$grp}->[$pos]->{hmmEnd} = $alignment->[$rule->{'Groups'}->{$grp}->[$pos]->{'end'}];
            }

        }

    }

    return $rule;
}





# returns array of seq->hmm positions for a given alignment string
sub align_map {
    my ($alignment) = @_;

    my $seq_pos = 1;
    my $hmm_pos = 1;
    my @map;

    foreach my $pos (split(//, $alignment)) {
        #print STDERR "$pos";
        if ($pos eq "-") {
            #delete position
            $hmm_pos++;
        } elsif ($pos eq ".") {
            #Alignment insert, skip
        } elsif ($pos =~ /[A-Z]/) {
            #Match position
            $map[$seq_pos] = $hmm_pos;
            $seq_pos++;
            $hmm_pos++;
        } elsif ($pos =~ /[a-z]/) {
            #hmm insert pos

            # non-match state
            # $map[$seq_pos] = '-';
            # Use valid hmm pos
            $map[$seq_pos] = $hmm_pos;
            $seq_pos++;
        }
    }

    return \@map;
}





# When hmmStart/hmmEnd is Nter/Cter, calculate offset for termination based on how many bases long the condition is
sub get_ter_offset {
    my ($cond_match) = @_;

    my $offset = -1;

    # transform the condition in a string of char counts
    $cond_match =~ s/-//g;
    $cond_match =~ tr/\(\)x/\{\}\./;
    $cond_match =~ s/\[\w+\]/1/;
    $cond_match =~ s/[A-Z]/1/g;
    $cond_match =~ s/\.\{//g;
    $cond_match =~ s/\}//g;

    foreach my $char (split //, $cond_match) {
      $offset += $char;
    }

    return $offset;

}







sub run_query {
    my ($self, $query_file) = @_;

    my %query_rules;


    my (undef, $out) = tempfile(
        DIR => '/tmp',
        UNLINK => 1
    );

    my $hmm_library = $self->{data_folder} . '/sr_hmm_all';


    open(my $in, '<', "$self->{data_folder}/sr_uru.json") or die "Failed to open $self->{data_folder}/sr_uru.json file: $!\n";

    my $json_string = do { local $/; <$in> };

    close($in) or die "Failed to close $self->{data_folder}/sr_uru.json: $!\n";

    my $rule_library = from_json($json_string);


    my $cmd = "$self->{hmmscan} --cpu $self->{cpus} --notextw -o $out $hmm_library $query_file";

    system($cmd) && die qq(Failed to run "$cmd");

    my $res_obj = Bio::Pfam::HMM::HMMResultsIO->new->parseMultiHMMER3($out);

    foreach my $query_match (@{$res_obj}) {

        my $query_id = $query_match->{'seqName'};
        next if !$query_id;

        $query_rules{$query_id} = {} unless $query_rules{$query_id};

        foreach my $target_match (@{$query_match->{'units'}}) {
            # loop over alignments
            my $rule_id = $target_match->{'name'};
            if ($query_rules{$query_id}{$rule_id}) {
                next;
            };

            my $rule = $rule_library->{$rule_id};

            my $tname = $target_match->{name}; # the sequence ID/accession
            my $hmm_seq = $target_match->{hmmalign}{hmm};
            my $query_seq = $target_match->{hmmalign}{seq};
            my $hmm_from = $target_match->{hmmFrom};
            my $seq_from = $target_match->{seqFrom};
            my $map = map_hmm_to_seq($hmm_from, $hmm_seq, $query_seq);

            foreach my $grp (sort keys %{$rule->{'Groups'}}) {

                my $pass_count = 0;

                foreach my $pos (0 .. $#{$rule->{'Groups'}->{$grp}} ) {

                    my $condition = $rule->{'Groups'}->{$grp}->[$pos]->{'condition'};

                    $condition =~ s/-//g;
                    $condition =~ tr/\(\)x/\{\}\./;

                    my $condition_regex = qr/\A${condition}\z/;

                    my $rule_hmm_start = $rule->{'Groups'}->{$grp}->[$pos]->{'hmmStart'};
                    my $rule_hmm_end = $rule->{'Groups'}->{$grp}->[$pos]->{'hmmEnd'};

                    my $seq_start = $map->[$rule_hmm_start];
                    my $seq_end = $map->[$rule_hmm_end];

                    $query_seq =~ s/-//g;
                    my $target_seq = '';
                    $target_seq = substr($query_seq, $seq_start, $seq_end - $seq_start + 1) unless (!defined $seq_start || !defined $seq_end);

                    if (!$target_seq) {
                        warn "Target sequence out of alignment borders for query ${query_id} on hit ${tname}\n" if ($self->verbose);
                    }

                    $pass_count += ($target_seq =~ /${condition_regex}/);

                    # # debug failures
                    # if ($target_seq =~ /${condition_regex}/) {
                    #     ### TARGET MATCH
                    #     my $condition = $rule->{'Groups'}->{$grp}->[$pos]->{'condition'};
                    #     ### $condition
                    #     ### $condition_regex
                    #     ### $target_seq
                    # }
                    # else {
                    #     ### TARGET NO MATCH
                    #     ### $rule
                    #     ### $condition
                    #     ### $condition_regex
                    #     ### $target_seq
                    # }

                }

                if (@{$rule->{'Groups'}->{$grp}} == $pass_count) {

                    $query_rules{$query_id}{$rule_id}{'aliAcc'} = $target_match->{'aliAcc'};
                    $query_rules{$query_id}{$rule_id}{'domEvalue'} = $target_match->{'domEvalue'};
                    $query_rules{$query_id}{$rule_id}{'envFrom'} = $target_match->{'envFrom'};
                    $query_rules{$query_id}{$rule_id}{'envTo'} = $target_match->{'envTo'};
                    $query_rules{$query_id}{$rule_id}{'hmmFrom'} = $target_match->{'hmmFrom'};
                    $query_rules{$query_id}{$rule_id}{'hmmTo'} = $target_match->{'hmmTo'};
                    $query_rules{$query_id}{$rule_id}{'seqFrom'} = $target_match->{'seqFrom'};
                    $query_rules{$query_id}{$rule_id}{'seqTo'} = $target_match->{'seqTo'};
                    $query_rules{$query_id}{$rule_id}{'evalue'} = $target_match->{'evalue'};
                    $query_rules{$query_id}{$rule_id}{'hmmalign'} = $target_match->{'hmmalign'};

                    $query_rules{$query_id}{$rule_id}{'Scope'} = $rule->{'Scope'};

                    push @{$query_rules{$query_id}{$rule_id}{'RuleSites'}}, @{$rule->{'Groups'}->{$grp}};

                }
                # # debug failures
                # else {
                #     ## NO PASS!
                #     ## $rule
                #     # die;
                # }

            }

        }

    }

    return \%query_rules;
}



# map base positions from alignment, from query HMM coords to (ungapped) target sequence coords
sub map_hmm_to_seq {
    my ($hmm_pos, $hmm, $seq) = @_;

    # so we can extract residues direct from the alignment; need to add the offset later
    my $seq_pos = 0;
    my @map;

    for (my $i = 0; $i < length $hmm; $i++) {
        $map[$hmm_pos] = $seq_pos;
        $hmm_pos++ if substr($hmm, $i, 1) ne '.';
        $seq_pos++ if substr($seq, $i, 1) ne '-';
    }

    return \@map;
}




__PACKAGE__->meta->make_immutable;

1;

__END__