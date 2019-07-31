package uk.ac.ebi.interpro.scan.io.match.hmmer.hmmer2;

import org.apache.log4j.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.ac.ebi.interpro.scan.model.raw.Hmmer2RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.SmartRawMatch;


import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link HmmPfamParser}
 *
 * @author Phil Jones
 * @author Gift Nuka
 *
 * @version $Id$
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class HmmPfamParserTest {
    private static final Logger LOGGER = Logger.getLogger(HmmPfamParserTest.class.getName());

    // SMART
    @Resource
    private HmmPfamParser<SmartRawMatch> smartMatchParser;
    @Resource
    private org.springframework.core.io.Resource smartMatchFile;


    @Test
    public void testHmmPfamParserForSMART() throws IOException {
        Set<RawProtein<SmartRawMatch>> proteins = parse(smartMatchParser, smartMatchFile.getInputStream());
        assertEquals( 1, proteins.size(), "Unexpected number of proteins parsed out.");
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
            assertTrue( proteins.size() > 0, "Must be at least one protein in collection");
        } finally {
            is.close();
        }
        return proteins;
    }


}
