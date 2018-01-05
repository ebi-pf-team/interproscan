package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.io.match.coils.ParseCoilsMatch;
import uk.ac.ebi.interpro.scan.model.Match;

import java.util.List;
import java.util.Set;

/**
 * @author Gift Nuka
 */
public interface WriteOutputMatchDAO extends GenericDAO<Match, Long> {

    /**
     * Get all matches in kv store
     *
     */
    @Transactional
    List<Match> getMatches();

    void closeDB();
}
