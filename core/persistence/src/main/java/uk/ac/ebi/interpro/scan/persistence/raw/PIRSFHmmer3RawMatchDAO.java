package uk.ac.ebi.interpro.scan.persistence.raw;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.raw.PIRSFHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Map;

/**
 * DAO methods for PirsfHmmer3RawMatchDAO objects.
 *
 * @author Matthew Fraser
 * @version $Id$
 */
public interface PIRSFHmmer3RawMatchDAO
        extends RawMatchDAO<PIRSFHmmer3RawMatch> {

    /**
     * Returns a Map of sequence identifiers to a List of
     * PirsfHmmer3RawMatch objects for the protein IDs in the range
     * specified (Database default String ordering)
     * <p/>
     * Essential for PIRSF HMMER3 post processing.
     *
     * @param bottomId                 return protein IDs >= this String
     * @param topId                    the return protein IDs <= this String
     * @param signatureDatabaseRelease
     * @return a Map of sequence identifiers to a List of
     *         PirsfHmmer3RawMatch objects for the protein IDs in the range
     *         specified (Database default String ordering)
     */
    @Transactional(readOnly = true)
    public Map<String, RawProtein<PIRSFHmmer3RawMatch>> getRawMatchesForProteinIdsInRange(long bottomId, long topId, String signatureDatabaseRelease);

}
