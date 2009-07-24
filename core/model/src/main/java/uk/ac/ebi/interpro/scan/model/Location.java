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
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Location of match on protein sequence.
 *
 * @author  Antony Quinn
 * @author  Phil Jones 
 * @version $Id$
 * @since   1.0
 */

@Entity
@XmlTransient
abstract class Location implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name="location_start")    // to match end - 'end' is reserved word in SQL.
    private int start;

    @Column (name="location_end")       // 'end' is reserved word in SQL.
    private int end;
    
    @ManyToOne
    private Match match;

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected Location()   { }

    public Location(int start, int end)   {
        setStart(start);
        setEnd(end);
    }    

    /**
     * Needs to be public for JPA as defined in the interface.
     * @return the persistence unique identifier for this object.
     */
    @XmlTransient
    public Long getId() {
        return null;
    }

    /**
     * Needs to be public for JPA as defined in the interface.
     * @param id being the persistence unique identifier for this object.
     */
    public void setId(Long id) {
    }

    /**
     * Returns the start coordinate of this Location.
     * @return the start coordinate of this Location.
     */
    @XmlAttribute(required=true)
    public int getStart() {
        return start;
    }

    /**
     *  Was private for Hibernate (see http://www.javalobby.org/java/forums/t49288.html)
     *  Now public for JPA (as defined in the interface).
     *
     * Required by JPA. The start coordinate of this Location.
     * @param start being the start coordinate of this Location
      */
    // TODO: Make setters private again!
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Returns the end coordinate of this Location.
     * @return  the end coordinate of this Location.
     */
    @XmlAttribute(required=true)
    public int getEnd() {
        return end;
    }

    // Originally private for Hibernate (see http://www.javalobby.org/java/forums/t49288.html)
    // Now public for JPA as needs to be defined in the interface.
    /**
     * Required by JPA.  The end coordinate of this Location.
     * @param end being the end coordinate of this Location.
     */
    public void setEnd(int end) {
        this.end = end;
    }

    @XmlTransient
    public Match getMatch()    {
        return match;
    }

    // Must be package-private so can use from Match.addLocation() and Match.removeLocation()
    // (better as package-private but not possible to specify in interface)
    void setMatch(Match match){
        this.match = match;
    }

    /**
     *  Ensure sub-classes of AbstractLocation are represented correctly in XML.
     */
    @XmlTransient
    static final class LocationAdapter extends XmlAdapter<LocationsType, Set<? extends Location>> {

        /** Map Java to XML type */
        @Override public LocationsType marshal(Set<? extends Location> locations) {
            Set<HmmLocation> hmmLocations = new LinkedHashSet<HmmLocation>();
            Set<FingerPrintsLocation> fingerPrintsLocations = new LinkedHashSet<FingerPrintsLocation>();
            Set<BlastProDomLocation> blastProDomLocations = new LinkedHashSet<BlastProDomLocation>();
            Set<PatternScanLocation> patternScanLocations = new LinkedHashSet<PatternScanLocation>();
            Set<ProfileScanLocation> profileScanLocations = new LinkedHashSet<ProfileScanLocation>();
            for (Location l : locations) {
                if (l instanceof HmmLocation) {
                    hmmLocations.add((HmmLocation)l);
                }
                else if (l instanceof FingerPrintsLocation) {
                    fingerPrintsLocations.add((FingerPrintsLocation)l);
                }
                else if (l instanceof BlastProDomLocation) {
                    blastProDomLocations.add((BlastProDomLocation)l);
                }
                else if (l instanceof PatternScanLocation) {
                    patternScanLocations.add((PatternScanLocation)l);
                }
                else if (l instanceof ProfileScanLocation) {
                    profileScanLocations.add((ProfileScanLocation)l);
                }                
                else    {
                    throw new IllegalArgumentException("Unrecognised Location class: " + l);
                }                
            }
            return new LocationsType(hmmLocations, fingerPrintsLocations, blastProDomLocations,
                                     patternScanLocations, profileScanLocations);
        }

        /** Map XML type to Java */
        @Override public Set<Location> unmarshal(LocationsType locationsType) {
            Set<Location> locations = new LinkedHashSet<Location>();
            locations.addAll(locationsType.getHmmLocations());
            locations.addAll(locationsType.getFingerPrintsLocations());
            locations.addAll(locationsType.getBlastProDomLocations());
            locations.addAll(locationsType.getPatternScanLocations());
            locations.addAll(locationsType.getProfileScanLocations());
            return locations;
        }

    }

    /**
     * Helper class for LocationAdapter
     */
    private final static class LocationsType {

        @XmlElement(name = "hmm-location")
        private final Set<HmmLocation> hmmLocations;

        @XmlElement(name = "fingerprints-location")
        private final Set<FingerPrintsLocation> fingerPrintsLocations;

        @XmlElement(name = "blastprodom-location")
        private final Set<BlastProDomLocation> blastProDomLocations;

        @XmlElement(name = "patternscan-location")
        private final Set<PatternScanLocation> patternScanLocations;

        @XmlElement(name = "profilescan-location")
        private final Set<ProfileScanLocation> profileScanLocations;

        private LocationsType() {
            hmmLocations          = null;        
            fingerPrintsLocations = null;
            blastProDomLocations  = null;
            patternScanLocations  = null;
            profileScanLocations  = null;
        }

        public LocationsType(Set<HmmLocation> hmmLocations,
                             Set<FingerPrintsLocation> fingerPrintsLocations,
                             Set<BlastProDomLocation> blastProDomLocations,
                             Set<PatternScanLocation> patternScanLocations,
                             Set<ProfileScanLocation> profileScanLocations) {
            this.hmmLocations           = hmmLocations;
            this.fingerPrintsLocations  = fingerPrintsLocations;
            this.blastProDomLocations   = blastProDomLocations;
            this.patternScanLocations   = patternScanLocations;
            this.profileScanLocations   = profileScanLocations;            
        }

        public Set<HmmLocation> getHmmLocations() {
            return (hmmLocations == null ? Collections.<HmmLocation>emptySet() : hmmLocations);
        }
        
        public Set<FingerPrintsLocation> getFingerPrintsLocations() {
            return (fingerPrintsLocations == null ? Collections.<FingerPrintsLocation>emptySet() : fingerPrintsLocations);
        }

        public Set<BlastProDomLocation> getBlastProDomLocations() {
            return (blastProDomLocations == null ? Collections.<BlastProDomLocation>emptySet() : blastProDomLocations);
        }

        public Set<PatternScanLocation> getPatternScanLocations() {
            return (patternScanLocations == null ? Collections.<PatternScanLocation>emptySet() : patternScanLocations);
        }
        
        public Set<ProfileScanLocation> getProfileScanLocations() {
            return (profileScanLocations == null ? Collections.<ProfileScanLocation>emptySet() : profileScanLocations);
        }

    }

    @Override public boolean equals(Object o) {
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

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 55)
                .append(start)
                .append(end)
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }
    
}
