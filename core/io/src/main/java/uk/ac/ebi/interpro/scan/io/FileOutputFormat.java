package uk.ac.ebi.interpro.scan.io;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple enum to describe file output formats (Introduced for more code consistency).
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public enum FileOutputFormat {
    TSV("tsv"), XML("xml"), GFF3("gff3"), HTML("html"), SVG("svg"), RAW("raw"),
    // GFF3 partial format is an internal format used for the MG pipeline only
    // It allows gff3 files to be concatenated together (impossible to do with normal gff3 format)
    // This output format is NOT to be mentioned in the documentation!
    GFF3_PARTIAL("gff3-partial"),
    // XML_SLIM is an internal format, the same as XML output but proteins with no matches are not included at all in
    // the file
    XML_SLIM("xml-slim"),
    //TSV_PRO is the format for production purposes in InterPRo
    TSV_PRO("tsv-pro");

    private static final Logger LOGGER = Logger.getLogger(FileOutputFormat.class.getName());

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
     * Is the provided file extension string present in the file output format enum?
     *
     * @param extension The file extension string to check
     * @return True if present, otherwise false
     */
    public static boolean isExtensionValid(String extension) {
        for (FileOutputFormat format : FileOutputFormat.values()) {
            if (extension.equalsIgnoreCase(format.getFileExtension())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Given the file extension as a string lookup the appropriate FileOutputFormat
     *
     * @param outputFormat File extension
     * @return File output format
     */
    public static FileOutputFormat stringToFileOutputFormat(String outputFormat) {
        if (outputFormat.equalsIgnoreCase(XML.getFileExtension())) {
            return XML;
        } else if (outputFormat.equalsIgnoreCase(XML_SLIM.getFileExtension())) {
            return XML_SLIM;
        } else if (outputFormat.equalsIgnoreCase(GFF3.getFileExtension()) || outputFormat.equalsIgnoreCase("gff")) {
            return GFF3;
        } else if (outputFormat.equalsIgnoreCase(GFF3_PARTIAL.getFileExtension())) {
            return GFF3_PARTIAL;
        } else if (outputFormat.equalsIgnoreCase(HTML.getFileExtension())) {
            return HTML;
        } else if (outputFormat.equalsIgnoreCase(SVG.getFileExtension())) {
            return SVG;
        } else if (outputFormat.equalsIgnoreCase(RAW.getFileExtension())) {
            return RAW;
        } else if (outputFormat.equalsIgnoreCase(TSV_PRO.getFileExtension())) {
            return TSV_PRO;
        } else {
            return TSV;
        }
    }

    /**
     * Given a comma separated string of file extensions produce a set of FileOutputFormats
     *
     * @param outputFormats File extensions
     * @return File output formats
     */
    public static Set<FileOutputFormat> stringToFileOutputFormats(String outputFormats) {
        Set<FileOutputFormat> fileOutputFormats = new HashSet<FileOutputFormat>();
        String[] formats = outputFormats.split(",\\s*");
        for (String format : formats) {
            if (format.equalsIgnoreCase(XML.getFileExtension())) {
                fileOutputFormats.add(XML);
            }  else if (format.equalsIgnoreCase(XML_SLIM.getFileExtension())) {
                fileOutputFormats.add(XML_SLIM);
            } else if (format.equalsIgnoreCase(GFF3.getFileExtension()) || format.equalsIgnoreCase("gff")) {
                fileOutputFormats.add(GFF3);
            }  else if (format.equalsIgnoreCase(GFF3_PARTIAL.getFileExtension())) {
                fileOutputFormats.add(GFF3_PARTIAL);
            }  else if (format.equalsIgnoreCase(HTML.getFileExtension())) {
                fileOutputFormats.add(HTML);
            } else if (format.equalsIgnoreCase(SVG.getFileExtension())) {
                fileOutputFormats.add(SVG);
            } else if (format.equalsIgnoreCase(TSV.getFileExtension())) {
                fileOutputFormats.add(TSV);
            } else if (format.equalsIgnoreCase(TSV_PRO.getFileExtension())) {
                    fileOutputFormats.add(TSV_PRO);
            } else if (format.equalsIgnoreCase(RAW.getFileExtension())) {
                fileOutputFormats.add(RAW);
            } else {
                LOGGER.warn("File format " + format + " was not a recognised option so will be ignored");
            }
        }
        return fileOutputFormats;
    }

}
