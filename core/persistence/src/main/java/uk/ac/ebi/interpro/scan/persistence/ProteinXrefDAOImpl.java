package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

import javax.persistence.Query;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of DAO Interface for data access to the Xref table
 * (which contains protein IDs).
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0
 */
public class ProteinXrefDAOImpl extends GenericDAOImpl<ProteinXref, Long> implements ProteinXrefDAO {

    private static final String UPI_ZERO = "UPI0000000000";

    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public ProteinXrefDAOImpl() {
        super(ProteinXref.class);
    }

    /**
     * Method to return the maximum UPI stored in the database.
     * Unlikely to be used outside the scope of the EBI.
     * <p/>
     * TODO - When database name is added to the Xref entity, use instead of the like clause.
     *
     * @return the maximum UPI in the Xref table.  Returns UPI0000000000 if no UPI xref is present.
     */
    @Transactional(readOnly = true)
    public String getMaxUniparcId() {
        Query query = entityManager.createQuery(
                "select max(x.identifier) from ProteinXref x where x.identifier like ('UPI__________') "
        );
        final String upi = (String) query.getSingleResult();
        if (upi == null || upi.length() == 0) {
            return UPI_ZERO;
        }
        return upi;
    }

    /**
     * Returns a List of Xrefs that are not unique.
     *
     * @return a List of Xrefs that are not unique.
     */
    @Transactional(readOnly = true)
    public List<String> getNonUniqueXrefs() {
        Query query = entityManager.createQuery(
                "select distinct a.identifier from ProteinXref a, ProteinXref b where a.id <> b.id and a.identifier = b.identifier"
        );
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public List<ProteinXref> getAllXrefs() {
        Query query = entityManager.createQuery(
                "select x from ProteinXref x"
        );
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public List<ProteinXref> getXrefAndProteinByProteinXrefIdentifier(String identifier) {
        Query query = entityManager.createQuery("select x from ProteinXref x left outer join fetch x.protein p where x.identifier = :identifier order by p.id asc");
        query.setParameter("identifier", identifier);
        return query.getResultList();
    }

    @Transactional
    public void updateAll(Collection<ProteinXref> proteinXrefs) {
        for (ProteinXref proteinXref : proteinXrefs) {
            entityManager.merge(proteinXref);
        }
    }
}
