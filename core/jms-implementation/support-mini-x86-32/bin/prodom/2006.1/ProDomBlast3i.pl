#!/ebi/extserv/bin/perl/bin/perl

# $Id: ProDomBlast3i.pl,v 1.2 2005/04/27 16:43:42 tuco Exp $
# $Log: ProDomBlast3i.pl,v $
# Revision 1.2  2005/04/27 16:43:42  tuco
# Updated  assignment.pl
#
# Revision 1.1.1.1  2005/01/05 16:48:58  tuco
# Exportable InterProScan project
#
# Revision 1.4  2004/09/06 16:15:20  tuco
# Fixed little bug while reading the input sequence file.
#
# Revision 1.3  2004/06/01 14:52:38  tuco
# Added the $ENV{IPRSCAN_HOME} setting at the begining of the file.
#
# Revision 1.2  2004/05/17 13:57:38  tuco
# Introduced new option to print or not message when BlastProDom is running (-v : verbose).
# Fixed bug on the loop which is writting temp sequence file.
#
# Revision 1.1  2004/03/01 15:32:40  tuco
# New implementation for BlastProDom 2004. It uses XML module (see lib directory)
# and a .c parser to parse Blast xml output and create results.
#
# Revision 1.13  2004/01/05 15:19:50  beausse
# Accepte des fichiers query multiples
#
# Revision 1.8  2003/10/21 14:12:33  manu
# changes blastp parameters
#
# Revision 1.7  2003/10/21 12:43:36  manu
# blastpgp ==> blastp
#
# Revision 1.6  2003/10/17 11:04:58  manu
# meilleure analyse de la ligne de commande
#
# Revision 1.5  2003/08/22 12:04:30  beausse
# Modification du parseur XML. Utilisation du parseur C
#
# Revision 1.3  2003/06/18 07:36:55  beausse
# *** empty log message ***
#
# Revision 1.2  2003/06/18 06:50:59  manu
# bug (nice)
#
# Revision 1.1  2003/06/18 06:08:47  manu
# ProDomBlast for Interpro
#
#

=pod
=head1 NAME

ProDomBlast3i.pl

=head1  SYNOPSIS

=over 5

=item ./ProDomBlast3i.pl  -d <DataBase> -s <SequenceFile>

=item [-P <path for your blast program (blastall)> (default in "$BLAST_PATH")]

=item [-p < blastp | blastx > (default in "$PROG_BLAST")]

=item [-f    (do non overlap HSP filtering)]

=item [-t <temp directory> (default "/tmp")]

=item [-o <OutputFile> (default is 'STDOUT')]

=item [-z <sizeFullProDomBank> (default is '$DEFAULT_Z_VALUE')]

=item [-h <'0' for no header|'1' for displaying header> (default is '1')]

=head1 DESCRIPTION

 Yoann BEAUSSE / Emmanuel COURCELLE  INRA Toulouse LIPM     May 16th 2003
 beausse@toulouse.inra.fr
 emmanuel.courcelle@toulouse.inra.fr

 This script displays the decomposition of a protein sequence
 into domains according to a BLAST search against the ProDom database

 Format of the source database:
 >7LES_DROME#PD000001#2210#2484 | 275 | pd_PD000001;sp_P13368_7LES_DROME; | (2773)  KINASE PROTEIN TRANSFERASE ATP-BINDING

 Option for formatdb:
    formatdb -d <database> -p T -o T

 OUTPUT FORMAT:
    query_name  begin  end // Closest_domain;  begin  end // S=Score  E=Expect [F=Frame] // (FamilySize) Commentary

=cut

=head1 MAIN de ProDomBlast3i.pl

=cut

BEGIN
{

# TODO It would be better if the paths to IPRSCAN_HOME and IPRSCAN_LIB were relative paths, or
# could be configured as a parameter and passed into this perl script.

# IPRSCAN_HOME = Path to ParseBlastXmlReport binary
$ENV{IPRSCAN_HOME} = "bin/prodom/2006.1";
  unless($ENV{IPRSCAN_LIB}){
    # IPRSCAN_LIB = Location of the "calcs" directory
    $ENV{IPRSCAN_LIB} = "bin/prodom/2006.1/calcs";
  }
}



########################################################
# GLOBAL VARIABLES, MODULES and LIBRARIES
########################################################
use strict;
use warnings;
use Getopt::Long qw(:config no_ignore_case);
use lib $ENV{IPRSCAN_LIB};
use XML_BLAST::Report1;

# Global Var
my $BLAST_PATH      = "/usr/local/blast/";
my $PROG_BLAST      = "blastp";
my $DEFAULT_Z_VALUE = 70000000;
my $DEFAULT_TMP     = "/tmp";

