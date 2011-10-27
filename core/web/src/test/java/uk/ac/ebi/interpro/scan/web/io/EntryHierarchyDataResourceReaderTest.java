package uk.ac.ebi.interpro.scan.web.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.io.ResourceReader;
import uk.ac.ebi.interpro.scan.web.model.EntryHierarchyData;

import javax.annotation.Resource;
import javax.management.monitor.StringMonitor;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Tests for {@link EntryHierarchyDataResourceReader}
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class EntryHierarchyDataResourceReaderTest {

    @Resource
    private EntryHierarchyDataResourceReader reader;

    @Resource
    private org.springframework.core.io.Resource resource;

    @Test
    public void testRead() throws IOException {
        Map<String, EntryHierarchyData> result = reader.read(resource);
        assertNotNull(result);
        assertEquals(235, result.size());

        EntryHierarchyData ipr000014 = result.get("IPR000014");
        EntryHierarchyData ipr013655 = result.get("IPR013655");
        assertEquals(1, ipr000014.getHierarchyLevel());
        assertEquals(2, ipr013655.getHierarchyLevel());
        assertEquals(4, ipr000014.getEntriesInSameHierarchy().size());
        assertEquals(ipr000014.getEntriesInSameHierarchy(), ipr013655.getEntriesInSameHierarchy());

        EntryHierarchyData ipr001840 = result.get("IPR001840");
        assertEquals(3, ipr001840.getHierarchyLevel());

        EntryHierarchyData ipr000276 = result.get("IPR000276");
        assertEquals(228, ipr000276.getEntriesInSameHierarchy().size());
    }

}
