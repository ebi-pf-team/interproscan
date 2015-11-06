#!/usr/local/bin/perl

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

#-------------------------------------------------------------
# FastaFile.pm - simple record-by-record interface to fasta files
#-------------------------------------------------------------

###########################
# Required Module variables

use strict;
use FileHandle;
package FastaFile;

####
#
# sub new creates a new FastaFile object.  Feed it a filename to read in 
#     and an option indexing file
#
# usage: my $fastaFile = new FastaFile("file.fasta", "file.idx")
#
# returns undef on errors: no such file, unreadable file, bad index, etc
#
###

sub new {
    my $class = shift;
    my $passFile = shift;
    my $indexFile = shift;

    # create ourselves & bless 
    my $self = {};
    bless $self, $class;

    # check to see what $passFile is and initialized correctly
    if (ref($passFile) eq "GLOB") {
	# do not allow indexing on raw filehandles
	return undef if ($indexFile);
	$self->_initFileHandle($passFile);
    } else {
	return undef unless ($self->_initFileName($passFile));
        # add indexing & test to see if we are set up OK
	if ($indexFile) {
	    # make index file platform-specific
            my $uname = `uname`;
	    chop $uname;
            $indexFile .= ".$uname";

            # check index exists & is up-to-date
	    die "Can't find index file $indexFile!" 
		unless ((-f "$indexFile.dir" && -s "$indexFile.dir") && (-f "$indexFile.pag" && -s "$indexFile.pag"));
	    warn "**** WARNING: $indexFile out of date for $passFile!!\n" 
		unless (-M "$indexFile.dir" <= -M "$passFile");
	    
	    my %indexHash;
	    dbmopen %indexHash, $indexFile, 0444;
       	    $self->{index} = \%indexHash;
        } else {
	    $self->{index} = undef;
	}
    }


    # return
    return $self;
}

sub _initFileName {
    my $self = shift;
    my $file = shift;

    # test to see if we are set up OK
    if (! -f $file) {
	$self = undef;
	return undef;
    }

    # create our filehandle object
    my $fh = new FileHandle;

    # set ourselves up
    $self->{isFH} = undef;
    $self->{file} = $file;
    $self->{fh} = $fh;
    $self->{lastLine} = undef;
    return $fh;
}

sub _initFileHandle {
    my $self = shift;
    my $fh = shift;

    # create a file handle object
    my $fhObj = new_from_fd FileHandle($fh, "r");

    # set ourselves up
    $self->{isFH} = 1;
    $self->{file} = undef;
    $self->{fh} = $fhObj;
    $self->{lastLine} = undef;
}

###
#
# sub open opens the fasta file for reads
#     returns file handle if successful
#     (if file is already opened, does nothing but returns filehandle)
#     returns undef on failure
#
# note: the returned file handle should not normally be used directly;
#       instead, use nextSeq() and other methods
#
# usage: $fasta->open()
#
###
sub open {
    my $self = shift;

    # if we were instanciated with a file handle, don't open ourselves
    return if ($self->{isFH});

    # otherwise open the file
    $self->{fh}->open($self->{file}, "r");

    # open up the index if we have it
    if ($self->{indexFile}) {
	my %index;
	return undef unless (dbmopen %index, $self->{indexFile}, 0444);
	$self->{index} = \%index;
    }

    # store reference to filehandle & return it
    return $self->{fh};
}


###
#
# sub close closes an open fasta file
#     returns nothing
#
# usage: $fasta->close()
#
###
sub close {
    my $self = shift;

    close $self->{fh} if ($self->{fh});
    dbmclose %{$self->{index}} if (ref($self->{index}) eq "HASH");
    $self->{fh} = undef;
    $self->{index} = undef;
}


