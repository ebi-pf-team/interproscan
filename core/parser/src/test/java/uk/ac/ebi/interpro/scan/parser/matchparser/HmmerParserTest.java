package uk.ac.ebi.interpro.scan.parser.matchparser;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.raw.RawSequenceIdentifier;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.parser.matchparser.HmmerParser;
import uk.ac.ebi.interpro.scan.parser.matchparser.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * JUnit tests for HmmerParser
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 * @see     HmmerParser
 */
@Ignore("The testParseHmmPfam test is not working following re-factoring of the model - the test may well need modifying.")
public class HmmerParserTest extends TestCase {

    /**
     * TODO - fix this test, currently fails.
     * @throws IOException
     */
   /* @Test
    public void testParseHmmPfam() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("hmmer2/hmmpfam/pfam-short.txt");
        Parser parser = new HmmerParser();
        Set<RawSequenceIdentifier> seqIds = parser.parse(is);
        assertEquals(seqIds.size(), 2);
        assertTrue(seqIds.contains(RawSequenceIdentifier.Factory.createSequenceIdentifier("UPI000000001B")));
        for (RawSequenceIdentifier id : seqIds)  {
            Set<RawMatch> matches = id.getRawMatches();
            if (id.getIdentifier().equals("UPI000000001B"))  {
//                assertTrue(matches.containsKey("PF03286.5"));
                boolean found = false;
                for (RawMatch match : matches){
                    if ("PF03286.5".equals(match.getModel().getAccession())){
                        found = true;
                        break;
                    }
                }
                if (! found){
                    fail("PF03286.5 not found as expected as an Xref of UPI000000001B.");
                }
            }

        }
    }*/


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
