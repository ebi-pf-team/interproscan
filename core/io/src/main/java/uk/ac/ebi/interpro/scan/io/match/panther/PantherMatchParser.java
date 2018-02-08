package uk.ac.ebi.interpro.scan.io.match.panther;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.match.AbstractLineMatchParser;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;

/**
 * Parser for PANTHER output. Parses a single line of the raw result.
 * <br/>2 example lines of Panther raw result (tab separated entries)
 * tr|Q6ZSE3|Q6ZSE3_HUMAN	PTHR10024:SF2	GB DEF: HYPOTHETICAL PROTEIN FLJ45597	2.3e-141	480.5	1-341
 * <p/>
 * UPI000000004D	PTHR24068	FAMILY NOT NAMED	6.1e-129	439.2(score)	1-147(sequence start and end)
 *
 * @author Maxim Scheremetjew
 * @author Antony Quinn
 * @version $Id$
 */
public final class PantherMatchParser
        extends AbstractLineMatchParser<PantherRawMatch>
        implements MatchParser<PantherRawMatch> {

    private static final Logger LOGGER = Logger.getLogger(PantherMatchParser.class.getName());


    /**
     * Constructor is only for JUnit testing.
     */
    protected PantherMatchParser() {
        super(null, null);
    }

    public PantherMatchParser(String signatureLibraryRelease) {
        super(SignatureLibrary.PANTHER, signatureLibraryRelease);
    }

    @Override
    protected PantherRawMatch createMatch(String line) {
        if (line == null || line.length() == 0) {
            LOGGER.warn("Couldn't parse the given raw match line, because it is NULL or of length 0.");
            return null;
        } else {
            String checkLine = line.toUpperCase();
            if (checkLine.contains("error") || checkLine.contains("warn") || checkLine.contains("cannot")) {
                LOGGER.fatal("Panther match parser detected some failure which occurred during running the binary file. " +
                        "The following lines are logs from the PANTHER Perl script.");
                LOGGER.fatal(line);
            }
        }
        final String[] splitLine = line.split("\\t");
        if (splitLine.length == 9) {
            //Protein Id
            final String sequenceIdentifier = splitLine[0].trim();
            //Parse Panther family ID
            final String modelId = splitLine[1].trim();
            //Parse family name
            final String familyName = splitLine[2].trim();
            //Parse E-Value
            final String eValueString = splitLine[3].trim();
            //Hit score provided by Panther
            final String scoreString = splitLine[4].trim();
            //Hit HMM start and end
            final String hmmLocationStartEnd = splitLine[5].trim();
            //Hit aligment start and end
            final String aliLocationStartEnd = splitLine[6].trim();
            //Hit envelope start and end
            final String envLocationStartEnd = splitLine[7].trim();
            // HMM length
            final String hmmLengthString = splitLine[8].trim();

            //Transform raw parsed values
            double score = 0.0d;
            double evalue = 0.0d;
            int[] hmmLocation = parseLocation(hmmLocationStartEnd);
            int[] aliLocation = parseLocation(aliLocationStartEnd);
            int[] envLocation = parseLocation(envLocationStartEnd);
            int hmmLength = 0;

            if (scoreString.length() > 0 && !".".equals(scoreString)) {
                score = Double.parseDouble(scoreString);
            }
            if (eValueString.length() > 0 && !".".equals(eValueString)) {
                evalue = Double.parseDouble(eValueString);
            }
            if (hmmLengthString.length() > 0) {
                hmmLength = Integer.parseInt(hmmLengthString);
            }

            return new PantherRawMatch(
                    sequenceIdentifier,
                    modelId,
                    getSignatureLibraryRelease(),
                    aliLocation[0],
                    aliLocation[1],
                    evalue,
                    score,
                    familyName,
                    hmmLocation[0],
                    hmmLocation[1],
                    hmmLength,
                    HmmBounds.calculateHmmBounds(envLocation[0], envLocation[1], aliLocation[0], aliLocation[1]),
                    envLocation[0],
                    envLocation[1]);
        }
        LOGGER.warn("Couldn't parse the given raw match line, because it is of an unexpected format.");
        LOGGER.warn("Unexpected Raw match line: " + line);
        return null;
    }

    private int[] parseLocation(String locationStartEnd) {
        int locationStart = 0;
        int locationEnd = 0;
        if (locationStartEnd.length() > 0 && locationStartEnd.contains("-")) {
            final String[] splitLocationStartEnd = locationStartEnd.split("-");
            if (splitLocationStartEnd.length == 2) {
                locationStart = Integer.parseInt(splitLocationStartEnd[0].trim());
                locationEnd = Integer.parseInt(splitLocationStartEnd[1].trim());
            }
        }
        int[] location = {locationStart, locationEnd};
        return location;
    }

}