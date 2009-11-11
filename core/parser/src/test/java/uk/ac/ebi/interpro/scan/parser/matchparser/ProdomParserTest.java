package uk.ac.ebi.interpro.scan.parser.matchparser;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.Ignore;
import uk.ac.ebi.interpro.scan.model.raw.RawSequenceIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * JUnit tests for ProdomParser
 *
 * @author  Phil Jones
 * @version $Id$
 * @since   1.0
 * @see     uk.ac.ebi.interpro.scan.parser.matchparser.ProdomParser
 */
public class ProdomParserTest extends TestCase {

    @Test
    @Ignore
    public void testParseProdom() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("prodom/prodom_output.txt");
        Parser parser = new ProdomParser();
        Set<RawSequenceIdentifier> sequenceIds = parser.parse(is);
        
    }
}