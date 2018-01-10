package uk.ac.ebi.interpro.scan.management.model.implementations.prosite;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.prosite.PatternPostProcessing;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.ProfileScanMatch;
import uk.ac.ebi.interpro.scan.model.raw.ProSitePatternRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchKVDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class PrositePatternsPostProcessingStep extends Step {

    private static final Logger LOGGER = Logger.getLogger(PrositePatternsPostProcessingStep.class.getName());

    private PatternPostProcessing postProcessor;

    private String signatureLibraryRelease;

    private RawMatchDAO<ProSitePatternRawMatch> rawMatchDAO;

    private FilteredMatchDAO<ProSitePatternRawMatch, ProfileScanMatch> filteredMatchDAO;

    private FilteredMatchKVDAO<ProfileScanMatch,ProSitePatternRawMatch> filteredMatchKVDAO;

    @Required
    public void setPostProcessor(PatternPostProcessing postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setRawMatchDAO(RawMatchDAO<ProSitePatternRawMatch> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    @Required
    public void setFilteredMatchDAO(FilteredMatchDAO<ProSitePatternRawMatch, ProfileScanMatch> filteredMatchDAO) {
        this.filteredMatchDAO = filteredMatchDAO;
    }

    @Required
    public void setFilteredMatchKVDAO(FilteredMatchKVDAO<ProfileScanMatch,ProSitePatternRawMatch> filteredMatchKVDAO) {
        this.filteredMatchKVDAO = filteredMatchKVDAO;
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
        Set<RawProtein<ProSitePatternRawMatch>> rawMatches = rawMatchDAO.getProteinsByIdRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                signatureLibraryRelease
        );

        Map<String, RawProtein<ProSitePatternRawMatch>> proteinIdToRawProteinMap = new HashMap<String, RawProtein<ProSitePatternRawMatch>>(rawMatches.size());
        for (RawProtein<ProSitePatternRawMatch> rawMatch : rawMatches) {
            proteinIdToRawProteinMap.put(rawMatch.getProteinIdentifier(), rawMatch);
        }
        Map<String, RawProtein<ProSitePatternRawMatch>> filteredMatches = postProcessor.process(proteinIdToRawProteinMap);
        filteredMatchKVDAO.persist(filteredMatches.values());
    }
}
