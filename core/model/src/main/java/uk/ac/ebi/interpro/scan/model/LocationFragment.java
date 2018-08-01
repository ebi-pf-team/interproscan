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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * Location fragment of location of match on protein sequence.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@XmlType(name = "LocationFragmentType", propOrder = {"start", "end"})
@JsonIgnoreProperties({"id"})
public abstract class LocationFragment implements Serializable, Cloneable, Comparable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "LOCN_FRAG_IDGEN")
    @TableGenerator(name = "LOCN_FRAG_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "locationFragment", initialValue = 0, allocationSize = 50)
    private Long id;

    @Column(name = "loc_start", nullable = false)
    // to match start - 'start' is reserved word in SQL.
    private int start;

    @Column(name = "loc_end", nullable = false)
    // 'end' is reserved word in SQL.
    private int end;

    @Column(name = "dc_status", nullable = false)
    private String dcStatus;

    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    @JsonBackReference
    private Location location;

    /**
     * protected no-arg constructor required by JPA - DO NOT USE DIRECTLY.
     */
    protected LocationFragment() {
    }

    public LocationFragment(int start, int end) {
        setStart(start);
        setEnd(end);
        dcStatus = "c";
    }

    public LocationFragment(int start, int end, String dcStatus) {
        setStart(start);
        setEnd(end);
        setDcStatus(dcStatus);
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
     * Returns the start coordinate of this LocationFragment.
     *
     * @return the start coordinate of this LocationFragment.
     */
    @XmlAttribute(required = true)
    public int getStart() {
        return start;
    }

    /**
     * Start coordinate of this LocationFragment.
     *
     * @param start Start coordinate of this LocationFragment
     */
    private void setStart(int start) {
        this.start = start;
    }

    /**
     * Returns the end coordinate of this LocationFragment.
     *
     * @return the end coordinate of this LocationFragment.
     */
    @XmlAttribute(required = true)
    public int getEnd() {
        return end;
    }

    /**
     * End coordinate of this LocationFragment.
     *
     * @param end End coordinate of this LocationFragment.
     */
    private void setEnd(int end) {
        this.end = end;
    }

    @XmlAttribute(required = true)
//    @XmlElement (name = "dc-status")
    @JsonProperty("dc-status")
    public String getDcStatus() {
        return dcStatus;
    }

    /**
     * Location dcStatus for this LocationFragment.
     *
     * @param dcStatus characteristics of this LocationFragment.
     */
    public void setDcStatus(String dcStatus) {
        this.dcStatus = dcStatus;
    }

    /**
     * This method is called by Location, upon the addition of a LocationFragment to a Location.
     *
     * @param location to which this LocationFragment is related.
     */
    void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Returns the Location that this LocationFragment is related to.
     *
     * @return
     */
    @XmlTransient
    public Location getLocation() {
        return location;
    }

    /**
     * Ensure sub-classes of AbstractLocationFragment are represented correctly in XML.
     *
     * @author Antony Quinn
     */
    @XmlTransient
    static final class LocationFragmentAdapter extends XmlAdapter<LocationFragmentsType, Set<? extends LocationFragment>> {

        /**
         * Map Java to XML type
         */
        @Override
        public LocationFragmentsType marshal(Set<? extends LocationFragment> locations) {
            Set<RPSBlastMatch.RPSBlastLocation.RPSBlastLocationFragment> rpsBlastLocationFragments = new LinkedHashSet<>();
            Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3LocationWithSitesFragment> hmmer3LocationFragmentsWithSites = new LinkedHashSet<>();
            Set<Hmmer2Match.Hmmer2Location.Hmmer2LocationFragment> hmmer2LocationFragments = new LinkedHashSet<>();
            Set<Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment> hmmer3LocationFragments = new LinkedHashSet<>();
            Set<MobiDBMatch.MobiDBLocation.MobiDBLocationFragment> mobiDBLocationFragments = new LinkedHashSet<>();
            Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment> superFamilyHmmer3LocationFragments = new LinkedHashSet<>();
            Set<FingerPrintsMatch.FingerPrintsLocation.FingerPrintsLocationFragment> fingerPrintsLocationFragments = new LinkedHashSet<>();
            Set<BlastProDomMatch.BlastProDomLocation.BlastProDomLocationFragment> blastProDomLocationFragments = new LinkedHashSet<>();
            Set<PatternScanMatch.PatternScanLocation.PatternScanLocationFragment> patternScanLocationFragments = new LinkedHashSet<>();
            Set<ProfileScanMatch.ProfileScanLocation.ProfileScanLocationFragment> profileScanLocationFragments = new LinkedHashSet<>();
            Set<PhobiusMatch.PhobiusLocation.PhobiusLocationFragment> phobiusLocationFragments = new LinkedHashSet<>();
            Set<CoilsMatch.CoilsLocation.CoilsLocationFragment> coilsLocationFragments = new LinkedHashSet<>();
            Set<PantherMatch.PantherLocation.PantherLocationFragment> pantherLocationFragments = new LinkedHashSet<>();
            Set<SignalPMatch.SignalPLocation.SignalPLocationFragment> signalPLocationFragments = new LinkedHashSet<>();
            Set<TMHMMMatch.TMHMMLocation.TMHMMLocationFragment> tmhmmLocationFragments = new LinkedHashSet<>();
            for (LocationFragment l : locations) {
                // LocationFragments that extend "LocationFragment"
                if (l instanceof RPSBlastMatch.RPSBlastLocation.RPSBlastLocationFragment) {
                    rpsBlastLocationFragments.add((RPSBlastMatch.RPSBlastLocation.RPSBlastLocationFragment) l);
                } else if (l instanceof Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3LocationWithSitesFragment) {
                    hmmer3LocationFragmentsWithSites.add((Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3LocationWithSitesFragment) l);
                } else if (l instanceof Hmmer2Match.Hmmer2Location.Hmmer2LocationFragment) {
                    hmmer2LocationFragments.add((Hmmer2Match.Hmmer2Location.Hmmer2LocationFragment) l);
                } else if (l instanceof Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment) {
                    hmmer3LocationFragments.add((Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment) l);
                } else if (l instanceof MobiDBMatch.MobiDBLocation.MobiDBLocationFragment) {
                    mobiDBLocationFragments.add((MobiDBMatch.MobiDBLocation.MobiDBLocationFragment) l);
                } else if (l instanceof SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment) {
                    superFamilyHmmer3LocationFragments.add((SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment) l);
                } else if (l instanceof FingerPrintsMatch.FingerPrintsLocation.FingerPrintsLocationFragment) {
                    fingerPrintsLocationFragments.add((FingerPrintsMatch.FingerPrintsLocation.FingerPrintsLocationFragment) l);
                } else if (l instanceof BlastProDomMatch.BlastProDomLocation.BlastProDomLocationFragment) {
                    blastProDomLocationFragments.add((BlastProDomMatch.BlastProDomLocation.BlastProDomLocationFragment) l);
                } else if (l instanceof PatternScanMatch.PatternScanLocation.PatternScanLocationFragment) {
                    patternScanLocationFragments.add((PatternScanMatch.PatternScanLocation.PatternScanLocationFragment) l);
                } else if (l instanceof ProfileScanMatch.ProfileScanLocation.ProfileScanLocationFragment) {
                    profileScanLocationFragments.add((ProfileScanMatch.ProfileScanLocation.ProfileScanLocationFragment) l);
                } else if (l instanceof PhobiusMatch.PhobiusLocation.PhobiusLocationFragment) {
                    phobiusLocationFragments.add((PhobiusMatch.PhobiusLocation.PhobiusLocationFragment) l);
                } else if (l instanceof CoilsMatch.CoilsLocation.CoilsLocationFragment) {
                    coilsLocationFragments.add((CoilsMatch.CoilsLocation.CoilsLocationFragment) l);
                } else if (l instanceof PantherMatch.PantherLocation.PantherLocationFragment) {
                    pantherLocationFragments.add((PantherMatch.PantherLocation.PantherLocationFragment) l);
                } else if (l instanceof SignalPMatch.SignalPLocation.SignalPLocationFragment) {
                    signalPLocationFragments.add((SignalPMatch.SignalPLocation.SignalPLocationFragment) l);
                } else if (l instanceof TMHMMMatch.TMHMMLocation.TMHMMLocationFragment) {
                    tmhmmLocationFragments.add((TMHMMMatch.TMHMMLocation.TMHMMLocationFragment) l);
                } else {
                    throw new IllegalArgumentException("Unrecognised Location class: " + l);
                }
            }
            return new LocationFragmentsType(rpsBlastLocationFragments, hmmer3LocationFragmentsWithSites,
                    hmmer2LocationFragments, hmmer3LocationFragments, mobiDBLocationFragments,
                    superFamilyHmmer3LocationFragments, fingerPrintsLocationFragments, blastProDomLocationFragments,
                    patternScanLocationFragments, profileScanLocationFragments, phobiusLocationFragments,
                    coilsLocationFragments, pantherLocationFragments, signalPLocationFragments, tmhmmLocationFragments);

        }

        /**
         * Map XML type to Java
         */
        @Override
        public Set<LocationFragment> unmarshal(LocationFragmentsType locationFragmentsType) {
            Set<LocationFragment> fragments = new LinkedHashSet<>();
            fragments.addAll(locationFragmentsType.getRpsBlastLocationFragments());
            fragments.addAll(locationFragmentsType.getHmmer3LocationFragmentsWithSites());
            fragments.addAll(locationFragmentsType.getHmmer2LocationFragments());
            fragments.addAll(locationFragmentsType.getHmmer3LocationFragments());
            fragments.addAll(locationFragmentsType.getMobiDBLocationFragments());
            fragments.addAll(locationFragmentsType.getSuperFamilyHmmer3LocationFragments());
            fragments.addAll(locationFragmentsType.getFingerPrintsLocationFragments());
            fragments.addAll(locationFragmentsType.getBlastProDomLocationFragments());
            fragments.addAll(locationFragmentsType.getPatternScanLocationFragments());
            fragments.addAll(locationFragmentsType.getProfileScanLocationFragments());
            fragments.addAll(locationFragmentsType.getPhobiusLocationFragments());
            fragments.addAll(locationFragmentsType.getCoilsLocationFragments());
            fragments.addAll(locationFragmentsType.getPantherLocationFragments());
            fragments.addAll(locationFragmentsType.getSignalPLocationFragments());
            fragments.addAll(locationFragmentsType.getTMHMMLocationFragments());
            return fragments;
        }

    }

    /**
     * Helper class for LocationFragmentAdapter
     */
    @XmlType(name = "locationFragmentsType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
    private final static class LocationFragmentsType {

        @XmlElement(name = "rpsblast-location-fragment")
        private final Set<RPSBlastMatch.RPSBlastLocation.RPSBlastLocationFragment> rpsBlastLocationFragments;

        @XmlElement(name = "hmmer3-location-fragment-with-sites")
        private final Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3LocationWithSitesFragment> hmmer3LocationFragmentsWithSites;

        @XmlElement(name = "hmmer2-location-fragment")
        private final Set<Hmmer2Match.Hmmer2Location.Hmmer2LocationFragment> hmmer2LocationFragments;

        @XmlElement(name = "hmmer3-location-fragment")
        private final Set<Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment> hmmer3LocationFragments;

        @XmlElement(name = "mobidblite-location-fragment")
        private final Set<MobiDBMatch.MobiDBLocation.MobiDBLocationFragment> mobiDBLocationFragments;

        @XmlElement(name = "superfamilyhmmer3-location-fragment")
        private final Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment> superFamilyHmmer3LocationFragments;

        @XmlElement(name = "fingerprints-location-fragment")
        private final Set<FingerPrintsMatch.FingerPrintsLocation.FingerPrintsLocationFragment> fingerPrintsLocationFragments;

        @XmlElement(name = "blastprodom-location-fragment")
        private final Set<BlastProDomMatch.BlastProDomLocation.BlastProDomLocationFragment> blastProDomLocationFragments;

        @XmlElement(name = "patternscan-location-fragment")
        private final Set<PatternScanMatch.PatternScanLocation.PatternScanLocationFragment> patternScanLocationFragments;

        @XmlElement(name = "profilescan-location-fragment")
        private final Set<ProfileScanMatch.ProfileScanLocation.ProfileScanLocationFragment> profileScanLocationFragments;

        @XmlElement(name = "phobius-location-fragment")
        private final Set<PhobiusMatch.PhobiusLocation.PhobiusLocationFragment> phobiusLocationFragments;

        @XmlElement(name = "coils-location-fragment")
        private final Set<CoilsMatch.CoilsLocation.CoilsLocationFragment> coilsLocationFragments;

        @XmlElement(name = "panther-location-fragment")
        private final Set<PantherMatch.PantherLocation.PantherLocationFragment> pantherLocationFragments;

        @XmlElement(name = "signalp-location-fragment")
        private final Set<SignalPMatch.SignalPLocation.SignalPLocationFragment> signalPLocationFragments;

        @XmlElement(name = "tmhmm-location-fragment")
        private final Set<TMHMMMatch.TMHMMLocation.TMHMMLocationFragment> tmhmmLocationFragments;

        private LocationFragmentsType() {
            rpsBlastLocationFragments = null;
            hmmer3LocationFragmentsWithSites = null;
            hmmer2LocationFragments = null;
            hmmer3LocationFragments = null;
            mobiDBLocationFragments = null;
            superFamilyHmmer3LocationFragments = null;
            fingerPrintsLocationFragments = null;
            blastProDomLocationFragments = null;
            patternScanLocationFragments = null;
            profileScanLocationFragments = null;
            phobiusLocationFragments = null;
            coilsLocationFragments = null;
            pantherLocationFragments = null;
            signalPLocationFragments = null;
            tmhmmLocationFragments = null;
        }

        public LocationFragmentsType(Set<RPSBlastMatch.RPSBlastLocation.RPSBlastLocationFragment> rpsBlastLocationFragments,
                                     Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3LocationWithSitesFragment> hmmer3LocationFragmentsWithSites,
                                     Set<Hmmer2Match.Hmmer2Location.Hmmer2LocationFragment> hmmer2LocationFragments,
                                     Set<Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment> hmmer3LocationFragments,
                                     Set<MobiDBMatch.MobiDBLocation.MobiDBLocationFragment> mobiDBLocationFragments,
                                     Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment> superFamilyHmmer3LocationFragments,
                                     Set<FingerPrintsMatch.FingerPrintsLocation.FingerPrintsLocationFragment> fingerPrintsLocationFragments,
                                     Set<BlastProDomMatch.BlastProDomLocation.BlastProDomLocationFragment> blastProDomLocationFragments,
                                     Set<PatternScanMatch.PatternScanLocation.PatternScanLocationFragment> patternScanLocationFragments,
                                     Set<ProfileScanMatch.ProfileScanLocation.ProfileScanLocationFragment> profileScanLocationFragments,
                                     Set<PhobiusMatch.PhobiusLocation.PhobiusLocationFragment> phobiusLocationFragments,
                                     Set<CoilsMatch.CoilsLocation.CoilsLocationFragment> coilsLocationFragments,
                                     Set<PantherMatch.PantherLocation.PantherLocationFragment> pantherLocationFragments,
                                     Set<SignalPMatch.SignalPLocation.SignalPLocationFragment> signalPLocationFragments,
                                     Set<TMHMMMatch.TMHMMLocation.TMHMMLocationFragment> tmhmmLocationFragments) {
            this.rpsBlastLocationFragments = rpsBlastLocationFragments;
            this.hmmer3LocationFragmentsWithSites = hmmer3LocationFragmentsWithSites;
            this.hmmer2LocationFragments = hmmer2LocationFragments;
            this.hmmer3LocationFragments = hmmer3LocationFragments;
            this.mobiDBLocationFragments = mobiDBLocationFragments;
            this.superFamilyHmmer3LocationFragments = superFamilyHmmer3LocationFragments;
            this.fingerPrintsLocationFragments = fingerPrintsLocationFragments;
            this.blastProDomLocationFragments = blastProDomLocationFragments;
            this.patternScanLocationFragments = patternScanLocationFragments;
            this.profileScanLocationFragments = profileScanLocationFragments;
            this.phobiusLocationFragments = phobiusLocationFragments;
            this.coilsLocationFragments = coilsLocationFragments;
            this.pantherLocationFragments = pantherLocationFragments;
            this.signalPLocationFragments = signalPLocationFragments;
            this.tmhmmLocationFragments = tmhmmLocationFragments;
        }

        public Set<RPSBlastMatch.RPSBlastLocation.RPSBlastLocationFragment> getRpsBlastLocationFragments() {
            return (rpsBlastLocationFragments == null ? Collections.<RPSBlastMatch.RPSBlastLocation.RPSBlastLocationFragment>emptySet() : rpsBlastLocationFragments);
        }

        public Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3LocationWithSitesFragment> getHmmer3LocationFragmentsWithSites() {
            return (hmmer3LocationFragmentsWithSites == null ? Collections.<Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3LocationWithSitesFragment>emptySet() : hmmer3LocationFragmentsWithSites);
        }
        public Set<Hmmer2Match.Hmmer2Location.Hmmer2LocationFragment> getHmmer2LocationFragments() {
            return (hmmer2LocationFragments == null ? Collections.<Hmmer2Match.Hmmer2Location.Hmmer2LocationFragment>emptySet() : hmmer2LocationFragments);
        }

        public Set<Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment> getHmmer3LocationFragments() {
            return (hmmer3LocationFragments == null ? Collections.<Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment>emptySet() : hmmer3LocationFragments);
        }

        public Set<MobiDBMatch.MobiDBLocation.MobiDBLocationFragment> getMobiDBLocationFragments() {
            return (mobiDBLocationFragments == null ? Collections.<MobiDBMatch.MobiDBLocation.MobiDBLocationFragment>emptySet() : mobiDBLocationFragments);
        }

        public Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment> getSuperFamilyHmmer3LocationFragments() {
            return (superFamilyHmmer3LocationFragments == null ? Collections.<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment>emptySet() : superFamilyHmmer3LocationFragments);
        }

        public Set<FingerPrintsMatch.FingerPrintsLocation.FingerPrintsLocationFragment> getFingerPrintsLocationFragments() {
            return (fingerPrintsLocationFragments == null ? Collections.<FingerPrintsMatch.FingerPrintsLocation.FingerPrintsLocationFragment>emptySet() : fingerPrintsLocationFragments);
        }

        public Set<BlastProDomMatch.BlastProDomLocation.BlastProDomLocationFragment> getBlastProDomLocationFragments() {
            return (blastProDomLocationFragments == null ? Collections.<BlastProDomMatch.BlastProDomLocation.BlastProDomLocationFragment>emptySet() : blastProDomLocationFragments);
        }

        public Set<PatternScanMatch.PatternScanLocation.PatternScanLocationFragment> getPatternScanLocationFragments() {
            return (patternScanLocationFragments == null ? Collections.<PatternScanMatch.PatternScanLocation.PatternScanLocationFragment>emptySet() : patternScanLocationFragments);
        }

        public Set<ProfileScanMatch.ProfileScanLocation.ProfileScanLocationFragment> getProfileScanLocationFragments() {
            return (profileScanLocationFragments == null ? Collections.<ProfileScanMatch.ProfileScanLocation.ProfileScanLocationFragment>emptySet() : profileScanLocationFragments);
        }

        public Set<PhobiusMatch.PhobiusLocation.PhobiusLocationFragment> getPhobiusLocationFragments() {
            return (phobiusLocationFragments == null ? Collections.<PhobiusMatch.PhobiusLocation.PhobiusLocationFragment>emptySet() : phobiusLocationFragments);
        }

        public Set<CoilsMatch.CoilsLocation.CoilsLocationFragment> getCoilsLocationFragments() {
            return (coilsLocationFragments == null ? Collections.<CoilsMatch.CoilsLocation.CoilsLocationFragment>emptySet() : coilsLocationFragments);
        }

        public Set<PantherMatch.PantherLocation.PantherLocationFragment> getPantherLocationFragments() {
            return (pantherLocationFragments == null ? Collections.<PantherMatch.PantherLocation.PantherLocationFragment>emptySet() : pantherLocationFragments);
        }

        public Set<SignalPMatch.SignalPLocation.SignalPLocationFragment> getSignalPLocationFragments() {
            return (signalPLocationFragments == null ? Collections.<SignalPMatch.SignalPLocation.SignalPLocationFragment>emptySet() : signalPLocationFragments);
        }

        public Set<TMHMMMatch.TMHMMLocation.TMHMMLocationFragment> getTMHMMLocationFragments() {
            return (tmhmmLocationFragments == null ? Collections.<TMHMMMatch.TMHMMLocation.TMHMMLocationFragment>emptySet() : tmhmmLocationFragments);
        }
    }

    private String getDCStatus(String statusOne, String statusTwo){
        String status = "";
        if (statusOne.equals(statusTwo)){
            status = statusOne;
        }else{
            status = statusOne + statusTwo;
            if (status.equals("es")){
                status = "se";
            }
        }
        return status;
    }

    public void updateDCStatus(Object o) {
        if (this == o || this.dcStatus.equals("se")) {
            return;
        }
        if (this.dcStatus.equals("c")){
            setDcStatus("");
        }
        final LocationFragment h = (LocationFragment) o;
        if (this.start < h.start) {
            setDcStatus(getDCStatus(this.dcStatus, "e"));
        }
        if (this.start > h.start) {
            setDcStatus(getDCStatus(this.dcStatus, "s"));
        }
        System.out.println("this.dcStatus: " + this.dcStatus +  " h.dcStatus: " + h.getDcStatus());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LocationFragment))
            return false;
        final LocationFragment h = (LocationFragment) o;
        return new EqualsBuilder()
                .append(start, h.start)
                .append(end, h.end)
                .isEquals();
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) {
            return 0;
        }
        final LocationFragment h = (LocationFragment) o;
        if (this.start < h.start) {
            return -10;
        }
        if (this.start > h.start) {
            return 10;
        }
        return -1;
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
