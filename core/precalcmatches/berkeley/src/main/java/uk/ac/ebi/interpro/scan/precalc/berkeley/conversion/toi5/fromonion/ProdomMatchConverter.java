package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import uk.ac.ebi.interpro.scan.model.BlastProDomMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.HashSet;
import java.util.Set;

/**
 * @author phil Jones
 *         Date: 12/08/11
 *         Time: 11:36
 *         <p/>
 *         Converts matches retrieved from the Berkeley pre-calc match lookup service
 *         to the I5 match type, for PRODOM.
 */
public class ProdomMatchConverter extends BerkeleyMatchConverter<BlastProDomMatch> {

    @Override
    public BlastProDomMatch convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {
        if (berkeleyMatch == null || signature == null) {
            return null;
        }
        Set<BlastProDomMatch.BlastProDomLocation> locations = new HashSet<BlastProDomMatch.BlastProDomLocation>(berkeleyMatch.getLocations().size());
        for (BerkeleyLocation berkeleyLocation : berkeleyMatch.getLocations()) {
            locations.add(new BlastProDomMatch.BlastProDomLocation(
                    berkeleyLocation.getStart(), berkeleyLocation.getEnd(), valueOrZero(berkeleyLocation.getScore()), valueOrZero(berkeleyLocation.geteValue())
            ));
        }

        return new BlastProDomMatch(
                signature,
                locations
        );
    }
}
