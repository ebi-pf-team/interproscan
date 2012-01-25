package uk.ac.ebi.interpro.scan.web.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Phil Jones
 *         Date: 24/01/12
 *         Time: 13:47
 *         <p/>
 *         Instances of this class are used to group together "supermatches"
 *         for the protein overview, where each supermatch is to an entry
 *         in the same hierarchy.  This is used to ensure non-overlapping matches in
 *         the same hierarchy appear on the same line.
 */
public class SuperMatchBucket {

    private final List<SimpleSuperMatch> supermatches = new ArrayList<SimpleSuperMatch>();

    public List<SimpleSuperMatch> getSupermatches() {
        return supermatches;
    }

    /**
     * Create a new bucket with its first SimpleSuperMatch
     *
     * @param superMatch being the first SimpleSuperMatch in this bucket.
     */
    public SuperMatchBucket(final SimpleSuperMatch superMatch) {
        if (superMatch == null) {
            throw new IllegalArgumentException("A SuperMatchBucket cannot be instantiated with a null superMatch reference");
        }
        supermatches.add(superMatch);
    }

    /**
     * This method combines testing if a SimpleSuperMatch is in the same
     * hierarchy as other matches in this Bucket.  At the same time, it merges
     * superMatches together, calculating the widest bounds of the supermatch.
     *
     * @param candidate
     * @return
     */
    public boolean addIfSameHierarchy(final SimpleSuperMatch candidate) {
        if (candidate != null && candidate.inSameHierarchy(supermatches.get(0))) {
            boolean merged = false;
            // Merge overlapping supermatches
            for (SimpleSuperMatch superMatch : supermatches) {
                if (candidate.matchesOverlap(superMatch)) {
                    // Merge this SimpleSuperMatch with the other.
                    superMatch.merge(candidate);
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                supermatches.add(candidate);
            }
            return true;
        }
        return false;
    }
}
