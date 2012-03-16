package uk.ac.ebi.interpro.scan.io;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Given the file extension as a string lookup the appropriate FileOutputFormat
     * @param outputFormat File extension
     * @return File output format
     */
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

    /**
     * Given a comma separated string of file extensions produce a set of FileOutputFormats
     * @param outputFormats File extensions
     * @return File output formats
     */
    public static Set<FileOutputFormat> stringToFileOutputFormats(String outputFormats) {
        Set<FileOutputFormat> fileOutputFormats = new HashSet<FileOutputFormat>();
        String[] formats = outputFormats.split(",");
        for (String format : formats){
            if (format.equalsIgnoreCase(XML.getFileExtension())) {
                fileOutputFormats.add(XML);
            } else if (format.equalsIgnoreCase(GFF3.getFileExtension()) || format.equalsIgnoreCase("gff")) {
                fileOutputFormats.add(GFF3);
            } else if (format.equalsIgnoreCase(HTML.getFileExtension())) {
                fileOutputFormats.add(HTML);
            } else {
                fileOutputFormats.add(TSV);
            }
        }
        return fileOutputFormats;
    }

}
