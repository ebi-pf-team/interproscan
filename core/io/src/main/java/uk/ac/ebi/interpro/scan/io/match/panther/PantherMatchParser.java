package uk.ac.ebi.interpro.scan.io.match.panther;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import uk.ac.ebi.interpro.scan.io.match.AbstractLineMatchParser;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;

/**
 * Parser for PANTHER/TreeGrafter output
 *
 * @author Maxim Scheremetjew
 * @author Antony Quinn
 * @author Gift Nuka
 * @author Matthias Blum
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
        /*
            Header of TreeGrafter's output:
            query_id	panther_id	panther_sf	node_id	score	evalue	dom_score	dom_evalue	hmm_start	hmm_end	ali_start	ali_end	env_start	env_end	annotations
         */
        if (splitLine.length >= 15) {
            final String sequenceId = splitLine[0].trim();
            final String familyId = splitLine[1].trim();
            final String subfamilyId = splitLine[2].trim();

            String modelId;
            if (subfamilyId.length() != 0 && !subfamilyId.equals("-")) {
                modelId = familyId + ":" + subfamilyId;
            } else {
                modelId = familyId;
            }

            String nodeId = splitLine[3].trim();;
            if (nodeId.length() == 0 || nodeId.equals("-")) {
                nodeId = null;
            }

            final String scoreString = splitLine[4].trim();
            double score = 0.0d;
            if (scoreString.length() > 0 && !".".equals(scoreString)) {
                score = Double.parseDouble(scoreString);
            }

            final String eValueString = splitLine[5].trim();
            double evalue = 0.0d;
            if (eValueString.length() > 0 && !".".equals(eValueString)) {
                evalue = Double.parseDouble(eValueString);
            }

            final int hmmLocationStart = Integer.parseInt(splitLine[8].trim());
            final int hmmLocationEnd = Integer.parseInt(splitLine[9].trim());
            final int aliLocationStart = Integer.parseInt(splitLine[10].trim());
            final int aliLocationEnd = Integer.parseInt(splitLine[11].trim());
            final int envLocationStart = Integer.parseInt(splitLine[12].trim());
            final int envLocationEnd = Integer.parseInt(splitLine[13].trim());

            String annotations = splitLine[14].trim();
            if (annotations.equals("-")) {
                annotations = "";
            } else if (annotations.length() >= 8000) {
                annotations = annotations.substring(0, 7990);
            }

            int hmmLength = 0;

            return new PantherRawMatch(
                    sequenceId,
                    modelId,
                    getSignatureLibraryRelease(),
                    aliLocationStart,
                    aliLocationEnd,
                    evalue,
                    score,
                    hmmLocationStart,
                    hmmLocationEnd,
                    hmmLength,
                    HmmBounds.calculateHmmBounds(envLocationStart,envLocationEnd, aliLocationStart, aliLocationEnd),
                    envLocationStart,
                    envLocationEnd,
                    nodeId,
                    annotations);
        }

        LOGGER.warn("Couldn't parse the given raw match line, because it is of an unexpected format.");
        LOGGER.warn("Unexpected Raw match line: " + line);
        return null;
    }
}
