package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import uk.ac.ebi.interpro.scan.model.TMHMMMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class TMHMMBerkeleyMatchConverter extends BerkeleyMatchConverter<TMHMMMatch> {

    @Override
    public TMHMMMatch convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {
        if (berkeleyMatch == null || signature == null) {
            return null;
        }
        Set<TMHMMMatch.TMHMMLocation> locations = new HashSet<>(berkeleyMatch.getLocations().size());
        for (BerkeleyLocation berkeleyLocation : berkeleyMatch.getLocations()) {
            locations.add(new TMHMMMatch.TMHMMLocation(
                    berkeleyLocation.getStart(),
                    berkeleyLocation.getEnd()
            ));
        }

        return new TMHMMMatch(signature, berkeleyMatch.getSignatureModels(), locations);
    }

}
