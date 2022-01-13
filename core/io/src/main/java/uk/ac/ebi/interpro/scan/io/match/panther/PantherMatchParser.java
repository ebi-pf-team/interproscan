package uk.ac.ebi.interpro.scan.io.match.panther;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
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

    private static final Logger LOGGER = LogManager.getLogger(PantherMatchParser.class.getName());


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
        if (line.startsWith("query_id")) {
            //LOGGER.warn("This is a header line .");
            return null;
        }
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
        //Utilities.verboseLog(110, "splitLine.length: " + splitLine.length);
        /*
            Header of TreeGrafter's output:
            query_id	panther_id	panther_sf	node_id	score	evalue	dom_score	dom_evalue	hmm_start	hmm_end	ali_start	ali_end	env_start	env_end	annotations
         */
        if (splitLine.length >= 15) {
            //Protein Id
            final String sequenceIdentifier = splitLine[0].trim();
            //Parse Panther family ID
            final String pantherFamilyId = splitLine[1].trim();
            //Parse sub family id
            final String subFamilyModelIdPart = splitLine[2].trim();
            String subFamilyModelId = null;
            //the subfamily maybe empty
            if ( subFamilyModelIdPart.strip().length() != 0) {
                subFamilyModelId = pantherFamilyId + ":" + subFamilyModelIdPart;
            }
            //Parse  annotation node Id
            final String annotationsNodeId = splitLine[3].trim();
            //Hit score provided by Panther
            final String scoreString = splitLine[4].trim();
            //Parse E-Value
            final String eValueString = splitLine[5].trim();
            //Hit domain score provided by Panther
            final String domainScoreString = splitLine[6].trim();
            //Hit domain evalue provided by Panther
            final String domainEValueStringString = splitLine[7].trim();
            //Hit HMM start
            final String hmmLocationStartString = splitLine[8].trim();
            //Hit HMM end
            final String hmmLocationEndString = splitLine[9].trim();
            //Hit aligment start
            final String aliLocationStartString = splitLine[10].trim();
            //Hit aligment start and end
            final String aliLocationEndString = splitLine[11].trim();
            //Hit envelope start
            final String envLocationStartString = splitLine[12].trim();
            //Hit envelope end
            final String envLocationEndString = splitLine[13].trim();
            //PAINT annotations
            String annotations = splitLine[14].trim();

            if (annotations.equals("-")) {
                annotations = "";
            } else if (annotations.length() >= 8000) {
                annotations = annotations.substring(0, 7990);
            }

            //Transform raw parsed values
            double score = 0.0d;
            double evalue = 0.0d;

            int hmmLocationStart = Integer.parseInt(hmmLocationStartString);
            int hmmLocationEnd = Integer.parseInt(hmmLocationEndString);
            int aliLocationStart = Integer.parseInt(aliLocationStartString);
            int aliLocationEnd = Integer.parseInt(aliLocationEndString);
            int envLocationStart = Integer.parseInt(envLocationStartString);
            int envLocationEnd = Integer.parseInt(envLocationEndString);

            int hmmLength = 0;

            if (scoreString.length() > 0 && !".".equals(scoreString)) {
                score = Double.parseDouble(scoreString);
            }
            if (eValueString.length() > 0 && !".".equals(eValueString)) {
                evalue = Double.parseDouble(eValueString);
            }

            return new PantherRawMatch(
                    sequenceIdentifier,
                    pantherFamilyId,
                    subFamilyModelId,
                    annotationsNodeId,
                    getSignatureLibraryRelease(),
                    aliLocationStart,
                    aliLocationEnd,
                    evalue,
                    score,
                    pantherFamilyId,
                    hmmLocationStart,
                    hmmLocationEnd,
                    hmmLength,
                    HmmBounds.calculateHmmBounds(envLocationStart,envLocationEnd, aliLocationStart, aliLocationEnd),
                    envLocationStart,
                    envLocationEnd,
                    annotations);
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
