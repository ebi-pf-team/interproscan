package uk.ac.ebi.interpro.scan.management.model.implementations.prodom;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.prodom.match.BlastProDomMatchParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.ProDomRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.ProDomFilteredMatchDAO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This step parses the output from the ProDom Perl script and then persists the matches.
 * No post processing (match filtering) required.
 *
 * @author Matthew Fraser
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class ParseAndPersistProDomOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParseAndPersistProDomOutputStep.class.getName());

    private String proDomBinaryOutputFileName;

    private BlastProDomMatchParser parser;

    private ProDomFilteredMatchDAO rawMatchDAO;

    @Required
    public void setProDomBinaryOutputFileName(String proDomBinaryOutputFileName) {
        this.proDomBinaryOutputFileName = proDomBinaryOutputFileName;
    }

    @Required
    public void setParser(BlastProDomMatchParser parser) {
        this.parser = parser;
    }

    @Required
    public void setRawMatchDAO(ProDomFilteredMatchDAO rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    /**
     * Parse the output file from the ProDom binary and persist the results in the database.
     *
     * @param stepInstance           containing the parameters for executing. Provides utility methods as described
     * above.
     * @param temporaryFileDirectory which can be passed into the
     * stepInstance.buildFullyQualifiedFilePath(String temporaryFileDirectory, String fileNameTemplate) method
     */
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {

        // Retrieve raw matches from the ProDom binary output file
        InputStream is = null;
        final String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, proDomBinaryOutputFileName);
        Set<RawProtein<ProDomRawMatch>> rawProteins;
        try {
            is = new FileInputStream(fileName);
            rawProteins = parser.parse(is);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Parsed out " + rawProteins.size() + " proteins with matches from file " + fileName);
                int count = 0;
                for (RawProtein<ProDomRawMatch> rawProtein : rawProteins) {
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
                LOGGER.debug("No ProDom matches were persisted as none were found in the ProDom binary output file: " + fileName);
            }
        }


    }
}
