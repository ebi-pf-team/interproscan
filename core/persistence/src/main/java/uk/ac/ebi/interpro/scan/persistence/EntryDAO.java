package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.model.Entry;

import java.util.Collection;
import java.util.Set;

/**
 * DAO Interface for data access to the Xref table
 * (which contains protein IDs).
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0
 */
public interface EntryDAO extends GenericDAO<Entry, Long> {
    /**
     * Merges a collection of entries and returns a set of merge entries.
     */
    Set<Entry> mergeEntries(Collection<Entry> entries);

    Entry mergeEntry(Entry entry);

    Entry readEntryByAccession(String entryAc);
}
