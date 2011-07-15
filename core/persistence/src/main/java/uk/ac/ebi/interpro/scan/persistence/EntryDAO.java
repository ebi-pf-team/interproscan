package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Entry;
import uk.ac.ebi.interpro.scan.model.ProteinXref;

/**
 * DAO Interface for data access to the Xref table
 * (which contains protein IDs).
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0
 */
public interface EntryDAO extends GenericDAO<Entry, Long> {

}
