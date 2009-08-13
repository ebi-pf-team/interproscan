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

/**
 * Tests cases for {@link FilteredHmmMatch}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public class FilteredHmmMatchTest extends TestCase {

    /**
     * Tests the equals() method works as expected
     */
    @Test public void testEquals() {
        FilteredHmmMatch original = new FilteredHmmMatch(new Signature("PF02310", "B12-binding"), 0.035, 3.7e-9);
        FilteredHmmMatch copy = (FilteredHmmMatch)SerializationUtils.clone(original);
        // Original should equal itself
        assertEquals(original, original);
        // Original and copy should be equal
        assertEquals(original, copy);
        // Original and copy should not be equal
        assertFalse("Original and copy should not be equal", original.equals(new FilteredHmmMatch(new Signature("1", "A"), 1, 2)));
        // Original and copy should not be equal
        HmmLocation location =
            original.addLocation(new HmmLocation(3, 107, 3.0, 3.7e-9, 1, 104, HmmLocation.HmmBounds.N_TERMINAL_COMPLETE));
        assertFalse("Original and copy should not be equal", original.equals(copy));
        //  Original and copy should be equal again
        copy.addLocation((HmmLocation)SerializationUtils.clone(location));
        assertEquals(original, copy);
        // Test sets
        Set<FilteredMatch> originalSet = new HashSet<FilteredMatch>();
        Set<FilteredMatch> copySet     = new HashSet<FilteredMatch>();
        originalSet.add(original);
        copySet.add(copy);
        assertEquals(originalSet, originalSet);
        assertEquals(originalSet, copySet);        
    }

}