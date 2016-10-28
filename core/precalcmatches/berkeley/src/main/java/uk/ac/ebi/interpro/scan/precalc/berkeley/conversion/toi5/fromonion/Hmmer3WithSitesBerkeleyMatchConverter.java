package uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.fromonion;

import uk.ac.ebi.interpro.scan.model.HmmBounds;
import uk.ac.ebi.interpro.scan.model.Hmmer3MatchWithSites;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.SiteLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.BerkeleyMatchConverter;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleySite;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleySiteLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * Converts a BerkeleyMatch to a HMMER3 Match.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class Hmmer3WithSitesBerkeleyMatchConverter extends BerkeleyMatchConverter<Hmmer3MatchWithSites> {

    public Hmmer3MatchWithSites convertMatch(BerkeleyMatch berkeleyMatch, Signature signature) {

        final Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites> locations = new HashSet<Hmmer3MatchWithSites.Hmmer3LocationWithSites>(berkeleyMatch.getLocations().size());

        for (BerkeleyLocation location : berkeleyMatch.getLocations()) {
            Set<BerkeleySite> berkeleySites = location.getSites();
            Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites.HmmerSite> sites = new HashSet<>();
            for (BerkeleySite berkeleySite: berkeleySites){
                sites.addAll(convertSite(berkeleySite));
            }

            final HmmBounds bounds;
            if (location.getHmmBounds() == null || location.getHmmBounds().isEmpty()) {
                bounds = HmmBounds.COMPLETE;   // FUDGE!  HmmBounds cannot be null...
            } else {
                bounds = HmmBounds.parseSymbol(location.getHmmBounds());
            }

            locations.add(new Hmmer3MatchWithSites.Hmmer3LocationWithSites(
                    valueOrZero(location.getStart()),
                    valueOrZero(location.getEnd()),
                    valueOrZero(location.getScore()),
                    valueOrZero(location.geteValue()),
                    valueOrZero(location.getHmmStart()),
                    valueOrZero(location.getHmmEnd()),
                    bounds,
                    location.getEnvelopeStart() == null
                            ? (location.getStart() == null ? 0 : location.getStart())
                            : location.getEnvelopeStart(),
                    location.getEnvelopeEnd() == null
                            ? location.getEnd() == null ? 0 : location.getEnd()
                            : location.getEnvelopeEnd(),
                    sites
            ));
        }

        return new Hmmer3MatchWithSites(
                signature,
                valueOrZero(berkeleyMatch.getSequenceScore()),
                valueOrZero(berkeleyMatch.getSequenceEValue()),
                locations
        );
    }

    public Set<Hmmer3MatchWithSites.Hmmer3LocationWithSites.HmmerSite> convertSite(BerkeleySite berkeleySite){

        Set< Hmmer3MatchWithSites.Hmmer3LocationWithSites.HmmerSite> hmmer3Sites = new HashSet<>();
        for (BerkeleySiteLocation bsloc: berkeleySite.getSiteLocations()) {
            Set<SiteLocation> siteLocations = new HashSet<>();
            siteLocations.add(new SiteLocation(bsloc.getResidue(), bsloc.getStart(), bsloc.getEnd()));
            Hmmer3MatchWithSites.Hmmer3LocationWithSites.HmmerSite site = new Hmmer3MatchWithSites.Hmmer3LocationWithSites.HmmerSite(bsloc.getDescription(), siteLocations);
            hmmer3Sites.add(site);
        }

        return  hmmer3Sites;

    }
}
