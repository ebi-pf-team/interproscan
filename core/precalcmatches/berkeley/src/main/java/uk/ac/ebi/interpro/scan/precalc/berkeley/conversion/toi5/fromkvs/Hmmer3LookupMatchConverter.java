package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.DCStatus;
import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.HashSet;
import java.util.Set;

/**
 * Converts a LookupMatch to a HMMER3 Match.
 *
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class Hmmer3LookupMatchConverter extends LookupMatchConverter<Hmmer3Match, Hmmer3Match.Hmmer3Location> {

    private static final Logger LOG = LogManager.getLogger(Hmmer3LookupMatchConverter.class.getName());

    public Hmmer3Match convertMatch(SimpleLookupMatch match, Set<String> sequenceSiteHits, Signature signature) {

        Utilities.verboseLog(110, "Considering hit:" + match.toString());

        final String sln = match.getSignatureLibraryName();
        boolean postProcessed = false;
        if (sln.equalsIgnoreCase("GENE3D") || sln.equalsIgnoreCase("PFAM") || sln.equalsIgnoreCase("PIRSF")) {
            postProcessed = true;
        }

        final Set<Hmmer3Match.Hmmer3Location> locations = new HashSet<>(1);  //we may have more than one location in a new way of processing lookupmatches

        int locationStart = valueOrZero(match.getSequenceStart());
        int locationEnd = valueOrZero(match.getSequenceEnd());

        int envStart = match.getEnvelopeStart() == null
                ? (match.getEnvelopeStart() == null ? 0 : locationStart)
                : match.getEnvelopeStart();
        int envEnd =  match.getEnvelopeEnd() == null
                ? match.getSequenceEnd() == null ? 0 : locationEnd
                : match.getEnvelopeEnd();

        String [] fragmentsTokens =  match.getFragments().split(";");
        final Set<Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment> locationFragments = new HashSet<>(fragmentsTokens.length);
        for(String fragmentsToken: fragmentsTokens){
            String [] fragmentCoordinates =  fragmentsToken.split("-");
            int fragStart = valueOrZero(Integer.parseInt(fragmentCoordinates[0]));
            int fragEnd = valueOrZero(Integer.parseInt(fragmentCoordinates[1]));
            String dcStatus = fragmentCoordinates[2];
            locationFragments.add(new Hmmer3Match.Hmmer3Location.Hmmer3LocationFragment(fragStart, fragEnd, DCStatus.parseSymbol(dcStatus)));
            Utilities.verboseLog(140, "LocationFragments:" + locationFragments.toString());
        }

        final HmmBounds bounds = HmmBounds.parseSymbol(HmmBounds.calculateHmmBounds(envStart, envEnd, locationStart, locationEnd));

        locations.add(new Hmmer3Match.Hmmer3Location(
                locationStart,
                locationEnd,
                valueOrZero(match.getLocationScore()),
                valueOrZero(match.getLocationEValue()),
                valueOrZero(match.getHmmStart()),
                valueOrZero(match.getHmmEnd()),
                valueOrZero(match.getHmmLength()),
                bounds,
                envStart,
                envEnd,
                postProcessed,
                locationFragments
        ));
        Utilities.verboseLog(140, "Locations:" + locations.toString());

//                public Hmmer3Location(int start, int end, double score, double evalue,
//        int hmmStart, int hmmEnd, int hmmLength, HmmBounds hmmBounds,
//        int envelopeStart, int envelopeEnd, boolean postProcessed, Set<Hmmer3LocationFragment> locationFragments)
        Hmmer3Match hmmer3Match = new Hmmer3Match(
                signature,
                match.getModelAccession(),
                valueOrZero(match.getSequenceScore()),
                valueOrZero(match.getSequenceEValue()),
                locations
        );
        Utilities.verboseLog(140, "hmmer3Match:" + hmmer3Match.toString());

        return hmmer3Match;
    }

}
