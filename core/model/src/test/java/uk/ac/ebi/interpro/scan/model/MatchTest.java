package uk.ac.ebi.interpro.scan.model;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Test cases for {@link Match} implementations
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public class MatchTest extends TestCase {

    // TODO: Write test code for all Raw... and Filtered... classes

    private static final Model MOD1     = new Model("MOD001");
    private static final Signature SIG1 = new Signature("SIG001");

    @Test public void testFilteredProfileScanMatch()    {
        FilteredProfileScanMatch m = new FilteredProfileScanMatch(SIG1);
        ProfileScanLocation l1 = m.addLocation(new ProfileScanLocation(1, 2, 3));
        ProfileScanLocation l2 = m.addLocation(new ProfileScanLocation(4, 5, 6));
        assertEquals(2, m.getLocations().size());
        m.removeLocation(l2);
        assertEquals(1, m.getLocations().size());
        m.removeLocation(l1);
        assertEquals(0, m.getLocations().size());
    }
    
    @Test public void testRawProfileScanMatch()    {
        RawProfileScanMatch m = new RawProfileScanMatch(MOD1);
        ProfileScanLocation l1 = m.addLocation(new ProfileScanLocation(1, 2, 3));
        ProfileScanLocation l2 = m.addLocation(new ProfileScanLocation(4, 5, 6));
        assertEquals(2, m.getLocations().size());
        m.removeLocation(l2);
        assertEquals(1, m.getLocations().size());
        m.removeLocation(l1);
        assertEquals(0, m.getLocations().size());
    }    
    
}
