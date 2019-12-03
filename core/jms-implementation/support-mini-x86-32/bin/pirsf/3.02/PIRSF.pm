package PIRSF;

use strict;
use warnings;
use File::Temp qw/tempdir/;

sub checkHmmFiles{
  my ($sf_hmm, $path) = @_;
  if($path and $path =~ /\S+/){
    $path.='/' if($path !~ /\/^/);
  }
  $path .= 'hmmpress';
  foreach my $ext (qw(.h3p .h3m .h3f .h3i)){
    if(!-e $sf_hmm.$ext){
      #Looks like the hmm database is not pressed
      warn "Running hmmpress ($path) on $sf_hmm\n";
      system("$path $sf_hmm") and die "Could not run hmmpress!\n";;
      last;
    }
  }
  return 1;
}

sub read_pirsf_dat {
  my ($pirsf_dat) = @_;

  my ($child, $data);
  $data = {};
  open(IN, '<', $pirsf_dat) or die "Failed top open $pirsf_dat file:[$!]\n";
  while(my $line = <IN>){
    chomp($line);
    my $acc; 
    if($line =~ /^\>/){
      my $rest;
      ($acc, $rest) = split(/\s/, $line, 2);
        substr($acc, 0, 1, ''); #Remove the leading '>'
      if($rest){
        substr($rest, 0, 7, ''); #Remove the leading 'child: '
        %{$data->{$acc}->{children}} = map{ $_ => 1 }(split /\s/,$rest); 
        foreach my $c (keys %{$data->{$acc}->{children}}){
          $child->{$c} = $acc;
        }
      }
        
      $line=<IN>;
      chomp $line;
      $data->{$acc}->{name} = $line;

      $line=<IN>;
      chomp $line;
      ($data->{$acc}->{meanL},
       $data->{$acc}->{stdL}, 
       $data->{$acc}->{minS}, 
       $data->{$acc}->{meanS}, 
       $data->{$acc}->{stdS}) = split (/ /,$line);
      
      $line=<IN>; 
      chomp $line;
      if ($line=~/Yes/) { 
        $data->{$acc}->{blast} = 1; 
      }else{
        $data->{$acc}->{blast} = 0;
      }
    }else{
      warn "Unrecognised line $line\n";
    }
    }
    #Clean up
    close(IN) or die "Failed to close $pirsf_dat\n";
  #return data structure
  return ($data, $child);
}
#This is not actually need, should be in the HMMER output (RDF).
sub read_fasta { 
  my ($infile, $matches) = @_;

  open(IN,'<', $infile) or die "Failed to open $infile:[$!]\n";

  while(my $line=<IN>){ 
    chomp $line;
    if ($line=~/^\>(\S+)/){
      $matches->{$1} = {};
    }
  } 
  close IN or die "Failed to close $infile filehandle:[$!]\n";

  return 1;
}

sub run_hmmer {
  my ($infile, $domtblout_file, $sf_hmm, $pirsf_data, $matches, $children, $path, $cpu, $hmmer, $i5_tmpdir) = @_;
  #Change to using table output.
  #system("$hmmscan --domtblout table -E 0.01 --acc $sf_hmm $infile")
  my $dir  = tempdir( CLEANUP => 1 , DIR=> $i5_tmpdir);
  if($path and $path =~ /\S+/){
    $path.='/' if($path !~ /\/^/);
  }

  my %promote;
  my $store; #Use this to store previous rows
  if($cpu and $cpu =~ /\d+/){
    $cpu = "--cpu $cpu";
  }

  $path .= "$hmmer";


  #Run HMM search for full-length models and get information
  # we run this outside this script and just get the domtblout
  #my @sf_out=` $path $cpu --domtblout $dir/table -E 0.01 --acc $sf_hmm $infile`;

  my @results;
  open(T, '<', "$domtblout_file") or die "Failed to open table\n";
  while(<T>){
    next if( substr($_, 0, 1) eq '#');
    chomp;
    push(@results, $_);
  }
  close(T);

  # deal with the case of no hits
  if (@results <= 0) {
    return 0;
  }

  my ($pirsf_acc, $seq_acc, @keep_row);
  ROW:
  for (my $i = 0; $i <= $#results; $i++){
    
    my @row = split(/\s+/, $results[$i], 24);
    if($hmmer eq 'hmmsearch'){
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
        process_hit(\@pRow, $children, $store, \%promote, $pirsf_data, $matches);
        @keep_row = ();
      }else{
       push(@keep_row, \@row);
       next ROW;
      }
    }
    
    $pirsf_acc = $row[1]; # 
    $seq_acc = $row[3];
    push(@keep_row, \@row);
  }
  process_hit(\@keep_row, $children, $store, \%promote, $pirsf_data, $matches);
  return 1;
}

