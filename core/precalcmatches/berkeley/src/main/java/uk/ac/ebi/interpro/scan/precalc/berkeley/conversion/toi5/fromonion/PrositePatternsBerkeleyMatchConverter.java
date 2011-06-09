package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import uk.ac.ebi.interpro.scan.model.PatternScanMatch;
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
public class PrositePatternsBerkeleyMatchConverter extends BerkeleyMatchConverter<PatternScanMatch> {

    @Override
    public PatternScanMatch convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {
        Set<PatternScanMatch.PatternScanLocation> locations = new HashSet<PatternScanMatch.PatternScanLocation>();

        for (BerkeleyLocation location : berkeleyMatch.getLocations()) {
            locations.add(new PatternScanMatch.PatternScanLocation(
                    valueOrZero(location.getStart()),
                    valueOrZero(location.getEnd()),
                    PatternScanMatch.PatternScanLocation.Level.getLevelByTag(location.getLevel()),
                    valueOrNotAvailable(location.getCigarAlignment())
            ));
        }

        return new PatternScanMatch(signature, locations);
    }

}
