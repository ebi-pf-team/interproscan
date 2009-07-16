package uk.ac.ebi.interpro.scan.batch.item;

import static org.junit.Assert.assertEquals;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.runner.RunWith;
import org.junit.Test;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.HmmMatch;
import uk.ac.ebi.interpro.scan.model.Model;

/**
 * Tests {@link ShortMatchLocationItemProcessor}.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ShortMatchItemProcessorTest {   
    
    @Autowired
    private ShortMatchLocationItemProcessor processor;

    @Test public final void testProcess() throws Exception {
        final double score  = 1;
        final double evalue = 1;
        final int start     = 1;
        final int cutoff    = processor.getCutoff();
        // Add match with several locations
        Protein protein = new Protein("ABCD");
        // TODO: Should we require locations to be added when we add a match? Otherwise can create match with no location(s)
        HmmMatch match = protein.addMatch(new HmmMatch(new Model("model1"), score, evalue));
        match.addLocation(buildLocation(start, start + cutoff + 1));   // OK (end - start > cutoff)
        match.addLocation(buildLocation(start, start + cutoff));       // Not OK (end - start = cutoff)
        match.addLocation(buildLocation(start, start + cutoff - 1));   // Not OK (end - start < cutoff)
        // Only one location should remain after processing
        assertEquals(1, protein.getMatches().size());
        assertEquals(3, match.getLocations().size());
        protein = processor.process(protein);
        assertEquals(1, protein.getMatches().size());
        assertEquals(1, protein.getMatches().get(match.getKey()).getLocations().size());
        // Add match with one location
        protein = new Protein("EFGH");
        match = protein.addMatch(new HmmMatch(new Model("model2"), score, evalue));
        match.addLocation(buildLocation(start, start + cutoff));       // Not OK (end - start = cutoff)
        // No match should remain after processing (we've removed the only location, so the match should be removed too)
        assertEquals(1, protein.getMatches().size());
        assertEquals(1, match.getLocations().size());        
        protein = processor.process(protein);
        assertEquals(0, protein.getMatches().size());
    }

    private HmmMatch.HmmLocation buildLocation(int start, int end)  {
        final double score = 1;
        final double evalue = 1;
        final int hmmStart = 1;
        final int hmmEnd = 10;
        return new HmmMatch.
                HmmLocation(start, end, hmmStart, hmmEnd, HmmMatch.HmmBounds.C_TERMINAL_COMPLETE, score, evalue);
    }

}
