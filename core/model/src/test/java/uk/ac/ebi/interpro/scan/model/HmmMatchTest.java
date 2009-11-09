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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import org.apache.commons.lang.SerializationUtils;
import junit.framework.TestCase;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Tests cases for {@link HmmerMatch}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public class HmmMatchTest extends TestCase {

    /**
     * Tests the equals() method works as expected
     */
    @Test public void testEquals() {
        HmmerMatch original = new HmmerMatch(
                new Signature("PF02310", "B12-binding"), 0.035, 3.7e-9,
                new HashSet<HmmerMatch.HmmerLocation>(Arrays.asList(
                        new HmmerMatch.HmmerLocation(3, 107, 3.0, 3.7e-9, 1, 104, HmmerMatch.HmmerLocation.HmmBounds.N_TERMINAL_COMPLETE)
                ))
        );
        HmmerMatch copy = (HmmerMatch)SerializationUtils.clone(original);
        assertEquals("Original should equal itself", original, original);
        assertEquals("Original and copy should be equal", original, copy);
        assertFalse("Original and copy should not be equal", 
                original.equals(
                        new HmmerMatch(new Signature("1", "A"), 1, 2,
                        (Set<HmmerMatch.HmmerLocation>)SerializationUtils.clone(new HashSet<HmmerMatch.HmmerLocation>(original.getLocations())))
                ));
        // Test sets
        Set<Match> originalSet = new HashSet<Match>();
        Set<Match> copySet     = new HashSet<Match>();
        originalSet.add(original);
        copySet.add(copy);
        assertEquals("Original set should equal itself", originalSet, originalSet);
        assertEquals("Original and copy sets should be equal", originalSet, copySet);
    }

}