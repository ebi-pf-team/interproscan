package uk.ac.ebi.interpro.scan.io.pirsf;

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
 * Run tests for parsing the pirsf.dat file.
 *
 * @author Matthew Fraser
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PirsfDatParserTest extends TestCase {
    private static final Logger LOGGER = Logger.getLogger(PirsfDatParserTest.class.getName());

    private static final String TEST_FILE_NAME = "data/pirsf/pirsf.dat";

    private static final String TEST_RELEASE_VERSION = "274";

    @Test
    public void testPirsfDatParser() throws IOException {

        // Setup expected result
        String modelAc1 = "PIRSF000077";
        PirsfDatRecord pirsfDat1 = new PirsfDatRecord(
                modelAc1,
                "Thioredoxin",
                new String[] {"110.136452241715", "9.11541109440914", "20.3", "167.482261208577", "57.6586203540026"},
                "No");

        String modelAc2 = "PIRSF000729";
        PirsfDatRecord pirsfDat2 = new PirsfDatRecord(
                modelAc2,
                "Glutamate 5-kinase",
                new String[] {"358.270731707317", "40.8471702485214", "47.1", "486.230487804878", "168.540809659098"},
                "Yes");

        Map<String, PirsfDatRecord> expectedResult = new HashMap<String, PirsfDatRecord>();
        expectedResult.put(modelAc1, pirsfDat1);
        expectedResult.put(modelAc2, pirsfDat2);

        // Run test method
        PirsfDatFileParser parser = new PirsfDatFileParser();
        Resource testFile = new ClassPathResource(TEST_FILE_NAME);
        Map<String, PirsfDatRecord> actualResult = parser.parse(testFile);

        // Compare actual result with expected result
        Assert.assertEquals(2, actualResult.size());
        Assert.assertEquals(expectedResult.get(modelAc1), actualResult.get(modelAc1));
        Assert.assertEquals(expectedResult.get(modelAc2), actualResult.get(modelAc2));       
    }


}
