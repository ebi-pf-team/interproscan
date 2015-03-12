package HmmerHit;

# Copyright (C)) 2005 Applied Biosystems. 
# This file may be copied and redistributed freely, without advance permission,
# provided that this Copyright statement is reproduced with each copy. 

# LIMITATION OF WARRANTY
# NOTHING IN THIS AGREEMENT WILL BE CONSTRUED AS A REPRESENTATION MADE OR
# WARRANTY GIVEN BY APPLIED BIOSYSTEMS OR ANY THIRD PARTY THAT THE USE OF
# DATA PROVIDED HEREUNDER WILL NOT INFRINGE ANY PATENT, COPYRIGHT, TRADEMARK
# OR OTHER RIGHTS OF ANY THIRD PARTY. DATA IS PROVIDED "AS IS" WITHOUT
# WARRANTY OF ANY KIND WHATSOEVER, EXPRESS OR IMPLIED, INCLUDING IMPLIED
# WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. APPLIED
# BIOSYSTEMS MAKES NO WARRANTY THAT ITS DATA DOES NOT CONTAIN ERRORS.

### v2.0 - added _joinSeqRanges which will join sequence alignments if breaks
###  are <= 15 AA - Rozina Loo 01/12/05

###  hmmlength_range = 5 if there is a stretch of gap that is equal to or 
###  greater than 5 positions (stretch of gaps are defined by continuous 
###  inserts and delete states between streches of Matched positions) 
###  and hmmrange_cutoff = 5 (there is a stetch of matched state of at least 5
###  positions right before this expanded gap,the hmm and alignment positions
###  will be recorded see (  _setCalculatedStartsEndsRanges )

###  alignSeq_cutoff = 15, if the breaks between seq alignment ranges is <= 15
###  AA then alignment will be joined see ( _joinSeqRange )

use strict;

sub new {
  my $class = shift;
  my $scoreLine = shift;
  my $alignmentText = shift;
  my $self = {};
  bless $self, $class;
  $self->init($scoreLine,$alignmentText);
  return $self;
}

#grab score info
sub init {
  my $self = shift;
  my $scoreLine = shift;
  my $alignmentText = shift;
  
  my ($name,$domain,$domainCnt,$seqStart,$seqEnd,$hmmStart,$hmmEnd,$score,$evalue) = $scoreLine=~/^(\S+)\s+(\d+)\/(\d+)\s+(\d+)\s+(\d+)\s+\S+\s+(\d+)\s+(\d+)\s+\S+\s+(\S+)\s+(\S+)/;

  #currently does not handle multiple domains
  print STDERR "More than 1 domain $name\n" if ($domain > 1);
  
  $self->{name} = $name;
  $self->{domain} = $domain;
  $self->{domainCnt} = $domainCnt;
  $self->{score} = $score;
  $self->{evalue} = $evalue;
  $self->{alignmentText} = $alignmentText;
  $self->{scoreLine} = $scoreLine;

  #unparsed results
  # rpetry 21/3/2007 - The change marked with this comment represents a change in
  # in the policy of storing and displaying of Panther matches: from the ranges
  # obtained from the actual aligned regions to the raw ranges, as output in the
  # non-alignment section of the HMMER output:

  # rpetry 21/3/2007 $self->{unparsedSeqStart} = $seqStart;
  # rpetry 21/3/2007 $self->{unparsedSeqEnd} = $seqEnd;
  # rpetry 21/3/2007 $self->{unparsedHmmStart} = $hmmStart;
  # rpetry 21/3/2007 $self->{unparsedHmmEnd} = $hmmEnd; 

   $self->{seqStart} = $seqStart; # rpetry 21/3/2007 
   $self->{seqEnd} = $seqEnd; # rpetry 21/3/2007 
   $self->{hmmStart} = $hmmStart; # rpetry 21/3/2007 
   $self->{hmmEnd} = $hmmEnd; # rpetry 21/3/2007 
   $self->{hmmRange} = "$hmmStart-$hmmEnd"; # rpetry 21/3/2007 
   $self->{seqRange} = "$seqStart-$seqEnd"; # rpetry 21/3/2007 
   $self->_joinSeqRanges($self->{seqRange}); # rpetry 21/3/2007 

  #calculate starts and ends based on aliged regions (ex: for interpro)
  # rpetry 21/3/2007 $self->_setCalculatedStartsEndsRanges($seqStart,$hmmStart);
}

