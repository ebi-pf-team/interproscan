package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.RPSBlastMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SiteLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupSite;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RPSBlastLookupMatchConverter extends LookupMatchConverter<RPSBlastMatch, RPSBlastMatch.RPSBlastLocation> {

    private static final Logger LOG = Logger.getLogger(RPSBlastLookupMatchConverter.class.getName());

    public RPSBlastMatch convertMatch(SimpleLookupMatch match, Set<String> sequenceSiteHits, Signature signature) {
        Utilities.verboseLog(30, " RPSBlastLookupMatchConverter for " + match.getProteinMD5() + " start: " + match.getSequenceStart() + " end:" + match.getSequenceEnd());

        final String signatureLibraryName = match.getSignatureLibraryName();
        final String signatureAccession = match.getSignatureAccession();

        Set<RPSBlastMatch.RPSBlastLocation> locations = new HashSet<>(1);
        int locationStart = valueOrZero(match.getSequenceStart());
        int locationEnd = valueOrZero(match.getSequenceEnd());
        Double score = valueOrZero(match.getLocationScore());
        Double eValue = valueOrZero(match.getLocationEValue());
        // TODO Add sites to lookup service
        //Set<String> sequenceSiteHits = new HashSet<>();  //just for prototyping
        Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> sites = null;
        int siteCount = 0;
        if (sequenceSiteHits != null && sequenceSiteHits.size() > 0) {
            Utilities.verboseLog(30, "Sites not null ... get sitelocations");
            sites = new HashSet<>();
            Map<String, Set<SiteLocation>> mapSiteLocations = getSiteLocationsMap(match.getProteinMD5(), sequenceSiteHits, signatureLibraryName, signatureAccession);
            //Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> sites = convertSites(match.getProteinMD5(), sequenceSiteHits);
            //for (SiteLocation siteLocation: mapSiteLocations.keySet()) {
            //String siteDescription = mapSiteLocations.get(siteLocation);

                for (String siteDescription : mapSiteLocations.keySet()) {
                    Set<SiteLocation> siteLocations = mapSiteLocations.get(siteDescription);
                    siteCount += siteLocations.size();
                    if (siteDescription.equalsIgnoreCase("nullDescription")){
                        siteDescription = null;
                    }
                    RPSBlastMatch.RPSBlastLocation.RPSBlastSite site = new RPSBlastMatch.RPSBlastLocation.RPSBlastSite(siteDescription, siteLocations);
                    sites.add(site);
                }
            //}

        }else{
            Utilities.verboseLog(30, "Sites is null ... ");
        }
        Utilities.verboseLog(30, "Sites  ... " + sites + " with " + siteCount + " locations");
        locations.add(new RPSBlastMatch.RPSBlastLocation(locationStart, locationEnd, score, eValue, sites));

        return new RPSBlastMatch(signature, match.getModelAccession(), locations);
    }


    private Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> convertSites(String md5, Set<String> sequenceSiteHits ){
        Map<String, Set<SiteLocation>> mapSiteLocations = new HashMap();
        for (String sequenceSiteHit : sequenceSiteHits) {
            //SFLD,4,SFLDS00029,5,347,3,C,105,105, description
            SimpleLookupSite simpleLookupSite =  new SimpleLookupSite(md5, sequenceSiteHit);
            String siteDescription = simpleLookupSite.getDescription();
            Set<SiteLocation> siteLocations = mapSiteLocations.get(siteDescription);
            SiteLocation siteLocation = new SiteLocation(simpleLookupSite.getResidue(), simpleLookupSite.getResidueStart(), simpleLookupSite.getResidueEnd());
            if(siteLocations != null){
                siteLocations.add(siteLocation);
            }else{
                siteLocations = new HashSet<>();
                siteLocations.add(siteLocation);
            }
            mapSiteLocations.put(siteDescription, siteLocations);

        }
        Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> sites = new HashSet<>();
        for (String siteDescription : mapSiteLocations.keySet()){
            RPSBlastMatch.RPSBlastLocation.RPSBlastSite site = new RPSBlastMatch.RPSBlastLocation.RPSBlastSite(siteDescription, mapSiteLocations.get(siteDescription));
            sites.add(site);
        }


        return sites;
    }

}
