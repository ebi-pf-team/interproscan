package uk.ac.ebi.interpro.scan.management.model.implementations.hamap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.prosite.ProfilePostProcessing;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.ProfileScanMatch;
import uk.ac.ebi.interpro.scan.model.raw.HamapRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Post processing for HamapHmm
 *
 * @author Gift Nuka
 *
 */

public class HamapHmmPostProcessingStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(HamapHmmPostProcessingStep.class.getName());

    private ProfilePostProcessing<HamapRawMatch> postProcessor;

    private String signatureLibraryRelease;

    private RawMatchDAO<HamapRawMatch> rawMatchDAO;

    private FilteredMatchDAO<HamapRawMatch, ProfileScanMatch> filteredMatchDAO;

    @Required
    public void setPostProcessor(ProfilePostProcessing<HamapRawMatch> postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setRawMatchDAO(RawMatchDAO<HamapRawMatch> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    @Required
    public void setFilteredMatchDAO(FilteredMatchDAO<HamapRawMatch, ProfileScanMatch> filteredMatchDAO) {
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
        // Retrieve raw results for protein range.
        Set<RawProtein<HamapRawMatch>> rawMatches = rawMatchDAO.getProteinsByIdRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                signatureLibraryRelease
        );

        LOGGER.debug("print the raw matches");
        for (RawProtein<HamapRawMatch> rawMatch: rawMatches ) {
            LOGGER.debug(rawMatch);
        }
        //do we need to run the post processor? maybe for now no???
        //we now persist the raw matches
        filteredMatchDAO.persist(rawMatches);

        //maybe we dont need the following for now

        /*
        Map<String, RawProtein<HamapRawMatch>> proteinIdToRawProteinMap = new HashMap<String, RawProtein<HamapRawMatch>>(rawMatches.size());
        for (RawProtein<HamapRawMatch> rawMatch : rawMatches) {
            proteinIdToRawProteinMap.put(rawMatch.getProteinIdentifier(), rawMatch);
        }
        Map<String, RawProtein<HamapRawMatch>> filteredMatches = postProcessor.process(proteinIdToRawProteinMap);
        LOGGER.debug("print the filtered matches");
        for (RawProtein<HamapRawMatch> rawMatch: filteredMatches.values() ) {
            LOGGER.debug(rawMatch);
        }
        filteredMatchDAO.persist(filteredMatches.values());
        */


    }
}
