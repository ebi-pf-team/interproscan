package uk.ac.ebi.interpro.scan.management.model.implementations.superfamily;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.superfamily.match.SuperFamilyHmmer3MatchParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SuperFamilyHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.persistence.SuperFamilyHmmer3FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * This step parses the output from the SuperFamily Perl script and then persists the matches.
 * No match filtering post processing required.
 *
 * @author Matthew Fraser
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ParseAndPersistSuperFamilyOutputStep extends Step {

    private static final Logger LOGGER = LogManager.getLogger(ParseAndPersistSuperFamilyOutputStep.class.getName());

    private String superFamilyBinaryOutputFileName;

    private SuperFamilyHmmer3MatchParser parser;

    private SuperFamilyHmmer3FilteredMatchDAO filteredMatchDAO;

    @Required
    public void setSuperFamilyBinaryOutputFileName(String superFamilyBinaryOutputFileName) {
        this.superFamilyBinaryOutputFileName = superFamilyBinaryOutputFileName;
    }

    @Required
    public void setParser(SuperFamilyHmmer3MatchParser parser) {
        this.parser = parser;
    }

    @Required
    public void setFilteredMatchDAO(SuperFamilyHmmer3FilteredMatchDAO filteredMatchDAO) {
        this.filteredMatchDAO = filteredMatchDAO;
    }

    /**
     * Parse the output file from the SuperFamily binary and persist the results in the database.
     *
     * @param stepInstance           containing the parameters for executing. Provides utility methods as described
     *                               above.
     * @param temporaryFileDirectory which can be passed into the
     *                               stepInstance.buildFullyQualifiedFilePath(String temporaryFileDirectory, String fileNameTemplate) method
     */
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        Long bottomProtein = stepInstance.getBottomProtein();
        Long topProtein = stepInstance.getTopProtein();

        //do we need to skip
        if (checkIfDoSkipRun(stepInstance.getBottomProtein(), stepInstance.getTopProtein())) {
            String key = getKey(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
            Utilities.verboseLog(110, "doSkipRun - step: "  + this.getId() + " - " +  key);
            return;
        }

        Utilities.verboseLog(1100, "ParseAndPersistSuperFamilyOutputStep: Start parse and persist [" + bottomProtein + "-" + topProtein + "]");
        // Retrieve raw matches from the SuperFamily binary output file
        InputStream inputStream = null;
        final String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, superFamilyBinaryOutputFileName);
        Set<RawProtein<SuperFamilyHmmer3RawMatch>> rawProteins;
        int count = 0;
        RawMatch representativeRawMatch = null;
        try {
            inputStream = new FileInputStream(fileName);
            rawProteins = parser.parse(inputStream);

            // Get a representative raw match and note the number of raw matches for logging and validation purposes
            for (RawProtein<SuperFamilyHmmer3RawMatch> rawProtein : rawProteins) {
                count += rawProtein.getMatches().size();
                if (representativeRawMatch == null) {
                    if (rawProtein.getMatches().size() > 0) {
                        representativeRawMatch = rawProtein.getMatches().iterator().next();
                    }
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Parsed out " + rawProteins.size() + " proteins with matches from file " + fileName);
                LOGGER.debug("A total of " + count + " raw matches from file " + fileName);
            }
            // NOTE: No post processing therefore no need to store the raw results here - we will just persist them to
            // the database later on...
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse " + fileName, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LOGGER.warn("Error closing input stream", e);
            }
        }

        if (rawProteins.size() > 0) {
            // Persist the matches
            Utilities.verboseLog(1100, "ParseAndPersistSuperFamilyOutputStep: Persist the parsed matches for proteins: " + bottomProtein + "-" + topProtein);
            filteredMatchDAO.persist(rawProteins);
            Utilities.verboseLog(1100, "ParseAndPersistSuperFamilyOutputStep: Completed persisting the parsed matches for proteins: " + bottomProtein + "-" + topProtein);
            //TODO refactor this
            Long now = System.currentTimeMillis();
            if (count > 0) {
                int waitTimeFactor = Utilities.getWaitTimeFactor(count).intValue();
                if (representativeRawMatch != null) {
                    Utilities.verboseLog(1100, "represantiveRawMatch :" + representativeRawMatch.toString());
                    Utilities.sleep(waitTimeFactor * 1000);
                    //ignore the usual check until refactoring of the parse step
                } else {
                    LOGGER.warn("Check if Raw matches committed " + count + " rm: " + representativeRawMatch);
                    Utilities.verboseLog(1100, "Check if Raw matches committed " + count + " rm: " + representativeRawMatch);
                }
                Long timeTaken = System.currentTimeMillis() - now;
                Utilities.verboseLog(1100, "ParseStep: count: " + count + " represantiveRawMatch : " + representativeRawMatch.toString()
                        + " time taken: " + timeTaken);
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No SuperFamily matches were persisted as none were found in the SuperFamily binary output file: " + fileName);
            }
        }
        Utilities.verboseLog(1100, "ParseAndPersistSuperFamilyOutputStep: Completed parse and persist [" + bottomProtein + "-" + topProtein + "]");

    }
}
