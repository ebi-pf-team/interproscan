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

package Hit;
use Hsp;
1; # return value for lib loader


sub new {
    my $class = shift;
    my @hitText = @_;
    my $self = {};
    bless $self, $class;
    $self->init(@hitText);
    return $self;
}

sub init {
    my $self = shift;
    $self->{parent} = shift;
    my @hitText = @_;
    
    # attributes for hit text & hsp retrieval
    $self->{hitText} = \@hitText;
    $self->{numLines} = $#hitText;
    $self->{curLine} = 0;
    $self->{noMoreHits} = undef;

    # parsing setup
    $self->{qStart} = 999999;
    $self->{hStart} = 999999;
    $self->{qEnd} = -1;
    $self->{hEnd} = -1;
    $self->{hitRanges} = ();
    $self->{queryRanges} = ();

    # find all appropriate values
    foreach $line (@hitText) {
	if ($line =~ /^>([\S]+)/) {
	  $self->{name} = $1 unless ($self->{name});
	  chomp $line;
	  $self->{desc} = $line;
	}
	if (($line =~ /Length = ([\d]+)/)) {
	  $self->{length} = $1 unless ($self->{length}); 
	}
	if (($line =~ /Expect(\(\d+\))? = (.*)/)) {
	  $self->{expect} = $2 unless ($self->{expect}); 
        }
	if (($line =~ /Score =[\s]+([\d\.]+) bits \(([\d]+)\),/)) {
	  $self->{bits} = $1 unless ($self->{bits}); 
	  $self->{score} = $2 unless ($self->{score}); 
        }
        if(($line =~ m!Identities = ([\d]+)/([\d]+)!)){
	  $self->{hitId} += $1 unless ($self->{hitId}); 
	  $self->{length_aln} += $2 unless ($self->{length_aln}); 
	}
        if($line =~ m!Positives = ([\d]+)!){
           $self->{hitCons} += $1 unless ($self->{hitCons});
        }
	if ($line =~ m!^Query: ([\d]+) .* ([\d]+)!) {
	    $self->{qStart} = ($1<$self->{qStart})?$1:$self->{qStart};
	    $self->{qEnd} = ($2>$self->{qEnd})?$2:$self->{qEnd};
        }
	if ($line =~ m!^Sbjct: ([\d]+) .* ([\d]+)!) {
	    $self->{hStart} = ($1<$self->{hStart})?$1:$self->{hStart};
	    $self->{hEnd} = ($2>$self->{hEnd})?$2:$self->{hEnd};
        }
    }

    # once we've processed the macro values, then do tiling
    $self->calcTiledRanges();
}

