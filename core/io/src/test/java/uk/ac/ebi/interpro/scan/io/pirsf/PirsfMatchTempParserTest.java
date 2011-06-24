package uk.ac.ebi.interpro.scan.io.pirsf;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.util.*;

/**
 * Test the PIRSF raw matches temporary file parser.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0
 */
public class PirsfMatchTempParserTest extends TestCase {
    private static final Logger LOGGER = Logger.getLogger(PirsfMatchTempParserTest.class.getName());

    private static final String TEST_FILE_NAME = "src/test/resources/data/pirsf/matchesTemp.out";

    private static final String TEST_RELEASE_VERSION = "2.74";

    @Test
    public void testPirsfMatchTempParser() throws IOException {

        LOGGER.warn("Note that some inputs are deliberately wrong, so errors/warnings may be thrown by this test! " +
                "Does it pass?");
        
        // Setup expected result
        String proteinId = "1";
        String modelId = "PIRSF001500";

        // Run test method
        PirsfMatchTempParser parser = new PirsfMatchTempParser();
        Set<RawProtein<PIRSFHmmer2RawMatch>> actualResult = parser.parse(TEST_FILE_NAME);

        // Compare actual result with expected result
        Assert.assertEquals(1, actualResult.size());

        Iterator<RawProtein<PIRSFHmmer2RawMatch>> i = actualResult.iterator();
        RawProtein<PIRSFHmmer2RawMatch> rawProtein = i.next();

        Collection<PIRSFHmmer2RawMatch> rawMatches = rawProtein.getMatches();
        Assert.assertEquals(1, rawMatches.size());
        Iterator<PIRSFHmmer2RawMatch> j = rawMatches.iterator();
        PIRSFHmmer2RawMatch rawMatch = j.next();

        Assert.assertEquals(proteinId, rawProtein.getProteinIdentifier());
        Assert.assertEquals(modelId, rawMatch.getModelId());
    }

}
