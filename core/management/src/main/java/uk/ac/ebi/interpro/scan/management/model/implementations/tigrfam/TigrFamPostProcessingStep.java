package uk.ac.ebi.interpro.scan.management.model.implementations.tigrfam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.TigrFamHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.LevelDBStore;

import java.util.Set;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class TigrFamPostProcessingStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(TigrFamPostProcessingStep.class.getName());

    private String signatureLibraryRelease;

    private RawMatchDAO<TigrFamHmmer2RawMatch> rawMatchDAO;

    private FilteredMatchDAO<TigrFamHmmer2RawMatch, Hmmer2Match> filteredMatchDAO;

    private LevelDBStore levelDBStore;

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

    public void setLevelDBStore(LevelDBStore levelDBStore) {
        this.levelDBStore = levelDBStore;
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
        // Retrieve raw results for protein range.
        Set<RawProtein<TigrFamHmmer2RawMatch>> rawMatches = rawMatchDAO.getProteinsByIdRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                signatureLibraryRelease
        );
        filteredMatchDAO.persist(rawMatches);
    }
}
