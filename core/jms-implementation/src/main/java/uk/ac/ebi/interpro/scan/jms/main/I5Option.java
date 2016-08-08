package uk.ac.ebi.interpro.scan.jms.main;

import java.util.EnumSet;

/**
 * Additional options:
 * -iprlookup    Switch on look up of corresponding InterPro annotation
 * <p/>
 * -goterms      Switch on look up of corresponding Gene Ontology
 * annotation (requires -iprlookup option to be used too)
 * <p/>
 * -pathways    Switch on look up of corresponding Pathway annotation  (requires -iprlookup option to be used too)
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public enum I5Option {
    MODE("mode", "m", false, "Optional, the mode in which InterProScan is being run, the default mode is " + Mode.STANDALONE.getRunnableBean() + ". Must be one of: " + Mode.getCommaSepModeList() + ".", "MODE-NAME", false, Mode.SET_OF_NO_MODES),
    INPUT("input", "i", false, "Optional, path to fasta file that should be loaded on Master startup. Alternatively, in CONVERT mode, the InterProScan 5 XML file to convert.", "INPUT-FILE-PATH", false, Mode.SET_OF_ALL_MODES),
    OUTPUT_FORMATS("formats", "f", false, "Optional, case-insensitive, comma separated list of output formats. Supported formats are TSV, XML, GFF3, HTML and SVG. Default for protein sequences are TSV, XML and GFF3, or for nucleotide sequences GFF3 and XML.", "OUTPUT-FORMATS", true, Mode.SET_OF_ALL_MODES),
    BASE_OUT_FILENAME("output-file-base", "b", false, "Optional, base output filename (relative or absolute path).  Note that this option, the --output-dir (-d) option and the --outfile (-o) option are mutually exclusive.  The appropriate file extension for the output format(s) will be appended automatically. By default the input file path/name will be used.", "OUTPUT-FILE-BASE", false, Mode.SET_OF_ALL_MODES),
    OUTPUT_FILE("outfile", "o", false, "Optional explicit output file name (relative or absolute path).  Note that this option, the --output-dir (-d) option and the --output-file-base (-b) option are mutually exclusive. If this option is given, you MUST specify a single output format using the -f option.  The output file name will not be modified. Note that specifying an output file name using this option OVERWRITES ANY EXISTING FILE.", "EXPLICIT_OUTPUT_FILENAME", false, Mode.SET_OF_ALL_MODES),
    OUTPUT_DIRECTORY("output-dir", "d", false, "Optional, output directory.  Note that this option, the --outfile (-o) option and the --output-file-base (-b) option are mutually exclusive. The output filename(s) are the same as the input filename, with the appropriate file extension(s) for the output format(s) appended automatically .", "OUTPUT-DIR", false, Mode.SET_OF_ALL_MODES),
    ANALYSES("applications", "appl", false, "Optional, comma separated list of analyses.  If this option is not set, ALL analyses will be run. ", "ANALYSES", true, Mode.SET_OF_STANDARD_MODES),
    PRIORITY("priority", "p", false, "Minimum message priority that the worker will accept (0 low -> 9 high).", "JMS-PRIORITY", false, Mode.SET_OF_NO_MODES),
    IPRLOOKUP("iprlookup", "iprlookup", false, "Also include lookup of corresponding InterPro annotation in the TSV and GFF3 output formats.", null, false, Mode.SET_OF_STANDARD_MODES),
    GOTERMS("goterms", "goterms", false, "Optional, switch on lookup of corresponding Gene Ontology annotation (IMPLIES -iprlookup option)", null, false, Mode.SET_OF_STANDARD_MODES),
    PATHWAY_LOOKUP("pathways", "pa", false, "Optional, switch on lookup of corresponding Pathway annotation (IMPLIES -iprlookup option)", null, false, Mode.SET_OF_STANDARD_MODES),
    NOSITES("exclude-sites", "x", false, "Optional, excludes sites from the XML output", null, false, Mode.SET_OF_STANDARD_MODES),
    MASTER_URI("masteruri", "masteruri", false, "The TCP URI of the Master.", "MASTER-URI", false, Mode.SET_OF_NO_MODES),
    MASTER_MAXLIFE("mastermaxlife", "mastermaxlife", false, "The maximum lifetime of the Master.", "MASTER-MAXLIFE", false, Mode.SET_OF_NO_MODES),
    SEQUENCE_TYPE("seqtype", "t", false, "Optional, the type of the input sequences (dna/rna (n) or protein (p)).  The default sequence type is protein.", "SEQUENCE-TYPE", false, Mode.SET_OF_STANDARD_MODES),
    MIN_SIZE("minsize", "ms", false, "Optional, minimum nucleotide size of ORF to report. Will only be considered if n is specified as a sequence type. " +
            "Please be aware of the fact that if you specify a too short value it might be that the analysis takes a very long time!", "MINIMUM-SIZE", false, Mode.SET_OF_STANDARD_MODES),
    TEMP_DIRECTORY_NAME("tempdirname", "td", false, "Optional, used to start up a worker with the correct temporary directory.", "TEMP-DIR-NAME", false, Mode.SET_OF_NO_MODES),
    TEMP_DIRECTORY("tempdir", "T", false, "Optional, specify temporary file directory (relative or absolute path). The default location is temp/.", "TEMP-DIR", false, Mode.SET_OF_ALL_MODES),
    DISABLE_PRECALC("disable-precalc", "dp", false, "Optional.  Disables use of the precalculated match lookup service.  All match calculations will be run locally.", null, false, Mode.SET_OF_STANDARD_MODES),
    HIGH_MEM("highmem", "hm", false, "Optional, switch on the creation of a high memory worker. Please note normal and high mem workers share the same Spring configuration file.", null, false, Mode.SET_OF_NO_MODES),
    TIER1("tier1", "tier1", false, "Optional, switch to indicate the high memory worker is a child of the master.", "TIER", false, Mode.SET_OF_NO_MODES),
    CLUSTER_RUN_ID("clusterrunid", "crid", false, "Optional, switch to specify the Project name for this i5 run.", "CLUSTER-RUN-ID", false, Mode.SET_OF_NO_MODES),
    USER_DIR("userdir", "u", false, "The base directory for results (if absolute paths not specified)", "USER_DIRECTORY", false, Mode.SET_OF_NO_MODES);

    private String longOpt;

    private boolean multipleArgs;

    private String shortOpt;

    private boolean required;

    private String description;

    private String argumentName;

    private EnumSet<Mode> includeInUsageMessage;

    I5Option(
            String longOpt,
            String shortOpt,
            boolean required,
            String description,
            String argumentName,
            boolean multipleArgs,
            EnumSet<Mode> includeInUsageMessage
    ) {
        this.longOpt = longOpt;
        this.shortOpt = shortOpt;
        this.required = required;
        this.description = description;
        this.argumentName = argumentName;
        this.multipleArgs = multipleArgs;
        this.includeInUsageMessage = includeInUsageMessage;
    }

    public String getLongOpt() {
        return longOpt;
    }

    public String getShortOpt() {
        return shortOpt;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDescription() {
        return description;
    }

    public String getArgumentName() {
        return argumentName;
    }

    public boolean hasMultipleArgs() {
        return multipleArgs;
    }

    public EnumSet<Mode> getIncludeInUsageMessage() {
        return includeInUsageMessage;
    }

    public static boolean showOptInHelpMessage(final String shortOpt, final Mode mode) {
        for (I5Option option : I5Option.values()) {
            if (option.getShortOpt().equals(shortOpt)) {
                EnumSet<Mode> modes = option.getIncludeInUsageMessage();
                if (modes.contains(mode)) {
                    return true;
                }
            }
        }
        return false;
    }

}
