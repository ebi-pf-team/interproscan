package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import uk.ac.ebi.interpro.scan.model.FingerPrintsMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.HashSet;
import java.util.Set;

/**
 * Converts a BerkeleyMatch to a PRINTS Match.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class FingerPrintsBerkeleyMatchConverter extends BerkeleyMatchConverter<FingerPrintsMatch> {

    private static final Logger LOG = LogManager.getLogger(FingerPrintsBerkeleyMatchConverter.class.getName());

    public FingerPrintsMatch convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {

        Set<FingerPrintsMatch.FingerPrintsLocation> locations = new HashSet<FingerPrintsMatch.FingerPrintsLocation>(berkeleyMatch.getLocations().size());
        int locationIndex = -1;
        BerkeleyLocation firstLocation = null;

        for (BerkeleyLocation location : berkeleyMatch.getLocations()) {
            locationIndex++;
            if (firstLocation == null) firstLocation = location;
            // Find the next position in the graphscan at which there is a match (not a period!)
            while (firstLocation.getHmmBounds().charAt(locationIndex) == '.') {
                locationIndex++;
            }
            locations.add(new FingerPrintsMatch.FingerPrintsLocation(
                    valueOrZero(location.getStart()),
                    valueOrZero(location.getEnd()),
                    valueOrZero(location.getpValue()),
                    valueOrZero(location.getScore()),
                    locationIndex + 1   // Motif number is 1 indexed.
            ));
        }
        if (firstLocation == null) {
            LOG.warn("The precalculated match lookup service has returned a PRINTS match with no locations:\n" + berkeleyMatch);
            return null;
        }

        return new FingerPrintsMatch(
                signature,
                berkeleyMatch.getSignatureModels(),
                valueOrZero(firstLocation.geteValue()),
                firstLocation.getHmmBounds() == null ? "" : firstLocation.getHmmBounds(),     // Note - Onion stores the graphscan in the HmmBounds column.
                locations
        );
    }

}
