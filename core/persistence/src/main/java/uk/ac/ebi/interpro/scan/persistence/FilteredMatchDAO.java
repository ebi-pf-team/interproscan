package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Collection;

/**
 * Filtered match data access object.
 *
 * @author Antony Quinn
 * @version $Id$
 */
public interface FilteredMatchDAO<T extends RawMatch, U extends Match> extends GenericDAO<U, Long> {

    /**
     * Persists filtered protein matches.
     *
     * @param filteredProteins Filtered protein matches.
     */
    @Transactional
    void persist(Collection<RawProtein<T>> filteredProteins);

}
