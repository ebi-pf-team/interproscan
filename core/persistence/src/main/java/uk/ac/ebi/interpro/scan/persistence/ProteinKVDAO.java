package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Protein;

import java.util.List;
import java.util.Map;

/**
 * Filtered match data KV access object.
 *
 * @author Gift Nuka
 * @version $Id$
 */
public interface ProteinKVDAO  extends GenericDAO<Protein, Long>  {

    /**
     * Persists filtered protein matches.
     *
     * @param keyToProteinMap Filtered protein matches with their keyIds.
     */
    @Transactional
    void persist(final Map<String, Protein> keyToProteinMap);

    @Transactional
    void insert(String key, Protein protein);

    @Transactional
    void persist(byte[] key, byte[] protein);

    @Transactional
    Protein getProtein(String key);

    @Transactional(readOnly = true)
    List<Protein> getProteins();

    @Transactional(readOnly = true)
    Map<String, Protein> getKeyToProteinMap();

    void setLevelDBStore(LevelDBStore levelDBStore);

    void closeDB();
}
