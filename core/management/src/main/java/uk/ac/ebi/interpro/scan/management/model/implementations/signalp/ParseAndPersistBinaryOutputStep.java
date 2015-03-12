package uk.ac.ebi.interpro.scan.management.model.implementations.signalp;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.signalp.match.SignalPMatchParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SignalPRawMatch;
import uk.ac.ebi.interpro.scan.persistence.SignalPFilteredMatchDAO;

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
     * above.
     * @param temporaryFileDirectory which can be passed into the
     * stepInstance.buildFullyQualifiedFilePath(String temporaryFileDirectory, String fileNameTemplate) method
     */
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {

        // Retrieve raw matches from the SignalP binary output file
        InputStream is = null;
        final String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, signalPBinaryOutputFileName);
        Set<RawProtein<SignalPRawMatch>> rawProteins;
        try {
            is = new FileInputStream(fileName);
            rawProteins = parser.parse(is);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Parsed out " + rawProteins.size() + " proteins with matches from file " + fileName);
                int count = 0;
                for (RawProtein<SignalPRawMatch> rawProtein : rawProteins) {
                    count += rawProtein.getMatches().size();
                }
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
        }
        else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No SignalP matches were persisted as none were found in the SignalP binary output file: " + fileName);
            }
        }


    }
}
