package uk.ac.ebi.interpro.scan.web.model;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Phil Jones
 *         Date: 24/01/12
 *         Time: 15:38
 *         <p/>
 *         Comprises the supermatches that will be displayed together on a single line.
 */
public class CondensedLine implements Comparable<CondensedLine> {

    /**
     * zero indexed line number.
     */
    private int lineNumber;

    private Set<SimpleSuperMatch> superMatchList = new TreeSet<SimpleSuperMatch>();

    public CondensedLine(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public Set<SimpleSuperMatch> getSuperMatchList() {
        return superMatchList;
    }

    /**
     * Comparator to get the lines in the right order.
     *
     * @param other to compare with
     * @return negative number if this comes first, 0 if they are the same line number
     *         or positive number if other comes first.
     */
    public int compareTo(CondensedLine other) {
        if (this == other || this.equals(other)) {
            return 0;
        }
        return this.lineNumber - other.lineNumber;
    }

    /**
     * Tests to see if any of the matches that are tied together overlap with
     * matches already on the line.  If there is no overlap, then the supermatches
     * are added to the line.
     *
     * @param superMatchBucket
     * @return
     */
    public boolean addSuperMatchesWithoutOverlap(final SuperMatchBucket superMatchBucket) {
        for (SimpleSuperMatch candidate : superMatchBucket.getSupermatches()) {
            for (SimpleSuperMatch existingMatch : superMatchList) {
                if (candidate.matchesOverlap(existingMatch)) {
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

        if (lineNumber != that.lineNumber) return false;
        if (!superMatchList.equals(that.superMatchList)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = lineNumber;
        result = 31 * result + superMatchList.hashCode();
        return result;
    }
}
