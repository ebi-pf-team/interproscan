package uk.ac.ebi.interpro.scan.persistence;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;

import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.DBException;

import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;

import javax.persistence.Query;
import java.util.*;

/**
 * Class factoring out most of the commmon code required to persist a Collection of RawProtein objects that have
 * been filtered, ready to be persisted as "proper" matches.
 * <p/>
 * Implementations just have to implement a method where the Protein objects and Signature objects
 * for these raw matches have already been achieved - implementations just need to link them together properly!?
 *
 * @author Gift Nuka, EMBL-EBI
 * @version $Id$
 * @since 1.0
 */

public class FilteredMatchKVDAOImpl <T extends Match>  extends GenericDAOImpl<T, Long> implements FilteredMatchKVDAO<T> {

    private static final Logger LOGGER = Logger.getLogger(FilteredMatchKVDAOImpl.class.getName());

    protected LevelDBStore levelDBStore;

    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     *
     * ??
     */
//    public FilteredMatchKVDAOImpl() {
//        //this.levelDBStore = levelDBStore;
//        //super();
//    }

    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     *
     * @param modelClass the model that the DOA instance handles.
     */
    public FilteredMatchKVDAOImpl(Class<T> modelClass) {
        super(modelClass);
    }

    /**
     * Persists filtered protein matches.
     *
     * @param keyToMatchMap Filtered protein matches.
     */
    @Transactional
    public void persist(final Map<String, T> keyToMatchMap) {
        try {
            WriteBatch batch = levelDBStore.getLevelDBStore().createWriteBatch();
            try {
                for (Map.Entry<String, T> entry : keyToMatchMap.entrySet()) {
                    String key = entry.getKey();
                    T value = entry.getValue();
                    byte[] byteKey = levelDBStore.serialize(key);
                    byte[] data = serialize(value);
                    batch.put(byteKey, data);
                }
                /**
                 * do batch operation with sync option.
                 */
                levelDBStore.getLevelDBStore().write(batch);
                //levelDBStore.getLevelDBStore().write(batch, SYNC_WRITE_OPTION);

                LOGGER.info("data flushed to kv db .... " + levelDBStore.getDbName());

            } finally {
                // close the batch
                try {
                    batch.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (DBException e) {
            new IOException(e).printStackTrace();
            //throw new IOException(e);
        }
    }


    @Transactional
    public void persist(String key,  T match) {
        ///byte[] keyInBytes = levelDBStore.serialize(key);
        LOGGER.warn("storing data for seq: " + key + "match: " + match.getId()
                + "  to kv db ...." + levelDBStore.getDbName());
        byte[]  data = serialize(match);
        if (data == null){
            LOGGER.error("match has problems: key " + key + " match : " + match.toString());
        }
        levelDBStore.put(key, data);
        LOGGER.warn("data for seq: " + key + " stored to kv db ...." + levelDBStore.getDbName());
    }

    @Transactional
    public void persist(byte[] key,  byte[] match) {
        if (key == null || match == null){
            LOGGER.error("match or key has problems:  " );
        }
        levelDBStore.put(key, match);
    }

    public byte[] serialize(T match) {
        byte[] data = null;
        data = SerializationUtils.serialize((Hmmer3Match) match);
//        if (match instanceof  Hmmer3Match) {
//            data = SerializationUtils.serialize((Hmmer3Match) match);
//        }
        return data;
    }


    public void setLevelDBStore(LevelDBStore levelDBStore) {
        this.levelDBStore = levelDBStore;
    }


    public void closeDB(){
        levelDBStore.close();
    }
}
