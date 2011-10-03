package uk.ac.ebi.interpro.scan.web.biomart;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.io.ResourceReader;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Tests for {@link uk.ac.ebi.interpro.scan.web.biomart.BioMartQueryResourceReader}
 *
 * @author  Matthew Fraser
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class BioMartQueryResourceReaderTest {

    @Resource
    private ResourceReader<BioMartQueryRecord> reader;

    @Resource
    private org.springframework.core.io.Resource file;

    private final static Collection<BioMartQueryRecord> EXPECTED = Arrays.asList(
            new BioMartQueryRecord("P38398", "BRCA1_HUMAN", "G3DSA:3.30.40.10", "Znf_RING/FYVE/PHD", "GENE3D", 10, 85, "IPR013083", "Zinc finger, RING/FYVE/PHD-type", "Domain"),
            new BioMartQueryRecord("P38398", "BRCA1_HUMAN", "G3DSA:3.40.50.10190", "G3DSA:3.40.50.10190", "GENE3D", 1648, 1754,	"Unintegrated", null, null),
            new BioMartQueryRecord("P38398", "BRCA1_HUMAN", "G3DSA:3.40.50.10190", "G3DSA:3.40.50.10190", "GENE3D", 1756, 1858,	"Unintegrated", null, null),
            new BioMartQueryRecord("P38398", "BRCA1_HUMAN", "PR00493", "BRSTCANCERI", "PRINTS", 55, 71, "IPR002378", "Breast cancer type I susceptibility protein", "Family"),
            new BioMartQueryRecord("P38398", "BRCA1_HUMAN", "PR00493", "BRSTCANCERI", "PRINTS", 760, 780, "IPR002378", "Breast cancer type I susceptibility protein", "Family"),
            new BioMartQueryRecord("P38398", "BRCA1_HUMAN", "PS00518", "ZF_RING_1", "PROSITE patterns", 39, 48, "IPR017907", "Zinc finger, RING-type, conserved site", "Conserved_site"),
            new BioMartQueryRecord("P38398", "BRCA1_HUMAN", "PS50089", "ZF_RING_2", "PROSITE profiles", 24, 65, "IPR001841", "Zinc finger, RING-type", "Domain")
    );

    @Test
    public void testRead() throws IOException {
        Collection<BioMartQueryRecord> result = reader.read(file);
        assertNotNull(result);
        assertEquals(7, result.size());
        assertEquals(EXPECTED, result);
    }
}
