package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.persistence.FilteredMatchKVDAO;
import uk.ac.ebi.interpro.scan.model.raw.SFLDHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.SFLDHmmer3RawSite;


/**
 * @author Gift Nuka
 *
 */

public class SFLDHmmer3FilteredMatchDAOImpl extends Hmmer3FilteredMatchAndSiteDAO<SFLDHmmer3RawMatch, SFLDHmmer3RawSite> {

    private FilteredMatchKVDAO<Hmmer3MatchWithSites, SFLDHmmer3RawMatch> filteredMatchKVDAO;

    public void setFilteredMatchKVDAO(FilteredMatchKVDAO filteredMatchKVDAO) {
        this.filteredMatchKVDAO = filteredMatchKVDAO;
    }

}
