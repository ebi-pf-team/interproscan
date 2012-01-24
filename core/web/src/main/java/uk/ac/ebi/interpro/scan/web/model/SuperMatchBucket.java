package uk.ac.ebi.interpro.scan.web.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Phil Jones
 *         Date: 24/01/12
 *         Time: 13:47
 */
public class SuperMatchBucket {

    private final List<SimpleSuperMatch> supermatches = new ArrayList<SimpleSuperMatch>();

    public List<SimpleSuperMatch> getSupermatches() {
        return supermatches;
    }

    public SuperMatchBucket(final SimpleSuperMatch superMatch) {
        if (superMatch == null) {
            throw new IllegalArgumentException("A SuperMatchBucket cannot be instantiated with a null superMatch reference");
        }
        supermatches.add(superMatch);
    }

    public boolean addIfSameHierarchy(final SimpleSuperMatch superMatch) {
        if (superMatch != null && superMatch.inSameHierarchy(supermatches.get(0))) {
            supermatches.add(superMatch);
            return true;
        }
        return false;
    }
}
