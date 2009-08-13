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

/**
 * Tests cases for {@link HmmLocation}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public class HmmLocationTest extends TestCase {

    /**
     * Tests the equals() method works as expected
     */
    @Test public void testEquals() {
        HmmLocation original = new HmmLocation(3, 107, 3.0, 3.7e-9, 1, 104, HmmLocation.HmmBounds.N_TERMINAL_COMPLETE);
        HmmLocation copy = (HmmLocation)SerializationUtils.clone(original);
        // Original should equal itself
        assertEquals(original, original);
        // Original and copy should be equal
        assertEquals(original, copy);
        // Original and copy should not be equal
        copy = new HmmLocation(1, 2, 3, 4, 5, 6, HmmLocation.HmmBounds.COMPLETE);
        assertFalse("Original and copy should not be equal", original.equals(copy));
    }
    
}