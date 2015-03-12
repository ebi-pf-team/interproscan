package uk.ac.ebi.interpro.scan.persistence.raw;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;

import java.util.Map;

/**
 * DAO methods for PfamHmmer3RawMatchDAO objects.
 *
 * @author Manjula Thimma
 * @author Phil Jones
 * @author Antony Quinn
 * @version $Id$
 */
public interface PfamHmmer3RawMatchDAO
        extends RawMatchDAO<PfamHmmer3RawMatch> {

    /**
     * Returns a Map of sequence identifiers to a List of
     * PfamHmmer3RawMatch objects for the protein IDs in the range
     * specified (Database default String ordering)
     * <p/>
     * Essential for Pfam-A HMMER3 post processing.
     *
     * @param bottomId                 return protein IDs >= this String
     * @param topId                    the return protein IDs <= this String
     * @param signatureDatabaseRelease
     * @return a Map of sequence identifiers to a List of
     *         PfamHmmer3RawMatch objects for the protein IDs in the range
     *         specified (Database default String ordering)
     */
    @Transactional(readOnly = true)
    public Map<String, RawProtein<PfamHmmer3RawMatch>> getRawMatchesForProteinIdsInRange(long bottomId, long topId, String signatureDatabaseRelease);

}
