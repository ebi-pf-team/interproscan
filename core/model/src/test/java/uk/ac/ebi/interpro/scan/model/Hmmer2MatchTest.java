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

import org.junit.Test;
import org.apache.commons.lang.SerializationUtils;
import junit.framework.TestCase;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Tests cases for {@link Hmmer2Match}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public class Hmmer2MatchTest extends TestCase {

    /**
     * Tests the equivalent() method works as expected
     */
    @Test
    public void testMatchEquals() {
        Hmmer2Match original = new Hmmer2Match(
                new Signature("PF02310", "B12-binding"), 0.035, 3.7e-9,
                new HashSet<Hmmer2Match.Hmmer2Location>(Arrays.asList(
                        new Hmmer2Match.Hmmer2Location(3, 107, 3.0, 3.7e-9, 1, 104, HmmBounds.N_TERMINAL_COMPLETE)
                ))
        );
        Hmmer2Match copy = (Hmmer2Match)SerializationUtils.clone(original);
        assertEquals("Original should equal itself", original, original);
        assertEquals("Original and copy should be equal", original, copy);
        @SuppressWarnings("unchecked") Set<Hmmer2Match.Hmmer2Location> locationsCopy =
                (Set<Hmmer2Match.Hmmer2Location>) SerializationUtils.
                        clone(new HashSet<Hmmer2Match.Hmmer2Location>(original.getLocations()));
        Hmmer2Match badCopy = new Hmmer2Match(new Signature("1", "A"), 1, 2, locationsCopy);
        assertFalse("Original and copy should not be equal", original.equals(badCopy));
        // Test sets
        Set<Match> originalSet = new HashSet<Match>();
        Set<Match> copySet     = new HashSet<Match>();
        originalSet.add(original);
        copySet.add(copy);
        assertEquals("Original set should equal itself", originalSet, originalSet);
        assertEquals("Original and copy sets should be equal", originalSet, copySet);
    }

    /**
     * Tests the equivalent() method works as expected
     */
    @Test
    public void testLocationEquals() {
        HmmerLocation original =
                new Hmmer2Match.Hmmer2Location(3, 107, 3.0, 3.7e-9, 1, 104, HmmBounds.N_TERMINAL_COMPLETE);
        HmmerLocation copy = (HmmerLocation)SerializationUtils.clone(original);
        assertEquals("Original should equal itself", original, original);
        assertEquals("Original and copy should be equal", original, copy);
        copy = new Hmmer2Match.Hmmer2Location(1, 2, 3, 4, 5, 6, HmmBounds.COMPLETE);
        assertFalse("Original and copy should not be equal", original.equals(copy));
    }

}