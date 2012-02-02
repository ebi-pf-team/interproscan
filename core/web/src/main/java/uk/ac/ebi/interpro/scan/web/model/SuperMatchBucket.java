package uk.ac.ebi.interpro.scan.web.model;

import org.apache.log4j.Logger;

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

    private static final Logger LOGGER = Logger.getLogger(SuperMatchBucket.class.getName());

    private final List<SimpleSuperMatch> supermatches = new ArrayList<SimpleSuperMatch>();

    private final String type;

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
    public String getType() {
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
    public boolean addIfSameHierarchy(final SimpleSuperMatch candidate) {
        if (candidate == null || !type.equals(candidate.getType()) || !candidate.inSameHierarchy(supermatches.get(0))) {
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
}
