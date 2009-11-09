package uk.ac.ebi.interpro.scan.model;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Test cases for {@link Match} implementations
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public class MatchTest extends TestCase {

    // TODO: Write test code for all sub-classes of Match

    @Test public void testProfileScanMatch()    {
        Set<ProfileScanMatch.ProfileScanLocation> locations = new HashSet<ProfileScanMatch.ProfileScanLocation>(Arrays.asList(
                new ProfileScanMatch.ProfileScanLocation(1, 2, 3),
                new ProfileScanMatch.ProfileScanLocation(4, 5, 6)
        ));
        ProfileScanMatch m = new ProfileScanMatch(new Signature("SIG001"), locations);
        assertEquals(2, m.getLocations().size());
    }
       
}
