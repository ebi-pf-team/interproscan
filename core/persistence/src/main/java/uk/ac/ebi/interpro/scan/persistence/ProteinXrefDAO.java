package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

import java.util.Collection;
import java.util.List;

/**
 * DAO Interface for data access to the Xref table
 * (which contains protein IDs).
 *
 * @author Phil Jones
 * @author David Binns
 * @author Maxim Scheremetjew
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
    public String getMaxUniparcId();

    /**
     * Returns a List of Xrefs that are not unique.
     *
     * @return a List of Xrefs that are not unique.
     */
    public List<String> getNonUniqueXrefs();

    public List<ProteinXref> getAllXrefs();

    /**
     * Returns a List of protein xrefs and their attached protein ordered by protein Id.
     *
     * @param identifier
     * @return
     */
    public List<ProteinXref> getXrefAndProteinByProteinXrefIdentifier(String identifier);

    public void updateAll(Collection<ProteinXref> proteinXrefs);
}
