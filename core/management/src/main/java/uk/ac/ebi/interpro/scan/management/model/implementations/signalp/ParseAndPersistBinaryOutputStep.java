package uk.ac.ebi.interpro.scan.management.model.implementations.signalp;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.signalp.match.SignalPMatchParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SignalPRawMatch;
import uk.ac.ebi.interpro.scan.persistence.SignalPFilteredMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Parse the outputs from running the SignalP binary (ran once in each mode).
 * Persist the raw matches in the database.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ParseAndPersistBinaryOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParseAndPersistBinaryOutputStep.class.getName());

    private String signalPBinaryOutputFileName;

    private SignalPMatchParser parser;

    private SignalPFilteredMatchDAO rawMatchDAO;

    @Required
    public void setSignalPBinaryOutputFileName(String signalPBinaryOutputFileName) {
        this.signalPBinaryOutputFileName = signalPBinaryOutputFileName;
    }

    @Required
    public void setParser(SignalPMatchParser parser) {
        this.parser = parser;
    }

    @Required
    public void setRawMatchDAO(SignalPFilteredMatchDAO rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    /**
     * Parse the output file from the SignalP binary and persist the results in the database.
     *
     * @param stepInstance           containing the parameters for executing. Provides utility methods as described
     *                               above.
     * @param temporaryFileDirectory which can be passed into the
     *                               stepInstance.buildFullyQualifiedFilePath(String temporaryFileDirectory, String fileNameTemplate) method
     */
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        //do we need to skip
        if (checkIfDoSkipRun(stepInstance.getBottomProtein(), stepInstance.getTopProtein())) {
            String key = getKey(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
            Utilities.verboseLog(110, "doSkipRun - step: "  + this.getId() + " - " +  key);
            return;
        }

        // Retrieve raw matches from the SignalP binary output file
        InputStream is = null;
        final String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, signalPBinaryOutputFileName);
        Set<RawProtein<SignalPRawMatch>> rawProteins;
        RawMatch represantiveRawMatch = null;
        int count = 0;
        try {
            is = new FileInputStream(fileName);
            rawProteins = parser.parse(is);

            for (RawProtein<SignalPRawMatch> rawProtein : rawProteins) {
                count += rawProtein.getMatches().size();
                if (represantiveRawMatch == null) {
                    represantiveRawMatch = rawProtein.getMatches().iterator().next();
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Parsed out " + rawProteins.size() + " proteins with matches from file " + fileName);

                LOGGER.debug("A total of " + count + " matches from file " + fileName);
            }
            // NOTE: No post processing therefore no need to store the raw results here - we will just persist them to
            // the database later on...
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse " + fileName, e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                LOGGER.warn("Error closing input stream", e);
            }
        }

        if (rawProteins != null && rawProteins.size() > 0) {
            // Persist the matches
            rawMatchDAO.persist(rawProteins);
            //TODO refactor this
            Long now = System.currentTimeMillis();
            if (count > 0) {
                int matchesFound = 0;
                int waitTimeFactor = Utilities.getWaitTimeFactor(count).intValue();
                if (represantiveRawMatch != null) {
                    Utilities.verboseLog(1100, "represantiveRawMatch :" + represantiveRawMatch.toString());
                    String signatureLibraryRelease = represantiveRawMatch.getSignatureLibraryRelease();
                    Utilities.sleep(waitTimeFactor * 1000);
                    //ignore the usual check until refactoring of the parse step
                } else {
                    LOGGER.warn("Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                    Utilities.verboseLog(1100, "Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                }
                Long timeTaken = System.currentTimeMillis() - now;
                Utilities.verboseLog(1100, "ParseStep: count: " + count + " represantiveRawMatch : " + represantiveRawMatch.toString()
                        + " time taken: " + timeTaken);
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No SignalP matches were persisted as none were found in the SignalP binary output file: " + fileName);
            }
        }


    }
}
