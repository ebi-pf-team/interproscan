package uk.ac.ebi.interpro.scan.io.prints;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * Runs tests of the KdatParser class.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */

public class KdatParserTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(KdatParserTest.class.getName());

    private static final String testFileName = "data/prints/prints_10_only.kdat";

    private static final String[] testAccessions = {"PR00439", "PR00305", "PR00916", "PR00159", "PR00551", "PR00352", "PR00003", "PR00353", "PR00512", "PR00513"};

    @Test
    public void testParser() throws IOException {
        URL testFile = KdatParserTest.class.getClassLoader().getResource(testFileName);
        KdatParser parser = new KdatParser();
        Map<String, String> results = parser.parse(testFile.getPath());
        assertEquals(10, results.size());
        for (String testAccession : testAccessions) {
            assertTrue("Accession missing from final Map: " + testAccession, results.keySet().contains(testAccession));
            String printsAbstract = results.get(testAccession);
            assertNotNull(printsAbstract);
            assertNotNull(printsAbstract);
            assertTrue(printsAbstract.length() > 0);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Accession: " + testAccession);
                LOGGER.debug("Abstract: " + printsAbstract);
            }
        }
    }
}
