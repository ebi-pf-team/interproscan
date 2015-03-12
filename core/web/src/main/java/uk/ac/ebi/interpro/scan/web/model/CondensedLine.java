package uk.ac.ebi.interpro.scan.web.model;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Phil Jones
 *         Date: 24/01/12
 *         Time: 15:38
 *         <p/>
 *         Comprises the supermatches that will be displayed together on a single line.
 */
public class CondensedLine implements Comparable<CondensedLine>, Serializable {

    /**
     * To ensure only features of the same type can be added.
     */
    private final EntryType type;

    private Set<SimpleSuperMatch> superMatchList = new TreeSet<SimpleSuperMatch>();

    public CondensedLine(SuperMatchBucket bucket) {
        this.type = bucket.getType();
        superMatchList.addAll(bucket.getSupermatches());
    }

    public Set<SimpleSuperMatch> getSuperMatchList() {
        return superMatchList;
    }

    public EntryType getType() {
        return type;
    }

    /**
     * Comparator to get the lines in the right order.
     * <p/>
     * Sort order:
     * <p/>
     * By type (domain, repeat)
     * <p/>
     * By first position first
     * <p/>
     * By most matches first
     * <p/>
     * By hashcode
     *
     * @param other to compare with
     * @return negative number if this comes first, 0 if they are the same line number
     *         or positive number if other comes first.
     */
    public int compareTo(CondensedLine other) {
        if (this == other || this.equals(other)) {
            return 0;
        }
        int comp = this.type.compareTo(other.type);

        if (comp == 0) {
            final Integer thisStart = this.getStart();
            final Integer otherStart = other.getStart();
            if (thisStart != null && otherStart != null) {
                comp = thisStart - otherStart;
            }
        }
        if (comp == 0) {
            comp = this.getSuperMatchList().size() - other.getSuperMatchList().size();
        }
        if (comp == 0) {
            comp = this.hashCode() - other.hashCode();
        }
        return comp;
    }

    private Integer getStart() {
        final SimpleSuperMatch firstMatch = superMatchList.iterator().next();
        if (firstMatch != null) {
            final SimpleLocation location = firstMatch.getLocation();
            if (location != null) {
                return location.getStart();
            }
        }
        return null;
    }

    /**
     * Tests to see if any of the matches that are tied together overlap with
     * matches already on the line.  If there is no overlap, then the supermatches
     * are added to the line.
     *
     * @param superMatchBucket
     * @return
     */
    public boolean addSuperMatchesSameTypeWithoutOverlap(final SuperMatchBucket superMatchBucket) {
        if (!this.type.equals(superMatchBucket.getType())) {
            return false;
        }
        for (SimpleSuperMatch candidate : superMatchBucket.getSupermatches()) {
            for (SimpleSuperMatch existingMatch : superMatchList) {
                if (candidate.matchesOverlap(existingMatch, true)) {
                    return false;
                }
            }
        }
        superMatchList.addAll(superMatchBucket.getSupermatches());
        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CondensedLine that = (CondensedLine) o;

        if (!superMatchList.equals(that.superMatchList)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return superMatchList.hashCode();
    }
}
