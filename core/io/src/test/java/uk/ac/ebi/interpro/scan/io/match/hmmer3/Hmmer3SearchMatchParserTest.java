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
import java.util.*;

import uk.ac.ebi.interpro.scan.io.ParseException;
import uk.ac.ebi.interpro.scan.io.match.domainfinder.DomainFinderInputWriter;
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
    @Resource private Hmmer3SearchMatchParser<Hmmer3RawMatch> pfamParser;
    @Resource private org.springframework.core.io.Resource pfamFile;

    // Gene3D
    @Resource private Hmmer3SearchMatchParser<Gene3dHmmer3RawMatch> gene3dParser;
    @Resource private org.springframework.core.io.Resource gene3dFile;
     DomainFinderInputWriter dfiw = new DomainFinderInputWriter();

    private String[] expectedAlignments=
            {
                    "HP0834:24M2I9M1D9M1D2M2D10M7I42M7D16M5D12M1I24M",
                    "HP0834:29M1I17M1D2M2D10M9I5M1I22M2D13M2D1M3D17M3D12M1I1M4I23M",
                    "NT01CJ0385:24M2I9M1D9M3D26M7I14M1D15M6D16M4D7M1D4M1I22M",
                    "NT01CJ0385:29M1D17M1D14M9I5M1I18M2D17M2D1M3D17M3I39M"
            };



    @Test
    public void testGene3DParser() throws ParseException, IOException {
        final Set<String> found=new HashSet<String>();
        final List<Gene3dHmmer3RawMatch> rawMatches = new ArrayList<Gene3dHmmer3RawMatch>();


        parse(gene3dParser, gene3dFile.getInputStream(),
                new RawMatchListener<Gene3dHmmer3RawMatch>() {
                    public void afterDebug(Gene3dHmmer3RawMatch rawMatch) {
                        rawMatches.add(rawMatch);
                        LOGGER.debug("\tcigar-alignment = "   + rawMatch.getCigarAlignment());
                        found.add(rawMatch.getSequenceIdentifier()+":"+rawMatch.getCigarAlignment());
                    }
                }
        );
        

        Set<String> expected=new HashSet<String>(Arrays.asList(expectedAlignments));
        assertTrue("Expected alignments not found",expected.equals(found));
    }

    @Test
    @Ignore("Currently need to have an entire hmm library in the classpath, so this needs to be switched on manually.  Note that the location / name of the hmm linbrary and the hmm results file should be set in the test context.xml file src/test/resources/uk/ac/ebi/interpro/scan/io/match/data.hmmer23.hmmer3/Hmmer3SearchMatchParserTest-context.xml")
    public void testPfamParser() throws ParseException, IOException {
        parse(pfamParser, pfamFile.getInputStream(), null);
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
