package uk.ac.ebi.interpro.scan.persistence.raw;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawProteinSite;
import uk.ac.ebi.interpro.scan.model.raw.RawSite;

import java.util.List;
import java.util.Set;

/**
 * Data access object methods for {@link RawSite}s.
 *
 * @author Gift Nuka
 * @version $Id$
 */
public interface RawSiteDAO<T extends RawSite>
        extends GenericDAO<T, Long> {

    /**
     * Inserts {@link RawSite}es contained within a {@link RawProtein} object.
     * Note: {@link RawProtein} is NOT persisted.
     *
     * @param rawSites Contains collection of {@link RawSite} to be persisted.
     */
    @Transactional
    public void insertSites(Set<RawProteinSite<T>> rawSites);

    /**
     * Retrieves sites using {@link uk.ac.ebi.interpro.scan.model.Model} IDs.
     *
     * @param modelId Corresponds to {@link uk.ac.ebi.interpro.scan.model.Model#getAccession()}
     * @return Sites
     */
    @Transactional(readOnly = true)
    public T getSitesByModel(String modelId);

    /**
     * Returns a List of
     * RawSite objects for the protein IDs in the range
     * specified (Database default String ordering)
     * <p/>
     * Essential for PFAM, PRINTS, etc post processing.
     *
     * @param bottomId                 return matches with protein IDs >= this String
     * @param topId                    the return matches with protein IDs <= this String
     * @param signatureDatabaseRelease
     * @return a List of
     *         RawMatch objects for the protein IDs in the range
     *         specified (Database default String ordering)
     */
    @Transactional(readOnly = true)
    default List<T> getActualRawMatchesForProteinIdsInRange(long bottomId, long topId, String signatureDatabaseRelease){
        throw new UnsupportedOperationException();
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
    public Set<T> getSitesByProteinIdRange(long bottomId, long topId, String signatureDatabaseRelease);

    /**
     * Returns Sistes with specified Protein IDs.
     * If the protein Id set is null or empty then an empty set of raw proteins shall be returned.
     *
     * @param proteinIds               Set of protein Ids to query
     * @param signatureDatabaseRelease Signature database release number.
     * @return Set of proteins with supplied IDs
     */
    @Transactional(readOnly = true)
    public Set<T> getSitesByProteinsIds(Set<Long> proteinIds, String signatureDatabaseRelease);

}
