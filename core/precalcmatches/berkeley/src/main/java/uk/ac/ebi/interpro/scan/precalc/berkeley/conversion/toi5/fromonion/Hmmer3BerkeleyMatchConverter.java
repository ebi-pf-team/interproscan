package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.DCStatus;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocationFragment;
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

        final String sln = berkeleyMatch.getSignatureLibraryName();
        boolean postProcessed = false;
        if (sln.equalsIgnoreCase("GENE3D") || sln.equalsIgnoreCase("PFAM")) {
            postProcessed = true;
        }

        final Set<Hmmer3Match.Hmmer3Location> locations = new HashSet<Hmmer3Match.Hmmer3Location>(berkeleyMatch.getLocations().size());

        for (BerkeleyLocation location : berkeleyMatch.getLocations()) {

            int locationStart = valueOrZero(location.getStart());
            int locationEnd = valueOrZero(location.getEnd());

            int envStart = location.getEnvelopeStart() == null
                    ? (location.getStart() == null ? 0 : location.getStart())
                    : location.getEnvelopeStart();
            int envEnd =  location.getEnvelopeEnd() == null
                    ? location.getEnd() == null ? 0 : location.getEnd()
                    : location.getEnvelopeEnd();

            final Set<Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment> locationFragments = new HashSet<>(location.getLocationFragments().size());
            for (BerkeleyLocationFragment fragment : location.getLocationFragments()) {
                int fragStart = valueOrZero(fragment.getStart());
                int fragEnd = valueOrZero(fragment.getEnd());
                String fragBounds = fragment.getBounds();
                locationFragments.add(new Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment(fragStart, fragEnd, DCStatus.parseSymbol(fragBounds)));
            }

            final HmmBounds bounds = HmmBounds.parseSymbol(HmmBounds.calculateHmmBounds(envStart, envEnd, locationStart, locationEnd));

            locations.add(new Hmmer3Match.Hmmer3Location(
                    locationStart,
                    locationEnd,
                    valueOrZero(location.getScore()),
                    valueOrZero(location.geteValue()),
                    valueOrZero(location.getHmmStart()),
                    valueOrZero(location.getHmmEnd()),
                    valueOrZero(location.getHmmLength()),
                    bounds,
                    envStart,
                    envEnd,
                    postProcessed,
                    locationFragments
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
