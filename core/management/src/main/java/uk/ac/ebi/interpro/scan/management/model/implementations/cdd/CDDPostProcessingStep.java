package uk.ac.ebi.interpro.scan.management.model.implementations.cdd;

import uk.ac.ebi.interpro.scan.management.model.implementations.MatchAndSitePostProcessingStep;
import uk.ac.ebi.interpro.scan.model.RPSBlastMatch;
import uk.ac.ebi.interpro.scan.model.raw.*;

/**
 * @author Gift Nuka, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class CDDPostProcessingStep<A extends CDDRawMatch, B extends RPSBlastMatch, C extends CDDRawSite, D extends RPSBlastMatch.RPSBlastLocation.RPSBlastSite> extends MatchAndSitePostProcessingStep<A, B, C, D> {

//    private static final Logger LOGGER = Logger.getLogger(CDDPostProcessingStep.class.getName());
//
//    /**
//     * This method is called to execute the action that the StepInstance must perform.
//     * <p/>
//     * If an error occurs that cannot be immediately recovered from, the implementation
//     * of this method MUST throw a suitable Exception, as the call
//     * to execute is performed within a transaction with the reply to the JMSBroker.
//     * <p/>
//     * Implementations of this method MAY call delayForNfs() before starting, if, for example,
//     * they are operating of file system resources.
//     *
//     * @param stepInstance           containing the parameters for executing.
//     * @param temporaryFileDirectory
//     */
//    @Override
//    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
//        // Retrieve raw results for protein range.
//        Set<RawProtein<A>> rawMatches = rawMatchDAO.getProteinsByIdRange(
//                stepInstance.getBottomProtein(),
//                stepInstance.getTopProtein(),
//                signatureLibraryRelease
//        );
//
//
//        Map<String, RawProtein<A>> proteinIdToRawProteinMap = new HashMap<>(rawMatches.size());
//        if(rawMatches.size() == 0){
//            Long sequenceCout = stepInstance.getTopProtein() - stepInstance.getBottomProtein();
//            Utilities.verboseLog(10, "Zero matches found: on " + sequenceCout + " proteins stepinstance:" + stepInstance.toString());
//
//            int waitTimeFactor = 2;
//            if (! Utilities.isRunningInSingleSeqMode()){
//                waitTimeFactor = Utilities.getWaitTimeFactorLogE(10 * sequenceCout.intValue()).intValue();
//            }
//            Utilities.sleep(waitTimeFactor * 1000);
//            //try again
//            rawMatches = rawMatchDAO.getProteinsByIdRange(
//                    stepInstance.getBottomProtein(),
//                    stepInstance.getTopProtein(),
//                    signatureLibraryRelease
//            );
//            Utilities.verboseLog(10, "matches after : " + rawMatches.size());
//        }
//
//        int matchCount = 0;
//        for (final RawProtein rawProtein : rawMatches) {
//            matchCount += rawProtein.getMatches().size();
//        }
//        Utilities.verboseLog(10, " CDD: Retrieved " + rawMatches.size() + " proteins to post-process."
//                + " A total of " + matchCount + " raw matches.");
//
//        for (RawProtein<A> rawMatch : rawMatches) {
//            proteinIdToRawProteinMap.put(rawMatch.getProteinIdentifier(), rawMatch);
//        }
//
//        Map<String, RawProtein<A>> filteredMatches = postProcessor.process(proteinIdToRawProteinMap);
//
//        Utilities.verboseLog("filtered matches count: " + filteredMatches.size());
//
//        Set<C> rawSites = rawSiteDAO.getSitesByProteinIdRange(
//                stepInstance.getBottomProtein(),
//                stepInstance.getTopProtein(),
//                signatureLibraryRelease
//        );
//        Utilities.verboseLog("filtered sites: " + rawSites);
//
//        filteredMatchAndSiteDAO.persist(filteredMatches.values(), rawSites);
//
//        matchCount = 0;
//        for (final RawProtein rawProtein : filteredMatches.values()) {
//            matchCount += rawProtein.getMatches().size();
//        }
//        Utilities.verboseLog(10,  " CDD: " + filteredMatches.size() + " proteins passed through post processing."
//                + " with a total of " + matchCount + " raw matches."
//                + " and a total of " + matchCount + " matches PASSED.");
//        if (LOGGER.isDebugEnabled()) {
//            LOGGER.debug("CDD: " + filteredMatches.size() + " proteins passed through post processing.");
//            LOGGER.debug("CDD: A total of " + matchCount + " matches PASSED.");
//        }
//    }
}
