package uk.ac.ebi.interpro.scan.model;


import org.apache.commons.lang.SerializationUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests cases for {@link SuperFamilyHmmer3Match}.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SuperFamilyHmmer3MatchTest {

    /**
     * Tests the equivalent() method works as expected
     */
    @Test
    public void testMatchEquals() {
        SuperFamilyHmmer3Match original = new SuperFamilyHmmer3Match(
                new Signature("PF02310", "B12-binding"),
                "PF02310",
                3.7e-9,
                new HashSet<>(Arrays.asList(
                        new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location(new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment(3, 107), 107)
                ))
        );
        SuperFamilyHmmer3Match copy = (SuperFamilyHmmer3Match)SerializationUtils.clone(original);
        assertEquals( original, original, "Original should equal itself");
        assertEquals( original, copy, "Original and copy should be equal");
        @SuppressWarnings("unchecked") Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> locationsCopy =
                (Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location>) SerializationUtils.
                        clone(new HashSet<>(original.getLocations()));
        SuperFamilyHmmer3Match badCopy = new SuperFamilyHmmer3Match(new Signature("1", "A"), "1", 1, locationsCopy);
        assertFalse( original.equals(badCopy), "Original and copy should not be equal");
        // Test sets
        Set<Match> originalSet = new HashSet<>();
        Set<Match> copySet     = new HashSet<>();
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
        SuperFamilyHmmer3Match.SuperFamilyHmmer3Location original =
                new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location(new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment(3, 107), 100);
        SuperFamilyHmmer3Match.SuperFamilyHmmer3Location copy = (SuperFamilyHmmer3Match.SuperFamilyHmmer3Location)SerializationUtils.clone(original);
        assertEquals(original, original, "Original should equal itself");
        assertEquals( original, copy, "Original and copy should be equal");
        copy = new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location(new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment(1, 2), 5);
        assertFalse( original.equals(copy), "Original and copy should not be equal");
    }
}
