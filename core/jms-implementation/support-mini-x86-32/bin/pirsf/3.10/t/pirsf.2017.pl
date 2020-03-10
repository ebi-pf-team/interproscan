#!/bin/sh -- perl

eval 'exec /usr/bin/perl -w -S $0 ${1+"$@"}'
    if 0;

# PIR, March 2003,
# Modified, Oct. 2004 
# Major changes:

# 1. Take off domain match.
# 2. Add sub-families
# 3. Replace cut-off by the following conditions:
#    L(matched)/Length >= 0.8
#     and
#    Score - mean(S) >= -Std(S)  or  (Score - mean(S)  >= -2.5Std(S)) and (Score >= min(S)) 
#     and
#    |Length-mean(L)| < 3.5Std(L)  or  |Length-mean(L)| < 50
#
#    where Length is the query seq length and Score is the query HMM score.
#          mean(L) and std(L) are for the length of the family
#          mean(S), std(S) and min(S) are for the HMM score of the family 


# Modified, April 2005
# Major changes:

# 1. condition: Score - mean(S) >= -Std(S)  or  (Score - mean(S)  >= -2.5Std(S)) and (Score >= min(S))
#     becomes  Score >= min(S)
# 2. if a sequence can be placed into a subfamily then place it into the parent PIRSF
# 3. add BLAST search:  predicted sequence is checked by BLAST for the best and "majority" hit to the PIRSF.


# Modified, June 2007
# BLAST command line was changed
# A few changes were made in "BLAST search" section

# Modified, Dec, 2013
# Using HMMER3
 
# Modified, Dec, 2015 
# Extend the match ranges

# Modified, May, 2017 
# Extend the match ranges only if the non-primary match ranges in both 
# query and model move toward the same direction.

#-----------------------------------------------------------------------
# File name: pirsf.pl
# Usage: perl pirsf.pl seq_file > out_file
# PIR SF scan program for one sequence in fasta format
#-----------------------------------------------------------------------




#program location 
#HMMER program
$hmmscan="../../../hmmer/hmmer3/3.1b1/hmmscan";
#BLAST program
$blastall="../../../blast/2.2.24/bin/blastall";

#data location
#PIRSF data file
$pirsf_dat="../../../../data/pirsf/3.02/pirsf.dat";
#HMM models (should run hmmpress first)
$sf_hmm_subf="../../../../data/pirsf/3.02/sf_hmm_all";
$sf_hmm="../../../../data/pirsf/3.02/sf_hmm_all";
#sf.tb file
$sftb="sf.tb";
#sf.seq file (should run formatdb first)
$sfseq="sf.seq";

#Read in pirsf.dat 
open(IN,$pirsf_dat);
while($line=<IN>)
{ chop $line;
  if ($line=~/^\>/) 
   {  $x=(split / /,$line)[0];
      $x=~s/\>//;
      if (index($line,"child")>0) { $child{$x}=(split /: /,$line)[1]; }
      $line=<IN>; chop $line;
      $name{$x}=$line;
      $line=<IN>; chop $line;
      ($lm{$x},$lsd{$x},$smin{$x},$sm{$x},$ssd{$x})=split (/ /,$line);
      # lm=mean(L); lsd=Std(L); smin=min(S); sm=mean(S); ssd=Std(S);
      $line=<IN>; chop $line;
      if ($line=~/Yes/) { $blast{$x}=1; }
   }
}
close IN;

#get seq length
$infile=$ARGV[0];
open(IN,$infile);
while($line=<IN>)
{ 
  # $line
  chop $line;
  # $line
  if ($line=~/^\>/){ $leng=0;}
  else { $line=~s/\s+//g; 
    # $line
         $leng= $leng + length($line);} 
         ### $leng
}
close IN;

#Run HMM search for full-length models and get information
@sf_out=` $hmmscan -E 0.01 --acc $sf_hmm $infile`;



