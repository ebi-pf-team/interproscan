package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Signature;

/**
 * TODO: Add class description
 *
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 */
public interface SignatureDAO extends GenericDAO<Signature, Long> {

    /**
     * Retrieves a signature and all of the Methods
     * associated with the signature.
     *
     * @param primaryKey being the primary key of the required object.
     * @return a Signature object populated with Models.
     */
    @Transactional(readOnly = true)
    public Signature getSignatureAndMethodsDeep(Long primaryKey);
}
