package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.FingerPrintsMatch;
import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Collection;

/**
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public interface PrintsFilteredMatchDAO extends GenericDAO<FingerPrintsMatch, Long> {

    /**
     * Persists filtered matches to the database that are referenced
     * from a RawProtein<PrintsRawMatch> object.
     *
     * @param rawProtein containing a Collection of filtered PrintsRawMatch objects
     */
    void persistFilteredMatches(Collection<RawProtein<PrintsRawMatch>> rawProtein);
}
