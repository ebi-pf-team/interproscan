package uk.ac.ebi.interpro.scan.web.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;
import uk.ac.ebi.interpro.scan.web.model.SimpleStructuralDatabase;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Tests for {@link CreateSimpleProteinFromMatchDataImpl}
 *
 * @author Matthew Fraser
 * @author Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CreateSimpleProteinFromMatchDataTest {

    @Resource
    private AnalyseMatchDataResult matchAnalyser;

    @Resource
    private AnalyseStructuralMatchDataResult structuralMatchAnalyser;

    @Resource
    private String matchDataLocation;

    @Resource
    private String structuralMatchDataLocation;

    @Test
    public void queryByAccessionTest() throws IOException {

        CreateSimpleProteinFromMatchData data = new CreateSimpleProteinFromMatchDataImpl(matchAnalyser,
                structuralMatchAnalyser,
                matchDataLocation,
                structuralMatchDataLocation);

        SimpleProtein protein = data.queryByAccession("P38398");

        assertNotNull(protein);
        assertEquals("P38398", protein.getAc());
        assertEquals("BRCA1_HUMAN", protein.getId());

        List<SimpleEntry> entries = protein.getAllEntries();
        assertNotNull(entries);
        assertEquals(9, entries.size());

        List<SimpleStructuralDatabase> structuralDatabases = protein.getStructuralDatabases();
        assertNotNull(structuralDatabases);
        assertEquals(4, structuralDatabases.size());
        for (SimpleStructuralDatabase database : structuralDatabases) {
            assertNotNull(database);
        }
    }
}
