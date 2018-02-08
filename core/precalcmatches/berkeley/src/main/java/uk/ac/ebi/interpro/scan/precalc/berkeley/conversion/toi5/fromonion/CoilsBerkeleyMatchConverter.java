package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import uk.ac.ebi.interpro.scan.model.CoilsMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Phil Jones
 *         Date: 16/05/11
 *         Time: 14:14
 */
public class CoilsBerkeleyMatchConverter extends BerkeleyMatchConverter<CoilsMatch> {

    @Override
    public CoilsMatch convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {
        if (berkeleyMatch == null || signature == null) {
            return null;
        }
        Set<CoilsMatch.CoilsLocation> locations = new HashSet<CoilsMatch.CoilsLocation>(berkeleyMatch.getLocations().size());
        for (BerkeleyLocation berkeleyLocation : berkeleyMatch.getLocations()) {
            locations.add(new CoilsMatch.CoilsLocation(
                    berkeleyLocation.getStart(), berkeleyLocation.getEnd()
            ));
        }

        return new CoilsMatch(signature, berkeleyMatch.getSignatureModels(), locations);
    }

}
