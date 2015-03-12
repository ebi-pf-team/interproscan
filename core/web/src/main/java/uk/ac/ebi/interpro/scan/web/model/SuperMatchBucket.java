package uk.ac.ebi.interpro.scan.web.model;

import org.apache.log4j.Logger;

import java.io.Serializable;
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
public class SuperMatchBucket implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(SuperMatchBucket.class.getName());

    private final List<SimpleSuperMatch> supermatches = new ArrayList<SimpleSuperMatch>();

    private final EntryType type;

    public List<SimpleSuperMatch> getSupermatches() {
        return supermatches;
    }

    /**
     * Create a new bucket with its first SimpleSuperMatch.
     * Set the type of the bucket based upon this first SimpleSuperMatch.
     *
     * @param superMatch being the first SimpleSuperMatch in this bucket.
     */
    public SuperMatchBucket(final SimpleSuperMatch superMatch) {
        if (superMatch == null) {
            throw new IllegalArgumentException("A SuperMatchBucket cannot be instantiated with a null superMatch reference");
        }
        supermatches.add(superMatch);
        this.type = superMatch.getType();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Placing match to " + superMatch.getFirstEntry().getAc() + ", " + superMatch.getLocation().getStart() + " - " + superMatch.getLocation().getEnd() +
                    " into NEW bucket " + this.toString());
        }
    }

    /**
     * Return the type of this bucket, which is immutable to ensure matches
     * of only one type can be placed in the bucket.
     *
     * @return the type of the bucket (Domain, repeat etc.)
     */
    public EntryType getType() {
        return type;
    }

    /**
     * This method combines testing if a SimpleSuperMatch is in the same
     * hierarchy as other matches in this Bucket.  At the same time, it merges
     * superMatches together, calculating the widest bounds of the supermatch.
     *
     * @param candidate to test for addition and add if possible.
     * @return true if the candidate SimpleSuperMatch could be added to this bucket.
     */
    public boolean addIfSameHierarchyMergeIfOverlap(final SimpleSuperMatch candidate) {
        if (candidate == null || type != candidate.getType() || !candidate.inSameHierarchy(supermatches.get(0))) {
            return false;
        }
        SimpleSuperMatch mergedMatch = null;
        // Merge overlapping supermatches
        for (SimpleSuperMatch superMatch : supermatches) {
            if ((mergedMatch = superMatch.mergeIfOverlap(candidate)) != null) {
                break;
            }
        }
        if (mergedMatch == null) {  // Need to add the candidate separately.
            // Does this match overlap (absolutely) with any of the other SuperMatches?
            // If so, return false - not in this bucket.
//            for (SimpleSuperMatch superMatch : supermatches) {
//                if (candidate.matchesOverlap(superMatch, true)){
//                    return false;
//                }
//            }

            supermatches.add(candidate);
        } else {
            // Now need to compare all of the other supermatches with the newly merged one
            // and remove any overlaps.
            final List<SimpleSuperMatch> testedAndOK = new ArrayList<SimpleSuperMatch>();
            SimpleSuperMatch matchToRemove;
            do {
                matchToRemove = null;
                for (SimpleSuperMatch existingSupMatch : supermatches) {
                    if (existingSupMatch == mergedMatch || testedAndOK.contains(existingSupMatch)) {
                        // Note - object equality above *is* what we want, no need to use equals.
                        continue;
                    }
                    if (mergedMatch.mergeIfOverlap(existingSupMatch) != null) {
                        matchToRemove = existingSupMatch;
                        break;
                    } else {
                        testedAndOK.add(existingSupMatch);
                    }
                }
                if (matchToRemove != null) {
                    supermatches.remove(matchToRemove);
                }
            }
            while (matchToRemove != null);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Placing match to " + candidate.getFirstEntry().getAc() + ", " + candidate.getLocation().getStart() + " - " + candidate.getLocation().getEnd() +
                    " into bucket " + this.toString());
        }
        return true;
    }

    public List<SuperMatchBucket> ensureNoOverlaps() {
        List<SuperMatchBucket> newBuckets = new ArrayList<SuperMatchBucket>();
        int currentCount = 0;
        int previousCount = supermatches.size();
        while (currentCount < previousCount) {
            previousCount = currentCount;
            List<SimpleSuperMatch> toRemove = new ArrayList<SimpleSuperMatch>();
            for (int outerIndex = 0; outerIndex < supermatches.size(); outerIndex++) {
                SimpleSuperMatch smOne = supermatches.get(outerIndex);
                handleSmOne:
                for (int innerIndex = outerIndex + 1; innerIndex < supermatches.size(); innerIndex++) {
                    SimpleSuperMatch smTwo = supermatches.get(innerIndex);
                    if (smOne.matchesOverlap(smTwo, true)) {
                        toRemove.add(smOne);
                        // Found overlapping matches.
                        // Remove one.  See if it fits into an existing new bucket, or create a new bucket if not.
                        // TODO 23/07/14 Does this methods code work properly? newBuckets always empty here?!
                        for (SuperMatchBucket otherBucket : newBuckets) {
                            if (otherBucket.addIfNoExactOverlap(smOne)) {
                                continue handleSmOne;
                            }
                        }

                        // No suitable existing bucket, so create one
                        newBuckets.add(new SuperMatchBucket(smOne));
                    }
                }
            }
            this.supermatches.removeAll(toRemove);
            currentCount = supermatches.size();
        }
        return newBuckets;
    }

    private boolean addIfNoExactOverlap(SimpleSuperMatch candidate) {
        for (SimpleSuperMatch matchAlreadyInBucket : this.getSupermatches()) {
            if (candidate.matchesOverlap(matchAlreadyInBucket, true)) {
                return false;
            }
        }
        this.supermatches.add(candidate);
        return true;
    }
}
