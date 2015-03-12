package uk.ac.ebi.interpro.scan.business.postprocessing.smart;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.smart.SmartOverlappingFileParser;
import uk.ac.ebi.interpro.scan.io.smart.SmartOverlaps;
import uk.ac.ebi.interpro.scan.io.smart.SmartThresholdFileParser;
import uk.ac.ebi.interpro.scan.io.smart.SmartThresholds;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SmartRawMatch;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class performs SMART post processing.  It is capable of running in two modes, depending upon
 * whether the licensed components of SMART are available or not.
 * <p/>
 * Smart post-processing requires 2 licensed files that need to be obtained from Smart for threshold and overlap data.
 * The "overlapping" and "THRESHOLDS" files are not included with an InterProScan by default, and if one or both are
 * not present then no filtering is performed (all raw matches become filtered matches).
 *
 * @author Craig McAnulla
 * @author Phil Jones
 */
public class SmartPostProcessing implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(SmartPostProcessing.class.getName());

    //Kinases are processed separately, and the Smart accessions for the relevant methods are stored here -> are they permanent?
    private static final String SMART_SER_THR_KINASE_METHOD = "SM00220";
    private static final String SMART_TYR_KINASE_METHOD = "SM00219";
    private static final List<String> KINASE_METHODS = Arrays.asList(SMART_TYR_KINASE_METHOD, SMART_SER_THR_KINASE_METHOD);
    private static final String SMART_SPLIT_TAG = "split";
    private static final Pattern SMART_TYR_REGEX = Pattern.compile(".*HRD[LIV][AR]\\w\\wN.*");
    private static final Pattern SMART_SER_THR_REGEX = Pattern.compile(".*D[LIVM]K\\w\\wN.*");
    private static final int SIBLINGS_OVERLAP_THRESHOLD = 10; //AAs

    // Properties
    private SmartThresholdFileParser thresholdFileParser;
    private SmartOverlappingFileParser overlappingFileParser;
    private SmartResourceManager smartResourceManager;
    private ProteinDAO proteinDAO;
    private SmartOverlaps smartOverlaps;
    private SmartThresholds smartThresholds;


    @Required
    public void setThresholdFileParser(SmartThresholdFileParser thresholdFileParser) {
        this.thresholdFileParser = thresholdFileParser;
    }

    @Required
    public void setOverlappingFileParser(SmartOverlappingFileParser overlappingFileParser) {
        this.overlappingFileParser = overlappingFileParser;
    }

    /**
     * Provides references to the resources for licensed mode:  Overlap and thresholds files.
     *
     * @param smartResourceManager
     */
    @Required
    public void setSmartResourceManager(SmartResourceManager smartResourceManager) {
        this.smartResourceManager = smartResourceManager;
    }

    /**
     * Used by the kinase hack - the protein sequence is required to run the regular
     * expressions for kinase confirmation.
     *
     * @param proteinDAO injected from Spring.
     */
    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    /**
     * Perform post processing.
     *
     * @param proteinIdToRawProteinMap Map of protein accessions to raw proteins to process
     * @return The filtered (processed) map of protein accessions to raw proteins
     * @throws IOException If a required file resource could not be accessed
     */
    public Map<String, RawProtein<SmartRawMatch>> process(Map<String, RawProtein<SmartRawMatch>> proteinIdToRawProteinMap) throws IOException {

        // Parse resources if they exist
        final Map<String, RawProtein<SmartRawMatch>> filteredMatches = new HashMap<String, RawProtein<SmartRawMatch>>();
        for (RawProtein<SmartRawMatch> protein : proteinIdToRawProteinMap.values()) {
            final RawProtein<SmartRawMatch> filteredProtein;
            if (smartResourceManager.isLicensed()) {
                filteredProtein = processProteinLicensed(protein);
            } else {
                filteredProtein = processProteinUnlicensed(protein);
            }
            filteredMatches.put(filteredProtein.getProteinIdentifier(), filteredProtein);
        }

        return filteredMatches;
    }

    /**
     * The SMART resources are available, so running in licensed mode.
     *
     * @param smartRawMatchRawProtein to filter
     * @return a RawProtein with filtered matches.
     * @throws IOException
     */
    private RawProtein<SmartRawMatch> processProteinLicensed(RawProtein<SmartRawMatch> smartRawMatchRawProtein) throws IOException {
        if (smartThresholds == null || smartOverlaps == null) {
            // Licensed resources are present, so parse them.
            smartThresholds = thresholdFileParser.parse(smartResourceManager.getThresholdFileResource());
            smartOverlaps = overlappingFileParser.parse(smartResourceManager.getOverlappingFileResource(), smartThresholds);
        }

        // Lots of filters...
        smartRawMatchRawProtein = filterByDerivedSequenceEvalue(smartRawMatchRawProtein);
        smartRawMatchRawProtein = filterByDerivedDomainEvalue(smartRawMatchRawProtein);
        smartRawMatchRawProtein = filterByRepeatCount(smartRawMatchRawProtein);
        smartRawMatchRawProtein = filterByFamilyOverlap(smartRawMatchRawProtein);


        smartRawMatchRawProtein = filterKinaseHack(smartRawMatchRawProtein);
        return smartRawMatchRawProtein;
    }

    /**
     * The HMMER sequence evalue just isn't good enough (apparently) - this method calculates a new one
     * based on values in the threshold file and checks matches against the cutoff.
     *
     * @param matchRawProtein to filter.
     * @return RawProtein to filter matches.
     */
    private RawProtein<SmartRawMatch> filterByDerivedSequenceEvalue(RawProtein<SmartRawMatch> matchRawProtein) {
        final RawProtein<SmartRawMatch> filtered = new RawProtein<SmartRawMatch>(matchRawProtein.getProteinIdentifier());
        for (SmartRawMatch match : matchRawProtein.getMatches()) {
            final SmartThresholdFileParser.SmartThreshold threshold = smartThresholds.getThresholdByModelId(match.getModelId());
            if (threshold.getDerivedEvalue(match.getScore()) < threshold.getCutoff()) {
                filtered.addMatch(match);
            }
        }
        return filtered;
    }

    /**
     * Another one where we need to calculate a shiny new evalue and then compare with
     * the threshold - thus filtering by domain evalue.
     *
     * @param matchRawProtein
     * @return
     */
    private RawProtein<SmartRawMatch> filterByDerivedDomainEvalue(RawProtein<SmartRawMatch> matchRawProtein) {
        final RawProtein<SmartRawMatch> filtered = new RawProtein<SmartRawMatch>(matchRawProtein.getProteinIdentifier());
        for (SmartRawMatch match : matchRawProtein.getMatches()) {
            final SmartThresholdFileParser.SmartThreshold threshold = smartThresholds.getThresholdByModelId(match.getModelId());
            final double derivedDomainEvalue = threshold.getDerivedEvalue(match.getLocationScore());
            if ((threshold.getRepeat_cut() == null || derivedDomainEvalue <= threshold.getRepeat_cut())
                    &&
                    (threshold.getFamily() == null || derivedDomainEvalue <= threshold.getCutoff())) {
                filtered.addMatch(match);
            }
        }
        return filtered;
    }

    /**
     * Some smart models have to match a minimum number of times.
     * This method filters for these models, checking against the value given in the thresholds file.
     *
     * @param matchRawProtein to be filtered.
     * @return
     */
    private RawProtein<SmartRawMatch> filterByRepeatCount(RawProtein<SmartRawMatch> matchRawProtein) {
        final RawProtein<SmartRawMatch> filtered = new RawProtein<SmartRawMatch>(matchRawProtein.getProteinIdentifier());
        final RawProtein<SmartRawMatch> considerCount = new RawProtein<SmartRawMatch>(matchRawProtein.getProteinIdentifier());
        Map<String, Integer> modelIdToMatchCount = new HashMap<String, Integer>();
        for (SmartRawMatch match : matchRawProtein.getMatches()) {
            final SmartThresholdFileParser.SmartThreshold threshold = smartThresholds.getThresholdByModelId(match.getModelId());
            if (threshold.getRepeats() == null) {
                filtered.addMatch(match);
            } else {
                considerCount.addMatch(match);
                if (modelIdToMatchCount.containsKey(match.getModelId())) {
                    modelIdToMatchCount.put(match.getModelId(), modelIdToMatchCount.get(match.getModelId()) + 1);
                } else {
                    modelIdToMatchCount.put(match.getModelId(), 1);
                }
            }
        }
        for (SmartRawMatch match : considerCount.getMatches()) {
            final SmartThresholdFileParser.SmartThreshold threshold = smartThresholds.getThresholdByModelId(match.getModelId());
            if (modelIdToMatchCount.get(match.getModelId()) >= threshold.getRepeats()) {
                filtered.addMatch(match);
            }
        }
        return filtered;
    }

    /**
     * Resolves overlaps between SMART models that are in the same family.
     * <p/>
     * Two different mechanisms are used - "SPLIT" keeps the match with the highest priority (lowest number)
     * and "MERGE" keeps the match with the best (lowest) evalue, and changes the matched modelID to the ID
     * of the model with the same name as the family.
     *
     * @param matchRawProtein to be filtered.
     * @return
     */
    private RawProtein<SmartRawMatch> filterByFamilyOverlap(RawProtein<SmartRawMatch> matchRawProtein) {
        final RawProtein<SmartRawMatch> filtered = new RawProtein<SmartRawMatch>(matchRawProtein.getProteinIdentifier());
        final RawProtein<SmartRawMatch> considerOverlap = new RawProtein<SmartRawMatch>(matchRawProtein.getProteinIdentifier());
        for (SmartRawMatch match : matchRawProtein.getMatches()) {
            final SmartOverlappingFileParser.SmartOverlap overlap = smartOverlaps.getSmartOverlapByModelId(match.getModelId());
            if (overlap == null) {
                filtered.addMatch(match);
            } else {
                considerOverlap.addMatch(match);
            }
        }
        final TreeSet<SmartRawMatch> orderedMatches = new TreeSet<SmartRawMatch>(considerOverlap.getMatches());
        final HashSet<SmartRawMatch> rejectedMatches = new HashSet<SmartRawMatch>();
        for (SmartRawMatch outer : orderedMatches) {
            for (SmartRawMatch inner : orderedMatches.tailSet(outer, false)) {
                if (rejectedMatches.contains(outer) || rejectedMatches.contains(inner)) {
                    continue;
                }
                final SmartOverlappingFileParser.SmartOverlap outerOverlap = smartOverlaps.getSmartOverlapByModelId(outer.getModelId());
                final SmartOverlappingFileParser.SmartOverlap innerOverlap = smartOverlaps.getSmartOverlapByModelId(inner.getModelId());

                if (!outerOverlap.getFamilyName().equals(innerOverlap.getFamilyName())) {
                    continue;
                }
                if (!matchesOverlap(outer, inner)) {
                    continue;
                }

                // This if statement is a sanity check only for an inconsistent overlaps file - NOT part of the algorithm.
                if (!outerOverlap.getResolutionType().equals(innerOverlap.getResolutionType())) {
                    throw new IllegalStateException("The SMART overlaps file contains two family members with different resolution types: Family: " + outerOverlap.getFamilyName() + ", domain " + outerOverlap.getDomainName() + " resolution: " + outerOverlap.getResolutionType() + ", domain " + innerOverlap.getDomainName() + " resolution: " + innerOverlap.getResolutionType());
                }
                // End of sanity check.
                if (SMART_SPLIT_TAG.equals(outerOverlap.getResolutionType())) {
                    // Another Sanity check - priorities for different domains must be different - NOT part of algorithm.
                    if (outerOverlap.getPriority() == innerOverlap.getPriority()) {
                        throw new IllegalStateException("The SMART overlaps file contains two family members with the same priority: Family: " + outerOverlap.getFamilyName() + ", domain " + outerOverlap.getDomainName() + " priority: " + outerOverlap.getPriority() + ", domain " + innerOverlap.getDomainName() + " priority: " + innerOverlap.getPriority());
                    }
                    // End of sanity check.
                    rejectedMatches.add(
                            (outerOverlap.getPriority() < innerOverlap.getPriority())
                                    ? inner
                                    : outer
                    );
                } else {
                    // Process the "MERGE" resolution type.
                    // The matches are ordered "best first", so the second ("inner") get thrown away.
                    rejectedMatches.add(inner);
                    // Then the winner gets renamed.
                    final SmartThresholdFileParser.SmartThreshold threshold = smartThresholds.getThresholdByDomainName(outerOverlap.getFamilyName());
                    outer.setModelId(threshold.getModelId());
                }
            }
        }

        orderedMatches.removeAll(rejectedMatches);
        filtered.addAllMatches(orderedMatches);
        return filtered;
    }

    /**
     * Do the two matches overlap by MORE THAN the value in  SIBLINGS_OVERLAP_THRESHOLD (10 last time I looked)
     *
     * @param one
     * @param two
     * @return true of false???
     */
    private boolean matchesOverlap(SmartRawMatch one, SmartRawMatch two) {
        int minEnd = (Math.min(one.getLocationEnd(), two.getLocationEnd()));
        int maxStart = (Math.max(one.getLocationStart(), two.getLocationStart()));
        int overlap = minEnd - maxStart + 1;

        return overlap > SIBLINGS_OVERLAP_THRESHOLD;
    }

    /**
     * For unlicensed use - additional parameters are added to the binary run (done in
     * a previous step) and the "filterKinaseHack" is applied to the matches - which
     * is all this filter does.
     *
     * @param matchRawProtein unfiltered matches
     * @return filtered matches.
     */
    private RawProtein<SmartRawMatch> processProteinUnlicensed(RawProtein<SmartRawMatch> matchRawProtein) {
        LOGGER.info("Smart licensed file resources are not available or readable - Smart post processing in unlicensed mode.");
        return filterKinaseHack(matchRawProtein);
    }


    /**
     * If both SerineThreonine Kinase and Tyrosine Kinase are matched, need
     * to check both kinds using the appropriate regular expressions (against
     * the protein sequence).
     *
     * @param matchRawProtein Unfiltered matches
     * @return filtered matches.
     */
    private RawProtein<SmartRawMatch> filterKinaseHack(RawProtein<SmartRawMatch> matchRawProtein) {
        boolean seenS = false;
        boolean seenT = false;
        for (SmartRawMatch match : matchRawProtein.getMatches()) {
            if (seenS && seenT) break; // No need to keep looking
            seenT |= SMART_TYR_KINASE_METHOD.equals(match.getModelId());
            seenS |= SMART_SER_THR_KINASE_METHOD.equals(match.getModelId());
        }
        if (!(seenS && seenT)) {
            return matchRawProtein;
        }

        // Have matches to both, so run regex etc.
        final Protein protein = proteinDAO.read(matchRawProtein.getProteinDatabaseId());
        if (protein == null) {
            throw new IllegalStateException("Cannot access the Protein via the protein primary key from the RawProtein object.");
        }
        final String sequence = protein.getSequence();
        final boolean tyrosineOK = SMART_TYR_REGEX.matcher(sequence).matches();
        final boolean serineThreonineOK = SMART_SER_THR_REGEX.matcher(sequence).matches();

        final RawProtein<SmartRawMatch> filtered = new RawProtein<SmartRawMatch>(matchRawProtein.getProteinIdentifier());
        for (SmartRawMatch match : matchRawProtein.getMatches()) {
            if (!KINASE_METHODS.contains(match.getModelId())) {
                filtered.addMatch(match);
            } else if (SMART_TYR_KINASE_METHOD.equals(match.getModelId()) && tyrosineOK) {
                filtered.addMatch(match);
            } else if (SMART_SER_THR_KINASE_METHOD.equals(match.getModelId()) && serineThreonineOK) {
                filtered.addMatch(match);
            }
        }
        return filtered;
    }
}
