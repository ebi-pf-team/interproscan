package uk.ac.ebi.interpro.scan.io.pirsf;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Run tests for parsing the blastMatches.out temporary file.
 *
 * @author Matthew Fraser
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class BlastMatchesParserTest extends TestCase {
    private static final Logger LOGGER = Logger.getLogger(BlastMatchesParserTest.class.getName());

    private static final String TEST_FILE_NAME = "src/test/resources/data/pirsf/blastMatches.out";

    private static final String TEST_RELEASE_VERSION = "2.74";

    @Test
    public void testBlastMatchesParser() throws IOException {

        // Setup expected result
        Long proteinId1 = new Long(2);
        Long proteinId2 = new Long(3);

        Map<Long, String> expectedResult = new HashMap<Long, String>();
        expectedResult.put(proteinId1, "PIRSF000729");
        expectedResult.put(proteinId2, "PIRSF000089");

        // Run test method
        BlastMatchesFileParser parser = new BlastMatchesFileParser();
        Map<Long, String> actualResult = parser.parse(TEST_FILE_NAME);

        // Compare actual result with expected result
        Assert.assertEquals(2, actualResult.size());
        Assert.assertEquals(expectedResult.get(proteinId1), actualResult.get(proteinId1));
        Assert.assertEquals(expectedResult.get(proteinId2), actualResult.get(proteinId2));
    }


}
