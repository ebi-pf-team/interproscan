package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.FingerPrintsMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

import java.util.*;

public class PrintsLookupMatchConverter extends LookupMatchConverter<FingerPrintsMatch, FingerPrintsMatch.FingerPrintsLocation> {

    private static final Logger LOG = Logger.getLogger(PrintsLookupMatchConverter.class.getName());

    public FingerPrintsMatch convertMatch(SimpleLookupMatch match, Signature signature) {

        if (match == null || signature == null) {
            return null;
        }

        final String graphscan = match.getSeqFeature() == null ? "" : match.getSeqFeature();
        int locationIndex = 0;
        // Find the next position in the graphscan at which there is a match (not a period!)
        while (graphscan.charAt(locationIndex) == '.') {
            locationIndex++;
        }

        Set<FingerPrintsMatch.FingerPrintsLocation> locations = new HashSet<>(1);
        locations.add(new FingerPrintsMatch.FingerPrintsLocation(
                valueOrZero(match.getSequenceStart()),
                valueOrZero(match.getSequenceEnd()),
                valueOrZero(match.getLocationEValue()),
                valueOrZero(match.getSequenceScore()),
                locationIndex + 1)); // Motif number is 1 indexed

        return new FingerPrintsMatch(
                signature,
                match.getModelAccession(),
                valueOrZero(match.getSequenceEValue()),
                graphscan,
                locations
        );
    }

    @Override
    public List<FingerPrintsMatch> convertMatches(List<SimpleLookupMatch> simpleLookupMatches, Signature signature) {
        List<FingerPrintsMatch> matches = new ArrayList<>();
        if (simpleLookupMatches != null && simpleLookupMatches.size() > 0) {
            // Get ready to put all locations against a single match (check that all items in the list have the same
            // signature accession and model)
            FingerPrintsMatch match = null;
            String signatureModels = null;
            String graphscan = null;
            int locationIndex = -1;

            // PRINTS location index requires the matches to be in sorted order
            simpleLookupMatches.sort(Comparator.comparing(SimpleLookupMatch::getSequenceStart)
                .thenComparing(SimpleLookupMatch::getSequenceEnd));

            for (SimpleLookupMatch simpleMatch : simpleLookupMatches) {
                locationIndex++;
                checkSignatureAc(signature, simpleMatch);
                if (signatureModels == null) {
                    signatureModels = simpleMatch.getModelAccession();
                }
                else if (!signatureModels.equals(simpleMatch.getModelAccession())) {
                    throw new IllegalArgumentException("Match signature model "
                            + simpleMatch.getModelAccession()
                            + " does not match previous model " + signatureModels);
                }
                if (graphscan == null) {
                    graphscan = simpleMatch.getSeqFeature() == null ? "" : simpleMatch.getSeqFeature();
                }
                // Find the next position in the graphscan at which there is a match (not a period!)
                while (graphscan.charAt(locationIndex) == '.') {
                    locationIndex++;
                }
                FingerPrintsMatch.FingerPrintsLocation location = new FingerPrintsMatch.FingerPrintsLocation(
                    valueOrZero(simpleMatch.getSequenceStart()),
                    valueOrZero(simpleMatch.getSequenceEnd()),
                    valueOrZero(simpleMatch.getLocationEValue()), //pValue
                    valueOrZero(simpleMatch.getSequenceScore()),
                    locationIndex + 1   // Motif number is 1 indexed.
                );
                if (match == null) {
                    // Initialise the match using the first SimpleLookupMatch
                    Set<FingerPrintsMatch.FingerPrintsLocation> locations = new HashSet<>();
                    locations.add(location);
                    match = new FingerPrintsMatch(
                            signature,
                            signatureModels,
                            valueOrZero(simpleMatch.getSequenceEValue()),
                            graphscan,
                            locations
                    );
                }
                else {
                    // Add location to existing match
                    match.addLocation(location);
                }
            }
            matches.add(match); // One match with multiple locations
        }
        return matches;
    }


}
