package uk.ac.ebi.interpro.scan.web.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.model.EntryHierarchyData;

import javax.annotation.Resource;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link uk.ac.ebi.interpro.scan.web.io.EntryHierarchy}
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class EntryHierarchyTest {

    @Resource
    private EntryHierarchy entryHierarchy;

    @Test(expected = UnsupportedOperationException.class)
    public void testGetEntryColoursMap() {
        Map<String, Integer> entryColoursMap = entryHierarchy.getEntryColourMap();
        assertNotNull(entryColoursMap);
        assertEquals(8840, entryColoursMap.size());
        assertFalse(entryColoursMap.containsKey("invalid"));
        entryColoursMap.put("IPR999999", 1);
    }

    @Test
    public void testGetEntryColour() {
        assertEquals(21, entryHierarchy.getEntryColour("IPR011987"));
        assertEquals(20, entryHierarchy.getEntryColour("IPR011986"));
        assertEquals(-1, entryHierarchy.getEntryColour("invalid"));
        assertEquals(-1, entryHierarchy.getEntryColour("doesnotexist"));
    }

    @Test
    public void testGetEntryHierarchyDataMap() {
        Map<String, EntryHierarchyData> data = entryHierarchy.getEntryHierarchyDataMap();
        assertNotNull(data);
        assertEquals(8610, data.size());
    }
}
