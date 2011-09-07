package uk.ac.ebi.interpro.scan.management.model.implementations.tmhmm;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.match.panther.PantherMatchParser;
import uk.ac.ebi.interpro.scan.io.tmhmm.TMHMMPredicationTableParser;
import uk.ac.ebi.interpro.scan.io.tmhmm.TMHMMPredicationTableParserOld;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.TMHMMMatch;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.TMHMMRawMatch;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * Parses TMHMM (Prediction of transmembrane helices in proteins) output.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public final class TMHMMParseStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(TMHMMParseStep.class.getName());
    private String outputFileNameTemplate;
    //    private RawMatchDAO<PantherRawMatch> rawMatchDAO;
    private TMHMMPredicationTableParser parser;
    private String signatureLibraryRelease;

    @Required
    public void setOutputFileNameTemplate(String PantherOutputFileNameTemplate) {
        this.outputFileNameTemplate = PantherOutputFileNameTemplate;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }

    @Required
    public void setParser(TMHMMPredicationTableParser parser) {
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

            parser.parse(stream);

//            if (LOGGER.isDebugEnabled()) {
//                LOGGER.debug("TMHMM: Retrieved " + parsedResults.size() + " proteins.");
//                int locationCount = 0;
//                for (final String sequenceIdentifier : parsedResults.keySet()) {
//                    locationCount += parsedResults.get(sequenceIdentifier).getMatches().size();
//                }
//                LOGGER.debug("TMHHM: A total of " + locationCount + " locations found.");
//            }

            // Persist parsed raw matches
//            LOGGER.info("Persisting parsed raw matches...");
//            rawMatchDAO.insertProteinMatches(parsedResults);
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
