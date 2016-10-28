package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.model.RPSBlastMatch;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SiteLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleySite;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleySiteLocation;
import uk.ac.ebi.interpro.scan.util.Utilities;

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
public class RPSBlastBerkeleyMatchConverter extends BerkeleyMatchConverter<RPSBlastMatch> {

    private static final Logger LOG = Logger.getLogger(RPSBlastBerkeleyMatchConverter.class.getName());

    public RPSBlastMatch convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {

        Set<RPSBlastMatch.RPSBlastLocation> locations = new HashSet<>(berkeleyMatch.getLocations().size());

        for (BerkeleyLocation location : berkeleyMatch.getLocations()) {
            Set<BerkeleySite> berkeleySites = location.getSites();
            Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> sites = new HashSet<>();
            for (BerkeleySite berkeleySite: berkeleySites){
                sites.addAll(convertSite(berkeleySite));
            }
            Utilities.verboseLog("Found sites: " + sites.toString());
            locations.add(new RPSBlastMatch.RPSBlastLocation(
                    valueOrZero(location.getStart()),
                    valueOrZero(location.getEnd()),
                    valueOrZero(location.getScore()),
                    valueOrZero(location.geteValue()),
                    sites // TODO Add sites to berkeley DB?
            ));
        }

        return new RPSBlastMatch(signature, locations);
    }

    public Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> convertSite(BerkeleySite berkeleySite){
        Set<RPSBlastMatch.RPSBlastLocation.RPSBlastSite> rpsBlastSites = new HashSet<>();
        for (BerkeleySiteLocation bsloc: berkeleySite.getSiteLocations()) {
            Set<SiteLocation> siteLocations = new HashSet<>();
            siteLocations.add(new SiteLocation(bsloc.getResidue(), bsloc.getStart(), bsloc.getEnd()));
            RPSBlastMatch.RPSBlastLocation.RPSBlastSite site = new RPSBlastMatch.RPSBlastLocation.RPSBlastSite(bsloc.getDescription(), siteLocations);
            rpsBlastSites.add(site);
        }

        return  rpsBlastSites;

    }
}
