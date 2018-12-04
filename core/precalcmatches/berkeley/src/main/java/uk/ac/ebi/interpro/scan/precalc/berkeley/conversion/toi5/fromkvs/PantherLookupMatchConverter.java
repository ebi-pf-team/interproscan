package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.PantherMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

import java.util.HashSet;
import java.util.Set;

public class PantherLookupMatchConverter extends LookupMatchConverter<PantherMatch, PantherMatch.PantherLocation> {

    //TODO: Add the e-value to the match location
    @Override
    public PantherMatch convertMatch(SimpleLookupMatch match, Signature signature) {
        if (match == null || signature == null) {
            return null;
        }

        final HmmBounds bounds;
        if (match.getHmmBounds() == null || match.getHmmBounds().isEmpty()) {
            bounds = HmmBounds.COMPLETE;   // FUDGE!  HmmBounds cannot be null...
        } else {
            bounds = HmmBounds.parseSymbol(match.getHmmBounds());
        }

        Set<PantherMatch.PantherLocation> locations = new HashSet<>(1);
        locations.add(new PantherMatch.PantherLocation(
                match.getSequenceStart(), match.getSequenceEnd(),
                valueOrZero(match.getHmmStart()), valueOrZero(match.getHmmEnd()), valueOrZero(match.getHmmLength()),
                bounds, valueOrZero(match.getEnvelopeStart()), valueOrZero(match.getEnvelopeEnd())
        ));

        return new PantherMatch(
                signature,
                match.getModelAccession(),
                locations,
                valueOrZero(match.getSequenceEValue()),
                "Not available",
                valueOrZero(match.getSequenceScore()));
    }
}
