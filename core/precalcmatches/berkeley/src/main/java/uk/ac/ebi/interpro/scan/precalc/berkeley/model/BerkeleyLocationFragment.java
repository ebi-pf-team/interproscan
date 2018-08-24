package uk.ac.ebi.interpro.scan.precalc.berkeley.model;

import com.sleepycat.persist.model.Persistent;


/**
 * Very simple Location implementation for data transfer &
 * storage in BerkeleyDB.
 * <p/>
 * Holds all of the fields that may appear in any Location
 * implementation (from the main InterProScan 5 data model).
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Persistent
public class BerkeleyLocationFragment implements Comparable<BerkeleyLocationFragment> {

    private Integer start;

    private Integer end;

    private String bounds;

    public BerkeleyLocationFragment() {
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public String getBounds() {
        return bounds;
    }

    public void setBounds(String bounds) {
        this.bounds = bounds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BerkeleyLocationFragment that = (BerkeleyLocationFragment) o;

        if (start != null ? !start.equals(that.start) : that.start != null) return false;
        if (end != null ? !end.equals(that.end) : that.end != null) return false;
        if (bounds != null ? !bounds.equals(that.bounds) : that.bounds != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (end != null ? end.hashCode() : 0);
        result = 31 * result + (bounds != null ? bounds.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BerkeleyLocationFragment{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }

    /**
     * Attempts to sort as follows:
     * <p/>
     * If equal (== or .equals) return 0.
     * Sort on start position
     * Sort on end position
     * Sort on envelope start
     * Sort on envelope end
     * Sort on HmmStart
     * Sort on HmmEnd
     * Sort on Score
     * Sort on Evalue
     *
     * @param that the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(BerkeleyLocationFragment that) {
        if (this == that || this.equals(that)) return 0;
        if (this.getStart() != null && that.getStart() != null) {
            if (this.getStart() < that.getStart()) return -1;
            if (this.getStart() > that.getStart()) return 1;
        }
        if (this.getEnd() != null && that.getEnd() != null) {
            if (this.getEnd() < that.getEnd()) return -1;
            if (this.getEnd() > that.getEnd()) return 1;
        }
        throw new IllegalStateException("Trying to compare a BerkeleyLocationFragment that has no state.  This: " + this + "\n\nThat: " + that);
    }
}
