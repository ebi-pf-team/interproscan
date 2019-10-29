package uk.ac.ebi.interpro.scan.management.model.implementations.prints;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.prints.PrintsPostProcessing;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.FingerPrintsMatch;
import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.PrintsRawMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.IOException;
import java.util.Map;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class PrintsPostProcessingStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(PrintsPostProcessingStep.class.getName());

    private PrintsPostProcessing postProcessor;

    private String signatureLibraryRelease;

    private PrintsRawMatchDAO rawMatchDAO;

    private FilteredMatchDAO<PrintsRawMatch, FingerPrintsMatch> filteredMatchDAO;

    @Required
    public void setPostProcessor(PrintsPostProcessing postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setRawMatchDAO(PrintsRawMatchDAO rawMatchDAO) {
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
        //do we need to skip
        if (doSkipRun) {
            Utilities.verboseLog(10, "doSkipRun - step: "  + this.getId());
            return;
        }

        // Retrieve raw results for protein range.
        Map<String, RawProtein<PrintsRawMatch>> rawMatches = rawMatchDAO.getRawMatchesForProteinIdsInRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                signatureLibraryRelease
        );

        // Post process
        try {
            Map<String, RawProtein<PrintsRawMatch>> filteredMatches = postProcessor.process(rawMatches);
            filteredMatchDAO.persist(filteredMatches.values());
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to post process filtered PRINTS matches.", e);
        }
    }
}
