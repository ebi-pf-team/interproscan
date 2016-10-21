package uk.ac.ebi.interpro.scan.io.match;

import uk.ac.ebi.interpro.scan.io.getorf.MatchSiteData;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawSite;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Parser for matches and sites.
 */
public interface MatchAndSiteParser<T extends RawMatch, U extends RawSite> extends MatchParser<T> {

    MatchSiteData<T, U> parseMatchesAndSites(InputStream is) throws IOException;

    default Set<RawProtein<T>> parse(InputStream is) throws IOException {
        MatchSiteData matchSiteData = parseMatchesAndSites(is);
        return new HashSet<>(matchSiteData.getRawProteins());
    }


}