=head2 MAIN

=cut

  ####################################################
########################################################
##             M A I N      P R O G R A M             ##
########################################################
  ####################################################

MAIN:
{

    my ($report, %h_parameter);

    &ReadCommandLine(\%h_parameter);

    if ( defined( $h_parameter{'outfile'} ) )
    {
	unlink ( $h_parameter{'outfile'} );
    }

    open ( QUERY,$h_parameter{'seqfile'} ) or die "No input file given";
    local $/ = "\n>"; #Take one sequence in one go
    my $tmpfile;
    my $nseq = 1;
    my $orig = $h_parameter{'seqfile'};

    while(<QUERY>){
      chomp $_;
      next unless $_;

      ## rpetry - 07/08/2006 - Added "$curUpi_" to the name of the tmpfile, because when
      ## lots of PRODOM jobs are run on the same cluster node at the same time, the script
      ## appears to get confused and output results from one jobs input sequences to the
      ## other jobs' output. doh!!!

      s/^>//;

      # Example ProDom protein Id: UPI000196C5BF
      #$_ =~ /^(UPI[0-9A-Fa-f]{10})/; ## rpetry - 07/08/2006 - specific to Onion

      # Example i5 protein Id: 1
      $_ =~ /^(\d+)/; ## rpetry - 07/08/2006 - specific to Onion

      my $curUpi = $1; ## rpetry - 07/08/2006 - specific to Onion

      $tmpfile = $h_parameter{'tmp_path'} . "/BLAST_PRODOM_TMP_${curUpi}_" . time() . $$ . ".$nseq";  ## rpetry - 07/08/2006 - specific to Onion

      unless(open(TMP, ">$tmpfile")){
	close(QUERY);
	unlink($tmpfile);
	die "Could not open in write mode file $tmpfile : $!";
      }
      print TMP ">$_";
      close(TMP);
      $h_parameter{'seqfile'} = $tmpfile;
      local $/ = "\n"; #Need to redefine the input record delimeter otherwise
		       #the results parsing would fail.

      $report = BLAST_INTERPRO::Blast->new(\%h_parameter);

      $report->nice(0);
      $report->execute_blast();
      $report->print_result();
      unlink ($tmpfile);
      $nseq++;
      $h_parameter{'seqfile'} = $orig;
      $tmpfile = "";
      local $/ = "\n>"; #Then redefine it to take one sequence in one go.
    }
    close(QUERY);

}


#----------------------------------------------------------------

=head2 Usage()

 Title        : Usage
 Usage        : Usage()
 Function     : Print how to use this script

=cut

#----------------------------------------------------------------
#
#   Usage()
#
sub Usage
{
    print STDERR " Usage :\t $0 \n";
    print STDERR "\t\t\t[-P <path for your blast program (blastall)> (default is '$BLAST_PATH')] \n";
    print STDERR "\t\t\t[-p < blastp | blastx > (default is '$PROG_BLAST')] \n";
    print STDERR "\t\t\t -d <DataBase> \n";
    print STDERR "\t\t\t -s <SequenceFile> \n";
    print STDERR "\t\t\t[-f    (do non overlap HSP filtering)]\n";
    print STDERR "\t\t\t[-t <temp directory> (default '/tmp')]\n";
    print STDERR "\t\t\t[-o <OutputFile> (default is 'STDOUT')]\n";
    print STDERR "\t\t\t[-z <sizeFullProDomBank> (default is '$DEFAULT_Z_VALUE')]\n";
    print STDERR "\t\t\t[-h <'0' for no header|'1' to display header> (default is '1')]\n";
    print STDERR "\t\t\t[-H to display this screen]\n";
    print STDERR "\t\t\t[-v print messages during execution in the directory where script is installed]\n";
    exit (2);
}    # end Usage

#----------------------------------------------------------------

=head2 ReadCommandLine()

 Title        : ReadCommandLine
 Usage        : ReadCommandLine($args)
 Function     : Read the parameters in the command line
 Args         : $args Reference sur un %hash

=cut

