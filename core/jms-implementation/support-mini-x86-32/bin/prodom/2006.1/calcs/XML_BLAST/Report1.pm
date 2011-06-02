# $Id: Report.pm 974 2005-01-20 13:16:00Z manu $

# $Log$
# Revision 1.6  2005/01/20 13:16:00  manu
# Names of functions corrected (2)
#
# Revision 1.5  2004/07/30 08:03:03  manu
# We may use env variable IPRSCAN_HOME to find the program ParseBlastXmlReport
#
# Revision 1.4  2003/12/01 08:04:26  beausse
# Remplacement des & par _ avant de lire le fichier XML
#
# Revision 1.2  2003/10/21 12:43:29  manu
# blastpgp ==> blastp
#
# Revision 1.1  2003/09/19 07:59:22  manu
# Blast for ProDom release 3
#

package XML_BLAST::Report;

use strict;
use XML_BLAST::Iter;
#use guidonvars;						             # If you want to use outside ProDom, comment here.
#RP !!!! my $PARSEBLASTXMLREPORT="/path/to/ParseBlastXmlReport";                     # ...and init the variable here
#RP !!!! my $PARSEBLASTXMLREPORT="./ParseBlastXmlReport";
# Location of the ParseBlastXmlReport binary (matthew)
my $PARSEBLASTXMLREPORT="$ENV{'IPRSCAN_HOME'}/ParseBlastXmlReport"; # !!!!     # init for interproscan

BEGIN {
    $XML_BLAST::Report::VERSION = "20030724";
}

#------------------------------------------------------------------------------
#
#   new()
#

sub new {
    my ( $class ) = @_;
    my $self = {};

    $self->{'_PROGRAM'};     # BLAST program, e.g., blastp, blastx, etc.
    $self->{'_VERSION'};     # version number of the BLAST engine.
    $self->{'_REFERENCE'};   # a reference to the article describing the algorith.
    $self->{'_DB'};          # the database searched.
    $self->{'_QUERYID'};     # the identifier of the query
    $self->{'_QUERYDEF'};    # the definition line of the query
    $self->{'_QUERYLEN'};    # the length of the query
    $self->{'_QUERYSEQ'};    # the query sequence (optional)

    $self->{'_MATRIX'};      # matrix used
    $self->{'_EXPECT'};      # expect value cutoff
    $self->{'_INCLUDE'};     # inclusion threshold for a psi-blast iteration
    $self->{'_SCMATCH'};     # match score for nucleotide-nucleotide comparison
    $self->{'_SCMISMATCH'};  # mismatch penality for nucleotide-nucleotide comparison
    $self->{'_GAPOPEN'};     # gap existence cost
    $self->{'_GAPEXTEND'};   # gap extension cost
    $self->{'_FILTER'};      # filtering options
    $self->{'_PATTERN'};     # pattern used for phi-blast search
    $self->{'_ENTREZQUERY'}; # entrez query used to limit search

    $self->{'_DBNUM'};       # number of sequence in the database
    $self->{'_DBLEN'};       # number of letters in the database

    $self->{'_ITERATIONS'};  # array to Iter objet
    $self->{'_HSPS'};        # array to all HSP objet

    bless( $self, $class );
    return $self;
}


#------------------------------------------------------------------------------
#
#   parse()
#

