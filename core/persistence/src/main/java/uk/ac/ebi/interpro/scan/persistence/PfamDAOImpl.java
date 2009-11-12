package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.raw.PfamRawMatch;


import javax.persistence.Query;


/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 06-Nov-2009
 * Time: 14:27:39
 * To change this template use File | Settings | File Templates.
 */
public class PfamDAOImpl extends GenericDAOImpl<PfamRawMatch, Long> implements PfamDAO {

     /**
     * Calls the GenericDAOImpl constructor passing in Protein.class as
     * argument, so that this DAO is set up to handle the correct class of model.
     */
    public PfamDAOImpl(){
        super(PfamRawMatch.class);
    }

    public PfamRawMatch getPfamMatchesById(Long id) {
        Query query = entityManager.createQuery("select p from PfamRawMatch p  where p.id = :id");
        query.setParameter("id", id);
        return (PfamRawMatch) query.getSingleResult();
    }

       public PfamRawMatch getPfamMatchesByModel(String methodAc) {
          // Pfam p = null;
            Query query = entityManager.createQuery("select p from PfamRawMatch p  where p.model = :methodAc");
           query.setParameter("methodAc", methodAc);
           return (PfamRawMatch) query.getSingleResult();
           //return p;
       }
}
