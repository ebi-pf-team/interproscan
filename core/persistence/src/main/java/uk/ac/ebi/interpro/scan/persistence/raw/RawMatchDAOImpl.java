package uk.ac.ebi.interpro.scan.persistence.raw;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;

import javax.persistence.Query;
import java.util.*;

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

    /**
     * Returns proteins within the given ID range.
     *
     * @param bottomId                  Lower bound (protein.id >= bottomId)
     * @param topId                     Upper bound (protein.id <= topId)
     * @param signatureDatabaseRelease  Signature database release number.
     * @return Proteins within the given ID range
     */
    @Transactional(readOnly=true)
    @Override public Set<RawProtein<T>> getProteinsByIdRange (String bottomId, String topId,
                                                              String signatureDatabaseRelease) {
        Query query = entityManager
                .createQuery(String.format("select p from %s p  " +
                             "where p.sequenceIdentifier >= :bottom " +
                             "and   p.sequenceIdentifier <= :top " +
                             "and   p.signatureLibraryRelease = :sigLibRelease", unqualifiedModelClassName))
                .setParameter("bottom", bottomId)
                .setParameter("top", topId)
                .setParameter("sigLibRelease", signatureDatabaseRelease);
        @SuppressWarnings("unchecked") List<RawProtein<T>> list = query.getResultList();
        return new HashSet<RawProtein<T>>(list);
    }    

}
