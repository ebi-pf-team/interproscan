package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.DCStatus;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SuperFamilyHmmer3Match;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.HashSet;
import java.util.Set;

public class SuperfamilyLookupMatchConverter extends LookupMatchConverter<SuperFamilyHmmer3Match, SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> {

    private static final Logger LOG = Logger.getLogger(SuperfamilyLookupMatchConverter.class.getName());

    //TODO: Add the e-value to the match location
    @Override
    public SuperFamilyHmmer3Match convertMatch(SimpleLookupMatch match, Set<String> sequenceSiteHits, Signature signature) {
        if (match == null || signature == null) {
            return null;
        }

        Utilities.verboseLog(120, "Considering hit:" + match.toString());

        Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location> locations = new HashSet<>(1);
        int locationStart = valueOrZero(match.getSequenceStart());
        int locationEnd = valueOrZero(match.getSequenceEnd());
        int hmmLength = valueOrZero(match.getHmmLength());

        String [] fragmentsTokens =  match.getFragments().split(";");
        final Set<SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment> locationFragments = new HashSet<>(fragmentsTokens.length);

        for(String fragmentsToken: fragmentsTokens){
            String [] fragmentCoordinates =  fragmentsToken.split("-");
            int fragStart = valueOrZero(Integer.parseInt(fragmentCoordinates[0]));
            int fragEnd = valueOrZero(Integer.parseInt(fragmentCoordinates[1]));
            String dcStatus = fragmentCoordinates[2];
            locationFragments.add(new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location.SuperFamilyHmmer3LocationFragment(fragStart, fragEnd, DCStatus.parseSymbol(dcStatus)));
            Utilities.verboseLog(140, "LocationFragments:" + locationFragments.toString());
        }

        locations.add(new SuperFamilyHmmer3Match.SuperFamilyHmmer3Location(
                locationStart,
                locationEnd,
                locationFragments,
                hmmLength
        ));
        Utilities.verboseLog(140, "Locations:" + locations.toString());

        SuperFamilyHmmer3Match superFamilyHmmer3Match = new SuperFamilyHmmer3Match(
                signature,
                match.getModelAccession(),
                valueOrZero(match.getSequenceEValue()),
                locations
        );
        Utilities.verboseLog(140, "superFamilyHmmer3Match:" + superFamilyHmmer3Match.toString());
        return superFamilyHmmer3Match;
    }
}
