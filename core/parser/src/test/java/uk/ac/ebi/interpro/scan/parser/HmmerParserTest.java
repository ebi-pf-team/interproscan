package uk.ac.ebi.interpro.scan.parser;

import org.junit.Test;

import java.io.InputStream;
import java.io.IOException;
import java.util.Set;
import java.util.Map;

import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Location;
import uk.ac.ebi.interpro.scan.model.SequenceIdentifier;
import junit.framework.TestCase;

/**
 * JUnit tests for HmmerParser
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 * @see     HmmerParser
 */
public class HmmerParserTest extends TestCase {

    @Test
    public void testParseHmmPfam() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("hmmer2/hmmpfam/pfam-short.txt");
        Parser parser = new HmmerParser();
        Set<SequenceIdentifier> seqIds = parser.parse(is);
        assertEquals(seqIds.size(), 2);
        assertTrue(seqIds.contains(SequenceIdentifier.Factory.createSequenceIdentifier("UPI000000001B")));
        for (SequenceIdentifier id : seqIds)  {
            Map<String, Match> matches = id.getMatches();
            if (id.getIdentifier().equals("UPI000000001B"))  {
                assertTrue(matches.containsKey("PF03286.5"));
            }
        }
    }

    @Test
    public void testParseHmmSearch() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("hmmer2/hmmsearch/pfam-small.txt");
        /* TODO: Re-enable test when happy with model
        Parser parser = new HmmerParser();
        Set<Protein> proteins = parser.parse(is);
        assertEquals(1, proteins.size());
        // We need a proper equals() method in Protein for this to work
        // assertTrue(proteins.contains("UPI0000013598"));
        // Use this for now:
        assertEquals("UPI0000013598", proteins.iterator().next().getAccession());
        for (Protein protein : proteins)  {
            Set<Match> matches = protein.getMatches();
            if (protein.getAccession().equals("UPI0000013598"))  {
                assertEquals(1, matches.size());
                for (Match m : matches)  {
                    Set<Location> locations = m.getLocations();
                    assertEquals(6, locations.size());
                }
            }
        }
        */
    }

}
