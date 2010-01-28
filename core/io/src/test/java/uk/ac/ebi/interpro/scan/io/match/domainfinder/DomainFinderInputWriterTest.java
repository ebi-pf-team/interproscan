package uk.ac.ebi.interpro.scan.io.match.domainfinder;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.junit.Test;


import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.model.raw.*;

/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 22-Jan-2010
 * Time: 13:22:35
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class DomainFinderInputWriterTest {

    @Resource 
    private org.springframework.core.io.Resource domainFinderInputFileTest;

    @Test
    public void testGene3DParser() throws ParseException, IOException {
        final String PROTEIN_ID = "1";
        RawProtein<Gene3dHmmer3RawMatch> protein = new RawProtein<Gene3dHmmer3RawMatch>(PROTEIN_ID);
        // Add matches
        protein.addMatch(new Gene3dHmmer3RawMatch(PROTEIN_ID, "2hxsA00", "Gene3D", "3.0.0", 1, 2, 3.7E-9, 0.035,
                1, 2, "[]", 3.0, 1, 2, 0, 0, 0, 0, 0, "24M2I9M1D9M1D2M2D10M7I42M7D16M5D12M1I24M"));
        DomainFinderInputWriter dfiw = new DomainFinderInputWriter(domainFinderInputFileTest.getFile());
        dfiw.writeGene3dRawMatchToSsfFile(new ArrayList<Gene3dHmmer3RawMatch>(protein.getMatches()));
    }

}
