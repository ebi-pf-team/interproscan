package uk.ac.ebi.interpro.scan.management.model.implementations.pirsf.hmmer3;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.pirsf.hmmer3.PirsfPostProcessor;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.raw.PirsfHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.PirsfHmmer3RawMatchDAO;

import java.io.IOException;
import java.util.Map;

/**
 * Currently this step just takes PIRSF Hmmer3 raw matches and persists the relevant matches to the database. However
 * in the future some post processing may also be required. TODO Review this!
 */
public class PirsfPostProcessAndPersistStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(PirsfPostProcessAndPersistStep.class.getName());

    private PirsfPostProcessor postProcessor;

    private String signatureLibraryRelease;

    private PirsfHmmer3RawMatchDAO rawMatchDAO;

    private FilteredMatchDAO<PirsfHmmer3RawMatch, Hmmer3Match> filteredMatchDAO;


    @Required
    public void setPostProcessor(PirsfPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setRawMatchDAO(PirsfHmmer3RawMatchDAO rawMatchDAO) {
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
        Map<String, RawProtein<PirsfHmmer3RawMatch>> rawMatches = rawMatchDAO.getRawMatchesForProteinIdsInRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                signatureLibraryRelease
        );

        // Post process
        try {
            Map<String, RawProtein<PirsfHmmer3RawMatch>> filteredMatches = postProcessor.process(rawMatches);
            filteredMatchDAO.persist(filteredMatches.values());
        } catch (IOException e) {
            throw new IllegalStateException("IOException thrown when attempting to post process filtered PIRSF matches.", e);
        }
    }

}