sub parse() {
    my $this = shift;
    my $file = shift;

    my ( %h_hsp, %h_iter, @tab_iter, %h_stat, @tab );
    my $file_reformat = $file . "_reformat";
    my @a_hsp;

    # Appel du programme C qui parse le fichier XML
    my $cde = $PARSEBLASTXMLREPORT;
    if (! -x $cde) {
	die "ERROR - $PARSEBLASTXMLREPORT is not found or is not executable.\nPlease have a look to Report.pm";
    }

    open (XML,"$file");
    open (OUT_XML,">$file_reformat");
    while ( my $line = <XML> ){
	$line =~ s/&/_/g;
	print OUT_XML $line;
    }
    close (XML);
    close (OUT_XML);

    $cde .= " < $file_reformat |";
    open (PARSE,"$cde") or die "Cannot run $cde";

    while ( my $line = <PARSE>){

	$line =~ s/_lt;|_lt_/</g;
	$line =~ s/_gt;|_gt_/>/g;

	if ( $line =~ /^HSP(\d+)/ ){
	    my $iter_num = $1;
	    if ( !exists($h_iter{$iter_num}) ) { my %h_tmp; $h_iter{$iter_num} = \%h_tmp; }
	    $line =~ s/^HSP\d+//;
	    my @elem = split /@/, $line;
	    my %hsp;
	    foreach my $el ( @elem ){
		$el =~ /(_\w+)=(.+)/;
		$hsp{$1} = $2;
	    }
	    my $o_hsp = XML_BLAST::HSP->new(\%hsp);
	    push ( @{$h_hsp{$iter_num}}, $o_hsp);
	    undef(%hsp);
	    undef($o_hsp);
	}
	elsif ( $line =~ /^ITERATION(\d+)/ ){
	    my $iter_num = $1;
	    $line =~ s/^ITERATION\d+//;
	    my @elem = split /@/, $line;

	    foreach my $el ( @elem ){
		$el =~ /(_\w+)=(.+)/;
		$h_iter{$iter_num}->{$1} = $2;
	    }
	}
	elsif ( $line =~ /^PARAMETERS/ ){
	    $line =~ s/^PARAMETERS//;
	    my @elem = split /@/, $line;

	    foreach my $el ( @elem ){
		$el =~ /(_\w+)=(.+)/;
		$this->{$1} = $2;
	    }
	}
	elsif ( $line =~ /^STATISTICS(\d+)/ ){
	    my $iter_num = $1;
	    $line =~ s/^STATISTICS(\d+)//;
	    my @elem = split /@/, $line;

	    foreach my $el ( @elem ){
		$el =~ /(_\w+)=(.+)/;
		$h_iter{$iter_num}->{$1} = $2;
	    }
	}
	elsif ( $line =~ /^BLASTOUTPUT/ ){
	    $line =~ s/^BLASTOUTPUT//;
	    my @elem = split /@/, $line;
	    foreach my $el ( @elem ){
		#print "$el\n";
		$el =~ /(_\w+)=(.+)/;
		$this->{$1} = $2;
	    }
	}
    }

    foreach my $iter_num ( keys %h_hsp ){

	$h_iter{$iter_num}->{'_HSPS'} = $h_hsp{$iter_num};

	my $o_iter = XML_BLAST::Iter->new($h_iter{$iter_num});
	push (@tab_iter, $o_iter);

	if ( $iter_num == 1 ){
	    $this->{'_DBNUM'} = $o_iter->get_db_num();
	    $this->{'_DBLEN'} = $o_iter->get_db_len();
	}
    }
    $this->{'_ITERATIONS'} = \@tab_iter;

    my @a_hsp_tmp;
    foreach my $o_iter ( @tab_iter ){
	foreach my $o_hsp ( @{$o_iter->get_list_hsp} ){
	    push ( @a_hsp_tmp , $o_hsp );
	}
    }
    $this->{'_HSPS'} = \@a_hsp_tmp;
    close(PARSE);
    unlink($file_reformat);
}

#------------------------------------------------------------------------------
#
#   get_program()
#

sub get_program {
    my $this = shift;
    return $this->{_PROGRAM};
}

#------------------------------------------------------------------------------
#
#   get_version()
#

sub get_version {
    my $this = shift;
    return $this->{_VERSION};
}

#------------------------------------------------------------------------------
#
#   get_reference()
#

sub get_reference {
    my $this = shift;
    return $this->{_REFERENCE};
}

#------------------------------------------------------------------------------
#
#   get_database()
#

sub get_database {
    my $this = shift;
    return $this->{_DB};
}

#------------------------------------------------------------------------------
#
#   get_query_id()
#

sub get_query_id {
    my $this = shift;
    return $this->{_QUERYID};
}

#------------------------------------------------------------------------------
#
#   get_query_def()
#

sub get_query_def {
    my $this = shift;
    return $this->{_QUERYDEF};
}

#------------------------------------------------------------------------------
#
#   get_query_length()
#

sub get_query_length {
    my $this = shift;
    return $this->{_QUERYLEN};
}

#------------------------------------------------------------------------------
#
#   get_sequence_query()
#

sub get_sequence_query {
    my $this = shift;
    return $this->{_QUERYSEQ};
}

#------------------------------------------------------------------------------
#
#   get_matrix()
#

sub get_matrix {
    my $this = shift;
    return $this->{_MATRIX};
}

