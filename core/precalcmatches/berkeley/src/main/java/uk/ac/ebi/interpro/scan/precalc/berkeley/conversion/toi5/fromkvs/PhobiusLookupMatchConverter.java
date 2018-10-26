package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import uk.ac.ebi.interpro.scan.model.PhobiusMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

import java.util.HashSet;
import java.util.Set;

public class PhobiusLookupMatchConverter extends LookupMatchConverter<PhobiusMatch> {

    @Override
    public PhobiusMatch convertMatch(SimpleLookupMatch match, Signature signature) {
        if (match == null || signature == null) {
            return null;
        }
        Set<PhobiusMatch.PhobiusLocation> locations = new HashSet<>(1);
        int locationStart = valueOrZero(match.getSequenceStart());
        int locationEnd = valueOrZero(match.getSequenceEnd());
        locations.add(new PhobiusMatch.PhobiusLocation(locationStart, locationEnd));

        return new PhobiusMatch(signature, match.getModelAccession(), locations);
    }

}
