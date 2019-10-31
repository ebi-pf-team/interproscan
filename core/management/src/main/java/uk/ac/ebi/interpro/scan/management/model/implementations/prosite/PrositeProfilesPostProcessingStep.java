package uk.ac.ebi.interpro.scan.management.model.implementations.prosite;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.prosite.ProfilePostProcessing;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.ProfileScanMatch;
import uk.ac.ebi.interpro.scan.model.raw.ProSiteProfileRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class PrositeProfilesPostProcessingStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(PrositeProfilesPostProcessingStep.class.getName());

    private ProfilePostProcessing<ProSiteProfileRawMatch> postProcessor;

    private String signatureLibraryRelease;

    private RawMatchDAO<ProSiteProfileRawMatch> rawMatchDAO;

    private FilteredMatchDAO<ProSiteProfileRawMatch, ProfileScanMatch> filteredMatchDAO;

    @Required
    public void setPostProcessor(ProfilePostProcessing<ProSiteProfileRawMatch> postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setRawMatchDAO(RawMatchDAO<ProSiteProfileRawMatch> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    @Required
    public void setFilteredMatchDAO(FilteredMatchDAO<ProSiteProfileRawMatch, ProfileScanMatch> filteredMatchDAO) {
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
            Utilities.verboseLog(10, "doSkipRun - step: "  + this.getId() + " - " +  key);
            return;
        }

        // Retrieve raw results for protein range.
        Set<RawProtein<ProSiteProfileRawMatch>> rawMatches = rawMatchDAO.getProteinsByIdRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                signatureLibraryRelease
        );

        Map<String, RawProtein<ProSiteProfileRawMatch>> proteinIdToRawProteinMap = new HashMap<String, RawProtein<ProSiteProfileRawMatch>>(rawMatches.size());
        for (RawProtein<ProSiteProfileRawMatch> rawMatch : rawMatches) {
            proteinIdToRawProteinMap.put(rawMatch.getProteinIdentifier(), rawMatch);
        }
        Map<String, RawProtein<ProSiteProfileRawMatch>> filteredMatches = postProcessor.process(proteinIdToRawProteinMap);
        filteredMatchDAO.persist(filteredMatches.values());
    }
}
