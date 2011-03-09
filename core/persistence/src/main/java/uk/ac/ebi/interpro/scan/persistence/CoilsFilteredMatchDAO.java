package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.io.match.coils.ParseCoilsMatch;
import uk.ac.ebi.interpro.scan.model.CoilsMatch;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 23-Jun-2010
 * Time: 12:34:03
 * To change this template use File | Settings | File Templates.
 */
public interface CoilsFilteredMatchDAO extends GenericDAO<CoilsMatch, Long> {

    /**
     * Persists a set of ParseCoilsMatch objects as filtered matches:
     * there is no filtering step with Coils.
     *
     * @param coilsMatches being a Set of ParseCoilsMatch objects to be persisted.
     */
    @Transactional
    void persist(Set<ParseCoilsMatch> coilsMatches);
}