sub process_hit {
   my ($rows, $children, $store, $promote, $pirsf_data, $matches) = @_;
   #Just get the key bits of information out.
   my($pirsf_acc, $seq_acc, $seq_leng, $seq_start, $seq_end,
       $hmm_start, $hmm_end) = @{$rows->[0]}[1,3,5,15,16,17,18];
  my $score = 0;
  #Now loop over all rows that are left, check that we do not have a smaller start or larger end for the sequence/hmm.
  foreach my $row (@{$rows}){
    $score += $row->[13];
    $seq_start = ($row->[17] < $seq_start ? $row->[17] : $seq_start); 
    $seq_end   = ($row->[18] > $seq_end   ? $row->[18] : $seq_end); 
    $hmm_start = ($row->[15] < $hmm_start ? $row->[15] : $hmm_start); 
    $hmm_end   = ($row->[16] > $hmm_end   ? $row->[16] : $hmm_end);
  }

  #Overall length
  my $ovl = (($seq_end - $seq_start) + 1)/$seq_leng;
  my $r   = (($hmm_end - $hmm_start) + 1)/ (($seq_end - $seq_start) + 1); #Ratio over coverage of sequence and profile HMM.
  if(! defined($store->{$seq_acc})){
    $store = {};
    $store->{$seq_acc}={};
  }

  #Length deviation
  my $ld = abs($seq_leng - $pirsf_data->{$pirsf_acc}->{meanL});
  if($children->{$pirsf_acc}){
    #If a sub-family, process slightly differently. Only consider the score.
    if($r > 0.67 && $score >=$pirsf_data->{$pirsf_acc}->{minS}){
        
      #No work out which family we may need to promote and check to see if
      #we have seen it nor not
      my $parent = $children->{$pirsf_acc};
      if($store->{$seq_acc}->{$parent}){
        $matches->{$seq_acc}->{$parent} = $store->{$seq_acc}->{$parent};
      }else{
        $promote->{$seq_acc.'-'.$parent} = 1;
      }
      #Store the sub family match.
      $matches->{$seq_acc}->{$pirsf_acc}->{score}=$score;
      $matches->{$seq_acc}->{$pirsf_acc}->{data}=$rows;
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
    #Store for later in case there is a subfamily match.
    $store->{$seq_acc}->{$pirsf_acc}->{score}=$score;
    $store->{$seq_acc}->{$pirsf_acc}->{data}=$rows;
  }
  return 1;
}

sub post_process {
  my($matches, $pirsf_data) = @_;
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
              $bestMatch->{$seq}->{sub} = $matches->{$seq}->{$pirsf_sub}->{data};
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
 
   if(lc($outfmt) eq 'pirsf'){ 
    foreach my $seq (keys %$bestMatch){
      print "Query Sequence: $seq ";
      if(exists($bestMatch->{$seq}->{sf})){
        my $sf_data = $bestMatch->{$seq}->{sf};
        print "matches $sf_data->[0]->[1]: $pirsf_data->{$sf_data->[0]->[1]}->{name}\n";
        _print_report($sf_data);
        if($bestMatch->{$seq}->{sub}){
          my $sub_data = $bestMatch->{$seq}->{sub};
          print " and matches Sub-Family $sub_data->[0]->[1]: $pirsf_data->{$sub_data->[0]->[1]}->{name}\n";
          _print_report($sub_data);
        }    
      }else{
        print "No match\n";
      }
    } 
  }elsif(lc($outfmt) eq 'i5'){
    foreach my $seq (keys %$bestMatch){
       if(exists($bestMatch->{$seq}->{sf})){
        my $sf_data = $bestMatch->{$seq}->{sf};
        _print_i5($sf_data, $seq);
        if($bestMatch->{$seq}->{sub}){
          my $sub_data = $bestMatch->{$seq}->{sub};
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
