package uk.ac.ebi.interpro.scan.io.superfamily.match;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SuperFamilyHmmer3RawMatch;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

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
        SuperFamilyHmmer3MatchParser parser = new SuperFamilyHmmer3MatchParser();
        InputStream inputStream = new FileInputStream(TEST_FILE_NAME);
        Set<RawProtein<SuperFamilyHmmer3RawMatch>> actualResult = parser.parse(inputStream);

        // Compare actual result with expected result
        Assert.assertEquals(5, actualResult.size());

        for (RawProtein<SuperFamilyHmmer3RawMatch> rawProtein: actualResult) {
            int numMatches = rawProtein.getMatches().size();
            try {
                int proteinId = Integer.parseInt(rawProtein.getProteinIdentifier());
                switch (proteinId) {
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
                fail("Protein Id " + rawProtein.getProteinIdentifier() + " cannot be parsed as an integer");
            }
        }
    }

}
