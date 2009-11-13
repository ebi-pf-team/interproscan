package uk.ac.ebi.interpro.scan.persistence.raw;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


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
     * @return a Map of sequence identifiers to a List of
     * PfamHmmer3RawMatch objects for the protein IDs in the range
     * specified (Database default String ordering)
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<PfamHmmer3RawMatch>> getMatchesForProteinIdsInRange(String bottomId, String topId) {
        Map<String, List<PfamHmmer3RawMatch>> proteinIdToMatchMap = new HashMap<String, List<PfamHmmer3RawMatch>>();
        Query query = entityManager.createQuery("select p from PfamHmmer3RawMatch p  where p.sequenceIdentifier >= :bottom and p.sequenceIdentifier <= :top");
        query.setParameter("bottom", bottomId);
        query.setParameter("top", topId);
        List<PfamHmmer3RawMatch> resultList = query.getResultList();
        for (PfamHmmer3RawMatch match : resultList){
            List<PfamHmmer3RawMatch> matchList = proteinIdToMatchMap.get(match.getSequenceIdentifier());
            if (matchList == null){
                matchList = new ArrayList<PfamHmmer3RawMatch>();
                proteinIdToMatchMap.put(match.getSequenceIdentifier(), matchList);
            }
            matchList.add(match);
        }
        return proteinIdToMatchMap;
    }
}
