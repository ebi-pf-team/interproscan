package uk.ac.ebi.interpro.scan.model;

import junit.framework.TestCase;
import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;

/**
 * Tests cases for {@link PhobiusMatch.PhobiusLocation}.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class PhobiusLocationTest extends TestCase {

    /**
     * Tests the equals() method works as expected
     */
    @Test
    public void testEquals() {
        PhobiusMatch.PhobiusLocation original = new PhobiusMatch.PhobiusLocation(3, 107);
        PhobiusMatch.PhobiusLocation copy = (PhobiusMatch.PhobiusLocation) SerializationUtils.clone(original);
        // Original should equal itself
        assertEquals(original, original);
        // Original and copy should be equal
        assertEquals(original, copy);
        // Original and copy should not be equal
        copy = new PhobiusMatch.PhobiusLocation(1, 2);
        assertFalse("Original and copy should not be equal", original.equals(copy));
    }
}
