package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.impl;

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
public class Hmmer2BerkeleyMatchConverter implements BerkeleyMatchConverter<Hmmer2Match> {

    public Hmmer2Match convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {

        Set<Hmmer2Match.Hmmer2Location> locations = new HashSet<Hmmer2Match.Hmmer2Location>(berkeleyMatch.getLocations().size());

        for (BerkeleyLocation location : berkeleyMatch.getLocations()) {

            final HmmBounds bounds;
            if (location.getHmmBounds() == null || location.getHmmBounds().isEmpty()) {
                bounds = HmmBounds.COMPLETE; // Fudge ! HmmBounds cannot be null.
            } else {
                bounds = HmmBounds.parseSymbol(location.getHmmBounds());
            }

            locations.add(new Hmmer2Match.Hmmer2Location(
                    location.getStart() == null ? 0 : location.getStart(),
                    location.getEnd() == null ? 0 : location.getEnd(),
                    location.getScore() == null ? 0 : location.getScore(),
                    location.geteValue() == null ? 0 : location.geteValue(),
                    location.getHmmStart() == null ? 0 : location.getHmmStart(),
                    location.getHmmEnd() == null ? 0 : location.getHmmEnd(),
                    bounds
            ));
        }

        return new Hmmer2Match(
                signature,
                berkeleyMatch.getSequenceScore() == null ? 0 : berkeleyMatch.getSequenceScore(),
                berkeleyMatch.getSequenceEValue() == null ? 0 : berkeleyMatch.getSequenceEValue(),
                locations
        );
    }

}
