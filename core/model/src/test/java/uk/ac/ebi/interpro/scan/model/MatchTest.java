package uk.ac.ebi.interpro.scan.model;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Test cases for {@link Match} implementations
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @version $Id$
 */
public class MatchTest extends TestCase {

    // TODO: Write test code for all sub-classes of Match

    @Test
    public void testProfileScanMatch() {
        Set<ProfileScanMatch.ProfileScanLocation> locations = new HashSet<ProfileScanMatch.ProfileScanLocation>(Arrays.asList(
                new ProfileScanMatch.ProfileScanLocation(1, 2, 1.1d, "CIGARALIGN"),
                new ProfileScanMatch.ProfileScanLocation(4, 5, 2.2d, "CIGARALIGN")
        ));
        ProfileScanMatch m = new ProfileScanMatch(new Signature("SIG001"), "MOD001", locations);
        assertEquals(2, m.getLocations().size());
    }

}
