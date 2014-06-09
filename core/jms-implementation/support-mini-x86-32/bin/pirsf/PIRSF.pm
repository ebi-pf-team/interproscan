package PIRSF;

use strict;
use warnings;
use File::Temp qw/tempdir/;

sub checkHmmFiles{
  my ($sf_hmm, $path) = @_;
  if($path and $path =~ /\S+/){
    $path.='/' if($path !~ /\/^/);
  }
  foreach my $ext (qw(.h3p .h3m .h3f .h3i)){
    if(!-e $sf_hmm.$ext){
      #Looks like the hmm database is not pressed
      warn "Running hmm press on $sf_hmm\n";
      system("hmmpress $sf_hmm") and die "Could not run hmmpress!\n";;
      last;
    }
  }
  return 1;
}

sub read_pirsf_dat {
  my ($pirsf_dat) = @_;

  my $data;
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
  return $data;
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

sub run_hmmscan {
  my ($infile, $sf_hmm, $pirsf_data, $matches, $path, $cpu) = @_;
  #Change to using table output.
  #system("$hmmscan --domtblout table -E 0.01 --acc $sf_hmm $infile")
  my $dir  = tempdir( CLEANUP => 1 );  
  if($path and $path =~ /\S+/){
    $path.='/' if($path !~ /\/^/);
  }

  if($cpu and $cpu =~ /\d+/){
    $cpu = "--cpu $cpu";
  }
  $path .= 'hmmscan';
  #Run HMM search for full-length models and get information
  my @sf_out=` $path $cpu --domtblout $dir/table -E 0.01 --acc $sf_hmm $infile`;

  open(T, '<', "$dir/table") or die "Failed to open table\n";
  while(<T>){
    chomp;
    next if( substr($_, 0, 1) eq '#');
    my @row = split(/\s+/, $_, 24);
    my $pirsf_acc = $row[1]; # 
    my $seq_acc = $row[3];
    my $seq_leng = $row[5];
    my $seq_start = $row[17];
    my $seq_end = $row[18];
    my $score = $row[13];
    my $evalue = $row[12]; #i-Evalue for the domain
    #Overall length
    my $ovl = ($seq_end - $seq_start + 1)/$seq_leng;
    
    #Length deviation
    my $ld = abs($seq_leng - $pirsf_data->{$pirsf_acc}->{meanL});
    if($ovl>=0.8 && ($score >=$pirsf_data->{$pirsf_acc}->{minS}) && ($ld<3.5*$pirsf_data->{$pirsf_acc}->{stdL} ||$ld< 50 )){ 
      $matches->{$seq_acc}->{$pirsf_acc}->{evalue}=$evalue;
      $matches->{$seq_acc}->{$pirsf_acc}->{data}=\@row;
    }
  }
  close(T);
  return 1;
}

sub post_process {
  my($matches, $pirsf_data) = @_;

  my $bestMatch; 
  #Sort all matches and find the smallest evalue. 
  foreach my $seq (keys %$matches){
    $bestMatch->{$seq} = {};#This enables us to capure no matches.

    #If there are matches.....sort them on evalue.
    my @matchesSort = sort{ $matches->{$seq}->{$a}->{evalue} <=> 
                            $matches->{$seq}->{$b}->{evalue}}(keys %{$matches->{$seq}});
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
        print "matches $sf_data->[1]: $pirsf_data->{$sf_data->[1]}->{name}\n";
        _print_report($sf_data);
        if($bestMatch->{$seq}->{sub}){
          my $sub_data = $bestMatch->{$seq}->{sub};
          print " and matches Sub-Family $sub_data->[1]: $pirsf_data->{$sub_data->[1]}->{name}\n";
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
  
  #Output
  #if ($SF) 
  #{$out=" matches $SF: $name{$SF}\n$sf_line\n$sf_out{$SF}";}
  #else { $out="No Match\n";}
  #print "Query sequence: $id $out";
  #if ($SF_subf)
  #{print " and matches Sub-Family $SF_subf: $name{$SF_subf}\n$sf_line_subf\n$sf_out_subf{$SF_subf}";}
}

sub _print_report {
  my($data) = @_;
  my ($hmmMatch, $seqMatch, $envMatch) = _matchBounds($data);
  
  print sprintf("%4s  %7s%6s%10s%10s%8s%8s   %8s%8s   %8s%8s   %5s\n" , 
                '#', 'score', 'bias', 'c-Evalue', 'i-Evalue', 'hmmfrom', 
                'hmm to', 'alifrom', 'ali to', 'envfrom', 'env to', 'acc' );
  
    
  print sprintf("%4s%2s%7s%6s%10s%10s%8s%8s%3s%8s%8s%3s%8s%8s%3s%5s\n" , 
                '1','!', $data->[13], $data->[14], $data->[11], $data->[12], $data->[15], $data->[16], $hmmMatch,
                $data->[17], $data->[18], $seqMatch, $data->[19], $data->[20], $envMatch, $data->[21]);   
}

sub _print_i5 {
  my ($data, $seq) = @_;  
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
                        $data->[12], #DOMAIN_BIAS
                        $data->[15], #DOMAIN_CE_VALUE
                        $data->[16], #DOMAIN_IE_VALUE
                        $data->[20], #ENVELOPE_END
                        $data->[19], #ENVELOPE_START
                        $data->[21], #EXPECTED_ACCURACY
                        $data->[8]); #FULL_SEQUENCE_BIAS
   print "$line\n";
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