#----------------------------------------------------------------
#
#   ReadCommandLine()
#
sub ReadCommandLine
{
    my $rh_parameter = shift;

    my($blast_path, $program, $database, $z_value, $db, $seqfile, $outfile, $tmp, $filter, $header, $verbose, $help);

    unless(GetOptions('P=s' => \$blast_path,
		      'p=s' => \$program,
		      'd=s' => \$database,
		      's=s' => \$seqfile,
		      'o=s' => \$outfile,
		      'f'   => \$filter,
		      't=s' => \$tmp,
		      'v'   => \$verbose,
		      'z=i' => \$z_value,
		      'h=i' => \$header,
		      'H'   => \$help
		     )){
      &Usage();
    }

    Usage() if($help);

    $rh_parameter->{'blast_path'} = $blast_path || $BLAST_PATH;
    $rh_parameter->{'program'}    = $program    || $PROG_BLAST;
    $rh_parameter->{'z_value'}    = $z_value    || $DEFAULT_Z_VALUE;
    $rh_parameter->{'db'}         = $database   || undef;
    $rh_parameter->{'seqfile'}    = $seqfile    || undef;
    $rh_parameter->{'outfile'}    = $outfile    || undef;
    $rh_parameter->{'filter'}     = $filter     || 0;
    $rh_parameter->{'tmp_path'}   = $tmp        || $DEFAULT_TMP;
    $rh_parameter->{'header'}     = $header     || 0;
    $rh_parameter->{'verbose'}    = $verbose    || 0;

    if (   !defined($rh_parameter->{'db'})
        || ($rh_parameter->{'db'} eq "")
        || !defined($rh_parameter->{'seqfile'})
        || ($rh_parameter->{'seqfile'} eq ""))
    {
        print STDERR "\nInvalid command line\n";
        &Usage();
    }
}    # end ReadCommandLine

#----------------------------------------------------------------

=head1 SUBROUTINES de BLAST_INTERPRO::Blast

=head2 Constructeur & Destructeur

=head2 new()

 Title        : new
 Usage        : new($arg)
 Function     : Construit une instance de la classe Blast
 Returns      : Une reference sur l instance
 Args         : $arg : reference sur un hash contenant les parametres de la ligne de commande

=cut

######################################################
########################################################
##               BLAST_INTERPRO::Blast                ##
########################################################
######################################################
package BLAST_INTERPRO::Blast;

BEGIN
{
    $BLAST_INTERPRO::Blast::VERSION = "v 2.0";
}

#----------------------------------------------------------------
#
#   new()
#
sub new
{
    my ($class) = shift;
    my $args = shift;
    my $self = {};

    $self->{_VERSION} = undef;    # Version du programme blast

    $self->{_REPORT}  = undef;   # Reference sur le rapport blast une fois parse

    #$self->{_DBNUM}        = undef;    # Nombre de sequence de la base
    #$self->{_DBLEN}        = undef;    # Nombre de lettres de la base
    #$self->{_QUERYDEF}     = undef;    # Def de la query
    #$self->{_QUERYLEN}     = undef;    # Nombre de lettres de la query
    $self->{_LISTHSP}      = undef;    # Reference sur un tableau d'objets BLAST::HSP
    $self->{_LISTHSP_SIZE} = 0;        # Nombre de HSP dans la liste ci-dessus

    $self->{_PARAMFORM} = $args;       # Reference sur %Hash contenant les parametres du formulaire CGI

    $self->{_BLASTREPORT} = undef;     # Pour le nom du fichier resultat de blast

    $self->{_BLASTPPARAM} = "-e 1e-2 -b 30000 -v 30000 -m 7";    # Parametre pour blastp
    $self->{_BLASTXPARAM} = "-e 1e-2 -b 30000 -v 30000 -m 7";    # Parametre pour blastx

    $self->{_NICE} = 0;    # Lance l'execution du blast avec (nice = 1) ou pas (nice = 0) la commande "nice"

    bless($self, $class);

    return $self;
}

#----------------------------------------------------------------

=head2 DESTROY()

 Title        : DESTROY
 Usage        : automatique
 Function     : Efface les fichier xml du blast

=cut

#----------------------------------------------------------------
#
#   DESTROY
#
sub DESTROY
{
    my $this = shift;
    unlink ( $this->blast_report() );
}

#----------------------------------------------------------------

=head2 Acces aux variables

=head2 function get_blast_param()

 Title        : get_blast_param
 Usage        : get_blast_param($arg)
 Returns      : Les parametres de la ligne de commande du blast ( p ou x selon $args ), sinon undef
 Args         : $arg : blastp ou blastx

=cut

#----------------------------------------------------------------#
#                                                                #
#                FONCTIONS D'ACCES AU VARIABLES                  #
#                                                                #
#----------------------------------------------------------------#

#----------------------------------------------------------------
#
#   get_blast_param()
#
sub get_blast_param
{
    my $this    = shift;
    my $program = shift;

    if    ($program =~ /blastp/) {return $this->{_BLASTPPARAM};}
    elsif ($program =~ /blastx/) {return $this->{_BLASTXPARAM};}
    else {return undef;}
}

