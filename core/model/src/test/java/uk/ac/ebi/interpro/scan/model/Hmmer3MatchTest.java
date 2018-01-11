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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.lang.SerializationUtils;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests cases for {@link Hmmer3Match}.
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class Hmmer3MatchTest extends AbstractXmlTest<Protein> {

    private static final Log LOGGER = LogFactory.getLog(Hmmer3MatchTest.class);

    private Hmmer3Match originalMatch;
    private HmmerLocation originalLocation;

    @Before
    public void init() {
        originalLocation = 
                new Hmmer3Match.Hmmer3Location(3, 107, 3.0, 3.7e-9, 1, 104, 104, HmmBounds.N_TERMINAL_COMPLETE, 1, 2);
        originalMatch = new Hmmer3Match(
                new Signature("PF02310", "B12-binding"), "PF02310", 0.035, 3.7e-9,
                new HashSet<Hmmer3Match.Hmmer3Location>(Arrays.asList(
                        new Hmmer3Match.Hmmer3Location(3, 107, 3.0, 3.7e-9, 1, 104, 104, HmmBounds.N_TERMINAL_COMPLETE, 1, 2)
                ))
        );
    }

    @Test
    public void testMarshal() throws IOException {
        if (LOGGER.isDebugEnabled()) {
            Protein p = new Protein("MGAAASIQTTVNTL");
            p.addMatch(originalMatch);
            LOGGER.debug(super.marshal(p));
        }
    }

    /**
     * Tests the equivalent() method works as expected
     */
    @Test
    public void testMatchEquals() {
        Hmmer3Match original = originalMatch;
        Hmmer3Match copy = (Hmmer3Match)SerializationUtils.clone(original);
        assertEquals("Original should equal itself", original, original);
        assertEquals("Original and copy should be equal", original, copy);
        @SuppressWarnings("unchecked") Set<Hmmer3Match.Hmmer3Location> locationsCopy =
                (Set<Hmmer3Match.Hmmer3Location>) SerializationUtils.
                        clone(new HashSet<Hmmer3Match.Hmmer3Location>(original.getLocations()));
        Hmmer3Match badCopy = new Hmmer3Match(new Signature("1", "A"), "1", 1, 2, locationsCopy);
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
        HmmerLocation original = originalLocation;
        HmmerLocation copy = (HmmerLocation)SerializationUtils.clone(original);
        assertEquals("Original should equal itself", original, original);
        assertEquals("Original and copy should be equal", original, copy);
        copy = new Hmmer3Match.Hmmer3Location(1, 2, 3, 4, 5, 6, 7, HmmBounds.COMPLETE, 7, 8);
        assertFalse("Original and copy should not be equal", original.equals(copy));
    }
    
}