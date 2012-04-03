#######################################################################
=pod

=head1 NAME

        FASTA - Support of FASTA format.

=cut
#######################################################################

package FASTA;                     # Package name              

use Exporter;                       # Load Exporter module (APP, p91)
@ISA = qw(Exporter);                # Inherit from Exporter module
@EXPORT = qw(readFASTA writeFASTA);            # List of symbols, OK to export
use strict;

##################################################
=pod

=head2 readFASTA

        readFASTA - read a FASTA entry from an open file

=head3 DESCRIPTION

        readFASTA reads a FASTA entry

=head3 SYNOPSIS

        %rec = readFASTA(\*fh)

=head3 PARAMETERS

        %rec     	 A FASTA hash
        \*fh     	 A reference to a filehandle (file or STDIN)

        $rec{len}    	length
        $rec{name}   	name of sequence
        $rec{desc}   	description of sequence
        $rec{seq}[$i]   sequence

=cut
##################################################
sub writeFASTA {
    my ($rh_rec, $fh,$from,$to)  = @_;

    if (! defined($from)){
	$from=1;
    }

    if (! defined($to)){
	$to=$rh_rec->{len};
    }

    my $len=$rh_rec->{len};
    if (($from != 0) && ($to != 0)){
	$len = $to - $from + 1;
    }
  #
  # WRITE THE FASTA HEADER
  #

    printf $fh (">%s", $rh_rec->{name});
    if (defined($rh_rec->{desc}) && ($rh_rec->{desc} ne "")){
	printf $fh (" %s", $rh_rec->{desc});
    }
    printf $fh ("\n");

  #
  # WRITE THE FASTA SEQUENCE
  #
  
    my $j=0;
    for (my $i=$from;$i<=$to;$i++){
	$j++;
	my $acid="";

	if (defined($rh_rec->{seq}[$i])){
	    $acid =  $rh_rec->{seq}[$i];
#      print STDERR ("# $i\t$acid\n");
	}
	else{
	    print STDERR ("# Undefined residue '$acid' no $i in $rh_rec->{name} (FASTA.pm:writeFASTA)\n");
	    $rh_rec->{len}--;
	    next;
	}
	printf $fh ("%s", $acid);
	if (($j % 60) == 0) {
	    print $fh ("\n");
	}
    }
    
    if ((int($len) % 60) != 0) {
	print $fh ("\n");
    }
    undef($rh_rec);
} # writeFASTA


sub readFASTA {
  my ($fh) = @_;
  my %rec = ();
  my @newsequence = ();
  my @sequence = ();
  my $verbose=0;
  
  $_ = <$fh>;
  
  if ($verbose == 1){
    warn "'$_'";
  }
  #
  # Skip comment lines
  #
  while (/^\#/){
    $_ = <$fh>;
    #chomp($_);
  }

  #
  # Skip lines not starting with ">"
  #
  while (substr($_,0,1) ne ">"){
    $_ = <$fh>;
  }

  if (substr($_, 0, 1) ne ">" )
    {
      print STDERR ("Incorrect FASTA format\n");
      $rec{error}=1;
      return %rec;
      exit;
    }

  while (substr($_,0,1) eq ">"||substr($_,0,1) eq " ") {
    $_ = substr($_,1);
#    $_=~ s/\|/ /g;
  }
  
  #
  # THE FIRST WORD IN THE COMMENT LINE BECOMES THE NAME OF THE ENTRY
  #

  my @fields=split(/\s+/,$_);
  $rec{name}=$fields[0];
#  $rec{name}=~s/\|/_/g;
  #
  # THE REMAINING PART OF THE COMMENT LINE BECOMES ADDED
  # 

  my $start=length($rec{name});
  my $end=length($_);
  $rec{desc}=substr($_, $start, $end);
  chomp($rec{desc});


  #
  # Read the sequence
  #
  $_ = <$fh>;
  chomp($_);
  $_=~ s/\W//g;
  $_=~ s/\s+//g;
  $_=~ s/\r//g;
  $_=~ s/\n//g;

  my $len=0;
  #
  # Check if the previous fasta entry was empty
  #
  if (substr($_, 0, 1) eq ">")
  {
    print STDERR "#\nTHE SEQUENCE: $rec{name} CONTAINS NO RESIDUES\n#\n";
    $rec{error}=1;
    return %rec;
    exit;
  }

  @sequence = split(//, "");
  $rec{seq}=\@sequence;
  $sequence[0]=" ";
  my $newentry=0;
  my $endoffile=0;
  $rec{len}=0;
  while ((! $newentry) && (! $endoffile)) 
  {
    chomp($_);
    @newsequence = split(//,$_);
    
    push (@sequence, @newsequence);
    $rec{len}=$rec{len}+length($_);
    
    if (! eof($fh))
    {
      $_ = <$fh>;
      $len=length($_);
      if (substr($_,0,1) eq ">")
      {
        $len=length($_);
        $newentry=1;
      }
    }
    else
    {
      $endoffile=1;
    }
  }

  if ($newentry)
  {
    $len=$len*-1;
    seek $fh,$len,1;
  }
  return %rec;
} # readFASTA


