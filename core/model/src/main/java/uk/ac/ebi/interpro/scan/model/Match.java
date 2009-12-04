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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.Serializable;
import java.util.*;

/**
 * Represents a signature match on a protein sequence. 
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @version $Id$
 * @since   1.0
 */

@Entity
@Table (name="match_") // TODO: Use "ProteinMatch" or "Matches" instead of "Hit" for table name
@XmlTransient
public abstract class Match<T extends Location> implements Serializable {

    // TODO: IMPACT XML: Add evidence, e.g. "HMMER 2.3.2 (Oct 2003)" [http://www.ebi.ac.uk/seqdb/jira/browse/IBU-894]
    // TODO: See http://www.ebi.ac.uk/seqdb/confluence/x/DYAg#ND3.3StandardXMLformatforallcommondatatypes-SMART

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(cascade=CascadeType.REFRESH, optional = false)
    private Protein protein;

    @ManyToOne(cascade= CascadeType.PERSIST, optional = false)
    private Signature signature;

    @OneToMany(cascade = CascadeType.PERSIST, targetEntity = Location.class)
    private Set<T> locations = new LinkedHashSet<T>();
   
    protected Match() {}

    protected Match(Signature signature, Set<T> locations)  {
        setLocations(locations);
        setSignature(signature);
    }

    @XmlTransient                                                                          
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @XmlTransient
    public Protein getProtein()  {
        return protein;
    }

    public void setProtein(Protein protein) {
        this.protein = protein;

    }

    @XmlElement(required=true)
    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    // TODO: Simplify by forcing locations to be passed into constructor?

    @Transient    
    @XmlJavaTypeAdapter(Location.LocationAdapter.class)
    public Set<T> getLocations() {
        return Collections.unmodifiableSet(locations);
    }

    // Private so can only be set by JAXB, Hibernate ...etc via reflection
    // Doh - changed to public for JPA annotations.
    private void setLocations(Set<T> locations) {
        if (locations.isEmpty())    {
            throw new IllegalArgumentException("There must be at least one location for the match");
        }
        for (T t : locations)    {
            addLocation(t);
        }
    }

    private T addLocation(T location) {
        if (location == null) {
            throw new IllegalArgumentException("'Location' is null");
        }
        if (location.getMatch() != null) {
            // This cast is correct because in sub-classes, for example HmmerMatch, we only allow a single
            // location type, for example HmmerLocation, so the type we remove is the same type we added.
            @SuppressWarnings("unchecked") Match<T> match = location.getMatch();
            match.removeLocation(location);
        }
        location.setMatch(this);
        locations.add(location);
        return location;
    }

    private void removeLocation(T location)   {
        locations.remove(location);
        location.setMatch(null);
    }

    @Override public boolean equals(Object o) {
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

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 51)
                .append(locations)
                .append(signature)
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

    // TODO: Now we're using abstract classes instead of interfaces, can we get rid of the following?
    /**
     *  Ensure sub-classes of Match are represented correctly in XML.
     */
    @XmlTransient
    static final class MatchAdapter extends XmlAdapter<MatchesType, Set<Match>> {

        /** Map Java to XML type */
        @Override public MatchesType marshal(Set<Match> matches) {
            Set<HmmerMatch> hmmerMatches = new LinkedHashSet<HmmerMatch>();
            Set<FingerPrintsMatch> fingerPrintsMatches = new LinkedHashSet<FingerPrintsMatch>();
            Set<BlastProDomMatch> proDomMatches      = new LinkedHashSet<BlastProDomMatch>();
            Set<PatternScanMatch> patternScanMatches = new LinkedHashSet<PatternScanMatch>();
            Set<ProfileScanMatch> profileScanMatches = new LinkedHashSet<ProfileScanMatch>();
            for (Match m : matches) {
                if (m instanceof HmmerMatch) {
                    hmmerMatches.add((HmmerMatch)m);
                }
                else if (m instanceof FingerPrintsMatch) {
                    fingerPrintsMatches.add((FingerPrintsMatch)m);
                }
                else if (m instanceof BlastProDomMatch) {
                    proDomMatches.add((BlastProDomMatch)m);
                }
                else if (m instanceof PatternScanMatch) {
                    patternScanMatches.add((PatternScanMatch)m);
                }
                else if (m instanceof ProfileScanMatch) {
                    profileScanMatches.add((ProfileScanMatch)m);
                }
                else    {
                    throw new IllegalArgumentException("Unrecognised Match class: " + m);
                }
            }
            return new MatchesType(hmmerMatches, fingerPrintsMatches, proDomMatches,
                                           patternScanMatches, profileScanMatches);
        }

        /** Map XML type to Java */
        @Override public Set<Match> unmarshal(MatchesType matchTypes) {
            Set<Match> matches = new HashSet<Match>();
            matches.addAll(matchTypes.getHmmMatches());
            matches.addAll(matchTypes.getFingerPrintsMatches());
            matches.addAll(matchTypes.getProDomMatches());
            matches.addAll(matchTypes.getPatternScanMatches());
            matches.addAll(matchTypes.getProfileScanMatches());
            return matches;
        }

    }

    /**
     * Helper class for MatchAdapter
     */
    private final static class MatchesType {

        @XmlElement(name = "hmm-match")
        private final Set<HmmerMatch> hmmerMatches;

        @XmlElement(name = "fingerprints-match")
        private final Set<FingerPrintsMatch> fingerPrintsMatches;

        @XmlElement(name = "blastprodom-match")
        private final Set<BlastProDomMatch> proDomMatches;

        @XmlElement(name = "patternscan-match")
        private final Set<PatternScanMatch> patternScanMatches;

        @XmlElement(name = "profilescan-match")
        private final Set<ProfileScanMatch> profileScanMatches;

        private MatchesType() {
            hmmerMatches = null;
            fingerPrintsMatches = null;
            proDomMatches       = null;
            patternScanMatches  = null;
            profileScanMatches  = null;
        }

        public MatchesType(Set<HmmerMatch> hmmerMatches,
                                   Set<FingerPrintsMatch> fingerPrintsMatches,
                                   Set<BlastProDomMatch> proDomMatches,
                                   Set<PatternScanMatch> patternScanMatches,
                                   Set<ProfileScanMatch> profileScanMatches) {
            this.hmmerMatches = hmmerMatches;
            this.fingerPrintsMatches = fingerPrintsMatches;
            this.proDomMatches       = proDomMatches;
            this.patternScanMatches  = patternScanMatches;
            this.profileScanMatches  = profileScanMatches;
        }

        public Set<HmmerMatch> getHmmMatches() {
            return (hmmerMatches == null ? Collections.<HmmerMatch>emptySet() : hmmerMatches);
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

    }

}
