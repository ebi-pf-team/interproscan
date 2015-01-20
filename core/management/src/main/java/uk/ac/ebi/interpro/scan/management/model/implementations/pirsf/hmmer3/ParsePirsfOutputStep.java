package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf.hmmer3;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.pirsf.hmmer3.PirsfHmmer3RawMatchParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.PirsfHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.raw.PirsfHmmer3RawMatchDAO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Parse the output of the pirsf.pl Hmmer3 based binary and persist raw matches.
 */
public class ParsePirsfOutputStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(ParsePirsfOutputStep.class.getName());

    private String pirsfBinaryOutputFileName;

    private PirsfHmmer3RawMatchParser parser;

    private PirsfHmmer3RawMatchDAO rawMatchDAO;

    @Required
    public void setPirsfBinaryOutputFileName(String pirsfBinaryOutputFileName) {
        this.pirsfBinaryOutputFileName = pirsfBinaryOutputFileName;
    }

    @Required
    public void setParser(PirsfHmmer3RawMatchParser parser) {
        this.parser = parser;
    }

    @Required
    public void setRawMatchDAO(PirsfHmmer3RawMatchDAO rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    /**
     * Parse the output file from the PIRSF Hmmer3 binary and persist the results in the database.
     *
     * @param stepInstance           containing the parameters for executing. Provides utility methods as described
     * above.
     * @param temporaryFileDirectory which can be passed into the
     * stepInstance.buildFullyQualifiedFilePath(String temporaryFileDirectory, String fileNameTemplate) method
     */
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {

        // Retrieve raw matches from the PIRSF Hmmer3 binary output file
        InputStream is = null;
        final String fileName = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, pirsfBinaryOutputFileName);
        Set<RawProtein<PirsfHmmer3RawMatch>> rawProteins;
        try {
            is = new FileInputStream(fileName);
            rawProteins = parser.parse(is);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Parsed out " + rawProteins.size() + " proteins with matches from file " + fileName);
                int count = 0;
                for (RawProtein<PirsfHmmer3RawMatch> rawProtein : rawProteins) {
                    count += rawProtein.getMatches().size();
                }
                LOGGER.debug("A total of " + count + " matches from file " + fileName);
            }
            // Store the raw results here - we may need to perform post processing and persist the final results to
            // the database later on...
            rawMatchDAO.insertProteinMatches(rawProteins);
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

    }
}