sub name {
    my $self = shift;
    return $self->{name};
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

sub desc {
    my $self = shift;
    return $self->{desc};
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

sub tot_query_aln {
    my $self = shift;
    return $self->{tot_query_aln};
}

sub tot_hit_aln {
    my $self = shift;
    return $self->{tot_hit_aln};
}

sub matches {
    my $self = shift;
    my $type = shift;
    if (lc($type) eq 'id') {
        return $self->{hitId};
    } elsif (lc($type) eq 'cons') {
        return $self->{hitCons};
    } else {
	return undef;
    }
}

sub frac_aligned_query {
    my $self = shift;
    my $type = shift;
    if (lc($type) eq "tot") {
        return ($self->{tot_query_aln} / $self->{parent}->length_query());
    } else {
        return ($self->{length_aln} / $self->{parent}->length_query());
    }   
}

sub frac_aligned_hit {
    my $self = shift;
    my $type = shift;
    if (lc($type) eq "tot") {
        return ($self->{tot_hit_aln} / $self->{length});
    } else {
        return ($self->{length_aln} / $self->{length});
    }
}

sub frac_identical {
    my $self = shift;
    my $type = shift;
    return ($self->{hitId} / $self->{length_aln});
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

sub seekNextHsp {
    my $self = shift;
    my $start = shift;
    $start = $self->{curLine} unless ($start);

    my $i;
    my $line;
    my $isLast = undef;

    # loop from our start position to end-of-array
    # bail if we find the next HSP ("Score")
    for ($i=$start+1 ; $i < $self->{numLines} ; $i++) {
	$line = $self->{hitText}[$i];
        last if ($line =~ /Score /);
    }
    
    # if we didn't go past the end-of-array, return the HSP pos
    # otherwise return last line pos & set the last flag
    if ($i < $self->{numLines}) {
        return ($i, 0);
    } else {
	return ($i, 1);
    }
}


sub nextHsp {
    my $self = shift;
    my $start;
    my $end;
    my $isLast;

    # bail if we're done
    if ($self->{noMoreHsps}){
	#reset $self->{noMoreHsp} which is used to control end of loop
	$self->{noMoreHsps} = 0;
	return undef;
    }
    
    # find the start & end of this HSP
    ($start, $isLast) = $self->seekNextHsp();
    ($end, $isLast) = $self->seekNextHsp($start);

    # return the HSP if we've got it
    if ($start && $end) {
	$end--;
        @hsp = @{$self->{hitText}}[$start..$end];
	$self->{curLine} = $end;
	$self->{noMoreHsps} = 1 if ($isLast);
	return new Hsp(@hsp);
    } else {
        return undef;
    }
}


# loop over all HSPs to make the correct tiling
sub calcTiledRanges {
    my $self = shift;
    my $oldCurLine = $self->{curLine};
    
    while (my $hsp = $self->nextHsp()) {
        my @qrange = $hsp->range('query');    
        my @hrange = $hsp->range('sbjct');
        &mergeRanges(\$self->{queryRanges}, @qrange);
        &mergeRanges(\$self->{hitRanges}, @hrange);
    }

    # sort & remove deletes
    $self->{queryRanges} = &cleanRanges($self->{queryRanges});
    $self->{hitRanges} = &cleanRanges($self->{hitRanges});

    # calc total lengths
    $self->{tot_query_aln} = &sumRanges($self->{queryRanges});
    $self->{tot_hit_aln} = &sumRanges($self->{hitRanges});

    $self->{curLine} = $oldCurLine;  # rewind
}

sub mergeRanges {
    my $oldRanges = shift;
    my @newRange  = @_;
    my $newStart  = $newRange[0];
    my $newEnd    = $newRange[1];

    # if this is first one, push on stack
    unless (@{$$oldRanges}) {
	push (@{$$oldRanges}, \@newRange);
	return;
    }

    # otherwise review all hit ranges
    # basic alg is: if overlap, merge (ie, extend to new range)
    # otherwise push new range on stack
    # if overlap with multiple, merge the old ranges
    my $startMerge;
    my $endMerge;
    my $skipFlag = 0;
    foreach $range (@{$$oldRanges}) {
	my $oldStart = $$range[0];
        my $oldEnd = $$range[1];

	next if (!$oldStart && !$oldEnd);    # skip if "deleted"

	if (&between($newStart, $oldStart, $oldEnd)) {
	    if (&between($newEnd, $oldStart, $oldEnd)) {
                $skipFlag = 1;
		last; # skip, we are enclosed in another HSP
	    } else {
		$$range[1] = $newEnd;  # extend towards new end
		$endMerge = $range;
	    }
	} else {
	    if (&between($newEnd, $oldStart, $oldEnd)) {
		$$range[0] = $newStart; # extend towards new start
		$startMerge = $range;
	    } else {
                # check we don't enclose this HSP ourselves!
                # if we do, then replace it with ourselves
	        if ((&between($oldStart, $newStart, $newEnd)) &&
	               (&between($oldEnd, $newStart, $newEnd))) {
                    $$range[0] = $newStart;
                    $$range[1] = $newEnd;
                    $skipFlag = 1;
		    last; # skip, we enclosed this HSP
                } else {
		    # no overlap == no action just yet, will push later
                }
	    }
	}
    }

    # if we merged on both sides, merge the merges
    # "delete" (set to zero) one copy
    if ($startMerge && $endMerge) {
	$$startMerge[0] = $$endMerge[0];
	$$endMerge[0] = 0;
	$$endMerge[1] = 0;
    }

    # otherwise if we got no overlaps push our range on the stack
    if (!$skipFlag && !$startMerge && !$endMerge) {
	push (@{$$oldRanges}, \@newRange);
	return;
    }
}
	

sub cleanRanges {
    my $ranges = shift;
    my $clean;

    foreach my $range (@{$ranges}) {
        next if (! $$range[0] && ! $$range[1]);
        push(@{$clean}, $range);
    }

    @{$clean} = sort {$$a[0] <=> $$b[0]} @{$clean};
    return $clean;
}

sub sumRanges {
    my $ranges = shift;
    my $len = 0;

    foreach my $range (@{$ranges}) {
        $len += ($$range[1] - $$range[0] + 1);
    }

    return $len;
}

# true if first number is between second & third numbers
# endpoints are included
sub between {
    my $x = shift;
    my $low = shift;
    my $high = shift;

    return 1 if (($x >= $low) && ($x <= $high));
    return 0;
}





