package uk.ac.ebi.interpro.scan.jms.master;

import java.util.Map;

/**
 * This interface is used to simplify the setup of standalone and convert mode.
 *
 * @author Maxim Scheremetjew
 */
public interface SimpleBlackBoxMaster extends Master {


    /**
     * Sets the input file path. Could be a FASTA formatted file (standalone mode) OR and XML file (convert mode).
     */
    void setFastaFilePath(String fastaFilePath);

    //User output settings

    /**
     * Sets the output base file name. The file name will be extended by the different output format file extensions (.gff3, .tsv and so on).
     */
    void setOutputBaseFilename(String outputBaseFilename);

    /**
     * Sets an explicit output file name. Only works in combination with 1 specific output format.
     */
    void setExplicitOutputFilename(String explicitFileName);

    /**
     * Allows the output formats to be changed from the default of all available formats for that sequence type.
     *
     * @param outputFormats The comma separated list of output formats.
     */
    void setOutputFormats(String[] outputFormats);
}
