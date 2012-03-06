package uk.ac.ebi.interpro.scan.io.pirsf;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for PirsfSubfamilyFileParser.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PirsfSubfamilyFileParserTest {
    private Resource testFile = new ClassPathResource("data/pirsf/subfam.out");

    private PirsfSubfamilyFileParser subfamilyFileParser;

    @Before
    public void init() {
        this.subfamilyFileParser = new PirsfSubfamilyFileParser();
    }


    @Test
    public void testParse() throws IOException {
        Map<String, String> resultMap = subfamilyFileParser.parse(testFile);
        assertTrue("Unexpected result. Result map shouldn't be NULL.", resultMap != null);
        assertEquals("Unexpected map size.", 2, resultMap.size());
        //Check map entries
        assertTrue(resultMap.containsKey("PIRSF500165"));
        assertTrue(resultMap.containsKey("PIRSF500166"));
        assertTrue(resultMap.containsValue("PIRSF016158"));
        //
        assertFalse(resultMap.containsKey("PIRSF000000"));
        assertFalse(resultMap.containsValue("PIRSF0000000"));
    }
}