#------------------------------------------------------------------------------
#
#   get_expect()
#

sub get_expect {
    my $this = shift;
    return $this->{_EXPECT};
}

#------------------------------------------------------------------------------
#
#   get_include()
#

sub get_include {
    my $this = shift;
    return $this->{_INCLUDE};
}

#------------------------------------------------------------------------------
#
#   get_sc_match()
#

sub get_sc_match {
    my $this = shift;
    return $this->{_SCMATCH};
}

#------------------------------------------------------------------------------
#
#   get_sc_mismatch()
#

sub get_sc_mismatch {
    my $this = shift;
    return $this->{_SCMISMATCH};
}

#------------------------------------------------------------------------------
#
#   get_gap_open()
#

sub get_gap_open {
    my $this = shift;
    return $this->{_GAPOPEN};
}

#------------------------------------------------------------------------------
#
#   get_gap_extend()
#

sub get_gap_extend {
    my $this = shift;
    return $this->{_GAPEXTEND};
}

#------------------------------------------------------------------------------
#
#   get_filter()
#

sub get_filter {
    my $this = shift;
    return $this->{_FILTER};
}

#------------------------------------------------------------------------------
#
#   get_pattern()
#

sub get_pattern {
    my $this = shift;
    return $this->{_PATTERN};
}

#------------------------------------------------------------------------------
#
#   get_entrez_query()
#

sub get_entrez_query {
    my $this = shift;
    return $this->{_ENTREZQUERY};
}

#------------------------------------------------------------------------------
#
#   get_db_num()
#

sub get_db_num {
    my $this = shift;
    return $this->{_DBNUM};
}

#------------------------------------------------------------------------------
#
#   get_db_len()
#

sub get_db_len {
    my $this = shift;
    return $this->{_DBLEN};
}

#------------------------------------------------------------------------------
#
#   get_list_iteration()
#

sub get_list_iteration {
    my $this = shift;
    return $this->{_ITERATIONS};
}

#------------------------------------------------------------------------------
#
#   get_list_hsp()
#

sub get_list_hsp {
    my $this = shift;
    return $this->{_HSPS};
}
1;

=head1 NAME

XML_BLAST::Report

=head1 SYNOPSIS

 use XML_BLAST::Report;
 $report = XML_BLAST::Report->new('MyBlastReport.xml');
 $report->parse();

 $hsp->get_program();
 $hsp->get_version();
 $hsp->get_database();
      .
      .
      .

=head1 DESCRIPTION


=head1 PUBLIC METHODS

=over 5

=item $I<hsp>->get_program()

BLAST program, e.g., blastp, blastx, etc.

=item $I<hsp>->get_version()

Version number of the BLAST engine.

=item $I<hsp>->get_reference()

A reference to the article describing the algorith.

=item $I<hsp>->get_database()

The database searched.

=item $I<hsp>->get_query_id()

The identifier of the query

=item $I<hsp>->get_query_def()

The definition line of the query

=item $I<hsp>->get_query_length()

The length of the query

=item $I<hsp>->get_sequence_query()

The query sequence (optional)

=item $I<hsp>->get_matrix()

Matrix used

=item $I<hsp>->get_expect()

Expect value cutoff

=item $I<hsp>->get_include()

Inclusion threshold for a psi-blast iteration

=item $I<hsp>->get_sc_match()

Match score for nucleotide-nucleotide comparison

=item $I<hsp>->get_sc_mismatch()

Mismatch penality for nucleotide-nucleotide comparison

=item $I<hsp>->get_gap_open()

Gap existence cost

=item $I<hsp>->get_gap_extend()

Gap extension cost

=item $I<hsp>->get_filter()

Filtering options

=item $I<hsp>->get_pattern()

Pattern used for phi-blast search

=item $I<hsp>->get_entrez_query()

Entrez query used to limit search

=item $I<hsp>->get_db_num()

Number of sequence in the database

=item $I<hsp>->get_db_len()

Number of letters in the database

=item $I<hsp>->get_list_iteration()

Return a pointer to a list of Iter objects. See XML_BLAST::Iter for information on these

=item $I<hsp>->get_list_hsp()

Return a pointer to a list of all HSP objects. See XML_BLAST::HSP for information on these

=back

=head1 SEE ALSO

I<XML_BLAST::Report>

=head1 AUTHOR

MOI

=cut
