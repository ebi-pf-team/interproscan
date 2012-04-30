package uk.ac.ebi.interpro.scan.management.model.implementations.superfamily;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.superfamily.match.SuperFamilyHmmer3MatchParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SuperFamilyHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.persistence.SuperFamilyHmmer3FilteredMatchDAO;

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

    private static final Logger LOGGER = Logger.getLogger(ParseAndPersistSuperFamilyOutputStep.class.getName());

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

        // Retrieve raw matches from the SuperFamily binary output file
        InputStream inputStream = null;
        final String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, superFamilyBinaryOutputFileName);
        Set<RawProtein<SuperFamilyHmmer3RawMatch>> rawProteins;
        try {
            inputStream = new FileInputStream(fileName);
            rawProteins = parser.parse(inputStream);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Parsed out " + rawProteins.size() + " proteins with matches from file " + fileName);
                int count = 0;
                for (RawProtein<SuperFamilyHmmer3RawMatch> rawProtein : rawProteins) {
                    count += rawProtein.getMatches().size();
                }
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
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No SuperFamily matches were persisted as none were found in the SuperFamily binary output file: " + fileName);
            }
        }


    }
}
