#!/usr/bin/env perl

=head1 SYNOPSIS

# This script is a very basic version of what is performed by the HMMER website
# searches. It has been stripped out so that anyone can run it without the need
# for Catalyst etc. There a heavier weight client that runs the catalyst models
# if you are trying to debug at a different level. 
# 
# The script will connet to a hmmpgmd client and take the input fasta file
# (containing one or more sequences) and push each sequence down the socket 
# to be searched by the master. When the results come back, the status is 
# read.  If successful, the whole message is read from the socket and written
# to file in /tmp, as well as being stored in a scalar. The scalar is then
# unpacked, just to the level of the stats. The sequence is searched against
# the first database in the hmmpgmd list of databases (whether sequence of HMM).
#
# NOTE: The socket call is wrapped up in a sig alarm that will die terminate when
# it does not response. Under normal website operation, this is set to 30 secs,
# but I have changed this to 10 minutes because of running HMMER under valgrind.
# 

=cut

use strict;
use warnings;
use IO::Socket::INET;
use Getopt::Long;
# use Data::Dump qw(dump);

#------------------------------------------------------------------------------
#Process the options;
#------------------------------------------------------------------------------

my ( $algo, $file, $nocleanup, $peerAddr, $peerPort, $proto, $verbose, $help);

GetOptions(
  "algo=s"     => \$algo,
  "file=s"     => \$file,
  "PeerAddr=s" => \$peerAddr,
  "PeerPort=s" => \$peerPort,
  "Proto=s"    => \$proto,
  "verbose"    => \$verbose,
  "nocleanup"  => \$nocleanup,
  "h|help"     => \$help
  )
  or die "Failed to parse options, run -h :[$@]\n";


if ( defined($file) ) {
  unless ( -s $file ) {
    warn "\n$file either does not exist, or has no size\n\n";
    $help = 1;
  }
}
else {
  warn "\nPlease specify a filename!\n\n";
  $help = 1;
}


help() if($help);

#------------------------------------------------------------------------------
#Now get the socket connection.
#------------------------------------------------------------------------------

my $connection;
$connection = {
  PeerAddr => defined($peerAddr) ? $peerAddr : '127.0.0.1',
  PeerPort => defined($peerPort) ? $peerPort : '51717',
  Proto    => defined($proto)    ? $proto    : 'tcp'
};

$verbose && print STDERR "Getting socket connection";
my $sock = IO::Socket::INET->new(%$connection)
  or die "Could not connect to socket: [$!] \n";
$sock->autoflush(1);

#------------------------------------------------------------------------------

#open the input fasta file.
open( my $fh, $file ) or die "Could not open $file:[$!]\n";

my $c = 1;
local $/ = "\n>";
while (<$fh>) {
  $verbose && print STDERR "Working on sequence #: $c\n";

  #open the file where we store the binary data.
  open( my $outFH, ">/tmp/ncoils.$$.$c.out" )
    or die "Could not open /tmp/ncoils.$$.$c.out:[$!]\n";
  my $seq = $_;
  $seq =~ s/>$//;
  #$seq =~ s/^([^>])/>$1/;
  my $query_name = 'Unknown';
  if( $seq =~ /^>([^\s]*)/ ){
    $query_name=$1;
  }
                     #This is it really
  $verbose && print STDERR "sending $query_name, |$seq| to the socket\n";
  chomp($seq);
  #Print the query to the socket
  print $sock "$seq";
  my $stats;
  local $SIG{ALRM} = sub { die "Failed to get response from hmmpgmd" };
  alarm 600;
  eval { $stats = &unpackResults( $sock, $outFH, $verbose ); };
  if ($@) {
    die "Timeout on the socket:$@\n";
  }
  alarm 0;

  $c++;
}
close($fh);

#------------------------------------------------------------------------------
# Normally we will want to clean up /tmp, but occasionally it will be useful.
#------------------------------------------------------------------------------

unless ($nocleanup) {
  $verbose && print STDERR "Cleaning up /tmp\n";
  my @files = glob("/tmp/hmmer.$$.*");
  foreach my $f (@files) {
    unlink($f) if ( -e $f );
  }
}

#------------------------------------------------------------------------------
#Subroutines
#------------------------------------------------------------------------------

sub unpackResults {
  my ( $sock, $fh, $verbose ) = @_;
  my $statusTemplate = "";
  my $STATUS         = 140;
  my $binaryData;
  my $totalRead = 0;
  while(1){
    my $rl             = read( $sock, my $statusB, $STATUS );
    $binaryData .= $statusB;
    $totalRead += $rl;

    last if($rl == 0 or $rl < $STATUS);
 }
 my $unpackNo = $totalRead/4;
 #print STDERR "|$binaryData|\n";
 my @data = unpack("f$unpackNo", $binaryData);
 #print STDERR "Read $totalRead\n";

 my $i =1;
 foreach(@data){
    printf("%d %10.4f\n", $i ,$_);
    $i++;
  }
}

sub readAndStore {
  my ( $sock, $messLen, $fh ) = @_;

  #Read the number of bytes in the messlen, from the socket, into the results
  #scalar (binary format)
  my $rl = read( $sock, my $resultsB, $messLen );

  #Check that the read length is correct.
  unless ( $rl == $messLen ) {
    die "Error reading STATS from socket. Requested " . $messLen
      . " bytes, but only read $rl bytes\n";
  }
  print $fh $resultsB;
  return $resultsB;

}

sub unpackStats {
  my ($binaryData)  = @_;
  my $STATS         = 120;
  my $statsTemplate = "d5 I2 q9";

  #Store how far we have read through the file
  my $bit = substr( $binaryData, 0, $STATS, '' );
  my @stats = unpack( $statsTemplate, $bit );

  my @statsKeys =
    qw(elapsed user sys Z domZ Z_setby domZ_setby nmodels nseqs
    n_past_msv n_past_bias n_past_vit n_past_fwd nhits nreported nincluded );

  unless ( $#stats == $#statsKeys ) {
    die "Missmatch between the number of stats data elements recieved ["
      . $#stats
      . "] and expected [$#statsKeys]\n";
  }

  my %stats = map { $statsKeys[$_] => $stats[$_] } 0 .. $#statsKeys;
  return \%stats;
}

sub help {
  
  
print<<EOF;

Summary: Search sequence in a file with the HMMER hmmpgmd master/worker process. 
For more information, run "perldoc $0".

usage: $0 -file seq.fa -algo hmmscan
  
  file      : The name of the fasta file.  I am not validating the file, give it
            : rubbish and bad things will happen.
  algo      : The HMMER algorithm to run by hmmpgmd, currently phmmer or hmmscan
  
  h|help    : Prints this help statement   
  verbose   : Prints debug statements/progress reports.
  nocleanup : When this flag is set, leaves the hmmpgmd binary files in
            : /tmp. The files has the format hmmer.#PID.#sequence.out.
 
   
  #The follow options control which hmmpgmd is connected to. The are used
  #by IO::Socket::INET, see CPAN for more information.
  
  PeerAddr  : The IP address of the machine where the master is running, default 127.0.0.1
  PeerPort  : The port number that the master is listening on, default 51371
  Proto     : The socket protocol - should not change this unless the master 
            : changes its communications protocol, which is tcp
           


EOF
 
exit 0;

}