foreach $x (@sf_out)
{ if ($x=~/^Query:/)
    { $id=(split /\s+/,$x)[1];}
   elsif ($x=~/^\>\>/) 
    {$hid=(split /\s+/,$x)[1];
     $S=0; $tmp="";
     $A=9999999; $B=0;
     $Y=9999999; $Z=0;
     }
   elsif (index($x,"!")>0 ) {
        ($a,$b,$y,$z,$s)=(split /\s+/,$x)[10,11,7,8,3];
        $S=$S+$s;  $tmp.=$x;
#        if ($a<$A) { $A=$a;}
#        if ($b>$B) { $B=$b;}
#        if ($y<$Y) { $Y=$y;}
#        if ($z>$Z) { $Z=$z;}

        if ($a<$A && $y<$Y) {$A=$a;$Y=$y;}
        if ($b>$B && $z>$Y) {$B=$b;$Z=$z;}


        }
   elsif ($x=~/Alignments for each/ && $tmp ) 
      { 
         $ovl=($B-$A+1)/$leng;
         $ld=abs($leng-$lm{$hid});
         $sf{$hid}=1;
         $sf_out{$hid}="$tmp";
         $L= ($Z-$Y)/($B-$A);
        if($L> 0.67 && $ovl>=0.8 && ($S>=$smin{$hid}) && ($ld<3.5*$lsd{$hid}||$ld< 50 ))
         { 
          $s{$hid}=$S;
         }
      }
    elsif (index($x,"#")>0) { chop $x; $sf_line=$x; }
}

#find the max score
$max=0;
foreach $x (sort keys %s)
{ if ($s{$x} > $max )
   { $max=$s{$x}; $SF=$x;}
}
if ($SF) {%sf=();$sf{$SF}=1;}

#sub-family search
foreach $z (keys %sf)
{if ($child{$z})
 { foreach $x ((split / /,$child{$z}))
     { $cf{$x}=1;  $subf=1;
       $parent{$x}=$z;
     }
 }
}
if ($subf)
 { @sf_out_subf=` $hmmscan -E 0.01 --acc $sf_hmm_subf $infile`;
%s=();  
foreach $x (@sf_out_subf)
  {
   if ($x=~/^\>\>/)
     {$hid=(split /\s+/,$x)[1];
     $S=0; $tmp="";
     $A=9999999; $B=0;
     $Y=9999999; $Z=0;
     }
   elsif (index($x,"!")>0 ) {
        ($a,$b,$y,$z,$s)=(split /\s+/,$x)[10,11,7,8,3];
        $S=$S+$s;  $tmp.=$x;
#        if ($a<$A) { $A=$a;}
#        if ($b>$B) { $B=$b;}
#        if ($y<$Y) { $Y=$y;}
#        if ($z>$Z) { $Z=$z;}

        if ($a<$A && $y<$Y) {$A=$a;$Y=$y;}
        if ($b>$B && $z>$Y) {$B=$b;$Z=$z;}

        }
   elsif ($x=~/Alignments for each/ && $tmp)
      { $L= ($Z-$Y)/($B-$A);


($c,$e)=(split /\s+/,$tmp)[3,6];
         if($L> 0.67 && $cf{$hid} && $S>=$smin{$hid})
         {
          $s{$hid}=$S;
          $sf_out_subf{$hid}=$tmp;
         }
      }
    elsif (index($x,"#")>0) {chop $x; $sf_line_subf=$x; }
   }


#find the max score
$max=0;
foreach $x (sort keys %s)
 { if ($s{$x} > $max )
   { $max=$s{$x}; $SF_subf=$x;}
  }

}

if ($SF_subf && !$SF) {$SF=$parent{$SF_subf};}

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


#Output
if ($SF) 
{$out="matches $SF: $name{$SF}\n$sf_line\n$sf_out{$SF}";} # Extra space removed in this line
else { $out="No Match\n";}
print "Query sequence: $id $out";
if ($SF_subf)
{print " and matches Sub-Family $SF_subf: $name{$SF_subf}\n$sf_line_subf\n$sf_out_subf{$SF_subf}";}
