package uk.ac.ebi.interpro.scan.persistence.kvstore;

import uk.ac.ebi.interpro.scan.model.*;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import org.iq80.leveldb.Options;

import java.io.File;

import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * level DB store
 */
abstract class KVDBImpl implements KVDB, AutoCloseable {

    String dbPath;
    String dbName;
    String dbType;

    Set<String> signatureLibraryNames = new HashSet<>();

    public void setKVDBStore(String dbStore, String dbName, String dbType) {
        //dbStore == dbPath
        this.dbPath = dbStore;
        this.dbName = dbName;
        this.dbType = dbType;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public String getKVDBStore() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbName() { //dbname is equiv to dbstore??
        return dbName;
    }

    public String getKVDBType() {
        return dbType;
    }

    public byte[] serialize(Serializable value) {
        byte[] data = SerializationUtils.serialize(value);
        return data;
    }

    public byte[] serialize(Entry entry) {
        byte[] data = SerializationUtils.serialize(entry);
        return data;
    }

    public byte[] serialize(Protein protein) {
        byte[] data = SerializationUtils.serialize(protein);
        return data;
    }

/*
    public byte[] serialize(Match match) {
        byte[] data = SerializationUtils.serialize(match);
        return data;
    }

*/
    public byte[]  serialize(HashSet<Match> matches) {
        byte[] data = SerializationUtils.serialize(matches);
        return data;
    }

    public String asString(byte[] byteKey) {
        String key = (String) SerializationUtils.deserialize(byteKey);
        return key;
    }

    public String asDeserializedString(byte[] byteKey) {
        String key = (String) SerializationUtils.deserialize(byteKey);
        return key;
    }

    public Entry asEntry(byte[] byteEntry) {
        Entry data = (Entry) SerializationUtils.deserialize(byteEntry);
        return data;
    }

    public Set<Match> asMatchSet(byte[] byteMatchSet) {
        Set<Match> data = (Set<Match>) SerializationUtils.deserialize(byteMatchSet);
        return data;
    }

    public Match asMatch(byte[] byteMatch) {
        Match data = (Match) SerializationUtils.deserialize(byteMatch);
        return data;
    }

    public Protein asProtein(byte[] byteProtein) {
        if(byteProtein != null) {
            Protein protein = (Protein) SerializationUtils.deserialize(byteProtein);
            return protein;
        }
        return null;
    }

    public NucleotideSequence asNucleotideSequence(byte[] byteNucleotideSequence) {
        if(byteNucleotideSequence != null) {
            NucleotideSequence nucleotideSequence = (NucleotideSequence) SerializationUtils.deserialize(byteNucleotideSequence);
            return nucleotideSequence;
        }
        return null;
    }

    public byte[] serialize(String value) {
        byte[] data = SerializationUtils.serialize(value);
        return data;
    }


    public Protein getProtein(String key) {
        byte[] outdata = get(key);
        Protein protein = (Protein) SerializationUtils.deserialize(outdata);
        return protein;
    }

    public NucleotideSequence getNucleotideSequence(String key) {
        byte[] outdata = get(key);
        NucleotideSequence nucleotideSequence = (NucleotideSequence) SerializationUtils.deserialize(outdata);
        return nucleotideSequence;
    }

    public byte[] bytes(String value) {
        if (value == null) {
            return null;
        }
        try {
            return value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    public void addSignatureLibraryName(String signatureLibraryName) {
        //System.out.println("addSignatureLibraryName: " + signatureLibraryName);
        signatureLibraryNames.add(signatureLibraryName);
        //System.out.println(signatureLibraryNames);
    }

    public Set<String> getSignatureLibraryNames() {
        return signatureLibraryNames;
    }

    @Override
    public String toString() {
        return "KVDBImpl{" +
                "dbName='" + dbName + '\'' +
                ", dbType='" + dbType + '\'' +
                ", signatureLibraryNames=" + signatureLibraryNames +
                '}';
    }
}

