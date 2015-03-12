package uk.ac.ebi.interpro.scan.io.prodom.match;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.model.raw.ProDomRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Test the ProDom result output file parser.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class BlastProDomMatchParserTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(BlastProDomMatchParserTest.class.getName());

    @Test
    public void testParse() throws IOException {

        LOGGER.warn("Note that some inputs are deliberately wrong, so errors/warnings may be thrown by this test! " +
                "Does it pass?");

        InputStream is = getClass().getClassLoader().getResourceAsStream("data/prodom/prodom_output.txt");
        MatchParser<ProDomRawMatch> parser = new BlastProDomMatchParser("2006.1");
        Set<RawProtein<ProDomRawMatch>> proteins = parser.parse(is);
        assertNotNull(proteins);
        assertTrue(proteins.size() == 3);

        for (RawProtein<ProDomRawMatch> protein : proteins) {
            assertNotNull(protein.getMatches());
        }
    }
}
