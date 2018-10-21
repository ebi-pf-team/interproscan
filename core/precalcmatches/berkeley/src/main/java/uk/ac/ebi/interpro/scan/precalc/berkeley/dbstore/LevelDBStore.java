package uk.ac.ebi.interpro.scan.precalc.berkeley.dbstore;

import org.iq80.leveldb.DB;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import org.iq80.leveldb.Options;

import java.io.File;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * level DB store
 */
public class LevelDBStore {

    DB levelDBStore;

    public LevelDBStore() {

    }

    public DB getLevelDBStore(String dbStore) {
        //DB levelDBStore;
        Options options = new Options();
        try {
            levelDBStore = factory.open(new File(dbStore), options);
            return levelDBStore;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;

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
}

