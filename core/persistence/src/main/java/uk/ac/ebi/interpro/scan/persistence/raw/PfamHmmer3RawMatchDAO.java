package uk.ac.ebi.interpro.scan.persistence.raw;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

import java.util.List;
import java.util.Map;

/**
 * DAO methods for PfamHmmer3RawMatchDAO objects.
 *
 * @author  Manjula Thimma
 * @author Phil Jones
 * @version $Id$
 */
public interface
        
        PfamHmmer3RawMatchDAO extends GenericDAO<PfamHmmer3RawMatch, Long> {

    public PfamHmmer3RawMatch getPfamMatchesByModel (String methodAc);

    /**
     * Returns a Map of sequence identifiers to a List of
     * PfamHmmer3RawMatch objects for the protein IDs in the range
     * specified (Database default String ordering)
     *
     * Essential for Pfam-A HMMER3 post processing.
     * @param bottomId return protein IDs >= this String
     * @param topId the return protein IDs <= this String
     * @return a Map of sequence identifiers to a List of
     * PfamHmmer3RawMatch objects for the protein IDs in the range
     * specified (Database default String ordering)
     */
    public Map<String, List<PfamHmmer3RawMatch>> getMatchesForProteinIdsInRange (String bottomId, String topId);

}
