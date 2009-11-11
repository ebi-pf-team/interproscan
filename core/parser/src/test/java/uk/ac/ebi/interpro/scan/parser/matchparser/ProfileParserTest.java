package uk.ac.ebi.interpro.scan.parser.matchparser;

import org.junit.Test;
import org.junit.Ignore;
import junit.framework.TestCase;
import java.io.IOException;

/**
 * JUnit tests for ProfileParser
 *
 * @author  Manjula Thimma
 * @version $Id$
 * @since   1.0
 * @see     uk.ac.ebi.interpro.scan.parser.matchparser.ProfileParser
 */
public class ProfileParserTest extends TestCase {

    // TODO: Add prosite/* files to resources directory and re-enable tests
    @Test
    @Ignore
    public void testParseProfile() throws IOException {
        /*
        InputStream is = getClass().getClassLoader().getResourceAsStream("prosite/prosite_output.txt");
        Parser parser = new ProfileParser();
        Set<SequenceIdentifier> sequenceIds = parser.parse(is);
        */
    }

    @Test
    @Ignore
    public void testParseProfileHamap() throws IOException {
        /*
        InputStream is = getClass().getClassLoader().getResourceAsStream("prosite/hamap_output.txt");
        Parser parser = new ProfileParser();
        Set<SequenceIdentifier> sequenceIds = parser.parse(is);
        */
    }
    
}
