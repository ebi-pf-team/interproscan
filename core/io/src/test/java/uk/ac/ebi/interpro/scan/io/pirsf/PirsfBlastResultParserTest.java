package uk.ac.ebi.interpro.scan.io.pirsf;

import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Test class for {@link PirsfBlastResultParser}.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class PirsfBlastResultParserTest {

    @Test
    public void testParseBlastResultLine() {
        //  testing  realistic Blast output
        String resultLine = "Q97R95\tQ97R95-SF000729\t100.00\t369\t0\t0\t1\t369\t1\t369\t0.0\t709.1";
        String actual = PirsfBlastResultParser.parseBlastResultLine(resultLine);
        String expected = "SF000729";
        assertEquals("The parsed value doesn't match the expected value!", expected, actual);

        //  testing unexpected Blast output
        resultLine = "Q97R95\tQ97R95-SF000729\t100.00\t369";
        actual = PirsfBlastResultParser.parseBlastResultLine(resultLine);
        assertNull("The parsed value doesn't match the expected value!", actual);

        resultLine = "Q97R95\tQ97R95&SF000729\t100.00\t369";
        actual = PirsfBlastResultParser.parseBlastResultLine(resultLine);
        assertNull("The parsed value doesn't match the expected value!", actual);

        resultLine = "Q97R95 Q97R95-SF000729 100.00 369";
        actual = PirsfBlastResultParser.parseBlastResultLine(resultLine);
        assertNull("The parsed value doesn't match the expected value!", actual);

        resultLine = "";
        actual = PirsfBlastResultParser.parseBlastResultLine(resultLine);
        assertNull("The parsed value doesn't match the expected value!", actual);

        resultLine = null;
        actual = PirsfBlastResultParser.parseBlastResultLine(resultLine);
        assertNull("The parsed value doesn't match the expected value!", actual);
    }
}