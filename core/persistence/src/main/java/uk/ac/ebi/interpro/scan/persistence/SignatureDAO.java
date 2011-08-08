package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Signature;

import java.util.Collection;
import java.util.Set;

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

    /**
     * Retrieves all signatures from the input and all of the Methods
     * associated with those signatures.
     *
     * @param accessions Set of signature accessions to lookup.
     * @return a Signature object populated with Models.
     */
    @Transactional(readOnly = true)
    public Collection<Signature> getSignaturesAndMethodsDeep(Set<String> accessions);

    @Transactional(readOnly = true)
    public Set<Signature> getSignatures(Collection<String> accessions);


    @Transactional(readOnly = true)
    public Signature getSignatureByAccession(String accession);

    /**
     * Updates the modified instances.
     *
     * @param modifiedInstances being an attached or unattached, persisted object that has been modified.
     */
    Collection<Signature> update(Collection<Signature> modifiedInstances);
}