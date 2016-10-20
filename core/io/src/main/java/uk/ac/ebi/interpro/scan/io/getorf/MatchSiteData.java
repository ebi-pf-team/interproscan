package uk.ac.ebi.interpro.scan.io.getorf;


import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawProteinSite;
import uk.ac.ebi.interpro.scan.model.raw.RawSite;

import java.util.Set;

/**
 * Created by nuka on 03/05/16.
 */
public class MatchSiteData<T extends RawMatch, U extends RawSite> {

    final Set<RawProtein<T>> rawProteins;
    final Set<RawProteinSite<U>> rawProteinSites;

    public MatchSiteData(Set<RawProtein<T>> rawProteins, Set<RawProteinSite<U>> rawProteinSites) {
        this.rawProteins = rawProteins;
        this.rawProteinSites = rawProteinSites;
    }

    public Set<RawProtein<T>> getRawProteins() {
        return rawProteins;
    }

    public Set<RawProteinSite<U>> getRawProteinSites() {
        return rawProteinSites;
    }

    @Override
    public String toString() {
        return "MatchSiteData{" +
                "rawProteins=" + rawProteins +
                ", rawProteinSites=" + rawProteinSites +
                '}';
    }
}
