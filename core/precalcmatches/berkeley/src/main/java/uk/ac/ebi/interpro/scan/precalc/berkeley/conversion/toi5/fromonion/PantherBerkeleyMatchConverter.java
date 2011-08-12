package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import uk.ac.ebi.interpro.scan.model.PantherMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Phil Jones
 *         Date: 12/08/11
 *         Time: 11:14
 *         <p/>
 *         Converts matches retrieved from the Berkeley pre-calc match lookup service
 *         to the I5 match type, for PANTHER.
 */
public class PantherBerkeleyMatchConverter extends BerkeleyMatchConverter<PantherMatch> {
    @Override
    public PantherMatch convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {
        if (berkeleyMatch == null || signature == null) {
            return null;
        }
        Set<PantherMatch.PantherLocation> locations = new HashSet<PantherMatch.PantherLocation>(berkeleyMatch.getLocations().size());
        for (BerkeleyLocation berkeleyLocation : berkeleyMatch.getLocations()) {
            locations.add(new PantherMatch.PantherLocation(
                    berkeleyLocation.getStart(), berkeleyLocation.getEnd()
            ));
        }

        return new PantherMatch(
                signature,
                locations,
                valueOrZero(berkeleyMatch.getSequenceEValue()),
                "Not available",
                valueOrZero(berkeleyMatch.getSequenceScore()));
    }
}
