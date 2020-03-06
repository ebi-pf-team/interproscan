package PIRSF;

use strict;
use warnings;
use File::Temp qw/tempfile/;

use Smart::Comments;



sub checkHmmFiles{
  my ($sf_hmm, $path) = @_;

  if ( $path =~ /\S+/ && $path !~ /\/^/ ) {
    $path .= '/';
  }
  $path .= 'hmmpress';

  foreach my $ext (qw(.h3p .h3m .h3f .h3i)) {
    if (!-e $sf_hmm.$ext) {
      #Looks like the hmm database is not pressed
      warn "Running hmmpress ($path) on $sf_hmm\n";
      system("$path $sf_hmm") and die "Could not run hmmpress!\n";
      last;
    }
  }

  return 1;
}

sub read_pirsf_dat {
  my ($pirsf_dat) = @_;
  my ($data, $child);

  open (my $in, '<', $pirsf_dat) or die "Failed top open $pirsf_dat file:[$!]\n";

  my $code = do {
    local $/ = '>';
    while (my $block = <$in>) {
      # PIRSFXX child: PIRSFXX PIRSFXX
      # name
      # 5 float values
      # BLAST: Yes|No
      if ($block =~
        m/
          (\w+)(\schild:\s)?\s?(.*?)\n
          ([^\n]+)\n
          ([^\s]+)\s+([^\s]+)\s+([^\s]+)\s+([^\s]+)\s+([^\s]+)\n
          BLAST:\s+(\w+)
        /xms ) {

        my $acc = $1;
        my $child_string = $3;

        if ($child_string) {

          my %children = map{ $_ => 1 } (split /\s/, $child_string);
          $data->{$acc}->{children} = \%children;

          foreach my $child_id (keys %children) {
            $child->{$child_id} = $acc;
          }

        }
        $data->{$acc}->{name} = $4;

        $data->{$acc}->{meanL} = $5;
        $data->{$acc}->{stdL}  = $6;
        $data->{$acc}->{minS}  = $7;
        $data->{$acc}->{meanS} = $8;
        $data->{$acc}->{stdS}  = $9;

        if ($10 eq 'No') {
          $data->{$acc}->{blast} = 0;
        } else {
          $data->{$acc}->{blast} = 1;
        }
      } else {
        warn "Unrecognised PIRSF data entry: \"$block\"\n" unless ($block eq '>');
      }
    }

  };

  close($in) or die "Failed to close $pirsf_dat\n";

  #return data structure
  return ($data, $child);
}

sub run_hmmer {
  my ($hmmer_path, $mode, $cpus, $sf_hmm, $fasta_file, $tmpdir) = @_;
  ## $fasta_file

  my (undef, $filename) = tempfile(
    DIR => $tmpdir,
    UNLINK => 1
  );

  my $err_code = system("${hmmer_path}/$mode --cpu $cpus -o /dev/null --domtblout $filename -E 0.01 --acc $sf_hmm $fasta_file");

  if ($err_code) {
    die "\"${hmmer_path}/$mode --cpu $cpus -o /dev/null --domtblout $filename -E 0.01 --acc $sf_hmm $fasta_file\" failed: $?";
  }


## $filename

  return $filename;

}


sub read_fasta {
  my ($infile) = @_;

  my $target_hash;

  open(my $fasta_fh,'<', $infile) or die "Failed to open $infile:[$!]\n";

  while (my $line=<$fasta_fh>) {
    chomp $line;
    if ($line=~/^\>(\S+)/){
      $target_hash->{$1} = {};
    }
  }
  close($fasta_fh) or die "Failed to close $infile filehandle:[$!]\n";

  return $target_hash;
}


sub read_dom_input {
  my ($dominput) = @_;

 ## $dominput

  my @results;
  # open(T, '<', "$dominput") or open(T, '<', \$dominput) or  die "Failed to open table\n";
  open(my $dom_fh, '<', $dominput) or die "Failed to open table file $dominput\n";
  while(my $line = <$dom_fh>){
    ## $line
    next if( substr($line, 0, 1) eq '#');
    chomp $line;
    ## $line
    push(@results, $line);
  }
  close($dom_fh) or die "Failed to close file $dominput\n";

  return \@results;
}




