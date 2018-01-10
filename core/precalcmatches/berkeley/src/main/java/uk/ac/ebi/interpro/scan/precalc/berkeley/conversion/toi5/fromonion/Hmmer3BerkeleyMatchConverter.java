package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.HashSet;
import java.util.Set;

/**
 * Converts a BerkeleyMatch to a HMMER3 Match.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class Hmmer3BerkeleyMatchConverter extends BerkeleyMatchConverter<Hmmer3Match> {

    public Hmmer3Match convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {

        final Set<Hmmer3Match.Hmmer3Location> locations = new HashSet<Hmmer3Match.Hmmer3Location>(berkeleyMatch.getLocations().size());

        for (BerkeleyLocation location : berkeleyMatch.getLocations()) {

            final HmmBounds bounds;
            if (location.getHmmBounds() == null || location.getHmmBounds().isEmpty()) {
                bounds = HmmBounds.COMPLETE;   // FUDGE!  HmmBounds cannot be null...
            } else {
                bounds = HmmBounds.parseSymbol(location.getHmmBounds());
            }

            Integer hmmLength = signature.getModels().get(0).getLength();//TODO hmmLength

            locations.add(new Hmmer3Match.Hmmer3Location(
                    valueOrZero(location.getStart()),
                    valueOrZero(location.getEnd()),
                    valueOrZero(location.getScore()),
                    valueOrZero(location.geteValue()),
                    valueOrZero(location.getHmmStart()),
                    valueOrZero(location.getHmmEnd()),
                    valueOrZero(hmmLength),
                    bounds,
                    location.getEnvelopeStart() == null
                            ? (location.getStart() == null ? 0 : location.getStart())
                            : location.getEnvelopeStart(),
                    location.getEnvelopeEnd() == null
                            ? location.getEnd() == null ? 0 : location.getEnd()
                            : location.getEnvelopeEnd()
            ));
        }

        return new Hmmer3Match(
                signature,
                berkeleyMatch.getSignatureModels(),
                valueOrZero(berkeleyMatch.getSequenceScore()),
                valueOrZero(berkeleyMatch.getSequenceEValue()),
                locations
        );
    }

}
