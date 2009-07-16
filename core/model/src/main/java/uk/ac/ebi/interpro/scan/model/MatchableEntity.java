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

import java.io.Serializable;
import java.util.Set;

/**
 * Represents an entity, such as a protein sequence or identifier, that can have matches associated with it.
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @version $Id$
 * @since   1.0
 * @see     Match
 */
public interface MatchableEntity extends Serializable {

    /**
     * Returns raw matches
     *
     * @return Matches
     */
    public Set<RawMatch> getRawMatches();

    /**
     * Adds and returns raw match to sequence
     *
     * @param  match Match to add
     * @return Raw match to sequence
     * @throws IllegalArgumentException if match is null
     */
    public <T extends RawMatch> T addRawMatch(T match) throws IllegalArgumentException;

    /**
     * Removes raw match from sequence
     *
     * @param match Match to remove
     */
    public <T extends RawMatch> void removeRawMatch(T match);

    /**
     * Returns matches
     *
     * @return matches
     */
    public Set<FilteredMatch> getFilteredMatches();

    /**
     * Adds and returns filtered match to sequence
     *
     * @param  match Match to add
     * @return Match to sequence
     * @throws IllegalArgumentException if match is null
     */
    public <T extends FilteredMatch> T addFilteredMatch(T match) throws IllegalArgumentException;

    /**
     * Removes filtered match from sequence
     *
     * @param match Match to remove
     */
    public <T extends FilteredMatch> void removeFilteredMatch(T match);


}
