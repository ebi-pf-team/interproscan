package uk.ac.ebi.interpro.scan.business.postprocessing.prints;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.prints.FingerPRINTSHierarchyDBParser;
import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.persistence.PrintsFilteredMatchDAOImpl;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 *        Date: 29-Jun-2010
 *        Time: 16:35:40
 *        <p/>
 *        PRINTS Post Processing Algorithm:
 *        <p/>
 *        Matches to "Domain" PRINTS models can match outside hierarchies - don't apply hierarchy rule.  evalue filtering still pertains.
 *        <p/>
 *        Algorithm:
 *        <p/>
 *        1. Filter by evalue <= cutoff
 *        2. Filter by motif count >= min motif count
 *        3. Order by evalue descending
 *        4. if domain:
 *        pass
 *        5. else if has hierarchy:
 *        FIRST (for protein), store hierarchy members, this match passes
 *        NOT FIRST , if in currently stored hierarchy members list, PASS and replace hierarchy members list from this one.
 */
public class PrintsPostProcessing implements Serializable {

    private static final Logger LOGGER = LogManager.getLogger(PrintsPostProcessing.class.getName());

    private FingerPRINTSHierarchyDBParser hierarchyDBParser;

    private Resource fingerPRINTSHierarchyDB;

    private Map<String, FingerPRINTSHierarchyDBParser.HierachyDBEntry> printsModelData;

    private List<String> allPrintsModelIDs;

    private static final Object HIERCH_DB_LOCK = new Object();

    @Required
    public void setHierarchyDBParser(FingerPRINTSHierarchyDBParser hierarchyDBParser) {
        this.hierarchyDBParser = hierarchyDBParser;
    }

    @Required
    public void setFingerPRINTSHierarchyDB(Resource fingerPRINTSHierarchyDB) {
        this.fingerPRINTSHierarchyDB = fingerPRINTSHierarchyDB;
    }

    /**
     * Post-processes raw results for Pfam HMMER3 in the batch requested.
     *
     * @param proteinIdToRawMatchMap being a Map of protein IDs to a List of raw matches
     * @return a Map of proteinIds to a List of filtered matches.
     */
    public Map<String, RawProtein<PrintsRawMatch>> process(Map<String, RawProtein<PrintsRawMatch>> proteinIdToRawMatchMap) throws IOException {
        if (printsModelData == null) {
            synchronized (HIERCH_DB_LOCK) {
                if (printsModelData == null) {
                    if (hierarchyDBParser == null || fingerPRINTSHierarchyDB == null) {
                        throw new IllegalStateException("The PrintsPostProcessing class requires the injection of a FingerPRINTSHierarchyDBParser and a fingerPRINTSHierarchyDB resource.");
                    }
                    printsModelData = hierarchyDBParser.parse(fingerPRINTSHierarchyDB);
                    allPrintsModelIDs = new ArrayList<String>(printsModelData.size());
                    for (FingerPRINTSHierarchyDBParser.HierachyDBEntry entry : printsModelData.values()) {
                        allPrintsModelIDs.add(entry.getId());
                    }
                    allPrintsModelIDs = Collections.unmodifiableList(allPrintsModelIDs);
                }
            }
        }


        Map<String, RawProtein<PrintsRawMatch>> proteinIdToFilteredMatch = new HashMap<String, RawProtein<PrintsRawMatch>>();
        for (String proteinId : proteinIdToRawMatchMap.keySet()) {
            proteinIdToFilteredMatch.put(proteinId, processProtein(proteinIdToRawMatchMap.get(proteinId)));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Total of " + getMatchCountDEBUG(proteinIdToRawMatchMap) + " raw matches passed in and " + getMatchCountDEBUG(proteinIdToFilteredMatch) + " passed out.");
        }

        return proteinIdToFilteredMatch;
    }

    /**
     * This method is for debugging only - counts the number of matches in the Collection.
     *
     * @param proteinCollection
     * @return
     */
    private int getMatchCountDEBUG(Map<String, RawProtein<PrintsRawMatch>> proteinCollection) {
        int count = 0;
        for (RawProtein<PrintsRawMatch> protein : proteinCollection.values()) {
            if (protein != null && protein.getMatches() != null) {
                count += protein.getMatches().size();
            }
        }
        return count;
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
        final Set<PrintsRawMatch> sortedRawMatches = new TreeSet<PrintsRawMatch>(PrintsFilteredMatchDAOImpl.PRINTS_RAW_MATCH_COMPARATOR); // Gets the raw matches into the correct order for processing.
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


}
