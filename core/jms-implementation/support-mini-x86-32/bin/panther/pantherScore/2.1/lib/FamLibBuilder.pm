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

package FamLibBuilder;
use FamLibBuilder_build;
use FamLibBuilder_prod;
use FamLibEntry;

# use strict;

# class variables, see defs at bottom
$FamLibVersion = "v1.0";
# END class variables

=head1 NAME

FamLibBuilder - manages access & creation of Panther Family Libraries

=head1 DESCRIPTION

FamLibBuilder manages the access to Panther libraries so that the internal 
directory structure is hidden from the user.  A FamLibBuilder object 
represents one library.  The books and entries within a library are
represented by FamLibEntry objects.  The primary use of a FamLibBuilder object
is to access the FamLibEntries inside it. 

FamLibBuilder acts as a superclass for FamLibBuilder_build and
FamLibBuilder_prod (likewise, FamLibEntry acts a superclass for the "build" 
and "prod" versions of FamLibEntry).  This allows the user to easily access the
directory structure of either the "build" (book-building/development) and 
"prod" (production/released/published) version of the library by simply 
specifying "build" or "prod" for the $flbType.  The $flbType only needs
to be specified when instanciating the FamLibBuilder object.

The default $flbType is "build".  Using the default or specifying "build"
creates the build instance of the FamLibBuilder object.
Specifying "prod" creates the prod instance of the FamLibBuilder object.

=head1 SYNOPSIS

    use FamLibBuilder;

    my $flb = new FamLibBuilder($library,$flbType);
    &usage("Cannot find library $library.\n") unless ($flb->exists());

    # loop over all books in the library
    @books = $flb->bookNames(); 
    foreach my $book (@books) {

        # get book entry
        my $fle = $flb->getLibEntry($book, $type);

	# use FamLibEntry as desired here
	...
    }

=head1 PUBLIC METHODS

=head2 new

Creates and returns a FamLibBuilder object referencing the directory
pointed to by $library.

=item Usage:

    my $flb = new FamLibBuilder($library,$flbType);

=item Parameters:

$library: the filepath to the library directory
$flbType: "build" (default) or "prod" - specifies the "build" or "prod"
          version of the library

=item Returns:

B<a valid FamLibBuilder object> on success

No failure mode defined

=item Notes:

The directory pointed to by $library need not exist at time of instanciation.
Use create() or assureExists() to create the library directory structure.

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified.

=back

=cut

###
### sub new()
###
sub new {
    my $class = shift;
    my $libName = shift;
    my $flbType = shift;
    my $self = {};

    if ((! $flbType) || ($flbType eq "build")) {
	$self->{flb_instance} = new FamLibBuilder_build($libName);
    } elsif ($flbType eq "prod") {
	$self->{flb_instance} = new FamLibBuilder_prod($libName);
    }
    
    bless $self, $class;
    return $self;
}


=head2 exists

Checks to see if Family Library has a directory structure 
on the filesystem.

=item Usage:

    my $bool = $flb->exists()

=item Parameters: 

None.

=item Returns:

B<undef> if directory does not exist or is not a FamLib

B<1> if directory exists and is a valid FamLib

=item Notes:

Does not check for existence of the failed and old directories.  
These directories are created (if they do not already exist) by the 
oldDir method and the failedDir method.

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub exists()
###
sub exists {
    my $self = shift;
    return($self->{flb_instance}->exists());
}


=head2 assureExists

create a Family Library directory structure on the filesystem
if and only if it does not exist.

=item Usage:

    $errcode = $flb->assureExists();

=item Parameters: 

None.

=item Returns:

B<-1> if library already exists

B<undef> on failure

B<1> on success

=item Notes:

B<Either this method or exists() should always be called before
using the FamLibBuilder instance>

Use this method if you are creating a new library entry or are overwriting
an old one.

Use exists() if your code must work from existing library contents.

B<This method supercedes create().>

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub assureExists()
###
sub assureExists {
    my $self = shift;
    return($self->{flb_instance}->assureExists());
}


=head2 getLibEntry

Gets a FamLibEntry object for the requested book.
FLEs are used too access the components of a book.
This is the accepted method to get FLEs ... do NOT call FLE->new()!

=item Usage:

    my $fle = $flb->getLibEntry($bookName, $bookType);

=item Parameters:

B<$bookName>, the book to retrieve

B<$bookType>, the type of entry (ORIG | NR | HMM | HMM0 | ...)

=item Returns: 

B<FamLibEntry> on success

B<undef> on failure

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub getLibEntry 
###
sub getLibEntry {
    my $self = shift;
    my $bookName = shift;
    my $bookType = shift;
    	
    return($self->{flb_instance}->getLibEntry($bookName,$bookType));
}

=head2 bookNames

get the names of all the books in the library

=item Parameters: 

None.

=item Returns: 

An array of the names of all books in the libary.

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub bookNames
###
sub bookNames {
    my $self = shift;
    return($self->{flb_instance}->bookNames());
}






=head2 log

Writes a date-stamped message to the library's log file.

=item Parameters:

B<$message> - what to write ... \n is not implied

B<$logFile> - if passed, write to this file ... undef, write to deflt

=item Returns: 

Nothing.

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub log
###
sub log {
    my $self = shift;
    my $message = shift;
    my $logFile = shift;
    return($self->{flb_instance}->log($message,$logFile));
}


=head2 libDir

Returns the filepath to the library.

=item Parameters: 

None.

=item Returns:

filepath to the library

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub libDir()
###
sub libDir {
    my $self = shift;
    return($self->{flb_instance}->libDir());
}


=head2 bookDir

Returns the path to the books directory.

