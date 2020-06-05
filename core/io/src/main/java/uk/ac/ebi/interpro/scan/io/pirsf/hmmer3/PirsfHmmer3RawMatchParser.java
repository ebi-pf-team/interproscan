package uk.ac.ebi.interpro.scan.io.pirsf.hmmer3;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import uk.ac.ebi.interpro.scan.io.match.AbstractLineMatchParser;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PirsfHmmer3RawMatch;

/**
 * Parse the output of the Hmmer3 based prisf.pl script and create raw matches.
 */
public class PirsfHmmer3RawMatchParser
        extends AbstractLineMatchParser<PirsfHmmer3RawMatch>
        implements MatchParser<PirsfHmmer3RawMatch> {

    private static final Logger LOGGER = LogManager.getLogger(PirsfHmmer3RawMatchParser.class.getName());

    /**
     * Constructor is only for JUnit testing.
     */
    protected PirsfHmmer3RawMatchParser() {
        super(null, null);
    }

    public PirsfHmmer3RawMatchParser(String signatureLibraryRelease) {
        super(SignatureLibrary.PIRSF, signatureLibraryRelease);
    }

    @Override
    protected PirsfHmmer3RawMatch createMatch(String line) {
        if (line == null) {
            LOGGER.warn("Couldn't parse the given raw match line, because it is NULL.");
            return null;
        }
        line = line.trim();
        if (line.length() < 1) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Skipping empty line.");
            }
            return null;
        }

        final String[] splitLine = line.split("\\t");
        if (splitLine.length == 17) {
            String locationEndString = splitLine[0].trim(); // To convert to int
            String locationStartString = splitLine[1].trim(); // To convert to int
            final String model = splitLine[2].trim();
            final String sequenceIdentifier = splitLine[3].trim();
            String evalueString = splitLine[4].trim(); // To convert to double
            final String hmmBounds = splitLine[5];
            String hmmEndString = splitLine[6].trim(); // To convert to int
            String hmmStartString = splitLine[7].trim(); // To convert to int
            String locationScoreString = splitLine[8].trim(); // To convert to double
            String scoreString = splitLine[9].trim(); // To convert to double
            String domainBiasString = splitLine[10].trim(); // To convert to double
            String domainCeValueString = splitLine[11].trim(); // To convert to double
            String domainIeValueString = splitLine[12].trim(); // To convert to double
            String envelopeEndString = splitLine[13].trim(); // To convert to int
            String envelopeStartString = splitLine[14].trim(); // To convert to int
            String expectedAccuracyString = splitLine[15].trim(); // To convert to double
            String fullSequenceBiasString = splitLine[16].trim(); // To convert to double

            // To transform raw parsed values
            int locationEnd = 0;
            int locationStart = 0;
            double evalue = 0.0d;
            int hmmEnd = 0;
            int hmmStart = 0;
            double locationScore = 0.0d;
            double score = 0.0d;
            double domainBias= 0.0d;
            double domainCeValue = 0.0d;
            double domainIeValue = 0.0d;
            int envelopeEnd = 0;
            int envelopeStart = 0;
            double expectedAccuracy = 0.0d;
            double fullSequenceBias = 0.0d;

            if (locationEndString.length() > 0) {
                locationEnd = Integer.parseInt(locationEndString);
            }
            if (locationStartString.length() > 0) {
                locationStart = Integer.parseInt(locationStartString);
            }
            if (evalueString.length() > 0) {
                evalue = Double.parseDouble(evalueString);
            }
            if (hmmEndString.length() > 0) {
                hmmEnd = Integer.parseInt(hmmEndString);
            }
            if (hmmStartString.length() > 0) {
                hmmStart =  Integer.parseInt(hmmStartString);
            }
            if (locationScoreString.length() > 0) {
                locationScore = Double.parseDouble(locationScoreString);
            }
            if (scoreString.length() > 0) {
                score = Double.parseDouble(scoreString);
            }
            if (domainBiasString.length() > 0) {
                domainBias = Double.parseDouble(domainBiasString);
            }
            if (domainCeValueString.length() > 0) {
                domainCeValue = Double.parseDouble(domainCeValueString);
            }
            if (domainIeValueString.length() > 0) {
                domainIeValue = Double.parseDouble(domainIeValueString);
            }
            if (envelopeEndString.length() > 0) {
                envelopeEnd =  Integer.parseInt(envelopeEndString);
            }
            if (envelopeStartString.length() > 0) {
                envelopeStart =  Integer.parseInt(envelopeStartString);
            }
            if (expectedAccuracyString.length() > 0) {
                expectedAccuracy =  Double.parseDouble(expectedAccuracyString);
            }
            if (fullSequenceBiasString.length() > 0) {
                fullSequenceBias =  Double.parseDouble(fullSequenceBiasString);
            }

            return new PirsfHmmer3RawMatch(locationEnd, locationStart, model, sequenceIdentifier, evalue, hmmBounds,
                    hmmEnd, hmmStart, locationScore, score, domainBias, domainCeValue, domainIeValue, envelopeEnd,
                    envelopeStart, expectedAccuracy, fullSequenceBias,
                    this.getSignatureLibrary(), this.getSignatureLibraryRelease());
        }
        LOGGER.warn("Couldn't parse the given raw match line, because it is of an unexpected format.");
        LOGGER.warn("Unexpected Raw match line: " + line);
        return null;
    }

}
