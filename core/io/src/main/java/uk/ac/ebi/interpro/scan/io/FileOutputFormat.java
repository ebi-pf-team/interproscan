package uk.ac.ebi.interpro.scan.io;

/**
 * Simple enum to describe file output formats (Introduced for more code consistency).
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public enum FileOutputFormat {
    TSV("tsv"), XML("xml"), GFF3("gff3");

    private String fileExtension;

    FileOutputFormat(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     * Returns file extension in lower case.
     */
    public String getFileExtension() {
        return fileExtension;
    }

    public static FileOutputFormat stringToFileOutputFormat(String outputFormat) {
        if (outputFormat.equalsIgnoreCase("xml")) {
            return XML;
        } else if (outputFormat.equalsIgnoreCase("gff3") || outputFormat.equalsIgnoreCase("gff")) {
            return GFF3;
        } else {
            return TSV;
        }
    }
}