###
#
# sub nextSeq returns the next sequence in a fasta file
#     returns undef if no more sequences / file not opened
#     currently, sequence returned as text block
#
# usage: while ($seq = $fastaFile->nextSeq()) {...}
#
###
sub nextSeq {
    my $self = shift;

    my $fh = $self->{fh};

    my $line;
    my @seq;

    # bail if not opened
    return undef unless ($self->{fh});

    # add in the last line of the previous read if we have it,
    # otherwise get the first '>' line
    if ($self->{lastLine}) {
	push(@seq, $self->{lastLine});
    } else {
	while ($line = <$fh>) {
	    last if ($line =~ /^>/);
	}
	push(@seq, $line) if ($line);
    }


    # now read in the sequence
    while ($line = <$fh>) {
	last if ($line =~ /^>/);
        push (@seq, $line);
    }
    
    # store the title line of the next sequence for the next read
    $self->{lastLine} = $line;
    
    # return our sequence or undef
    if (@seq) {
	return (join("",@seq));
    } else {
	return undef;
    }
}

######
# sub revSeq - reverses the chraracters in the sequence portion of a
#              fasta record. If "characters" are non-traditional,
#              supply separator as an additional parameter.
# @params: $seqRec, the fasta format sequence record to operate on
#          $sep, the separator between seq positions, assumed to 
#                be '' unless specfifed.
# @return: the record with the sequence positions reversed
######
sub revSeq {
	my $seqRec = shift;
	my $sep = shift;
	unless ($sep) {$sep = ""};
	
	my ($seq, $title) = FastaFile::splitFasta($seqRec);
	my $revTitle = $title . "_rev";
	$seq =~ s/\n//g;
	$seq =~ s/\cM//g;
	my @tmp = split("$sep", $seq);
	my @revTmp = reverse(@tmp);
	my $revSeq = join("$sep", @revTmp);
	my $retVal = FastaFile::joinFasta($revSeq, $revTitle);
	return $retVal;
}

####
# sub stripTitle() replaces the title of the sequence with 
# just the name or the ID of the sequence
#
# @params: $seq, the sequence to get the name from
#          $type, "name" for name or "id" for id
#                  does id if undef
#          $id, the id portion (gi, etc) of the field to return -- see id()
#                  does gi on undef
# @return: the sequence with only the name (ie first word) or id on the title
####
sub stripTitle {
    my $seq = shift;
    my $type = lc(shift);
    my $id = lc(shift);

    my ($text, $title) = splitFasta($seq);
    if ($type eq "name") {
	$title = name($seq);
    } else {
	$title = id($seq, $id);
    }
    $title =~ s/[\W]+$//;              #strip out trailing punctuation as well
    return joinFasta($text, $title);
}

####
# sub name() gets the name of the sequence
#
# @params: $seq, the sequence to get the name from
# @return: the name (ie first word) of the sequence
####
sub name {
    my $seq = shift;
    my ($text, $name) = &splitFasta($seq);
    ($name) = split(/[\s]+/, $name);
    return $name;
}

####
# Sub id is a MAG-specific function.  It returns the requested ID portion
# of a NCBI-style complex name, or the name of the sequence if not in NCBI
# format
#
# params: $type = {gi|spid|spacc|$key}.  If undef, returns gi
#                     $key returns value of key=value or 
#                     key|value pair via getValueForKey sub

# return: the appropriate id of the sequence, or the bare name
####
sub id {
    my $seq = shift;
    my $type = lc(shift);
    my $name = &name($seq);

    # ret name if not NCBI (|) or Celera (/)
    unless ($name =~ /[\|\/]/)  {
	$name =~ s/[\W]+$//;
	return $name;
    }

    # parse name
    if (! $type || $type eq "gi") {
        return $1 if ($name =~ /gi\|([\d]+)/);
    } elsif ($type eq "spid") {
	return $2 if ($name =~ /sp\|([\w]+)\|([\w]+)/);
    } elsif ($type eq "spacc") {
	return $1 if ($name =~ /sp\|([\w]+)\|([\w]+)/);
    } else {
        #$type is the $key for the $key / $value pair - get $value
        my $id = &getValueForKey($seq,$type);
        return $id if (defined($id));
    }

    # on failure, just return the name
    # but remove trailing punctuation
    $name =~ s/[\W]+$//;
    return $name;
}

