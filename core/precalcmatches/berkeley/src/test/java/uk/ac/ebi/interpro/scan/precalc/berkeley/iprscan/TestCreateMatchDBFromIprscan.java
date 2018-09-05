package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import org.junit.Test;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocationFragment;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests.
 */
public class TestCreateMatchDBFromIprscan {

    @Test
    public void check1() {
        final String fragments = "10-20-C,34-39-N";
        Set<BerkeleyLocationFragment> locationFragments = CreateMatchDBFromIprscan.parseLocationFragments(fragments);
        assertNotNull(locationFragments);
        assertEquals(2, locationFragments.size());
        for (BerkeleyLocationFragment locationFragment : locationFragments) {
            assertTrue(locationFragment.getStart() <= locationFragment.getEnd());
            assertTrue(locationFragment.getBounds().matches("^[S|N|C|NC]$"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void check2() {
        final String fragments = "10-20-S,34-32-S";
        Set<BerkeleyLocationFragment> locationFragments = CreateMatchDBFromIprscan.parseLocationFragments(fragments);
        // Should have thrown an exception
        fail("Test should have failed but didn't");
    }

    @Test
    public void check3() {
        final String fragments = "";
        Set<BerkeleyLocationFragment> locationFragments = CreateMatchDBFromIprscan.parseLocationFragments(fragments);
        assertNotNull(locationFragments);
        assertEquals(0, locationFragments.size());
    }

}
