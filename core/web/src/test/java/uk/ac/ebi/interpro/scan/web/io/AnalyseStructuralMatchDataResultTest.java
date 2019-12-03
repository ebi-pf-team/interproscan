package uk.ac.ebi.interpro.scan.web.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.interpro.scan.web.model.SimpleStructuralDatabase;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link AnalyseStructuralMatchDataResult}
 *
 * @author  Matthew Fraser
 * @author  Antony Quinn
 * @author Gift Nuka
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
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

    @Test //(expected = NullPointerException.class)
    public void testResourceNull() {
        assertThrows(NullPointerException.class, () ->{
            parser.parseStructuralMatchDataOutput(null);
        });


    }
}
