package uk.ac.ebi.interpro.scan.jms.master;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 */
public interface BlackBoxMaster extends Master {
    /**
     * If a fasta file path is set, load the proteins at start up and analyse them.
     *
     * @param fastaFilePath from which to load the proteins at start up and analyse them.
     */
    void setFastaFilePath(String fastaFilePath);

    /**
     * Allows the output formats to be changed from the default of all available formats for that sequence type.
     *
     * @param outputFormats The comma separated list of output formats.
     */
    void setOutputFormats(String[] outputFormats);

    void setMapToInterProEntries(boolean mapToInterPro);

    void setMapToGOAnnotations(boolean mapToGO);

    /**
     * Sets pathway option.
     *
     * @param mapToPathway Indicates if pathway option is activate or not.
     *                     If so entry to pathway mappings will be print ou to result as well.
     */
    void setMapToPathway(boolean mapToPathway);

    /**
     * Parameter passed in on command line to set kind of input sequence
     * p: Protein
     * n: nucleic acid (DNA or RNA)
     *
     * @param sequenceType the kind of input sequence
     */
    void setSequenceType(String sequenceType);

    /**
     * Parameter passed in on command line to set minimum nucleotide size of ORF to report (EMBOSS getorf parameter).
     * Default size for InterProScan is 50 nucleic acids (which overwrites the getorf default value of 30).<br>
     * This option is also configurable within the interproscan.properties file, but will be overwritten by the command value if specified.
     *
     * @param minSize Minimum nucleotide size of ORF to report (EMBOSS getorf parameter).
     */
    void setMinSize(String minSize);


    /**
     * Allows an explicit (i.e. not modifiable) output file name to be specified.  If this is set,
     * it is guaranteed that the user has specified a single output form (excluding HTML) and
     * is responsible for giving the file a sensible name on a writable path.
     *
     * @param explicitFileName to be set.
     */
    void setExplicitOutputFilename(String explicitFileName);

    /**
     * Called to turn off the use of the precalculated match lookup service on this run.
     */
    void disablePrecalc();

    /**
     * @param outputBaseFilename if set, then the results will be output to this file in the format specified in
     *                           the field outputFormat (defaulting to XML).
     */
    void setOutputBaseFilename(String outputBaseFilename);

    void processOutputFormats(final Map<String, String> params, final String[] outputFormats);
}
