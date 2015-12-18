package uk.ac.ebi.interpro.scan.web.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.io.EntryHierarchy;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link SimpleEntry}
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SimpleEntryTest {

    @Resource
    private EntryHierarchy entryHierarchy;

    private static SimpleEntry entry1;

    // TODO Change to @BeforeClass
    @Before
    public void init() {
        // All tests compare another entry with this one
        entry1 = new SimpleEntry("IPR011992", "EF-hand-like_dom", "EF-hand-like domain", EntryType.DOMAIN, this.entryHierarchy);
        List<SimpleLocation> locations = new ArrayList<SimpleLocation>();
        locations.add(new SimpleLocation(244, 321));
        locations.add(new SimpleLocation(322, 395));
        entry1.setLocations(locations);
        Map<String, SimpleSignature> signatures = new HashMap<String, SimpleSignature>();
        SimpleSignature signature1 = new SimpleSignature("G3DSA:1.10.238.10", "EF-Hand_type", "GENE3D");
        signature1.addLocation(new SimpleLocation(244, 321));
        signature1.addLocation(new SimpleLocation(322, 395));
        signatures.put("G3DSA:1.10.238.10", signature1);
        entry1.setSignatures(signatures);
    }

    @Test
    public void testSimpleEntrySorting() {
        SimpleEntry entry2 = new SimpleEntry("IPR00001", "Test", "Test", EntryType.DOMAIN, this.entryHierarchy);
        List<SimpleLocation> locations = new ArrayList<SimpleLocation>();
        locations.add(new SimpleLocation(44, 121));
        entry2.setLocations(locations);
        assertEquals(1, entry1.compareTo(entry2));

    }
}