####
# sub getValueForKey - title lines for sequences often contain multiple
# sequence IDs in a key=value or key|value list format.  
# This sub should be used through the id subroutine, rather than directly 
# Two formats exist:  key|value   key=value (key can start with ^ or /)
#
# params: $seq
#         $key (key of key-value pair) - if not provided first id is returned
# return: the appropriate value for the key 
####
sub getValueForKey {
    my $seq = shift;
    my $key = shift;

    my ($seqText,$title) = &splitFasta($seq,"1");

    my ($value) = $title=~m/(?:[\/]|^)$key[=|]([^\/]+)/i;
    
    #get rid of leading and trailing spaces
    $value=~s/^\s+//;
    $value=~s/\s+$//;
    
    return($value);
}

# sub splitFasta takes a fasta file & splits it into
# sequence & name portions
#
# pass $title =1 to return entire title line, otherwise just get name back
sub splitFasta {
    my $fasta = shift;
    my $title = shift;
    my @fasta = split("\n", $fasta);
    my $name;

    if ($fasta[0] =~ m!>!) {
	if ($title) {
	    $name = $fasta[0];
	    $name =~ s/^>//;
	    shift (@fasta);
	} else {
            if ($fasta[0] =~ m!>[\s]*([^/\s]+)!) {
                $name = $1;
                shift(@fasta);
            } elsif ($fasta[0] =~ m!>([\w-]*)!) {
                $name = $1;
                shift(@fasta);
            }
	}
    }
    return (join("\n", @fasta), $name);
}

# sub getSeqText takes a fasta seq, and returns back seq text with no
# definition line, and no spaces
sub getSeqText {
    my $fasta = shift;
    
    my ($seqText,$title) = splitFasta($fasta);
    $seqText=~s/\s+//gm;
    return $seqText;
}




# sub joinFasta takes a sequence & name and creates a fasta
# file from it
sub joinFasta {
    my ($seq, $name) = @_;

    $seq .= "\n" unless ($seq =~ /\n/);
    return (">$name\n$seq\n");
}


###
#
# sub find looks for the requested sequence
#     returns the sequence block if requested
#     returns null if sequence not found - file will be at EOF if no index
#
#     file will be pointing at sequence after this one / EOF
#     in non-indexed version, REWIND DONE BEFORE EACH SEARCH if $rewind set!
#     (yes, this is inefficient, but necessary.)
#
# usage: $fasta->find($seqId)
#
###
sub find {
    my $self = shift;
    my $seqId = shift;
    my $rewind = shift;

    my $line;
    my $fh = $self->{fh};

    # if indexed, seek and return that sequence
    if ($self->{index}) {
	my $seekPos;
	return undef unless (defined($seekPos = $self->{index}->{$seqId}));
	return undef unless (seek $self->{fh}, $seekPos, 0);
	$self->{lastLine} = undef;
	
	# Check to make sure you really found the id you're looking for
	my $seq = $self->nextSeq();
	if(  $seq =~ m/[>\|]$seqId[\|\s]/){
	    return $seq;
	} else {
	    return undef;
	}
	return $seq;

    # if not indexed, scan until we find a match
    } else {
	seek($fh, 0, 0) if ($rewind);   # rewind the file first
	while ($line = <$fh>) {
	    if ($line =~/^>/) {
		my @idLines = split (/\cA/, $line);
		chomp @idLines;
		foreach my $idLine (@idLines){
		    #remove > and grab first word
		    $idLine=~s/^>//;
		    $idLine=~s/^(\S+).*/$1/;

		    my $foundId;
		    if ($idLine=~/\|/) {
			#id must have pipe at beginning, &pipe or space at end
			$foundId = 1 if ($idLine=~/\|$seqId[\|\s]/);
		    } else {
			#pipe does not exists, so just look at first word
			$foundId = 1 if ($idLine eq $seqId);
		    }
		    
		    #if the $seqId has been found, we found the match
		    if ($foundId) {
			$self->{lastLine} = $line;
			return $self->nextSeq();
		    }
		}
	    }
	}
	return undef;
    }
    # just a dummy assertion, should never reach
    return undef;
}

