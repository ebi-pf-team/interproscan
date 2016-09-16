package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer3;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SFLDHmmer3RawMatch;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.*;

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

        Set<RawProtein<SFLDHmmer3RawMatch>> proteins = sfldParser.parse(is);
        LOGGER.debug("result: " + proteins.toString());
        System.out.println("result: " + proteins);
        assertEquals(5, proteins.size());
        int matchCount = 0;
        for (final RawProtein<SFLDHmmer3RawMatch> rawProtein : proteins) {
            final Collection<SFLDHmmer3RawMatch> rawMatches = rawProtein.getMatches();
            matchCount += rawMatches.size();
            final SFLDHmmer3RawMatch rawMatch = rawMatches.iterator().next();
            final String modelId = rawMatch.getModelId();
            assertTrue(rawMatches.size() > 0);
            assertNotNull(modelId);
        }
        assertEquals(16, matchCount);

    }

}
