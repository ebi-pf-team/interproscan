package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.BlastProDomMatch;
import uk.ac.ebi.interpro.scan.model.transactiontracking.TransactionSlice;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.List;

/**
 * TODO: Add class description
 *
 * @author  Manjula Thimma
 * @version $Id$
 */
public class BlastProDomLocationDAOImpl extends GenericDAOImpl<BlastProDomMatch.BlastProDomLocation, Long> implements BlastProDomLocationDAO{

    /**
     * Calls the GenericDAOImpl constructor passing in Protein.class as
     * argument, so that this DAO is set up to handle the correct class of model.
     */
    public BlastProDomLocationDAOImpl(){
        super(BlastProDomMatch.BlastProDomLocation.class);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<BlastProDomMatch.BlastProDomLocation> getBlastProDomHitLocationByScore(Double score) {
        Query query = entityManager.createQuery("select bpl from BlastProDomLocation bpl where bpl.score >= :score");
        query.setParameter("score", score);
        return query.getResultList();
    }
    
}
