package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.raw.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link SFLDHmmer3MatchParser}
 *
 * @author Gift Nuka
 *
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SFLDHmmer3MatchParserTest {

    private static final Logger LOGGER = Logger.getLogger(SFLDHmmer3MatchParserTest.class.getName());

    // SFLD
    @Resource
    private SFLDHmmer3MatchParser<SFLDHmmer3RawMatch> sfldParser;

    @Resource
    private org.springframework.core.io.Resource sfldFile;

    @Test
    public void testSFLDParser() throws IOException {

        InputStream is = sfldFile.getInputStream();

        MatchSiteData result = sfldParser.parseMatchesAndSites(is);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("result: " + result);
        }

        Set<RawProtein<SFLDHmmer3RawMatch>> rawProteins = result.getRawProteins();
        LOGGER.debug("result: " + proteins.toString());
        System.out.println("result: " + proteins);
        assertNotNull(rawProteins);
        assertEquals(5, rawProteins.size());
        int matchCount = 0;
        for (final RawProtein<SFLDHmmer3RawMatch> rawProtein : rawProteins) {
            final Collection<SFLDHmmer3RawMatch> rawMatches = rawProtein.getMatches();
            assertNotNull(rawMatches);
            matchCount += rawMatches.size();
            final SFLDHmmer3RawMatch rawMatch = rawMatches.iterator().next();
            final String modelId = rawMatch.getModelId();
            assertTrue(rawMatches.size() > 0);
            assertNotNull(modelId);
        }
        assertEquals(16, matchCount);

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
