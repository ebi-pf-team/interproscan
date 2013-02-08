package uk.ac.ebi.interpro.scan.web.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;
import uk.ac.ebi.interpro.scan.web.model.SimpleSignature;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Tests for {@link AnalyseMatchDataResult}
 *
 * @author  Matthew Fraser
 * @author  Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AnalyseMatchDataResultTest {

    @Resource
    private AnalyseMatchDataResult parser;

    @Resource
    org.springframework.core.io.Resource resource;

    @Test
    public void testParse() throws IOException {
        SimpleProtein protein = parser.parseMatchDataOutput(resource);
        assertNotNull(protein);
        assertEquals("P38398", protein.getAc());
        assertEquals("BRCA1_HUMAN", protein.getId());
        assertNotNull(protein.getAllEntries());
        assertEquals(9, protein.getAllEntries().size());
    }

    @Test(expected = NullPointerException.class)
    public void testResourceNull() {
        parser.parseMatchDataOutput(null);
    }

    @Test
    public void testCreateSimpleProtein() {
        List<MatchDataRecord> records = new ArrayList<MatchDataRecord>();
        records.add(new MatchDataRecord("A2AP18",
                "PLCH2_MOUSE",
                "1-phosphatidylinositol 4,5-bisphosphate phosphodiesterase eta-2",
                1501,
                "6C2CA0E2864B327B",
                "PTHR10336:SF21",
                "PTHR10336:SF21",
                "PANTHER",
                29,
                1489,
                0.0,
                "",
                "",
                "",
                "",
                10090,
                "Mus musculus",
                "Mus musculus (Mouse)",
                false));
        records.add(new MatchDataRecord("A2AP18",
                "PLCH2_MOUSE",
                "1-phosphatidylinositol 4,5-bisphosphate phosphodiesterase eta-2",
                1501,
                "6C2CA0E2864B327B",
                "PTHR10336",
                "PTHR10336",
                "PANTHER",
                29,
                1489,
                0.0,
                "",
                "",
                "",
                "",
                10090,
                "Mus musculus",
                "Mus musculus (Mouse)",
                false));

        SimpleProtein protein = parser.createSimpleProtein(records);
        assertNotNull(protein);
        List<SimpleEntry> entries = protein.getEntries();
        assertNotNull(entries);
        assertTrue(entries.size() == 0);
        List<SimpleSignature> signatures = protein.getUnintegratedSignatures();
        assertNotNull(signatures);
        assertTrue(signatures.size() == 2);
    }
}
