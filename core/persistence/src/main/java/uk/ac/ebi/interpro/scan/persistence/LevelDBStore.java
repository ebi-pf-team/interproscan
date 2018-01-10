package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.*;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;
import org.iq80.leveldb.Options;

import java.io.File;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * level DB store
 */
public class LevelDBStore {

    DB levelDBStore;
    String dbName ;

    Set<String> signatureLibraryNames;
   
    public LevelDBStore() {
        signatureLibraryNames = new HashSet<>();
    }

    public void setLevelDBStore(String dbStore) {
	//DB levelDBStore;
        Options options = new Options();
        try {
            levelDBStore = factory.open(new File(dbStore), options);
            dbName = dbStore;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public DB getLevelDBStore() {
        return levelDBStore;
    }

    public void put(String key, byte[] data) {
        byte[] keyInBytes = SerializationUtils.serialize(key);
        levelDBStore.put(keyInBytes, data);
    }

    public void put(byte[] key, byte[] data) {
        levelDBStore.put(key, data);
    }

    public  byte[] get(String key) {
	byte[] keyInBytes = SerializationUtils.serialize(key);
        return levelDBStore.get(keyInBytes);
    }

    public byte[] serialize(Protein protein) {
        byte[] data = SerializationUtils.serialize(protein);
	return data;
    }


    public byte[] serialize(Match match) {
        byte[] data = SerializationUtils.serialize(match);
        return data;
    }

    public static String asString(byte[] byteKey) {
        String key = (String) SerializationUtils.deserialize(byteKey );
        return key;
    }

   public static String asDeserializedString(byte[] byteKey) {
        String key = (String) SerializationUtils.deserialize(byteKey );
        return key;
    }


    public static HashSet<Match> asDeserializedMatchSet(byte[] byteMatchSet) {
        HashSet<Match> data = (HashSet<Match>) SerializationUtils.deserialize(byteMatchSet);
        return data;
    }

    public static Match asDeserializedMatch(byte[] byteMatch) {
        Match data = (Match) SerializationUtils.deserialize(byteMatch);
        return data;
    }

   public static Protein asProtein(byte[] byteProtein) {
        Protein protein = (Protein) SerializationUtils.deserialize(byteProtein);
        return protein;
    }

    public byte[] serialize(String value) {
        byte[] data = SerializationUtils.serialize(value);
        return data;
    }

    public Map<byte[], byte[]> getAllElements() {
        Map<byte[], byte[]> allElements = new HashMap<>();
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

    public Protein getProtein(String key){
	byte[] keyInBytes = SerializationUtils.serialize(key);
        byte[] outdata = levelDBStore.get(keyInBytes);
        Protein protein = (Protein) SerializationUtils.deserialize(outdata);
	return protein;
    }



    public void addValues(DB db) {

        System.out.println("Adding");
        for (int i = 0; i < 1000 * 1000; i++) {
            if (i % 100000 == 0) {
                System.out.println("  at: " + i);
            }
            db.put(bytes("key" + i), bytes("value" + i));
        }

    }

    public static byte[] bytes(String value) {
        if (value == null) {
            return null;
        }
        try {
            return value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    /*
    public static String asNewString(byte[] value) {
        if (value == null) {
            return null;
        }
        try {
            return new String(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    */

   public void addSignatureLibraryName(String signatureLibraryName){
        //System.out.println("addSignatureLibraryName: " + signatureLibraryName);
        signatureLibraryNames.add(signatureLibraryName);
        //System.out.println(signatureLibraryNames);
    }

   public Set<String> getSignatureLibraryNames(){
        return signatureLibraryNames;
   }

    public void close(){
	try {
            levelDBStore.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }	
    }

    public String getDbName() {
        return dbName;
    }

    
}

