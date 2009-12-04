package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.HmmerMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

import java.util.Collection;

/**
 * Method to persist filtered matches to the database
 * for Pfam / Hmmer3 results.
 * User: pjones
 * Date: Dec 3, 2009
 * Time: 3:39:23 PM
 */
public interface PfamFilteredMatchDAO extends GenericDAO<HmmerMatch,  Long> {

    /**
     * Persists filtered matches to the database that are referenced
     * from a RawProtein<PfamHmmer3RawMatch> object.
     * @param rawProtein containing a Collection of filtered PfamHmmer3RawMatch objects
     */
    void persistFilteredMatches (Collection<RawProtein<PfamHmmer3RawMatch>> rawProtein);
}
