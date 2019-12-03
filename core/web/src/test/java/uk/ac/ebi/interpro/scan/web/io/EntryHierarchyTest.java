package uk.ac.ebi.interpro.scan.web.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.interpro.scan.web.model.EntryHierarchyData;

import javax.annotation.Resource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link uk.ac.ebi.interpro.scan.web.io.EntryHierarchy}
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class EntryHierarchyTest {

    @Resource
    private EntryHierarchy entryHierarchy;

    @Test //(expected = UnsupportedOperationException.class)
    public void testGetEntryColoursMap() {
        assertThrows(UnsupportedOperationException.class, () -> {
            Map<String, Integer> entryColoursMap = entryHierarchy.getEntryColourMap();
            assertNotNull(entryColoursMap);
            assertEquals(2, entryColoursMap.size());
            assertFalse(entryColoursMap.containsKey("invalid"));
            entryColoursMap.put("IPR999999", 1);
        });

    }

    @Test
    public void testGetEntryColour() {
        assertEquals(45, entryHierarchy.getEntryColour("IPR011987"));
        assertEquals(44, entryHierarchy.getEntryColour("IPR011986"));
        assertEquals(-1, entryHierarchy.getEntryColour("invalid"));
        assertEquals(-1, entryHierarchy.getEntryColour("doesnotexist"));
    }

    @Test
    public void testGetEntryHierarchyDataMap() {
        Map<String, EntryHierarchyData> data = entryHierarchy.getEntryHierarchyDataMap();
        assertNotNull(data);
        assertEquals(25, data.size());
    }
}
