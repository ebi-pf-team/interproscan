package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.PantherMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import javax.persistence.Column;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Phil Jones
 * @author Gift Nuka
 *         Date: 12/08/11
 *         Time: 11:14
 *         <p/>
 *         Converts matches retrieved from the Berkeley pre-calc match lookup service
 *         to the I5 match type, for PANTHER.
 */
public class PantherBerkeleyMatchConverter extends BerkeleyMatchConverter<PantherMatch> {

    //TODO: Add the e-value to the match location
    @Override
    public PantherMatch convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {
        if (berkeleyMatch == null || signature == null) {
            return null;
        }
        Set<PantherMatch.PantherLocation> locations = new HashSet<>(berkeleyMatch.getLocations().size());

        String annotationsNodeId = "";

        String annotations = "";
        for (BerkeleyLocation berkeleyLocation : berkeleyMatch.getLocations()) {

            //the annotationsNodeId is in the level columns
            annotationsNodeId = berkeleyLocation.getLevel();
            //the annotations is in the sequence features column
            annotations = berkeleyLocation.getSeqFeature();

            final HmmBounds bounds;
            if (berkeleyLocation.getHmmBounds() == null || berkeleyLocation.getHmmBounds().isEmpty()) {
                bounds = HmmBounds.COMPLETE;   // FUDGE!  HmmBounds cannot be null...
            } else {
                bounds = HmmBounds.parseSymbol(berkeleyLocation.getHmmBounds());
            }

            locations.add(new PantherMatch.PantherLocation(
                    berkeleyLocation.getStart(), berkeleyLocation.getEnd(),
                    valueOrZero(berkeleyLocation.getHmmStart()), valueOrZero(berkeleyLocation.getHmmEnd()), valueOrZero(berkeleyLocation.getHmmLength()),
                    bounds, valueOrZero(berkeleyLocation.getEnvelopeStart()), valueOrZero(berkeleyLocation.getEnvelopeEnd())
            ));
        }

        return new PantherMatch(
                signature,
                berkeleyMatch.getSignatureModels(),
                locations,
                annotationsNodeId,
                valueOrZero(berkeleyMatch.getSequenceEValue()),
                "Not available",
                valueOrZero(berkeleyMatch.getSequenceScore()),
                annotations
                );
    }
}
