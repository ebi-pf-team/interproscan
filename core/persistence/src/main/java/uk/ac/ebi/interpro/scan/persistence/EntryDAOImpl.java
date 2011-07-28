package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.Entry;

import javax.persistence.Query;
import java.util.Collection;
import java.util.Collections;
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

    @Transactional
    public Set<Entry> mergeEntries(Collection<Entry> entries) {
        Set<Entry> result = Collections.emptySet();
        for (Entry entry : entries) {
            result.add(entityManager.merge(entry));
        }
        return result;
    }

    @Transactional
    public Entry mergeEntry(Entry entry) {
        return entityManager.merge(entry);
    }

    @Transactional(readOnly = true)
    public Entry readEntryByAccession(String accession) {
        final Query query = entityManager.createQuery("select e from Entry e where e.accession = :accession");
        query.setParameter("accession", accession);
        @SuppressWarnings("unchecked") List<Entry> entries = query.getResultList();
        if (entries != null && entries.size() > 0)
            return entries.get(0);
        return null;
    }
}