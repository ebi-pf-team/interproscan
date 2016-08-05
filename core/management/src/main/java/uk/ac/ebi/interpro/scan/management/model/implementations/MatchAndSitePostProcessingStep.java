package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.PostProcessor;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.StepInstanceCreatingStep;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Site;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawSite;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchAndSiteDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawSiteDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Gift Nuka, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class MatchAndSitePostProcessingStep<A extends RawMatch, B extends Match, C extends RawSite, D extends Site> extends Step {

    private static final Logger LOGGER = Logger.getLogger(MatchAndSitePostProcessingStep.class.getName());

    protected PostProcessor<A> postProcessor;

    protected String signatureLibraryRelease;

    protected RawMatchDAO<A> rawMatchDAO;

    protected RawSiteDAO<C> rawSiteDAO;

    protected FilteredMatchAndSiteDAO<A, B, C, D> filteredMatchAndSiteDAO;

    public void setPostProcessor(PostProcessor<A> postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setRawMatchDAO(RawMatchDAO<A> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    public void setRawSiteDAO(RawSiteDAO<C> rawSiteDAO) {
        this.rawSiteDAO = rawSiteDAO;
    }

    @Required
    public void setFilteredMatchAndSiteDAO(FilteredMatchAndSiteDAO<A, B, C, D> filteredMatchAndSiteDAO) {
        this.filteredMatchAndSiteDAO = filteredMatchAndSiteDAO;
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
        Set<RawProtein<A>> rawMatches = rawMatchDAO.getProteinsByIdRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                signatureLibraryRelease
        );


        Map<String, RawProtein<A>> proteinIdToRawProteinMap = new HashMap<>(rawMatches.size());
        if (rawMatches.size() == 0) {
            Long sequenceCout = stepInstance.getTopProtein() - stepInstance.getBottomProtein();
            Utilities.verboseLog(10, "Zero matches found: on " + sequenceCout + " proteins stepinstance:" + stepInstance.toString());

            int waitTimeFactor = 2;
            if (!Utilities.isRunningInSingleSeqMode()) {
                waitTimeFactor = Utilities.getWaitTimeFactorLogE(10 * sequenceCout.intValue()).intValue();
            }
            Utilities.sleep(waitTimeFactor * 1000);
            //try again
            rawMatches = rawMatchDAO.getProteinsByIdRange(
                    stepInstance.getBottomProtein(),
                    stepInstance.getTopProtein(),
                    signatureLibraryRelease
            );
            Utilities.verboseLog(10, "matches after : " + rawMatches.size());
        }

        int matchCount = 0;
        for (final RawProtein rawProtein : rawMatches) {
            matchCount += rawProtein.getMatches().size();
        }
        Utilities.verboseLog(10, " Retrieved " + rawMatches.size() + " proteins to post-process."
                + " A total of " + matchCount + " raw matches.");

        for (RawProtein<A> rawMatch : rawMatches) {
            proteinIdToRawProteinMap.put(rawMatch.getProteinIdentifier(), rawMatch);
        }

        Map<String, RawProtein<A>> filteredMatches;
        if (postProcessor == null) {
            // No post processing required, raw matches = filtered matches
            filteredMatches = proteinIdToRawProteinMap;
        } else {
            // Post processing required
            filteredMatches = postProcessor.process(proteinIdToRawProteinMap);
        }

        Utilities.verboseLog("filtered matches count: " + filteredMatches.size());

        final Map<String, String> parameters = stepInstance.getParameters();
        final boolean excludeSites = Boolean.TRUE.toString().equals(parameters.get(StepInstanceCreatingStep.EXCLUDE_SITES));
        Set<C> rawSites = new HashSet<>();
        if (!excludeSites) {
            rawSites = rawSiteDAO.getSitesByProteinIdRange(
                    stepInstance.getBottomProtein(),
                    stepInstance.getTopProtein(),
                    signatureLibraryRelease
            );
            Utilities.verboseLog("filtered sites: " + rawSites);
        }
        filteredMatchAndSiteDAO.persist(filteredMatches.values(), rawSites);

        matchCount = 0;
        for (final RawProtein rawProtein : filteredMatches.values()) {
            matchCount += rawProtein.getMatches().size();
        }
        Utilities.verboseLog(10,  "  " + filteredMatches.size() + " proteins passed through post processing."
                + " and a total of " + matchCount + " matches PASSED.");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(filteredMatches.size() + " proteins passed through post processing.");
            LOGGER.debug("A total of " + matchCount + " matches PASSED.");
        }
    }
}
