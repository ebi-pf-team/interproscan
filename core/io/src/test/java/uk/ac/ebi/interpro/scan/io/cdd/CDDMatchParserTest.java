package uk.ac.ebi.interpro.scan.io.cdd;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.io.getorf.MatchSiteData;
import uk.ac.ebi.interpro.scan.io.match.cdd.CDDMatchParser;
import uk.ac.ebi.interpro.scan.model.raw.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests createMatch method of CDDMatchParser class.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class CDDMatchParserTest {

    private static final Logger LOGGER = Logger.getLogger(CDDMatchParserTest.class.getName());

    private static final String TEST_MODEL_FILE = "data/cdd/cdd.output.txt";

    private CDDMatchParser instance;

    @Before
    public void setUp() {
        instance = new CDDMatchParser();
    }

    @Test
    public void testCreateMatch() throws IOException {
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
        for (final RawProtein<CDDRawMatch> rawProtein : rawProteins) {
            final Collection<CDDRawMatch> rawMatches = rawProtein.getMatches();
            assertNotNull(rawMatches);
            assertEquals(1, rawMatches.size());
            final CDDRawMatch rawMatch = rawMatches.iterator().next();
            final String modelId = rawMatch.getModelId();
            assertTrue(modelId.equals("cd07765") || modelId.equals("cd14735"));
        }

        // Check sites for raw proteins
        Set<RawProteinSite> rawProteinSites = result.getRawProteinSites();
        assertNotNull(rawProteinSites);
        assertEquals(2, rawProteinSites.size());
        for (RawProteinSite rawProteinSite : rawProteinSites) {
            Collection<CDDRawSite> s = rawProteinSite.getSites();
            assertNotNull(s);
            assertTrue(s.size() > 0);
        }
    }
}