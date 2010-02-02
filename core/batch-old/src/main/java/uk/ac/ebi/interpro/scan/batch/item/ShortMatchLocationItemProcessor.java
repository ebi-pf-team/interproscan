package uk.ac.ebi.interpro.scan.batch.item;

import org.springframework.batch.item.ItemProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.Gene3dHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.Hmmer3RawMatch;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

/**
 * Removes {@link Location)s from {@link Match}es where location length is less than or equals
 * to the given cutoff.
 *
 * @author  Antony Quinn
 * @version $Id$
 * @since   1.0
 */
public class ShortMatchLocationItemProcessor implements ItemProcessor<RawProtein<Gene3dHmmer3RawMatch>, Protein> {
    
    private static final Log logger = LogFactory.getLog(ShortMatchLocationItemProcessor.class);

    int cutoff = 10;

    public int getCutoff() {
        return cutoff;
    }

    public void setCutoff(int cutoff) {
        this.cutoff = cutoff;
    }

    public Protein process(RawProtein<Gene3dHmmer3RawMatch> item) throws Exception {
        Collection<Gene3dHmmer3RawMatch> matches = new HashSet<Gene3dHmmer3RawMatch>();
        for (Gene3dHmmer3RawMatch match : item.getMatches())   {
            if (match.getLocationEnd() - match.getLocationStart() > cutoff)    {
                if (logger.isDebugEnabled())    {
                    logger.debug("Adding " + match);
                }
                matches.add(match);
            }
        }
        Collection<Hmmer3Match> filteredMatches = Gene3dHmmer3RawMatch.getMatches(matches, new RawMatch.Listener() {
            public Signature getSignature(String s, String s1, String s2) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        Protein p = new Protein("TODO");    // TODO: Get sequence
        for (Hmmer3Match m : filteredMatches)   {
            p.addMatch(m);
        }
        return p;
    }
}
