package uk.ac.ebi.interpro.scan.io.match.domainfinder;

import static junit.framework.Assert.assertTrue;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.apache.log4j.Logger;
import org.junit.Test;


import javax.annotation.Resource;
import java.io.InputStream;
import java.io.IOException;
//import java.io.File;
import java.util.*;

import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.hmmer3.Hmmer3SearchMatchParser;
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

    private static final Logger LOGGER = Logger.getLogger(DomainFinderInputWriterTest.class);


    // Gene3D
    @Resource private Hmmer3SearchMatchParser<Gene3dHmmer3RawMatch> gene3dParser;
    @Resource private org.springframework.core.io.Resource gene3dFile;
    @Resource private org.springframework.core.io.Resource domainFinderInputFileTest;

    @Test
    public void testGene3DParser() throws ParseException, IOException {
        final List<Gene3dHmmer3RawMatch> rawMatches = new ArrayList<Gene3dHmmer3RawMatch>();


        parse(gene3dParser, gene3dFile.getInputStream(),
                new RawMatchListener<Gene3dHmmer3RawMatch>() {
                    public void afterDebug(Gene3dHmmer3RawMatch rawMatch) {
                        rawMatches.add(rawMatch);
                    }
                }
        );
         DomainFinderInputWriter dfiw = new DomainFinderInputWriter(domainFinderInputFileTest.getFile());
        dfiw.writeGene3dRawMatchToSsfFile(rawMatches);

    }


    private <X extends Hmmer3RawMatch> void parse(Hmmer3SearchMatchParser<X> parser, InputStream is, RawMatchListener<X> listener) throws ParseException, IOException {
        try{
            Set<RawProtein<X>> proteins = parser.parse(is);
            assertTrue("Must be at least one protein in collection", proteins.size() > 0);
            for (RawProtein<X> protein : proteins){
                LOGGER.debug("Protein ID: " + protein.getProteinIdentifier());
                for (X rawMatch : protein.getMatches()){

                    LOGGER.debug("\tmodel = " + rawMatch.getModel());
                    LOGGER.debug("\tstart = " + rawMatch.getLocationStart());
                    LOGGER.debug("\tend = "   + rawMatch.getLocationEnd());
                    // Call-back handler here for member DB-specific testing
                    if (listener != null)   {
                        listener.afterDebug(rawMatch);
                    }
                }
            }
        }
        finally {
            is.close();
        }
    }

    // Call-back handler for specific member DB testing
    private interface RawMatchListener<X extends Hmmer3RawMatch> {
        public void afterDebug(X rawMatch);
    }


}
