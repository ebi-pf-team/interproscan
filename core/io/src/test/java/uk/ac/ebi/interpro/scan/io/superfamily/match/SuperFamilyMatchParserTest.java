package uk.ac.ebi.interpro.scan.io.superfamily.match;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SuperFamilyRawMatch;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Test the SuperFamily binary output file parser.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0
 */
public class SuperFamilyMatchParserTest extends TestCase {
    private static final Logger LOGGER = Logger.getLogger(SuperFamilyMatchParserTest.class.getName());

    private static final String TEST_FILE_NAME = "src/test/resources/data/superfamily/perlBinaryOutput.ass";

    @Test
    public void testSuperFamilyMatchParser() throws IOException {

        LOGGER.warn("Note that some inputs are deliberately wrong, so errors/warnings may be thrown by this test! " +
                "Does it pass?");

        // Run test method
        SuperFamilyMatchParser parser = new SuperFamilyMatchParser();
        InputStream inputStream = new FileInputStream(TEST_FILE_NAME);
        Map<String, RawProtein<SuperFamilyRawMatch>> actualResult = parser.parse(inputStream);

        // Compare actual result with expected result
        Assert.assertEquals(5, actualResult.size());

        for (String proteinId : actualResult.keySet()) {
            RawProtein<SuperFamilyRawMatch> rawProtein = actualResult.get(proteinId);
            int numMatches = rawProtein.getMatches().size();
            try {
                int proteinIntId = Integer.parseInt(proteinId);
                switch (proteinIntId) {
                    case 1:
                        Assert.assertEquals(4, numMatches);
                        break;
                    case 2:
                        Assert.assertEquals(7, numMatches);
                        break;
                    case 4:
                        Assert.assertEquals(3, numMatches);
                        break;
                    case 5:
                        Assert.assertEquals(1, numMatches);
                        break;
                    case 6:
                        Assert.assertEquals(3, numMatches);
                        break;
                    default:
                        fail("Unexpected result - " + numMatches + " found for protein " + proteinId);
                }
            }
            catch(NumberFormatException e) {
                fail("Protein Id " + proteinId + " cannot be parsed as an integer");
            }
        }
    }

}
