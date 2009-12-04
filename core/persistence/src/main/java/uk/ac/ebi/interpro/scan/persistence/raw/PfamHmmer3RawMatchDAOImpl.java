package uk.ac.ebi.interpro.scan.persistence.raw;

import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;
import uk.ac.ebi.interpro.scan.model.raw.RawProtein;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;

import javax.persistence.Query;
import java.util.*;



/**
 * DAO implementation for PfamHmmer3RawMatchDAO objects.
 *
 * @author  Manjula Thimma
 * @version $Id$
 */
public class PfamHmmer3RawMatchDAOImpl extends GenericDAOImpl<PfamHmmer3RawMatch, Long> implements PfamHmmer3RawMatchDAO {

    /**
     * Calls the GenericDAOImpl constructor passing in Protein.class as
     * argument, so that this DAO is set up to handle the correct class of model.
     */
    public PfamHmmer3RawMatchDAOImpl(){
        super(PfamHmmer3RawMatch.class);
    }

    public PfamHmmer3RawMatch getPfamMatchesByModel(String methodAc) {
        // Pfam p = null;
        Query query = entityManager.createQuery("select p from PfamHmmer3RawMatch p  where p.model = :methodAc");
        query.setParameter("methodAc", methodAc);
        return (PfamHmmer3RawMatch) query.getSingleResult();
        //return p;
    }

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
    @SuppressWarnings("unchecked")
    public Map<String, RawProtein> getRawMatchesForProteinIdsInRange(String bottomId, String topId, String signatureDatabaseRelease) {
        Map<String, RawProtein> proteinIdToMatchMap = new HashMap<String, RawProtein>();
        Query query = entityManager.createQuery("select p from PfamHmmer3RawMatch p  where p.sequenceIdentifier >= :bottom and p.sequenceIdentifier <= :top and p.signatureLibraryRelease = :sigLibRelease");
        query.setParameter("bottom", bottomId);
        query.setParameter("top", topId);
        query.setParameter("sigLibRelease", signatureDatabaseRelease);
        List<PfamHmmer3RawMatch> resultList = query.getResultList();
        for (PfamHmmer3RawMatch match : resultList){
            RawProtein rawProtein = proteinIdToMatchMap.get(match.getSequenceIdentifier());
            if (rawProtein == null){
                rawProtein = new RawProtein(match.getSequenceIdentifier());
                proteinIdToMatchMap.put(match.getSequenceIdentifier(), rawProtein);
            }
            rawProtein.addMatch(match);
        }
        return proteinIdToMatchMap;
    }
}
