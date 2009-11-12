package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;


import javax.persistence.Query;


/**
 * Created by IntelliJ IDEA.
 * User: thimma
 * Date: 06-Nov-2009
 * Time: 14:27:39
 * To change this template use File | Settings | File Templates.
 */
public class PfamHmmer3RawDAOImpl extends GenericDAOImpl<PfamHmmer3RawMatch, Long> implements PfamHmmer3RawDAO {

     /**
     * Calls the GenericDAOImpl constructor passing in Protein.class as
     * argument, so that this DAO is set up to handle the correct class of model.
     */
    public PfamHmmer3RawDAOImpl(){
        super(PfamHmmer3RawMatch.class);
    }

    public PfamHmmer3RawMatch getPfamMatchesById(Long id) {
        Query query = entityManager.createQuery("select p from PfamHmmer3RawMatch p  where p.id = :id");
        query.setParameter("id", id);
        return (PfamHmmer3RawMatch) query.getSingleResult();
    }

       public PfamHmmer3RawMatch getPfamMatchesByModel(String methodAc) {
          // Pfam p = null;
            Query query = entityManager.createQuery("select p from PfamHmmer3RawMatch p  where p.model = :methodAc");
           query.setParameter("methodAc", methodAc);
           return (PfamHmmer3RawMatch) query.getSingleResult();
           //return p;
       }
}
