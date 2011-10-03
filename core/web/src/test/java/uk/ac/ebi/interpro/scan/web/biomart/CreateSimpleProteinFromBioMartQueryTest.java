package uk.ac.ebi.interpro.scan.web.biomart;

import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.ProteinViewController.*;

import static junit.framework.Assert.assertNotNull;

/**
* Tests for {@link CreateSimpleProteinFromBioMartQuery}
*
* @author  Matthew Fraser
* @version $Id$
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Ignore
public class CreateSimpleProteinFromBioMartQueryTest {

    String proteinAc;
    AnalyseBioMartQueryResult analyser;
    CreateSimpleProteinFromBioMartQuery proteinFromQuery = new CreateSimpleProteinFromBioMartQuery(proteinAc, analyser);

    @Test
    public void parseBioMartQueryTest() {
        this.proteinAc = "P38398";
        SimpleProtein protein = proteinFromQuery.sendBioMartQuery();

        assertNotNull(protein);
    }
}