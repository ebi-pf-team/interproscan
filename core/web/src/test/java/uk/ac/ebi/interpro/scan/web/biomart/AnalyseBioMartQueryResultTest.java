package uk.ac.ebi.interpro.scan.web.biomart;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.ProteinViewController;

import javax.annotation.Resource;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * Tests for {@link AnalyseBioMartQueryResult}
 *
 * @author  Matthew Fraser
 * @author  Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AnalyseBioMartQueryResultTest {

    @Resource
    private AnalyseBioMartQueryResult parser;

    @Resource
    org.springframework.core.io.Resource resource;

    @Test
    public void testParse() throws IOException {
        ProteinViewController.SimpleProtein protein = parser.parseBioMartQueryOutput(resource);
        assertNotNull(protein);
        assertEquals("P38398", protein.getAc());
        assertEquals("BRCA1_HUMAN", protein.getName());
        assertNotNull(protein.getEntries());
        assertEquals(5, protein.getEntries().size());
    }

    @Test(expected = NullPointerException.class)
    public void testResourceNull() {
        parser.parseBioMartQueryOutput(null);
    }
}