#####
# sub internalIndex - builds in-memory index hash, based on FASTA IDENTIFIER
#                     (i.e. what this module calls "name", or, regexpishly,
#                      /^>(\S+)/. Applications will need to modify return values if some
#                      other portion is desired.) and an ordered list of seq names.
#                    - use for fasta files that don't have an index file.
# 
# returns - <hash reference>  all seqs hashed by "name" as described above.
#         - <array reference> list of all names in file order.
#
# usage: my ($indexRef, $namesRef) = $fasta->internalIndex();
#
# example use of return info:
#        my @found_names;
#        foreach my $name (@{$namesRef}) {
#          # Do something to differentiate some of the seqs...
#          push(@found_names, $found);
#        }
#
#        foreach my $i (@found_names) {
#          # print seq records that passed the test
#          print $indexRef->{$i} 
#        } 
#         
#####
sub internalIndex {
	my $self = shift;
	my $href;
	my $aref;
	
	while (my $rec = $self->nextSeq()) {
		my $name = FastaFile::name($rec);
		$href->{$name} = $rec;
		push(@{$aref}, $name);
	}
	return ($href, $aref);
}

# sub cleanLines makes all lines the same length
sub cleanLines {
    my($seq, $len) = @_;
    my @ret;
    $len = 70 unless ($len);

    $seq =~ s/[\s\.-]//g;
    while ($seq) {
        push(@ret, substr($seq, 0, $len)."\n");
        $seq = substr($seq, $len);
    }
    return(@ret);
}

#####
# sub sortByLen - sorts array of fasta seqs by length
#               - call getSeqs
# parameters:  @cluster - unsorted cluster
#              $reverse - flag to sort from smallest to largest
# @return - the sorted array
#####

sub sortByLen {   
    my $reverse = shift;
    my @clust = @_;

    my @sort;
    if ($reverse) {
        @sort = sort byLength_rev @clust;
    } else {
        @sort = sort byLength_fwd @clust;
    }
    return @sort;

    # inner class functions to do sort
    sub byLength_fwd{
	my ($aseq, $aname)=splitFasta($a);
	my ($bseq, $bname)=splitFasta($b);
        return length($bseq) <=> length($aseq);
    }
    sub byLength_rev{
	my ($aseq, $aname)=splitFasta($a);
	my ($bseq, $bname)=splitFasta($b);
	return length($aseq) <=> length($bseq);
    }
}

#####
# sub getSeqs - returns all seqs in the fasta file in an array
#
#####

sub getSeqs{
    my $self = shift;
    my $fh = $self->{fh};
    my @origAlign = ();
    my $seq;

    ### File foo.
    my $cp = $self->tell();
    ### Get ALL seqs.
    $self->rewind();

    while( $seq = $self->nextSeq() ){
	push @origAlign, $seq; 
    }
    ### Reset file pointer.
    $self->seek($cp);

    return( @origAlign ) ;
}