#calculate starts and ends based on aliged regions (ex: for interpro)
sub _setCalculatedStartsEndsRanges {
  my $self = shift;
  my $hmmerSeqStart = shift;
  my $hmmerHmmStart = shift; 
 
  my  $hmmlength_cutoff = 5;
  my $hmmrange_cutoff = 5; 

  my $alignment = $self->alignedSequence();

  #position before looping through alignment
  my $seq_poscounter = $hmmerSeqStart - 1;
  my $hmm_poscounter = $hmmerHmmStart - 1;
  
  my @al = split(//, $alignment); 
  my $gap_counter = 0; 
  my ($seq_start,$seq_end,$seq_range);
  my ($hmm_start,$hmm_end,$hmm_range);
  my ($temphmm_range_end,$temphmm_range_start);
  my ($tempseq_range_end,$tempseq_range_start);

  #loop through alignment
  for(my $x=0; $x < @al; $x++){ 
    if($al[$x] =~ /[a-z]/){ 
      #if lowercase (insert), increase seq counter
      $seq_poscounter++; 
      $gap_counter++ if($seq_start);
    } 
    elsif($al[$x] =~ /-/){   
      #if delete, increase hmm counter
      $hmm_poscounter++; 
      $gap_counter++ if($hmm_start);
    } 
    elsif($al[$x] =~ /[A-Z]/){ 
      #if UC, increase hmm and seq counters
      $hmm_poscounter++; 
      $seq_poscounter++; 
      # if gap long enough check hmm match length and reset range/gap counters 
      if($gap_counter >= $hmmrange_cutoff && $temphmm_range_start && $tempseq_range_start){ 
	# if HMM match long enough record alignment positions 
	if(($temphmm_range_end - $temphmm_range_start) >= $hmmlength_cutoff){ 
	  $hmm_range.="$temphmm_range_start-$temphmm_range_end,"; 
	  $seq_range.="$tempseq_range_start-$tempseq_range_end,"; 
	} 
	$temphmm_range_start = $hmm_poscounter; 
	$tempseq_range_start = $seq_poscounter; 
      } 
      $gap_counter = 0; 
      if(!$hmm_start){ 
	$hmm_start = $hmm_poscounter; 
	$temphmm_range_start = $hmm_poscounter; 
      } 
      if(!$seq_start){ 
	$seq_start = $seq_poscounter; 
	$tempseq_range_start = $seq_poscounter; 
      } 
      $hmm_end = $hmm_poscounter; 
      $seq_end = $seq_poscounter; 
      $temphmm_range_end = $hmm_poscounter; 
      $tempseq_range_end = $seq_poscounter; 
    } 
  } 
  # if HMM match long enough record alignment positions 
  if(($temphmm_range_end - $temphmm_range_start) >= $hmmlength_cutoff){ 
    $hmm_range.="$temphmm_range_start-$temphmm_range_end,"; 
    $seq_range.="$tempseq_range_start-$tempseq_range_end,"; 
  } 
  $self->{seqStart} = $seq_start;
  $self->{seqEnd} = $seq_end;
  $self->{hmmStart} = $hmm_start;
  $self->{hmmEnd} = $hmm_end;
  $self->{hmmRange} = $hmm_range;
  $self->{seqRange} = $seq_range;
  $self->_joinSeqRanges($seq_range);
}


#private method
#created a new subroutine for the option of joining Sequence Alignments if breaks are less than 15
sub _joinSeqRanges{
  my $self = shift;
  my $seq_range = shift;
  my ($join,$last);

  my $alignSeq_cutoff = 15;

  my @compare = split(/\-/,$seq_range);
  $join = shift(@compare);
  $last = pop(@compare);
  
  foreach my $al(@compare){
    my ($end,$begin)=$al=~/(\d+)\,(\d+)/; 
    unless(($begin - $end)<= $alignSeq_cutoff){
      $join .= "-$end,$begin";
    }

  }
  $join .= "-$last";
  
  $self->{joinedSeqRange} = $join;
}

#parsed out alignment sequence (just the sequence)
sub alignedSequence {
  my $self = shift;
  
  if (! defined($self->{alignedSequence})) {
    $self->{alignedSequence} = $self->_parseAlignedSequence();
  }
  return $self->{alignedSequence};
}

sub _parseAlignedSequence {
  my $self = shift;
  my $alignmentText = $self->{alignmentText};

  die "died - Missing alignment text\n" unless ($alignmentText);
  
  my @lines = split("\n",$alignmentText);
  my $firstLine = shift @lines;
  my ($name,$domain,$domainCnt)= $firstLine=~/^(\S+): domain (\d+) of (\d+)/;
  
  #validate same as score info
  if ( ($name != $self->name()) && ($domain != $self->domain()) && ($domain != $self->domainCnt())) {
    die "Error parsing domains -- died\n";
  }
  my ($consensus,$alignment);
  for (my $i=0; $i<@lines; $i++) {
    my $consensusLine = $lines[$i];
    next unless ($consensusLine=~/\S/);
    
    $i++;
    my $matchLine = $lines[$i];
    $i++;
    my $alignmentLine = $lines[$i];
    $alignmentLine=~s/\s+$//;
    $alignmentLine=~s/^\s+\S+\s+\S+\s+(\S*)\s+\S+$/$1/;

    $alignment .= $alignmentLine;
    $consensus .= $consensusLine;
  }
  $consensus=~s/\s+//g;
  $consensus=~s/^\*->//;
  $consensus=~s/<-\*$//; 
  $alignment=~s/\s+//g;

  print STDERR "error $name - consensus and alignment lengths not equal\n" unless (length($consensus) eq length($alignment));
  
  return $alignment;
}

#raw HMMER alignment text with consensus seuqence and aligned sequence
sub alignmentText {
  my $self = shift;
  return $self->{alignmentText};
}

sub name {
    my $self = shift;
    return $self->{name};
}

sub domain {
    my $self = shift;
    return $self->{domain};
}

sub domainCnt {
    my $self = shift;
    return $self->{domainCnt};
}

#start position as calculated by joining aligned regions
sub seqStart {
    my $self = shift;
    return $self->{seqStart};
}

### return ths seq range after joining
sub joinedSeqRange{
  my $self = shift;
  return $self->{joinedSeqRange};
}

#end position as calculated by joining aligned regions
sub seqEnd {
    my $self = shift;
    return $self->{seqEnd};
}

# joinAlignmentFlag - if set will automatically return the joined alignment
sub seqRange {
  my $self = shift;
  my $joinAlignmentFlag = shift;

  if($joinAlignmentFlag){
    return $self->{joinedSeqRange};
  }
  else{
    return $self->{seqRange};
  }
}

#start position as calculated by joining aligned regions 
sub hmmStart {
    my $self = shift;
    return $self->{hmmStart};
}

#end position as calculated by joining aligned regions 
sub hmmEnd {
    my $self = shift;
    return $self->{hmmEnd};
}

sub hmmRange {
  my $self = shift;
  return $self->{hmmRange};
}

sub score {
  my $self = shift;
  return $self->{score};
}

sub evalue {
  my $self = shift;
  return $self->{evalue};
}

#unparsed sequence start, as displayed by HMMER
sub unparsedSeqStart {
  my $self = shift;
  return $self->{unparsedSeqStart};
}

#unparsed sequence end, as displayed by HMMER
sub unparsedSeqEnd {
  my $self = shift;
  return $self->{unparsedSeqEnd};
}

#unparsed HMM start, as displayed by HMMER
sub unparsedHmmStart {
  my $self = shift;
  return $self->{unparsedHmmStart};
}

#unparsed HMM end, as displayed by HMMER
sub unparsedHmmEnd {
  my $self = shift;
  return $self->{unparsedHmmEnd};
}

#given a position in the protein, based on the alignment, returns
#the corresponding position in the HMM
sub proteinPosToHmmPos {
  my $self = shift;
  my $protPos = shift;
  my $hmmPos;
  
  my $insertStateMsg = "insert state";

  my $seqStart = $self->unparsedSeqStart();
  my $seqEnd = $self->unparsedSeqEnd();
  if ( ($protPos < $seqStart) || ($protPos > $seqEnd)) {
    $hmmPos = $insertStateMsg;
  } else {
    my $alignSeq = $self->alignedSequence();
    my @chars = split(//,$alignSeq);
    my $seqCounter = $seqStart -1;
    my $hmmNodeCounter = $self->unparsedHmmStart() - 1;

    my $i=0;
    foreach my $char (@chars) { 
      $i++;
      $hmmNodeCounter++ if ($char=~/[A-Z\-]/);  
      next unless ($char=~/[A-Za-z]/); #continue if part of seq 
      $seqCounter++;

      if ($seqCounter == $protPos) { 
	#note if match state or not
	if ($char=~/[A-Z]/) { 
	  $hmmPos = $hmmNodeCounter; 
	} else { 
	  $hmmPos = $insertStateMsg; 
	} 
	last; 
      }
    }
  }
  return $hmmPos;
}

####
# sub id is a MAG-specific function.  It returns the requested ID portion
# of a NCBI-style complex name, or the name of the sequence if not in NCBI 
# format
#
# params: $type = {gi|spid|spacc}.  If undef, returns gi
# return: the appropriate id of the sequence, or the bare name
####
sub id {
    my $self = shift;
    my $type = lc(shift);
    return $self->{name} unless ($self->{name} =~ /\|/); # ret name if not NCBI
    if (! $type || $type eq "gi") {
	return $1 if ($self->{name} =~ /gi\|([\d]+)/); 
    }

    # on failure, just return the name
    return $self->{name};
}

1;  # so the require or use succeeds  
