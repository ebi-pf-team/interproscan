package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.Entry;
import uk.ac.ebi.interpro.scan.persistence.kvstore.KVDB;
import uk.ac.ebi.interpro.scan.persistence.kvstore.LevelDBStore;

import java.util.Map;
import java.util.Set;

public interface EntryKVDAO<T extends Entry>  extends GenericKVDAO<T>  {

    void persist(String key, Entry entry);

    Entry getEntry(String key);

    Set<Entry> getEntries() throws Exception;

    void checkKVDBStores();

    KVDB getKvStoreEntry();
}
