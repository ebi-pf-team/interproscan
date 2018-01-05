package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.*;

import org.iq80.leveldb.DB;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;
import org.iq80.leveldb.Options;

import java.io.File;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * level DB store
 */
public class LevelDBStore {

    DB levelDBStore;
    String dbName ;

    public LevelDBStore() {

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

    public byte[] serialize(String value) {
        byte[] data = SerializationUtils.serialize(value);
        return data;
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

    public static String asString(byte[] value) {
        if (value == null) {
            return null;
        }
        try {
            return new String(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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

