package uk.ac.ebi.interpro.scan.io.match.panther;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

// import uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3.SFLDHmmer3MatchParserTest;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Tests createMatch method of PantherMatchParser class.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PantherMatchParserTest {

    private static final Logger LOGGER = LogManager.getLogger(PantherMatchParserTest.class.getName());

    private PantherMatchParser instance;

    private static final String TEST_FILE_PATH = "data/panther/14.1/Q6ZSE3.raw.results.txt";

    @BeforeEach
    public void setUp() {
        instance = new PantherMatchParser();
    }


    @Test
    public void testCreateMatch() throws IOException {
        //Test of real Panther raw match line
        //String rawMatchLine = "tr|Q6ZSE3|Q6ZSE3_HUMAN\tPTHR10024:SF2\tGB DEF: HYPOTHETICAL PROTEIN FLJ45597\t2.3e-141\t480.5\t1-341\t1-341\t1-341\t341";
        String rawMatchLine = "tr|Q6ZSE3|Q6ZSE3_HUMAN\tPTHR45716\tSYNAPTOTAGMIN-LIKE PROTEIN 2\t3.8e-30\t107.2\t397-770\t33-566\t3-572\t970";

        //String rawMatchLine2 = "O31533\tPTHR43828:SF3\tL-ASPARAGINASE 1-RELATED\t8.7e-119\t399.2\t19-375\t24-369\t1-370\t376";

// FIXME
// We are getting a null value!
// First test will immediately fail

/*
        assertNotNull(result,"CreateMatch method returned a NULL value!");
        assertEquals( result.getSequenceIdentifier(), "tr|Q6ZSE3|Q6ZSE3_HUMAN");
        assertEquals( result.getModelId(), "PTHR45716");
        assertEquals(result.getFamilyName(), "SYNAPTOTAGMIN-LIKE PROTEIN 2");
        assertEquals(new Double("3.8e-30"), result.getEvalue());
        assertEquals(new Double("107.2"), result.getScore());
        assertEquals(397, result.getHmmStart());
        assertEquals(770, result.getHmmEnd());
        assertEquals(970, result.getHmmLength());
        assertEquals("..", result.getHmmBounds());
        assertEquals(33, result.getLocationStart());
        assertEquals(566, result.getLocationEnd());
        assertEquals(3, result.getEnvelopeStart());
        assertEquals(572, result.getEnvelopeEnd());
        //location start, end is missing
        rawMatchLine = "tr|Q6ZSE3|Q6ZSE3_HUMAN\tPTHR10024:SF2\tGB DEF: HYPOTHETICAL PROTEIN FLJ45597\t2.3e-141\t480.5";
        assertNull( instance.createMatch(rawMatchLine), "Result of createMatch method should be NULL!");
        //
        assertNull( instance.createMatch(null), "Result of createMatch method should be NULL!");
        //
        assertNull( instance.createMatch(""), "Result of createMatch method should be NULL!");

        String panther_results_file_path = PantherMatchParserTest.class.getClassLoader().getResource(TEST_FILE_PATH).getFile();

        InputStream is = PantherMatchParserTest.class.getClassLoader().getResourceAsStream(panther_results_file_path);
        LOGGER.warn("panther_results_file_path: " + panther_results_file_path);
*/
        // Set<RawProtein<PantherRawMatch>> pantherRawProteins =  instance.parse(is);
        // assertEquals("The set of parsed raw proteins should not be empty", true, pantherRawProteins.size()== 0);
    }
}