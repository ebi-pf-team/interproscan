package uk.ac.ebi.interpro.scan.business.postprocessing.panther;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.prints.FingerPRINTSHierarchyDBParser;
import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * PANTHER post-processing
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public class PantherPostProcessing implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(PantherPostProcessing.class.getName());


    
///*====================================================================*/
///* Procedure :  p_export_panther                                      */
///* Post-process Panther raw hits and insert them into iprscan table   */
///*====================================================================*/
//
//  PROCEDURE p_export_panther(analysis_type_id  IN   NUMBER) AS
//
//  max_completed_upi CHAR(13);
//  last_processed_upi CHAR(13);
//  -- Note: LOG(10, 1.00E-23) = -23
//  -- Note: LOG(10, 1.00E-3) = -3
//
//
//  BEGIN
//
//       max_completed_upi := f_analysis_complete(analysis_type_id);
//
//       EXECUTE IMMEDIATE 'SELECT max(upi) FROM onion.iprscan partition(panther) ipr, onion.iprscan_releases rel where ipr.analysis_type_id = rel.analysis_type_id and ipr.relno_major = rel.cur_relno_major and ipr.relno_minor = rel.cur_relno_minor' INTO last_processed_upi;
//
//       IF (last_processed_upi IS NULL) THEN
//               last_processed_upi := 'UPI0000000000';
//       END IF;
//
//       INSERT INTO onion.iprscan
//       SELECT rel.analysis_type_id, upi, fam || ':' || subfam, rel.cur_relno_major, rel.cur_relno_minor, seq_start, seq_end, null, null, null, score, null, evalue, 'T', sysdate
//       FROM onion.panther_analysis panther, onion.iprscan_releases rel
//       WHERE panther.relno_major = rel.cur_relno_major
//       AND panther.relno_minor = rel.cur_relno_minor
//       AND panther.analysis_type_id = rel.analysis_type_id
//       AND fam != subfam
//       AND upi < max_completed_upi
//       AND upi > last_processed_upi
// AND evalue <= -11
// ;
//
//       -- Now store superfamily hits for those families
//       INSERT INTO onion.iprscan
//       SELECT rel.analysis_type_id, upi, fam, rel.cur_relno_major, rel.cur_relno_minor, seq_start, seq_end, null, null, null, score, null, evalue, 'T', sysdate
//       FROM onion.panther_analysis panther, onion.iprscan_releases rel
//       WHERE panther.relno_major = rel.cur_relno_major
//       AND panther.relno_minor = rel.cur_relno_minor
//       AND panther.analysis_type_id = rel.analysis_type_id
//       AND fam != subfam
//       AND upi < max_completed_upi
//       AND upi > last_processed_upi
// AND evalue <= -11
// ;
//
//       -- Now store superfamilies, that don't have families
//       INSERT INTO onion.iprscan
//       SELECT rel.analysis_type_id, upi, fam, rel.cur_relno_major, rel.cur_relno_minor, seq_start, seq_end, null, null, null, score, null, evalue, 'T', sysdate
//       FROM onion.panther_analysis panther, onion.iprscan_releases rel
//       WHERE panther.relno_major = rel.cur_relno_major
//       AND panther.relno_minor = rel.cur_relno_minor
//       AND panther.analysis_type_id = rel.analysis_type_id
//       AND fam = subfam
//       AND upi < max_completed_upi
//       AND upi > last_processed_upi
// AND evalue <= -11
// ;
//
//       COMMIT;
//
//       -- p_analyze_estimate('onion.iprscan');
//  END;





    private Map<String, FingerPRINTSHierarchyDBParser.HierachyDBEntry> printsModelData;

    private List<String> allPrintsModelIDs;

    /**
     * Returns filtered matches.
     *
     * @param   rawMatches Raw matches, with protein ID as key
     * @return  Filtered matches.
     */
    public Map<String, RawProtein<PrintsRawMatch>> process(Map<String, RawProtein<PrintsRawMatch>> rawMatches) {
        Map<String, RawProtein<PrintsRawMatch>> filteredMatches = new HashMap<String, RawProtein<PrintsRawMatch>>();
        for (String proteinId : rawMatches.keySet()) {
            filteredMatches.put(proteinId, processProtein(rawMatches.get(proteinId)));
        }
        return filteredMatches;
    }

    /**
     * Algorithm:
     * <p/>
     * 1. Filter by evalue <= cutoff
     * 2. Filter by motif count >= min motif count
     * 3. Order by evalue descending
     * 4. if domain:
     * pass
     * 5. else if has hierarchy:
     * FIRST (for protein), store hierarchy members, this match passes
     * NOT FIRST , if in currently stored hierarchy members list, PASS and replace hierarchy members list from this one.
     *
     * @param rawProteinUnfiltered
     * @return
     */
    private RawProtein<PrintsRawMatch> processProtein(final RawProtein<PrintsRawMatch> rawProteinUnfiltered) {

        final RawProtein<PrintsRawMatch> filteredMatches = new RawProtein<PrintsRawMatch>(rawProteinUnfiltered.getProteinIdentifier());
        final Set<PrintsRawMatch> sortedRawMatches = new TreeSet<PrintsRawMatch>(PRINTS_RAW_MATCH_COMPARATOR); // Gets the raw matches into the correct order for processing.
        sortedRawMatches.addAll(rawProteinUnfiltered.getMatches());
        LOGGER.debug("New 'sortedRawMatches' set contains " + sortedRawMatches.size() + " matches.");
        String currentModelAccession = null;
        Set<PrintsRawMatch> motifMatchesForCurrentModel = new HashSet<PrintsRawMatch>();
        boolean currentMatchesPass = true;
        FingerPRINTSHierarchyDBParser.HierachyDBEntry currentHierachyDBEntry = null;
        final List<String> hierarchyModelIDLimitation = new ArrayList<String>(allPrintsModelIDs);

        for (PrintsRawMatch rawMatch : sortedRawMatches) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Iterating over sorted raw matches.  Currently looking at protein " + rawProteinUnfiltered.getProteinIdentifier() + " model " + rawMatch.getModelId());
            }
            if (currentModelAccession == null || !currentModelAccession.equals(rawMatch.getModelId())) {
                // Either just started, or got to the end of the matches for one model, so filter & reset.

                // Process matches
                if (currentMatchesPass && currentModelAccession != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("In processProtein method, calling filterModelMatches for protein " + rawProteinUnfiltered.getProteinIdentifier() + " and model " + currentModelAccession);
                    }
                    filteredMatches.addAllMatches(filterModelMatches(motifMatchesForCurrentModel, currentHierachyDBEntry, hierarchyModelIDLimitation));
                }

                // Reset
                currentMatchesPass = true;
                motifMatchesForCurrentModel.clear();
                currentModelAccession = rawMatch.getModelId();
                currentHierachyDBEntry = printsModelData.get(currentModelAccession);
                if (currentHierachyDBEntry == null) {
                    throw new IllegalStateException("There is no entry in the FingerPRINThierarchy.db file for model accession " + rawMatch.getModelId());
                }
            }
            // Fail any matches that do not hit the evalue cutoff - first filter..
            if (currentMatchesPass)
                currentMatchesPass = rawMatch.getEvalue() <= currentHierachyDBEntry.getEvalueCutoff();
            if (currentMatchesPass) motifMatchesForCurrentModel.add(rawMatch);
        }
        // Don't forget to process the last set of matches!
        if (currentMatchesPass) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("In processProtein method, calling filterModelMatches for protein " + rawProteinUnfiltered.getProteinIdentifier() + " and model " + currentModelAccession);
            }
            filteredMatches.addAllMatches(filterModelMatches(motifMatchesForCurrentModel, currentHierachyDBEntry, hierarchyModelIDLimitation));
        }

        return filteredMatches;
    }

    /**
     * For a single protein & PRINTS model, filters the set of motif matches according the PRINTS PP algorithm.
     *
     * @param motifMatchesForCurrentModel being the Set of motif match records for a single protein / PRINTS model.
     * @param hierachyDBEntry             Details of the FingerPRINTShierarchy.db record for the current model.
     * @param hierarchyModelIDLimitation  List of model IDs that limit passing matches.  If empty, there is no restriction.
     * @return an empty set if the raw matches fail the filter, or the set of raw matches if they pass.
     */
    private Set<PrintsRawMatch> filterModelMatches(final Set<PrintsRawMatch> motifMatchesForCurrentModel,
                                                   final FingerPRINTSHierarchyDBParser.HierachyDBEntry hierachyDBEntry,
                                                   final List<String> hierarchyModelIDLimitation) {

        // Belt and braces - if the matches passed in are null / empty, just pass back nothing.
        if (motifMatchesForCurrentModel == null || motifMatchesForCurrentModel.size() == 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No matches passed into filterModelMatches method - exiting.");
            }
            return Collections.emptySet();
        }

        // Second filter - number of matched motifs must exceed the minimum number.
        if (motifMatchesForCurrentModel.size() < hierachyDBEntry.getMinimumMotifCount()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Not enough motif matches: " + motifMatchesForCurrentModel.size() + " matched, but must be " + hierachyDBEntry.getMinimumMotifCount());
            }
            return Collections.emptySet();  // Failed filter.
        }

        // Third filter - pass if domain... (by definition, has no hierarchy to record).
        if (hierachyDBEntry.isDomain()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Match passes as is domain model.");
            }
            return motifMatchesForCurrentModel; // Passed filter, nothing else to do.
        }

        // Fourth filter - if there is a limitation from the previous model hierarchy, enforce
        if (!hierarchyModelIDLimitation.contains(hierachyDBEntry.getId())) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Match FAILED model hierarchy test, model ID " + hierachyDBEntry.getId());
            }
            return Collections.emptySet();       // Failed filter.
        }

        // Passed filter and may have its own hierarchy.  Check and set the current hierarchy limitation
        // if this is the case.
        if (hierachyDBEntry.getHierarchicalRelations().size() < hierarchyModelIDLimitation.size()) {
            hierarchyModelIDLimitation.clear();
            hierarchyModelIDLimitation.addAll(hierachyDBEntry.getHierarchicalRelations());
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Matches passed!");
        }
        return motifMatchesForCurrentModel;
    }


    private static final Comparator<PrintsRawMatch> PRINTS_RAW_MATCH_COMPARATOR = new Comparator<PrintsRawMatch>() {

        /**
         * This comparator is CRITICAL to the working of PRINTS post-processing, so it has been defined
         * here rather than being the 'natural ordering' of PrintsRawMatch objects so it is not
         * accidentally modified 'out of context'.
         *
         * Sorts the raw matches by:
         *
         * evalue (best first)
         * model accession
         * motif number (ascending)
         * location start
         * location end
         *
         * @param o1 the first PrintsRawMatch to be compared.
         * @param o2 the second PrintsRawMatch to be compared.
         * @return a negative integer, zero, or a positive integer as the
         *         first PrintsRawMatch is less than, equal to, or greater than the
         *         second PrintsRawMatch.
         */
        @Override
        public int compare(PrintsRawMatch o1, PrintsRawMatch o2) {
            int comparison = o1.getSequenceIdentifier().compareTo(o2.getSequenceIdentifier());
            if (comparison == 0) {
                if (o1.getEvalue() < o2.getEvalue()) comparison = -1;
                else if (o1.getEvalue() > o2.getEvalue()) comparison = 1;
            }
            if (comparison == 0) {
                comparison = o1.getModelId().compareTo(o2.getModelId());
            }
            if (comparison == 0) {
                if (o1.getMotifNumber() < o2.getMotifNumber()) comparison = -1;
                else if (o1.getMotifNumber() > o2.getMotifNumber()) comparison = 1;
            }
            if (comparison == 0) {
                if (o1.getLocationStart() < o2.getLocationStart()) comparison = -1;
                else if (o1.getLocationStart() > o2.getLocationStart()) comparison = 1;
            }
            if (comparison == 0) {
                if (o1.getLocationEnd() < o2.getLocationEnd()) comparison = -1;
                else if (o1.getLocationEnd() > o2.getLocationEnd()) comparison = 1;
            }
            return comparison;
        }
    };
}
