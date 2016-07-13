package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.RPSBlastMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.HashSet;
import java.util.Set;

/**
 * Converts a BerkeleyMatch to a RPSBlast Match.
 *
 * @author Gift Nuka
 * @date 20/05/2016
 * @version $Id$
 * @since 5.19.0-SNAPSHOT
 */
public class RPSBlastBerkeleyMatchConverter extends BerkeleyMatchConverter<RPSBlastMatch> {

    private static final Logger LOG = Logger.getLogger(RPSBlastBerkeleyMatchConverter.class.getName());

    public RPSBlastMatch convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {

        Set<RPSBlastMatch.RPSBlastLocation> locations = new HashSet<>(berkeleyMatch.getLocations().size());

        for (BerkeleyLocation location : berkeleyMatch.getLocations()) {
            locations.add(new RPSBlastMatch.RPSBlastLocation(
                    valueOrZero(location.getStart()),
                    valueOrZero(location.getEnd()),
                    valueOrZero(location.getScore()),
                    valueOrZero(location.geteValue()),
                    null // TODO Add sites to berkeley DB?
            ));
        }

        return new RPSBlastMatch(signature, locations);
    }

}
