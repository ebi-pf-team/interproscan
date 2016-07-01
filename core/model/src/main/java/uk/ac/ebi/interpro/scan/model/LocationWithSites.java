/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.interpro.scan.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Location of match on protein sequence.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@XmlType(name = "LocationWithSitesType")
//@XmlType(name = "LocationWithSitesType", propOrder = {"start", "end"})
public abstract class LocationWithSites<T extends Site> extends Location {

    @OneToMany(cascade = CascadeType.PERSIST, targetEntity = Site.class, mappedBy = "location")
    @BatchSize(size=4000)
    protected Set<T> sites = new LinkedHashSet<>();

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected LocationWithSites() {
    }

    public LocationWithSites(int start, int end, Set<T> sites) {
        super(start, end);
        setSites(sites);
    }

    @Transient
    @XmlJavaTypeAdapter(Site.SiteAdapter.class)
    public Set<T> getSites() {
//        return Collections.unmodifiableSet(sites);
        return sites;
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection

    protected void setSites(final Set<T> sites) {
        if (sites != null) {
            for (T site : sites) {
                site.setLocation(this);
                this.sites.add(site);
            }
        }
    }

    @Transient
    public void addSite(T site) {
        site.setLocation(this);
        this.sites.add(site);
    }

    /**
     * Ensure sub-classes of AbstractLocation are represented correctly in XML.
     *
     * @author Antony Quinn
     */
    @XmlTransient
    static final class LocationWithSitesAdapter extends XmlAdapter<LocationsWithSitesType, Set<? extends LocationWithSites>> {

        /**
         * Map Java to XML type
         */
        @Override
        public LocationsWithSitesType marshal(Set<? extends LocationWithSites> locations) {
            Set<RPSBlastMatch.RPSBlastLocation> rpsBlastLocations = new LinkedHashSet<RPSBlastMatch.RPSBlastLocation>();
            for (LocationWithSites l : locations) {
                if (l instanceof RPSBlastMatch.RPSBlastLocation) {
                    rpsBlastLocations.add((RPSBlastMatch.RPSBlastLocation) l);
                } else {
                    throw new IllegalArgumentException("Unrecognised Location class: " + l);
                }
            }
            return new LocationsWithSitesType(rpsBlastLocations);
        }

        /**
         * Map XML type to Java
         */
        @Override
        public Set<LocationWithSites> unmarshal(LocationsWithSitesType locationsWithSitesType) {
            Set<LocationWithSites> locations = new LinkedHashSet<>();
            locations.addAll(locationsWithSitesType.getRpsBlastLocations());
            return locations;
        }

    }

    /**
     * Helper class for LocationAdapter
     */
    @XmlType(name = "locationsWithSitesType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
    private final static class LocationsWithSitesType {

        @XmlElement(name = "rpsblast-location")
        private final Set<RPSBlastMatch.RPSBlastLocation> rpsBlastLocations;

        private LocationsWithSitesType() {
            rpsBlastLocations = null;
        }

        public LocationsWithSitesType(Set<RPSBlastMatch.RPSBlastLocation> rpsBlastLocations) {
            this.rpsBlastLocations = rpsBlastLocations;
        }

        public Set<RPSBlastMatch.RPSBlastLocation> getRpsBlastLocations() {
            return (rpsBlastLocations == null ? Collections.<RPSBlastMatch.RPSBlastLocation>emptySet() : rpsBlastLocations);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LocationWithSites))
            return false;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 55)
                .appendSuper(super.hashCode())
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
     * @see Cloneable
     */
    @Override
    public abstract Object clone() throws CloneNotSupportedException;
}
