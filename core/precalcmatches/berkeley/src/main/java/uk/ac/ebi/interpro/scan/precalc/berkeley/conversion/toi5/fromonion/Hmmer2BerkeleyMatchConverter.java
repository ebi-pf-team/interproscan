package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.Hmmer2Match;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.HashSet;
import java.util.Set;

/**
 * Converts a BerkeleyMatch to a HMMER2 Match.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class Hmmer2BerkeleyMatchConverter extends BerkeleyMatchConverter<Hmmer2Match> {

    public Hmmer2Match convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {

        Set<Hmmer2Match.Hmmer2Location> locations = new HashSet<Hmmer2Match.Hmmer2Location>(berkeleyMatch.getLocations().size());

        for (BerkeleyLocation location : berkeleyMatch.getLocations()) {

            final HmmBounds bounds;
            if (location.getHmmBounds() == null || location.getHmmBounds().isEmpty()) {
                bounds = HmmBounds.COMPLETE; // Fudge ! HmmBounds cannot be null.
            } else {
                bounds = HmmBounds.parseSymbol(location.getHmmBounds());
            }

            Integer hmmLength = signature.getModels().get(0).getLength();//TODO hmmLength

            locations.add(new Hmmer2Match.Hmmer2Location(
                    valueOrZero(location.getStart()),
                    valueOrZero(location.getEnd()),
                    valueOrZero(location.getScore()),
                    valueOrZero(location.geteValue()),
                    valueOrZero(location.getHmmStart()),
                    valueOrZero(location.getHmmEnd()),
                    valueOrZero(hmmLength),
                    bounds
            ));
        }

        return new Hmmer2Match(
                signature,
                valueOrZero(berkeleyMatch.getSequenceScore()),
                valueOrZero(berkeleyMatch.getSequenceEValue()),
                locations
        );
    }

}
