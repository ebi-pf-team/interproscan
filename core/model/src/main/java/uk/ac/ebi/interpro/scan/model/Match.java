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
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents a signature match on a protein sequence.
 *
 * @author Antony Quinn
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@XmlType(name = "MatchType", propOrder = {"signature", "locations"})
@JsonIgnoreProperties({"id"})
public abstract class Match<T extends Location> implements Serializable, Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MATCH_IDGEN")
    @TableGenerator(name = "MATCH_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "match", initialValue = 0, allocationSize = 50)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PROTEIN_ID", referencedColumnName = "ID")
    @JsonBackReference
    private Protein protein;

    @ManyToOne(optional = false)
    @JoinColumn(name = "SIGNATURE_ID", referencedColumnName = "ID")
    private Signature signature;

    @OneToMany(cascade = CascadeType.PERSIST, targetEntity = Location.class, mappedBy = "match")
    @BatchSize(size=4000)
    @JsonManagedReference
    protected Set<T> locations = new LinkedHashSet<T>();

    protected Match() {
    }

    protected Match(Signature signature, Set<T> locations) {
        setLocations(locations);
        setSignature(signature);
    }

    @XmlTransient
    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    @XmlTransient
    public Protein getProtein() {
        return protein;
    }

    void setProtein(Protein protein) {
        this.protein = protein;

    }

    @XmlElement(required = true)
    public Signature getSignature() {
        return signature;
    }

    private void setSignature(Signature signature) {
        this.signature = signature;
    }

    @Transient
    @XmlJavaTypeAdapter(Location.LocationAdapter.class)
    public Set<T> getLocations() {
//        return Collections.unmodifiableSet(locations);
        return locations;
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection

    protected void setLocations(final Set<T> locations) {
        if (locations != null) {
            for (T location : locations) {
                location.setMatch(this);
                this.locations.add(location);
            }
        }
    }

    @Transient
    public void addLocation(T location) {
        location.setMatch(this);
        this.locations.add(location);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Match))
            return false;
        final Match m = (Match) o;
        return new EqualsBuilder()
                .append(locations, m.locations)
                .append(signature, m.signature)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 51)
                .append(locations)
                .append(signature)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    // TODO: Now we're using abstract classes instead of interfaces, can we get rid of the following?

    /**
     * Ensure sub-classes of Match are represented correctly in XML.
     *
     * @author Antony Quinn
     */
    @XmlTransient
    static final class MatchAdapter extends XmlAdapter<MatchesType, Set<Match>> {

        /**
         * Map Java to XML type
         */
        @Override
        public MatchesType marshal(Set<Match> matches) {
            Set<RPSBlastMatch> rpsBlastMatches = new LinkedHashSet<>();
            Set<Hmmer3MatchWithSites> hmmer3MatchesWithSites = new LinkedHashSet<>();
            Set<Hmmer2Match> hmmer2Matches = new LinkedHashSet<Hmmer2Match>();
            Set<Hmmer3Match> hmmer3Matches = new LinkedHashSet<Hmmer3Match>();
            Set<MobiDBMatch> mobiDBMatches = new LinkedHashSet<MobiDBMatch>();
            Set<SuperFamilyHmmer3Match> superFamilyHmmer3Matches = new LinkedHashSet<SuperFamilyHmmer3Match>();
            Set<FingerPrintsMatch> fingerPrintsMatches = new LinkedHashSet<FingerPrintsMatch>();
            Set<BlastProDomMatch> proDomMatches = new LinkedHashSet<BlastProDomMatch>();
            Set<PatternScanMatch> patternScanMatches = new LinkedHashSet<PatternScanMatch>();
            Set<ProfileScanMatch> profileScanMatches = new LinkedHashSet<ProfileScanMatch>();
            Set<PhobiusMatch> phobiusMatches = new LinkedHashSet<PhobiusMatch>();
            Set<CoilsMatch> coilsMatches = new LinkedHashSet<CoilsMatch>();
            Set<PantherMatch> pantherMatches = new LinkedHashSet<PantherMatch>();
            Set<SignalPMatch> signalPMatches = new LinkedHashSet<SignalPMatch>();
            Set<TMHMMMatch> tmhmmPMatches = new LinkedHashSet<TMHMMMatch>();
            for (Match m : matches) {
                if (m instanceof RPSBlastMatch) {
                    rpsBlastMatches.add((RPSBlastMatch) m);
                } else if (m instanceof Hmmer3MatchWithSites) {
                    hmmer3MatchesWithSites.add((Hmmer3MatchWithSites) m);
                } else if (m instanceof Hmmer2Match) {
                    hmmer2Matches.add((Hmmer2Match) m);
                } else if (m instanceof Hmmer3Match) {
                    hmmer3Matches.add((Hmmer3Match) m);
                } else if (m instanceof MobiDBMatch) {
                    mobiDBMatches.add((MobiDBMatch) m);
                } else if (m instanceof SuperFamilyHmmer3Match) {
                    superFamilyHmmer3Matches.add((SuperFamilyHmmer3Match) m);
                } else if (m instanceof FingerPrintsMatch) {
                    fingerPrintsMatches.add((FingerPrintsMatch) m);
                } else if (m instanceof BlastProDomMatch) {
                    proDomMatches.add((BlastProDomMatch) m);
                } else if (m instanceof PatternScanMatch) {
                    patternScanMatches.add((PatternScanMatch) m);
                } else if (m instanceof ProfileScanMatch) {
                    profileScanMatches.add((ProfileScanMatch) m);
                } else if (m instanceof PhobiusMatch) {
                    phobiusMatches.add((PhobiusMatch) m);
                } else if (m instanceof CoilsMatch) {
                    coilsMatches.add((CoilsMatch) m);
                } else if (m instanceof PantherMatch) {
                    pantherMatches.add((PantherMatch) m);
                } else if (m instanceof SignalPMatch) {
                    signalPMatches.add((SignalPMatch) m);
                } else if (m instanceof TMHMMMatch) {
                    tmhmmPMatches.add((TMHMMMatch) m);
                } else {
                    throw new IllegalArgumentException("Unrecognised Match class: " + m);
                }
            }
            return new MatchesType(rpsBlastMatches, hmmer3MatchesWithSites, hmmer2Matches, hmmer3Matches, mobiDBMatches, superFamilyHmmer3Matches, fingerPrintsMatches, proDomMatches,
                    patternScanMatches, profileScanMatches, phobiusMatches, coilsMatches, pantherMatches, signalPMatches, tmhmmPMatches);
        }

        /**
         * Map XML type to Java
         */
        @Override
        public Set<Match> unmarshal
        (MatchesType
                 matchTypes) {
            Set<Match> matches = new HashSet<Match>();
            matches.addAll(matchTypes.getRpsBlastMatches());
            matches.addAll(matchTypes.getHmmer3MatchesWithSites());
            matches.addAll(matchTypes.getHmmer2Matches());
            matches.addAll(matchTypes.getHmmer3Matches());
            matches.addAll(matchTypes.getHmmer3Matches());
            matches.addAll(matchTypes.getMobiDBMatches());
            matches.addAll(matchTypes.getSuperFamilyHmmer3Matches());
            matches.addAll(matchTypes.getFingerPrintsMatches());
            matches.addAll(matchTypes.getProDomMatches());
            matches.addAll(matchTypes.getPatternScanMatches());
            matches.addAll(matchTypes.getProfileScanMatches());
            matches.addAll(matchTypes.getPhobiusMatches());
            matches.addAll(matchTypes.getCoilsMatches());
            matches.addAll(matchTypes.getPantherMatches());
            matches.addAll(matchTypes.getSignalPMatches());
            matches.addAll(matchTypes.getTmhmmMatches());
            return matches;
        }

    }

    /**
     * Helper class for MatchAdapter
     */
    @XmlType(name = "matchesType", namespace = "http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5")
    @XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
    private final static class MatchesType {
        @XmlElement(name = "rpsblast-match")
        private final Set<RPSBlastMatch> rpsBlastMatches;

        @XmlElement(name = "hmmer3-match-with-sites")
        private final Set<Hmmer3MatchWithSites> hmmer3MatchesWithSites;

        @XmlElement(name = "hmmer2-match")
        private final Set<Hmmer2Match> hmmer2Matches;

        @XmlElement(name = "hmmer3-match")
        private final Set<Hmmer3Match> hmmer3Matches;

        @XmlElement(name = "mobidblite-match")
        private final Set<MobiDBMatch> mobiDBMatches;

        @XmlElement(name = "superfamilyhmmer3-match")
        private final Set<SuperFamilyHmmer3Match> superFamilyHmmer3Matches;

        @XmlElement(name = "fingerprints-match")
        private final Set<FingerPrintsMatch> fingerPrintsMatches;

        @XmlElement(name = "blastprodom-match")
        private final Set<BlastProDomMatch> proDomMatches;

        @XmlElement(name = "patternscan-match")
        private final Set<PatternScanMatch> patternScanMatches;

        @XmlElement(name = "profilescan-match")
        private final Set<ProfileScanMatch> profileScanMatches;

        @XmlElement(name = "phobius-match")
        private final Set<PhobiusMatch> phobiusMatches;

        @XmlElement(name = "coils-match")
        private final Set<CoilsMatch> coilsMatches;

        @XmlElement(name = "panther-match")
        private final Set<PantherMatch> pantherMatches;

        @XmlElement(name = "signalp-match")
        private final Set<SignalPMatch> signalPMatches;

        @XmlElement(name = "tmhmm-match")
        private final Set<TMHMMMatch> tmhmmMatches;


        private MatchesType() {
            rpsBlastMatches = null;
            hmmer3MatchesWithSites = null;
            hmmer2Matches = null;
            hmmer3Matches = null;
            mobiDBMatches = null;
            superFamilyHmmer3Matches = null;
            fingerPrintsMatches = null;
            proDomMatches = null;
            patternScanMatches = null;
            profileScanMatches = null;
            phobiusMatches = null;
            coilsMatches = null;
            pantherMatches = null;
            signalPMatches = null;
            tmhmmMatches = null;
        }

        public MatchesType(Set<RPSBlastMatch> rpsBlastMatches,
                           Set<Hmmer3MatchWithSites> hmmer3MatchesWithSites,
                           Set<Hmmer2Match> hmmer2Matches,
                           Set<Hmmer3Match> hmmer3Matches,
                           Set<MobiDBMatch> mobiDBMatches,
                           Set<SuperFamilyHmmer3Match> superFamilyHmmer3Matches,
                           Set<FingerPrintsMatch> fingerPrintsMatches,
                           Set<BlastProDomMatch> proDomMatches,
                           Set<PatternScanMatch> patternScanMatches,
                           Set<ProfileScanMatch> profileScanMatches,
                           Set<PhobiusMatch> phobiusMatches,
                           Set<CoilsMatch> coilsMatches,
                           Set<PantherMatch> pantherMatches,
                           Set<SignalPMatch> signalPMatches,
                           Set<TMHMMMatch> tmhmmMatches) {
            this.rpsBlastMatches = rpsBlastMatches;
            this.hmmer3MatchesWithSites = hmmer3MatchesWithSites;
            this.hmmer2Matches = hmmer2Matches;
            this.hmmer3Matches = hmmer3Matches;
            this.mobiDBMatches = mobiDBMatches;
            this.superFamilyHmmer3Matches = superFamilyHmmer3Matches;
            this.fingerPrintsMatches = fingerPrintsMatches;
            this.proDomMatches = proDomMatches;
            this.patternScanMatches = patternScanMatches;
            this.profileScanMatches = profileScanMatches;
            this.phobiusMatches = phobiusMatches;
            this.coilsMatches = coilsMatches;
            this.pantherMatches = pantherMatches;
            this.signalPMatches = signalPMatches;
            this.tmhmmMatches = tmhmmMatches;
        }

        public Set<RPSBlastMatch> getRpsBlastMatches() {
            return (rpsBlastMatches == null ? Collections.<RPSBlastMatch>emptySet() : rpsBlastMatches);
        }

        public Set<Hmmer3MatchWithSites> getHmmer3MatchesWithSites() {
            return (hmmer3MatchesWithSites == null ? Collections.<Hmmer3MatchWithSites>emptySet() : hmmer3MatchesWithSites);
        }

        public Set<Hmmer2Match> getHmmer2Matches() {
            return (hmmer2Matches == null ? Collections.<Hmmer2Match>emptySet() : hmmer2Matches);
        }

        public Set<Hmmer3Match> getHmmer3Matches() {
            return (hmmer3Matches == null ? Collections.<Hmmer3Match>emptySet() : hmmer3Matches);
        }
        public Set<MobiDBMatch> getMobiDBMatches() {
            return (mobiDBMatches == null ? Collections.<MobiDBMatch>emptySet() : mobiDBMatches);
        }

        public Set<SuperFamilyHmmer3Match> getSuperFamilyHmmer3Matches() {
            return (superFamilyHmmer3Matches == null ? Collections.<SuperFamilyHmmer3Match>emptySet() : superFamilyHmmer3Matches);
        }

        public Set<FingerPrintsMatch> getFingerPrintsMatches() {
            return (fingerPrintsMatches == null ? Collections.<FingerPrintsMatch>emptySet() : fingerPrintsMatches);
        }

        public Set<BlastProDomMatch> getPatternScanMatches() {
            return (proDomMatches == null ? Collections.<BlastProDomMatch>emptySet() : proDomMatches);
        }

        public Set<PatternScanMatch> getProDomMatches() {
            return (patternScanMatches == null ? Collections.<PatternScanMatch>emptySet() : patternScanMatches);
        }

        public Set<ProfileScanMatch> getProfileScanMatches() {
            return (profileScanMatches == null ? Collections.<ProfileScanMatch>emptySet() : profileScanMatches);
        }

        public Set<PhobiusMatch> getPhobiusMatches() {
            return (phobiusMatches == null ? Collections.<PhobiusMatch>emptySet() : phobiusMatches);
        }

        public Set<CoilsMatch> getCoilsMatches() {
            return (coilsMatches == null ? Collections.<CoilsMatch>emptySet() : coilsMatches);
        }

        public Set<PantherMatch> getPantherMatches() {
            return (pantherMatches == null ? Collections.<PantherMatch>emptySet() : pantherMatches);
        }

        public Set<SignalPMatch> getSignalPMatches() {
            return (signalPMatches == null ? Collections.<SignalPMatch>emptySet() : signalPMatches);
        }

        public Set<TMHMMMatch> getTmhmmMatches() {
            return (tmhmmMatches == null ? Collections.<TMHMMMatch>emptySet() : tmhmmMatches);
        }
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
