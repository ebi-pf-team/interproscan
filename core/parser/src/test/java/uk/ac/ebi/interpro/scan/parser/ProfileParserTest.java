package uk.ac.ebi.interpro.scan.parser;

import org.junit.Test;
import junit.framework.TestCase;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SequenceIdentifier;

/**
 * JUnit tests for ProfileParser
 *
 * @author  Manjula Thimma
 * @version $Id: ProfileParserTest.java,v 1.3 2009/02/27 17:21:30 aquinn Exp $
 * @since   1.0
 * @see     ProfileParser
 */
public class ProfileParserTest extends TestCase {

    // TODO: Add prosite/* files to resources directory and re-enable tests
    @Test public void testParseProfile() throws IOException {
        /*
        InputStream is = getClass().getClassLoader().getResourceAsStream("prosite/prosite_output.txt");
        Parser parser = new ProfileParser();
        Set<SequenceIdentifier> sequenceIds = parser.parse(is);
        */
    }

    @Test public void testParseProfileHamap() throws IOException {
        /*
        InputStream is = getClass().getClassLoader().getResourceAsStream("prosite/hamap_output.txt");
        Parser parser = new ProfileParser();
        Set<SequenceIdentifier> sequenceIds = parser.parse(is);
        */
    }
    
}
