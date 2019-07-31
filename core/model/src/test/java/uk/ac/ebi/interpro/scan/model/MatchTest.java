package uk.ac.ebi.interpro.scan.model;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Test cases for {@link Match} implementations
 *
 * @author  Antony Quinn
 * @author  Phil Jones
 * @author Gift Nuka
 * @version $Id$
 */
public class MatchTest {

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
