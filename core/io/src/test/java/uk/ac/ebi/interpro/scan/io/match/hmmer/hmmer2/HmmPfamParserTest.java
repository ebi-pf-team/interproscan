package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SmartRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.TigrFamHmmer2RawMatch;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests for {@link HmmPfamParser}
 *
 * @author Phil Jones
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class HmmPfamParserTest {
    private static final Logger LOGGER = Logger.getLogger(HmmPfamParserTest.class.getName());

    // TIGRfam
    @Resource
    private HmmPfamParser<TigrFamHmmer2RawMatch> tigrMatchParser;
    @Resource
    private org.springframework.core.io.Resource tigrMatchFile;

    // SMART
    @Resource
    private HmmPfamParser<SmartRawMatch> smartMatchParser;
    @Resource
    private org.springframework.core.io.Resource smartMatchFile;

    @Test
    public void testHmmPfamParserForTIGR() throws IOException {
        Set<RawProtein<TigrFamHmmer2RawMatch>> proteins = parse(tigrMatchParser, tigrMatchFile.getInputStream());
        assertEquals("Unexpected number of proteins parsed out.", 840, proteins.size());
        for (RawProtein<TigrFamHmmer2RawMatch> p : proteins) {
            assertNotNull(p);
            assertNotNull(p.getMatches());
            assertTrue(p.getMatches().size() > 0);
            for (TigrFamHmmer2RawMatch m : p.getMatches()) {
                assertNotNull(m);
            }
        }
    }

    @Test
    public void testHmmPfamParserForSMART() throws IOException {
        Set<RawProtein<SmartRawMatch>> proteins = parse(smartMatchParser, smartMatchFile.getInputStream());
        assertEquals("Unexpected number of proteins parsed out.", 1, proteins.size());
        for (RawProtein<SmartRawMatch> p : proteins) {
            assertNotNull(p);
            assertNotNull(p.getMatches());
            assertEquals(11, p.getMatches().size());
            for (SmartRawMatch m : p.getMatches()) {
                assertNotNull(m);
                LOGGER.warn(m);
            }
        }
    }

    private <T extends Hmmer2RawMatch> Set<RawProtein<T>> parse(HmmPfamParser<T> parser,
                                                                InputStream is)
            throws IOException {
        Set<RawProtein<T>> proteins = null;
        try {
            proteins = parser.parse(is);
            assertTrue("Must be at least one protein in collection", proteins.size() > 0);
        } finally {
            is.close();
        }
        return proteins;
    }


}
