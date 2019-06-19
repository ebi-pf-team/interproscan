package uk.ac.ebi.interpro.scan.io.sequence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

/**
 * Tests {@link FastaSequenceReader}.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
@ExtendWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class FastaSequenceReaderTest {

    private static final String AATM_ID  = "P12345";
    private static final String AATM_MD5 = "d52d835d42692a6b9a38ef7d2811ec41";

    private static final String BRCA1_ID  = "P38398";
    private static final String BRCA1_MD5 = "e40f752dedf675e2f7c99142ebb2607a";

    // There should be two sequences in this file: AATM (P12345) and BRCA1 (P38398)
    @Resource
    private org.springframework.core.io.Resource aatmBrca1File;

    @Resource
    private org.springframework.core.io.Resource brca1File;

    @Resource
    private org.springframework.core.io.Resource missingIdFile;

    @Resource
    private org.springframework.core.io.Resource missingSequenceFile;

    @Test
    public void readAatmBrca1() throws IOException {
        final Map<String, Protein> proteins = read(aatmBrca1File);
        assertEquals( 2, proteins.size(), "Should be two proteins");
        assertEquals( AATM_ID, proteins.get(AATM_MD5).getCrossReferences().iterator().next().getIdentifier(), "Incorrect MD5");
        assertEquals( AATM_MD5, proteins.get(AATM_MD5).getMd5(), "Incorrect MD5");
        assertEquals( BRCA1_ID, proteins.get(BRCA1_MD5).getCrossReferences().iterator().next().getIdentifier(), "Incorrect MD5");
        assertEquals( BRCA1_MD5, proteins.get(BRCA1_MD5).getMd5(), "Incorrect MD5");
    }

    @Test
    public void readBrca1() throws IOException {
        final Map<String, Protein> proteins = read(brca1File);
        assertEquals(1, proteins.size(), "Should be one protein");
        assertEquals( BRCA1_MD5, proteins.get(BRCA1_MD5).getMd5(), "Incorrect MD5");
    }

    @Test(expected=IllegalStateException.class)
    public void readMissingIdFile() throws IOException {
        read(missingIdFile);
    }

    @Test(expected=IllegalArgumentException.class)
    public void readMissingSequenceFile() throws IOException {
        read(missingSequenceFile);
    }

    private Map<String, Protein> read(org.springframework.core.io.Resource resource) throws IOException {
        final Map<String, Protein> proteins = new HashMap<String, Protein>();
        SequenceReader reader = new FastaSequenceReader(
                new SequenceReader.Listener() {
                    @Override public void mapRecord(SequenceRecord r) {
                        Protein p = new Protein.Builder(r.getSequence()).crossReference(new ProteinXref(r.getId())).build();
                        proteins.put(p.getMd5(), p); // Use MD5 as key
                    }
                }
        );
        reader.read(resource);
        return proteins;
    }

}
