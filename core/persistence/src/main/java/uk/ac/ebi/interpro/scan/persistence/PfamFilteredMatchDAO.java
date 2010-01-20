package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Hmmer3Match;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

import java.util.Collection;

/**
 * Persists filtered Pfam HMMER3 matches to the database.
 *
 * @author  Phil Jones
 * @version $Id$
 */
public interface PfamFilteredMatchDAO extends GenericDAO<Hmmer3Match,  Long> {

    /**
     * Persists filtered matches to the database that are referenced
     * from a RawProtein<PfamHmmer3RawMatch> object.
     * @param rawProtein containing a Collection of filtered PfamHmmer3RawMatch objects
     */
    void persistFilteredMatches (Collection<RawProtein<PfamHmmer3RawMatch>> rawProtein);
    
}
