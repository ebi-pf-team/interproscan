package uk.ac.ebi.interpro.scan.io.match;

import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.ProDomRawMatch;
import uk.ac.ebi.interpro.scan.io.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * JUnit tests for ProdomParser
 *
 * @author  Phil Jones
 * @version $Id$
 * @since   1.0
 * @see     ProDomMatchParser
 */
public class ProdomMatchParserTest extends TestCase {

    @Test
    public void testParse() throws IOException, ParseException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("data/prodom/prodom_output.txt");
        MatchParser<ProDomRawMatch> parser = new ProDomMatchParser("ProDom", "3.0");
        Set<RawProtein<ProDomRawMatch>> proteins = parser.parse(is);
        assertNotNull(proteins);
    }
}
