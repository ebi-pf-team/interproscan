package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import uk.ac.ebi.interpro.scan.model.TMHMMMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

import java.util.HashSet;
import java.util.Set;

public class TMHMMLookupMatchConverter extends LookupMatchConverter<TMHMMMatch> {

    @Override
    public TMHMMMatch convertMatch(SimpleLookupMatch match, Signature signature) {
        if (match == null || signature == null) {
            return null;
        }
        Set<TMHMMMatch.TMHMMLocation> locations = new HashSet<>(1);
        int locationStart = valueOrZero(match.getSequenceStart());
        int locationEnd = valueOrZero(match.getSequenceEnd());
        locations.add(new TMHMMMatch.TMHMMLocation(locationStart, locationEnd));

        return new TMHMMMatch(signature, match.getModelAccession(), locations);
    }

}
