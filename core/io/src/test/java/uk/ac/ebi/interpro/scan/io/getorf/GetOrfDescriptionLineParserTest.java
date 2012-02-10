package uk.ac.ebi.interpro.scan.io.getorf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.io.sequence.FastaSequenceReader;
import uk.ac.ebi.interpro.scan.io.sequence.SequenceReader;
import uk.ac.ebi.interpro.scan.io.sequence.SequenceRecord;
import uk.ac.ebi.interpro.scan.model.NucleotideSequenceStrand;
import uk.ac.ebi.interpro.scan.model.OpenReadingFrame;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

/**
 * Tests {@link uk.ac.ebi.interpro.scan.io.getorf.GetOrfDescriptionLineParser}.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class GetOrfDescriptionLineParserTest {

    @Resource
    private GetOrfDescriptionLineParser parser;

    @Resource
    private org.springframework.core.io.Resource getOrfTestFile;

    private Set<OpenReadingFrame> orfs;

    @Before
    public void init() {
        orfs = new HashSet<OpenReadingFrame>();
        orfs.add(new OpenReadingFrame(10, 1100, NucleotideSequenceStrand.SENSE));
        orfs.add(new OpenReadingFrame(50, 400, NucleotideSequenceStrand.ANTISENSE));
        orfs.add(new OpenReadingFrame(1, 230, NucleotideSequenceStrand.SENSE));
        orfs.add(new OpenReadingFrame(10, 230, NucleotideSequenceStrand.ANTISENSE));
        assertNotNull("Parser shouldn't be NULL before running test.", parser);
        assertNotNull("GetORF test file doesn't exist!", getOrfTestFile);
    }

    @Test
    public void testParseGetOrfDescLine() throws IOException {
        final Set<String> descriptions = read(getOrfTestFile);
        for (String description : descriptions) {
            String[] chunks = parser.parseGetOrfDescriptionLine(description);
            OpenReadingFrame orf = parser.createORFFromParsingResult(chunks);
            assertNotNull("ORF result shouldn't be NULL!", orf);
            assertTrue("ORF (" + orf.getStrand() + ") " + orf.getStart() + ":" + orf.getEnd() + " should be an item of the result set!", orfs.contains(orf));
        }
    }

    @Test
    public void testGetIdentifier() {
        String actual = parser.getIdentifier("");
        assertNull("Return value should be NULL!", actual);
        //
        actual = parser.getIdentifier(null);
        assertNull("Return value should be NULL!", actual);
        //
        actual = parser.getIdentifier("test");
        assertEquals("test", actual);
        //
        actual = parser.getIdentifier("test_test");
        assertEquals("test", actual);
        //
        String expected = "test";
        actual = parser.getIdentifier("test_1 [230 - 10] (REVERSE SENSE)");
        assertNotNull("Actual shouldn't be NULL!", actual);
//        assertEquals("Strings should have the same length!", expected.length(), actual.length());
        assertEquals(expected, actual);
        //
        expected = "reverse translation of P22298";
        //getorf result: reverse_1 [2 - 76] translation of P22298
        actual = parser.getIdentifier("reverse_1 [2 - 76] translation of P22298");
        assertEquals(expected, actual);
        //
        expected = "reverse translation of P22298";
        actual = parser.getIdentifier("reverse_7 [394 - 224] (REVERSE SENSE) translation of P22298");
        assertEquals(expected, actual);
        //
        expected = "seq1";
        //getorf result: seq1_1 [2 - 76]
        actual = parser.getIdentifier("seq1_1 [2 - 76]");
        assertEquals(expected, actual);
    }


    private Set<String> read(org.springframework.core.io.Resource resource) throws IOException {
        final Set<String> descriptions = new HashSet<String>();
        SequenceReader reader = new FastaSequenceReader(
                new SequenceReader.Listener() {
                    @Override
                    public void mapRecord(SequenceRecord r) {
                        descriptions.add(r.getId());
                    }
                }
        );
        reader.read(resource);
        return descriptions;
    }
}