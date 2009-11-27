package uk.ac.ebi.interpro.scan.io.match;

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
 * @see     ProfileMatchParser
 */
public class ProfileMatchParserTest extends TestCase {

    // TODO: Add prosite/* files to resources directory and re-enable tests
    @Test
    @Ignore
    public void testParseProfile() throws IOException {
        /*
        InputStream is = getClass().getClassLoader().getResourceAsStream("prosite/prosite_output.txt");
        MatchParser io = new ProfileMatchParser();
        Set<SequenceIdentifier> sequenceIds = io.parse(is);
        */
    }

    @Test
    @Ignore
    public void testParseProfileHamap() throws IOException {
        /*
        InputStream is = getClass().getClassLoader().getResourceAsStream("prosite/hamap_output.txt");
        MatchParser io = new ProfileMatchParser();
        Set<SequenceIdentifier> sequenceIds = io.parse(is);
        */
    }
    
}