###
# fastaDiff function takes two fasta files as input and
#           returns a list of id's that are different
#           between the two files.
#
# params:
#      oldFile, original fasta file
#      newFile, new fasta file 
# 
###
sub fastaDiff{
    my $oldFile = shift;
    my $newFile = shift;
    my $identicalFlag1 = 0;
    my $identicalFlag2 = 0;
    my @diffIds1 = ();
    my @diffIds2 = ();
    my @diffIds = ();
    

    # Check input
    return undef if (not -s $oldFile);
    return undef if (not -s $newFile);

    # Create file objects for each file and read in ids
    my $oldObj = new FastaFile( $oldFile );
    my $newObj = new FastaFile( $newFile );
    $oldObj->open();
    $newObj->open();
    my @oldIds = $oldObj->getIds();
    my @newIds = $newObj->getIds();
 
    # Find the difference between inputFile and inputFile2
    my( %MARK );
    grep( $MARK{$_}++, @oldIds);
    my @result1 = grep ( !$MARK{$_}, @newIds );

    # Set flag if still
    if ( scalar @result1 < 1){
	$identicalFlag1 = 1;
    }

    foreach my $r ( @result1 ){
	push @diffIds1, $r;
    }


    # Find the difference between inputFile and inputFile2
    my( %MARK2 );
    grep( $MARK2{$_}++, @newIds);
    my @result2 = grep ( !$MARK2{$_}, @oldIds );

    foreach my $r ( @result2 ){
	push @diffIds2, $r;
    }

    push @diffIds, @diffIds1;
    push @diffIds, @diffIds2;

    return( @diffIds );
}

#####
# sub getIds returns all ids in the fasta file in an array
# 
# params:
#       $self, fasta file object
#       $type, type of id (see list of acceptable types in id() )
#####
sub getIds{
    my $self = shift;
    my $type = shift;
    my @idList = ();
    my $seq;
    my $id;

    # File foo.
    my $fh = $self->{fh};
    my $cp = $self->tell();
    ### Get ALL ids from file.
    $self->rewind();

    # Set defaults
    $type = undef if ( not $type );

    while( $seq = $self->nextSeq() ){
	$id = &id( $seq , $type);
	push @idList, $id;
	$id = "";
    }
    ### Restore position in file.
    $self->seek($cp);

    ### Return what was asked for.
    return( @idList ) ;
}

#####
# sub updateId takes an input id, and "updates" the id to the most current
#     version in the input fasta file.  
#         -this method is most commonly used to find the most up-to-date 
#          genbank ID in the nr fasta file.
#         -because the find method is used by the updateId method, it is very 
#          strongly recommended that when the fasta object is created, it is 
#          created with the fasta file index
#         -if the input ID does not exist in the fasta file, or has not been   #          updated, the input ID is returned.
#
# usage: my $newId = $fasta->updateId($id,$idType);
#
# params:
#         $self, fasta file object
#         $id, id to be updated
#         $idType, id type required to parse $newId from $nrfasta (uses the
#                  id method)
# return:
#         $id - most up-to-date id
####

sub updateId {
    my $self = shift;
    my $id = shift;
    my $type = shift;
    
    my $seq = $self->find($id,"1");
    
    if ($seq){
        #sequence was returned - id exists in fasta file
        return (&id($seq,$type));
    }
    else{
	#returns back original id if id does not exists in fasta file
        return ($id);
    }    
}


#####
# sub rewind returns the file pointer to the top 
#            of the file for (re)reading.
#
# params: 
#        $self, fasta file object.
#####

sub rewind {
    my $self = shift;
    my $fh = $self->{fh};
    seek($fh, 0,0);
}

#####
# sub tell returns the current file pointer.
#
# params: 
#        $self, fasta file object.
#####

sub tell {
    my $self = shift;
    my $fh = $self->{fh};
    tell($fh);
}

#####
# sub seek sets the file pointer to the specified
#          position in the file.
#
# params: 
#        $self, fasta file object.
#        $pos, the position to point to.
#####

sub seek {
    my $self = shift;
    my $fh = $self->{fh};
    my $pos = shift;
    seek($fh, $pos, 0);
}


###
#
# sub test0 tests simple non-indexed, non-find fasta reads
#
###
sub test0 {
    my $fasta = new FastaFile("test.fasta");
    $fasta->open();

    print "First Sequence: \n";
    print $fasta->nextSeq();
    print "Done\n";

    print "Rest of Sequences:\n";
    my $seq;
    while ($seq = $fasta->nextSeq()) {
	print $seq;
    }
    print "Done.\n";

    $fasta->close();
    $fasta->open();
    print "First Sequence: \n";
    print $fasta->nextSeq();
    print "Done\n";
    $fasta->close();
}

