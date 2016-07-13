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

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
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
@XmlType(name = "LocationType", propOrder = {"start", "end"})
@XmlSeeAlso(LocationWithSites.class)
public abstract class Location implements Serializable, Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "LOCN_IDGEN")
    @TableGenerator(name = "LOCN_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "location", initialValue = 0, allocationSize = 50)
    private Long id;

    @Column(name = "loc_start", nullable = false)
    // to match start - 'start' is reserved word in SQL.
    private int start;

    @Column(name = "loc_end", nullable = false)
    // 'end' is reserved word in SQL.
    private int end;

    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    private Match match;

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected Location() {
    }

    public Location(int start, int end) {
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

    /**
     * Returns the start coordinate of this Location.
     *
     * @return the start coordinate of this Location.
     */
    @XmlAttribute(required = true)
    public int getStart() {
        return start;
    }

    /**
     * Start coordinate of this Location.
     *
     * @param start Start coordinate of this Location
     */
    private void setStart(int start) {
        this.start = start;
    }

    /**
     * Returns the end coordinate of this Location.
     *
     * @return the end coordinate of this Location.
     */
    @XmlAttribute(required = true)
    public int getEnd() {
        return end;
    }

    /**
     * End coordinate of this Location.
     *
     * @param end End coordinate of this Location.
     */
    private void setEnd(int end) {
        this.end = end;
    }

    /**
     * This method is called by Match, upon the addition of a Location to a Match.
     *
     * @param match to which this Location is related.
     */
    void setMatch(Match match) {
        this.match = match;
    }

    /**
     * Returns the Match that this Location is related to.
     *
     * @return
     */
    @XmlTransient
    public Match getMatch() {
        return match;
    }

    /**
     * Ensure sub-classes of AbstractLocation are represented correctly in XML.
     *
     * @author Antony Quinn
     */
    @XmlTransient
    static final class LocationAdapter extends XmlAdapter<LocationsType, Set<? extends Location>> {

        /**
         * Map Java to XML type
         */
        @Override
        public LocationsType marshal(Set<? extends Location> locations) {
            Set<RPSBlastMatch.RPSBlastLocation> rpsBlastLocations = new LinkedHashSet<>();
            Set<Hmmer2Match.Hmmer2Location> hmmer2Locations = new LinkedHashSet<Hmmer2Match.Hmmer2Location>();
            Set<Hmmer3Match.Hmmer3Location> hmmer3Locations = new LinkedHashSet<Hmmer3Match.Hmmer3Location>();
            Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> superFamilyHmmer3Locations = new LinkedHashSet<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location>();
            Set<FingerPrintsMatch.FingerPrintsLocation> fingerPrintsLocations = new LinkedHashSet<FingerPrintsMatch.FingerPrintsLocation>();
            Set<BlastProDomMatch.BlastProDomLocation> blastProDomLocations = new LinkedHashSet<BlastProDomMatch.BlastProDomLocation>();
            Set<PatternScanMatch.PatternScanLocation> patternScanLocations = new LinkedHashSet<PatternScanMatch.PatternScanLocation>();
            Set<ProfileScanMatch.ProfileScanLocation> profileScanLocations = new LinkedHashSet<ProfileScanMatch.ProfileScanLocation>();
            Set<PhobiusMatch.PhobiusLocation> phobiusLocations = new LinkedHashSet<PhobiusMatch.PhobiusLocation>();
            Set<CoilsMatch.CoilsLocation> coilsLocations = new LinkedHashSet<CoilsMatch.CoilsLocation>();
            Set<PantherMatch.PantherLocation> pantherLocations = new LinkedHashSet<PantherMatch.PantherLocation>();
            Set<SignalPMatch.SignalPLocation> signalPLocations = new LinkedHashSet<SignalPMatch.SignalPLocation>();
            Set<TMHMMMatch.TMHMMLocation> tmhmmLocations = new LinkedHashSet<TMHMMMatch.TMHMMLocation>();
            for (Location l : locations) {
                // TODO RPSBlastLocation is not a Location but is acually a LocationWithSite subclass - review?
                if (l instanceof RPSBlastMatch.RPSBlastLocation) {
                    rpsBlastLocations.add((RPSBlastMatch.RPSBlastLocation) l);
                } else if (l instanceof Hmmer2Match.Hmmer2Location) {
                    hmmer2Locations.add((Hmmer2Match.Hmmer2Location) l);
                } else if (l instanceof Hmmer3Match.Hmmer3Location) {
                    hmmer3Locations.add((Hmmer3Match.Hmmer3Location) l);
                } else if (l instanceof SuperFamilyHmmer3Match.SuperFamilyHmmer3Location) {
                    superFamilyHmmer3Locations.add((SuperFamilyHmmer3Match.SuperFamilyHmmer3Location) l);
                } else if (l instanceof FingerPrintsMatch.FingerPrintsLocation) {
                    fingerPrintsLocations.add((FingerPrintsMatch.FingerPrintsLocation) l);
                } else if (l instanceof BlastProDomMatch.BlastProDomLocation) {
                    blastProDomLocations.add((BlastProDomMatch.BlastProDomLocation) l);
                } else if (l instanceof PatternScanMatch.PatternScanLocation) {
                    patternScanLocations.add((PatternScanMatch.PatternScanLocation) l);
                } else if (l instanceof ProfileScanMatch.ProfileScanLocation) {
                    profileScanLocations.add((ProfileScanMatch.ProfileScanLocation) l);
                } else if (l instanceof PhobiusMatch.PhobiusLocation) {
                    phobiusLocations.add((PhobiusMatch.PhobiusLocation) l);
                } else if (l instanceof CoilsMatch.CoilsLocation) {
                    coilsLocations.add((CoilsMatch.CoilsLocation) l);
                } else if (l instanceof PantherMatch.PantherLocation) {
                    pantherLocations.add((PantherMatch.PantherLocation) l);
                } else if (l instanceof SignalPMatch.SignalPLocation) {
                    signalPLocations.add((SignalPMatch.SignalPLocation) l);
                } else if (l instanceof TMHMMMatch.TMHMMLocation) {
                    tmhmmLocations.add((TMHMMMatch.TMHMMLocation) l);
                } else {
                    throw new IllegalArgumentException("Unrecognised Location class: " + l);
                }
            }
            return new LocationsType(rpsBlastLocations, hmmer2Locations, hmmer3Locations, superFamilyHmmer3Locations, fingerPrintsLocations, blastProDomLocations,
                    patternScanLocations, profileScanLocations, phobiusLocations, coilsLocations, pantherLocations, signalPLocations, tmhmmLocations);
        }

        /**
         * Map XML type to Java
         */
        @Override
        public Set<Location> unmarshal(LocationsType locationsType) {
            Set<Location> locations = new LinkedHashSet<>();
            locations.addAll(locationsType.getRpsBlastLocations());
            locations.addAll(locationsType.getHmmer2Locations());
            locations.addAll(locationsType.getHmmer3Locations());
            locations.addAll(locationsType.getSuperFamilyHmmer3Locations());
            locations.addAll(locationsType.getFingerPrintsLocations());
            locations.addAll(locationsType.getBlastProDomLocations());
            locations.addAll(locationsType.getPatternScanLocations());
            locations.addAll(locationsType.getProfileScanLocations());
            locations.addAll(locationsType.getPhobiusLocations());
            locations.addAll(locationsType.getCoilsLocations());
            locations.addAll(locationsType.getPantherLocations());
            locations.addAll(locationsType.getSignalPLocations());
            locations.addAll(locationsType.getTMHMMLocations());
            return locations;
        }

    }

    /**
     * Helper class for LocationAdapter
     */
    @XmlType(name = "locationsType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
    private final static class LocationsType {

        @XmlElement(name = "rpsblast-location")
        private final Set<RPSBlastMatch.RPSBlastLocation> rpsBlastLocations;

        @XmlElement(name = "hmmer2-location")
        private final Set<Hmmer2Match.Hmmer2Location> hmmer2Locations;

        @XmlElement(name = "hmmer3-location")
        private final Set<Hmmer3Match.Hmmer3Location> hmmer3Locations;

        @XmlElement(name = "superfamilyhmmer3-location")
        private final Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> superFamilyHmmer3Locations;

        @XmlElement(name = "fingerprints-location")
        private final Set<FingerPrintsMatch.FingerPrintsLocation> fingerPrintsLocations;

        @XmlElement(name = "blastprodom-location")
        private final Set<BlastProDomMatch.BlastProDomLocation> blastProDomLocations;

        @XmlElement(name = "patternscan-location")
        private final Set<PatternScanMatch.PatternScanLocation> patternScanLocations;

        @XmlElement(name = "profilescan-location")
        private final Set<ProfileScanMatch.ProfileScanLocation> profileScanLocations;

        @XmlElement(name = "phobius-location")
        private final Set<PhobiusMatch.PhobiusLocation> phobiusLocations;

        @XmlElement(name = "coils-location")
        private final Set<CoilsMatch.CoilsLocation> coilsLocations;

        @XmlElement(name = "panther-location")
        private final Set<PantherMatch.PantherLocation> pantherLocations;

        @XmlElement(name = "signalp-location")
        private final Set<SignalPMatch.SignalPLocation> signalPLocations;

        @XmlElement(name = "tmhmm-location")
        private final Set<TMHMMMatch.TMHMMLocation> tmhmmLocations;

        private LocationsType() {
            rpsBlastLocations = null;
            hmmer2Locations = null;
            hmmer3Locations = null;
            superFamilyHmmer3Locations = null;
            fingerPrintsLocations = null;
            blastProDomLocations = null;
            patternScanLocations = null;
            profileScanLocations = null;
            phobiusLocations = null;
            coilsLocations = null;
            pantherLocations = null;
            signalPLocations = null;
            tmhmmLocations = null;
        }

        public LocationsType(Set<RPSBlastMatch.RPSBlastLocation> rpsBlastLocations,
                             Set<Hmmer2Match.Hmmer2Location> hmmer2Locations,
                             Set<Hmmer3Match.Hmmer3Location> hmmer3Locations,
                             Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> superFamilyHmmer3Locations,
                             Set<FingerPrintsMatch.FingerPrintsLocation> fingerPrintsLocations,
                             Set<BlastProDomMatch.BlastProDomLocation> blastProDomLocations,
                             Set<PatternScanMatch.PatternScanLocation> patternScanLocations,
                             Set<ProfileScanMatch.ProfileScanLocation> profileScanLocations,
                             Set<PhobiusMatch.PhobiusLocation> phobiusLocations,
                             Set<CoilsMatch.CoilsLocation> coilsLocations,
                             Set<PantherMatch.PantherLocation> pantherLocations,
                             Set<SignalPMatch.SignalPLocation> signalPLocations,
                             Set<TMHMMMatch.TMHMMLocation> tmhmmLocations) {
            this.rpsBlastLocations = rpsBlastLocations;
            this.hmmer2Locations = hmmer2Locations;
            this.hmmer3Locations = hmmer3Locations;
            this.superFamilyHmmer3Locations = superFamilyHmmer3Locations;
            this.fingerPrintsLocations = fingerPrintsLocations;
            this.blastProDomLocations = blastProDomLocations;
            this.patternScanLocations = patternScanLocations;
            this.profileScanLocations = profileScanLocations;
            this.phobiusLocations = phobiusLocations;
            this.coilsLocations = coilsLocations;
            this.pantherLocations = pantherLocations;
            this.signalPLocations = signalPLocations;
            this.tmhmmLocations = tmhmmLocations;
        }

        public Set<RPSBlastMatch.RPSBlastLocation> getRpsBlastLocations() {
            return (rpsBlastLocations == null ? Collections.<RPSBlastMatch.RPSBlastLocation>emptySet() : rpsBlastLocations);
        }

        public Set<Hmmer2Match.Hmmer2Location> getHmmer2Locations() {
            return (hmmer2Locations == null ? Collections.<Hmmer2Match.Hmmer2Location>emptySet() : hmmer2Locations);
        }

        public Set<Hmmer3Match.Hmmer3Location> getHmmer3Locations() {
            return (hmmer3Locations == null ? Collections.<Hmmer3Match.Hmmer3Location>emptySet() : hmmer3Locations);
        }

        public Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> getSuperFamilyHmmer3Locations() {
            return (superFamilyHmmer3Locations == null ? Collections.<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location>emptySet() : superFamilyHmmer3Locations);
        }

        public Set<FingerPrintsMatch.FingerPrintsLocation> getFingerPrintsLocations() {
            return (fingerPrintsLocations == null ? Collections.<FingerPrintsMatch.FingerPrintsLocation>emptySet() : fingerPrintsLocations);
        }

        public Set<BlastProDomMatch.BlastProDomLocation> getBlastProDomLocations() {
            return (blastProDomLocations == null ? Collections.<BlastProDomMatch.BlastProDomLocation>emptySet() : blastProDomLocations);
        }

        public Set<PatternScanMatch.PatternScanLocation> getPatternScanLocations() {
            return (patternScanLocations == null ? Collections.<PatternScanMatch.PatternScanLocation>emptySet() : patternScanLocations);
        }

        public Set<ProfileScanMatch.ProfileScanLocation> getProfileScanLocations() {
            return (profileScanLocations == null ? Collections.<ProfileScanMatch.ProfileScanLocation>emptySet() : profileScanLocations);
        }

        public Set<PhobiusMatch.PhobiusLocation> getPhobiusLocations() {
            return (phobiusLocations == null ? Collections.<PhobiusMatch.PhobiusLocation>emptySet() : phobiusLocations);
        }

        public Set<CoilsMatch.CoilsLocation> getCoilsLocations() {
            return (coilsLocations == null ? Collections.<CoilsMatch.CoilsLocation>emptySet() : coilsLocations);
        }

        public Set<PantherMatch.PantherLocation> getPantherLocations() {
            return (pantherLocations == null ? Collections.<PantherMatch.PantherLocation>emptySet() : pantherLocations);
        }

        public Set<SignalPMatch.SignalPLocation> getSignalPLocations() {
            return (signalPLocations == null ? Collections.<SignalPMatch.SignalPLocation>emptySet() : signalPLocations);
        }

        public Set<TMHMMMatch.TMHMMLocation> getTMHMMLocations() {
            return (tmhmmLocations == null ? Collections.<TMHMMMatch.TMHMMLocation>emptySet() : tmhmmLocations);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Location))
            return false;
        final Location h = (Location) o;
        return new EqualsBuilder()
                .append(start, h.start)
                .append(end, h.end)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 55)
                .append(start)
                .append(end)
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
