package uk.ac.ebi.interpro.scan.persistence.raw;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;

import java.util.Set;
import java.util.HashSet;

/**
 * Data access object methods for {@link RawMatch}es.
 *
 * @author  Phil Jones
 * @author  Antony Quinn
 * @version $Id$
 */
public class RawMatchDAOImpl <T extends RawMatch>
        extends GenericDAOImpl<T, Long>
        implements RawMatchDAO<T> {

    public RawMatchDAOImpl(Class<T> modelClass) {
        super(modelClass);
    }

    @Transactional
    @Override public void insertProteinMatches(Set<RawProtein<T>> rawProteins) {
        for (RawProtein<T> rawProtein : rawProteins){
            insert(new HashSet<T>(rawProtein.getMatches()));
        }
    }

    @Transactional(readOnly=true)
    @Override public T getMatchesByModel(String modelId) {
        return readSpecific(modelId);
    }

}
