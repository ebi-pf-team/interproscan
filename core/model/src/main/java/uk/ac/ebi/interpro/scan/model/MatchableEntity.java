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

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an entity, such as a protein sequence or identifier, that can have matches associated with it.
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @version $Id$
 * @since   1.0
 */
@MappedSuperclass   // Chosen not to make this an entity, as it would never be queried upon.
@XmlTransient
public abstract class MatchableEntity implements Serializable {

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<RawMatch> rawMatches = new HashSet<RawMatch>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<FilteredMatch> filteredMatches = new HashSet<FilteredMatch>();

    protected MatchableEntity() { }
    
    protected MatchableEntity(Set<FilteredMatch> filteredMatches) {
        setFilteredMatches(filteredMatches);
    }

    protected MatchableEntity(Set<RawMatch> rawMatches, Set<FilteredMatch> filteredMatches) {
        setRawMatches(rawMatches);
        setFilteredMatches(filteredMatches);        
    }

    /**
     * Returns raw matches
     *
     * @return Matches
     */
    public Set<RawMatch> getRawMatches() {
        return Collections.unmodifiableSet(rawMatches);
    }

    private void setRawMatches(Set<RawMatch> rawMatches) {
        for (RawMatch m : rawMatches)   {
            addRawMatch(m);
        }
    }

    /**
     * Adds and returns raw match to sequence
     *
     * @param  match Match to add
     * @return Raw match to sequence
     * @throws IllegalArgumentException if match is null
     */
    public <T extends RawMatch> T addRawMatch(T match) throws IllegalArgumentException {
        if (match == null) {
            throw new IllegalArgumentException("'Match' must not be null");
        }
        if (match.getSequence() != null) {
            match.getSequence().removeRawMatch(match);
        }
        match.setSequence(this);
        rawMatches.add(match);
        return match;
    }

    /**
     * Removes raw match from sequence
     *
     * @param match Match to remove
     */
    public <T extends RawMatch> void removeRawMatch(T match) {
        rawMatches.remove(match);
        match.setSequence(null);
    }

    /**
     * Returns matches
     *
     * @return matches
     */
    public Set<FilteredMatch> getFilteredMatches() {
        return Collections.unmodifiableSet(filteredMatches);
    }

    private void setFilteredMatches(Set<FilteredMatch> filteredMatches) {
        for (FilteredMatch m : filteredMatches)   {
            addFilteredMatch(m);
        }
    }

    /**
     * Adds and returns filtered match to sequence
     *
     * @param  match Match to add
     * @return Match to sequence
     * @throws IllegalArgumentException if match is null
     */
    public <T extends FilteredMatch> T addFilteredMatch(T match) throws IllegalArgumentException {
        if (match == null) {
            throw new IllegalArgumentException("'Match' must not be null");
        }
        if (match.getSequence() != null) {
            match.getSequence().removeFilteredMatch(match);
        }
        match.setSequence(this);
        filteredMatches.add(match);
        return match;
    }

    /**
     * Removes filtered match from sequence
     *
     * @param match Match to remove
     */
    public <T extends FilteredMatch> void removeFilteredMatch(T match) {
        filteredMatches.remove(match);
        match.setSequence(null);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MatchableEntity))
            return false;
        final MatchableEntity m = (MatchableEntity) o;
        return new EqualsBuilder()
                .append(rawMatches, m.rawMatches)
                .append(filteredMatches, m.filteredMatches)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(19, 55)
                .append(rawMatches)
                .append(filteredMatches)
                .toHashCode();
    }

    @Override public String toString()  {
        return ToStringBuilder.reflectionToString(this);
    }

}
