package uk.ac.ebi.interpro.scan.management.model.implementations.panther;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.panther.PantherPostProcessor;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.PantherMatch;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.Set;

/**
 * Panther post processing step.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0
 */

public class PantherPostProcessingStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(PantherPostProcessingStep.class.getName());

    private PantherPostProcessor postProcessor;

    private String signatureLibraryRelease;

    private RawMatchDAO<PantherRawMatch> rawMatchDAO;

    private FilteredMatchDAO<PantherRawMatch, PantherMatch> filteredMatchDAO;

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setFilteredMatchDAO(FilteredMatchDAO filteredMatchDAO) {
        this.filteredMatchDAO = filteredMatchDAO;
    }

    @Required
    public void setRawMatchDAO(RawMatchDAO<PantherRawMatch> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    public void setPostProcessor(PantherPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
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
        LOGGER.info("Starting step with Id " + this.getId());
        // Retrieve raw results for protein range.
        Set<RawProtein<PantherRawMatch>> rawMatches = rawMatchDAO.getProteinsByIdRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                signatureLibraryRelease
        );

        // Post process
        Set<RawProtein<PantherRawMatch>> filteredMatches = postProcessor.process(rawMatches);
        LOGGER.info("Finally persisting filtered raw matches.");
        filteredMatchDAO.persist(filteredMatches);
        Utilities.verboseLog("PostProcess panther matches: protein-range : "
                + stepInstance.getBottomProtein() + " - " + stepInstance.getTopProtein()
                + " rawMatches count:  " + rawMatches.size());
        LOGGER.info("Step with Id " + this.getId() + " finished.");

    }
}
