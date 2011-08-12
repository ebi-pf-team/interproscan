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
public abstract class BerkeleyMatchConverter<T extends Match> {

    public abstract T convertMatch(BerkeleyMatch berkeleyMatch, Signature signature);

    protected static double valueOrZero(Double value) {
        if (value == null ||
                value.isInfinite() ||
                value.isNaN()) {
            return 0.0d;
        }
        return value;
    }

    protected static int valueOrZero(Integer value) {
        if (value == null) {
            return 0;
        }
        return value;
    }

    protected static long valueOrZero(Long value) {
        if (value == null) {
            return 0;
        }
        return value;
    }

    protected static String valueOrNotAvailable(String value) {
        return (value == null || value.isEmpty())
                ? "Not available"
                : value;
    }


}
