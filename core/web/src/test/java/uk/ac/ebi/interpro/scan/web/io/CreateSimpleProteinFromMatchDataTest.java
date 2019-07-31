package uk.ac.ebi.interpro.scan.web.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.interpro.scan.web.model.SimpleEntry;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;
import uk.ac.ebi.interpro.scan.web.model.SimpleStructuralDatabase;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link CreateSimpleProteinFromMatchDataImpl}
 *
 * @author Matthew Fraser
 * @author Antony Quinn
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
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
