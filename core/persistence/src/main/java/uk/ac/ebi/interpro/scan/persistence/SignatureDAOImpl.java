package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.Signature;

import javax.persistence.Query;

/**
 * TODO: Add class description
 *
 * @author  Phil Jones, EMBL-EBI
 * @version $Id$
 */
public class SignatureDAOImpl extends GenericDAOImpl<Signature, Long> implements SignatureDAO {

    /**
     * Calls the GenericDAOImpl constructor passing in Signature.class as
     * argument, so that this DAO is set up to handle the correct class of model.
     */
    public SignatureDAOImpl(){
        super (Signature.class);
    }

    @Transactional(readOnly = true)
    public Signature getSignatureAndMethodsDeep(Long id) {
        // TODO - probably need to go deeper than this?
        Query query = entityManager.createQuery(
                "select s from Signature s " +
                "left outer join fetch s.models " +
                "left outer join fetch s.crossReferences " +
                "where s.id = :id"                
        );
        query.setParameter("id", id);
        return (Signature) query.getSingleResult();
    }
}
