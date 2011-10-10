package uk.ac.ebi.interpro.scan.web.biomart;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.ProteinViewController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

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
        String proteinAc = "P38398";
        List<ProteinViewController.SimpleStructuralMatch> structuralMatches = parser.parseStructuralMatchDataOutput(resource, proteinAc);
        assertNotNull(structuralMatches);
        assertEquals(9, structuralMatches.size());
    }

    @Test(expected = NullPointerException.class)
    public void testResourceNull() {
        String proteinAc = "P38398";
        parser.parseStructuralMatchDataOutput(null, proteinAc);
    }
}
