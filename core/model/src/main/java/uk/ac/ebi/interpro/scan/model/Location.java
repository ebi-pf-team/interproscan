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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
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
public abstract class Location implements Serializable {

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
            Set<Hmmer2Match.Hmmer2Location> hmmer2Locations = new LinkedHashSet<Hmmer2Match.Hmmer2Location>();
            Set<Hmmer3Match.Hmmer3Location> hmmer3Locations = new LinkedHashSet<Hmmer3Match.Hmmer3Location>();
            Set<FingerPrintsMatch.FingerPrintsLocation> fingerPrintsLocations = new LinkedHashSet<FingerPrintsMatch.FingerPrintsLocation>();
            Set<BlastProDomMatch.BlastProDomLocation> blastProDomLocations = new LinkedHashSet<BlastProDomMatch.BlastProDomLocation>();
            Set<PatternScanMatch.PatternScanLocation> patternScanLocations = new LinkedHashSet<PatternScanMatch.PatternScanLocation>();
            Set<ProfileScanMatch.ProfileScanLocation> profileScanLocations = new LinkedHashSet<ProfileScanMatch.ProfileScanLocation>();
            Set<PhobiusMatch.PhobiusLocation> phobiusLocations = new LinkedHashSet<PhobiusMatch.PhobiusLocation>();
            Set<CoilsMatch.CoilsLocation> coilsLocations = new LinkedHashSet<CoilsMatch.CoilsLocation>();
            Set<ProDomMatch.ProDomLocation> proDomLocations = new LinkedHashSet<ProDomMatch.ProDomLocation>();
            for (Location l : locations) {
                if (l instanceof Hmmer2Match.Hmmer2Location) {
                    hmmer2Locations.add((Hmmer2Match.Hmmer2Location) l);
                } else if (l instanceof Hmmer3Match.Hmmer3Location) {
                    hmmer3Locations.add((Hmmer3Match.Hmmer3Location) l);
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
                } else if (l instanceof ProDomMatch.ProDomLocation) {
                    proDomLocations.add((ProDomMatch.ProDomLocation) l);
                } else {
                    throw new IllegalArgumentException("Unrecognised Location class: " + l);
                }
            }
            return new LocationsType(hmmer2Locations, hmmer3Locations, fingerPrintsLocations, blastProDomLocations,
                    patternScanLocations, profileScanLocations, phobiusLocations, coilsLocations, proDomLocations);
        }

        /**
         * Map XML type to Java
         */
        @Override
        public Set<Location> unmarshal(LocationsType locationsType) {
            Set<Location> locations = new LinkedHashSet<Location>();
            locations.addAll(locationsType.getHmmer2Locations());
            locations.addAll(locationsType.getHmmer3Locations());
            locations.addAll(locationsType.getFingerPrintsLocations());
            locations.addAll(locationsType.getBlastProDomLocations());
            locations.addAll(locationsType.getPatternScanLocations());
            locations.addAll(locationsType.getProfileScanLocations());
            locations.addAll(locationsType.getPhobiusLocations());
            locations.addAll(locationsType.getCoilsLocations());
            locations.addAll(locationsType.getProDomLocations());
            return locations;
        }

    }

    /**
     * Helper class for LocationAdapter
     */
    private final static class LocationsType {

        @XmlElement(name = "hmmer2-location")
        private final Set<Hmmer2Match.Hmmer2Location> hmmer2Locations;

        @XmlElement(name = "hmmer3-location")
        private final Set<Hmmer3Match.Hmmer3Location> hmmer3Locations;

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

        @XmlElement(name = "prodom-location")
        private final Set<ProDomMatch.ProDomLocation> proDomLocations;

        private LocationsType() {
            hmmer2Locations = null;
            hmmer3Locations = null;
            fingerPrintsLocations = null;
            blastProDomLocations = null;
            patternScanLocations = null;
            profileScanLocations = null;
            phobiusLocations = null;
            coilsLocations = null;
            proDomLocations = null;
        }

        public LocationsType(Set<Hmmer2Match.Hmmer2Location> hmmer2Locations,
                             Set<Hmmer3Match.Hmmer3Location> hmmer3Locations,
                             Set<FingerPrintsMatch.FingerPrintsLocation> fingerPrintsLocations,
                             Set<BlastProDomMatch.BlastProDomLocation> blastProDomLocations,
                             Set<PatternScanMatch.PatternScanLocation> patternScanLocations,
                             Set<ProfileScanMatch.ProfileScanLocation> profileScanLocations,
                             Set<PhobiusMatch.PhobiusLocation> phobiusLocations,
                             Set<CoilsMatch.CoilsLocation> coilsLocations,
                             Set<ProDomMatch.ProDomLocation> proDomLocations) {
            this.hmmer2Locations = hmmer2Locations;
            this.hmmer3Locations = hmmer3Locations;
            this.fingerPrintsLocations = fingerPrintsLocations;
            this.blastProDomLocations = blastProDomLocations;
            this.patternScanLocations = patternScanLocations;
            this.profileScanLocations = profileScanLocations;
            this.phobiusLocations = phobiusLocations;
            this.coilsLocations = coilsLocations;
            this.proDomLocations = proDomLocations;
        }

        public Set<Hmmer2Match.Hmmer2Location> getHmmer2Locations() {
            return (hmmer2Locations == null ? Collections.<Hmmer2Match.Hmmer2Location>emptySet() : hmmer2Locations);
        }

        public Set<Hmmer3Match.Hmmer3Location> getHmmer3Locations() {
            return (hmmer3Locations == null ? Collections.<Hmmer3Match.Hmmer3Location>emptySet() : hmmer3Locations);
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

        public Set<ProDomMatch.ProDomLocation> getProDomLocations() {
            return (proDomLocations == null ? Collections.<ProDomMatch.ProDomLocation>emptySet() : proDomLocations);
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

}
