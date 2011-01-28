package uk.ac.ebi.interpro.scan.business.postprocessing.smart;

/**
* Created by IntelliJ IDEA.
* User: maslen
* Date: Sep 30, 2010
* Time: 12:01:07 PM
* To change this template use File | Settings | File Templates.
*/


import oracle.jdbc.driver.OracleResultSet;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.*;
import java.text.NumberFormat;



public final class SmartPP {

    protected static final int SIBLINGS_OVERLAP_THRESHOLD = 10; //AAs

    public static void smartPP(PrintWriter logOut,
                               Connection uniparcConn,
                               boolean testBatch,
                               int verbose) throws Exception {

        // Storage for evalue calculation data and cutoffs from SMART thresholds file
        Map<String, String> dbSize = new HashMap<String, String>();
        Map<String, String> mu = new HashMap<String, String>();
        Map<String, String> lambda = new HashMap<String, String>();
        Map<String, Double> domainCutoffs = new HashMap<String, Double>();   // Applies to the Evalue of the whole sequence agains SMART method (seqscore is used to calculate Evalue)
        // HashMap falsePosCutoffs = new HashMap(); // false positive cutoffs are no longer used
        Map<String, Double> familyCutoffs = new HashMap<String, Double>();   // family cutoffs are no longer used
        // Maps repeat method to the minimum number of repeats for that method
        Map<String, String> minSeqRepeats = new HashMap<String, String>();
        // E-value cutoffs compared to the single (repeat) hit E-value during post-processing
        Map<String, Double> repeatsCutoffs = new HashMap<String, Double>();
        // Currently not needed - see the definition of a repeat in the code below; HashSet repeatsMethodAcs = new HashSet();
        List<IPRScanMatch> potentialRepeatMatches = new ArrayList<IPRScanMatch>();
        Map<Integer, IPRScanMatch> serThrKinaseMatches = null;
        Map<Integer, IPRScanMatch> tyrKinaseMatches = null;

        // Contains methods which have family of size > 1 -> maps to family the method belongs to
        Map<String, String> methodsWithSiblings = new HashMap<String, String>();


        /** siblingsHits is refreshed for each Upi and, for a given family, contains the best sibling hit encountered
         * so far for a given sequence region. The ones which are left once we have finished with the sequence are the
         * ones that get persisted. Every time we encounter a hit to a family member, we go through all hits of that
         * family's members to the sequence. If we detect an overlap >= 10aa's between the current hit and one encountered
         * so far, then in the case of SMART_SPLIT_RESOLUTION_TYPE, we check if the current hit's methods' priority
         * in methods2OvlResolutionPriorities is greater than that of the already encountered hit's. If yes, then we
         * replace the old hit with the current one in siblingsHits; If no, the current hit is rejected and we stop
         * comparing it against any remaining hits of the same family.
         *
         */
        Map<String, List<IPRScanMatch>> siblingsHits = new HashMap<String, List<IPRScanMatch>>();

        /** Maps domain Names to method Acs; used in conjunction with the info from overlaps file to resolve overlaps
         * between family members
         */
        Map<String, String> domainName2Methods = new HashMap<String, String>();

        /** Maps methodAcs to the two overlap resolution types, via domainName2Methods and domain name -> resol. type
         * in overlaps file
         */
        Map<String, String> methods2OvlResolutionTypes = new HashMap<String, String>();

        /** Maps methodAcs to the two overlap resolution priorities, via domainName2Methods and domain name -> resol. type
         * in overlaps file
         */
        Map<String, Integer> methods2OvlResolutionPriorities = new HashMap<String, Integer>();


        /** Maps methodAcs to family methods into which the best of all overlapping hits
         * in a given family will be turned into.
         * Note in 'overlaps' fail, the methods underlined with '^^^'
         * are the family methods into which
         * the best hit from amongst their respective families will be merged:
         DEXDc        | DEXDc2       | merge |      2
         DEXDc        | DEXDc        | merge |      1    SM00487
         ^^^^^
         DEXDc        | DEXDc3       | merge |      3

         HELICc       | HELICc       | merge |      1    SM00490
         HELICc       | HELICc2      | merge |      2
         ^^^^^^
         HELICc       | HELICc3      | merge |      3

         RRM          | RRM_1        | merge |      2
         RRM          | RRM_2        | merge |      3
         RRM          | RRM          | merge |      1    SM00360
         ^^^
         VWC          | VWC          | merge |      1    SM00214
         ^^^
         VWC          | VWC_def      | merge |      3
         VWC          | VWC_out      | merge |      2
         */
        HashMap<String, String> methods2OvlMergeMethods = new HashMap<String, String>();

        /** The most stringent cutoff - all matches with evals below it are accepted as 'T' */
        double domainCutoff = Double.NaN;
        /** The least stringent cutoff - all matches with evals above it are rejected as false */

        /** All matches to SMART methods which are marked as having repeats in the THRESHOLDS file are scanned against this cutoff and
         * the matches with eval greater than repeatsCutoff are rejected; after that the overall number of repeat hits in a given sequence
         * is compared to minRepeatsInSeq - if it is less, all matches of that method against the sequences are rejected. */
        double repeatsCutoff = Double.MAX_VALUE;
        Double repeatsCutoffD = null;
        // For a given upi, maps repeats method that hits it to that method's minRepeats threshold
        // For a given upi, maps repeats method that hits it to the amount of times it hits that upi
        Map<String, Integer> repeatsCntHM = new HashMap<String, Integer>();
        String minRepeatsInSeqS = null;

        String maxCompletedUpi = Consts.NONEXISTENT_UPI;
        String lastPostProcessedUpi = Consts.NONEXISTENT_UPI;

        // match variables
        int analTypeId;
        String prevUpi = null;  // used for dealing with repeat matches
        String upi = null;
        String methodAc = null;
        int relNoMajor;
        int relNoMinor;
        int seqStart;
        int seqEnd;
        int hmmStart;
        int hmmEnd;
        String hmmBounds;
        float score; // domain bitscore
        float seqScore = Float.NaN;  // cumulative sequence bitscore
        double eValue;

          // Get the current release number for SMART
        String relNo = Utils.readCurrentRelNo(Analysis.SMART, uniparcConn);

        // First values from SMART thresholds file
        readThresholds(dbSize, mu, lambda, relNo,
                domainCutoffs, minSeqRepeats, repeatsCutoffs,
                methodsWithSiblings, familyCutoffs,
                domainName2Methods,
                logOut, Scheduler.ERROR_MAIL_SUBJECT, verbose);

        // Read which methods are repeats from SMART descriptions file
        // Not currently necessary; readDescriptions(repeatsMethodAcs, relNo, logOut, verbose);

        // Read resolution strategies and priorities for overlap resolution among family members
        readOverlaps(methods2OvlResolutionTypes,
                methods2OvlResolutionPriorities,
                methods2OvlMergeMethods,
                domainName2Methods,
                relNo,
                logOut, Scheduler.ERROR_MAIL_SUBJECT, verbose);

        // Get maximum upi for which raw SMART results are available
        maxCompletedUpi =
                Utils.getMaxUpiForAvailableResults(Analysis.SMART, uniparcConn, logOut, verbose);

        // Get last post-processed upi from onion.iprscan table
        lastPostProcessedUpi = Utils.getmaxPostProcessedUpi(uniparcConn, logOut, verbose, Analysis.SMART);

        // Now start post-processessing raw HMMER results.
        PreparedStatement getSmartRawResultsPS =
                uniparcConn.prepareStatement(Consts. GET_SMART_RAW_RESULTS_SQL(testBatch));

        getSmartRawResultsPS.setString(1, maxCompletedUpi);
        getSmartRawResultsPS.setString(2, lastPostProcessedUpi);
        ResultSet res = getSmartRawResultsPS.executeQuery();
        OracleResultSet result = (OracleResultSet) res;

        while (result.next()) {
            // Get match data
            analTypeId = result.getInt(1);
            upi = result.getString(2);
            methodAc = result.getString(3);
            relNoMajor = result.getInt(4);
            relNoMinor = result.getInt(5);
            seqStart = result.getInt(6);
            seqEnd = result.getInt(7);
            hmmStart = result.getInt(8);
            hmmEnd = result.getInt(9);
            hmmBounds = result.getString(10);
            score = result.getFloat(11);
            seqScore = result.getFloat(12);
            eValue = result.getDouble(13);

            // Create a match object
            IPRScanMatch match =
                    new IPRScanMatch(Analysis.getAnalysisByAnalysisTypeId(analTypeId), upi, methodAc, relNoMajor, relNoMinor, seqStart, seqEnd, hmmStart, hmmEnd, hmmBounds, score, seqScore, eValue, null, uniparcConn);

            if (upi != null) {
                if (prevUpi != null && !prevUpi.equals(upi)) {
                    if (verbose > Consts.VERBOSE) {
                        logOut.println("upi = " + upi + " != " + " prevUpi = " + prevUpi);
                        logOut.flush();
                    }
                    persistKinaseHackMatches(serThrKinaseMatches, tyrKinaseMatches,
                            mu, lambda, dbSize, methodAc, domainCutoff,
                            logOut, verbose);
                    serThrKinaseMatches = null;
                    tyrKinaseMatches = null;

                    persistWinningSiblingsHits(siblingsHits, logOut, verbose);
                    siblingsHits = new HashMap<String, List<IPRScanMatch>>(); // refresh siblings storage for the next upi

                    persistRepeatMatches(repeatsCntHM, minSeqRepeats, potentialRepeatMatches, logOut, verbose);
                    potentialRepeatMatches = null;
                    repeatsCntHM = new HashMap<String, Integer>();
                } // if (prevUpi != null) {
                prevUpi = upi;
            } // if (upi != null) {

            if (verbose > Consts.VERBOSE) {
                match.print(logOut);
                logOut.println(seqScore + " : " + (String) mu.get(methodAc) + " : " + (String) lambda.get(methodAc) + " : " + Integer.parseInt((String) dbSize.get(methodAc)) + " : " + Consts.hmmerCalcEValue(seqScore, (String) mu.get(methodAc), (String) lambda.get(methodAc), Integer.parseInt((String) dbSize.get(methodAc))));
                logOut.flush();
            }

            // Create the eValue for the match, according to Smart THRESHOLDS file
            double wholeSeqEVal =
                    Consts.hmmerCalcEValue(seqScore, (String) mu.get(methodAc), (String) lambda.get(methodAc), Integer.parseInt((String) dbSize.get(methodAc)));
            double singleHitEVal =
                    Consts.hmmerCalcEValue(score, (String) mu.get(methodAc), (String) lambda.get(methodAc), Integer.parseInt((String) dbSize.get(methodAc)));

            domainCutoff = ((Double) domainCutoffs.get(methodAc)).doubleValue();
            repeatsCutoffD = (Double) repeatsCutoffs.get(methodAc);
            minRepeatsInSeqS = (String) minSeqRepeats.get(methodAc);

            if (verbose > Consts.VERBOSE) {
                logOut.println("mu = " + (String) mu.get(methodAc));
                logOut.println("lambda = " + (String) lambda.get(methodAc));
                logOut.println("dbSize = " + Integer.parseInt((String) dbSize.get(methodAc)));
                logOut.println("wholeSeqEVal = " + wholeSeqEVal);
                logOut.println("singleHitEVal = " + singleHitEVal);
                logOut.println("domainCutoff = " + (Double) domainCutoffs.get(methodAc));
                logOut.println("repeatsCutoffD = " + repeatsCutoffD);
                logOut.println("min repeats in sequence = " + (String) minSeqRepeats.get(methodAc));
                logOut.flush();
            }

            if (repeatsCutoffD != null) {
                // N.B. We define repeats signatures for which either repeatsCutoffD and/or minRepeatsInSeqS != null holds;
                repeatsCutoff = repeatsCutoffD.doubleValue();
                if (verbose > Consts.VERBOSE) {
                    logOut.println("repeatsCutoffD = " + repeatsCutoffD);
                    logOut.println("repeatsCutoff = " + repeatsCutoff);
                    logOut.flush();
                }
            }

            if (setEval2NPrec(wholeSeqEVal, getRequiredPrecForComparisonTo(domainCutoff, logOut, verbose), logOut, verbose) >= domainCutoff) {
                // the hit is a false positive => reject it.

                if (verbose > Consts.VERBOSE) {
                    logOut.println("Rejecting a hit as wholeSeqEVal = " + setEval2NPrec(wholeSeqEVal, getRequiredPrecForComparisonTo(domainCutoff, logOut, verbose), logOut, verbose) + " >= domainCutoff = " + domainCutoff);
                    logOut.flush();
                }
            } else if (repeatsCutoffD != null &&
                    // The repeat hit is better than the domain cutoff - check if it's better than the repeatsCutoff
                    setEval2NPrec(singleHitEVal, getRequiredPrecForComparisonTo(repeatsCutoff, logOut, verbose), logOut, verbose) > repeatsCutoff) {

                if (verbose > Consts.VERBOSE) {
                    logOut.println("Rejecting hit as singleHitEVal = " + setEval2NPrec(singleHitEVal, getRequiredPrecForComparisonTo(repeatsCutoff, logOut, verbose), logOut, verbose) + " > repeatsCutoff = " + repeatsCutoff);
                    logOut.flush();
                }
            } else if (minRepeatsInSeqS != null) {
                // The repeat hit either doesn't have repeatsCutoffD set, or satisfies it;
                // now, if enough repeats are present (repeatsCnt >= minRepeatsInSeq), this hit will be accepted =>
                // store it in potentialRepeatMatches for now

                if (verbose > Consts.VERBOSE) {
                    logOut.println("Potentially (if enough repeats present) accepting hit as singleHitEVal = " + setEval2NPrec(singleHitEVal, getRequiredPrecForComparisonTo(repeatsCutoff, logOut, verbose), logOut, verbose) + " <= repeatsCutoff = " + repeatsCutoff);
                    logOut.flush();
                }
                if (potentialRepeatMatches == null)
                    potentialRepeatMatches = new ArrayList<IPRScanMatch>();

                int repeatsCntSoFar = 1;
                if (repeatsCntHM.containsKey(methodAc)) {
                    Integer repeatsCntSoFarI = (Integer) repeatsCntHM.get(methodAc);
                    repeatsCntSoFar = repeatsCntSoFarI.intValue() + 1;
                }
                repeatsCntHM.put(methodAc, repeatsCntSoFar);
                /** Assumption: There are currently no signatures in SMART which are family members and have
                 * minRepeatsInSeq set; therefore we don't need to consider hits in potentialRepeatMatches for
                 * overlaps with other family members.
                 */
                potentialRepeatMatches.add(match);
            } else {
                /** the hit is a true positive (and could be a repeat which satisifies its repeat cutoff,if one exists,
                 * and doesn't have a minRepeatsInSeqS specified in THRESHOLDS file.
                 */
                if (methodAc.equals(Consts.SMART_TYR_KINASE_METHOD) || methodAc.equals(Consts.SMART_SER_THR_KINASE_METHOD)) {
                    // We need to apply the kinase hack at the sequence position match.seqStart
                    if (methodAc.equals(Consts.SMART_TYR_KINASE_METHOD)) {

                        if (tyrKinaseMatches == null)
                            tyrKinaseMatches = new HashMap<Integer, IPRScanMatch>();

                        tyrKinaseMatches.put(match.seqStart, match); // First store the match

                        if (serThrKinaseMatches != null && serThrKinaseMatches.containsKey(match.seqStart)) {// There exists conflict => need to resolve it.
                            // apply regex hack to the match to (hopefully) eliminate the conflict
                            smartKinaseHackRegex(match, uniparcConn, serThrKinaseMatches, tyrKinaseMatches, logOut, verbose, Scheduler.ERROR_MAIL_SUBJECT);
                        }
                    } else if (methodAc.equals(Consts.SMART_SER_THR_KINASE_METHOD)) {
                        if (serThrKinaseMatches == null)
                            serThrKinaseMatches = new HashMap<Integer, IPRScanMatch>();

                        serThrKinaseMatches.put(match.seqStart, match); // First store the match

                        if (tyrKinaseMatches != null && tyrKinaseMatches.containsKey(match.seqStart)) {// There exists conflict => need to resolve it.
                            // apply regex hack to match
                            smartKinaseHackRegex(match, uniparcConn, serThrKinaseMatches, tyrKinaseMatches, logOut, verbose, Scheduler.ERROR_MAIL_SUBJECT);
                        }
                    } // if (methodAc.equals(Consts.SMART_TYR_KINASE_METHOD)) - end
                } else if (methodsWithSiblings.keySet().contains(methodAc)) {

                    // We're dealing with a multi-sibling family (other than the kinase one)
                    String fam = (String) methodsWithSiblings.get(methodAc); // get family of methodAc in the current match

                    if (setEval2NPrec(singleHitEVal, getRequiredPrecForComparisonTo(domainCutoff, logOut, verbose), logOut, verbose) > domainCutoff) {
                        if (verbose > Consts.VERBOSE) {
                            logOut.println("Rejecting sibling hit because " +
                                    setEval2NPrec(singleHitEVal, getRequiredPrecForComparisonTo(domainCutoff, logOut, verbose), logOut, verbose) + " > " + domainCutoff);
                            logOut.flush();
                        }
                    } else {
                        checkOverlapsWithSiblings(match, siblingsHits,
                                methods2OvlResolutionTypes, methods2OvlResolutionPriorities, methods2OvlMergeMethods,
                                Scheduler.ERROR_MAIL_SUBJECT,
                                logOut, verbose, fam);
                    }
                } else { // a true positive hit which is neither a kinase nor in a multi-member family -> simply persist it
                    if (verbose > Consts.VERBOSE) {
                        logOut.println("Accepting hit with status 'T' as wholeSeqEVal = " + setEval2NPrec(wholeSeqEVal, getRequiredPrecForComparisonTo(domainCutoff, logOut, verbose), logOut, verbose) + " < domainCutoff = " + domainCutoff);
                        logOut.flush();
                    }
                    match.persist(Consts.TRUE_STATUS, logOut, verbose);
                }
            } // // the hit is a true positive - end
        } // while (result.next()) {

        result.close();
        getSmartRawResultsPS.close();


        // Process the last upi and methodAc
        if (verbose >= Consts.VERBOSE) {
            logOut.println("About to persist any remaining repeat matches");
            logOut.flush();
        }
        persistRepeatMatches(repeatsCntHM, minSeqRepeats, potentialRepeatMatches, logOut, verbose);

        if (verbose >= Consts.VERBOSE) {
            logOut.println("About to persist any remaining kinase matches");
            logOut.flush();
        }
        persistKinaseHackMatches(serThrKinaseMatches, tyrKinaseMatches,
                mu, lambda, dbSize, methodAc, domainCutoff,
                logOut, verbose);

        if (verbose >= Consts.VERBOSE) {
            logOut.println("About to persist any remaining siblings matches");
            logOut.flush();
        }
        persistWinningSiblingsHits(siblingsHits, logOut, verbose);

        if (verbose >= Consts.VERBOSE) {
            logOut.println("About to commit");
            logOut.flush();
        }
        uniparcConn.commit();
        if (verbose >= Consts.VERBOSE) {
            logOut.println("Committed successfully");
            logOut.flush();
        }
    } // end of smartPP


