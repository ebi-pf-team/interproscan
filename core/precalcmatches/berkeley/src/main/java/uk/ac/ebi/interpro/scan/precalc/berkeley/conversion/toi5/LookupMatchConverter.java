package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupSite;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.*;

public abstract class LookupMatchConverter<T extends Match, U extends Location> {

    private static final Logger LOGGER = Logger.getLogger(LookupStoreToI5ModelDAOImpl.class.getName());

    private boolean flattenMatches;

    @Required
    public void setFlattenMatches(boolean flattenMatches) {
        this.flattenMatches = flattenMatches;
    }

    public abstract T convertMatch(SimpleLookupMatch simpleLookupMatch, Set<String> sequenceSiteHits, Signature signature);

    public List<T> convertMatches(List<SimpleLookupMatch> simpleLookupMatches, Set<String> sequenceSiteHits, Signature signature) {
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
                    T match = convertMatch(simpleMatch, sequenceSiteHits, signature);
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
                    T match = convertMatch(simpleMatch, sequenceSiteHits, signature);
                    matches.add(match); // Multiple matches each with one location
                }
            }
        }
        return matches;
    }


    protected Map<String, Set<SiteLocation>> getSiteLocationsMap(SimpleLookupMatch simpleLookupMatch, Set<String> sequenceSiteHits, String signatureLibraryName,  String signatureAccession ){
        Utilities.verboseLog(30, "getSiteLocationsMap: " + sequenceSiteHits.size());

        Map<String, Set<SiteLocation>> mapSiteLocations = new HashMap();
        //Map<SiteLocation, String> siteLocationsDescptions = new HashMap();

        String md5 = simpleLookupMatch.getProteinMD5();
        int siteCount = 0;
        try {
            for (String sequenceSiteHit : sequenceSiteHits) {

                //SFLD,4,SFLDS00029,5,347,3,C,105,105, description
                SimpleLookupSite simpleLookupSite = new SimpleLookupSite(md5, sequenceSiteHit);

                if (simpleLookupSite.getSignatureLibraryName().equals(signatureLibraryName) && simpleLookupSite.getSignatureAccession().equals(signatureAccession)) {
                    siteCount ++;
                    String siteDescription = simpleLookupSite.getDescription();
                    if (siteDescription == null){
                        Utilities.verboseLog(30, "null description ....");
                        siteDescription = "nullDescription"; //this has to be dealth with specially as having null as a key is not a good idea
                    }
                    Set<SiteLocation> siteLocations = mapSiteLocations.get(siteDescription);
                    SiteLocation siteLocation = new SiteLocation(simpleLookupSite.getResidue(), simpleLookupSite.getResidueStart(), simpleLookupSite.getResidueEnd());

                    if (! siteInLocationRange(simpleLookupMatch, siteLocation)){
                        LOGGER.warn("site NOT In LocationRange");
                        continue;
                    }
                    if (siteLocations != null) {
                        if (siteLocations.contains(siteLocation)){
                            LOGGER.warn("Duplicate site location: " + siteLocation);
                        }
                        siteLocations.add(siteLocation);
                    } else {
                        siteLocations = new HashSet<>();
                        siteLocations.add(siteLocation);
                    }
                    mapSiteLocations.put(siteDescription, siteLocations);

                    Utilities.verboseLog(30,"" +
                            "siteLocation ...: " + siteLocation.toString() + " description: " + siteDescription);
                    //siteLocationsDescptions.put(siteLocation, siteDescription);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Utilities.verboseLog(30,"Processed for md5 - " + md5 + " accession - " + signatureAccession + " : " + siteCount
                + " of " + sequenceSiteHits.size() +  " site Locations with max of " + mapSiteLocations.keySet().size() + " descriptions");
        return mapSiteLocations;
        //return siteLocationsDescptions;
    }

    protected boolean siteInLocationRange(SimpleLookupMatch simpleLookupMatch, SiteLocation siteLocation){
        return siteLocation.getStart() >= simpleLookupMatch.getSequenceStart() && siteLocation.getEnd() <= simpleLookupMatch.getSequenceEnd();
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
