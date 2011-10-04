package uk.ac.ebi.interpro.scan.web.biomart;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.ProteinViewController.*;

import javax.annotation.Resource;

import java.io.IOException;

import static junit.framework.Assert.assertNotNull;

/**
* Tests for {@link CreateSimpleProteinFromBioMartQuery}
*
* @author  Matthew Fraser
* @author  Antony Quinn
* @version $Id$
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CreateSimpleProteinFromBioMartQueryTest {

    @Resource
    AnalyseBioMartQueryResult analyser;

    @Test
    public void queryByAccessionTest() throws IOException {
        // TODO: This is really an integration test (relies on BioMart), so should not be part of usual unit tests - use main() method instead?
        CreateSimpleProteinFromBioMartQuery biomart = new CreateSimpleProteinFromBioMartQuery(analyser);
        SimpleProtein protein = biomart.queryByAccession("P38398");
        assertNotNull(protein);
    }
}