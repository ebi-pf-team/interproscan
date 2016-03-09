package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.cdd.CDDPostProcessing;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchDAO;
import uk.ac.ebi.interpro.scan.persistence.raw.RawMatchDAO;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Gift Nuka, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class PostProcessingStep<T extends RawMatch, U extends Match> extends Step {

    private static final Logger LOGGER = Logger.getLogger(PostProcessingStep.class.getName());

    private PostProcessor<T> postProcessor;

    private String signatureLibraryRelease;

    private RawMatchDAO<T> rawMatchDAO;

    private FilteredMatchDAO<T, U> filteredMatchDAO;

    @Required
    public void setPostProcessor(PostProcessor<T> postProcessor) {
        this.postProcessor = postProcessor;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    @Required
    public void setRawMatchDAO(RawMatchDAO<T> rawMatchDAO) {
        this.rawMatchDAO = rawMatchDAO;
    }

    @Required
    public void setFilteredMatchDAO(FilteredMatchDAO<T, U> filteredMatchDAO) {
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
        Set<RawProtein<T>> rawMatches = rawMatchDAO.getProteinsByIdRange(
                stepInstance.getBottomProtein(),
                stepInstance.getTopProtein(),
                signatureLibraryRelease
        );

        Map<String, RawProtein<T>> proteinIdToRawProteinMap = new HashMap<String, RawProtein<T>>(rawMatches.size());
        if(rawMatches.size() == 0){
            Long sequenceCout = stepInstance.getTopProtein() - stepInstance.getBottomProtein();
            Utilities.verboseLog(10, "Zero matches found: on " + sequenceCout + " proteins stepinstance:" + stepInstance.toString());

            int waitTimeFactor = 2;
            if (! Utilities.isRunningInSingleSeqMode()){
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
        Utilities.verboseLog(10, "  Retrieved " + rawMatches.size() + " proteins to post-process."
                + " A total of " + matchCount + " raw matches.");

        for (RawProtein<T> rawMatch : rawMatches) {
            proteinIdToRawProteinMap.put(rawMatch.getProteinIdentifier(), rawMatch);
        }
        Map<String, RawProtein<T>> filteredMatches = postProcessor.process(proteinIdToRawProteinMap);
        filteredMatchDAO.persist(filteredMatches.values());
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
