package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.MobiDBMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

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
public class MobiDBLookupMatchConverter extends LookupMatchConverter<MobiDBMatch, MobiDBMatch.MobiDBLocation> {

    private static final Logger LOG = Logger.getLogger(MobiDBLookupMatchConverter.class.getName());

    public MobiDBMatch convertMatch(SimpleLookupMatch match, Signature signature) {

        Set<MobiDBMatch.MobiDBLocation> locations = new HashSet<>(1);
        int locationStart = valueOrZero(match.getSequenceStart());
        int locationEnd = valueOrZero(match.getSequenceEnd());
        locations.add(new MobiDBMatch.MobiDBLocation(locationStart, locationEnd, match.getSeqFeature()));

        return new MobiDBMatch(signature, match.getModelAccession(), locations);
    }

}
