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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Collections;

/**
 * Represents a filtered protein match.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@XmlTransient
abstract class AbstractFilteredMatch<T extends Location>
        extends AbstractMatch<T>
        implements FilteredMatch<T>, Serializable {
    
    private Signature signature;

    AbstractFilteredMatch() {}

    protected AbstractFilteredMatch(Signature signature)  {
        setSignature(signature);
    }

    protected AbstractFilteredMatch(Signature signature, Set<T> locations)  {
        super(locations);
        setSignature(signature);
    }

    @XmlElement(required=true)
    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    @XmlTransient
    public String getKey() {
        return signature.getKey();
    }

    /**
     *  Ensure sub-classes of AbstractFilteredMatch are represented correctly in XML.
     */
    @XmlTransient
    static final class FilteredMatchAdapter extends XmlAdapter<FilteredMatchesType, Set<FilteredMatch>> {

        /** Map Java to XML type */
        @Override public FilteredMatchesType marshal(Set<FilteredMatch> matches) {
            Set<FilteredHmmMatch> hmmMatches = new LinkedHashSet<FilteredHmmMatch>();
            Set<FilteredFingerPrintsMatch> fingerPrintsMatches = new LinkedHashSet<FilteredFingerPrintsMatch>();
            for (FilteredMatch m : matches) {
                if (m instanceof FilteredHmmMatch) {
                    hmmMatches.add((FilteredHmmMatch)m);
                }
                else {
                    fingerPrintsMatches.add((FilteredFingerPrintsMatch)m);
                }
            }
            return new FilteredMatchesType(hmmMatches, fingerPrintsMatches);
        }

        /** Map XML type to Java */
        @Override public Set<FilteredMatch> unmarshal(FilteredMatchesType matchTypes) {
            int size = matchTypes.getHmmMatches().size() + matchTypes.getFingerPrintsMatches().size();
            Set<FilteredMatch> matches = new HashSet<FilteredMatch>(size);
            for (FilteredMatch m : matchTypes.getHmmMatches()) {
                matches.add(m);
            }
            for (FilteredMatch m : matchTypes.getFingerPrintsMatches()) {
                matches.add(m);
            }
            return matches;
        }

    }

    /**
     * Helper class for MatchAdapter
     */
    private final static class FilteredMatchesType {

        @XmlElement(name = "hmm-match")
        private final Set<FilteredHmmMatch> hmmMatches;

        @XmlElement(name = "fingerprints-match")
        private final Set<FilteredFingerPrintsMatch> fingerPrintsMatches;

        private FilteredMatchesType() {
            hmmMatches          = null;   
            fingerPrintsMatches = null;
        }

        public FilteredMatchesType(Set<FilteredHmmMatch> hmmMatches, Set<FilteredFingerPrintsMatch> fingerPrintsMatches) {
            this.hmmMatches          = hmmMatches;
            this.fingerPrintsMatches = fingerPrintsMatches;
        }

        public Set<FilteredHmmMatch> getHmmMatches() {
            return (hmmMatches == null ? Collections.<FilteredHmmMatch>emptySet() : hmmMatches);
        }

        public Set<FilteredFingerPrintsMatch> getFingerPrintsMatches() {
            return (fingerPrintsMatches == null ? Collections.<FilteredFingerPrintsMatch>emptySet() : fingerPrintsMatches);
        }

    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AbstractFilteredMatch))
            return false;
        final AbstractFilteredMatch m = (AbstractFilteredMatch) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(signature, m.signature)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 51)
                .appendSuper(super.hashCode())
                .append(signature)
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}
