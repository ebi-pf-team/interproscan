# $Id: Iter.pm,v 1.1.1.1 2005/08/18 13:18:26 hunter Exp $

# $Log: Iter.pm,v $
# Revision 1.1.1.1  2005/08/18 13:18:26  hunter
# initial import into CVS
#
# Revision 1.1.1.1  2005/01/05 16:48:59  tuco
# Exportable InterProScan project
#
# Revision 1.1  2004/05/26 16:55:51  tuco
# XML_Blast modules for new blastprodom.
#
# Revision 1.1  2003/10/22 10:09:51  tuco
# Perl libraries needed by C program to parse the xml output of blastp for BlastProDom.
#
# Revision 1.1  2003/09/19 07:59:22  manu
# Blast for ProDom release 3
#

package XML_BLAST::Iter;

use strict;
use XML_BLAST::HSP;

BEGIN {
    $XML_BLAST::Iter::VERSION = "20030724";
}

#------------------------------------------------------------------------------
#
#   new()
#

sub new {
    my ( $class, $args ) = @_;
    my $self = {};

    $self->{'_ITERNUM'}  = $args->{'_ITERNUM'};  # the psi-blast iteration number
    $self->{'_MESSAGE'}  = $args->{'_MESSAGE'};  # error message

    $self->{'_DBNUM'}    = $args->{'_DBNUM'};    # number of sequence in the database
    $self->{'_DBLEN'}    = $args->{'_DBLEN'};    # number of letters in the database
    $self->{'_HSPLEN'}   = $args->{'_HSPLEN'};   # the effective HSP length
    $self->{'_EFFSPACE'} = $args->{'_EFFSPACE'}; # the effective search space
    $self->{'_KAPPA'}    = $args->{'_KAPPA'};    # Karlin-Altschul parameter K
    $self->{'_LAMBDA'}   = $args->{'_LAMBDA'};   # Karlin-Altschul parameter Lambda
    $self->{'_ENTROPY'}  = $args->{'_ENTROPY'};  # Karlin-Altschul parameter H

    $self->{'_HSPS'}     = $args->{'_HSPS'};     # Array to HSP object

    bless( $self, $class );
    return $self;
}

#------------------------------------------------------------------------------
#
#   get_iter_num()
#

sub get_iter_num {
    my $this = shift;
    return $this->{_ITERNUM};
}

#------------------------------------------------------------------------------
#
#   get_message()
#

sub get_massage {
    my $this = shift;
    return $this->{_MESSAGE};
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
#   get_hsp_len()
#

sub get_hsp_len {
    my $this = shift;
    return $this->{_HSPLEN};
}

#------------------------------------------------------------------------------
#
#   get_eff_space()
#

sub get_eff_space {
    my $this = shift;
    return $this->{_EFFSPACE};
}

#------------------------------------------------------------------------------
#
#   get_kappa()
#

sub get_kappa {
    my $this = shift;
    return $this->{_KAPPA};
}

#------------------------------------------------------------------------------
#
#   get_lambda()
#

sub get_lambda {
    my $this = shift;
    return $this->{_LAMBDA};
}

#------------------------------------------------------------------------------
#
#   get_entropy()
#

sub get_entropy {
    my $this = shift;
    return $this->{_ENTROPY};
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

XML_BLAST::Iter

=head1 SYNOPSIS

 use XML_BLAST::Iter;

 $hsp->get_iter_num();
 $hsp->get_message();
 $hsp->get_db_num();
      .
      .
      .

=head1 DESCRIPTION



=head1 PUBLIC METHODS

=over 5

=item $I<hsp>->get_iter_num()

The psi-blast iteration number

=item $I<hsp>->get_message()

Error message

=item $I<hsp>->get_db_num()

Number of sequence in the database

=item $I<hsp>->get_db_len()

Number of letters in the database

=item $I<hsp>->get_hsp_len()

The effective HSP length

=item $I<hsp>->get_eff_space()

The effective search space

=item $I<hsp>->get_kappa()

Karlin-Altschul parameter K

=item $I<hsp>->get_lambda()

Karlin-Altschul parameter Lambda

=item $I<hsp>->get_entropy()

Karlin-Altschul parameter H

=item $I<hsp>->get_list_hsp()

Return a pointer to a list of HSP objects. See XML_BLAST::HSP for information on these

=back

=head1 SEE ALSO

I<XML_BLAST::Iter>

=head1 AUTHOR

MOI

=cut
