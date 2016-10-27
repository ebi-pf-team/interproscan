package uk.ac.ebi.interpro.scan.precalc.berkeley.model;

import com.sleepycat.persist.model.Persistent;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.Set;
import java.util.TreeSet;


/**
 * Very simple Site Location implementation for data transfer &
 * storage in BerkeleyDB.
 * <p/>
 * Holds all of the fields that may appear in any per Residue Site
 * implementation (from the main InterProScan 5 data model).
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Persistent
public class BerkeleySite implements Comparable<BerkeleySite> {

    private Integer numSites;

    private String residue;

    private Integer start;

    private Integer end;

    private String description;

    public BerkeleySite() {
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

    public String getResidue() {
        return residue;
    }

    public void setResidue(String residue) {
        this.residue = residue;
    }

    public Integer getNumSites() {
        return numSites;
    }

    public void setNumSites(Integer numSites) {
        this.numSites = numSites;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BerkeleySite that = (BerkeleySite) o;

        if (residue != null ? !residue.equals(that.residue) : that.residue != null) {
            return false;
        }
        if (start != null ? !start.equals(that.start) : that.start != null) return false;
        if (end != null ? !end.equals(that.end) : that.end != null) return false;
        if (numSites != null ? !end.equals(that.numSites) : that.numSites != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (end != null ? end.hashCode() : 0);
        result = 31 * result + (residue != null ? residue.hashCode() : 0);
        result = 31 * result + (numSites != null ? numSites.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BerkeleySite{" +
                "residue" + residue +
                ", start=" + start +
                ", end=" + end +
                ", numSites=" + numSites +
                ", description=" + description +
                '}';
    }

    /**
     * Attempts to sort as follows:
     * <p/>
     * If equal (== or .equals) return 0.
     * Sort on start position
     * Sort on end position
     * Sort on Residue
     *
     * @param that the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(BerkeleySite that) {
        if (this == that || this.equals(that)) return 0;

        if (this.getStart() != null && that.getStart() != null) {
            if (this.getStart() < that.getStart()) return -1;
            if (this.getStart() > that.getStart()) return 1;
        }
        if (this.getEnd() != null && that.getEnd() != null) {
            if (this.getEnd() < that.getEnd()) return -1;
            if (this.getEnd() > that.getEnd()) return 1;
        }
        if (this.getResidue() != null && that.getResidue() != null) {
            if (this.getResidue().compareTo(that.getResidue()) < 0) return -1;
            if (this.getResidue().compareTo(that.getResidue()) > 0) return 1;
        }
        throw new IllegalStateException("Trying to compare a BerkeleySite that has no state.  This: " + this + "\n\nThat: " + that);
    }
}
