package uk.ac.ebi.interpro.scan.persistence.raw;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

import javax.persistence.Query;


/**
 * TODO: Add class description
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
