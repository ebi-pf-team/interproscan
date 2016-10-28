package uk.ac.ebi.interpro.scan.precalc.berkeley.model;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.Set;
import java.util.TreeSet;

import static com.sleepycat.persist.model.Relationship.MANY_TO_ONE;


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
//@Persistent
@Entity
public class BerkeleySite {

    @PrimaryKey(sequence = "site_unique_index_sequence")
    private Long siteId;

    @SecondaryKey(relate = MANY_TO_ONE)
    private Long matchId;

    private Set<BerkeleySiteLocation> siteLocations;

    public BerkeleySite() {
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    // XmLElementWrapper generates a wrapper element around XML representation
    @XmlElementWrapper(name = "siteLocations")
    // XmlElement sets the name of the entities
    @XmlElement(name = "site-location")
    public Set<BerkeleySiteLocation> getSiteLocations() {
        return siteLocations;
    }

    public void setSiteLocations(Set<BerkeleySiteLocation> locations) {
        this.siteLocations = siteLocations;
    }

    public void addSiteLocation(BerkeleySiteLocation siteLocation) {
        if (this.siteLocations == null) {
            this.siteLocations = new TreeSet<BerkeleySiteLocation>();
        }
        siteLocations.add(siteLocation);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BerkeleySite that = (BerkeleySite) o;

        if (matchId != null ? !matchId.equals(that.matchId) : that.matchId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = matchId != null ? matchId.hashCode() : 0;
        result = 31 * result;
        return result;
    }

    @Override
    public String toString() {
        return "BerkeleySite{" +
                " matchId:" + matchId +
                ", numSites=" + getSiteLocations().size() +
                '}';
    }
//
//    /**
//     * Attempts to sort as follows:
//     * <p/>
//     * If equal (== or .equals) return 0.
//     * Sort on start position
//     * Sort on end position
//     * Sort on Residue
//     *
//     * @param that the object to be compared.
//     * @return a negative integer, zero, or a positive integer as this object
//     *         is less than, equal to, or greater than the specified object.
//     * @throws ClassCastException if the specified object's type prevents it
//     *                            from being compared to this object.
//     */
//    @Override
//    public int compareTo(BerkeleySite that) {
//        if (this == that || this.equals(that)) return 0;
//
//        if (this.getStart() != null && that.getStart() != null) {
//            if (this.getStart() < that.getStart()) return -1;
//            if (this.getStart() > that.getStart()) return 1;
//        }
//        if (this.getEnd() != null && that.getEnd() != null) {
//            if (this.getEnd() < that.getEnd()) return -1;
//            if (this.getEnd() > that.getEnd()) return 1;
//        }
//        if (this.getResidue() != null && that.getResidue() != null) {
//            if (this.getResidue().compareTo(that.getResidue()) < 0) return -1;
//            if (this.getResidue().compareTo(that.getResidue()) > 0) return 1;
//        }
//        throw new IllegalStateException("Trying to compare a BerkeleySite that has no state.  This: " + this + "\n\nThat: " + that);
//    }
}
