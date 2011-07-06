package uk.ac.ebi.interpro.scan.model;

import junit.framework.TestCase;
import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests cases for {@link SuperFamilyHmmer3Match}.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SuperFamilyHmmer3MatchTest extends TestCase {

    /**
     * Tests the equivalent() method works as expected
     */
    @Test
    public void testMatchEquals() {
        SuperFamilyHmmer3Match original = new SuperFamilyHmmer3Match(
                new Signature("PF02310", "B12-binding"),
                3.7e-9,
                new HashSet<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location>(Arrays.asList(
                        new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location(3, 107)
                ))
        );
        SuperFamilyHmmer3Match copy = (SuperFamilyHmmer3Match)SerializationUtils.clone(original);
        assertEquals("Original should equal itself", original, original);
        assertEquals("Original and copy should be equal", original, copy);
        @SuppressWarnings("unchecked") Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> locationsCopy =
                (Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location>) SerializationUtils.
                        clone(new HashSet<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location>(original.getLocations()));
        SuperFamilyHmmer3Match badCopy = new SuperFamilyHmmer3Match(new Signature("1", "A"), 1, locationsCopy);
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
        SuperFamilyHmmer3Match.SuperFamilyHmmer3Location original =
                new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location(3, 107);
        SuperFamilyHmmer3Match.SuperFamilyHmmer3Location copy = (SuperFamilyHmmer3Match.SuperFamilyHmmer3Location)SerializationUtils.clone(original);
        assertEquals("Original should equal itself", original, original);
        assertEquals("Original and copy should be equal", original, copy);
        copy = new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location(1, 2);
        assertFalse("Original and copy should not be equal", original.equals(copy));
    }
}