###
#
# sub test1 tests simple non-indexed, finds
#
###
sub test1 {
    my $fasta = new FastaFile("test.fasta");
    $fasta->open();

    print "Sequence 1dxtb: \n";
    print $fasta->find("1dxtb");
    print "Done\n";

    print "Sequence 1cpcl: \n";
    print $fasta->find("1cpcl");
    print "Done\n";

    print "Sequence 1onc_: \n";
    print $fasta->find("1onc_");
    print "Done\n";
}


###
#
# sub test2 tests simple indexed finds
#
###
sub test2 {
    my $fasta = new FastaFile("test.fasta", "test.idx");
    $fasta->open();

    print "Sequence 1dxtb: \n";
    print $fasta->find("1dxtb");
    print "Done\n";

    print "Sequence 1cpcl: \n";
    print $fasta->find("1cpcl");
    print "Done\n";

    print "Sequence 1onc_: \n";
    print $fasta->find("1onc_");
    print "Done\n";
}



###
#
# sub test3 tests indexed finds against NR
#
###
sub test3 {
    my $fasta = new FastaFile("/mag/research/DB/fasta/nr.aa", 
			      "/mag/research/data/dbIndexes/nr.brk");
    $fasta->open();

    print "Sequence 544311: \n";
    print $fasta->find("544311");
    print "Done\n";

    print "Sequence DHET_ACEPO: \n";
    print $fasta->find("DHET_ACEPO");
    print "Done\n";

    print "Sequence 4754485: \n";
    print $fasta->find("4754485");
    print "Done\n";
}

####
#
# sub tests function getIds()
#
####
sub testGetIds(){
    my $printFlag = shift;
    my $fname = "/mag/research/data/testdata/FastaFile.pm.GetIds";
    my $expect = 22;
    
    my $ff = new FastaFile($fname);
    if ($ff){
	$ff->open();
	my @ids = $ff->getIds();
	
	# Print results
	print "-"x40, "\n";
	print "TESTING: FastaFile::getIds()\n";
	print "\tEXPECT: $expect seq ids\n";
	print "\tACTUAL: ",scalar @ids, " seq ids\n";
	print @ids, "\n" if ($printFlag);
	if ($expect == scalar @ids){
	    print "PASS TEST\n";
	}
	else{
	    print "FAIL TEST\n";
	}
	print "-"x40, "\n";
    }
    else{
	print "test fails for FastaFile::getIds()\n";
    }
    

}

####
#
#
#
####
sub testDiffIds{
    my $file1 = "/mag/research/data/testdata/FastaFile.pm.DiffIds1.fasta";
    my $file2 = "/mag/research/data/testdata/FastaFile.pm.DiffIds2.fasta";
    my $file3 = "/mag/research/data/testdata/FastaFile.pm.DiffIds3.fasta";
    
    my @diffIdsSame = &fastaDiff( $file1, $file2);
    my @diffIdsDiff = &fastaDiff( $file1, $file3);
	
    print "-"x40, "\n\n";
    print "TESTING: FastaFile::fastaDiff()\n";
    print "These two files should be identical:\n";
    print "\tFile1: $file1\n";
    print "\tFile2: $file2\n";
    print "\tDiff Ids: @diffIdsSame\n";

    print "-"x40, "\n\n";
    print "TESTING: FastaFile::fastaDiff()\n";    
    print "These two files should be different:\n";
    print "\tFile1: $file1\n";
    print "\tFile3: $file3\n";
    print "\tDiff Ids: @diffIdsDiff\n";

    print "-"x40, "\n\n";
}




# uncomment for testing
#&test0;
#&test1;
#&test2;
#&test3;
#&testGetIds();
#&testDiffIds();

1; # required for library loading - do not remove

