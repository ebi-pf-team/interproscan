package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SuperFamilyHmmer3Match;
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
 *         to the I5 match type, for SUPERFAMILY.
 */
public class SuperfamilyMatchConverter extends BerkeleyMatchConverter<SuperFamilyHmmer3Match> {

    //TODO: Add the e-value to the match location
    @Override
    public SuperFamilyHmmer3Match convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {
        if (berkeleyMatch == null || signature == null) {
            return null;
        }
        Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> locations = new HashSet<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location>(berkeleyMatch.getLocations().size());
        for (BerkeleyLocation berkeleyLocation : berkeleyMatch.getLocations()) {
            locations.add(new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location(
                    berkeleyLocation.getStart(), berkeleyLocation.getEnd()
            ));
        }
        //TODO remove this line
        System.out.println("evalue: " + valueOrZero(berkeleyMatch.getSequenceEValue()));
        return new SuperFamilyHmmer3Match(
                signature,
                valueOrZero(berkeleyMatch.getSequenceEValue()),
                locations
        );
    }
}
