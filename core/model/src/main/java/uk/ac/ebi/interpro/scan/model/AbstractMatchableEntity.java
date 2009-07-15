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
 * Represents a sequence that can have matches.
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @version $Id: AbstractMatchableEntity.java,v 1.3 2009/07/10 13:24:41 aquinn Exp $
 * @since   1.0
 */
@XmlTransient
@MappedSuperclass   // Chosen not to make this an entity, as it would never be queried upon.
abstract class AbstractMatchableEntity implements MatchableEntity, Serializable {

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<RawMatch> rawMatches      = new HashSet<RawMatch>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<FilteredMatch> filteredMatches = new HashSet<FilteredMatch>();

    public Set<RawMatch> getRawMatches() {
        return Collections.unmodifiableSet(rawMatches);
    }    

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

    public <T extends RawMatch> void removeRawMatch(T match) {
        rawMatches.remove(match);
        match.setSequence(null);
    }

    public Set<FilteredMatch> getFilteredMatches() {
        return Collections.unmodifiableSet(filteredMatches);
    }    

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

    public <T extends FilteredMatch> void removeFilteredMatch(T match) {
        filteredMatches.remove(match);
        match.setSequence(null);
    }    

}
