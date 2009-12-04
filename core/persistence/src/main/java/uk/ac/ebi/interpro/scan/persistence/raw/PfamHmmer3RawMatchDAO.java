package uk.ac.ebi.interpro.scan.persistence.raw;

import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

/**
 * DAO methods for PfamHmmer3RawMatchDAO objects.
 *
 * @author  Manjula Thimma
 * @author Phil Jones
 * @version $Id$
 */
public interface PfamHmmer3RawMatchDAO extends GenericDAO<PfamHmmer3RawMatch, Long> {

    public PfamHmmer3RawMatch getPfamMatchesByModel (String methodAc);

    /**
     * Returns a Map of sequence identifiers to a List of
     * PfamHmmer3RawMatch objects for the protein IDs in the range
     * specified (Database default String ordering)
     *
     * Essential for Pfam-A HMMER3 post processing.
     * @param bottomId return protein IDs >= this String
     * @param topId the return protein IDs <= this String
     * @param signatureDatabaseRelease
     * @return a Map of sequence identifiers to a List of
     * PfamHmmer3RawMatch objects for the protein IDs in the range
     * specified (Database default String ordering)
     */
    public <T extends RawMatch> Map<String, RawProtein<T>> getRawMatchesForProteinIdsInRange  (String bottomId, String topId, String signatureDatabaseRelease);

}
