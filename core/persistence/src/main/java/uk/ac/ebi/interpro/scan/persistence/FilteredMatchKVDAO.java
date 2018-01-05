package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.Signature;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Collection;
import java.util.Map;

/**
 * Filtered match data KV access object.
 *
 * @author Gift Nuka
 * @version $Id$
 */
public interface FilteredMatchKVDAO <T extends Match>  extends GenericDAO<T, Long>  {

    /**
     * Persists filtered protein matches.
     *
     * @param keyToMatchMap Filtered protein matches with their keyIds.
     */
    @Transactional
    void persist(final Map<String, T> keyToMatchMap);

    @Transactional
    void persist(String key,  T match);

    @Transactional
    void persist(byte[] key,  byte[] match);

    void setLevelDBStore(LevelDBStore levelDBStore);

    void closeDB();
}
