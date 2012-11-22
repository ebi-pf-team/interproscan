package uk.ac.ebi.interpro.scan.io.getorf;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.NucleotideSequenceStrand;
import uk.ac.ebi.interpro.scan.model.OpenReadingFrame;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description line parser for GetOrf formatted FASTA files.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class GetOrfDescriptionLineParser {

    private static final Logger LOGGER = Logger.getLogger(GetOrfDescriptionLineParser.class.getName());

    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("^\\[(\\d+)\\s+\\-\\s+(\\d+)]\\s*(\\(REVERSE SENSE\\))?\\s*(.*)$");

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^(.+)\\_(.+)");

    private static final Pattern WHITE_SPACE_PATTERN = Pattern.compile("\\s+");

    private static final Pattern UNDERSCORE_PATTERN = Pattern.compile("\\_");

//    private static final Pattern ACCESSION_EXTRACTOR_PATTERN = Pattern.compile("^\\#=GF\\s+[A-Z]{2}\\s+([A-Z0-9]+).*$");


    public OpenReadingFrame createORFFromParsingResult(String description) {
        final Matcher matcher = DESCRIPTION_PATTERN.matcher(description);
        System.out.println("DESCRIPTION: '" + description + "'");
        if (matcher.find()) {
            System.out.println("Matched!");
            final int start = Integer.parseInt(matcher.group(1));
            final int end = Integer.parseInt(matcher.group(2));
            if (matcher.group(3) == null) {
                return new OpenReadingFrame(start, end, NucleotideSequenceStrand.SENSE);
            } else {
                return new OpenReadingFrame(end, start, NucleotideSequenceStrand.ANTISENSE);
            }
        } else {
            LOGGER.error("Description line in getorf output (fasta) file not as expected:" + description);
            throw new IllegalStateException("Description line in getorf output (fasta) file not as expected. See fine logging for details.");
        }
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
    public static String getIdentifier(String input) {
        Matcher matcher = IDENTIFIER_PATTERN.matcher(input);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            LOGGER.error("The ORF identifier in the getorf output file not as expected:" + input);
            throw new IllegalStateException("The ORF identifier in the getorf output file not as expected:" + input);
        }

//        if (chunk != null && chunk.length() > 0) {
////            chunk = chunk.replaceAll(VERSION_PATTERN.pattern(), "");
//            int indexOfUnderline = chunk.indexOf("_");
//            if (indexOfUnderline > 0) {
//                String searchPattern = "SENSE)";
//                String result = chunk.substring(0, indexOfUnderline);
//                int nextIndex = chunk.indexOf(searchPattern);
//                if (nextIndex < 0) {
//                    searchPattern = "] ";
//                    //Minus 1 because of the white space at the end of the pattern
//                    nextIndex = chunk.indexOf(searchPattern) - 1;
//                }
//                if (nextIndex > -1) {
//                    nextIndex = nextIndex + searchPattern.length();
//                    return (result + chunk.substring(nextIndex)).trim();
//                } else {
//                    return result.trim();
//                }
//            } else {
//                return chunk.trim();
//            }
//        }
//        return null;
    }
}
