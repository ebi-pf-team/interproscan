package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

/**
 * DAO Interface for data access to the Xref table
 * (which contains protein IDs).
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0
 */
public interface ProteinXrefDAO extends GenericDAO<ProteinXref, Long> {

    /**
     * Method to return the maximum UPI stored in the database.
     * Unlikely to be used outside the scope of the EBI.
     *
     * @return the maximum UPI in the Xref table.  Returns null if no UPI xref is present.
     */
    @Transactional(readOnly = true)
    public String getMaxUniparcId();

}
