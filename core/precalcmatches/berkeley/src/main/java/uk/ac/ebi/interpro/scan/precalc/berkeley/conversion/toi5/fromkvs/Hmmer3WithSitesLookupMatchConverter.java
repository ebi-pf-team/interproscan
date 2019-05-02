package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromkvs;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.LookupMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Converts a LookupMatch to a HMMER3 Match.
 *
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class Hmmer3WithSitesLookupMatchConverter extends LookupMatchConverter<Hmmer3MatchWithSites, Hmmer3MatchWithSites.Hmmer3LocationWithSites> {

    private static final Logger LOG = Logger.getLogger(Hmmer3WithSitesLookupMatchConverter.class.getName());

    public Hmmer3MatchWithSites convertMatch(SimpleLookupMatch match, Set<String> sequenceSiteHits, Signature signature) {

        Utilities.verboseLog(30, " Hmmer3WithSitesLookupMatchConverter for " + match.getProteinMD5() +  " start: " + match.getSequenceStart() +  " end:" + match.getSequenceEnd());
        final String signatureLibraryName = match.getSignatureLibraryName();
        final String signatureAccession = match.getSignatureAccession();

        boolean postProcessed = false;
        if (signatureLibraryName.equalsIgnoreCase("GENE3D") || signatureLibraryName.equalsIgnoreCase("PFAM")) {
            postProcessed = true;
        }

        //final Set<Hmmer3Match.Hmmer3Location> locations = new HashSet<>(1);  //we may have more than one location in a new way of processing lookupmatches

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
        }

        final HmmBounds bounds = HmmBounds.parseSymbol(HmmBounds.calculateHmmBounds(envStart, envEnd, locationStart, locationEnd));

        Utilities.verboseLog(30, " locationFragments : " + locationFragments.size());

        //Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site> sites = new HashSet<>();
        Set<HmmerLocationWithSites.HmmerSite> sites = new HashSet<>();
        //Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites.HmmerSite> sites = new HashSet<>();
        if (sequenceSiteHits != null) {
            if (sequenceSiteHits.size() > 0) {
                Map<String, Set<SiteLocation>> mapSiteLocations = getSiteLocationsMap(match.getProteinMD5(), sequenceSiteHits, signatureLibraryName, signatureAccession);
                Utilities.verboseLog(30, "mapSiteLocations: " + mapSiteLocations.keySet());



                for (String siteDescription : mapSiteLocations.keySet()) {
                    Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site site = new Hmmer3MatchWithSites.Hmmer3LocationWithSites.Hmmer3Site(
                            siteDescription, mapSiteLocations.get(siteDescription));
                    sites.add(site);
                }
            }
        }
        Utilities.verboseLog(30," sites : " + sites.size());

        Hmmer3MatchWithSites.Hmmer3LocationWithSites location = new Hmmer3MatchWithSites.Hmmer3LocationWithSites(
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
                sites
        );

        Utilities.verboseLog(30," location : " + location);
        Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites> locations = new HashSet<>();
        locations.add(location);

        Utilities.verboseLog(30, " now create SFLD match ");

        return new Hmmer3MatchWithSites(signature,
                match.getModelAccession(),
                valueOrZero(match.getSequenceScore()),
                valueOrZero(match.getSequenceEValue()), locations);

    }

}
