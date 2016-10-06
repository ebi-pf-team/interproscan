package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;;
import java.io.Serializable;
import java.util.*;

/**
 * @author Gift Nuka
 *
 */
@Entity
//@XmlRootElement(name = "site")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@XmlType(name = "SiteType")
public abstract class Site implements Serializable, Cloneable {

    /**
     * Used as unique identifier of the record, e.g. for JPA persistence.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SITE_IDGEN")
    @TableGenerator(name = "SITE_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "site", initialValue = 0, allocationSize = 50)
    private Long id;

    @Column(name = "description")
    private String description;


    @Column(name = "num_locations", nullable = false)
    private int numLocations;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, targetEntity = SiteLocation.class, mappedBy = "site")
//    @MapKey(name = "id")
    @BatchSize(size=4000)
    private Set<SiteLocation> siteLocations = new LinkedHashSet<>();

    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    private LocationWithSites location;

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected Site() {
    }

    public Site(String description, Set<SiteLocation> siteLocations) {
        setDescription(description);
        setNumLocations((siteLocations == null) ? 0 : siteLocations.size());
        setSiteLocations(siteLocations);
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
    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    @XmlAttribute(required = true)
    public int getNumLocations() {
        return numLocations;
    }

    private void setNumLocations(int numLocations) {
        this.numLocations = numLocations;
    }

    @Transient
    @XmlJavaTypeAdapter(SiteLocationAdapter.class)
    @XmlElement(name = "site-locations")
    public Set<SiteLocation> getSiteLocations() {
//        return Collections.unmodifiableSet(siteLocations);
        return siteLocations;
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection

    protected void setSiteLocations(final Set<SiteLocation> siteLocations) {
        if (siteLocations != null) {
            for (SiteLocation siteLocation : siteLocations) {
                siteLocation.setSite(this);
                this.siteLocations.add(siteLocation);
            }
        }
    }

    @Transient
    public void addSiteLocation(SiteLocation siteLocation) {
        siteLocation.setSite(this);
        this.siteLocations.add(siteLocation);
    }

    /**
     * This method is called by Match, upon the addition of a Location to a Match.
     *
     * @param location to which this Location is related.
     */
    void setLocation(LocationWithSites location) {
        this.location = location;
    }

    /**
     * Returns the location that this site is related to.
     *
     * @return
     */
    @XmlTransient
    public LocationWithSites getLocation() {
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
            if (sites == null) {
                return null;
            }

            Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> rpsBlastSites = new LinkedHashSet<>();
            Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site> hmmer3Sites = new LinkedHashSet<>();

            for (Site s : sites) {
                if (s instanceof RPSBlastMatch.RPSBlastLocation.RPSBlastSite) {
                    rpsBlastSites.add((RPSBlastMatch.RPSBlastLocation.RPSBlastSite) s);
                } else if (s instanceof Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site) {
                    hmmer3Sites.add((Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site) s);
                } else {
                    throw new IllegalArgumentException("Unrecognised Site class: " + s);
                }
            }
            return new SitesType(rpsBlastSites, hmmer3Sites);
        }

