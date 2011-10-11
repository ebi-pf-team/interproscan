package uk.ac.ebi.interpro.scan.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Tests for {@link ProteinViewController}
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ProteinViewControllerTest {

    @Test
    public void testProtein()    {
        ProteinViewController c = new ProteinViewController();
        c.proteinFeatures("P38398");
    }

    @Test
    public void testSimpleEntrySort() {
        List<ProteinViewController.SimpleEntry> entries = new ArrayList<ProteinViewController.SimpleEntry>();

        ProteinViewController.SimpleEntry entry1 = new ProteinViewController.SimpleEntry("IPR000001", "Kringle", "Name not available", "Domain");
        List<ProteinViewController.SimpleLocation> locations1 = new ArrayList<ProteinViewController.SimpleLocation>();
        ProteinViewController.SimpleLocation location1 = new ProteinViewController.SimpleLocation(55, 66);
        locations1.add(location1);
        entry1.setLocations(locations1);
        entries.add(entry1);

        ProteinViewController.SimpleEntry entry2 = new ProteinViewController.SimpleEntry(null, "Unintegrated", "Name not available", null);
        List<ProteinViewController.SimpleLocation> locations2 = new ArrayList<ProteinViewController.SimpleLocation>();
        ProteinViewController.SimpleLocation location2 = new ProteinViewController.SimpleLocation(33, 44);
        locations2.add(location2);
        entry2.setLocations(locations2);
        entries.add(entry2);

        ProteinViewController.SimpleEntry entry3 = new ProteinViewController.SimpleEntry("IPR000007", "Tubby_C", "Name not available", "Domain");
        List<ProteinViewController.SimpleLocation> locations3 = new ArrayList<ProteinViewController.SimpleLocation>();
        ProteinViewController.SimpleLocation location3 = new ProteinViewController.SimpleLocation(11, 22);
        locations3.add(location3);
        entry3.setLocations(locations3);
        entries.add(entry3);

        // Test the entries sort correctly
        Collections.sort(entries);
        assertEquals(3, entries.size());
        assertEquals("IPR000007", entries.get(0).getAc());
        assertEquals("IPR000001", entries.get(1).getAc());
        assertEquals(null, entries.get(2).getAc());
    }

}
