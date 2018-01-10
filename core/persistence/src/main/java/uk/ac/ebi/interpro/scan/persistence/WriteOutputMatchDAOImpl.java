package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.model.*;
import uk.ac.ebi.interpro.scan.model.raw.RawMatch;

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
public class WriteOutputMatchDAOImpl extends FilteredMatchKVDAOImpl<Match, RawMatch> implements WriteOutputMatchDAO {

    //Other DAOs
    //private ProteinKVDAO proteinKVDAO;
    private ProteinKVDAO proteinKVDAO;

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

    @Required
    public void setProteinKVDAO(ProteinKVDAO proteinKVDAO) {
        this.proteinKVDAO = proteinKVDAO;
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
    public Map<String, HashSet<Match>> getKeyToMatchMap() {
        Map<String, HashSet<Match>> keyToMatchMap = new HashMap<>();
        DBIterator iterator = levelDBStore.getLevelDBStore().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Map.Entry<byte[], byte[]> entry = iterator.next();
            byte[] byteKey = (byte[]) entry.getKey();
            byte[] byteData = (byte[]) entry.getValue();
            String key = LevelDBStore.asDeserializedString(byteKey);
            HashSet<Match> matches = LevelDBStore.asDeserializedMatchSet(byteData);
            keyToMatchMap.put(key, matches);
            count++;
        }
	Utilities.verboseLog(" Number of match sets " + count);//
        try {
            iterator.close();
        } catch (Exception e) {
            new IOException(e).printStackTrace();
        }
        return keyToMatchMap;
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

    @Transactional(readOnly = true)
    public HashSet<Match> getMatchSet(String key) {
        byte[] byteMatchSet = levelDBStore.get(key);
        if (byteMatchSet != null){
            return LevelDBStore.asDeserializedMatchSet(byteMatchSet);
        }
	return null;
    }


    @Transactional
    public List<Protein>  getCompleteProteins(){
        //Map<String, HashSet<Match>> keyToMatchMap = getKeyToMatchMap();

        Set<String> signatureLibraryNames = getSignatureLibraryNames();
        Utilities.verboseLog("SignatureLibrary names:" + signatureLibraryNames.toString());        
	Map<String, Protein>  completeProteins = new HashMap<>();
        Map<String, Protein> keyToProteinMap = proteinKVDAO.getKeyToProteinMap();
        Iterator it = keyToProteinMap.keySet().iterator();
        int count = 0;
        while (it.hasNext()) {
           String key = (String) it.next();
           Protein protein = keyToProteinMap.get(key);
           for(String signatureLibraryName: signatureLibraryNames){
             String matchKey = key + signatureLibraryName;
             //Utilities.verboseLog("Get matches for key: " + key + " matchKey: " + matchKey);
             HashSet<Match> matches = getMatchSet(matchKey);
             if (matches != null){
                 for(Match match: matches){
               	    protein.addMatch(match);
                    count ++;
                 }
	     }
           }
           keyToProteinMap.put(key, protein);
        }
        Utilities.verboseLog("Total number of matches: " + count  );      	
        return new ArrayList(keyToProteinMap.values());

        /*
	Iterator it = keyToMatchMap.entrySet().iterator();
        Protein rep = null;
        int count = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String key = (String) pair.getKey();
            HashSet<Match> matches = (HashSet<Match>) pair.getValue();
            Protein protein = completeProteins.get(key);
            if (protein == null){
		//Utilities.verboseLog("get Protein for key: " + key);                
		protein = proteinKVDAO.getProtein(key);                                
	    }
            for(Match match: matches){
        	protein.addMatch(match);
                count ++;
	    }
            if (rep == null){
                rep = protein;
            }
            completeProteins.put(key, protein);  
	}
        Utilities.verboseLog("rep protein: " + rep.toString());
       
        Utilities.verboseLog("Total number of matches: " + count  );
        
        return new ArrayList(completeProteins.values());
        */
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
