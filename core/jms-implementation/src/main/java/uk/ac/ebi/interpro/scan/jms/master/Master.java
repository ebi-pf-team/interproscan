package uk.ac.ebi.interpro.scan.jms.master;

/**
 * Interface for the Master application.
 *
 * @author Phil Jones
 * @version $Id: Master.java,v 1.2 2009/10/16 12:05:10 pjones Exp $
 * @since 1.0
 */

public interface Master extends Runnable {

    /**
     * If a fasta file path is set, load the proteins at start up and analyse them.
     *
     * @param fastaFilePath from which to load the proteins at start up and analyse them.
     */
    void setFastaFilePath(String fastaFilePath);

    /**
     * @param outputFile if set, then the results will be output to this file in the format specified in
     *                   the field outputFormat (defaulting to XML).
     */
    void setOutputFile(String outputFile);

    /**
     * Allows the output format to be changed from the default XML.  If no value is specified for outputFile, this
     * value will be ignored.
     *
     * @param outputFormat the output format.  If no value is specified for outputFile, this format
     *                     value will be ignored.
     */
    void setOutputFormat(String outputFormat);

    /**
     * Optionally, set the analyses that should be run.
     * If not set, or set to null, all analyses will be run.
     *
     * @param analyses a comma separated list of analyses (job names) that should be run. Null for all jobs.
     */
    void setAnalyses(String[] analyses);

    void setMapToInterProEntries(boolean mapToInterPro);

    void setMapToGOAnnotations(boolean mapToGO);

    /**
     * Sets pathway option.
     *
     * @param isMapToPathway Indicates if pathway option is activate or not.
     *                       If so entry to pathway mappings will be print ou to result as well.
     */
    void setPathway(boolean isMapToPathway);

    //TODO: Unfinished integration. Setter is unused at the moment
    public void setCleanDatabase(boolean clean);

    /**
     * Parameter passed in on command line to set kind of input sequence
     * p: Protein
     * n: nucleic acid (DNA or RNA)
     *
     * @param sequenceType the kind of input sequence
     */
    void setSequenceType(String sequenceType);
}
