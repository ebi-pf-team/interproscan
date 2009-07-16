package uk.ac.ebi.interpro.scan.batch.item;

import org.springframework.batch.item.ItemProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Location;

import java.util.Set;
import java.util.HashSet;

/**
 * Removes {@link Location)s from {@link Match}es where location length is less than or equals
 * to the given cutoff.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public class ShortMatchLocationItemProcessor implements ItemProcessor<Protein, Protein> {
    
    private static final Log logger = LogFactory.getLog(ShortMatchLocationItemProcessor.class);

    int cutoff = 10;

    public int getCutoff() {
        return cutoff;
    }

    public void setCutoff(int cutoff) {
        this.cutoff = cutoff;
    }

    // TODO: Why can't we do for (Location location : match.getLocations()) ?

    public Protein process(Protein item) throws Exception {
        // Get copy of matches to avoid ConcurrentModificationException
        Set<Match> matches = new HashSet<Match>(item.getMatches().values());
        for (Match match : matches)   {
            // Get copy of locations to avoid ConcurrentModificationException
            Set<Location> locations = new HashSet<Location>(match.getLocations());
            for (Location location : locations)  {
                if (location.getEnd() - location.getStart() <= cutoff)    {
                    // TODO: Write toString method for Location implementations
                    logger.info("Removing " + location);
                    match.removeLocation(location);
                    // Remove match from protein if removed all locations
                    // TODO: Add to model code? (Remove match from protein if removed all locations)
                    if (match.getLocations().size() == 0)   {
                        item.removeMatch(match);
                    }
                }
            }
        }
        return item;
    }
}
