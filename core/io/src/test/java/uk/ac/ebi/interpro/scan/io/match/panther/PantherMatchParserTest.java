package uk.ac.ebi.interpro.scan.io.match.panther;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.raw.PantherRawMatch;

/**
 * Tests createMatch method of PantherMatchParser class.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PantherMatchParserTest extends TestCase {

    private PantherMatchParser instance;

    @Before
    public void setUp() {
        instance = new PantherMatchParser();
    }


    @Test
    public void testCreateMatch() {
        //Test of real Panther raw match line
        String rawMatchLine = "tr|Q6ZSE3|Q6ZSE3_HUMAN\tPTHR10024:SF2\tGB DEF: HYPOTHETICAL PROTEIN FLJ45597\t2.3e-141\t480.5\t1-341\t1-341\t1-341\t341";
        PantherRawMatch result = instance.createMatch(rawMatchLine);
        assertNotNull("CreateMatch method returned a NULL value!", result);
        assertEquals("tr|Q6ZSE3|Q6ZSE3_HUMAN", result.getSequenceIdentifier());
        assertEquals("PTHR10024:SF2", result.getModelId());
        assertEquals("GB DEF: HYPOTHETICAL PROTEIN FLJ45597", result.getFamilyName());
        assertEquals(new Double("2.3e-141"), result.getEvalue());
        assertEquals(new Double("480.5"), result.getScore());
        assertEquals(1, result.getHmmStart());
        assertEquals(341, result.getHmmEnd());
        assertEquals(341, result.getHmmLength());
        assertEquals(1, result.getLocationStart());
        assertEquals(341, result.getLocationEnd());
        assertEquals(1, result.getEnvelopeStart());
        assertEquals(341, result.getEnvelopeEnd());
        //location start, end is missing
        rawMatchLine = "tr|Q6ZSE3|Q6ZSE3_HUMAN\tPTHR10024:SF2\tGB DEF: HYPOTHETICAL PROTEIN FLJ45597\t2.3e-141\t480.5";
        assertNull("Result of createMatch method should be NULL!", instance.createMatch(rawMatchLine));
        //
        assertNull("Result of createMatch method should be NULL!", instance.createMatch(null));
        //
        assertNull("Result of createMatch method should be NULL!", instance.createMatch(""));
    }
}