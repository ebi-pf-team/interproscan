package uk.ac.ebi.interpro.scan.io.pirsf.hmmer2;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Test class for {@link PirsfBlastResultParser}.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PirsfBlastResultParserTest {

    private static final String TEST_FILE_NAME = "src/test/resources/data/pirsf/hmmer2/blastResult.out";

    private static final String TEST_RELEASE_VERSION = "2.74";

    @Test
    public void testParseBlastResults() throws IOException {

        // Setup expected result
        Map<String, Integer> expectedResults = new HashMap<String, Integer>();
        String key1 = "3-SF000729";
        int value1 = 242;
        String key2 = "4-SF000210";
        int value2 = 207;
        expectedResults.put(key1, value1);
        expectedResults.put(key2, value2);

        PirsfBlastResultParser parser = new PirsfBlastResultParser();
        // Run test method
        Map<String, Integer> actualResults = parser.parseBlastOutputFile(TEST_FILE_NAME);

        // Compare actual result with expected result
        Assert.assertEquals(2, actualResults.size());
        for (Map.Entry<String, Integer> a : actualResults.entrySet()) {
            System.out.println(a.getKey() +  ", " + a.getValue());
        }
        Assert.assertTrue(actualResults.containsKey(key1));
        Assert.assertEquals(value1, (int)actualResults.get(key1));
        Assert.assertTrue(actualResults.containsKey(key2));
        Assert.assertEquals(value2, (int)actualResults.get(key2));

    }
}
