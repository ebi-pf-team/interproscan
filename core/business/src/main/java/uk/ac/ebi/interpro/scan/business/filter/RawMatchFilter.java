/*
 * Copyright 2010 the original author or authors.
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
package uk.ac.ebi.interpro.scan.business.filter;

import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;

import java.util.Set;

/**
 * Represents a raw match filter.
 * Equivalent to post-processing in Onion.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public interface RawMatchFilter<T extends RawMatch> {

    /**
     * Returns filtered raw matches.
     *
     * @param  rawProteins Raw matches
     * @return Filtered matches.
     */
    Set<RawProtein<T>> filter(Set<RawProtein<T>> rawProteins);
    
}