    /**
     * Note a perverse case of overlaps (of over 10aa's in both cases):
     * match: e3
     * bestHitSoFarForThisSeqRange : e1 and e2
     * evals: e1 < e2 < e3
     * priority: e1 - 3; e2 - 1; e3 - 2
     * e1                  e2
     * |___________|  e3 |_____________|
     * .        |____________|
     * 1. 'merge' resolution strategy
     * a. e1 merged into e2 - because of overlap (e3 rejected)
     * b. e2 merged into itself (it _is_ the merge method) - because of overlap (e3 still rejected)
     * <p/>
     * 2. 'split' resolution strategy
     * a. e3 should replace e1 (its priority, 2, better than e1's: 3)
     * b. but then e3 itself is knocked out by e2 (e2's priority: 1 better than e3's: 2)
     * c. therefore e1 (along with e2) is staying after all, and e3 is rejected.
     */
    private static void checkOverlapsWithSiblings(IPRScanMatch match,
                                                  Map<String, List<IPRScanMatch>> siblingsHits,
                                                  Map<String, String> methods2OvlResolutionTypes,
                                                  Map<String, Integer> methods2OvlResolutionPriorities,
                                                  Map<String, String> methods2OvlMergeMethods,
                                                  String ERROR_MAIL_SUBJECT,
                                                  PrintWriter logOut, int verbose, String family)
            throws Exception {
        boolean overlaps = false;
        // matchRejected used to break out of the main overlap processing loop. but only for SMART_SPLIT_RESOLUTION_TYPE;
        // the match is always rejected for SMART_MERGE_RESOLUTION_TYPE, but we have to assess it for overlap with
        // _all_ hits stored so far, so that _all_ hits which overlap with it can be turned into their respective 'merge'
        // methods
        boolean matchRejectedForSplit = false;
        // Spot _more than_ 10aa overlaps
        List<IPRScanMatch> siblingsMatchesForFam = siblingsHits.get(family);
        // We store all siblings hits which will be knocked out by the time we have
        // processed all overlaps for match, unless match itself is knocked out
        // by one of the existing hits (only used for SMART_SPLIT_RESOLUTION_TYPE)
        Set<IPRScanMatch> siblingsToBeRemovedForMatch = null;

        if (siblingsMatchesForFam != null) {
            Iterator<IPRScanMatch> iter = siblingsMatchesForFam.iterator();
            while (iter.hasNext() && !matchRejectedForSplit) {
                IPRScanMatch bestHitSoFarForThisSeqRange = iter.next();
                // First find out which hits starts earlier
                int eE, lS, lE; // earlier hit start and end/ later hit start and end
                if (match.seqStart <= bestHitSoFarForThisSeqRange.seqStart) {
                    eE = match.seqEnd;
                    lS = bestHitSoFarForThisSeqRange.seqStart;
                    lE = bestHitSoFarForThisSeqRange.seqEnd;
                } else {
                    eE = bestHitSoFarForThisSeqRange.seqEnd;
                    lS = match.seqStart;
                    lE = match.seqEnd;
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
                int overlapLen = Consts.min(eE, lE) - lS + 1;

                if (verbose >= Consts.VERBOSE) {
                    logOut.println("Overlap checking for family: " + family + "; OverlapLen = " + overlapLen + " between: ");
                    match.print(logOut);
                    logOut.println("and a previously stored sibling hit: ");
                    bestHitSoFarForThisSeqRange.print(logOut);
                    logOut.flush();
                }

                if (overlapLen > SIBLINGS_OVERLAP_THRESHOLD) {
                    // If the current hit overlaps with the best hit so far for that sequence range within that family
                    overlaps = true;
                    // Getting ovlResType for just one method will do, as all methods in one family have the same ovlResType
                    String ovlResType = (String) methods2OvlResolutionTypes.get(match.methodAc);
                    if (ovlResType == null) {
                        Consts.reportError(ERROR_MAIL_SUBJECT,
                                "smartPP: Overlap Resolution Type missing for " + match.methodAc,
                                logOut);
                        throw Consts.EXCEPTION_ALREADY_HANDLED;
                    }
                    Integer bestHitSoFarForThisSeqRange_ResPriorityI =
                            (Integer) methods2OvlResolutionPriorities.get(bestHitSoFarForThisSeqRange.methodAc);
                    Integer match_ResPriorityI =
                            (Integer) methods2OvlResolutionPriorities.get(match.methodAc);

                    if (bestHitSoFarForThisSeqRange_ResPriorityI != null && match_ResPriorityI != null) {
                        if (ovlResType.equals(Consts.SMART_SPLIT_RESOLUTION_TYPE)) {
                            if (bestHitSoFarForThisSeqRange_ResPriorityI.intValue() < match_ResPriorityI.intValue()) {
                                // The lesser priority, the better; 1 is the best
                                if (verbose >= Consts.VERBOSE) {
                                    logOut.println("SPLIT: Rejecting hit (family: " + family + ") of priority: " +
                                            match_ResPriorityI.intValue() + "; overlap resolution type: " + ovlResType);
                                    match.print(logOut);
                                    logOut.println("...as it overlaps by more than 10aa's with a sibling hit of higher priority: (" +
                                            bestHitSoFarForThisSeqRange_ResPriorityI.intValue() +
                                            "; overlap resolution type: " + ovlResType + ")");
                                    bestHitSoFarForThisSeqRange.print(logOut);
                                    logOut.flush();
                                }
                                siblingsToBeRemovedForMatch = null;
                                matchRejectedForSplit = true; // this will break out of the main overlaps processing loop
                            } else {
                                // The current hit is better - take it instead of bestHitSoFarForThisSeqRange
                                if (verbose >= Consts.VERBOSE) {
                                    logOut.println("SPLIT: Marking for removal best hit so far (family: " + family + ") of priority: " +
                                            bestHitSoFarForThisSeqRange_ResPriorityI.intValue() + "; overlap resolution type: " + ovlResType);
                                    bestHitSoFarForThisSeqRange.print(logOut);
                                    logOut.println("... because it overlaps by more than 10aa's with a (new) sibling hit of higher priority: (" +
                                            match_ResPriorityI.intValue() +
                                            "; overlap resolution type: " + ovlResType + ")");
                                    match.print(logOut);
                                    logOut.flush();
                                }
                                if (siblingsToBeRemovedForMatch == null)
                                    siblingsToBeRemovedForMatch = new HashSet<IPRScanMatch>();
                                siblingsToBeRemovedForMatch.add(bestHitSoFarForThisSeqRange);
                            }

                        } else if (ovlResType.equals(Consts.SMART_MERGE_RESOLUTION_TYPE)) {
                            /**
                             * Since the raw HMMER output query returns results sorted by evalue in asc order,
                             * and we already have a bestHitSoFarForThisSeqRange hit, it is bound to be better or
                             * equal than match, so we reject match.
                             */
                            if (verbose >= Consts.VERBOSE) {
                                logOut.println("MERGE: Rejecting hit (family: " + family + ") of priority: " +
                                        match_ResPriorityI.intValue() + "; overlap resolution type: " + ovlResType);
                                match.print(logOut);
                                logOut.println("...as it overlaps by more than 10aa's with a sibling hit of better evalue: (" +
                                        bestHitSoFarForThisSeqRange_ResPriorityI.intValue() +
                                        "; overlap resolution type: " + ovlResType + ")");
                                bestHitSoFarForThisSeqRange.print(logOut);
                                logOut.flush();
                            }
                            /**
                             * Note that now that we have established that the overlap existed, according to Consts.SMART_MERGE_RESOLUTION_TYPE,
                             * we also need to replace methodAc in bestHitSoFarForThisSeqRange with mergeMethodAc =
                             * methods2OvlMergeMethods.get(bestHitSoFarForThisSeqRange.methodAc)
                             */
                            bestHitSoFarForThisSeqRange.methodAc = methods2OvlMergeMethods.get(bestHitSoFarForThisSeqRange.methodAc);
                        }
                    } else {
                        Consts.reportError(ERROR_MAIL_SUBJECT,
                                "smartPP: Overlap Resolution Priority missing for " +
                                (bestHitSoFarForThisSeqRange_ResPriorityI == null ? bestHitSoFarForThisSeqRange.methodAc : match.methodAc),
                                logOut);
                        throw Consts.EXCEPTION_ALREADY_HANDLED;
                    }
                } // if (overlapLen > SIBLINGS_OVERLAP_THRESHOLD) { - end
            } // while (iter.hasNext() && ret) { - end
        }// if (siblingsMatchesForFam != null) { - end

        if (!overlaps) {
            // Add match to siblingsHits as it doesn't overlap with any hits in that family encountered so far
            List<IPRScanMatch> al = (siblingsHits.keySet().contains(family) ? siblingsHits.get(family) : new ArrayList<IPRScanMatch>());
            al.add(match);
            siblingsHits.put(family, al);
            if (verbose >= Consts.VERBOSE) {
                logOut.println("Adding best hit so far to siblingHits (family: " + family + ")");
                match.print(logOut);
                logOut.println("... because no overlaps with other siblings have been detected.");
                logOut.flush();
            }
        } else if (siblingsToBeRemovedForMatch == null) {
            //  SMART_MERGE_RESOLUTION_TYPE or (SMART_SPLIT_RESOLUTION_TYPE and match was rejected by one of the existsing hits)
            if (verbose >= Consts.VERBOSE) {
                logOut.println("Rejecting hit (family: " + family + "; ovlResType = " + (String) methods2OvlResolutionTypes.get(match.methodAc) + "):");
                match.print(logOut);
                logOut.println("because there had been overlaps with already existing better hits ");
                logOut.flush();
            }
        } else if (siblingsToBeRemovedForMatch != null) {
            // SMART_SPLIT_RESOLUTION_TYPE - match won against all the overlapping hits in siblingsToBeRemovedForMatch
            Iterator<IPRScanMatch> iter = siblingsToBeRemovedForMatch.iterator();
            // First remove all the current hits which overlap with the better hit in match
            while (iter.hasNext()) {
                IPRScanMatch tbRemoved = iter.next();
                siblingsMatchesForFam.remove(tbRemoved);
                if (verbose >= Consts.VERBOSE) {
                    logOut.println("SPLIT: Rejecting hit (family: " + family + "; ovlResType = " + (String) methods2OvlResolutionTypes.get(tbRemoved.methodAc) + "):");
                    tbRemoved.print(logOut);
                    logOut.println("because there had been overlaps with the new (better) hit ");
                    logOut.flush();
                }
            } // while (iter.hasNext()) { - end
            // Now add match to siblingsMatchesForFam
            if (verbose >= Consts.VERBOSE) {
                logOut.println("SPLIT: Adding hit (family: " + family + "; ovlResType = " + (String) methods2OvlResolutionTypes.get(match.methodAc) + "):");
                match.print(logOut);
                logOut.println("because there had been overlaps with already existing (worse) hits ");
                logOut.flush();
            }
            siblingsMatchesForFam.add(match);
            siblingsHits.put(family, siblingsMatchesForFam);
        } //  if (!overlaps) { - end
    } // checkOverlapsWithSiblings() - end


    // Utility methods below:

    /**
     * Removes match from its respective serThrKinaseMatches or tyrKinaseMatches list if it doesn't matches the
     * relevant regex
     *
     * @param match               - Kinase match under consideration which conflicts with another (already encountered) kinase hit
     * @param uniparcConn
     * @param serThrKinaseMatches - Map key:match start_pos -> A serThr kinase hit
     * @param tyrKinaseMatches    - - Map key:match start_pos -> A tyr kinase hit
     * @param logOut
     * @param verbose
     * @param ERROR_MAIL_SUBJECT
     * @throws Exception
     */
    private static void smartKinaseHackRegex(IPRScanMatch match,
                                             Connection uniparcConn,
                                             Map<Integer, IPRScanMatch> serThrKinaseMatches,
                                             Map<Integer, IPRScanMatch> tyrKinaseMatches,
                                             PrintWriter logOut,
                                             int verbose,
                                             String ERROR_MAIL_SUBJECT) throws Exception {

        String seq = Consts.getUniParcSequence(match.upi, uniparcConn);

        if (verbose > Consts.VERBOSE) {
            logOut.println("Applying regex hack to sequence for upi: " + match.upi);
            logOut.flush();
        }

        boolean tyr = false;
        boolean serThr = false;

        if (seq != null) {
            if (seq.matches(Consts.SMART_TYR_REGEX)) {
                tyr = true;
            }
            if (seq.matches(Consts.SMART_SER_THR_REGEX)) {
                serThr = true;
            }

            if (tyr == true && serThr == false) {
                serThrKinaseMatches.remove(match.seqStart);
            } else if (tyr == false && serThr == true) {
                tyrKinaseMatches.remove(match.seqStart);
            } else if (tyr == false && serThr == false) { // remove both matches, irrespective of status
                serThrKinaseMatches.remove(match.seqStart);
                tyrKinaseMatches.remove(match.seqStart);
            } else if (tyr == true && serThr == true) {
                // return both hits for the curator to decide (they cannot both be true at the same sequence position,
                // but we can't tell the pattern at which position determines the correct classification of the protein)
            }
        } else {
            Consts.reportError(ERROR_MAIL_SUBJECT,
                    "smartPP: Failed to retrieve sequences for upi: " + match.upi,
                    logOut);
            throw Consts.EXCEPTION_ALREADY_HANDLED;
        }
    } // smartKinaseHackRegex() - end

    /**
     * @param siblingsHits - list of siblings hits which have passed through the overlap-removal procedure
     * @param logOut
     * @param verbose
     * @throws SQLException
     */
    private static void persistWinningSiblingsHits(Map<String, List<IPRScanMatch>> siblingsHits,
                                                   PrintWriter logOut,
                                                   int verbose)
            throws SQLException {
        Iterator<String> famIter = siblingsHits.keySet().iterator();

        while (famIter.hasNext()) {
            String curFam = famIter.next();
            List<IPRScanMatch> siblingsMatchesForFam = siblingsHits.get(curFam);

            if (siblingsMatchesForFam != null) {
                Iterator<IPRScanMatch> sbIter = siblingsMatchesForFam.iterator();
                while (sbIter.hasNext()) {
                    IPRScanMatch match = sbIter.next();

                    if (verbose >= Consts.VERBOSE) {
                        logOut.println("Accepting the best sibling match for family: " + curFam + ": ");
                        match.print(logOut);
                        logOut.flush();
                    }
                    match.persist(Consts.TRUE_STATUS, logOut, verbose);
                } // while (sbIter.hasNext()) { - end
            } // if (siblingsMatchesForFam != null) { - end
        } // while (famIter.hasNext()) { - end
    }

    /**
     * REad the SMART 'overlapping' file
     *
     * @param methods2OvlResolutionTypes : Map key: method_name -> value: resolution type
     * @param methods2OvlResolutionPriorities
     *                                   : Map key: method_name -> value: resolution priority
     * @param methods2OvlMergeMethods    Map key: method_name -> key: that method's corresponding merge method
     * @param domainName2Methods
     * @param relNo                     <Release major>.<release minor> identifier
     * @param logOut
     * @param ERROR_MAIL_SUBJECT
     * @param verbose
     * @throws Exception
     */
    public static void readOverlaps(Map<String, String> methods2OvlResolutionTypes,
                                    Map<String, Integer> methods2OvlResolutionPriorities,
                                    Map<String, String> methods2OvlMergeMethods,
                                    Map<String, String> domainName2Methods,
                                    String relNo,
                                    PrintWriter logOut,
                                    String ERROR_MAIL_SUBJECT,
                                    int verbose)
            throws Exception {


        String in = null;
        String methodAc = null;
        String tok;
        String curFamily = null;
        List<String> curFamilyMethods = new ArrayList<String>();
        String curMergeMethod = null;
        String curOvlResType = null;
        String prevFamily = null;
        boolean dataStarted = false;

        BufferedReader fReader =
                new BufferedReader(new FileReader(Consts.SMART_OVERLAPS_FILE(relNo)));

        /**
         * Example content:
         *
         name     |    domain    |  typ  | number
         --------------+--------------+-------+--------
         DEXDc        | DEXDc2       | merge |      2
         .              ^^^^^^         ^^^^^        ^
         DEXDc        | DEXDc        | merge |      1
         DEXDc        | DEXDc3       | merge |      3
         */

        while ((in = fReader.readLine()) != null) {
            if (in.startsWith("------")) {
                dataStarted = true;
            } else if (dataStarted) {
                String[] line = in.split("\\|");
                int len = line.length;
                int cnt = 0;
                while (cnt < len) {
                    tok = line[cnt].trim();
                    if (cnt == 0) {
                        curFamily = tok;
                        if (prevFamily != null && !curFamily.equals(prevFamily)) {
                            if (curOvlResType.equals(Consts.SMART_MERGE_RESOLUTION_TYPE)) {
                                // Store curMergeMethod for all methods in prevFamily
                                Iterator<String> iter = curFamilyMethods.iterator();
                                while (iter.hasNext()) {
                                    methods2OvlMergeMethods.put(iter.next(), curMergeMethod);
                                }
                            }
                            curMergeMethod = null;
                            curOvlResType = null;
                            curFamilyMethods = new ArrayList<String>();
                        }
                        prevFamily = curFamily;
                    } else if (cnt == 1) {
                        if (domainName2Methods.keySet().contains(tok)) {
                            methodAc = (String) domainName2Methods.get(tok);
                            /** Store in case this family is of SMART_MERGE_RESOLUTION_TYPE, and later the merge method
                             * for this method_ac needs to be stored.
                             */
                            curFamilyMethods.add(methodAc);
                        } else {
                            /** Report error, but not fail - in the current overlapping file the ones missing
                             * are either deleted domain names or family names - all with low (or for families, always
                             * the lowest) priority.
                             */
                            logOut.println("smartPP: Failed to retrieve methodAc for domainName: " + tok);
                            logOut.flush();
                            cnt++;
                            break; // skip to next line
                        }
                    } else if (cnt == 2) {
                        if (!tok.equals(Consts.SMART_SPLIT_RESOLUTION_TYPE) && !tok.equals(Consts.SMART_MERGE_RESOLUTION_TYPE)) {
                            Consts.reportError(ERROR_MAIL_SUBJECT,
                                    "smartPP: Unknown overlap resolution type encountered: " + tok,
                                    logOut);
                            throw Consts.EXCEPTION_ALREADY_HANDLED;
                        } else {
                            curOvlResType = tok;
                            methods2OvlResolutionTypes.put(methodAc, tok);
                            if (verbose > Consts.VERBOSE) {
                                logOut.println("Res type for : " + methodAc + " = " + tok);
                                logOut.flush();
                            }
                        }
                    } else if (cnt == 3) {
                        try {
                            Integer pr = Integer.parseInt(tok);
                            methods2OvlResolutionPriorities.put(methodAc, pr);
                            if (verbose > Consts.VERBOSE) {
                                logOut.println("Res priority for : " + methodAc + " = " + tok);
                                logOut.flush();
                            }
                            if (pr.intValue() == 1 && curOvlResType.equals(Consts.SMART_MERGE_RESOLUTION_TYPE))
                                curMergeMethod = methodAc;
                        } catch (NumberFormatException e) {
                            Consts.reportError(ERROR_MAIL_SUBJECT,
                                    "smartPP: Overlap priority not a number: " + tok,
                                    logOut);
                            throw Consts.EXCEPTION_ALREADY_HANDLED;
                        }
                    }
                    cnt++;
                } // while (cnt < len) { - end
            } // if (dataStarted) - end
        } //  while ((in = fReader.readLine()) != null) { - end

        if (verbose >= Consts.VERBOSE) {
            Iterator iter = methods2OvlMergeMethods.keySet().iterator();
            logOut.println("Merge methods: ");
            while (iter.hasNext()) {
                String mAc = (String) iter.next();
                logOut.println(mAc + ": " + (String) methods2OvlMergeMethods.get(mAc));
            }
            logOut.flush();
        }
    } // readOverlaps() - end

    /**
     * Read various cutoffs, needed for SMART post-processing, from the Smart THRESHOLDS file
     *
     * @param dbSize              - Smart has method-specific E-value cutoffs. In order to compare against them, the E-values of
     *                            raw HMMER hits need to be calculated using values from dbSize, mu and lambda in this file
     * @param mu                  - see above
     * @param lambda              - see above
     * @param relNo                     <Release major>.<release minor> identifier
     * @param domainCutoffs       - E-value cutoffs compared to the whole sequence E-value during post-processing
     * @param minSeqRepeats       - minimum number of repeats for the method in question
     * @param repeatsCutoffs      - E-value cutoffs compared to the single (repeat) hit E-value during post-processing
     * @param methodsWithSiblings - methods which have siblings (and therefore compete with each other if siblings hits
     *                            overlap)
     * @param familyCutoffs       - Not currently used in post-processing, but still included in the THRESHOLDS file
     * @param domainName2Methods  - a mapping between domain names (used in 'overlapping' file) and SMART method_acs
     * @param logOut
     * @param ERROR_MAIL_SUBJECT
     * @param verbose
     * @return
     * @throws Exception
     */
    public static double readThresholds(Map<String, String> dbSize,
                                        Map<String, String> mu,
                                        Map<String, String> lambda,
                                        String relNo,
                                        Map<String, Double> domainCutoffs,
                                        Map<String, String> minSeqRepeats,
                                        Map<String, Double> repeatsCutoffs,
                                        Map<String, String> methodsWithSiblings,
                                        Map<String, Double> familyCutoffs,
                                        Map<String, String> domainName2Methods,
                                        PrintWriter logOut,
                                        String ERROR_MAIL_SUBJECT,
                                        int verbose)
            throws Exception {

        double maxRepeatsCutOff = 0;
        String in = null;
        String methodAc = null;
        String tok;
        double eVal;
        HashMap families = new HashMap();  // Mapping family name -> array list of SMART method accessions in that family (see readThresholds)
        BufferedReader fReader =
                new BufferedReader(new FileReader(Consts.SMART_THRESHOLDS_FILE(relNo)));

        while ((in = fReader.readLine()) != null) {

            if (in.startsWith(HMMERCalc.SMART_METHOD_AC_PRE)) {
                String[] line = in.split("\\s+");
                int len = line.length;
                int cnt = 0;
/* Example entry:
      0            1              2            3            4          5             6          7         8          9           10
    Acc       Domain         Family      DB size           mu      lamda        cutoff    cut_low    family    repeats   repeat_cut
SM00028          TPR            TPR       263816   -24.964378   0.262049      1.00e+01   1.10e+01         -         3        100.00$
^^^^^^^       ^^^^^^        ^^^^^^^       ^^^^^^    ^^^^^^^^^   ^^^^^^^^      ^^^^^^^^   ^^^^^^^^         ^         ^        ^^^^^^
*/

                while (cnt < len) {
                    tok = line[cnt].trim();
                    if (cnt == 0) {
                        methodAc = tok;
                        if (verbose > Consts.VERBOSE)
                            logOut.print(methodAc + ": ");
                        if (methodAc == null) {
                            Consts.reportError(ERROR_MAIL_SUBJECT,
                                    "smartPP: method_ac missing in line: " + in,
                                    logOut);
                            throw Consts.EXCEPTION_ALREADY_HANDLED;
                        }
                    } else if (cnt == 1) {
                        if (verbose > Consts.VERBOSE)
                            logOut.print(tok + ": ");
                        domainName2Methods.put(tok, methodAc);
                    } else if (cnt == 2) {
                        if (verbose > Consts.VERBOSE)
                            logOut.print(tok + ": ");
                        ArrayList familyMembers = new ArrayList();
                        if (families.keySet().contains(tok)) {
                            familyMembers = (ArrayList) families.get(tok);
                        }
                        familyMembers.add(methodAc);
                        families.put(tok, familyMembers);
                    } else if (cnt == 3) {
                        if (verbose > Consts.VERBOSE)
                            logOut.print(tok + ": ");
                        dbSize.put(methodAc, tok);
                    } else if (cnt == 4) {
                        if (verbose > Consts.VERBOSE)
                            logOut.print(tok + ": ");
                        mu.put(methodAc, tok);
                    } else if (cnt == 5) {
                        if (verbose > Consts.VERBOSE)
                            logOut.print(tok + ": ");
                        lambda.put(methodAc, tok);
                    } else if (cnt == 6) {
                        if (verbose > Consts.VERBOSE)
                            logOut.print(tok + ": ");
                        eVal = Double.parseDouble(tok);
                        domainCutoffs.put(methodAc, eVal);
                        if (verbose > Consts.VERBOSE)
                            logOut.print(" (" + ((Double) domainCutoffs.get(methodAc)).doubleValue() + ") : ");
                    } else if (cnt == 7) {
                        if (verbose > Consts.VERBOSE)
                            logOut.print(tok + ": ");
                        // eVal = Double.parseDouble(tok);
                    } else if (cnt == 8) {
                        if (!tok.equals(Consts.SMART_THRESHOLDS_EMPTY_FIELD_MARKER)) {
                            if (verbose > Consts.VERBOSE)
                                logOut.print(tok + ": ");
                            eVal = Double.parseDouble(tok);
                            familyCutoffs.put(methodAc, eVal);
                        }
                    } else if (cnt == 9) {
                        if (!tok.equals(Consts.SMART_THRESHOLDS_EMPTY_FIELD_MARKER)) {
                            if (verbose > Consts.VERBOSE)
                                logOut.print(tok + ": ");
                            minSeqRepeats.put(methodAc, tok);
                        }
                    } else if (cnt == 10) {
                        if (!tok.equals(Consts.SMART_THRESHOLDS_EMPTY_FIELD_MARKER)) {
                            if (verbose > Consts.VERBOSE)
                                logOut.print(tok + ": ");
                            eVal = Double.parseDouble(tok);
                            repeatsCutoffs.put(methodAc, eVal);
                            if (eVal > maxRepeatsCutOff) {
                                maxRepeatsCutOff = eVal;
                            }
                        }
                    }
                    cnt++;
                } //  while (vsTok.hasMoreTokens()) {
                if (verbose > Consts.VERBOSE) {
                    logOut.println();
                    logOut.flush();
                }
            } // if (in.startsWith(HMMERCalc.SMART_METHOD_AC_PRE)) {
        } // while ((in = fReader.readLine()) != null) {

        // Now gather all methods which have families of size > 1 (i.e. containing methods other then themselves)
        Iterator iter = families.keySet().iterator();
        Iterator siblingsIter = null;
        while (iter.hasNext()) {
            String fam = (String) iter.next();
            ArrayList al = (ArrayList) families.get(fam);
            if (al.size() > 1 && !al.contains(Consts.SMART_SER_THR_KINASE_METHOD)) {
                // Exclude the Kinases family as they get their own mutual-exclusion treatment
                logOut.println("Multi-member family: " + fam + ": has the following members: ");
                // We're dealing with a multi-member family -> add all its members to  methodsWithSiblings
                siblingsIter = al.iterator();
                while (siblingsIter.hasNext()) {
                    String famMember = (String) siblingsIter.next();
                    methodsWithSiblings.put(famMember, fam);
                    logOut.println(famMember);
                }
            } // if (al.size() > 1) {
            logOut.flush();
        } // gather all methods which have families of size > 1  - end

        if (verbose > Consts.VERBOSE) {
            logOut.println("Completed reading thesholds file!");
            logOut.flush();
        }

        return maxRepeatsCutOff;
    } // readThresholds() - end


    /**
     * Persist matches from serThrKinaseMatches and tyrKinaseMatches if their wholeSeqEVal is better than domainCutoff
     *
     * @param serThrKinaseMatches
     * @param tyrKinaseMatches
     * @param mu
     * @param lambda
     * @param dbSize
     * @param methodAc
     * @param domainCutoff
     * @param logOut
     * @param verbose
     * @throws SQLException
     */
    private static void persistKinaseHackMatches(Map<Integer, IPRScanMatch> serThrKinaseMatches, Map<Integer, IPRScanMatch> tyrKinaseMatches,
                                                 Map<String, String> mu, Map<String, String> lambda, Map<String, String> dbSize, String methodAc,
                                                 double domainCutoff,
                                                 PrintWriter logOut, int verbose)
            throws SQLException {

        Iterator<Integer> iter = null;
        if (serThrKinaseMatches != null && serThrKinaseMatches.keySet() != null) {
            // But first persist any matches which are left-over after the kinase hack
            iter = serThrKinaseMatches.keySet().iterator();
            while (iter.hasNext()) {
                IPRScanMatch serThrMatch = serThrKinaseMatches.get(iter.next());
                double wholeSeqEVal =
                        Consts.hmmerCalcEValue(serThrMatch.seqScore, mu.get(methodAc), lambda.get(methodAc), Integer.parseInt(dbSize.get(methodAc)));

                if (verbose >= Consts.VERBOSE) {
                    logOut.println("Accepting kinase hit with status 'T' as wholeSeqEVal = " + wholeSeqEVal + " < domainCutoff = " + domainCutoff);
                    serThrMatch.print(logOut);
                }
                serThrMatch.persist(Consts.TRUE_STATUS, logOut, verbose);
            }
        } // if (serThrKinaseMatches != null) {

        if (tyrKinaseMatches != null && tyrKinaseMatches.keySet() != null) {
            iter = tyrKinaseMatches.keySet().iterator();
            while (iter.hasNext()) {
                IPRScanMatch tyrMatch = tyrKinaseMatches.get(iter.next());
                double wholeSeqEVal =
                        Consts.hmmerCalcEValue(tyrMatch.seqScore,
                                (String) mu.get(methodAc),
                                (String) lambda.get(methodAc),
                                Integer.parseInt(dbSize.get(methodAc)));

                if (verbose >= Consts.VERBOSE) {
                    logOut.println("Accepting kinase hit status 'T' as wholeSeqEVal = " + wholeSeqEVal + " < domainCutoff = " + domainCutoff);
                    tyrMatch.print(logOut);
                }
                tyrMatch.persist(Consts.TRUE_STATUS, logOut, verbose);
            }

            if (verbose >= Consts.VERBOSE) {
                logOut.flush();
            }

        } // if (tyrKinaseMatches != null) {
    } // PersistsKinaseHackMatches() - end

    /**
     * @param repeatsCntHM           - Map key: method -> value: number of that method's hits so far encountered on the current sequence
     * @param minSeqRepeats          - Map key:method -> value:minimum number of repeats for that method
     * @param potentialRepeatMatches - List of repeat matches under consideration
     * @param logOut
     * @param verbose
     * @throws SQLException
     */
    private static void persistRepeatMatches(Map<String, Integer> repeatsCntHM,
                                             Map<String, String> minSeqRepeats,
                                             List<IPRScanMatch> potentialRepeatMatches,
                                             PrintWriter logOut, int verbose)
            throws SQLException {
        int minRepeatsInSeq;
        int repeatsCnt;

        if (potentialRepeatMatches != null) {
            Iterator<IPRScanMatch> iter = potentialRepeatMatches.iterator();
            while (iter.hasNext()) {
                IPRScanMatch match = iter.next();
                String methodAc = match.methodAc;
                /**  Give it true status regardless of the individual repeats eValues; until we hear from Smart otherwise,
                 * the current thinking is that if there are enough repeats, we should accept them all if the eValue
                 * derived from the cumulative sequence score is less than the domain cutoff in the THRESHOLDS file.
                 * However, in all cases of repeats I observed, the eValue derived from the sequence score is much, much
                 * smaller than the (usually lenient in the case of repeat methods) domain cutoff.
                 * Hence in here we don't even bother to check that condition.
                 */
                minRepeatsInSeq = Integer.parseInt(minSeqRepeats.get(methodAc));
                repeatsCnt = ((Integer) repeatsCntHM.get(methodAc)).intValue();
                if (verbose > Consts.VERBOSE) {
                    logOut.println("repeatsCnt = " + repeatsCnt + "; minRepeatsInSeq = " + minRepeatsInSeq + " for: " + methodAc);
                    logOut.flush();
                }
                if (repeatsCnt > 0 && repeatsCnt >= minRepeatsInSeq) {
                    if (verbose >= Consts.VERBOSE) {
                        logOut.print("Accepting repeat hit with status 'T' as repeatsCnt: " + repeatsCnt + " > minRepeatsInSeq = " + minRepeatsInSeq + " : ");
                        match.print(logOut);
                    }
                    match.persist(Consts.TRUE_STATUS, logOut, verbose);
                }
            }
            logOut.flush();
        }
    } //persistRepeatMatches() - end


    /**
     * In order to compare our calculated E-values to the E-values in THRESHOLDS file, we need to adjust the precision
     * of our E-values to be able to compare them.
     *
     * @param eVal - original eValue whose precision needs to be adjusted to n
     * @param n
     * @param logOut
     * @param verbose
     * @return eVal at precision n
     */
    private static double setEval2NPrec(double eVal, int n, PrintWriter logOut, int verbose) {
        NumberFormat nf = NumberFormat.getInstance(Locale.UK);
        nf.setMaximumFractionDigits(n);
        String eValS = nf.format(eVal);
        // Locale.UK appears to add ',' between thousands, which in turn causes
        // Float.parseFloat( to throw NumberFormatException
        eValS = eValS.replaceAll(",", "");
        // This is only applicable to log10Eval
        // if (log10Eval == Float.NEGATIVE_INFINITY) {
        //    return log10Eval; // Otherwise NumberFormatException is being thrown by nf.format() call below
        //}
        if (verbose > Consts.VERBOSE) {
            logOut.println(" eVal " + eVal + " with prec n =  " + n + "  = " + Double.parseDouble(eValS));
            logOut.flush();
        }
        return Double.parseDouble(eValS);
    } // setEval2NPrec() - end

    /**
     *
     * @param val
     * @param logOut
     * @param verbose
     * @return the precision of val so that other double values could be compared to it, but only to the extent required
     *         by the precision of val (e.g. 0.00054 would be deemed equal to val = 0.0005, because the precision of val is
     *         4 decimal places)
     */
    private static int getRequiredPrecForComparisonTo(double val, PrintWriter logOut, int verbose) {
        int ret = Consts.NOT_FOUND;
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
        if (verbose > Consts.VERBOSE) {
            logOut.println("prec = " + ret + " for val = " + val);
            logOut.flush();
        }
        return ret;
    } // getRequiredPrecForComparisonTo() - end


    /**
     * This method is not currently used.
     * @param repeatsMethodAcs
     * @param relNo - the current release number for SMART
     * @param logOut
     * @param verbose
     * @throws Exception
     */
    public static void readDescriptions(HashSet repeatsMethodAcs,
                                        String relNo,
                                        PrintWriter logOut,
                                        int verbose)
            throws Exception {

        String in = null;
        String methodAc = null;
        BufferedReader fReader =
                new BufferedReader(new FileReader(Consts.SMART_DESCRIPTIONS_FILE(relNo)));

        if (verbose >= Consts.VERBOSE) {
            logOut.println("Found the following repeats signatures: ");
            logOut.flush();
        }

        while ((in = fReader.readLine()) != null) {

            if (in.startsWith(HMMERCalc.SMART_METHOD_AC_PRE)) {
                int firstSpInd = in.indexOf(" ");
                methodAc = in.substring(0, firstSpInd);
/* Example entry:
Accession    Name                        Profile   Description
....
SM00025      Pumilio                     Pumilio.HMM  Pumilio-like repeats
^^^^^^                     --> we search for the word 'repeat'     ^^^^^^
*/
                if (in.toLowerCase().indexOf(Consts.SMART_REPEATS_KW) != -1) {
                    repeatsMethodAcs.add(methodAc);
                    if (verbose >= Consts.VERBOSE) {
                        logOut.println(methodAc);
                        logOut.flush();
                    }
                }
            } // if (in.startsWith(HMMERCalc.SMART_METHOD_AC_PRE)) {
        } // while ((in = fReader.readLine()) != null) {

        if (verbose >= Consts.VERBOSE) {
            logOut.println("Completed reading descriptions file!");
            logOut.flush();
        }
    } // readDescriptions() - end
}
