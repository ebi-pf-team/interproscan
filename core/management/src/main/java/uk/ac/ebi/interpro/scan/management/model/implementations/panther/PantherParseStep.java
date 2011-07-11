package uk.ac.ebi.interpro.scan.management.model.implementations.panther;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.panther.PantherMatchParser;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Parses PANTHER output.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class PantherParseStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(PantherParseStep.class.getName());
    private String outputFileNameTemplate;
    private RawMatchDAO<PantherRawMatch> rawMatchDAO;
    private PantherMatchParser parser;
    private String signatureLibraryRelease;

    @Required
    public void setOutputFileNameTemplate(String PantherOutputFileNameTemplate) {
        this.outputFileNameTemplate = PantherOutputFileNameTemplate;
    }

    public String getOutputFileNameTemplate() {
        return outputFileNameTemplate;
    }

    @Required
    public void setRawMatchDAO(RawMatchDAO<PantherRawMatch> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    public void setParser(PantherMatchParser parser) {
        this.parser = parser;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     * <p/>
     * If an error occurs that cannot be immediately recovered from, the implementation
     * of this method MUST throw a suitable Exception, as the call
     * to execute is performed within a transaction with the reply to the JMSBroker.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     * @throws Exception could be anything thrown by the execute method.
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        LOGGER.info("Starting step with Id " + this.getId());
        InputStream stream = null;
        try {
            final String outputFilePath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, outputFileNameTemplate);
            stream = new FileInputStream(outputFilePath);
            final PantherMatchParser parser = this.parser;
            Set<RawProtein<PantherRawMatch>> parsedResults = parser.parse(stream);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("PANTHER: Retrieved " + parsedResults.size() + " proteins.");
                int matchCount = 0;
                for (final RawProtein rawProtein : parsedResults) {
                    matchCount += rawProtein.getMatches().size();
                }
                LOGGER.debug("PANTHER: A total of " + matchCount + " raw matches.");
            }

            // Persist parsed raw matches
            LOGGER.info("Persisting parsed raw matches...");
            rawMatchDAO.insertProteinMatches(parsedResults);
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to parse Panther file " + outputFileNameTemplate, e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    LOGGER.error("Unable to close connection to the Panther output file located at " + outputFileNameTemplate, e);
                }
            }
        }
        LOGGER.info("Step with Id " + this.getId() + " finished.");
    }
}