sub process_results {
  my ($results, $pirsf_data, $children, $hmmer_mode) = @_;


## $results

  # deal with the case of no hits
  if (!scalar @{$results}) {
    return {};
  }


  my %promote;
  my $store; #Use this to store previous rows
  my $matches = {};
## $matches
  my ($pirsf_acc, $seq_acc, @keep_row);
  ROW:
  foreach my $row (@{$results}) {

    my @row = split(/\s+/, $row, 24);
    if($hmmer_mode eq 'hmmsearch'){
      my @reorder = @row[0..5];
      $row[0] = $reorder[3];
      $row[1] = $reorder[4];
      $row[2] = $reorder[5];
      $row[3] = $reorder[0];
      $row[4] = $reorder[1];
      $row[5] = $reorder[2];
    }
    if(defined($pirsf_acc) and defined($seq_acc)){
      if( ($row[1] ne $pirsf_acc) or ($row[3] ne $seq_acc)){
        #The sequence or the profile accession has changed.
        my @pRow = @keep_row;
        $store = process_hit(\@pRow, $children, $store, \%promote, $pirsf_data, $matches);
        @keep_row = ();
      }else{
       push(@keep_row, \@row);
       next ROW;
      }
    }

    $pirsf_acc = $row[1];
    $seq_acc = $row[3];
    push(@keep_row, \@row);
  }
  $store = process_hit(\@keep_row, $children, $store, \%promote, $pirsf_data, $matches);

  return $matches;
}

sub process_hit {
   my ($rows, $children, $store, $promote, $pirsf_data, $matches) = @_;
   #Just get the key bits of information out.
   my($pirsf_acc, $seq_acc, $seq_leng, $seq_start, $seq_end,
       $hmm_start, $hmm_end) = @{$rows->[0]}[1,3,5,17,18,15,16];
  my $score = 0;
  #Now loop over all rows that are left, check that we do not have a smaller start or larger end for the sequence/hmm.
  foreach my $row (@{$rows}){
    $score += $row->[13];

    # $seq_start = ($row->[17] < $seq_start ? $row->[17] : $seq_start); 
    # $seq_end   = ($row->[18] > $seq_end   ? $row->[18] : $seq_end); 
    # $hmm_start = ($row->[15] < $hmm_start ? $row->[15] : $hmm_start); 
    # $hmm_end   = ($row->[16] > $hmm_end   ? $row->[16] : $hmm_end);

    if ($row->[17] < $seq_start && $row->[15] < $hmm_start) {
      $seq_start = $row->[17];
      $hmm_start = $row->[15];
    }
    if ($row->[18] > $seq_end && $row->[16] > $hmm_end) {
      $seq_end = $row->[18];
      $hmm_end = $row->[16];
    }

  }

  #Overall length
  my $ovl = (($seq_end - $seq_start) + 1)/$seq_leng;
## $seq_end
## $seq_start
## $seq_leng
## $ovl

  my $r   = (($hmm_end - $hmm_start) + 1)/ (($seq_end - $seq_start) + 1); #Ratio over coverage of sequence and profile HMM.
## $hmm_end
## $hmm_start
## $seq_end
## $seq_start
## $r
  # if(! defined($store->{$seq_acc})){
  #   $store = {};
  #   $store->{$seq_acc}={};
  # }


  #Length deviation
  my $ld = abs($seq_leng - $pirsf_data->{$pirsf_acc}->{meanL});

## $r
## $score
## $ld

## $pirsf_acc
## $children


  if($children->{$pirsf_acc}){
    #If a sub-family, process slightly differently. Only consider the score.
    if($r > 0.67 && $score >=$pirsf_data->{$pirsf_acc}->{minS}){
## hit score for child
      #No work out which family we may need to promote and check to see if
      #we have seen it nor not
      my $parent = $children->{$pirsf_acc};
      ## $parent
      ## $store
      ## $promote
      if($store->{$seq_acc}->{$parent}){
        ## FIRST IF
        $matches->{$seq_acc}->{$parent} = $store->{$seq_acc}->{$parent};
      }else{
        ## SECOND IF
        $promote->{$seq_acc.'-'.$parent} = 1;
      }
      #Store the sub family match.
      $matches->{$seq_acc}->{$pirsf_acc}->{score}=$score;
      $matches->{$seq_acc}->{$pirsf_acc}->{data}=$rows;
      ## $promote
      ## $matches
    }
  }elsif($r > 0.67 && $ovl>=0.8 && 
          ($score >=$pirsf_data->{$pirsf_acc}->{minS}) && 
          ($ld<3.5*$pirsf_data->{$pirsf_acc}->{stdL} || $ld < 50 ) ){ 
    #Looks like everything passes the threshold of length, score and standard deviations of length.
    $matches->{$seq_acc}->{$pirsf_acc}->{score}=$score;
    $matches->{$seq_acc}->{$pirsf_acc}->{data}=$rows;
  }elsif( defined ( $promote->{$seq_acc.'-'.$pirsf_acc} ) ){
    #Do we promote this?
    $matches->{$seq_acc}->{$pirsf_acc}->{score}=$score;
    $matches->{$seq_acc}->{$pirsf_acc}->{data}=$rows;
  }else{
    ## last if 
    #Store for later in case there is a subfamily match.
    $store->{$seq_acc}->{$pirsf_acc}->{score}=$score;
    $store->{$seq_acc}->{$pirsf_acc}->{data}=$rows;
    ## $store
    ## $seq_acc
    ## $pirsf_acc
    ## $score
    ## $rows
  }
        ## process hit
        ## $matches
  return $store;
}

