package uk.ac.ebi.interpro.scan.model.raw.alignment;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link CigarAlignmentEncoder}.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public class CigarAlignmentEncoderTest {

    private static final String GOOD_ALIGNMENT = "QEFHRK-----KDgnfGAD";
    private static final String GOOD_ALIGNMENT_ENCODING = "6M5D2M3I3M";

    // Contains illegal character (".")
    private static final String BAD_ALIGNMENT = "Q=FHRK-----KDgnfGAD";

    @Test
    public void testEncode() {
        AlignmentEncoder encoder = new CigarAlignmentEncoder();
        assertEquals(GOOD_ALIGNMENT_ENCODING, encoder.encode(GOOD_ALIGNMENT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncodeBadAlignment() {
        AlignmentEncoder encoder = new CigarAlignmentEncoder();
        encoder.encode(BAD_ALIGNMENT);
        Assert.fail("IllegalArgumentException expected but not thrown");
    }

    @Test(expected = NullPointerException.class)
    public void testEncodeNullAlignment() {
        AlignmentEncoder encoder = new CigarAlignmentEncoder();
        encoder.encode(null);
        Assert.fail("NullPointerException expected but not thrown");
    }

    @Test
    public void testParser() {
        AlignmentEncoder.Parser parser = new CigarAlignmentEncoder.Parser(GOOD_ALIGNMENT_ENCODING);
        assertEquals("Match count", 11, parser.getMatchCount());
        assertEquals("Insert count", 3, parser.getInsertCount());
        assertEquals("Delete count", 5, parser.getDeleteCount());
    }

}
