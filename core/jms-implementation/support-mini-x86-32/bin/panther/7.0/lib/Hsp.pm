#! /usr/local/bin/perl

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

package Hsp;
1; # return value for lib loader


sub new {
    my $class = shift;
    my @hspText = @_;
    my $self = {};
    bless $self, $class;
    $self->init(@hspText);
    return $self;
}

sub init {
    my $self = shift;
    my @hspText = @_;
    
    $self->{hspText} = \@hspText;

    $self->{qStart} = 999999;
    $self->{hStart} = 999999;
    $self->{qEnd} = -1;
    $self->{hEnd} = -1;

    foreach $line (@hspText) {
	if (($line =~ /Length = ([\d]+)/)) {
	  $self->{length} = $1 unless ($self->{length}); 
	}
	if (($line =~ /Expect = (.*)/)) {
	  $self->{expect} = $1 unless ($self->{expect}); 
        }
	if (($line =~ /Score =[\s]+([\d\.]+) bits \(([\d]+)\),/)) {
	  $self->{bits} = $1 unless ($self->{bits}); 
	  $self->{score} = $2 unless ($self->{score}); 
        }
	if (($line =~ m!Identities = ([\d]+)/([\d]+).*Positives = ([\d]+)/!)) {
	  $self->{hspId} += $1 unless ($self->{hspId}); 
	  $self->{hspCons} += $3 unless ($self->{hspCons}); 
	  $self->{length_aln} += $2 unless ($self->{length_aln}); 
	}
	if ($line =~ m!^Query: ([\d]+)[\s]+(.*)[\s]+([\d]+)!) {
	    $self->{qStart} = ($1<$self->{qStart})?$1:$self->{qStart};
	    $self->{qEnd} = ($3>$self->{qEnd})?$3:$self->{qEnd};
	    $self->{queryString} .= $2;
        }
	if ($line =~ m!^Sbjct: ([\d]+)[\s]+(.*)[\s]+([\d]+)!) {
	    $self->{hStart} = ($1<$self->{hStart})?$1:$self->{hStart};
	    $self->{hEnd} = ($3>$self->{hEnd})?$3:$self->{hEnd};
	    $self->{hitString} .= $2;
        }
    }
}

sub score {
    my $self = shift;
    return $self->{score};
}

sub bits {
    my $self = shift;
    return $self->{bits};
}

sub expect {
    my $self = shift;
    return $self->{expect};
}

sub length {
    my $self = shift;
    return $self->{length};
}

sub length_aln {
    my $self = shift;
    return $self->{length_aln};
}

sub matches {
    my $self = shift;
    my $type = shift;
    if (lc($type) eq 'id') {
        return $self->{hspId};
    } elsif (lc($type) eq 'cons') {
        return $self->{hspCons};
    } else {
	return undef;
    }
}

sub frac_aligned_query {
    my $self = shift;
    my $type = shift;

    if ($self->{length} != 0) {
         return ($self->{length_aln} / $self->{length});
    } else {
         return 0;
    }
}

sub frac_identical {
    my $self = shift;
    my $type = shift;
    if ($self->{length_aln} != 0) {
         return ($self->{hspId} / $self->{length_aln});
    } else {
         return 0;
    }
}


sub start {
    my $self = shift;
    my $type = shift;
    if (lc($type) eq 'query') {
        return $self->{qStart};
    } elsif (lc($type) eq 'sbjct') {
        return $self->{hStart};
    } else {
	return undef;
    }
}

sub end {
    my $self = shift;
    my $type = shift;
    if (lc($type) eq 'query') {
        return $self->{qEnd};
    } elsif (lc($type) eq 'sbjct') {
        return $self->{hEnd};
    } else {
	return undef;
    }
}


sub range {
    my $self = shift;
    my $type = shift;
    if (lc($type) eq 'query') {
        return ($self->{qStart}, $self->{qEnd});
    } elsif (lc($type) eq 'sbjct') {
        return ($self->{hStart}, $self->{hEnd});
    } else {
        return undef;
    }
}


sub seq_str {
    my $self = shift;
    my $type = shift;
    if (lc($type) eq 'query') {
        return $self->{queryString};
    } elsif (lc($type) eq 'sbjct') {
        return $self->{hitString};
    } else {
        return undef;
    }
}