sub post_process {
  my($matches, $pirsf_data) = @_;
  ## $matches
  my $bestMatch; 
  #Sort all matches and find the smallest evalue. 
  foreach my $seq (keys %$matches){
    $bestMatch->{$seq} = {};#This enables us to capure no matches.

    #If there are matches.....sort them on evalue.
    my @matchesSort = sort{ $matches->{$seq}->{$b}->{score} <=> 
                            $matches->{$seq}->{$a}->{score}}(keys %{$matches->{$seq}});
    foreach my $pirsf_acc (@matchesSort){
      next if($pirsf_acc =~ /^PIRSF5/); #Ignore sub-families 

      $bestMatch->{$seq}->{sf} = $matches->{$seq}->{$pirsf_acc}->{data};
        
      #Now see if this entry has sub-families
      if(defined($pirsf_data->{$pirsf_acc}->{children})){
         foreach my $pirsf_sub (@matchesSort){
            if($pirsf_data->{$pirsf_acc}->{children}->{$pirsf_sub}){
              $bestMatch->{$seq}->{subf} = $matches->{$seq}->{$pirsf_sub}->{data};
              last;
            }
         }
      }
      
      last; #We only want the best match!
    }
  }
  return $bestMatch;
}



sub print_output {
  my($bestMatch, $pirsf_data, $outfmt) = @_;
   
#For PIRSF outfmt
#Query sequence: A0B5N6  matches PIRSF006429: Predicted glutamate synthase, large subunit domain 2 (FMN-binding)
#   #    score  bias  c-Evalue  i-Evalue hmmfrom  hmm to    alifrom  ali to    envfrom  env to     acc
#   1 !  511.7   0.1  1.5e-156  4.4e-154      24     495 ..      16     494 ..       2     497 .] 0.88
# and matches Sub-Family PIRSF500061: Predicted glutamate synthase, large subunit domain 2
#   #    score  bias  c-Evalue  i-Evalue hmmfrom  hmm to    alifrom  ali to    envfrom  env to     acc
#   1 !  652.6   0.0  6.7e-200  1.6e-198       6     474 ..       5     496 ..       1     497 [] 0.95


#Query sequence: A4YCN9 No Match
 

## $bestMatch
## $pirsf_data

## $bestMatch

   if(lc($outfmt) eq 'pirsf'){ 
    foreach my $seq (sort keys %$bestMatch){
      print "Query sequence: $seq ";
      if(exists($bestMatch->{$seq}->{sf})){
        my $sf_data = $bestMatch->{$seq}->{sf};
        print "matches $sf_data->[0]->[1]: $pirsf_data->{$sf_data->[0]->[1]}->{name}\n";
        _print_report($sf_data);
        if($bestMatch->{$seq}->{subf}){
          my $sub_data = $bestMatch->{$seq}->{subf};
          print " and matches Sub-Family $sub_data->[0]->[1]: $pirsf_data->{$sub_data->[0]->[1]}->{name}\n";
          _print_report($sub_data);
        }    
      }else{
        print "No match\n";
      }
    } 
  }elsif(lc($outfmt) eq 'i5'){
    foreach my $seq (sort keys %$bestMatch){
       if(exists($bestMatch->{$seq}->{sf})){
        my $sf_data = $bestMatch->{$seq}->{sf};
        _print_i5($sf_data, $seq);
        if($bestMatch->{$seq}->{subf}){
          my $sub_data = $bestMatch->{$seq}->{subf};
          _print_i5($sub_data, $seq);
        }
      }
    }
  }
  
}