        /**
         * Map XML type to Java
         */
        @Override
        public Set<Site> unmarshal(SitesType sitesType) {
            Set<Site> sites = new LinkedHashSet<>();
            sites.addAll(sitesType.getRpsBlastSites());
            sites.addAll(sitesType.getHmmer3Sites());
            return sites;
        }

    }
    /**
     * Helper class for SiteAdapter
     */
    @XmlType(name = "sitesType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
    private final static class SitesType {

        @XmlElement(name = "rpsblast-site")
        private final Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> rpsBlastSites;

        @XmlElement(name = "hmmer3-site")
        private final Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site> hmmer3Sites;

        private SitesType() {
            rpsBlastSites = null;
            hmmer3Sites = null;
        }

        public SitesType(Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> rpsBlastSites,
                         Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site> hmmer3Sites) {
            this.rpsBlastSites = rpsBlastSites;
            this.hmmer3Sites = hmmer3Sites;
        }


        public Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> getRpsBlastSites() {
            return (rpsBlastSites == null ? Collections.<RPSBlastMatch.RPSBlastLocation.RPSBlastSite>emptySet() : rpsBlastSites);
        }

        public Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site> getHmmer3Sites() {
            return (hmmer3Sites == null ? Collections.<Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site>emptySet() : hmmer3Sites);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Site))
            return false;
        final Site h = (Site) o;
        return new EqualsBuilder()
                .append(description, h.description)
                .append(numLocations, h.numLocations)
                .append(siteLocations, h.siteLocations)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 57)
                .append(description)
                .append(numLocations)
                .append(siteLocations)
                .toHashCode();
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }


    /**
     * Creates and returns a copy of this object.  The precise meaning
     * of "copy" may depend on the class of the object. The general
     * intent is that, for any object <tt>x</tt>, the expression:
     * <blockquote>
     * <pre>
     * x.clone() != x</pre></blockquote>
     * will be true, and that the expression:
     * <blockquote>
     * <pre>
     * x.clone().getClass() == x.getClass()</pre></blockquote>
     * will be <tt>true</tt>, but these are not absolute requirements.
     * While it is typically the case that:
     * <blockquote>
     * <pre>
     * x.clone().equals(x)</pre></blockquote>
     * will be <tt>true</tt>, this is not an absolute requirement.
     *
     * By convention, the returned object should be obtained by calling
     * <tt>super.clone</tt>.  If a class and all of its superclasses (except
     * <tt>Object</tt>) obey this convention, it will be the case that
     * <tt>x.clone().getClass() == x.getClass()</tt>.
     *
     * By convention, the object returned by this method should be independent
     * of this object (which is being cloned).  To achieve this independence,
     * it may be necessary to modify one or more fields of the object returned
     * by <tt>super.clone</tt> before returning it.  Typically, this means
     * copying any mutable objects that comprise the internal "deep structure"
     * of the object being cloned and replacing the references to these
     * objects with references to the copies.  If a class contains only
     * primitive fields or references to immutable objects, then it is usually
     * the case that no fields in the object returned by <tt>super.clone</tt>
     * need to be modified.
     *
     * The method <tt>clone</tt> for class <tt>Object</tt> performs a
     * specific cloning operation. First, if the class of this object does
     * not implement the interface <tt>Cloneable</tt>, then a
     * <tt>CloneNotSupportedException</tt> is thrown. Note that all arrays
     * are considered to implement the interface <tt>Cloneable</tt>.
     * Otherwise, this method creates a new instance of the class of this
     * object and initializes all its fields with exactly the contents of
     * the corresponding fields of this object, as if by assignment; the
     * contents of the fields are not themselves cloned. Thus, this method
     * performs a "shallow copy" of this object, not a "deep copy" operation.
     *
     * The class <tt>Object</tt> does not itself implement the interface
     * <tt>Cloneable</tt>, so calling the <tt>clone</tt> method on an object
     * whose class is <tt>Object</tt> will result in throwing an
     * exception at run time.
     *
     * @return a clone of this instance.
     * @throws CloneNotSupportedException if the object's class does not
     *                                    support the <code>Cloneable</code> interface. Subclasses
     *                                    that override the <code>clone</code> method can also
     *                                    throw this exception to indicate that an instance cannot
     *                                    be cloned.
     * @see java.lang.Cloneable
     */
    @Override
    public abstract Object clone() throws CloneNotSupportedException;

    /**
     * Map models to and from XML representation
     */
    @XmlTransient
    private static class SiteLocationAdapter extends XmlAdapter<SiteLocationsType, Set<SiteLocation>> {

        /**
         * Map Java to XML type
         */
        public SiteLocationsType marshal(Set<SiteLocation> set) {
            return (set == null || set.isEmpty() ? null : new SiteLocationsType(set));
        }

        /**
         * Map XML type to Java
         */
        @Override
        public Set<SiteLocation> unmarshal(SiteLocationsType siteLocationsType) {
            return siteLocationsType.getSiteLocations();
        }

    }

    /**
     * Helper class for ModelAdapter
     */
    @XmlType(name = "siteLocationsType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
    private final static class SiteLocationsType {

        @XmlElement(name = "site-location")
        private final Set<SiteLocation> siteLocations;

        private SiteLocationsType() {
            siteLocations = null;
        }

        public SiteLocationsType(Set<SiteLocation> siteLocations) {
            this.siteLocations = siteLocations;
        }

        public Set<SiteLocation> getSiteLocations() {
            return siteLocations;
        }

    }

}
