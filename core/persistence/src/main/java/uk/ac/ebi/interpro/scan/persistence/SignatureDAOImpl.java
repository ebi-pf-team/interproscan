package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.Signature;

import javax.persistence.Query;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DAO For the Signature model class.
 *
 * @author Phil Jones, EMBL-EBI
 * @version $Id$
 */
public class SignatureDAOImpl extends GenericDAOImpl<Signature, Long> implements SignatureDAO {

    /**
     * Calls the GenericDAOImpl constructor passing in Signature.class as
     * argument, so that this DAO is set up to handle the correct class of model.
     */
    public SignatureDAOImpl() {
        super(Signature.class);
    }

    @Transactional(readOnly = true)
    public Signature getSignatureAndMethodsDeep(Long id) {
        // TODO - probably need to go deeper than this?
        Query query = entityManager.createQuery(
                "select s from Signature s " +
                        "left outer join fetch s.crossReferences " +
                        "left outer join fetch s.deprecatedAccessions " +
                        "left outer join fetch s.descriptionChunks " +
                        "left outer join fetch s.models model " +
                        "left outer join fetch model.descriptionChunks " +
                        "left outer join fetch model.definitionChunks " +
                        "where s.id = :id"
        );
        query.setParameter("id", id);
        return (Signature) query.getSingleResult();
    }

    @Transactional(readOnly = true)
    public Collection<Signature> getSignaturesAndMethodsDeep(Set<String> accessions) {

        if (accessions == null || accessions.size() < 1) {
            return null;
        }

        // TODO - probably need to go deeper than this?
        Query query = entityManager.createQuery("select s from Signature s " +
                "left outer join fetch s.models " +
                "left outer join fetch s.crossReferences " +
                "where s.accession in (:accessions)");
        query.setParameter("accessions", accessions);

        List<Signature> results = query.getResultList();
        return new HashSet<Signature>(results);
    }

    @Transactional(readOnly = true)
    public Set<Signature> getSignatures(Collection<String> accessions) {
        if (accessions == null || accessions.size() < 1) {
            return null;
        }
        Query query = entityManager.createQuery("select s from Signature s " +
                "where s.accession in (:accessions)");
        query.setParameter("accessions", accessions);

        List<Signature> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return new HashSet<Signature>(results);
    }

    @Transactional(readOnly = true)
    public Signature getSignatureByAccession(String accession) {
        if (accession == null) {
            return null;
        }
        Query query = entityManager.createQuery("select s from Signature s " +
                "where s.accession = :accession");
        query.setParameter("accession", accession);

        List<Signature> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0);


    }

    @Transactional
    public Collection<Signature> update(Collection<Signature> modifiedInstances) {
        for (Signature modifiedInstance : modifiedInstances) {
            entityManager.merge(modifiedInstance);
        }
        return modifiedInstances;
    }
}
