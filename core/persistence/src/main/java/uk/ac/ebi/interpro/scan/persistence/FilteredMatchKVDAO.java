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
public interface FilteredMatchKVDAO <U extends Match, T extends RawMatch>  extends GenericDAO<U, Long>  {

    /**
     * Persists filtered protein matches.
     *
     * @param keyToMatchMap Filtered protein matches with their keyIds.
     */
    @Transactional
    void persist(final Map<String, U> keyToMatchMap);

    /**
     * Persists filtered protein matches.
     *
     * @param filteredProteins Filtered protein matches.
     */
    @Transactional
    void persist(Collection<RawProtein<T>> filteredProteins);

    /**
     * This is the method that should be implemented by specific FilteredMatchDAOImpl's to
     * persist filtered matches.
     *
     * @param filteredProteins             being the Collection of filtered RawProtein objects to persist
     * @param modelAccessionToSignatureMap a Map of model accessions to Signature objects.
     * @param proteinIdToProteinMap        a Map of Protein IDs to Protein objects
     */
    @Transactional
    default  void persist(Collection<RawProtein<T>> filteredProteins,
                                    final Map<String, Signature> modelAccessionToSignatureMap,
                                    final Map<String, Protein> proteinIdToProteinMap) {
        //if this class is called but not implemeneted, thow an exception
        throw new UnsupportedOperationException();
    }

    @Transactional
    void persist(String key,  U match);

    @Transactional
    void persist(byte[] key,  byte[] match);

    void setLevelDBStore(LevelDBStore levelDBStore);

    void closeDB();
}
