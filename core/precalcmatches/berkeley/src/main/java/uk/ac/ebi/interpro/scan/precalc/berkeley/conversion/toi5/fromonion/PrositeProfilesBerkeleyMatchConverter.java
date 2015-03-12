package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import uk.ac.ebi.interpro.scan.model.ProfileScanMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Phil Jones
 *         Date: 16/05/11
 *         Time: 16:01
 */
public class PrositeProfilesBerkeleyMatchConverter extends BerkeleyMatchConverter<ProfileScanMatch> {

    @Override
    public ProfileScanMatch convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {
        Set<ProfileScanMatch.ProfileScanLocation> locations = new HashSet<ProfileScanMatch.ProfileScanLocation>();

        for (BerkeleyLocation location : berkeleyMatch.getLocations()) {
            locations.add(new ProfileScanMatch.ProfileScanLocation(
                    valueOrZero(location.getStart()),
                    valueOrZero(location.getEnd()),
                    valueOrZero(location.getScore()),
                    valueOrNotAvailable(location.getCigarAlignment())
            ));
        }

        return new ProfileScanMatch(signature, locations);
    }

}
