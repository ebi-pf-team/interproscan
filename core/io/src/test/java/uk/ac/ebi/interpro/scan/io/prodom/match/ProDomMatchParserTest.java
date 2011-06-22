package uk.ac.ebi.interpro.scan.io.prodom.match;

import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.io.match.MatchParser;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
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
public class ProDomMatchParserTest extends TestCase {

    @Test
    public void testParse() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("data/prodom/prodom_output.txt");
        MatchParser<ProDomRawMatch> parser = new ProDomMatchParser("2006.1");
        Set<RawProtein<ProDomRawMatch>> proteins = parser.parse(is);
        assertNotNull(proteins);
        assertTrue(proteins.size() == 3);

        for (RawProtein<ProDomRawMatch> protein : proteins) {
            assertNotNull(protein.getMatches());
        }
    }
}
