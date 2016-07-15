package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.io.superfamily.match.SuperFamilyHmmer3MatchParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.ProfileScanMatch;
import uk.ac.ebi.interpro.scan.model.raw.HamapRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SuperFamilyHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.SuperFamilyHmmer3FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * This step parses the output from the binary run and then persists the matches.
 * No match filtering post processing required.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ParseAndPersistStep<T extends RawMatch, U extends Match> extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParseAndPersistStep.class.getName());

    private String outputFileTemplate;
    private MatchParser<T> parser;

    private FilteredMatchDAO<T, U> filteredMatchDAO;

    @Required
    public void setParser(MatchParser<T> parser) {
        this.parser = parser;
    }

    public String getOutputFileTemplate() {
        return outputFileTemplate;
    }

    @Required
    public void setOutputFileTemplate(String template) {
        this.outputFileTemplate = template;
    }

    @Required
    public void setFilteredMatchDAO(FilteredMatchDAO<T, U> filteredMatchDAO) {
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

        // Retrieve raw matches from the SuperFamily binary output file
        InputStream inputStream = null;
        final String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, outputFileTemplate);
        Set<RawProtein<T>> rawProteins;
        int count = 0;
        RawMatch represantiveRawMatch = null;
        try {
            inputStream = new FileInputStream(fileName);
            rawProteins = parser.parse(inputStream);

            for (RawProtein<T> rawProtein : rawProteins) {
                count += rawProtein.getMatches().size();
                if (represantiveRawMatch == null) {
                    if (rawProtein.getMatches().size() > 0) {
                        represantiveRawMatch = rawProtein.getMatches().iterator().next();
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

        if (rawProteins != null && rawProteins.size() > 0) {
            // Persist the matches
            filteredMatchDAO.persist(rawProteins);
            //TODO refactor this
            Long now = System.currentTimeMillis();
            if (count > 0){
                int matchesFound = 0;
                int waitTimeFactor = Utilities.getWaitTimeFactor(count).intValue();
                if (represantiveRawMatch != null) {
                    Utilities.verboseLog("represantiveRawMatch :" + represantiveRawMatch.toString());
                    String signatureLibraryRelease = represantiveRawMatch.getSignatureLibraryRelease();
                    Utilities.sleep(waitTimeFactor * 1000);
                    //ignore the usual check until refactoring of the parse step
                }else{
                    LOGGER.warn("Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                    Utilities.verboseLog("Check if Raw matches committed " + count + " rm: " + represantiveRawMatch);
                }
                Long timeTaken = System.currentTimeMillis() - now;
                Utilities.verboseLog("ParseStep: count: " + count + " represantiveRawMatch : " + represantiveRawMatch.toString()
                        + " time taken: " + timeTaken);
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No SuperFamily matches were persisted as none were found in the SuperFamily binary output file: " + fileName);
            }
        }


    }
}
