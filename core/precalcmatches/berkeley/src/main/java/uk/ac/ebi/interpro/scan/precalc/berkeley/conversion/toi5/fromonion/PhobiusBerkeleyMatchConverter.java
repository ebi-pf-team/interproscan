package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.PhobiusMatch;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class PhobiusBerkeleyMatchConverter extends BerkeleyMatchConverter<PhobiusMatch> {

    @Override
    public PhobiusMatch convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {
        if (berkeleyMatch == null || signature == null) {
            return null;
        }
        Set<PhobiusMatch.PhobiusLocation> locations = new HashSet<>(berkeleyMatch.getLocations().size());
        for (BerkeleyLocation berkeleyLocation : berkeleyMatch.getLocations()) {
            locations.add(new PhobiusMatch.PhobiusLocation(
                    berkeleyLocation.getStart(),
                    berkeleyLocation.getEnd()
            ));
        }

        return new PhobiusMatch(signature, berkeleyMatch.getSignatureModels(), locations);
    }

}
