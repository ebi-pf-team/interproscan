package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.DCStatus;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SuperFamilyHmmer3Match;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocationFragment;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.HashSet;
import java.util.Set;

/**
 * @author phil Jones
 *         Date: 12/08/11
 *         Time: 11:36
 *         <p/>
 *         Converts matches retrieved from the Berkeley pre-calc match lookup service
 *         to the I5 match type, for SUPERFAMILY.
 */
public class SuperfamilyMatchConverter extends BerkeleyMatchConverter<SuperFamilyHmmer3Match> {

    private static final Logger LOG = Logger.getLogger(SuperfamilyMatchConverter.class.getName());

    //TODO: Add the e-value to the match location
    @Override
    public SuperFamilyHmmer3Match convertMatch(BerkeleyMatch match, Signature signature) {
        if (match == null || signature == null) {
            return null;
        }
        Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> locations = new HashSet<>(match.getLocations().size());
        for (BerkeleyLocation location : match.getLocations()) {
            int start = valueOrZero(location.getStart());
            int end = valueOrZero(location.getEnd());
            Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment> locationFragments = new HashSet<>(location.getLocationFragments().size());
            for (BerkeleyLocationFragment locationFragment : location.getLocationFragments()) {
                int fragStart = valueOrZero(locationFragment.getStart());
                int fragEnd = valueOrZero(locationFragment.getEnd());
                String dcStatus = locationFragment.getDcStatus(); // Always default ("S") for SUPERFAMILY?
                if (dcStatus == null) {
                    LOG.warn("NULL dcStatus for fragment " + fragStart + " - " + fragEnd + " in SUPERFAMILY " + match.getSignatureAccession());
                    dcStatus = "S"; // Default
                }
                locationFragments.add(new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment(fragStart, fragEnd, DCStatus.parseSymbol(dcStatus)));
            }
            locations.add(new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location(
                    start,
                    end,
                    locationFragments,
                    valueOrZero(location.getHmmLength())
            ));
        }

        return new SuperFamilyHmmer3Match(
                signature,
                match.getSignatureModels(),
                valueOrZero(match.getSequenceEValue()),
                locations
        );
    }
}
