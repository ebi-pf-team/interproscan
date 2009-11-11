package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.BlastProDomMatch;
import uk.ac.ebi.interpro.scan.model.transactiontracking.TransactionSlice;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 21-Jul-2009
 * Time: 14:25:10
 * To change this template use File | Settings | File Templates.
 */
public class BlastProDomLocationDAOImpl extends GenericDAOImpl<BlastProDomMatch.BlastProDomLocation, Long> implements BlastProDomLocationDAO{

    /**
     * Calls the GenericDAOImpl constructor passing in Protein.class as
     * argument, so that this DAO is set up to handle the correct class of model.
     */
    public BlastProDomLocationDAOImpl(){
        super(BlastProDomMatch.BlastProDomLocation.class);
    }

    public List<BlastProDomMatch.BlastProDomLocation> getBlastProDomHitLocationByScore(Double score) {
            Query query = entityManager.createQuery("select bpl from BlastProDomLocation bpl where bpl.score >= " + score.doubleValue());
        //query.setParameter("bottom", slice.getBottom());
        //query.setParameter("top", slice.getTop());
        return (List<BlastProDomMatch.BlastProDomLocation>) query.getResultList();
    }
    
}
