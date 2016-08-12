package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.io.getorf.MatchSiteData;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawProteinSite;
import uk.ac.ebi.interpro.scan.model.raw.SFLDHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.SFLDHmmer3RawSite;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests the match parsing of the SFLDHmmer3MatchParser class.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SFLDHmmer3MatchParserTest {

    private static final Logger LOGGER = Logger.getLogger(SFLDHmmer3MatchParserTest.class.getName());

    private static final String TEST_MODEL_FILE = "data/sfld/201607_27/sfld.example.raw.out";

    @javax.annotation.Resource
    private SFLDHmmer3MatchParser<SFLDHmmer3RawMatch> instance;


    @Test
    public void testParseMatches() throws IOException {
        Resource modelFileResource = new ClassPathResource(TEST_MODEL_FILE);
        InputStream is = modelFileResource.getInputStream();

        MatchSiteData result = instance.parseMatchesAndSites(is);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("result: " + result);
        }

        // Check raw proteins
        Set<RawProtein> rawProteins = result.getRawProteins();
        assertNotNull(rawProteins);
        assertEquals(2, rawProteins.size());

        for (final RawProtein<SFLDHmmer3RawMatch> rawProtein : rawProteins) {
            final Collection<SFLDHmmer3RawMatch> rawMatches = rawProtein.getMatches();
            assertNotNull(rawMatches);
            final SFLDHmmer3RawMatch rawMatch = rawMatches.iterator().next();
            final String modelId = rawMatch.getModelId();
            assertTrue(modelId.equals("SFLDS00014") || modelId.equals("SFLDS00024") || modelId.equals("SFLDS00454"));
        }

        // Check sites for raw proteins
        Set<RawProteinSite> rawProteinSites = result.getRawProteinSites();
        assertNotNull(rawProteinSites);
        assertEquals(2, rawProteinSites.size());
        for (RawProteinSite rawProteinSite : rawProteinSites) {
            Collection<SFLDHmmer3RawSite> s = rawProteinSite.getSites();
            assertNotNull(s);
            assertTrue(s.size() > 0);
        }
    }
}