package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocationFragment;

import java.util.Set;


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
        }
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
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

    @Test
    public void check4() {
        final String fragments = "262-303-NC,109-148-C,421-509-N";
        Set<BerkeleyLocationFragment> locationFragments = CreateMatchDBFromIprscan.parseLocationFragments(fragments);
        assertNotNull(locationFragments);
        assertEquals(3, locationFragments.size());
        for (BerkeleyLocationFragment locationFragment : locationFragments) {
            assertTrue(locationFragment.getStart() <= locationFragment.getEnd());
        }
    }


}
