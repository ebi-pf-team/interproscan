package uk.ac.ebi.interpro.scan.io.getorf;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
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

    private static final Logger LOGGER = LogManager.getLogger(GetOrfDescriptionLineParser.class.getName());

    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("\\[(\\d+)\\s+\\-\\s+(\\d+)]\\s*(\\(REVERSE SENSE\\))?\\s*(.*)$");

    public OpenReadingFrame createORFFromParsingResult(String description) {
        final Matcher matcher = DESCRIPTION_PATTERN.matcher(description);
        if (LOGGER.isDebugEnabled()) LOGGER.debug("DESCRIPTION: '" + description + "'");
        if (matcher.find()) {
            if (LOGGER.isDebugEnabled()) LOGGER.debug("Matched!");
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
}
