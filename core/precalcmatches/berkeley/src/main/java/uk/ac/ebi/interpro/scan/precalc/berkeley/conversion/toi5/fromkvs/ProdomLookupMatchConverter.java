package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import uk.ac.ebi.interpro.scan.model.BlastProDomMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

import java.util.HashSet;
import java.util.Set;

public class ProdomLookupMatchConverter extends LookupMatchConverter<BlastProDomMatch, BlastProDomMatch.BlastProDomLocation> {

    @Override
    public BlastProDomMatch convertMatch(SimpleLookupMatch match, Set<String> sequenceSiteHits, Signature signature) {
        if (match == null || signature == null) {
            return null;
        }
        Set<BlastProDomMatch.BlastProDomLocation> locations = new HashSet<>(1);
        //TODO Change from locationScore to sequenceScore when correct in MV_IPRSCAN
        locations.add(new BlastProDomMatch.BlastProDomLocation(
                match.getSequenceStart(), match.getSequenceEnd(), valueOrZero(match.getLocationScore()), valueOrZero(match.getSequenceEValue())
        ));

        return new BlastProDomMatch(
                signature,
                match.getModelAccession(),
                locations
        );
    }
}
