package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.model.Location;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class LookupMatchConverter<T extends Match, U extends Location> {

    private boolean flattenMatches;

    @Required
    public void setFlattenMatches(boolean flattenMatches) {
        this.flattenMatches = flattenMatches;
    }

    public abstract T convertMatch(SimpleLookupMatch simpleLookupMatch, Signature signature);

    public List<T> convertMatches(List<SimpleLookupMatch> simpleLookupMatches, Signature signature) {
        List<T> matches = new ArrayList<>();
        if (simpleLookupMatches != null && simpleLookupMatches.size() > 0) {
            //String signatureAc = signature.getAccession();
            if (flattenMatches) {
                // Put all locations against a single match (check that all items in the list have the same signature
                // accession and model)
                T firstMatch = null;
                String signatureModels = null;
                for (SimpleLookupMatch simpleMatch : simpleLookupMatches) {
                    checkSignatureAc(signature, simpleMatch);
                    if (signatureModels == null) {
                        signatureModels = simpleMatch.getModelAccession();
                    }
                    else if (!signatureModels.equals(simpleMatch.getModelAccession())) {
                            throw new IllegalArgumentException("Match signature model "
                                    + simpleMatch.getModelAccession()
                                    + " does not match previous model " + signatureModels);
                    }
                    T match = convertMatch(simpleMatch, signature);
                    if (firstMatch == null) {
                        firstMatch = match;
                    }
                    else {
                        Set<U> matchLocations = match.getLocations();
                        for (U matchLocation : matchLocations) {
                            firstMatch.addLocation(matchLocation);
                        }
                    }
                }
                matches.add(firstMatch); // One match with multiple locations
            }
            else {
                // Keep each hit as a separate match, each with it's own single location.
                for (SimpleLookupMatch simpleMatch : simpleLookupMatches) {
                    checkSignatureAc(signature, simpleMatch);
                    T match = convertMatch(simpleMatch, signature);
                    matches.add(match); // Multiple matches each with one location
                }
            }
        }
        return matches;
    }

    protected void checkSignatureAc(Signature signature, SimpleLookupMatch simpleMatch) {
        String signatureAc = signature.getAccession();
        if (!simpleMatch.getSignatureAccession().equals(signatureAc)) {
            throw new IllegalArgumentException("Match signature accession "
                    + simpleMatch.getSignatureAccession()
                    + " does not match provided signature " + signatureAc);
        }
    }

    protected static double valueOrZero(Double value) {
        if (value == null ||
                value.isInfinite() ||
                value.isNaN()) {
            return 0.0d;
        }
        return value;
    }

    protected static int valueOrZero(Integer value) {
        if (value == null) {
            return 0;
        }
        return value;
    }

    protected static long valueOrZero(Long value) {
        if (value == null) {
            return 0;
        }
        return value;
    }

    protected static String valueOrNotAvailable(String value) {
        return (value == null || value.isEmpty())
                ? "Not available"
                : value;
    }

    protected static String valueOrEmpty(String value) {
        return (value == null)
                ? ""
                : value;
    }


}
