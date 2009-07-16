package uk.ac.ebi.interpro.scan.parser;

import junit.framework.TestCase;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.SequenceIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * JUnit tests for ProdomParser
 *
 * @author  Phil Jones
 * @version $Id$
 * @since   1.0
 * @see     ProdomParser
 */
public class ProdomParserTest extends TestCase {

    @Test
    public void testParseProdom() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("prodom/prodom_output.txt");
        Parser parser = new ProdomParser();
        Set<SequenceIdentifier> sequenceIds = parser.parse(is);
        
    }
}