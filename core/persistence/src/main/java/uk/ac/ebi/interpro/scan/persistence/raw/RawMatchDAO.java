package uk.ac.ebi.interpro.scan.persistence.raw;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import java.util.Set;
import java.util.Collection;

/**
 * Data access object methods for {@link RawMatch}es.
 *
 * @author  Phil Jones
 * @author  Antony Quinn
 * @version $Id$
 */
public interface RawMatchDAO<T extends RawMatch> 
        extends GenericDAO<T, Long> {

    /**
     * Inserts {@link RawMatch}es contained within a {@link RawProtein} object.
     * Note: {@link RawProtein} is NOT persisted.
     *
     * @param rawProteins Contains collection of {@link RawMatch} to be persisted.
     */
    @Transactional
    public void insertProteinMatches(Set<RawProtein<T>> rawProteins);

    /**
     * Retrieves matches using {@link uk.ac.ebi.interpro.scan.model.Model} IDs.
     * 
     * @param  modelId Corresponds to {@link uk.ac.ebi.interpro.scan.model.Model#getAccession()}
     * @return Matches
     */
    public T getMatchesByModel (String modelId);

    /**
     * Returns proteins within the given ID range.
     *
     * @param bottomId                  Lower bound (protein.id >= bottomId)
     * @param topId                     Upper bound (protein.id <= topId)
     * @param signatureDatabaseRelease  Signature database release number.
     * @return Proteins within the given ID range
     */
    public Set<RawProtein<T>> getProteinsByIdRange (String bottomId, String topId, String signatureDatabaseRelease);
    
}