#----------------------------------------------------------------

=head2 function get_param()

 Title        : get_param
 Usage        : get_param($arg)
 Function     : Accept aux valeurs des parametres du formulaire html
 Returns      : La valeur du parametre $arg ou undef si $arg n\'est pas un parametre
 Args         : $arg : nom de variable du formulaire html
              : 'blast_path' 'program' 'z_value' 'filter' 'seqfile' 'outfile' 'db' 'header'

=cut
#----------------------------------------------------------------
#
#   get_param()
#
sub get_param {

    my $this = shift;
    my $arg = shift;
    if ( defined($arg) ) {
	return $this->{_PARAMFORM}->{$arg}
    }
    return undef;
}
#----------------------------------------------------------------

=head2 function version()

 Title      : version
 Usage      : version()
 Returns    : La version du programme blast utilise

=cut

#----------------------------------------------------------------
#
#   version()
#
sub version
{
    my $this = shift;

    return $this->{_REPORT}->get_version;
}

#----------------------------------------------------------------

=head2 function blast_report()

 Title          : blast_report
 Usage          : blast_report   or   blast_report($arg)
 Function       : si "blast_report($arg)", initialise la variable avec $arg avant de la retourner
 Args & Returns : Le nom du fichier de sortie du blast

=cut

#----------------------------------------------------------------
#
#   blast_report()
#
sub blast_report
{
    my $this = shift;
    my $arg  = shift;
    if ( defined($arg) )
    {
	$this->{_BLASTREPORT} = $arg;
    }
    return $this->{_BLASTREPORT};
}

#----------------------------------------------------------------

=head2 function db_num()

 Title    : db_num
 Usage    : db_num
 Returns  : Le nombre de sequence de la base

=cut

#----------------------------------------------------------------
#
#   db_num()
#
sub db_num
{
    my $this = shift;

    return $this->{_REPORT}->get_db_num();
}

#----------------------------------------------------------------

=head2 function db_letters()

 Title    : db_letters
 Usage    : db_letters()
 Returns  : Le nombre de lettres de la base

=cut

#----------------------------------------------------------------
#
#   db_letters()
#
sub db_letters
{
    my $this = shift;

    return $this->{_REPORT}->get_db_len();
}

#----------------------------------------------------------------

=head2 function query_def()

 Title     : query_def
 Usage     : query_def()
 Returns   : La definition de la query

=cut

#----------------------------------------------------------------
#
#   query_def()
#
sub query_def
{
    my $this = shift;

    return $this->{_REPORT}->get_query_def();
}

#----------------------------------------------------------------

=head2 function query_length()

 Title    : query_length
 Usage    : query_length()
 Returns  : La longueur de la query

=cut

#----------------------------------------------------------------
#
#   query_length()
#
sub query_length
{
    my $this = shift;

    return $this->{_REPORT}->get_query_length();
}

#----------------------------------------------------------------

=head2 function list_hsp()

 Title          : list_hsp
 Usage          : list_hsp   or   list_hsp($arg)
 Prerequisite   : $arg est une reference sur un tableau
 Function       : si "list_hsp($arg)", initialise la variable avec $arg avant de la retourner
 Args & Returns : Une reference sur le tableau des HSP

=cut

#----------------------------------------------------------------
#
#   list_hsp()
#
sub list_hsp
{
    my $this = shift;
    my $arg  = shift;
    if ( defined($arg) )
    {
        if (ref($arg) eq "ARRAY") {$this->{_LISTHSP} = $arg;}
    }
    return $this->{_LISTHSP};
}

#----------------------------------------------------------------

=head2 function list_hsp_size()

 Title          : list_hsp_size
 Usage          : list_hsp_size   or   list_hsp_size($arg)
 Prerequisite   : $arg est une reference sur un tableau
 Function       : si "list_hsp_size($arg)", initialise la variable avec $arg avant de la retourner
 Args & Returns : Le nombre de HSP dans la liste des HSP

=cut

#----------------------------------------------------------------
#
#   list_hsp_size()
#
sub list_hsp_size
{
    my $this = shift;
    my $arg  = shift;
    if ( defined($arg) )
    {
	$this->{_LISTHSP_SIZE} = $arg;
    }
    return $this->{_LISTHSP_SIZE};
}

#----------------------------------------------------------------

=head2 Other public functions

=head2 execute_blast()

 Title        : execute_blast
 Usage        : execute_blast()
 Function     : Excute le Blast,analyse les resultats et selectionne les HSP

=cut

