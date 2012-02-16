package uk.ac.ebi.interpro.scan.web.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.model.SimpleStructuralDatabase;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Tests for {@link AnalyseStructuralMatchDataResult}
 *
 * @author  Matthew Fraser
 * @author  Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AnalyseStructuralMatchDataResultTest {

    @Resource
    private AnalyseStructuralMatchDataResult parser;

    @Resource
    org.springframework.core.io.Resource resource;

    @Test
    public void testParse() throws IOException {
        Collection<SimpleStructuralDatabase> structuralDatabases = parser.parseStructuralMatchDataOutput(resource);
        assertNotNull(structuralDatabases);
        assertEquals(4, structuralDatabases.size());

        for(SimpleStructuralDatabase database : structuralDatabases) {
            assertNotNull(database.getDataSource());
            assertNotNull(database.getStructuralMatches());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testResourceNull() {
        parser.parseStructuralMatchDataOutput(null);
    }
}
