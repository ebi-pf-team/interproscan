package uk.ac.ebi.interpro.scan.business.postprocessing.smart;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.smart.SmartOverlappingFileParser;
import uk.ac.ebi.interpro.scan.io.smart.SmartThresholdFileParser;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SmartRawMatch;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAOImpl;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

/**
 * Smart post-processing requires 2 licensed files that need to be obtained from Smart for threshold and overlap data.
 * The "overlapping" and "THRESHOLDS" files are not included with an InterProScan by default, and if one or both are
 * not present then no filtering is performed (all raw matches become filtered matches).
 */
public class SmartPostProcessing implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(SmartPostProcessing.class.getName());

    //Kinases are processed separately, and the Smart accessions for the relevant methods are stored here -> are they permanent?
    private final String SMART_SER_THR_KINASE_METHOD    = "SM00220";
    private final String SMART_TYR_KINASE_METHOD        = "SM00219";
    private final String SMART_MERGE_TAG                = "merge";
    private final String SMART_SPLIT_TAG                = "split";
    private final int SMART_MAX_PRIORITY                = 1;
    private final double DBL_EPSILON                    = 2.220446049250313E-16;
    private final int DBL_MAX_10_EXP                    = 308;
    private final String SMART_TYR_REGEX                = ".*HRD[LIV][AR]\\w\\wN.*";
    private final String SMART_SER_THR_REGEX            = ".*D[LIVM]K\\w\\wN.*";
    private final int SIBLINGS_OVERLAP_THRESHOLD        = 10; //AAs

    // Properties
    private SmartThresholdFileParser thresholdFileParser;
    private SmartOverlappingFileParser overlappingFileParser;
    private Resource thresholdFileResource;
    private Resource overlappingFileResource;

    @Required
    public void setThresholdFileParser(SmartThresholdFileParser thresholdFileParser) {
        this.thresholdFileParser = thresholdFileParser;
    }

    @Required
    public void setOverlappingFileParser(SmartOverlappingFileParser overlappingFileParser) {
        this.overlappingFileParser = overlappingFileParser;
    }

    /**
     * Required for post processing to happen - if not present then match filtering is not performed (all raw matches
     * becomes filtered matches).
     *
     * @param thresholdFileResource The location of the Smart threshold data file, e.g. "THRESHOLDS"
     */
    public void setThresholdFileResource(Resource thresholdFileResource) {
        this.thresholdFileResource = thresholdFileResource;
    }

    /**
     * Required for post processing to happen - if not present then match filtering is not performed (all raw matches
     * becomes filtered matches).
     *
     * @param overlappingFileResource The location of the Smart overlap data file, e.g. "overlapping"
     */
    public void setOverlappingFileResource(Resource overlappingFileResource) {
        this.overlappingFileResource = overlappingFileResource;
    }

    /**
     * Perform post processing.
     *
     * @param proteinIdToRawProteinMap Map of protein accessions to raw proteins to process
     * @return The filtered (processed) map of protein accessions to raw proteins
     * @throws IOException If a required file resource could not be accessed
     */
    public Map<String, RawProtein<SmartRawMatch>> process(Map<String, RawProtein<SmartRawMatch>> proteinIdToRawProteinMap) throws IOException {

        // FIX: Moved from module level
        Map<String, SmartThresholdFileParser.SmartThreshold> smartThresholdMap;
        Map<String, SmartOverlappingFileParser.SmartOverlap> smartOverlapMap;
        Map<String, String> familyMap = new HashMap<String, String>();
        Map<String, String> accessionMap = new HashMap<String, String>();
        Map<String, String> mergeMap = new HashMap<String, String>();

        Map<String, RawProtein<SmartRawMatch>> allFilteredMatches = new HashMap<String, RawProtein<SmartRawMatch>>();

        if (thresholdFileResource == null || overlappingFileResource == null) {
            // One of the thresholds and overlapping files was not present (possibly the user is using an unlicensed
            // version of SMART), therefore no filtering can be performed
            if (thresholdFileResource == null) {
                LOGGER.warn("Smart threshold file resource is not configured - Smart post processing skipped");
            }
            if (overlappingFileResource == null) {
                LOGGER.warn("Smart overlapping file resource is not configured - Smart post processing skipped");
            }
            // All raw matches become filtered matches
            for (String proteinId : proteinIdToRawProteinMap.keySet()) {
                RawProtein<SmartRawMatch> matchRawProtein = proteinIdToRawProteinMap.get(proteinId);
                for (SmartRawMatch smartRawMatch : matchRawProtein.getMatches()) {
                    String matchId = smartRawMatch.getSequenceIdentifier();
                    RawProtein<SmartRawMatch> p;
                    if (allFilteredMatches.containsKey(matchId)) {
                        p = allFilteredMatches.get(matchId);
                    } else {
                        p = new RawProtein<SmartRawMatch>(matchId);
                        allFilteredMatches.put(matchId, p);
                    }
                    p.addMatch(smartRawMatch);
                }
            }
        }
        else {
            // Thresholds and overlapping files are present, therefore filtering can be performed
            smartThresholdMap = thresholdFileParser.parse(thresholdFileResource);
            smartOverlapMap = overlappingFileParser.parse(overlappingFileResource);

            // Need hashmap of accession, family name, only where no. family members is > 1 and family name is not serine kinase (other kinase??)
            populateFamilyMap(smartThresholdMap, accessionMap, familyMap);
            populateMergeMap(smartOverlapMap, accessionMap, mergeMap);

            for (String proteinId : proteinIdToRawProteinMap.keySet()) {
                processProtein(proteinIdToRawProteinMap.get(proteinId), allFilteredMatches, smartThresholdMap, smartOverlapMap, familyMap, mergeMap);
            }
        }

        return allFilteredMatches;
    }

    private void processProtein (RawProtein<SmartRawMatch> matchRawProtein,
                                 Map<String, RawProtein<SmartRawMatch>> filteredMatches,
                                 Map<String, SmartThresholdFileParser.SmartThreshold> smartThresholdMap,
                                 Map<String, SmartOverlappingFileParser.SmartOverlap> smartOverlapMap,
                                 Map<String, String> familyMap,
                                 Map<String, String> mergeMap) {

        Map<String, Integer> repeatsCntHM = new HashMap<String, Integer>();
        Map<String, List<SmartRawMatch>> siblingsHits = new HashMap<String, List<SmartRawMatch>>();

        for (SmartRawMatch smartRawMatch : matchRawProtein.getMatches()) {
            String methodAc = smartRawMatch.getModelId();
            // TODO: Score and seqscore wrong way round?
            Double score = smartRawMatch.getScore();
            Double seqScore = smartRawMatch.getLocationScore();
            SmartThresholdFileParser.SmartThreshold smartThreshold = smartThresholdMap.get(methodAc);
            // Create the eValue for the match, according to Smart THRESHOLDS file
            double wholeSeqEVal = hmmerCalcEValue(seqScore.floatValue(), smartThreshold.getMuValue(), smartThreshold.getLambdaValue(), smartThreshold.getDbSize());
            double singleHitEVal =hmmerCalcEValue(score.floatValue(), smartThreshold.getMuValue(), smartThreshold.getLambdaValue(), smartThreshold.getDbSize());

            // FIX: Moved from module level
            double domainCutoff = Double.NaN;
            double repeatsCutoff = Double.MAX_VALUE;
            Double repeatsCutoffDomain = null;
            String minRepeatsInSeqS = null;
            List<SmartRawMatch> potentialRepeatMatches = new ArrayList<SmartRawMatch>();
            Map<Integer, SmartRawMatch> serThrKinaseMatches = null;
            Map<Integer, SmartRawMatch> tyrKinaseMatches = null;

            domainCutoff = smartThreshold.getCutoff();
            repeatsCutoffDomain = smartThreshold.getRepeat_cut();
            minRepeatsInSeqS = smartThreshold.getRepeats();
            if (repeatsCutoffDomain != null) {
                // N.B. We define repeats signatures for which either repeatsCutoffD and/or minRepeatsInSeqS != null holds;
                repeatsCutoff = repeatsCutoffDomain;
            }

            if (setEval2NPrec(wholeSeqEVal, getRequiredPrecForComparisonTo(domainCutoff)) >= domainCutoff) {
                // the hit is a false positive => reject it.

            } else if (repeatsCutoffDomain != null && setEval2NPrec(singleHitEVal, getRequiredPrecForComparisonTo(repeatsCutoff)) > repeatsCutoff) {
                //reject

            } else if (minRepeatsInSeqS != null) {
                // The repeat hit either doesn't have repeatsCutoffD set, or satisfies it;
                // now, if enough repeats are present (repeatsCnt >= minRepeatsInSeq), this hit will be accepted =>
                // store it in potentialRepeatMatches for now
                if (potentialRepeatMatches == null)
                    potentialRepeatMatches = new ArrayList<SmartRawMatch>();

                int repeatsCount = 1;
                if (repeatsCntHM.containsKey(methodAc)) {
                    repeatsCount = repeatsCntHM.get(methodAc) + 1;
                }
                repeatsCntHM.put(methodAc, repeatsCount);
                /** Assumption: There are currently no signatures in SMART which are family members and have
                 * minRepeatsInSeq set; therefore we don't need to consider hits in potentialRepeatMatches for
                 * overlaps with other family members.
                 */
                potentialRepeatMatches.add(smartRawMatch);
            } else {
                /** the hit is a true positive (and could be a repeat which satisifies its repeat cutoff,if one exists,
                 * and doesn't have a minRepeatsInSeqS specified in THRESHOLDS file.
                 */
                if (methodAc.equals(SMART_TYR_KINASE_METHOD) || methodAc.equals(SMART_SER_THR_KINASE_METHOD)) {
                    // We need to apply the kinase hack at the sequence position match.seqStart
                    if (methodAc.equals(SMART_TYR_KINASE_METHOD)) {

                        if (tyrKinaseMatches == null)
                            tyrKinaseMatches = new HashMap<Integer, SmartRawMatch>();

                        tyrKinaseMatches.put(smartRawMatch.getLocationStart(), smartRawMatch); // First store the match

                        if (serThrKinaseMatches != null && serThrKinaseMatches.containsKey(smartRawMatch.getLocationStart())) {// There exists conflict => need to resolve it.
                            // apply regex hack to the match to (hopefully) eliminate the conflict
                            try {
                                smartKinaseHackRegex(smartRawMatch, serThrKinaseMatches, tyrKinaseMatches);
                            } catch (Exception e) {
                                LOGGER.debug("Protein sequence not found");
                            }
                        }
                    } else if (methodAc.equals(SMART_SER_THR_KINASE_METHOD)) {
                        if (serThrKinaseMatches == null)
                            serThrKinaseMatches = new HashMap<Integer, SmartRawMatch>();

                        serThrKinaseMatches.put(smartRawMatch.getLocationStart(), smartRawMatch); // First store the match

                        if (tyrKinaseMatches != null && tyrKinaseMatches.containsKey(smartRawMatch.getLocationStart())) {// There exists conflict => need to resolve it.
                            // apply regex hack to match
                            try {
                                smartKinaseHackRegex(smartRawMatch, serThrKinaseMatches, tyrKinaseMatches);
                            } catch (Exception e) {
                                LOGGER.debug("Protein sequence not found");
                            }
                        }
                    }
                } else if (familyMap.keySet().contains(methodAc)) {

                    // We're dealing with a multi-sibling family (other than the kinase one)
                    String fam = (String) familyMap.get(methodAc); // get family of methodAc in the current match

                    if (setEval2NPrec(singleHitEVal, getRequiredPrecForComparisonTo(domainCutoff)) > domainCutoff) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Rejecting sibling hit because " + setEval2NPrec(singleHitEVal, getRequiredPrecForComparisonTo(domainCutoff)) + " > " + domainCutoff);
                        }
                    } else {
                        try {
                            checkOverlapsWithSiblings(smartRawMatch, siblingsHits, smartOverlapMap, mergeMap, fam, smartThresholdMap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else { // a true positive hit which is neither a kinase nor in a multi-member family -> simply persist it
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Accepting hit with status 'T' as wholeSeqEVal = " + setEval2NPrec(wholeSeqEVal, getRequiredPrecForComparisonTo(domainCutoff)) + " < domainCutoff = " + domainCutoff);
                    }
                    String id = smartRawMatch.getSequenceIdentifier();
                    RawProtein<SmartRawMatch> p;
                    if (filteredMatches.containsKey(id)) {
                        p = filteredMatches.get(id);
                    } else {
                        p = new RawProtein<SmartRawMatch>(id);
                        filteredMatches.put(id, p);
                    }
                    p.addMatch(smartRawMatch);
                }
            }


        }
    }

    private void checkOverlapsWithSiblings(SmartRawMatch match,
                                           Map<String, List<SmartRawMatch>> siblingsHits,
                                           Map<String, SmartOverlappingFileParser.SmartOverlap> smartOverlapMap,
                                           Map<String, String> mergeMap,
                                           String family,
                                           Map<String, SmartThresholdFileParser.SmartThreshold> smartThresholdMap)
            throws Exception {

        boolean overlaps = false;
        // matchRejected used to break out of the main overlap processing loop. but only for SMART_SPLIT_RESOLUTION_TYPE;
        // the match is always rejected for SMART_MERGE_RESOLUTION_TYPE, but we have to assess it for overlap with
        // _all_ hits stored so far, so that _all_ hits which overlap with it can be turned into their respective 'merge'
        // methods
        boolean matchRejectedForSplit = false;
        // Spot _more than_ 10aa overlaps
        List<SmartRawMatch> siblingsMatchesForFam = siblingsHits.get(family);
        // We store all siblings hits which will be knocked out by the time we have
        // processed all overlaps for match, unless match itself is knocked out
        // by one of the existing hits (only used for SMART_SPLIT_RESOLUTION_TYPE)
        Set<SmartRawMatch> siblingsToBeRemovedForMatch = null;

        if (siblingsMatchesForFam != null) {
            for (SmartRawMatch smartRawMatch : siblingsMatchesForFam) {
                while (!matchRejectedForSplit) {
                    // First find out which hits starts earlier
                    int eE, lS, lE; // earlier hit start and end/ later hit start and end
                    if (match.getLocationStart() <= smartRawMatch.getLocationStart()) {
                        eE = match.getLocationEnd();
                        lS = smartRawMatch.getLocationStart();
                        lE = smartRawMatch.getLocationEnd();
                    } else {
                        eE = smartRawMatch.getLocationEnd();
                        lS = match.getLocationStart();
                        lE = match.getLocationEnd();
                    }

                    /**  Three cases:
                     *  eS_________________eE
                     *          lS_________________lE
                     *
                     *       eS________________________eE
                     *          lS_________________lE
                     *
                     *   eS_________eE
                     *                 lS_________________lE
                     * In all cases, overlapLength = min(eE, lE) - lS. Note that in the last case, overlapLength < 0.
                     */
                    int overlapLen = min(eE, lE) - lS + 1;

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Overlap checking for family: " + family + "; OverlapLen = " + overlapLen + " between: " + match.getModelId() + "and a previously stored sibling hit: " + smartRawMatch.getModelId());
                    }

                    if (overlapLen > SIBLINGS_OVERLAP_THRESHOLD) {
                        // If the current hit overlaps with the best hit so far for that sequence range within that family
                        overlaps = true;
                        // Getting ovlResType for just one method will do, as all methods in one family have the same ovlResType
                        String ovlResType = smartOverlapMap.get(match.getModelId()).getResolutionType();
                        if (ovlResType == null) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("smartPP: Overlap Resolution Type missing for " + match.getModelId());
                            }
                            throw new Exception("handled");
                        }
                        Integer bestHitSoFarForThisSeqRange_ResPriorityI = smartOverlapMap.get(smartRawMatch.getModelId()).getPriority();
                        Integer match_ResPriorityI = smartOverlapMap.get(match.getModelId()).getPriority();

                        if (bestHitSoFarForThisSeqRange_ResPriorityI != null && match_ResPriorityI != null) {
                            if (ovlResType.equals(SMART_SPLIT_TAG)) {
                                if (bestHitSoFarForThisSeqRange_ResPriorityI < match_ResPriorityI) {
                                    // The lesser priority, the better; 1 is the best
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("SPLIT: Rejecting hit (family: " + family + ") of priority: " + match_ResPriorityI + "; overlap resolution type: " + ovlResType + " (" +
                                                match.getModelId() + ") " + "as it overlaps by more than 10aa's with a sibling hit of higher priority: (" + bestHitSoFarForThisSeqRange_ResPriorityI +
                                                "; overlap resolution type: " + ovlResType + " (" + smartRawMatch.getModelId() + ") )");
                                    }

                                    siblingsToBeRemovedForMatch = null;
                                    matchRejectedForSplit = true; // this will break out of the main overlaps processing loop
                                } else {
                                    // The current hit is better - take it instead of bestHitSoFarForThisSeqRange
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("SPLIT: Marking for removal best hit so far (family: " + family + ") of priority: " + bestHitSoFarForThisSeqRange_ResPriorityI + "; overlap resolution type: " + ovlResType + " (" +
                                                smartRawMatch.getModelId() + ") " + "as it overlaps by more than 10aa's with a sibling hit of higher priority: (" + match_ResPriorityI +
                                                "; overlap resolution type: " + ovlResType + " (" + match.getModelId() + ") )");
                                    }
                                    if (siblingsToBeRemovedForMatch == null)
                                        siblingsToBeRemovedForMatch = new HashSet<SmartRawMatch>();
                                    siblingsToBeRemovedForMatch.add(smartRawMatch);
                                }

                            } else if (ovlResType.equals(SMART_MERGE_TAG)) {
                                /**
                                 * Since the raw HMMER output query returns results sorted by evalue in asc order,
                                 * and we already have a bestHitSoFarForThisSeqRange hit, it is bound to be better or
                                 * equal than match, so we reject match.
                                 */
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("MERGE: Rejecting hit (family: " + family + ") of priority: " + match_ResPriorityI + "; overlap resolution type: " + ovlResType + " (" +
                                            match.getModelId() + ") " + " as it overlaps by more than 10aa's with a sibling hit of better evalue: (" + bestHitSoFarForThisSeqRange_ResPriorityI +
                                            "; overlap resolution type: " + ovlResType + " (" + smartRawMatch.getModelId() + ") )");
                                }
                                /**
                                 * Note that now that we have established that the overlap existed, according to Consts.SMART_MERGE_RESOLUTION_TYPE,
                                 * we also need to replace methodAc in bestHitSoFarForThisSeqRange with mergeMethodAc =
                                 * methods2OvlMergeMethods.get(bestHitSoFarForThisSeqRange.methodAc)
                                 */
                                smartRawMatch.setModelId(mergeMap.get(smartThresholdMap.get(smartRawMatch.getModelId()).getFamilyName()));
                            }
                        } else {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Overlap Resolution Priority missing for " + bestHitSoFarForThisSeqRange_ResPriorityI == null ? smartRawMatch.getModelId() : match.getModelId());
                            }
                            throw new Exception("handled");
                        }
                    } // if (overlapLen > SIBLINGS_OVERLAP_THRESHOLD) { - end
                } // while (iter.hasNext() && ret) { - end
            }// if (siblingsMatchesForFam != null) { - end

            if (!overlaps) {
                // Add match to siblingsHits as it doesn't overlap with any hits in that family encountered so far
                List<SmartRawMatch> al = (siblingsHits.keySet().contains(family) ? siblingsHits.get(family) : new ArrayList<SmartRawMatch>());
                al.add(match);
                siblingsHits.put(family, al);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Adding best hit so far to siblingHits (family: " + family + ") as no others detected so far...");
                }
            } else if (siblingsToBeRemovedForMatch == null) {
                //  SMART_MERGE_RESOLUTION_TYPE or (SMART_SPLIT_RESOLUTION_TYPE and match was rejected by one of the existsing hits)
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Rejecting hit (family: " + family + "; ovlResType = " + smartOverlapMap.get(match.getModelId()).getResolutionType() + ") because there had been overlaps with already existing better hits");
                }
            } else if (siblingsToBeRemovedForMatch != null) {
                // SMART_SPLIT_RESOLUTION_TYPE - match won against all the overlapping hits in siblingsToBeRemovedForMatch
                // First remove all the current hits which overlap with the better hit in match
                for (SmartRawMatch s : siblingsToBeRemovedForMatch) {
                    siblingsMatchesForFam.remove(s);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("SPLIT: Rejecting hit (family: " + family + "; ovlResType = " + smartOverlapMap.get(s.getModelId()).getResolutionType() + ") because there are overlaps with the new (better) hit");
                    }
                }
                // Now add match to siblingsMatchesForFam
                siblingsMatchesForFam.add(match);
                siblingsHits.put(family, siblingsMatchesForFam);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("SPLIT: Adding hit (family: " + family + "; ovlResType = " + smartOverlapMap.get(match.getModelId()).getResolutionType() + ") because there had been overlaps with already existing (worse) hits");
                }

            } //  if (!overlaps) { - end
        } }// checkOverlapsWithSiblings() - end

    private int getRequiredPrecForComparisonTo(double val) {
        int ret;
        String valS = Double.toString(val);
        int eInd = valS.indexOf("E");
        int dotInd = valS.indexOf(".");
        if (eInd != -1) {
            /**
             * Expected formats: x.yEz  (e.g 1.7E10 i.e. 1.7E+10    => ret = z - 1
             *                   x.yE-z (e.g. 1.7E-23)              => ret = abs(-z-1)
             *                   x.y    (e.g. 8.8)                  => ret = 1
             *                   x      (e.g. 8)                    => ret = 0
             *
             *
             */
            ret = Math.abs(Integer.parseInt(valS.substring(eInd + 1)) - 1);
        } else {
            if (dotInd != -1) {
                ret = valS.substring(dotInd + 1).length();
            } else {
                ret = 0;
            }
        }
        return ret;
    }

    public double hmmerCalcEValue(float bitScore, String mu, String lambda, int dbSize) {
        double pVal = getPValue(bitScore, mu, lambda);
        double eVal = (double) dbSize * pVal;
        return eVal;
    }

    private double getPValue(float bitScore, String mu, String lambda) {
        double pVal, pVal2;

        // the bound from Bayes
        if (bitScore >= sreLOG2(Double.MAX_VALUE))
            pVal = 0.0;
        else if (bitScore <= -1.0 * sreLOG2(Double.MAX_VALUE))
            pVal = 1.0;
        else
            pVal = 1.0 / (1.0 + sreEXP2(bitScore));

        // try for a better estimate from EVD fit
        if (mu != null && lambda != null) {// If We have EVD values mu and lambda
            pVal2 = extremeValueP(bitScore, Double.parseDouble(mu), Double.parseDouble(lambda));
            if (pVal2 < pVal) pVal = pVal2;
        }
        return pVal;
    }

    private double sreLOG2(double x) {
        return ((x) > 0 ? Math.log(Double.MAX_VALUE) * 1.44269504 : -9999.0);
    }

    private double sreEXP2(double x) {
        return (Math.exp((x) * 0.69314718));
    }

    private int min(int a, int b) {
        if (a < b) return a;
        return b;
    }

    private double extremeValueP(float bitScore, double mu, double lambda) {
        double ret;
        // avoid exceptions near P=1.0
        // typical 32-bit sys: if () < -3.6, return 1.0
        if ((lambda * (bitScore - mu)) <= -1.0 * Math.log(-1.0 * Math.log(DBL_EPSILON)))
            return 1.0;

            // avoid underflow fp exceptions near P=0.0*/
        else if ((lambda * (bitScore - mu)) >= 2.3 * (double) DBL_MAX_10_EXP)
            return 0.0;

        // a roundoff issue arises; use 1 - e^-x --> x for small x */
        ret = Math.exp(-1.0 * lambda * (bitScore - mu));
        if (ret < 1e-7)
            return ret;
        else
            return (1.0 - Math.exp(-1.0 * ret));
    }

    private double setEval2NPrec(double eVal, int n) {
        NumberFormat nf = NumberFormat.getInstance(Locale.UK);
        nf.setMaximumFractionDigits(n);
        nf.setGroupingUsed(false);
        String eValS = nf.format(eVal);
        return Double.parseDouble(eValS);
    }

    private void smartKinaseHackRegex(SmartRawMatch match,
                                      Map<Integer, SmartRawMatch> serThrKinaseMatches,
                                      Map<Integer, SmartRawMatch> tyrKinaseMatches) throws Exception {

        ProteinDAO dao = new ProteinDAOImpl();
        Protein protein = dao.getProteinAndMatchesById(match.getNumericSequenceId());
        String  proteinSequence = protein.getSequence();
        boolean tyr = false;
        boolean serThr = false;

        if (proteinSequence != null) {
            if (proteinSequence.matches(SMART_TYR_REGEX)) {
                tyr = true;
            }
            if (proteinSequence.matches(SMART_SER_THR_REGEX)) {
                serThr = true;
            }

            if (tyr == true && serThr == false) {
                serThrKinaseMatches.remove(match.getLocationStart());
            } else if (tyr == false && serThr == true) {
                tyrKinaseMatches.remove(match.getLocationStart());
            } else if (tyr == false && serThr == false) { // remove both matches, irrespective of status
                serThrKinaseMatches.remove(match.getLocationStart());
                tyrKinaseMatches.remove(match.getLocationStart());
            } else if (tyr == true && serThr == true) {
                // return both hits for the curator to decide (they cannot both be true at the same sequence position,
                // but we can't tell the pattern at which position determines the correct classification of the protein)
            }
        } else {
            throw new Exception("handled");
        }
    }

    private void populateFamilyMap (Map<String, SmartThresholdFileParser.SmartThreshold> smartThresholdMap,
                                    Map<String, String> accessionMap,
                                    Map<String, String> familyMap) {
        Map<String, List<String>> tempFamilyMap = new HashMap<String, List<String>>();
        for (String s : smartThresholdMap.keySet()) {
            SmartThresholdFileParser.SmartThreshold smartThreshold = smartThresholdMap.get(s);
            String familyName = smartThreshold.getFamilyName();
            accessionMap.put(smartThreshold.getDomainName(), smartThreshold.getId());
            List<String> accessionList = new ArrayList<String>();
            if (tempFamilyMap.containsKey(familyName)) {
                accessionList =  tempFamilyMap.get(familyName);
            }
            accessionList.add(s);
            tempFamilyMap.put(familyName, accessionList);
        }
        for (String s : tempFamilyMap.keySet()) {
            List<String> tempAccList = tempFamilyMap.get(s);
            if(tempAccList.size() > 1 && !tempAccList.contains(SMART_SER_THR_KINASE_METHOD)) {
                for (String t : tempAccList) {
                    familyMap.put(t, s);
                }
            }
        }
    }

    private void populateMergeMap(Map<String, SmartOverlappingFileParser.SmartOverlap> smartOverlapMap,
                                  Map<String, String> accessionMap,
                                  Map<String, String> mergeMap) {
        Map<String, String> tempDomainAccMap = new HashMap<String, String>();
        for (String s : smartOverlapMap.keySet()) {
            SmartOverlappingFileParser.SmartOverlap overlap = smartOverlapMap.get(s);
            if (overlap.getPriority() == SMART_MAX_PRIORITY && overlap.getResolutionType().equals(SMART_MERGE_TAG)) {
                tempDomainAccMap.put(overlap.getFamilyName(), accessionMap.get(s));
            }
        }
        for (String s : tempDomainAccMap.keySet()) {
            for  (String t : smartOverlapMap.keySet()) {
                if (smartOverlapMap.get(t).getFamilyName().equals(s)) {
                    mergeMap.put(t,tempDomainAccMap.get(s));
                }
            }
        }
    }
}
