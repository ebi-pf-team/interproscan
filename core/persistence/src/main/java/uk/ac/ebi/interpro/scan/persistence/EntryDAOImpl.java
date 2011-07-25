package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.Entry;
import uk.ac.ebi.interpro.scan.model.Release;
import uk.ac.ebi.interpro.scan.model.Signature;

import javax.persistence.Query;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public Set<Entry> mergeEntries(Set<Entry> entries) {
        Set<Entry> result = new HashSet<Entry>();
        for (Entry entry : entries) {
            result.add(entityManager.merge(entry));
        }
        return result;
    }

    public Entry mergeEntry(Entry entry) {
        return entityManager.merge(entry);
    }
}