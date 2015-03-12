package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamClan;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamClanData;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamModel;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * This class performs post processing of HMMER3 output for
 * Pfam.
 *
 * @author Phil Jones
 * @version $Id: PfamHMMER3PostProcessing.java,v 1.10 2009/11/09 13:35:50 craigm Exp $
 * @since 1.0
 */
public class PfamHMMER3PostProcessing implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(PfamHMMER3PostProcessing.class.getName());

    private PfamClanData clanData;

    private ClanFileParser clanFileParser;

    private SeedAlignmentDataRetriever seedAlignmentDataRetriever;

    @Required
    public void setClanFileParser(ClanFileParser clanFileParser) {
        this.clanFileParser = clanFileParser;
    }

    /**
     * TODO: Will eventually be 'required', but not till after milestone one.
     *
     * @param seedAlignmentDataRetriever to retrieve seed alignment data for
     *                                   a range of proteins.
     */
    public void setSeedAlignmentDataRetriever(SeedAlignmentDataRetriever seedAlignmentDataRetriever) {
        this.seedAlignmentDataRetriever = seedAlignmentDataRetriever;
    }


    /**
     * Post-processes raw results for Pfam HMMER3 in the batch requested.
     *
     * @param proteinIdToRawMatchMap being a Map of protein IDs to a List of raw matches
     * @return a Map of proteinIds to a List of filtered matches.
     */
    public Map<String, RawProtein<PfamHmmer3RawMatch>> process(Map<String, RawProtein<PfamHmmer3RawMatch>> proteinIdToRawMatchMap) throws IOException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Pfam A Post Processing: Number of proteins being considered: " + ((proteinIdToRawMatchMap == null) ? 0 : proteinIdToRawMatchMap.size()));
        }
        if (clanData == null) {
            clanData = clanFileParser.getClanData();
        }
        final Map<String, RawProtein<PfamHmmer3RawMatch>> proteinIdToRawProteinMap = new HashMap<String, RawProtein<PfamHmmer3RawMatch>>();
        if (proteinIdToRawMatchMap == null) {
            return proteinIdToRawProteinMap;
        }
        long startNanos = System.nanoTime();
        // Iterate over UniParc IDs in range and processBatch them
        SeedAlignmentDataRetriever.SeedAlignmentData seedAlignmentData = null;
        if (seedAlignmentDataRetriever != null) {
            seedAlignmentData = seedAlignmentDataRetriever.retrieveSeedAlignmentData(proteinIdToRawMatchMap.keySet());
        }

        for (String proteinId : proteinIdToRawMatchMap.keySet()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Pfam A post processing: processing protein " + proteinId);
            }
            List<SeedAlignment> seedAlignments = null;
            if (seedAlignmentData != null) {
                seedAlignments = seedAlignmentData.getSeedAlignments(proteinId);
            }
            proteinIdToRawProteinMap.put(proteinId, processProtein(proteinIdToRawMatchMap.get(proteinId), seedAlignments));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(new StringBuilder().append("Batch containing").append(proteinIdToRawMatchMap.size()).append(" proteins took ").append(((double) (System.nanoTime() - startNanos)) / 1.0e9d).append(" s to run.").toString());
        }
        return proteinIdToRawProteinMap;
    }

    /**
     * Implementation of Rob Finn's algorithm for post processing, translated from Perl to Java.
     * <p/>
     * Also includes additional code to ensure seed alignments are included as matches, regardless of
     * score.
     *
     * @param rawProteinUnfiltered being a List of the raw matches to filter
     * @param seedAlignments       being a Collection of SeedAlignment objects, to check for matches to
     *                             methods where this protein was part of the seed alignment.
     * @return a List of filtered matches.
     */
    private RawProtein processProtein(final RawProtein<PfamHmmer3RawMatch> rawProteinUnfiltered, final List<SeedAlignment> seedAlignments) {
        RawProtein<PfamHmmer3RawMatch> filteredMatches = new RawProtein<PfamHmmer3RawMatch>(rawProteinUnfiltered.getProteinIdentifier());

        // First of all, place any rawProteinUnfiltered to methods for which this protein was a seed
        // into the filteredMatches collection.
        final Set<PfamHmmer3RawMatch> seedMatches = new HashSet<PfamHmmer3RawMatch>();

        if (seedAlignments != null) {        // TODO This check can be removed, once the seed alignment stuff has been sorted.
            for (final SeedAlignment seedAlignment : seedAlignments) {
                for (final PfamHmmer3RawMatch candidateMatch : rawProteinUnfiltered.getMatches()) {
                    if (!seedMatches.contains(candidateMatch)) {
                        if (seedAlignment.getModelAccession().equals(candidateMatch.getModelId()) &&
                                seedAlignment.getAlignmentStart() <= candidateMatch.getLocationStart() &&
                                seedAlignment.getAlignmentEnd() >= candidateMatch.getLocationEnd()) {
                            // Found a match to a seed, where the coordinates fall within the seed alignment.
                            // Add it directly to the filtered rawProteinUnfiltered...
                            filteredMatches.addMatch(candidateMatch);
                            seedMatches.add(candidateMatch);
                        }
                    }
                }
            }
        }

        // Then iterate over the non-seed raw rawProteinUnfiltered, sorted in order ievalue ASC score DESC
        final Set<PfamHmmer3RawMatch> unfilteredByEvalue = new TreeSet<PfamHmmer3RawMatch>(rawProteinUnfiltered.getMatches());

        for (final RawMatch rawMatch : unfilteredByEvalue) {
            final PfamHmmer3RawMatch candidateMatch = (PfamHmmer3RawMatch) rawMatch;
            if (!seedMatches.contains(candidateMatch)) {
                final PfamClan candidateMatchClan = clanData.getClanByModelAccession(candidateMatch.getModelId());

                boolean passes = true;   // Optimistic algorithm!

                if (candidateMatchClan != null) {
                    // Iterate over the filtered rawProteinUnfiltered (so far) to check for passes
                    for (final PfamHmmer3RawMatch match : filteredMatches.getMatches()) {
                        final PfamClan passedMatchClan = clanData.getClanByModelAccession(match.getModelId());
                        // Are both the candidate and the passedMatch in the same clan?
                        if (candidateMatchClan.equals(passedMatchClan)) {
                            // Both in the same clan, so check for overlap.  If they overlap
                            // and are NOT nested, then set passes to false and break out of the inner for loop.
                            if (matchesOverlap(candidateMatch, match)) {
                                if (!matchesAreNested(candidateMatch, match)) {
                                    passes = false;
                                    break;  // out of loop over filtered rawProteinUnfiltered.
                                }
                            }
                        }
                    }
                }

                if (passes) {
                    // Add filtered match to collection
                    filteredMatches.addMatch(candidateMatch);
                }
            }
        }
        return filteredMatches;
    }

    /**
     * Determines if two domains overlap.
     *
     * @param one domain match one.
     * @param two domain match two.
     * @return true if the two domain matches overlap.
     */
    boolean matchesOverlap(PfamHmmer3RawMatch one, PfamHmmer3RawMatch two) {
        return !
                ((one.getLocationStart() > two.getLocationEnd()) ||
                        (two.getLocationStart() > one.getLocationEnd()));
    }

    /**
     * Determines if two Pfam families are nested (in either direction)
     *
     * @param one domain match one.
     * @param two domain match two.
     * @return true if the two domain matches are nested.
     */
    boolean matchesAreNested(PfamHmmer3RawMatch one, PfamHmmer3RawMatch two) {
        PfamModel oneModel = clanData.getModelByModelAccession(one.getModelId());
        PfamModel twoModel = clanData.getModelByModelAccession(two.getModelId());

        return !(oneModel == null || twoModel == null) &&
                (oneModel.isNestedIn(twoModel) || twoModel.isNestedIn(oneModel));

    }

}
