package uk.ac.ebi.interpro.scan.management.model.implementations.smart;

/**
 * User: maslen
 * Date: Oct 5, 2010
 */

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.smart.SmartPostProcessing;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SmartRawMatch;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.SmartHmmer2RawMatchDAO;

import java.io.IOException;
import java.util.Map;


// TODO: Eliminate all code by extending uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.FilterStep
public class SmartPostProcessingStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(SmartPostProcessingStep.class.getName());

    private SmartPostProcessing postProcessor;

    private String signatureLibraryRelease;

    private SmartHmmer2RawMatchDAO rawMatchDAO;

    private FilteredMatchDAO<SmartRawMatch, Hmmer2Match> filteredMatchDAO;


    @Required
    public void setPostProcessor(SmartPostProcessing postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setRawMatchDAO(SmartHmmer2RawMatchDAO rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    @Required
    public void setFilteredMatchDAO(FilteredMatchDAO filteredMatchDAO) {
        this.filteredMatchDAO = filteredMatchDAO;
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
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        // Retrieve raw results for protein range.
        Map<String, RawProtein<SmartRawMatch>> rawMatches = rawMatchDAO.getRawMatchesForProteinIdsInRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                signatureLibraryRelease
        );

        // Post process
        try {
            Map<String, RawProtein<SmartRawMatch>> filteredMatches = postProcessor.process(rawMatches);
            filteredMatchDAO.persist(filteredMatches.values());
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to post process filtered PRINTS matches.", e);
        }
    }
}
