package uk.ac.ebi.interpro.scan.io.prints;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 29-Jun-2010
 * Time: 16:16:55
 * To change this template use File | Settings | File Templates.
 */
public class FingerPRINTSHierarchDBParserTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(FingerPRINTSHierarchDBParserTest.class.getName());

    private static final String TEST_FILE = "data/prints/FingerPRINTShierarchy.db";

    @Test
    public void testFingerPRINTSHierarchDBParser() throws IOException {
        FingerPRINTSHierarchyDBParser parser = new FingerPRINTSHierarchyDBParser();
        Resource printsHierarchyDBResource = new ClassPathResource(TEST_FILE);
        Map<String, FingerPRINTSHierarchyDBParser.HierachyDBEntry> results = parser.parse(printsHierarchyDBResource);
        assertNotNull(results);
        // Correct number of records?
        assertEquals(2000, results.size());

        // Check a couple of (very different) entries

        // CHITINBINDNG|PR00451|1e-04|0|*
        FingerPRINTSHierarchyDBParser.HierachyDBEntry entry = results.get("PR00451");
        assertNotNull(entry);
        assertTrue("CHITINBINDNG".equals(entry.getId()));
        assertTrue("PR00451".equals(entry.getAccession()));
        assertTrue(1e-04d == entry.getEvalueCutoff());
        assertTrue(0 == entry.getMinimumMotifCount());
        assertTrue(entry.isDomain());


        // GLUCTRNSPORT|PR00172|1e-04|2|SUGRTRNSPORT,GLUCTRSPORT1,GLUCTRSPORT2,GLUCTRSPORT3,GLUCTRSPORT4,GLUCTRSPORT5
        entry = results.get("PR00172");
        assertNotNull(entry);
        assertTrue("GLUCTRNSPORT".equals(entry.getId()));
        assertTrue("PR00172".equals(entry.getAccession()));
        assertTrue(1e-04d == entry.getEvalueCutoff());
        assertTrue(2 == entry.getMinimumMotifCount());
        assertFalse(entry.isDomain());
        assertEquals(6, entry.getHierarchicalRelations().size());
        assertTrue(entry.getHierarchicalRelations().contains("SUGRTRNSPORT"));
        assertTrue(entry.getHierarchicalRelations().contains("GLUCTRSPORT1"));
        assertTrue(entry.getHierarchicalRelations().contains("GLUCTRSPORT2"));
        assertTrue(entry.getHierarchicalRelations().contains("GLUCTRSPORT3"));
        assertTrue(entry.getHierarchicalRelations().contains("GLUCTRSPORT4"));
        assertTrue(entry.getHierarchicalRelations().contains("GLUCTRSPORT5"));
    }
}
