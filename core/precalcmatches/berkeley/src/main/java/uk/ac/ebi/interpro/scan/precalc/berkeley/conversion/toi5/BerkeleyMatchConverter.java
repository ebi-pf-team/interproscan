package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5;

import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

/**
 * Interface for class that converts BerkeleyMatches to I5 matches.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface BerkeleyMatchConverter<T extends Match> {
    T convertMatch(BerkeleyMatch berkeleyMatch, Signature signature);
}
