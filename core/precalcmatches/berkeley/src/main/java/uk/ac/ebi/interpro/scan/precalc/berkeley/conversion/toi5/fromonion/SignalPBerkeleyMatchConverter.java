package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import uk.ac.ebi.interpro.scan.model.SignalPMatch;
import uk.ac.ebi.interpro.scan.model.SignalPOrganismType;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 9/5/11
 * Time: 11:53 AM
 * Converts SignalP 3 matches from Berkeley matches to be retrievable from I5.
 * <p/>
 * Note: Onion analysis types:
 * <p/>
 * 1	SIGNALP_EUK
 * 2	SIGNALP_GRAM+
 * 3	SIGNALP_GRAM-
 */
public class SignalPBerkeleyMatchConverter extends BerkeleyMatchConverter<SignalPMatch> {

    public SignalPMatch convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {

        if (berkeleyMatch == null || signature == null) {
            return null;
        }
        if (berkeleyMatch.getLocations() == null || berkeleyMatch.getLocations().size() != 1) {
            throw new IllegalStateException("A SignalP Berkeley match has been returned that does not have exactly one location: " + berkeleyMatch.toString());
        }
        final BerkeleyLocation berkeleyLocation = berkeleyMatch.getLocations().iterator().next();
        final SignalPMatch.SignalPLocation location = new SignalPMatch.SignalPLocation(berkeleyLocation.getStart(), berkeleyLocation.getEnd());
        final SignalPOrganismType type = SignalPOrganismType.getSignalPOrganismTypeByOnionType(berkeleyMatch.getSignatureLibraryName());
        if (type == null) {
            return null;
        }

        return new SignalPMatch(signature, berkeleyMatch.getSignatureModels(), type, Collections.singleton(location));
    }

}
