package uk.ac.ebi.interpro.scan.management.model.implementations.tigrfam;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.TigrFamHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.Set;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class TigrFamPostProcessingStep extends Step {

    private static final Logger LOGGER = LogManager.getLogger(TigrFamPostProcessingStep.class.getName());

    private String signatureLibraryRelease;

    private RawMatchDAO<TigrFamHmmer2RawMatch> rawMatchDAO;

    private FilteredMatchDAO<TigrFamHmmer2RawMatch, Hmmer2Match> filteredMatchDAO;

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setRawMatchDAO(RawMatchDAO<TigrFamHmmer2RawMatch> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    @Required
    public void setFilteredMatchDAO(FilteredMatchDAO<TigrFamHmmer2RawMatch, Hmmer2Match> filteredMatchDAO) {
        this.filteredMatchDAO = filteredMatchDAO;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     * <p/>
     * If an error occurs that cannot be immediately recovered from, the implementation
     * of this method MUST throw a suitable Exception, as the call
     * to execute is performed within a transaction with the reply to the JMSBroker.
     * <p/>
     * Implementations of this method MAY call delayForNfs() before starting, if, for example,
     * they are operating of file system resources.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        //do we need to skip
        if (checkIfDoSkipRun(stepInstance.getBottomProtein(), stepInstance.getTopProtein())) {
            String key = getKey(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
            Utilities.verboseLog(110, "doSkipRun - step: "  + this.getId()  + " -- " + key);
            return;
        }

        // Retrieve raw results for protein range.
        Set<RawProtein<TigrFamHmmer2RawMatch>> rawMatches = rawMatchDAO.getProteinsByIdRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                signatureLibraryRelease
        );
        filteredMatchDAO.persist(rawMatches);
    }
}
