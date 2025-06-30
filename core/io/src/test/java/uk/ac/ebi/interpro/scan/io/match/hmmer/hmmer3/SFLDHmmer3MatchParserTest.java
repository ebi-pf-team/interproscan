package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.ac.ebi.interpro.scan.io.getorf.MatchSiteData;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawProteinSite;
import uk.ac.ebi.interpro.scan.model.raw.SFLDHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.SFLDHmmer3RawSite;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

/**
 * Tests for {@link SFLDHmmer3MatchParser}
 *
 * @author Gift Nuka
 *
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class SFLDHmmer3MatchParserTest {

    private static final Logger LOGGER = LogManager.getLogger(SFLDHmmer3MatchParserTest.class.getName());

    // SFLD
    @Resource
    private SFLDHmmer3MatchParser<SFLDHmmer3RawMatch> sfldParser;

    private static final String SFLD_HIERARCHY_FILE_PATH = "data/sfld/sfld_hierarchy.tsv";

    @Resource
    private org.springframework.core.io.Resource sfldFile;

    @Test
    public void testSFLDParser() throws IOException {

        InputStream is = sfldFile.getInputStream();

        String sfld_hierarchy_flat_file = SFLDHmmer3MatchParserTest.class.getClassLoader().getResource(SFLD_HIERARCHY_FILE_PATH).getFile(); //getResourceAsStream(SFLD_HIERARCHY_FILE_PATH);

        LOGGER.warn("sfld_hierarchy_flat_file: " + sfld_hierarchy_flat_file);
        sfldParser.setSfldHierarchyFilePath(sfld_hierarchy_flat_file);

        MatchSiteData result = sfldParser.parseMatchesAndSites(is);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("result: " + result);
        }

        Set<RawProtein<SFLDHmmer3RawMatch>> rawProteins = result.getRawProteins();
        LOGGER.debug("result: " + rawProteins.toString());
        System.out.println("result: " + rawProteins);
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
        assertEquals(9, matchCount);

        // Check sites for raw proteins
        Set<RawProteinSite> rawProteinSites = result.getRawProteinSites();
        assertNotNull(rawProteinSites);
        assertEquals(0, rawProteinSites.size());
        for (RawProteinSite rawProteinSite : rawProteinSites) {
            Collection<SFLDHmmer3RawSite> s = rawProteinSite.getSites();
            assertNotNull(s);
            assertTrue(s.size() > 0);
        }

    }

}
