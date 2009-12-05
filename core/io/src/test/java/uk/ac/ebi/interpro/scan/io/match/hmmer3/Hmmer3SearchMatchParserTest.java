package uk.ac.ebi.interpro.scan.io.match.hmmer3;

import static junit.framework.Assert.assertTrue;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.Ignore;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.IOException;
import java.util.Set;

import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.model.raw.*;

/**
 * Tests for {@link Hmmer3SearchMatchParser}
 *
 * @author  Phil Jones
 * @author  Antony Quinn
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class Hmmer3SearchMatchParserTest {

    private static final Logger LOGGER = Logger.getLogger(Hmmer3SearchMatchParserTest.class);

    // Pfam
    @Resource private Hmmer3SearchMatchParser pfamParser;
    @Resource private org.springframework.core.io.Resource pfamFile;

    // Gene3D
    @Resource private Hmmer3SearchMatchParser gene3dParser;
    @Resource private org.springframework.core.io.Resource gene3dFile;

    @Test
    public void testGene3DParser() throws ParseException, IOException {
        parse(gene3dParser, gene3dFile.getInputStream(),
                new RawMatchListener() {
                    public void afterDebug(Hmmer3RawMatch rawMatch) {
                        LOGGER.debug("\tcigar-alignment = "   + ((Gene3dHmmer3RawMatch)rawMatch).getCigarAlignment());                        
                    }
                }
        );
    }

    @Test
    @Ignore("Currently need to have an entire hmm library in the classpath, so this needs to be switched on manually.  Note that the location / name of the hmm linbrary and the hmm results file should be set in the test context.xml file src/test/resources/uk/ac/ebi/interpro/scan/io/match/data.hmmer23.hmmer3/Hmmer3SearchMatchParserTest-context.xml")
    public void testPfamParser() throws ParseException, IOException {
        parse(pfamParser, pfamFile.getInputStream(), null);
    }

    private void parse(Hmmer3SearchMatchParser<Hmmer3RawMatch> parser, InputStream is, RawMatchListener listener) throws ParseException, IOException {
        try{
            Set<RawProtein<Hmmer3RawMatch>> proteins = parser.parse(is);
            assertTrue("Must be at least one protein in collection", proteins.size() > 0);
            for (RawProtein<Hmmer3RawMatch> protein : proteins){
                LOGGER.debug("Protein ID: " + protein.getProteinIdentifier());
                for (RawMatch rawMatch : protein.getMatches()){
                    Hmmer3RawMatch hmmer3RawMatch = (Hmmer3RawMatch) rawMatch;
                    LOGGER.debug("\tmodel = " + hmmer3RawMatch.getModel());
                    LOGGER.debug("\tstart = " + hmmer3RawMatch.getLocationStart());
                    LOGGER.debug("\tend = "   + hmmer3RawMatch.getLocationEnd());
                    // Call-back handler here for member DB-specific testing
                    if (listener != null)   {
                        listener.afterDebug(hmmer3RawMatch);
                    }
                }
            }
        }
        finally {
            is.close();
        }
    }

    // Call-back handler for specific member DB testing
    private interface RawMatchListener {
        public void afterDebug(Hmmer3RawMatch rawMatch);
    }

}
