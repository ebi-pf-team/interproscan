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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.apache.commons.lang.SerializationUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;


/**
 * Tests cases for {@link Hmmer3Match}.
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @author Gift Nuka
 *
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class Hmmer3MatchTest extends AbstractXmlTest<Protein> {

    private static final Log LOGGER = LogFactory.getLog(Hmmer3MatchTest.class);

    private Hmmer3Match originalMatch;
    private HmmerLocation originalLocation;

    @BeforeEach
    public void init() {
        originalLocation =
                new Hmmer3Match.Hmmer3Location(3, 107, 3.0, 3.7e-9, 1, 104, 104, HmmBounds.N_TERMINAL_COMPLETE, 1, 2, false, DCStatus.CONTINUOUS);
        originalMatch = new Hmmer3Match(
                new Signature("PF02310", "B12-binding"), "PF02310", 0.035, 3.7e-9,
                new HashSet<>(Arrays.asList(
                        new Hmmer3Match.Hmmer3Location(3, 107, 3.0, 3.7e-9, 1, 104, 104, HmmBounds.N_TERMINAL_COMPLETE, 1, 2, false, DCStatus.CONTINUOUS)
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
        assertEquals( original, original, "Original should equal itself");
        assertEquals( original, copy, "Original and copy should be equal");
        @SuppressWarnings("unchecked") Set<Hmmer3Match.Hmmer3Location> locationsCopy =
                (Set<Hmmer3Match.Hmmer3Location>) SerializationUtils.
                        clone(new HashSet<>(original.getLocations()));
        Hmmer3Match badCopy = new Hmmer3Match(new Signature("1", "A"), "1", 1, 2, locationsCopy);
        assertFalse(original.equals(badCopy), "Original and copy should not be equal");
        // Test sets
        Set<Match> originalSet = new HashSet<Match>();
        Set<Match> copySet     = new HashSet<Match>();
        originalSet.add(original);
        copySet.add(copy);
        assertEquals( originalSet, originalSet, "Original set should equal itself");
        assertEquals( originalSet, copySet, "Original and copy sets should be equal");
    }

    /**
     * Tests the equivalent() method works as expected
     */
    @Test
    public void testLocationEquals() {
        HmmerLocation original = originalLocation;
        HmmerLocation copy = (HmmerLocation)SerializationUtils.clone(original);
        assertEquals( original, original, "Original should equal itself");
        assertEquals( original, copy, "Original and copy should be equal");
        copy = new Hmmer3Match.Hmmer3Location(1, 2, 3, 4, 5, 6, 7, HmmBounds.COMPLETE, 7, 8, false, DCStatus.CONTINUOUS);
        assertFalse( original.equals(copy), "Original and copy should not be equal");
    }
    
}