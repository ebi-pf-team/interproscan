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

    private static final String SEQUENCE       = "QEFHRKPQQPHKDGNFGAD";

    private static final String GOOD_ALIGNMENT = "QEFHRK-----KDgnfGAD";
    private static final String GOOD_ALIGNMENT_ENCODING = "6M5D2M3I3M";

    // Contains illegal character ("=")
    private static final String BAD_ALIGNMENT = "Q=FHRK-----KDgnfGAD";
    
    // Contains illegal character ("T")
    private static final String BAD_ALIGNMENT_ENCODING = "6T5D2M3I3M";

    @Test
    public void testDecode() {
        AlignmentEncoder encoder = new CigarAlignmentEncoder();
        assertEquals(GOOD_ALIGNMENT, encoder.decode(SEQUENCE, GOOD_ALIGNMENT_ENCODING, 1, SEQUENCE.length()));
    }

    @Test
    public void testDecodeSubSequence() {
        final int start        = 4;
        final int end          = 14;
        final String sequence  = "QEFHRKPQQPHKDGNFGAD";
        final String alignment =    "HRK-----KDg";
        //                           ^         ^
        //                           4         14
        final String encoding  = "3M5D2M1I";
        AlignmentEncoder encoder = new CigarAlignmentEncoder();
        assertEquals(alignment, encoder.decode(sequence, encoding, start, end));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeBadAlignment() {
        AlignmentEncoder encoder = new CigarAlignmentEncoder();
        encoder.decode(SEQUENCE, BAD_ALIGNMENT_ENCODING, 1, SEQUENCE.length());
        Assert.fail("IllegalArgumentException expected but not thrown");
    }

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
