package uk.ac.ebi.interpro.scan.web.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.web.model.SimpleStructuralMatch;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

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
        List<SimpleStructuralMatch> structuralMatches = parser.parseStructuralMatchDataOutput(resource);
        assertNotNull(structuralMatches);
        assertEquals(9, structuralMatches.size());
    }

    @Test(expected = NullPointerException.class)
    public void testResourceNull() {
        parser.parseStructuralMatchDataOutput(null);
    }
}
