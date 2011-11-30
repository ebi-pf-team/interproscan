package uk.ac.ebi.interpro.scan.io.getorf;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.NucleotideSequenceStrand;
import uk.ac.ebi.interpro.scan.model.OpenReadingFrame;

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

    private static final String WHITESPACE = " ";

    private static final Pattern ACCESSION_EXTRACTOR_PATTERN = Pattern.compile("^\\#=GF\\s+[A-Z]{2}\\s+([A-Z0-9]+).*$");

    /**
     * Parses out start and end position as well as the strand from the description line.
     *
     * @param descLine Description line.
     */
    public OpenReadingFrame parseGetOrfDescriptionLine(String descLine) {
        if (descLine == null) {
            LOGGER.warn("The specified description line is NULL!");
            return null;
        }
        //Check description line format using PATTERN class
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
        //Filter step
        descLine = filterDescriptionLine(descLine);
        //Split by white space
        String[] chunks = descLine.trim().split(WHITESPACE);
        //Get values by index
        String startPosChunk = chunks[1];
        String endPosChunk = chunks[3];
        NucleotideSequenceStrand strand = NucleotideSequenceStrand.SENSE;
        if (descLine.contains("REVERSE")) {
            strand = NucleotideSequenceStrand.ANTISENSE;
            startPosChunk = endPosChunk;
            endPosChunk = chunks[1];
        }
        int start = Integer.parseInt(startPosChunk);
        int end = Integer.parseInt(endPosChunk);

        return new OpenReadingFrame(start, end, strand);
    }

    private String filterDescriptionLine(String descLine) {
        descLine = descLine.replace("[", "");
        return descLine.replace("]", "");
    }
}