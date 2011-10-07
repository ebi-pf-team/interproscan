package uk.ac.ebi.interpro.scan.web.biomart;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.ProteinViewController.*;

import javax.annotation.Resource;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
* Tests for {@link CreateSimpleProteinFromMatchData}
*
* @author  Matthew Fraser
* @author  Antony Quinn
* @version $Id$
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CreateSimpleProteinFromMatchDataTest {

    @Resource
    AnalyseMatchDataResult analyser;

    @Test
    public void queryByAccessionTest() throws IOException {
        // TODO: This is really an integration test (relies on web service), so should not be part of usual unit tests - use main() method instead?
        CreateSimpleProteinFromMatchData data = new CreateSimpleProteinFromMatchData(analyser);
        SimpleProtein protein = data.queryByAccession("P38398");
        assertNotNull(protein);
        assertEquals("P38398", protein.getAc());
        assertEquals("BRCA1_HUMAN", protein.getId());
        assertNotNull(protein.getEntries());
        assertEquals(9, protein.getEntries().size());
    }
}