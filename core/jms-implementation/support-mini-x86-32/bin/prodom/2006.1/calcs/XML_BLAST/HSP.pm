# $Id: HSP.pm,v 1.1.1.1 2005/08/18 13:18:26 hunter Exp $

# $Log: HSP.pm,v $
# Revision 1.1.1.1  2005/08/18 13:18:26  hunter
# initial import into CVS
#
# Revision 1.1.1.1  2005/01/05 16:48:59  tuco
# Exportable InterProScan project
#
# Revision 1.1  2004/05/26 16:55:51  tuco
# XML_Blast modules for new blastprodom.
#
# Revision 1.1  2003/10/22 10:10:31  tuco
# Perl librairies needed by the C program to parse the xml output for BlastProDom
#
# Revision 1.1  2003/09/19 07:59:22  manu
# Blast for ProDom release 3
#

package XML_BLAST::HSP;

use strict;

BEGIN {
    $XML_BLAST::HSP::VERSION = "20020724";
}

#------------------------------------------------------------------------------
#
#   new()
#

sub new {
    my ( $class, $args ) = @_;
    my $self = {};

    $self->{_HITNUM}       = $args->{'_HITNUM'};       # ordinal number of the hit, with one-offset
    $self->{_HITID}        = $args->{'_HITID'};        # identifier of the database sequence
    $self->{_HITDEF}       = $args->{'_HITDEF'};       # definition line of the database sequence
    $self->{_HITACCESSION} = $args->{'_HITACCESSION'}; # accession of the database sequence
    $self->{_HITLEN   }    = $args->{'_HITLEN'};       # length of the database sequence

    $self->{_HSPNUM}       = $args->{'_HSPNUM'};       # ordinal number of the HSP, one-offset
    $self->{_BITSCORE}     = $args->{'_BITSCORE'};     # bit-score of the hsp
    $self->{_HSPSCORE}     = $args->{'_HSPSCORE'};     # score of the hsp
    $self->{_EVALUE}       = $args->{'_EVALUE'};       # expect value of the hsp
    $self->{_QUERYFROM}    = $args->{'_QUERYFROM'};    # offset of query at the start of the alignment
    $self->{_QUERYTO}      = $args->{'_QUERYTO'};      # offset of query at the end of the alignment
    $self->{_HITFROM}      = $args->{'_HITFROM'};      # offset of database sequece at the start of the alignement
    $self->{_HITTO}        = $args->{'_HITTO'};        # offset of database sequece at the end of the alignement
    $self->{_PATTERNFROM}  = $args->{'_PATTERNFROM'};  # start of phi-blast pattern on the query
    $self->{_PATTERNTO}    = $args->{'_PATTERNTO'};    # end of phi-blast pattern on the query
    $self->{_QUERYFRAME}   = $args->{'_QUERYFRAME'};   # frame of the query if applicable
    $self->{_HITFRAME}     = $args->{'_HITFRAME'};     # frame of the database sequence if applicable
    $self->{_IDENTITY}     = $args->{'_IDENTITY'};     # number of identitiesin the alignment
    $self->{_POSITIVE}     = $args->{'_POSITIVE'};     # number of positive (conservative) substitutions in the alignment
    $self->{_GAPS}         = $args->{'_GAPS'};         # number of gaps in the alignment
    $self->{_ALIGNLEN}     = $args->{'_ALIGNLEN'};     # length of the alignment
    $self->{_DENSITY}      = $args->{'_DENSITY'};      # score density
    $self->{_QSEQ}         = $args->{'_QSEQ'};         # alignment string for the query
    $self->{_HSEQ}         = $args->{'_HSEQ'};         # alignment string for the database
    $self->{_MIDLINE}      = $args->{'_MIDLINE'};      # formating middle line as normally seen in BLAST report

    bless( $self, $class );
    return $self;
}

#------------------------------------------------------------------------------
#
#   get_hit_num()
#

sub get_hit_num {
    my $this = shift;
    return $this->{_HITNUM};
}

#------------------------------------------------------------------------------
#
#   get_hit_id()
#

sub get_hit_id {
    my $this = shift;
    return $this->{_HITID};
}

#------------------------------------------------------------------------------
#
#   get_hit_def()
#

sub get_hit_def {
    my $this = shift;
    return $this->{_HITDEF};
}

#------------------------------------------------------------------------------
#
#   get_hit_accession()
#

sub get_hit_accession {
    my $this = shift;
    return $this->{_HITACCESSION};
}

#------------------------------------------------------------------------------
#
#   get_hit_length()
#

sub get_hit_length {
    my $this = shift;
    return $this->{_HITLEN};
}

#------------------------------------------------------------------------------
#
#   get_hsp_num()
#


sub get_hsp_num {
    my $this = shift;
    return $this->{_HSPNUM};
}

#------------------------------------------------------------------------------
#
#   get_bit_score()
#

sub get_bit_score {
    my $this = shift;
    return $this->{_BITSCORE};
}

#------------------------------------------------------------------------------
#
#   get_hsp_score()
#

sub get_hsp_score {
    my $this = shift;
    return $this->{_HSPSCORE};
}

#------------------------------------------------------------------------------
#
#   get_evalue()
#

sub get_evalue {
    my $this = shift;
    return $this->{_EVALUE};
}

#------------------------------------------------------------------------------
#
#   get_query_from()
#

sub get_query_from {
    my $this = shift;
    return $this->{_QUERYFROM};
}

#------------------------------------------------------------------------------
#
#   get_query_to()
#

sub get_query_to {
    my $this = shift;
    return $this->{_QUERYTO};
}

#------------------------------------------------------------------------------
#
#   get_hit_from()
#

