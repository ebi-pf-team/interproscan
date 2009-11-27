package uk.ac.ebi.interpro.scan.io.match;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.io.match.HmmerMatchParser;

import java.io.IOException;
import java.io.InputStream;

/**
 * JUnit tests for HmmerParser
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 * @see     HmmerMatchParser
 */
@Ignore("The testParseHmmPfam test is not working following re-factoring of the model - the test may well need modifying.")
public class HmmerMatchParserTest extends TestCase {

    /**
     * TODO - fix this test, currently fails.
     * @throws IOException
     */
   /* @Test
    public void testParseHmmPfam() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("data.hmmer23/hmmpfam/pfam-short.txt");
        MatchParser io = new HmmerMatchParser();
        Set<RawProtein> seqIds = io.parse(is);
        assertEquals(seqIds.size(), 2);
        assertTrue(seqIds.contains(RawProtein.Factory.createSequenceIdentifier("UPI000000001B")));
        for (RawProtein id : seqIds)  {
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
        MatchParser io = new HmmerMatchParser();
        Set<Protein> proteins = io.parse(is);
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