sub _print_report {
  my($all_data) = @_;
  
  print sprintf("%4s  %7s%6s%10s%10s%8s%8s   %8s%8s   %8s%8s   %5s\n" , 
                '#', 'score', 'bias', 'c-Evalue', 'i-Evalue', 'hmmfrom', 
                'hmm to', 'alifrom', 'ali to', 'envfrom', 'env to', 'acc' );
  
  my $cnt = 1;
  foreach my $data (@{$all_data}){  
    my ($hmmMatch, $seqMatch, $envMatch) = _matchBounds($data);
    print sprintf("%4s%2s%7s%6s%10s%10s%8s%8s%3s%8s%8s%3s%8s%8s%3s%5s\n" , 
                $cnt,'!', $data->[13], $data->[14], $data->[11], $data->[12], $data->[15], $data->[16], $hmmMatch,
                $data->[17], $data->[18], $seqMatch, $data->[19], $data->[20], $envMatch, $data->[21]);   
    $cnt++;
  }
}

sub _print_i5 {
  my ($all_data, $seq) = @_;  
 
  foreach my $data (@$all_data){
    my ($hmmMatch, $seqMatch, $envMatch) = _matchBounds($data);
  
    my $line = join("\t", 
                        $data->[20], #LOCATION_END
                        $data->[19], #LOCATION_START
                        $data->[1],  #MODEL_ID
                        $seq,        #SEQUENCE_ID
                        $data->[6],  #EVALUE
                        $hmmMatch,   #HMM_BOUNDS
                        $data->[18], #HMM_END
                        $data->[17], #HMM_START
                        $data->[13], #LOCATION_SCORE
                        $data->[7],  #SCORE
                        $data->[14], #DOMAIN_BIAS
                        $data->[11], #DOMAIN_CE_VALUE
                        $data->[12], #DOMAIN_IE_VALUE
                        $data->[20], #ENVELOPE_END
                        $data->[19], #ENVELOPE_START
                        $data->[21], #EXPECTED_ACCURACY
                        $data->[8]); #FULL_SEQUENCE_BIAS
    print "$line\n";
  }
}

sub _matchBounds{
  my ($data) = @_;
  my ($hmmMatch, $seqMatch, $envMatch); 
  $hmmMatch .= $data->[15] == 1 ? "[" : ".";
  $hmmMatch .= $data->[16] == $data->[2] ? "]" : ".";
  $seqMatch .= $data->[17] == 1 ? "[" : ".";
  $seqMatch .= $data->[18] == $data->[5] ? "]" : ".";
  $envMatch .= $data->[19] == 1 ? "[" : ".";
  $envMatch .= $data->[20] == $data->[5] ? "]" : ".";

  return($hmmMatch, $seqMatch, $envMatch);
}
=head

#BLAST search
if ($SF && $blast{$SF})
{
  @blast_tmp=`$blastall -p blastp -F F -e 0.0005 -b 300 -v 300 -d $sfseq -i $infile -m 8`; 
  open (IN,$sftb);
  while($line=<IN>)
   { chop $line;
     ($a,$b)=(split / /,$line)[0,1];
     $SFn{$a}=$b;
   }
  close IN;

  %n=();
  foreach $x (@blast_tmp)
  { 
  $y=(split /\s+/,$x)[1]; 
  if (index($y,"-")>0)
   { $n{$y}++;
     if ($n{$y}<2) 
      {$sf=(split /-/,$y)[1];
       if (!$sf1){$sf1=$sf;}
       if ($sf eq $sf1 ){ $hit++; }
      }
   }
  }

 foreach $x ( keys %n)
  { if ($hit>9 || ($SFn{$sf1} && $hit/$SFn{$sf1}>0.33334))
    { $bl_sf="PIR$sf1";}
  }
if ($bl_sf ne $SF) {$SF="";$SF_subf="";}
}
=cut

1;
