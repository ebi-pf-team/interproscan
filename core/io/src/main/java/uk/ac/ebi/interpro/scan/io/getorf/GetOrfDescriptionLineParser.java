package uk.ac.ebi.interpro.scan.io.getorf;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.NucleotideSequenceStrand;
import uk.ac.ebi.interpro.scan.model.OpenReadingFrame;

/**
 * Description line parser for GetOrf formatted FASTA files.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class GetOrfDescriptionLineParser {

    private static final Logger LOGGER = Logger.getLogger(GetOrfDescriptionLineParser.class.getName());

    private static final String WHITESPACE = " ";

//    private static final Pattern ACCESSION_EXTRACTOR_PATTERN = Pattern.compile("^\\#=GF\\s+[A-Z]{2}\\s+([A-Z0-9]+).*$");


    /**
     * Parses out start and end position as well as the strand from the description line.
     *
     * @param descLine Description line.
     */
    public String[] parseGetOrfDescriptionLine(String descLine) {
        if (descLine == null) {
            LOGGER.warn("The specified description line is NULL!");
            return null;
        }
        //TODO: Check description line format using PATTERN class
        //... e.g.
        //        Matcher acLineMatcher = ACCESSION_EXTRACTOR_PATTERN.matcher(line);
        //                    if (acLineMatcher.find()) {
        //                        record.setModelAc(acLineMatcher.group(1));
        //                    }
        //OR
//        if (!descLine.matches("")) {
//            LOGGER.warn("The specified description line: " + descLine + " doesn't match the given regular expression!");
//            return null;
//        }
        //Filter step: Removes characters, which makes your parsing life easier (hopefully).
        descLine = filterDescriptionLine(descLine);
        //Split by white space
        return descLine.trim().split(WHITESPACE);
    }

    public OpenReadingFrame createORFFromParsingResult(String[] chunks) {
        if (chunks.length > 3) {
            String startPosChunk = chunks[1];
            String endPosChunk = chunks[3];
            NucleotideSequenceStrand strand = NucleotideSequenceStrand.SENSE;
            if (chunks.length > 4) {
                if (chunks[4].equalsIgnoreCase("REVERSE")) {
                    strand = NucleotideSequenceStrand.ANTISENSE;
                    startPosChunk = endPosChunk;
                    endPosChunk = chunks[1];
                }
            }
            int start = Integer.parseInt(startPosChunk);
            int end = Integer.parseInt(endPosChunk);
            return new OpenReadingFrame(start, end, strand);
        }
        return null;
    }

    /**
     * Handles identifiers like<br>
     * test_1 [230 - 10]<br>
     * test_1 [230 - 10] codes for the lac repressor<br>
     * test_1 [230 - 10] (REVERSE SENSE)<br>
     * test_1 [230 - 10] (REVERSE SENSE) codes for the lac repressor
     * OR<br>
     * AACH01000026.1_8 [261 - 1] (REVERSE SENSE) Saccharomyces mikatae IFO 1815 YM4906-Contig2858, whole genome shotgun sequence.
     */
    public static String getIdentifier(String chunk) {
        if (chunk != null && chunk.length() > 0) {
//            chunk = chunk.replaceAll(VERSION_PATTERN.pattern(), "");
            int indexOfUnderline = chunk.indexOf("_");
            if (indexOfUnderline > 0) {
                String searchPattern = "SENSE)";
                String result = chunk.substring(0, indexOfUnderline);
                int nextIndex = chunk.indexOf(searchPattern);
                if (nextIndex < 0) {
                    searchPattern = "] ";
                    //Minus 1 because of the white space at the end of the pattern
                    nextIndex = chunk.indexOf(searchPattern) - 1;
                }
                if (nextIndex > -1) {
                    nextIndex = nextIndex + searchPattern.length();
                    return (result + chunk.substring(nextIndex)).trim();
                } else {
                    return result.trim();
                }
            } else {
                return chunk.trim();
            }
        }
        return null;
    }

    private String filterDescriptionLine(String descLine) {
        return descLine
                .replace("[", "")
                .replace(")", "")
                .replace("(", "")
                .replace("]", "");
    }
}