=item Parameters: 

None.

=item Returns: 

The filepath to the directory containing the books.

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub bookDir() 
###
sub bookDir {
    my $self = shift;
    return($self->{flb_instance}->bookDir());
}


=head2 blastDir

Returns the path to the directory containing blastable databases

=item Parameters: 

None.

=item Returns: 

The filepath to the blast directory.

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub blastDir()
###
sub blastDir {
    my $self = shift;
    return($self->{flb_instance}->blastDir());
}


=head2 threadDir

Returns the filepath to the directory containing threading models
    
=item Parameters: 

None.

=item Returns:

The filepath to the threading directory.

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub threadDir()
###
sub threadDir {
    my $self = shift;
    return($self->{flb_instance}->threadDir());
}


=head2 logDir

Returns the path to the log directory

=item Parameters: 

None.

=item Returns: 

The filepath to the log directory.

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub logDir()
###
sub logDir {
    my $self = shift;
    return($self->{flb_instance}->logDir());
}


=head2 oldDir

Returns the path to the old directory

=item Parameters: 

None.

=item Returns: 

The filepath to the old directory.

=item Notes:

Returns old/ filepath.  If directory does not exist, old/ is created.
There is no corresponding _makeOldDir method.

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub oldDir()
###
sub oldDir {
    my $self = shift;
    return($self->{flb_instance}->oldDir());
}


=head2 failedDir

Returns the path to the failed directory

=item Parameters: 

None.

=item Returns: 

The filepath to the failed directory.

=item Notes:

Returns failed/ filepath. If directory does not exist, failed/ is 
created.  There is no corresponding _makeFailedDir method.

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub failedDir()
###
sub failedDir {
    my $self = shift;
    return($self->{flb_instance}->failedDir());
}

=head2 globalsDir

Returns the path to the globals directory.

=item Parameters: 

None.

=item Returns: 

The filepath to the globals directory.

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub globalsDir()
###
sub globalsDir {
    my $self = shift;
    return ($self->{flb_instance}->globalsDir());
}


=head2 create

Creates a Family Library directory structure on the filesystem.

=item Usage:

    $errcode = $flb->create()

=item Parameters: 

None.

=item Returns:

B<-1> if library already exists

B<undef> on failure

B<1> on success

=item Notes:

This method is depricated in favor of assureExists().  This method does
not create the old and failed directories.  This is done by the oldDir and
failedDir methods.
    
B<TBD: cleanup on failure>

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub create()
###
sub create {
    my $self = shift;
    return($self->{flb_instance}->create());
}


=head2 register

Registers a new object in our library.

=item Parameters:

B<$type> - the type of object

B<$name> - the name of the object

=item Returns: 

Nothing.

=item Notes:

B<Not currently working>
(not currently used, either.)

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub register()
### 
sub register {
    my $self = shift;
    my $type = shift;
    my $name = shift;
    return($self->{flb_instance}->register($type,$name));
}


=head2 defTab

returns path to definitions.tab

=item Usage:

    $defTab = $flb->defTab()

=item Parameters: 

None.

=item Returns:

The filepath to definitions.tab

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub defTab
###
sub defTab {
    my $self = shift;
    
    return ($self->{flb_instance}->defTab());
}

=head2 consensusFasta

returns path to con.Fasta

=item Usage:

    $conFasta = $flb->consensusFasta()

=item Parameters: 

None.

=item Returns:

The filepath to con.Fasta

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub consensusFasta
###
sub consensusFasta{
my $self = shift;

return($self->{flb_instance}->consensusFasta());

}


=head2 namesTab

returns path to names.tab

=item Usage:

    $namesTab = $flb->namesTab()

=item Parameters: 

None.

=item Returns:

The filepath to names.tab

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut
###
### sub namesTab
###
sub namesTab {
    my $self = shift;
    return ($self->{flb_instance}->namesTab());
}

=head2 binHmm

returns path to binary hmm model file of whole library

=item Usage:

    $binHmm = $flb->binHmm()

=item Parameters: 

None.

=item Returns:

The filepath to binary hmm model file of whole library

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut
##
## sub binHmm
##
sub binHmm {
    my $self = shift;
    return ($self->{flb_instance}->binHmm());
}


=head2 superfamTab

returns path to superfam.tab

=item Usage:

    $superfamTab = $flb->superfamTab()

=item Parameters: 

None.

=item Returns:

The filepath to superfam.tab

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub superfamTab
###
sub superfamTab {
    my $self = shift;
    return ($self->{flb_instance}->superfamTab());
}

=head2 blastFasta

returns path to blast.fasta

=item Usage:

    $blastFasta = $flb->blastFasta()

=item Parameters: 

None.

=item Returns:

Filepath to blast.fasta

=item Notes:

Operates via FamLibBuilder_build or FamLibBuilder_prod, depending on $flbType
specified when instanciating FamLibBuilder object.

=back

=cut

###
### sub blastFasta
###
sub blastFasta {
    my $self = shift;
    return ($self->{flb_instance}->blastFasta());
}


=head1 CLASS VARIABLES

=over 4

=item FamLibVersion

    The version of the library structure

=back

=head1 REQUIRES

    Perl5.004, Exporter, FamLibEntry

=head1 AUTHOR

    Anish Kejarwial
    Brian Karlak

=head1 SEE ALSO

    perl(1), FamLibBuilder_build, FamLibBuilder_prod, FamLibEntry, 
    FamLibEntry_build, FamLibEntry_prod, Cluster, FastaFile, Algs::Blast, 
    Algs::Sam

=cut


# uncomment for debug
# &_test;

# required for loading
1;







