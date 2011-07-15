package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.Entry;
import uk.ac.ebi.interpro.scan.model.Signature;

import javax.persistence.Query;

/**
 * Represents an implementation of EntryDAO interface.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 */
public class EntryDAOImpl extends GenericDAOImpl<Entry, Long> implements EntryDAO {

    /**
     * Calls the GenericDAOImpl constructor passing in Entry.class as
     * argument, so that this DAO is set up to handle the correct class of model.
     */
    public EntryDAOImpl() {
        super(Entry.class);
    }
}