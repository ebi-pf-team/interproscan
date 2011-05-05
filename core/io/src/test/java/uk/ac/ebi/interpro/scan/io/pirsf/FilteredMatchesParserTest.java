package uk.ac.ebi.interpro.scan.io.pirsf;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Run tests for parsing the filteredMatches.out temporary file.
 *
 * @author Matthew Fraser
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class FilteredMatchesParserTest extends TestCase {
    private static final Logger LOGGER = Logger.getLogger(FilteredMatchesParserTest.class.getName());

    private static final String TEST_FILE_NAME = "src/test/resources/data/pirsf/filteredMatches.out";

    private static final String TEST_RELEASE_VERSION = "2.74";

    @Test
    public void testBlastMatchesParser() throws IOException {

        // Setup expected result
        Long proteinId1 = new Long(2);
        Long proteinId2 = new Long(3);

        Set<Long> expectedResult = new HashSet<Long>();
        expectedResult.add(proteinId1);
        expectedResult.add(proteinId2);

        // Run test method
        FilteredMatchesFileParser parser = new FilteredMatchesFileParser();
        Set<Long> actualResult = parser.parse(TEST_FILE_NAME);

        // Compare actual result with expected result
        Assert.assertEquals(2, actualResult.size());
        Assert.assertTrue(expectedResult.contains(proteinId1));
        Assert.assertTrue(expectedResult.contains(proteinId2));
    }


}
