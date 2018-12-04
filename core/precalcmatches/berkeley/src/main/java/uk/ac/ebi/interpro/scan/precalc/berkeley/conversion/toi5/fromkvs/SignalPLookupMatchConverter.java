package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import uk.ac.ebi.interpro.scan.model.SignalPMatch;
import uk.ac.ebi.interpro.scan.model.SignalPOrganismType;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

import java.util.HashSet;
import java.util.Set;

public class SignalPLookupMatchConverter extends LookupMatchConverter<SignalPMatch, SignalPMatch.SignalPLocation> {

    @Override
    public SignalPMatch convertMatch(SimpleLookupMatch match, Signature signature) {
        if (match == null || signature == null) {
            return null;
        }
        Set<SignalPMatch.SignalPLocation> locations = new HashSet<>(1);
        int locationStart = valueOrZero(match.getSequenceStart());
        int locationEnd = valueOrZero(match.getSequenceEnd());
        Double locationScore = valueOrZero(match.getLocationScore());
        locations.add(new SignalPMatch.SignalPLocation(locationStart, locationEnd, locationScore));
        final SignalPOrganismType type = SignalPOrganismType.getSignalPOrganismTypeByOnionType(match.getSignatureLibraryName());
        if (type == null) {
            return null;
        }

        return new SignalPMatch(signature, match.getModelAccession(), type, locations);
    }

}
