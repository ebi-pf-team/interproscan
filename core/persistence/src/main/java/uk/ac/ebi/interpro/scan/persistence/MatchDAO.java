package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Match;

import java.util.Map;
import java.util.Set;

public interface MatchDAO<T extends Match>  extends GenericKVDAO<T>  {

    void persist(String key, Set<Match> matches);

    Set<Match> getMatchSet(String key);

    Map<String, Set<Match>> getMatchesForEachProtein() throws Exception;;

    Set<Match> getMatches() throws Exception;
}
