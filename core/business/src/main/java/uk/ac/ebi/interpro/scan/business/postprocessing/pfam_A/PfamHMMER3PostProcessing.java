package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamClan;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamClanData;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.model.PfamModel;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

import java.util.*;
import java.io.Serializable;

/**
 * This class performs post processing of HMMER3 output for
 * Pfam.
 *
 * @author Phil Jones
 * @version $Id: PfamHMMER3PostProcessing.java,v 1.10 2009/11/09 13:35:50 craigm Exp $
 * @since 1.0
 */
public class PfamHMMER3PostProcessing implements Serializable {

    private static Logger LOGGER = Logger.getLogger(SeedAlignmentPersister.class);

    private PfamClanData clanData;

    private SeedAlignmentDataRetriever seedAlignmentDataRetriever;



    @Required
    public void setClanFileParser(ClanFileParser clanFileParser) throws Exception {
        this.clanData = clanFileParser.getClanData();
    }

    /**
     * TODO: Will eventually be 'required', but not till after milestone one.
     * @param seedAlignmentDataRetriever to retrieve seed alignment data for
     * a range of proteins.
     */
    public void setSeedAlignmentDataRetriever(SeedAlignmentDataRetriever seedAlignmentDataRetriever) {
        this.seedAlignmentDataRetriever = seedAlignmentDataRetriever;
    }


    /**
     * Post-processes raw results for Pfam HMMER3 in the batch requested.
     * @param proteinIdToRawMatchMap being a Map of protein IDs to a List of raw matches
     * @return a Map of proteinIds to a List of filtered matches.
     */
    public Map<String, List<PfamHmmer3RawMatch>> process(Map<String, List<PfamHmmer3RawMatch>> proteinIdToRawMatchMap) {
        Map<String, List<PfamHmmer3RawMatch>> proteinIdToFilteredMatchMap = new HashMap<String, List<PfamHmmer3RawMatch>>();
        final long startNanos = System.nanoTime();
        // Iterate over UniParc IDs in range and processBatch them
        SeedAlignmentDataRetriever.SeedAlignmentData seedAlignmentData = null;
        if (seedAlignmentDataRetriever != null){
            seedAlignmentData = seedAlignmentDataRetriever.retrieveSeedAlignmentData(proteinIdToRawMatchMap.keySet());
        }
        for (String proteinId : proteinIdToRawMatchMap.keySet()){
            List<SeedAlignment> seedAlignments = null;
            if (seedAlignmentData != null){
                seedAlignments = seedAlignmentData.getSeedAlignments(proteinId);
            }
            proteinIdToFilteredMatchMap.put (proteinId,  processProtein(proteinIdToRawMatchMap.get(proteinId), seedAlignments));
        }
        LOGGER.debug(new StringBuilder().append("Batch containing").append(proteinIdToRawMatchMap.size()).append(" proteins took ").append(((double) (System.nanoTime() - startNanos)) / 1.0e9d).append(" s to run.").toString());
        return proteinIdToFilteredMatchMap;
    }


    /**
     * Implementation of Rob Finn's algorithm for post processing, translated from Perl to Java.
     *
     * Also includes additional code to ensure seed alignments are included as matches, regardless of
     * score.
     * @param rawMatches being a List of the raw matches to filter
     * @param seedAlignments being a Collection of SeedAlignment objects, to check for matches to
     * methods where this protein was part of the seed alignment.
     * @return a List of filtered matches.
     */
     private List<PfamHmmer3RawMatch> processProtein(final List<PfamHmmer3RawMatch> rawMatches, final List<SeedAlignment> seedAlignments){
        List<PfamHmmer3RawMatch> filteredMatches = new ArrayList<PfamHmmer3RawMatch>();

        // First of all, place any rawMatches to methods for which this protein was a seed
        // into the filteredMatches collection.
        final Set<PfamHmmer3RawMatch> seedMatches = new HashSet<PfamHmmer3RawMatch>();

        if (seedAlignments != null) {        // TODO This check can be removed, once the seed alignment stuff has been sorted.
            for (final SeedAlignment seedAlignment : seedAlignments){
                for (final PfamHmmer3RawMatch candidateMatch : rawMatches){
                    if (! seedMatches.contains(candidateMatch)){
                        if (seedAlignment.getModelAccession().equals(candidateMatch.getModel())  &&
                                seedAlignment.getAlignmentStart() <= candidateMatch.getLocationStart() &&
                                seedAlignment.getAlignmentEnd() >= candidateMatch.getLocationEnd()){
                            // Found a match to a seed, where the coordinates fall within the seed alignment.
                            // Add it directly to the filtered rawMatches...
                            filteredMatches.add(candidateMatch);
                            seedMatches.add(candidateMatch);
                        }
                    }
                }
            }
        }

        // Then iterate over the non-seed raw rawMatches, sorted in order "best (lowest) evalue first"
        for (final PfamHmmer3RawMatch candidateMatch : rawMatches){
            if (! seedMatches.contains(candidateMatch)){
                final PfamClan candidateMatchClan = clanData.getClanByModelAccession(candidateMatch.getModel());

                boolean passes = true;   // Optimistic algorithm!

                if (candidateMatchClan != null){
                    // Iterate over the filtered rawMatches (so far) to check for passes
                    for (final PfamHmmer3RawMatch passedMatch : filteredMatches){
                        final PfamClan passedMatchClan = clanData.getClanByModelAccession(passedMatch.getModel());
                        // Are both the candidate and the passedMatch in the same clan?
                        if (candidateMatchClan.equals(passedMatchClan)){
                            // Both in the same clan, so check for overlap.  If they overlap
                            // and are NOT nested, then set passes to false and break out of the inner for loop.
                            if (matchesOverlap (candidateMatch, passedMatch)){
                                if (! matchesAreNested (candidateMatch, passedMatch)){
                                    passes = false;
                                    break;  // out of loop over filtered rawMatches.
                                }
                            }
                        }
                    }
                }

                if (passes){
                    // Add filtered match to collection
                    filteredMatches.add(candidateMatch);
                }
            }
        }
        return filteredMatches;
    }

    /**
     * Determines if two domains overlap.
     * @param one domain match one.
     * @param two domain match two.
     * @return true if the two domain matches overlap.
     */
    boolean matchesOverlap(PfamHmmer3RawMatch one, PfamHmmer3RawMatch two) {
        return  !
                ((one.getLocationStart() > two.getLocationEnd()) ||
                        (two.getLocationStart() > one.getLocationEnd()));
    }

    /**
     * Determines if two Pfam families are nested (in either direction)
     * @param one domain match one.
     * @param two domain match two.
     * @return true if the two domain matches are nested.
     */
    boolean matchesAreNested(PfamHmmer3RawMatch one, PfamHmmer3RawMatch two) {
        PfamModel oneModel = clanData.getModelByModelAccession(one.getModel());
        PfamModel twoModel = clanData.getModelByModelAccession(two.getModel());

        return !(oneModel == null || twoModel == null) &&
                (oneModel.isNestedIn(twoModel) || twoModel.isNestedIn(oneModel));

    }

}
