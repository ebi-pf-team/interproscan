package uk.ac.ebi.interpro.scan.persistence.kvstore;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

/**
 * level DB store
 */
public class LevelDBStore extends KVDBImpl implements AutoCloseable {

    DB levelDBStore;

    public LevelDBStore() {

    }

    public LevelDBStore(String dbStore) {
        dbType = "leveldb";
        //setKVDBStore(dbStore, dbType);
        setLevelDBStore(dbStore);
    }

    public void setLevelDBStore(String levelDBStorePath) {
        //DB levelDBStore;
        dbType = "leveldb";
        Options options = new Options();
        try {
            setKVDBStore(levelDBStorePath, dbName, dbType);
            this.levelDBStore = factory.open(new File(levelDBStorePath), options);
            Utilities.verboseLog("Configured this LevelDb Store: " + levelDBStorePath);
            System.out.println("Configured this LevelDb Store: " + levelDBStorePath);
            System.out.println(toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public DB getLevelDBStore() {
        return levelDBStore;
    }

    /**
     * put a String key  and byte value pair in the DB
     *
     * @param key
     * @param data
     */
    public void put(String key, byte[] data) {
        //System.out.println(dbName  +": put [key byte ] key:" + key);
        byte[] byteKey = serialize(key);
        //levelDBStore.put(byteKey, data);
        put(byteKey, data);
    }

    /**
     * put a byte key  and byte value pair in the DB
     *
     * @param key
     * @param data
     */
    public void put(byte[] key, byte[] data) {
        //System.out.println(dbName  +":put [byte byte ] key :" + key);
        //levelDBStore.put(key, data);


        //due to compression etc, the insert might fail
        for (int retries = 0;; retries++) {
            try{
                levelDBStore.put(key, data);
                break; //otherwise its an infinite loop
            } catch (Exception exception) {
                if (exception instanceof FileNotFoundException ) {
                    if (retries > 3) {
                        exception.printStackTrace();  //TODO  for debug ??
                        throw new IllegalStateException("Problem inserting data into the DBStore: FileNotFoundException :- " + exception);
                    } else {
                        try {
                            Thread.sleep(2 * 1000); // cool off, then try again
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    throw new IllegalStateException("Problem inserting data into the DBStore " + exception);
                }
            }
        }

    }

    /**
     * get a byte value  given a String key
     *
     * @param key
     * @return
     */
    public byte[] get(String key) {
        byte[] byteKey = serialize(key);
        return levelDBStore.get(byteKey);
    }

    public Map<byte[], byte[]> getAllElements() throws Exception{
        Map<byte[], byte[]> allElements = new HashMap<>();
        if(levelDBStore == null){
            throw new Exception("this kv store is not properly configured");
        }
        DBIterator iterator = levelDBStore.iterator();
        while (iterator.hasNext()) {
            Map.Entry<byte[], byte[]> entry = iterator.next();
            allElements.put(entry.getKey(), entry.getValue());
        }
        //Utilities.verboseLog(" Number of elements " + count);
        try {
            iterator.close();
        } catch (Exception e) {
            new IOException(e).printStackTrace();

        }
        return allElements;
    }

    public void close() {
        try {
            if (levelDBStore != null) {
                levelDBStore.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "LevelDBStore{" +
                "levelDBStore is not NULL: " + (levelDBStore != null) +
                ", dbStore='" + getKVDBStore() + '\'' +
                ", dbPath='" + getDbPath() + '\'' +
                ", dbName='" + dbName + '\'' +
                ", dbType='" + dbType + '\'' +
                ", signatureLibraryNames=" + signatureLibraryNames +
                '}';
    }
}

