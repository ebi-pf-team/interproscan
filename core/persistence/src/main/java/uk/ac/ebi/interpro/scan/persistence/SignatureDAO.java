package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Signature;

/**
 * Created by IntelliJ IDEA.
 * User: mumdad
 * Date: 16-Jul-2009
 * Time: 20:30:00
 * To change this template use File | Settings | File Templates.
 */
public interface SignatureDAO extends GenericDAO<Signature, Long> {

    /**
     * Retrieves a signature and all of the Methods
     * associated with the signature.
     * @param primaryKey being the primary key of the required object.
     * @return a Signature object populated with Models.
     */
    public Signature getSignatureAndMethodsDeep (Long primaryKey);
}
