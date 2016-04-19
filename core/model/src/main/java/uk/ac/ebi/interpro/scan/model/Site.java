package uk.ac.ebi.interpro.scan.model;

import javax.persistence.*;
//import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.XmlAttribute;;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Gift Nuka
 *
 */
@Entity
//@XmlRootElement(name = "site")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@XmlType(name = "SiteType", propOrder = {"start", "end"})
public class Site implements Serializable {

    /**
     * Used as unique identifier of the record, e.g. for JPA persistence.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SITE_IDGEN")
    @TableGenerator(name = "SITE_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "site", initialValue = 0, allocationSize = 50)
    private Long id;

    @Column(name = "residue", nullable = false)
    private String residue;

    @Column(name = "site_loc_start", nullable = false)
    // to match site start - 'start' is reserved word in SQL.
    private int start;

    @Column(name = "site_loc_end", nullable = false)
    // 'end' is reserved word in SQL.
    private int end;

    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    private Location location;

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected Site() {
    }

    public Site(String residue, int start, int end) {
        setResidue(residue);
        setStart(start);
        setEnd(end);
    }

    /**
     * @return the persistence unique identifier for this object.
     */
    @XmlTransient
    public Long getId() {
        return null;
    }

    /**
     * @param id being the persistence unique identifier for this object.
     */
    private void setId(Long id) {
    }

    @XmlAttribute(required = true)
    public String getResidue() {
        return residue;
    }

    public void setResidue(String residue) {
        this.residue = residue;
    }

    /**
     * Returns the start coordinate of this Site.
     *
     * @return the start coordinate of this Site.
     */
    @XmlAttribute(required = true)
    public int getStart() {
        return start;
    }

    /**
     * Start coordinate of this Site.
     *
     * @param start Start coordinate of this Site
     */
    private void setStart(int start) {
        this.start = start;
    }

    /**
     * Returns the end coordinate of this Site.
     *
     * @return the end coordinate of this Site.
     */
    @XmlAttribute(required = true)
    public int getEnd() {
        return end;
    }

    /**
     * End coordinate of this Site.
     *
     * @param end End coordinate of this Site.
     */
    private void setEnd(int end) {
        this.end = end;
    }

    /**
     * This method is called by Location, upon the addition of a site to a location.
     *
     * @param location to which this site is related.
     */
    void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Returns the Location that this site is related to.
     *
     * @return
     */
    @XmlTransient
    public Location getLocation() {
        return location;
    }

        /**
     * Ensure sub-classes of AbstractSite are represented correctly in XML.
     *
     */
    @XmlTransient
    static final class SiteAdapter extends javax.xml.bind.annotation.adapters.XmlAdapter<SitesType, Set<? extends Site>> {

            /**
             * Map Java to XML type
             */
            @Override
            public SitesType marshal(Set<? extends Site> sites) {
                Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> rpsBlastSites = new LinkedHashSet<>();
                for (Site s : sites) {
                    if (s instanceof RPSBlastMatch.RPSBlastLocation.RPSBlastSite) {
                        rpsBlastSites.add((RPSBlastMatch.RPSBlastLocation.RPSBlastSite) s);
//                } else if (s instanceof Hmmer2Match.Hmmer2Location.CDDSite) {
//                    hmmer2Locations.add((Hmmer2Match.Hmmer2Location.CDDSite) s);
                    } else {
                        throw new IllegalArgumentException("Unrecognised Site class: " + s);
                    }
                }
                return new SitesType(rpsBlastSites);
            }

            /**
             * Map XML type to Java
             */
            @Override
            public Set<Site> unmarshal(SitesType sitesType) {
                Set<Site> sites = new LinkedHashSet<Site>();
                sites.addAll(sitesType.getRpsBlastSites());
                return sites;
            }

        }
        /**
         * Helper class for SiteAdapter
         */
        private final static class SitesType {

            @XmlElement(name = "rpsBlastSite-site")
            private final Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> rpsBlastSites;

            private SitesType() {
                rpsBlastSites = null;
            }

            public SitesType(Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> rpsBlastSites
                ) {
                this.rpsBlastSites = rpsBlastSites;
            }


            public Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> getRpsBlastSites() {
                return (rpsBlastSites == null ? Collections.<RPSBlastMatch.RPSBlastLocation.RPSBlastSite>emptySet() : rpsBlastSites);
            }
        }
}
