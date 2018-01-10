package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.io.match.coils.ParseCoilsMatch;
import uk.ac.ebi.interpro.scan.model.CoilsMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;

import java.util.Set;

/**
 * @author Gift Nuka
 *
 */
public interface CoilsMatchKVDAO extends FilteredMatchKVDAO<CoilsMatch, RawMatch> {

    /**
     * Persists a set of ParseCoilsMatch objects as filtered matches:
     * there is no filtering step with Coils.
     *
     * @param coilsMatches being a Set of ParseCoilsMatch objects to be persisted.
     */
    @Transactional
    void persist(Set<ParseCoilsMatch> coilsMatches);
}
