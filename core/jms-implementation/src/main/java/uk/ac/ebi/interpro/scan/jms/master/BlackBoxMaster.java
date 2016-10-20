package uk.ac.ebi.interpro.scan.jms.master;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 */
public interface BlackBoxMaster extends SimpleBlackBoxMaster {

    /**
     * Boolean switch which excludes sites from the output.
     *
     * @param excludeSites Default is false (not activated)
     */
    void setExcludeSites(boolean excludeSites);

    /**
     * Boolean switch which activates the InterPro lookup.
     *
     * @param mapToInterPro Default is FALSE (not activated)
     */
    void setMapToInterProEntries(boolean mapToInterPro);

    /**
     * Boolean switch which activates the GO annotation lookup.
     *
     * @param mapToGO Default is FALSE (not activated)
     */

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
     * Called to turn off the use of the precalculated match lookup service on this run.
     */
    void disablePrecalc();

    void processOutputFormats(final Map<String, String> params, final String[] outputFormats);

    void setUserDir(String userDir);

}