package uk.ac.ebi.interpro.scan.persistence.raw;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import javax.persistence.Query;
import java.util.*;

/**
 * Data access object methods for {@link RawMatch}es.
 *
 * @author Phil Jones
 * @author Antony Quinn
 * @version $Id$
 */
public class RawMatchDAOImpl<T extends RawMatch>
        extends GenericDAOImpl<T, Long>
        implements RawMatchDAO<T> {

    private static final Logger LOGGER = Logger.getLogger(RawMatchDAOImpl.class.getName());

    public RawMatchDAOImpl(Class<T> modelClass) {
        super(modelClass);
    }

    @Transactional
    @Override
    public void insertProteinMatches(Set<RawProtein<T>> rawProteins) {
        for (RawProtein<T> rawProtein : rawProteins) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Persisting " + rawProtein.getMatches().size() + " raw matches.");
            }
            insert(new HashSet<T>(rawProtein.getMatches()));
        }
        entityManager.flush();
    }

    @Transactional(readOnly = true)
    @Override
    public T getMatchesByModel(String modelId) {
        return readSpecific(modelId);
    }

    /**
     * Returns proteins within the given ID range.
     *
     * @param bottomId                 Lower bound (protein.id >= bottomId)
     * @param topId                    Upper bound (protein.id <= topId)
     * @param signatureDatabaseRelease Signature database release number.
     * @return Proteins within the given ID range
     */
    @Transactional(readOnly = true)
    @Override
    public Set<RawProtein<T>> getProteinsByIdRange(long bottomId, long topId,
                                                   String signatureDatabaseRelease) {
        // Get raw matches
        Query query = entityManager
                .createQuery(String.format("select p from %s p  " +
                        "where p.numericSequenceId >= :bottom " +
                        "and   p.numericSequenceId <= :top " +
                        "and   p.signatureLibraryRelease = :sigLibRelease", unqualifiedModelClassName))
                .setParameter("bottom", bottomId)
                .setParameter("top", topId)
                .setParameter("sigLibRelease", signatureDatabaseRelease);
        @SuppressWarnings("unchecked") List<T> list = query.getResultList();
        // Create raw proteins from raw matches
        Map<String, RawProtein<T>> map = new HashMap<String, RawProtein<T>>();
        for (T match : list) {
            String id = match.getSequenceIdentifier();
            RawProtein<T> rawProtein = map.get(id);
            if (rawProtein == null) {
                rawProtein = new RawProtein<T>(id);
                map.put(id, rawProtein);
            }
            rawProtein.addMatch(match);
        }
        return new HashSet<RawProtein<T>>(map.values());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public Set<RawProtein<T>> getProteinsByIds(Set<Long> proteinIds,
                                               String signatureDatabaseRelease) {

        Set<RawProtein<T>> rawProteins = new HashSet<RawProtein<T>>();
        if (proteinIds != null && proteinIds.size() >0) {
            // Get raw matches
            Query query = entityManager
                    .createQuery(String.format("select p from %s p  " +
                            "where p.numericSequenceId in(:ids)" +
                            "and   p.signatureLibraryRelease = :sigLibRelease", unqualifiedModelClassName))
                    .setParameter("ids", proteinIds)
                    .setParameter("sigLibRelease", signatureDatabaseRelease);
            @SuppressWarnings("unchecked") List<T> list = query.getResultList();
            // Create raw proteins from raw matches
            Map<String, RawProtein<T>> map = new HashMap<String, RawProtein<T>>();
            for (T match : list) {
                String id = match.getSequenceIdentifier();
                RawProtein<T> rawProtein = map.get(id);
                if (rawProtein == null) {
                    rawProtein = new RawProtein<T>(id);
                    map.put(id, rawProtein);
                }
                rawProtein.addMatch(match);
            }
            rawProteins =  new HashSet<RawProtein<T>>(map.values());
        }
        return rawProteins;
    }








    /**
     * Returns a Map of sequence identifiers to a List of
     * RawMatch objects for the protein IDs in the range
     * specified (Database default String ordering)
     * <p/>
     * Essential for Pfam-A HMMER3, PRINTS, etc post processing.
     *
     * @param bottomId                 return protein IDs >= this String
     * @param topId                    the return protein IDs <= this String
     * @param signatureDatabaseRelease
     * @return a Map of sequence identifiers to a List of
     *         RawMatch objects for the protein IDs in the range
     *         specified (Database default String ordering)
     */
    @Transactional(readOnly = true)
    public List<T>  getActualRawMatchesForProteinIdsInRange(
            long bottomId, long topId, String signatureDatabaseRelease) {
        Map<String, RawProtein<T>> proteinIdToMatchMap =
                new HashMap<String, RawProtein<T>>();

        // Get raw matches
        Query query = entityManager
                .createQuery(String.format("select p from %s  p  " +
                        "where p.numericSequenceId >= :bottom " +
                        "and   p.numericSequenceId <= :top " +
                        "and   p.signatureLibraryRelease = :sigLibRelease", unqualifiedModelClassName))
                .setParameter("bottom", bottomId)
                .setParameter("top", topId)
                .setParameter("sigLibRelease", signatureDatabaseRelease);
        @SuppressWarnings("unchecked") List<T> resultList = query.getResultList();

        return resultList;
    }

}
