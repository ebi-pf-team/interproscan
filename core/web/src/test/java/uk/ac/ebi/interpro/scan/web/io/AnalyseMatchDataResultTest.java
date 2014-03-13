package uk.ac.ebi.interpro.scan.web.io;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;
import uk.ac.ebi.interpro.scan.web.model.SimpleSignature;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Tests for {@link AnalyseMatchDataResult}
 *
 * @author Matthew Fraser
 * @author Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AnalyseMatchDataResultTest {

    @Autowired
    private AnalyseMatchDataResult instance;

    @Autowired
    org.springframework.core.io.Resource resource1;

    @Autowired
    org.springframework.core.io.Resource incorrectFormattedResource;

    @Test
    public void testParse() throws IOException {
        SimpleProtein protein = instance.parseMatchDataOutput(resource1);
        assertNotNull(protein);
        assertEquals("P38398", protein.getAc());
        assertEquals("BRCA1_HUMAN", protein.getId());
        assertNotNull(protein.getAllEntries());
        assertEquals(9, protein.getAllEntries().size());
    }

    public void testIncorrectFormattedResource() {
        SimpleProtein protein = instance.parseMatchDataOutput(incorrectFormattedResource);
        assertNull("Resulting protein should be NULL!", protein);
    }

    @Test(expected = IllegalStateException.class)
    public void testResourceNull() {
        instance.parseMatchDataOutput(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testReadInMatchDataFromNullResource() {
        instance.readInMatchDataFromResource(null);
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

        SimpleProtein protein = instance.createSimpleProtein(records);
        assertNotNull(protein);
        List<SimpleEntry> entries = protein.getEntries();
        assertNotNull(entries);
        assertTrue(entries.size() == 0);
        List<SimpleSignature> signatures = protein.getUnintegratedSignatures();
        assertNotNull(signatures);
        assertTrue(signatures.size() == 2);
    }
}
