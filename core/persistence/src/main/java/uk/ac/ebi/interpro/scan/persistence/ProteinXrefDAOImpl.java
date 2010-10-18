package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

import javax.persistence.Query;

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
     *
     */
    public ProteinXrefDAOImpl() {
        super(ProteinXref.class);
    }

    /**
     * Method to return the maximum UPI stored in the database.
     * Unlikely to be used outside the scope of the EBI.
     *
     * TODO - When database name is added to the Xref entity, use instead of the like clause.
     *
     * @return the maximum UPI in the Xref table.  Returns UPI0000000000 if no UPI xref is present.
     */
    @Transactional (readOnly = true)
    public String getMaxUniparcId() {
        Query query = entityManager.createQuery(
                "select max(x.identifier) from ProteinXref x where x.identifier like ('UPI__________') "
        );
        final String upi = (String) query.getSingleResult();
        if (upi == null || upi.length() == 0){
            return UPI_ZERO;
        }
        return upi;
    }
}
