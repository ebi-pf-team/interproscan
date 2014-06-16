package uk.ac.ebi.interpro.scan.io.pirsf.hmmer2;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Run tests for parsing the sf.tb file.
 *
 * @author Matthew Fraser
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class SfTbParserTest extends TestCase {
    private static final Logger LOGGER = Logger.getLogger(SfTbParserTest.class.getName());

    private static final String TEST_FILE_NAME = "data/pirsf/hmmer2/sf.tb";

    private static final String TEST_RELEASE_VERSION = "2.74";

    @Test
    public void testSfTbParser() throws IOException {

        // Setup expected result
        String sfAc1 = "SF000077";
        String sfAc2 = "SF000729";

        Map<String, Integer> expectedResult = new HashMap<String, Integer>();
        expectedResult.put(sfAc1, new Integer(1260));
        expectedResult.put(sfAc2, new Integer(410));

        // Run test method
        SfTbFileParser parser = new SfTbFileParser();
        Resource testFile = new ClassPathResource(TEST_FILE_NAME);
        Map<String, Integer> actualResult = parser.parse(testFile);

        // Compare actual result with expected result
        Assert.assertEquals(2, actualResult.size());
        Assert.assertEquals(expectedResult.get(sfAc1), actualResult.get(sfAc1));
        Assert.assertEquals(expectedResult.get(sfAc2), actualResult.get(sfAc2));
    }


}
