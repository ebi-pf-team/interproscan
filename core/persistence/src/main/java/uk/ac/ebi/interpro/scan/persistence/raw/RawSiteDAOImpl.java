package uk.ac.ebi.interpro.scan.persistence.raw;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawProteinSite;
import uk.ac.ebi.interpro.scan.model.raw.RawSite;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.persistence.Query;
import java.util.*;

/**
 * Data access object methods for {@link RawSite}s.
 *
 * @author Gift Nuka
 * @version $Id$
 */
public class RawSiteDAOImpl<T extends RawSite>
        extends GenericDAOImpl<T, Long>
        implements RawSiteDAO<T> {

    private static final Logger LOGGER = Logger.getLogger(RawSiteDAOImpl.class.getName());

    public RawSiteDAOImpl(Class<T> modelClass) {
        super(modelClass);
    }

    @Transactional
//    @Override
    public void insertSites(Set<RawProteinSite<T>> rawSites) {
        for (RawProteinSite<T> rawProteinSite : rawSites) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Persisting " + rawSites.size() + " raw sites.");
            }
            Utilities.verboseLog("Persisting " + rawSites.size() + " raw sites.");
            insert(new HashSet<T>(rawProteinSite.getSites()));
        }
        entityManager.flush();

    }

    @Transactional(readOnly = true)
    @Override
    public T getSitesByModel(String modelId) {
        return readSpecific(modelId);
    }

    /**
     * Returns sites within the given ID range.
     *
     * @param bottomId                 Lower bound (protein.id >= bottomId)
     * @param topId                    Upper bound (protein.id <= topId)
     * @param signatureDatabaseRelease Signature database release number.
     * @return Proteins within the given ID range
     */
    @Transactional(readOnly = true)
    @Override
    public Set<T> getSitesByProteinIdRange(long bottomId, long topId,
                                                   String signatureDatabaseRelease) {
        // Get raw sites
        Query query = entityManager
                .createQuery(String.format("select p from %s p  " +
                        "where p.numericSequenceId >= :bottom " +
                        "and   p.numericSequenceId <= :top " +
                        "and   p.signatureLibraryRelease = :sigLibRelease", unqualifiedModelClassName))
                .setParameter("bottom", bottomId)
                .setParameter("top", topId)
                .setParameter("sigLibRelease", signatureDatabaseRelease);
        @SuppressWarnings("unchecked") List<T> list = query.getResultList();
        // Create raw proteins from raw sites
        Set<T> rawSites = new HashSet<T>();
        for (T rawSite : list) {
            String id = rawSite.getSequenceIdentifier();

            rawSites.add(rawSite);

        }
        return rawSites;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public Set<T> getSitesByProteinsIds(Set<Long> proteinIds,
                                               String signatureDatabaseRelease) {

        Set<T> rawSites = new HashSet<T>();
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

            for (T rawSite : list) {
                String id = rawSite.getSequenceIdentifier();

                rawSites.add(rawSite);

            }

        }
        return rawSites;
    }



}
