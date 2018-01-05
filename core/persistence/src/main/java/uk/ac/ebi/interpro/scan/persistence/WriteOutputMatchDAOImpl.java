package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;

import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.IOException;
import java.util.*;

import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.DBException;

import org.apache.commons.lang3.SerializationUtils;

import javax.swing.*;

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
    public List<HashSet<Match>> getMatchSets() {
        Map<String, HashSet<Match>> keyToMatchMap = new HashMap<>();
        List<HashSet<Match>> allMatches = new ArrayList<>();
        DBIterator iterator = levelDBStore.getLevelDBStore().iterator();
//        iterator.seek(first);
        int count = 0;
        while (iterator.hasNext()) {
            Map.Entry<byte[], byte[]> entry = iterator.next();
            byte[] byteKey = entry.getKey();
            byte[] byteData = entry.getValue();
            String key = LevelDBStore.asString(byteKey);
            HashSet<Match> matches = LevelDBStore.asDeserializedMatchSet(byteData);
            allMatches.add(matches);
//            byte[] match = serializer.parseMetricID(key);
            count++;

        }
        Utilities.verboseLog(" Number of matche sets " + count);//
//        iterator.close();
        try {
            iterator.close();
        } catch (Exception e) {
            new IOException(e).printStackTrace();
        }
        return allMatches;


    }

    @Transactional(readOnly = true)
    public List<Match> getMatches() {
        Map<String, HashSet<Match>> keyToMatchMap = new HashMap<>();
        List<Match> allMatches = new ArrayList<>();
        DBIterator iterator = levelDBStore.getLevelDBStore().iterator();
//        iterator.seek(first);
        int count = 0;
        while (iterator.hasNext()) {
            Map.Entry<byte[], byte[]> entry = iterator.next();
            byte[] byteKey = entry.getKey();
            byte[] byteData = entry.getValue();
            String key = LevelDBStore.asString(byteKey);
            Match match = LevelDBStore.asDeserializedMatch(byteData);

            allMatches.add(match);
//            byte[] match = serializer.parseMetricID(key);
            count++;

        }
        Utilities.verboseLog(" Number of matches " + count);//
//        iterator.close();
        try {
            iterator.close();
        } catch (Exception e) {
            new IOException(e).printStackTrace();
        }
        return allMatches;


    }

    @Transactional
    public List<Protein>  getCompleteProteins(List<Protein> proteins){
        List<Match> allMatches = getMatches();

        return proteins;

    }

    @Transactional(readOnly = true)
    public List<Protein> getProteins() {
        List<Protein> proteins = new ArrayList<>();
        return proteins;
    }


    @Override
    public void closeDB() {
        levelDBStore.close();
        proteinKVStore.close();
    }
}
