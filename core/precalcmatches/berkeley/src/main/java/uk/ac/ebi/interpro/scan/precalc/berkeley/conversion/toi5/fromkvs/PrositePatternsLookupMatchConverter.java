package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import uk.ac.ebi.interpro.scan.model.PatternScanMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

import java.util.HashSet;
import java.util.Set;

public class PrositePatternsLookupMatchConverter extends LookupMatchConverter<PatternScanMatch> {

    @Override
    public PatternScanMatch convertMatch(SimpleLookupMatch match, Signature signature) {
        Set<PatternScanMatch.PatternScanLocation> locations = new HashSet<>(1);
        int locationStart = valueOrZero(match.getSequenceStart());
        int locationEnd = valueOrZero(match.getSequenceEnd());
        String alignment = valueOrNotAvailable(match.getSeqFeature());
        // Level is known to always be STRONG as anything else would have been filtered out by post processing
        PatternScanMatch.PatternScanLocation.Level level = PatternScanMatch.PatternScanLocation.Level.STRONG;
        locations.add(new PatternScanMatch.PatternScanLocation(locationStart, locationEnd, level, alignment));

        return new PatternScanMatch(signature, match.getModelAccession(), locations);
    }

}
