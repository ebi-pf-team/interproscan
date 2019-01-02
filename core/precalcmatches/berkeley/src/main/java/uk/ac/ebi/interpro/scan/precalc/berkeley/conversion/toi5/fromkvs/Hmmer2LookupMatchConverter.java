package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

import java.util.HashSet;
import java.util.Set;

public class Hmmer2LookupMatchConverter extends LookupMatchConverter<Hmmer2Match, Hmmer2Match.Hmmer2Location> {

    public Hmmer2Match convertMatch(SimpleLookupMatch match, Signature signature) {

        final HmmBounds bounds;
        if (match.getHmmBounds() == null || match.getHmmBounds().isEmpty()) {
            bounds = HmmBounds.COMPLETE; // Fudge ! HmmBounds cannot be null.
        } else {
            bounds = HmmBounds.parseSymbol(match.getHmmBounds());
        }

        Set<Hmmer2Match.Hmmer2Location> locations = new HashSet<>(1);
        locations.add(new Hmmer2Match.Hmmer2Location(
                valueOrZero(match.getSequenceStart()),
                valueOrZero(match.getSequenceEnd()),
                valueOrZero(match.getLocationScore()),
                valueOrZero(match.getLocationEValue()),
                valueOrZero(match.getHmmStart()),
                valueOrZero(match.getHmmEnd()),
                valueOrZero(match.getHmmLength()),
                bounds
        ));

        return new Hmmer2Match(
                signature,
                match.getModelAccession(),
                valueOrZero(match.getSequenceScore()),
                valueOrZero(match.getSequenceEValue()),
                locations
        );
    }

}
