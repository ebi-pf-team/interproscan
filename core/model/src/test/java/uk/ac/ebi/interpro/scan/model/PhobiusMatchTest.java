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

import junit.framework.TestCase;
import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests cases for {@link PhobiusMatch}.
 *
 * @author  Phil Jones
 * @version $Id$
 * @since   1.0
 */
public class PhobiusMatchTest extends TestCase {

    /**
     * Tests the equals() method works as expected
     */
    @Test public void testEquals() {
        PhobiusMatch original = new PhobiusMatch(
                new Signature("SIGNAL", "Signal Peptide"),

                new HashSet<PhobiusMatch.PhobiusLocation>(Arrays.asList(
                        new PhobiusMatch.PhobiusLocation(3, 107)
                ))
        );
        PhobiusMatch copy = (PhobiusMatch)SerializationUtils.clone(original);
        assertEquals("Original should equal itself", original, original);
        assertEquals("Original and copy should be equal", original, copy);
        assertFalse("Original and copy should not be equal",
                original.equals(
                        new PhobiusMatch(
                new Signature("DOMAIN", "NON CYTOPLASMIC"),

                new HashSet<PhobiusMatch.PhobiusLocation>(Arrays.asList(
                        new PhobiusMatch.PhobiusLocation(3, 107)
                ))
        )));
        // Test sets
        Set<Match> originalSet = new HashSet<Match>();
        Set<Match> copySet     = new HashSet<Match>();
        originalSet.add(original);
        copySet.add(copy);
        assertEquals("Original set should equal itself", originalSet, originalSet);
        assertEquals("Original and copy sets should be equal", originalSet, copySet);
    }

}