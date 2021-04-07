package uk.ac.ebi.interpro.scan.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.Entry;
import uk.ac.ebi.interpro.scan.persistence.kvstore.KVDB;
import uk.ac.ebi.interpro.scan.persistence.kvstore.LevelDBStore;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.*;

public class EntryKVDAOImpl extends GenericKVDAOImpl<Entry> implements EntryKVDAO<Entry> {

    private static final Logger LOGGER = LogManager.getLogger(EntryKVDAOImpl.class.getName());
//    LevelDBStore kvStoreEntry;
//    String kvStoreEntryDBPath;

    public EntryKVDAOImpl() {
        super(Entry.class);
//        if (kvStoreEntryDBPath != null) {
//            kvStoreEntry.setLevelDBStore(kvStoreEntryDBPath);
//        }
    }

    @Override
    @Transactional
    public void persist(String key,  Entry entry) {
        byte[]  data = serialize(entry);
        dbStore.put(key, data);
    }

    @Override
    public Entry getEntry(String key){
        byte [] byteEntry = dbStore.get(key);
        if(byteEntry != null) {
            return dbStore.asEntry(byteEntry);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public  Set<Entry> getEntries() throws Exception{
        Set<Entry> allEntries = new HashSet<>();
        Map<byte[], byte[]> allElements = dbStore.getAllElements();
        for (byte[] byteKey : allElements.keySet()){
            byte[] byteData = allElements.get(byteKey);
            String key = dbStore.asString(byteKey);
            Entry entry = dbStore.asEntry(byteData);
            allEntries.add(entry);
        }
        return allEntries;
    }


    public void checkKVDBStores(){
        Utilities.verboseLog(110, "Main Store: " + dbStore.getKVDBStore());

//        if(this.kvStoreEntry == null){
//            LOGGER.warn("kvStoreEntry == null");
//        }else{
//            if (! (this.kvStoreEntry.getLevelDBStore() == null)){
//                Utilities.verboseLog(1100, "kvStoreEntryDB LevelDBStore is NOT NULL");
//            }else{
//                LOGGER.warn("kvStoreEntryDB is NULL - storename: " +  kvStoreEntry.getKVDBStore() +
//                        " dbmane: " + kvStoreEntry.getDbName());
//            }
//        }
        Utilities.verboseLog(110, dbStore.toString());
    }

    public KVDB getKvStoreEntry(){
        return dbStore;
    }

    @Override
    public String toString() {
        return "EntryKVDAOImpl{" +
//                "kvStoreEntry=" + kvStoreEntry.toString() +
//                ", kvStoreEntryDBPath='" + kvStoreEntryDBPath + '\'' +
                ", dbStore=" + dbStore +
                '}';
    }
}
