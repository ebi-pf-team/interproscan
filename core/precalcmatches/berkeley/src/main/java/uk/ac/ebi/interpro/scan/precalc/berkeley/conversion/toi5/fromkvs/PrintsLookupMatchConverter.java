package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.FingerPrintsMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

import java.util.HashSet;
import java.util.Set;

public class PrintsLookupMatchConverter extends LookupMatchConverter<FingerPrintsMatch> {

    private static final Logger LOG = Logger.getLogger(PrintsLookupMatchConverter.class.getName());

    public FingerPrintsMatch convertMatch(SimpleLookupMatch match, Signature signature) {

        if (match == null || signature == null) {
            return null;
        }

        Set<FingerPrintsMatch.FingerPrintsLocation> locations = new HashSet<>(1);
        locations.add(new FingerPrintsMatch.FingerPrintsLocation(
                valueOrZero(match.getSequenceStart()),
                valueOrZero(match.getSequenceEnd()),
                valueOrZero(match.getLocationScore()),
                valueOrZero(match.getSequenceScore()),
                0)); // TODO Implement motif number

//        int locationIndex = -1;
//        BerkeleyLocation firstLocation = null;
//
//        for (BerkeleyLocation location : match.getLocations()) {
//            locationIndex++;
//            if (firstLocation == null) firstLocation = location;
//            // Find the next position in the graphscan at which there is a match (not a period!)
//            while (firstLocation.getHmmBounds().charAt(locationIndex) == '.') {
//                locationIndex++;
//            }
//            locations.add(new FingerPrintsMatch.FingerPrintsLocation(
//                    valueOrZero(location.getStart()),
//                    valueOrZero(location.getEnd()),
//                    valueOrZero(location.getpValue()),
//                    valueOrZero(location.getScore()),
//                    locationIndex + 1   // Motif number is 1 indexed.
//            ));
//        }
//        if (firstLocation == null) {
//            LOG.warn("The precalculated match lookup service has returned a PRINTS match with no locations:\n" + match);
//            return null;
//        }

        return new FingerPrintsMatch(
                signature,
                match.getModelAccession(),
                valueOrZero(match.getSequenceEValue()),
                match.getHmmBounds() == null ? "" : match.getHmmBounds(),     // Note - Onion stores the graphscan in the HmmBounds column.
                locations
        );
    }

}