#----------------------------------------------------------------#
#                                                                #
#                      FONCTIONS PUBLIC                          #
#                                                                #
#----------------------------------------------------------------#

#----------------------------------------------------------------
#
#   execute_blast()
#
sub execute_blast
{
    my $this = shift;

    $this->LOG("execute_blast") if($this->get_param('verbose'));

    # fonction privee de Blast::
    $this->_blast();

    # fonction privee de Blast::
    $this->_parse_blast_report;

    # fonction privee de Blast::
    $this->_select_hsp();
}

#----------------------------------------------------------------

=head2 print_result()

 Title        : print_result
 Usage        : print_result()
 Function     : Affiche sur STDOUT ou ecrit dans outfile le resultats du blast

=cut

#----------------------------------------------------------------
#
#   print_result()
#
sub print_result
{
    my $this = shift;

    $this->LOG("print_result") if($this->get_param('verbose'));

    my ($ra_list_hsp, $ra_list_hsp_score);

    # Recuperation de la liste des hsp
    $ra_list_hsp = $this->list_hsp();

    # Selection des hsp selon les parametres de la ligne de commandes + trie par ordre decroissant des scores
    if ($this->get_param('filter'))
    {
        $ra_list_hsp = $this->_RemoveOverlaps($ra_list_hsp);
    }
    @$ra_list_hsp_score = sort sort_best_score @$ra_list_hsp;

    # Redirection du flux de sortie
    if (defined($this->get_param('outfile')))
    {
        my $file = $this->get_param('outfile');
        open(OUT, ">>$file");
    }
    else {*OUT = *STDOUT;}

    # Ecriture du header selon le parametre de la ligne de commande
    if ($this->get_param('header'))
    {
        print OUT $this->_print_header();
    }

    # Ecriture des hsp selectionnes
    foreach my $o_hsp (@$ra_list_hsp_score)
    {
        my $query_def     = $this->query_def();
	$query_def =~ s/^\s*//g;
	$query_def =~ s/\s*$//g;
	$query_def =~ s/\s/_/g;
        my @hit_def       = split (/\|/, $o_hsp->get_hit_def());
        my @hit_accession = split (/\#/, $o_hsp->get_hit_accession());
        my $score         = sprintf("%-6d", $o_hsp->get_hsp_score());
        my $begin_s       = sprintf("%6d", $hit_accession[2]);
        my $end_s         = sprintf("%6d", $hit_accession[3]);
        my $begin_q       = sprintf("%6d", $o_hsp->get_query_from());
        my $end_q         = sprintf("%6d", $o_hsp->get_query_to());
        my $expect        = sprintf("%-6.1g", $o_hsp->get_evalue());
        my $length        = sprintf("%d", $o_hsp->get_hit_length());
        my $frame         = undef;

        if ($this->get_param('program') =~ /blastx/)
        {
            $frame = sprintf("%2s", $o_hsp->get_query_frame());
            if ($frame !~ /^-/) {$frame =~ s/^\s*/+/;}
            print OUT "$query_def $begin_q $end_q // $hit_def[2] $begin_s $end_s // S=$score E=$expect F=$frame // $hit_def[3]\t Length = $length\n";
        }
        else
        {
            print OUT "$query_def $begin_q $end_q // $hit_def[2] $begin_s $end_s // S=$score E=$expect // $hit_def[3]\t Length = $length\n";
        }
      }

    print OUT "//\n" if(scalar(@{$ra_list_hsp_score}));
    close(OUT) if(defined($this->get_param('outfile')));
}

#----------------------------------------------------------------

=head2 function nice()

 Title        : nice
 Usage        : nice($arg)
 Function     : Si $arg = 1, le blast sera execute avec "nice"
 Returns      : La valeur de la variable privee _NICE
 Args         : $arg : 1 si execution du blast avec "nice"
              :        0 si non

=cut

#----------------------------------------------------------------
#
#   nice()
#
sub nice
{
    my $this = shift;
    my $arg = shift;

    if (defined($arg))
    {
	$this->{_NICE} = $arg;
    }
    return $this->{_NICE};
}

#----------------------------------------------------------------

=head2 Prive

=head2 _blast()

 Title        : _blast
 Usage        : _blast()
 Function     : Constitue la ligne de commande blast et lance son excecution

=cut

#----------------------------------------------------------------#
#                                                                #
#                      FONCTIONS PRIVEES                         #
#                                                                #
#----------------------------------------------------------------#

#---Procedure Privee---------------------------------------------
#
#   _blast()
#
sub _blast
{
    my $this = shift;

    $this->LOG("_blast") if($this->get_param('verbose'));

    my ($blast_path, $blast_prgm, $blast_db, $blast_seqfile, $blast_seqfile_reformat, $blast_param, $cde);

    if ($this->get_param('program') =~ /blastp/) {$blast_prgm = "blastp";}
    else {$blast_prgm = "blastx";}

    $blast_path    = $this->get_param('blast_path');
    $blast_param   = $this->get_blast_param($blast_prgm) . " -z " . $this->get_param('z_value');
    $blast_db      = $this->get_param('db');
    $blast_seqfile = $this->get_param('seqfile');
    $blast_seqfile_reformat = $blast_seqfile . "_reformat";


    # Verification du fichier query
    $this->_CheckSequenceFile();

    #--

    # Prepation du fichier de sortie blast

    ## rpetry - 07/08/2006 - Replaced the name of the tmpfile, because when
    ## lots of PRODOM jobs are run on the same cluster node at the same time, the script
    ## appears to get confused and output results from one jobs input sequences to the
    ## other jobs' output. doh!!!

    ## rpetry - 07/08/2006 - see above my $date         = time;
    ## rpetry - 07/08/2006 - see above my $tmp_namefile = "BLAST." . $this->get_param('program') . ".$date\.$$";
    my $tmp_namefile = $this->get_param('seqfile') . "." . $this->get_param('program'); ## rpetry - 07/08/2006  - see above

    $this->blast_report($tmp_namefile);

    #--

    # Preparation de la ligne de commende blast
#    if ($blast_prgm =~ /blastp/)
#    {
#        $cde = "$blast_path/$blast_prgm $blast_param -d $blast_db -i $blast_seqfile";
#    }
#    elsif ($blast_prgm =~ /blastx/)
#    {
        $cde = "$blast_path/blastall -p $blast_prgm $blast_param -d $blast_db -i $blast_seqfile";
#    }
#    else
#    {
#        exit(2);
#    }
    $cde = "$cde > " . $this->blast_report();

    if ($this->nice() == 1)
    {
        $cde = "nice $cde";
    }

    #--
    system("$cde");

}

#---Procedure Privee---------------------------------------------

=head2 _CheckSequenceFile()

 Title        : _CheckSequenceFile
 Usage        : _CheckSequenceFile()
 Function     : Check the sequence file existence and format

=cut

#---Procedure Privee---------------------------------------------
#
#   _CheckSequenceFile()
#
sub _CheckSequenceFile
{
    my $this = shift;

    $this->LOG("_CheckSequenceFile") if($this->get_param('verbose'));

    my $seqfile = $this->get_param('seqfile');
    my ($line) = ("");
    # Init sequence variables:

    if (-e "$seqfile")
    {
        open(SEQ, "$seqfile" || die "Can't open file $seqfile \n");
    }
    else
    {
        print STDERR "Can't find sequence file $seqfile\n";
        exit(2);
    }

    open(SEQ, "$seqfile" || die "Can't open file $seqfile \n");
    while ((defined($line = <SEQ>)) && ($line !~ /^>/)) { }

    if ((!defined($line)) || ($line !~ /^>/))
    {
        print STDERR "Sequence file format error\n";
        exit(2);
    }
    close(SEQ);
}    # end CheckSequenceFile

#----------------------------------------------------------------

=head2 _parse_blast_report()

 Title        : _parse_blast_report
 Usage        : _parse_blast_report()
 Prerequisite : Un fichier result blast au format xml et son chemin d acces dans this->blast_report
 Function     : Parse le fichier xml de blast et initialise : this->list_hsp, this->list_hsp_size,
                this->db_num, this->db_letters, this->query_def et this->query_length

=cut

#---Procedure Privee---------------------------------------------
#
#   _parse_blast_report
#
sub _parse_blast_report
{
    my $this = shift;

    $this->LOG("_parse_blast_report") if($this->get_param('verbose'));

    my $report = XML_BLAST::Report->new();
    $report->parse($this->blast_report());

    $this->{_REPORT} = $report;

    $this->list_hsp($report->get_list_hsp());

}

#----------------------------------------------------------------

=head2 _select_hsp()

 Title        : _select_hsp
 Usage        : _select_hsp()
 Function     : A partir du %hash retourne par arrange_dom, retourne un tableau contenant les meilleurs hsp
                est sans redondance ou chevauchement, pour chaque domaine

=cut

#---Fonction Privee----------------------------------------------
#
#   _select_hsp
#
sub _select_hsp
{
    my $this = shift;

    $this->LOG("_select_hsp") if($this->get_param('verbose'));

    my @a_list_hsp_ok;
    my $ra_list_hsp_tmp;
    my $rh_PD_hsp = $this->_arrange_dom();

    foreach my $ID_fam (keys %$rh_PD_hsp)
    {
        $ra_list_hsp_tmp = $this->_search_overlap($$rh_PD_hsp{$ID_fam});
        push (@a_list_hsp_ok, @$ra_list_hsp_tmp);
    }

    # Actualise la liste des hsp de l'objet
    $this->list_hsp(\@a_list_hsp_ok);
    $this->list_hsp_size(($#a_list_hsp_ok + 1));
}

#----------------------------------------------------------------

=head2 _search_overlap()

 Title        : _search_overlap
 Usage        : _search_overlap($args)
 Function     : Teste si un hsp est chevauchant avec des hsp deja retenus pour un domaine donne
 Returns      : Une reference sur un @array contenant le hsp a retenir pour un domaine donne
 Args         : reference sue un @array contenant tous les hsp d un domaine donne

=cut

#---Fonction Privee----------------------------------------------
#
#   _search_overlap
#
sub _search_overlap
{
    my $this = shift;

    $this->LOG("_search_overlap") if($this->get_param('verbose'));

    my $ra_list_hsp_tmp = shift;
    my @a_list_hsp_ok;

    # Tri des hsp par score decroissant
    @$ra_list_hsp_tmp = (sort sort_best_score @$ra_list_hsp_tmp);

    # Pour tous les hsp du tableau
    foreach my $o_hsp (@$ra_list_hsp_tmp)
    {

        # Recuperation des positions de debut et de fin du hsp sur la query
        # (pour tester les chevauchements)
        my $start_hsp = $o_hsp->get_query_from();
        my $end_hsp   = $o_hsp->get_query_to();

        my $overlap = 0;

        # Pour tous les hsp deja retenus
        foreach my $o_hsp_ok (@a_list_hsp_ok)
        {

            # Recuperation des positions de debut et de fin du hsp sur la query
            # (pour tester les chevauchements)
            my $start_hsp_ok = $o_hsp_ok->get_query_from();
            my $end_hsp_ok   = $o_hsp_ok->get_query_to();

            # Test de chevauchement contre le hsp deja selectionne (celui avec le meilleur score)
            if (   ($start_hsp <= $end_hsp_ok && $start_hsp >= $start_hsp_ok)
                || ($end_hsp <= $end_hsp_ok     && $end_hsp >= $start_hsp_ok)
                || ($start_hsp <= $start_hsp_ok && $end_hsp >= $end_hsp_ok))
            {
                $overlap = 1;
            }
        }

        # S'il n'y a pas de chevauchement, c'est que le hsp correspond a un autre domaine.
        # Il est alors ajoute a la liste des hsp a garder si le score est sup a score_min
        if ((!$overlap)) {push (@a_list_hsp_ok, $o_hsp);}
    }

    return \@a_list_hsp_ok;
}

#----------------------------------------------------------------

=head2 _arrange_dom()

 Title        : _arrange_dom
 Usage        : _arrange_dom()
 Function     : Regroupe dans un meme tableau, tous les hits du meme domaine. Les references a ces tableaux sont
                placees dans un %hash avec l\'ID prodom de la famille comme cle.
 Returns      : Une reference a un %hash

=cut

#---Fonction Privee----------------------------------------------
#
#   _arrange_dom
#
sub _arrange_dom
{
    my $this = shift;

    $this->LOG("_arrange_dom") if($this->get_param('verbose'));

    my $ra_list_hsp = $this->list_hsp();
    my %h_pd_hsp;

    foreach my $o_hsp (@$ra_list_hsp)
    {

        # Recup le PD de la famille
	my @AC = split ('#', $o_hsp->get_hit_accession());
        my $frame = $o_hsp->get_query_frame();
        if ($frame !~ /^-/) {$frame = "+" . $frame;}

        my $id = $AC[1] . $frame;

        # Si nouvelle famille, creation d'un tableau dans le hash
        if (!$h_pd_hsp{$id}) {my @array; $h_pd_hsp{$id} = \@array;}

        # Ajout de l'objet BLAST::HSP au tableau
        push (@{$h_pd_hsp{$id}}, $o_hsp);
    }

    return \%h_pd_hsp;
}

#----------------------------------------------------------------

=head2 _RemoveOverlaps()

 Title        : _RemoveOverlaps
 Usage        : _RemoveOverlaps()
 Function     : Supprime les chevauchements de domaines sur la query en gardant le meilleur des hits

=cut

#---Fonction Privee----------------------------------------------
#
#   _RemoveOverlaps
#
#  Query : |-------------------------------------------------|
#  Hit 1 :      |---450---------|
#  Hit 2 :                 |--------------300---------|
#
#  Result:      |---450---------||--------300---------|
sub _RemoveOverlaps
{
    my $this = shift;

    $this->LOG("_RemoveOverlaps") if($this->get_param('verbose'));

    my $ra_hsp_list = shift;

    my (@matches)         = ();
    my (@Tab_NON_OVERLAP) = ();

    # Fill in matches array with HSP:
    foreach my $o_hsp (sort sort_bad_score @$ra_hsp_list)
    {
        my ($begin, $end) = ($o_hsp->get_query_from(), $o_hsp->get_query_to());
        foreach my $i ($begin .. $end)
        {
            $matches[$i] = $o_hsp;
        }
    }

    # Read matches array to determine non overlapping HSP:
    my $old_HSP = undef;
    my $begin   = 0;
    my $max_i   = $#matches + 1;

    foreach my $i (1 .. $max_i)
    {
        my $current_HSP = $matches[$i];

        if (defined($current_HSP))
        {
            if (defined($old_HSP))
            {
                if ($current_HSP->get_hit_accession() ne $old_HSP->get_hit_accession() )
                {
		    my $hsp_OK = XML_BLAST::HSP->new($old_HSP);
		    $hsp_OK->{'_QUERYFROM'} = $begin;
		    $hsp_OK->{'_QUERYTO'}   = $i - 1;
		    push (@Tab_NON_OVERLAP, $hsp_OK);
                    $begin = $i;
                }
            }
            else
            {
                $begin = $i;
            }
        }
        else
        {
            if (defined($old_HSP))
            {
		$old_HSP->{'_QUERYFROM'} = $begin;
		$old_HSP->{'_QUERYTO'}   = $i - 1;
		push (@Tab_NON_OVERLAP, $old_HSP);
            }
        }
        $old_HSP = $current_HSP;
    }

    return \@Tab_NON_OVERLAP;
}

#----------------------------------------------------------------

=head2 _print_header()

 Title        : _print_header
 Usage        : _print_header()
 Function     : Concatene dans un scalaire le header du blast, devant etre affiche
 Returns      : Un scalaire contenant le header

=cut

#---Fonction Privee----------------------------------------------
#
#   _print_header()
#
sub _print_header()
{
    my $this = shift;

    my ($blast_version, $blast_param, $blast_db, $blast_seqfile, $query_name, $query_length, $blast_prgm, $string);

#    if ($this->get_param('program') =~ /blastp/) {$blast_prgm = "blastpgp";}
#    else {$blast_prgm = "blastx";}

    $blast_prgm    = $this->get_param('program');
    $blast_version = $this->version();
    $blast_param   = $this->get_blast_param($blast_prgm) . " -z " . $this->get_param('z_value');
    $blast_db      = $this->get_param('db');
    $blast_seqfile = $this->get_param('seqfile');
    $blast_seqfile =~ s/.tmp//g;
    $query_name    = $this->query_def();
    $query_name    =~ s/^\s*//g;
    $query_name    =~ s/\s*$//g;
    $query_name    =~ s/\s/_/g;
    $query_length  = $this->query_length();

    $string .= "PROGRAM \t: $blast_version\n";
    $string .= "PARAMETERS \t: $blast_param\n";
    $string .= "DATABASE \t: $blast_db\n";
    $string .= "QUERYFILE \t: $blast_seqfile\n";
    $string .= "QUERYNAME \t: $query_name\n";
    $string .= "QUERYLENGTH \t: $query_length letters\n\n";

    return $string;
}

#----------------------------------------------------------------

=head2 LOG()

 Title        : LOG
 Usage        : LOG($args)
 Function     : Ouvre en ajout un fichier "LOG" dans le repertoire de session et inscrire
              : le message en argument et la date
 Args         : Une chaine de caracteres (nom de la fonction en execution par exemple)...

=cut

#----------------------------------------------------------------
#
#   LOG
#
sub LOG
{
    my $this    = shift;
    my $message = shift;
    my $Time    = `date`;

    open(LOG, ">>LOG");
    print LOG "$message\n";
    print LOG "\t$Time\n\n";
    close(LOG);
}

#----------------------------------------------------------------
#
#   Fonction outils pour le fonctionnement
#   de certaines fonctions membres
#

#   sort_best_score
sub sort_best_score {$b->get_hsp_score() <=> $a->get_hsp_score}

#   sort_bad_score
sub sort_bad_score {$a->get_hsp_score() <=> $b->get_hsp_score}

#----------------------------------------------------------------
1;
