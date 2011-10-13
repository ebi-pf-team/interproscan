package uk.ac.ebi.interpro.scan.web.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.model.SimpleProtein;

import javax.annotation.Resource;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

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
        assertNotNull(protein.getEntries());
        assertEquals(9, protein.getEntries().size());
    }

    @Test(expected = NullPointerException.class)
    public void testResourceNull() {
        parser.parseMatchDataOutput(null);
    }
}
