package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

import java.util.HashSet;
import java.util.Set;

public class Hmmer2LookupMatchConverter extends LookupMatchConverter<Hmmer2Match> {

    public Hmmer2Match convertMatch(SimpleLookupMatch match, Signature signature) {

        Set<Hmmer2Match.Hmmer2Location> locations = new HashSet<>(1);

        for (BerkeleyLocation location : match.getLocations()) {

            final HmmBounds bounds;
            if (location.getHmmBounds() == null || location.getHmmBounds().isEmpty()) {
                bounds = HmmBounds.COMPLETE; // Fudge ! HmmBounds cannot be null.
            } else {
                bounds = HmmBounds.parseSymbol(location.getHmmBounds());
            }

            locations.add(new Hmmer2Match.Hmmer2Location(
                    valueOrZero(location.getStart()),
                    valueOrZero(location.getEnd()),
                    valueOrZero(location.getScore()),
                    valueOrZero(location.geteValue()),
                    valueOrZero(location.getHmmStart()),
                    valueOrZero(location.getHmmEnd()),
                    valueOrZero(location.getHmmLength()),
                    bounds
            ));
        }

        return new Hmmer2Match(
                signature,
                match.getModelAccession(),
                valueOrZero(match.getSequenceScore()),
                valueOrZero(match.getSequenceEValue()),
                locations
        );
    }

}
