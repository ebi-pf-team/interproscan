package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5;

import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

public abstract class LookupMatchConverter<T extends Match> {

    public abstract T convertMatch(SimpleLookupMatch simpleLookupMatch, Signature signature);

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

    protected static String valueOrEmpty(String value) {
        return (value == null)
                ? ""
                : value;
    }


}
