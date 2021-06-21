package uk.ac.ebi.interpro.scan.io.match.panther;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        String rawMatchLine = "tr|Q4Q8C9|Q4Q8C9_LEIMA\tPTHR10000\tSF48\tAN11\t210.8\t2e-64\t210.7\t2.2e-64\t4\t265\t2\t259\t1\t259\tSF:PTHR10000:SF48;NAME:Pyridoxal phosphate phosphatase YbhA;";
        PantherRawMatch result = instance.createMatch(rawMatchLine);
        assertNotNull(result,"CreateMatch method returned a NULL value!");
        assertEquals( result.getSequenceIdentifier(), "tr|Q4Q8C9|Q4Q8C9_LEIMA");
        assertEquals( result.getModelId(), "PTHR10000");
        assertEquals(result.getFamilyName(), "PTHR10000");
        assertEquals(new Double("2e-64"), result.getEvalue());
        assertEquals(new Double("210.8"), result.getScore());
        assertEquals(4, result.getHmmStart());
        assertEquals(265, result.getHmmEnd());
        assertEquals(0, result.getHmmLength());
        assertEquals(".]", result.getHmmBounds());
        assertEquals(2, result.getLocationStart());
        assertEquals(259, result.getLocationEnd());
        assertEquals(1, result.getEnvelopeStart());
        assertEquals(259, result.getEnvelopeEnd());
    }
}