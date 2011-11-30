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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

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
        orfs.add(new OpenReadingFrame(735, 1112, NucleotideSequenceStrand.SENSE));
        orfs.add(new OpenReadingFrame(49, 465, NucleotideSequenceStrand.ANTISENSE));
        orfs.add(new OpenReadingFrame(1, 231, NucleotideSequenceStrand.SENSE));
    }

    @Test
    public void testExistenceOfInputFile() {
        assertNotNull("GetORF test file doesn't exist!", getOrfTestFile);
    }

    @Test
    public void testParseGetOrfDescLine() throws IOException {
        final Set<String> descriptions = read(getOrfTestFile);
        for (String description : descriptions) {
            OpenReadingFrame orf = parser.parseGetOrfDescriptionLine(description);
            assertNotNull("ORF result shouldn't be NULL!", orf);
            assertTrue("ORF (" + orf.getStrand() + ") " + orf.getStart() + ":" + orf.getEnd() + " should be an item of the result set!", orfs.contains(orf));
        }
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