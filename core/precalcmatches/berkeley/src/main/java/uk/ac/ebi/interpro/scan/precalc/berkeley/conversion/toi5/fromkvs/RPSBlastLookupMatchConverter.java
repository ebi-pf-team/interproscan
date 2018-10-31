package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.RPSBlastMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

import java.util.HashSet;
import java.util.Set;

public class RPSBlastLookupMatchConverter extends LookupMatchConverter<RPSBlastMatch> {

    private static final Logger LOG = Logger.getLogger(RPSBlastLookupMatchConverter.class.getName());

    public RPSBlastMatch convertMatch(SimpleLookupMatch match, Signature signature) {

        Set<RPSBlastMatch.RPSBlastLocation> locations = new HashSet<>(1);
        int locationStart = valueOrZero(match.getSequenceStart());
        int locationEnd = valueOrZero(match.getSequenceEnd());
        Double score = valueOrZero(match.getLocationScore());
        Double eValue = valueOrZero(match.getLocationEValue());
        // TODO Add sites to lookup service
        locations.add(new RPSBlastMatch.RPSBlastLocation(locationStart, locationEnd, score, eValue, null));

        return new RPSBlastMatch(signature, match.getModelAccession(), locations);
    }

}
