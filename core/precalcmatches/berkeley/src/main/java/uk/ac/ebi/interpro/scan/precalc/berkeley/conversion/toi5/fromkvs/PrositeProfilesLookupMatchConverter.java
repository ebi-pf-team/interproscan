package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import uk.ac.ebi.interpro.scan.model.ProfileScanMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

import java.util.HashSet;
import java.util.Set;

public class PrositeProfilesLookupMatchConverter extends LookupMatchConverter<ProfileScanMatch> {

    @Override
    public ProfileScanMatch convertMatch(SimpleLookupMatch match, Signature signature) {
        Set<ProfileScanMatch.ProfileScanLocation> locations = new HashSet<>(1);
        int locationStart = valueOrZero(match.getSequenceStart());
        int locationEnd = valueOrZero(match.getSequenceEnd());
        Double score = valueOrZero(match.getLocationScore());
        String alignment = valueOrNotAvailable(match.getSeqFeature());
        locations.add(new ProfileScanMatch.ProfileScanLocation(locationStart, locationEnd, score, alignment));
        return new ProfileScanMatch(signature, match.getModelAccession(), locations);
    }

}
