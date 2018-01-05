package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;

import java.io.IOException;
import java.util.*;

import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.DBException;

import org.apache.commons.lang3.SerializationUtils;

/**
 * Implements the persistence method for Coils matches (as filtered matches).
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */
public class WriteOutputMatchDAOImpl extends FilteredMatchKVDAOImpl<Match> implements WriteOutputMatchDAO {

    //Other DAOs
    private ProteinDAO proteinDAO;

    private ProteinXrefDAO proteinXrefDAO;

    //KV stores
    private LevelDBStore proteinKVStore;
    //private LevelDBStore matchKVStore;

    /**
     * Sets the class of the model that the DAO instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public WriteOutputMatchDAOImpl() {
        super(Match.class);
    }

    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    public void setProteinXrefDAO(ProteinXrefDAO proteinXrefDAO) {
        this.proteinXrefDAO = proteinXrefDAO;
    }

    public void setProteinKVStore(LevelDBStore proteinKVStore) {
        this.proteinKVStore = proteinKVStore;
    }

    public void setMatchKVStore(LevelDBStore matchKVStore) {
        //this.matchKVStore = matchKVStore;
        setLevelDBStore(matchKVStore);
    }

    @Transactional(readOnly = true)
    public List<Match> getMatches(){
        DBIterator iterator = levelDBStore.getLevelDBStore().iterator();
//        iterator.seek(first);
        while (iterator.hasNext()) {
            Map.Entry<byte[], byte[]> entry = iterator.next();
            byte[] key = entry.getKey();
            byte[] data = entry.getValue();
//            byte[] match = serializer.parseMetricID(key);

        }
//
//        iterator.close();
        try {
            iterator.close();
        } catch (Exception e) {
            new IOException(e).printStackTrace();
        }
        return null;


    }

    @Override
    public void closeDB(){
        levelDBStore.close();
        proteinKVStore.close();
    }
}
