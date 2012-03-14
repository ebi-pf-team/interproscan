package uk.ac.ebi.interpro.scan.io;

/**
 * Simple enum to describe file output formats (Introduced for more code consistency).
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public enum FileOutputFormat {
    TSV("tsv"), XML("xml"), GFF3("gff3"), HTML("html");

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
        if (outputFormat.equalsIgnoreCase(XML.getFileExtension())) {
            return XML;
        } else if (outputFormat.equalsIgnoreCase(GFF3.getFileExtension()) || outputFormat.equalsIgnoreCase("gff")) {
            return GFF3;
        } else if (outputFormat.equalsIgnoreCase(HTML.getFileExtension())) {
            return HTML;
        } else {
            return TSV;
        }
    }
}