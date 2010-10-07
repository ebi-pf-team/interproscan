package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5;

import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.Set;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public abstract class AbstractConversion<T extends Match> {

    public Protein populateProteinMatches(Set<BerkeleyMatch> berkeleyMatches) {
        /*
        Collection of matches of different kinds.
        Iterate over them, 
         */
        return null;
    }

}
