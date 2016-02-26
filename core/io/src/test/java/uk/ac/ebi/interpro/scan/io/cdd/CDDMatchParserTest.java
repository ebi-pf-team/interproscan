package uk.ac.ebi.interpro.scan.io.cdd;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.match.cdd.CDDMatchParser;
import uk.ac.ebi.interpro.scan.io.match.panther.PantherMatchParser;
import uk.ac.ebi.interpro.scan.model.raw.CDDRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Tests createMatch method of CDDMatchParser class.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class CDDMatchParserTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(CDDMatchParserTest.class.getName());

    private static final String TEST_MODEL_FILE = "data/cdd/cdd.output.txt";

    private CDDMatchParser instance;

    @Before
    public void setUp() {
        instance = new CDDMatchParser();
    }

    @Test
    public void testCreateMatch() {
        //Test of real CDD raw match line
        String rawMatchLine = "1";
        try {
            Resource modelFileResource = new ClassPathResource(TEST_MODEL_FILE);
            InputStream is = modelFileResource.getInputStream();

            Set<RawProtein<CDDRawMatch>> result = instance.parse(is, modelFileResource.getFilename());
            LOGGER.debug("result: " + result.toString());
            System.out.println("result: " + result);
            assertEquals(2, result.size());
//            assertNotNull("CreateMatch method returned a NULL value!", result);
//            assertEquals("tr|Q6ZSE3|Q6ZSE3_HUMAN", result.getSequenceIdentifier());
//            assertEquals("PTHR10024:SF2", result.getModelId());
//            assertEquals("GB DEF: HYPOTHETICAL PROTEIN FLJ45597", result.getFamilyName());
//            assertEquals(new Double("2.3e-141"), result.getEvalue());
//            assertEquals(new Double("480.5"), result.getScore());
//            assertEquals(1, result.getLocationStart());
//            assertEquals(341, result.getLocationEnd());
//            //location start, end is missing
//            rawMatchLine = "tr|Q6ZSE3|Q6ZSE3_HUMAN\tPTHR10024:SF2\tGB DEF: HYPOTHETICAL PROTEIN FLJ45597\t2.3e-141\t480.5";
//            assertNull("Result of createMatch method should be NULL!", instance.createMatch(rawMatchLine));
//            //
//            assertNull("Result of createMatch method should be NULL!", instance.createMatch(null));
//            //
//            assertNull("Result of createMatch method should be NULL!", instance.createMatch(""));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}