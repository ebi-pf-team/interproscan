package uk.ac.ebi.interpro.scan.web.biomart;

import org.junit.Test;
import org.junit.runner.RunWith;
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
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AnalyseBioMartQueryResultTest {

    @Resource
    private AnalyseBioMartQueryResult parser;

    @Test
    public void testParse() throws IOException {
        ProteinViewController.SimpleProtein protein = parser.parseBioMartQueryOutput();
        assertNotNull(protein);
        assertEquals("P38398", protein.getAc());
        assertEquals("BRCA1_HUMAN", protein.getName());
        assertNotNull(protein.getEntries());
        assertEquals(5, protein.getEntries().size());
    }

    @Test
    public void testResourceNull() throws IOException {
        ProteinViewController.SimpleProtein protein = null;
        parser.setResource(null);
        try {
            protein = parser.parseBioMartQueryOutput();
            fail("Test should have thrown an exception but did not!");
        }
        catch (NullPointerException e) {
            assertEquals(null, protein);
        }
    }
}