sub get_hit_from {
    my $this = shift;
    return $this->{_HITFROM};
}

#------------------------------------------------------------------------------
#
#   get_hit_to()
#

sub get_hit_to {
    my $this = shift;
    return $this->{_HITTO};
}

#------------------------------------------------------------------------------
#
#   get_pattern_from()
#

sub get_pattern_from {
    my $this = shift;
    return ($this->{_PATTERNFROM} == 0) ? undef : $this->{_PATTERNFROM};
}

#------------------------------------------------------------------------------
#
#   get_pattern_to()
#

sub get_pattern_to {
    my $this = shift;
    return ($this->{_PATTERNTO} == 0) ? undef : $this->{_PATTERNTO};
}

#------------------------------------------------------------------------------
#
#   get_query_frame()
#

sub get_query_frame {
    my $this = shift;
    return $this->{_QUERYFRAME};
}

#------------------------------------------------------------------------------
#
#   get_hit_frame()
#

sub get_hit_frame {
    my $this = shift;
    return $this->{_HITFRAME};
}

#------------------------------------------------------------------------------
#
#   get_identity()
#

sub get_identity {
    my $this = shift;
    return $this->{_IDENTITY};
}

#------------------------------------------------------------------------------
#
#   get_positive()
#

sub get_positive {
    my $this = shift;
    return $this->{_POSITIVE};
}

#------------------------------------------------------------------------------
#
#   get_gaps()
#

sub get_gaps {
    my $this = shift;
    return $this->{_GAPS};
}

#------------------------------------------------------------------------------
#
#   get_align_length()
#

sub get_align_length {
    my $this = shift;
    return $this->{_ALIGNLEN};
}

#------------------------------------------------------------------------------
#
#   get_density ()
#

sub get_density {
    my $this = shift;
    return ($this->{_DENSITY} == 0) ? undef : $this->{_DENSITY};
}

#------------------------------------------------------------------------------
#
#   get_query_seq ()
#

sub get_query_seq {
    my $this = shift;
    return $this->{_QSEQ};
}

#------------------------------------------------------------------------------
#
#   get_hit_seq ()
#

sub get_hit_seq {
    my $this = shift;
    return $this->{_HSEQ};
}

#------------------------------------------------------------------------------
#
#   get_midline ()
#

sub get_midline {
    my $this = shift;
    return $this->{_MIDLINE};
}

#------------------------------------------------------------------------------
#
#   get_identity_percentage ()
#

sub get_identity_percentage {
    my $this       = shift;
    my $alignlen   = $this->{_ALIGNLEN};
    my $identity   = $this->{_IDENTITY};
    my $percentage = 100 / $alignlen * $identity;
    return $percentage;
}

#------------------------------------------------------------------------------
#
#   get_gaps_percentage ()
#

sub get_gaps_percentage {
    my $this       = shift;
    my $alignlen   = $this->{_ALIGNLEN};
    my $gaps       = $this->{_GAPS};
    my $percentage = 100 / $alignlen * $gaps;
    return $percentage;
}
1;


=head1 NAME

XML_BLAST::HSP

=head1 SYNOPSIS

 use XML_BLAST::HSP;

 $hsp->get_hit_def();
 $hsp->get_hit_evalue();
 $hsp->get_hit_length();
      .
      .
      .

=head1 DESCRIPTION

XML_BLAST::HSP is a helper class for XML_BLAST::Report. Each HSP
object created by a XML_BLAST::Report contains information 
on the high scoring pairs in Report.

=head1 PUBLIC METHODS

=over 5

=item $I<hsp>->get_hit_num()

Ordinal number of the hit, with one-offset

=item $I<hsp>->get_hit_id()

Identifier of the database sequence

=item $I<hsp>->get_hit_def()

Definition line of the database sequence

=item $I<hsp>->get_hit_accession()

Accession of the database sequence

=item $I<hsp>->get_hit_length()

Length of the database sequence

=item $I<hsp>->get_hsp_num()

Ordinal number of the HSP, one-offset

=item $I<hsp>->get_bit_score()

Bit-score of the hsp

=item $I<hsp>->get_hsp_score()

Score of the hsp

=item $I<hsp>->get_evalue()

Expect value of the hsp

=item $I<hsp>->get_query_from()

Offset of query at the start of the alignment

=item $I<hsp>->get_query_to()

Offset of query at the end of the alignment

=item $I<hsp>->get_hit_from()

Offset of database sequece at the start of the alignement

=item $I<hsp>->get_hit_to()

Offset of database sequece at the end of the alignement

=item $I<hsp>->get_pattern_from()

Start of phi-blast pattern on the query

=item $I<hsp>->get_pattern_to()

End of phi-blast pattern on the query

=item $I<hsp>->get_query_frame()

Frame of the query if applicable

=item $I<hsp>->get_hit_frame()

Frame of the database sequence if applicable

=item $I<hsp>->get_identity()

Number of identitiesin the alignment

=item $I<hsp>->get_positive()

Number of positive (conservative) substitutions in the alignment

=item $I<hsp>->get_gaps()

Number of gaps in the alignment

=item $I<hsp>->get_align_length()

Length of the alignment

=item $I<hsp>->get_density()

Score density

=item $I<hsp>->get_query_seq()

Alignment string for the query

=item $I<hsp>->get_hit_seq()

Alignment string for the database

=item $I<hsp>->get_midline()

Formating middle line as normally seen in BLAST report

=item $I<hsp>->get_identity_percentage()

Percentage of identity between the query and the database sequence

=item $I<hsp>->get_gaps_percentage()

Percentage of gaps

=back

=head1 SEE ALSO

I<XML_BLAST::HSP>

=head1 AUTHOR

MOI

=cut

