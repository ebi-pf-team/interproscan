package uk.ac.ebi.interpro.scan.persistence.raw;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.raw.PrintsRawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: Jun 23, 2010
 */
public interface PrintsRawMatchDAO extends RawMatchDAO<PrintsRawMatch> {

    /**
     * Returns a Map of sequence identifiers to a List of
     * PrintsRawMatch objects for the protein IDs in the range
     * specified (Database default String ordering)
     * <p/>
     * Essential for PRINTS post processing.
     *
     * @param bottomId                 return protein IDs >= this String
     * @param topId                    the return protein IDs <= this String
     * @param signatureDatabaseRelease
     * @return a Map of sequence identifiers to a List of
     *         PrintsRawMatch objects for the protein IDs in the range
     *         specified (Database default String ordering)
     */
    @Transactional(readOnly = true)
    public Map<String, RawProtein<PrintsRawMatch>> getRawMatchesForProteinIdsInRange(long bottomId, long topId, String signatureDatabaseRelease);


}
