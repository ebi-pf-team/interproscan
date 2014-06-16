package uk.ac.ebi.interpro.scan.io.pirsf.hmmer3;

import org.apache.log4j.Logger;
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

    private static final Logger LOGGER = Logger.getLogger(PirsfHmmer3RawMatchParser.class.getName());

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
                locationEnd = new Integer(locationEndString);
            }
            if (locationStartString.length() > 0) {
                locationStart = new Integer(locationStartString);
            }
            if (evalueString.length() > 0) {
                evalue = new Double(evalueString);
            }
            if (hmmEndString.length() > 0) {
                hmmEnd = new Integer(hmmEndString);
            }
            if (hmmStartString.length() > 0) {
                hmmStart = new Integer(hmmStartString);
            }
            if (locationScoreString.length() > 0) {
                locationScore = new Double(locationScoreString);
            }
            if (scoreString.length() > 0) {
                score = new Double(scoreString);
            }
            if (domainBiasString.length() > 0) {
                domainBias = new Double(domainBiasString);
            }
            if (domainCeValueString.length() > 0) {
                domainCeValue = new Double(domainCeValueString);
            }
            if (domainIeValueString.length() > 0) {
                domainIeValue = new Double(domainIeValueString);
            }
            if (envelopeEndString.length() > 0) {
                envelopeEnd = new Integer(envelopeEndString);
            }
            if (envelopeStartString.length() > 0) {
                envelopeStart = new Integer(envelopeStartString);
            }
            if (expectedAccuracyString.length() > 0) {
                expectedAccuracy = new Double(expectedAccuracyString);
            }
            if (fullSequenceBiasString.length() > 0) {
                fullSequenceBias = new Double(fullSequenceBiasString);
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

//    public Set<PirsfHmmer3RawMatch> parse(InputStream is) throws IOException, ParseException {
//        BufferedReader reader = null;
//        Set<PirsfHmmer3RawMatch> matches = new HashSet<PirsfHmmer3RawMatch>();
//        try {
//            reader = new BufferedReader(new InputStreamReader(is));
//            String line;
//            int lineCount = 0;
//
//
//            int locationEnd;
//            int locationStart;
//            String model;
//            String sequenceIdentifier;
//            double eValue;
//            String hmmBounds;
//            int hmmEnd;
//            int hmmStart;
//            double locationScore;
//            double score;
//            double domainBias;
//            int domainCeValue;
//            int domainIeValue;
//            int envelopeEnd;
//            int envelopeStart;
//            double expectedAccuracy;
//            double fullSequenceBias;
//
//            while ((line = reader.readLine()) != null) {
//                lineCount++;
//                line = line.trim();
//
//                if (line.equals("")) {
//                    // Empty line, skip!
//                    continue;
//                }
//
//                String[] splitLine = line.split("\t");
//
//                if (splitLine.length != 17) {
//                    LOGGER.warn("Line: " + line + "; has length " + splitLine.length + ", not expected 17.");
//                }
//                locationEnd = Integer.parseInt(splitLine[0]);
//                locationStart = Integer.parseInt(splitLine[1]);
//                model = splitLine[2];
//                sequenceIdentifier = splitLine[3];
//                eValue = Double.parseDouble(splitLine[4]);
//                hmmBounds = splitLine[5];
//                hmmEnd = Integer.parseInt(splitLine[6]);
//                hmmStart = Integer.parseInt(splitLine[7]);
//                locationScore = Double.parseDouble(splitLine[8]);
//                score = Double.parseDouble(splitLine[9]);
//                domainBias = Double.parseDouble(splitLine[10]);
//                domainCeValue = Integer.parseInt(splitLine[11]);
//                domainIeValue = Integer.parseInt(splitLine[12]);
//                envelopeEnd = Integer.parseInt(splitLine[13]);
//                envelopeStart = Integer.parseInt(splitLine[14]);
//                expectedAccuracy = Double.parseDouble(splitLine[15]);
//                fullSequenceBias = Double.parseDouble(splitLine[16]);
//
//                matches.add(new PirsfHmmer3RawMatch(locationEnd, locationStart, model, sequenceIdentifier, eValue, hmmBounds,
//                        hmmEnd, hmmStart, locationScore, score, domainBias, domainCeValue, domainIeValue,
//                        envelopeEnd, envelopeStart, expectedAccuracy, fullSequenceBias));
//            }
//            if (LOGGER.isInfoEnabled()) {
//                LOGGER.info("Finished reading " + lineCount + " lines.");
//            }
//        } finally {
//            if (reader != null) {
//                reader.close();
//            }
//        }
//        return matches;
//
//    }
}
