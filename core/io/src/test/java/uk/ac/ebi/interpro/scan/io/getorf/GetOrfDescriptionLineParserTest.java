package uk.ac.ebi.interpro.scan.io.getorf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import uk.ac.ebi.interpro.scan.io.sequence.FastaSequenceReader;
import uk.ac.ebi.interpro.scan.io.sequence.SequenceReader;
import uk.ac.ebi.interpro.scan.io.sequence.SequenceRecord;
import uk.ac.ebi.interpro.scan.model.NucleotideSequenceStrand;
import uk.ac.ebi.interpro.scan.model.OpenReadingFrame;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;



/**
 * Tests {@link uk.ac.ebi.interpro.scan.io.getorf.GetOrfDescriptionLineParser}.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @author Gift Nuka
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class GetOrfDescriptionLineParserTest {

    @Resource
    private GetOrfDescriptionLineParser parser;

    @Resource
    private org.springframework.core.io.Resource getOrfTestFile;

    private Set<OpenReadingFrame> orfs;

    @BeforeEach
    public void init() {
        orfs = new HashSet<OpenReadingFrame>();
        orfs.add(new OpenReadingFrame(10, 1100, NucleotideSequenceStrand.SENSE));
        orfs.add(new OpenReadingFrame(50, 400, NucleotideSequenceStrand.ANTISENSE));
        orfs.add(new OpenReadingFrame(1, 230, NucleotideSequenceStrand.SENSE));
        orfs.add(new OpenReadingFrame(10, 230, NucleotideSequenceStrand.ANTISENSE));
        assertNotNull(parser, "Parser shouldn't be NULL before running test.");
        assertNotNull(getOrfTestFile,"GetORF test file doesn't exist!");
    }

    @Test
    public void testParseGetOrfDescLine() throws IOException {
        final Set<String> descriptions = read(getOrfTestFile);
        for (String description : descriptions) {
            OpenReadingFrame orf = parser.createORFFromParsingResult(description);
            assertNotNull( orf, "ORF result shouldn't be NULL!");
            assertTrue( orfs.contains(orf), "ORF (" + orf.getStrand() + ") " + orf.getStart() + ":" + orf.getEnd() + " should be an item of the result set!");
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
