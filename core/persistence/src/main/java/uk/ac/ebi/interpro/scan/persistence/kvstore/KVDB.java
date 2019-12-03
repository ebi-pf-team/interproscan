package uk.ac.ebi.interpro.scan.persistence.kvstore;

import uk.ac.ebi.interpro.scan.model.Match;
import uk.ac.ebi.interpro.scan.model.NucleotideSequence;
import uk.ac.ebi.interpro.scan.model.Protein;

import org.iq80.leveldb.DB;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * interface to DB store
 */
public interface KVDB {

    void setKVDBStore(String dbStore, String dbName, String dbType);

    String getDbPath();

    String getKVDBStore();

    DB getLevelDBStore();

    String getDbName(); //dbname is equiv to dbstore??

    String getKVDBType();

    void put(String key, byte[] data);

    void put(byte[] key, byte[] data);

    byte[] get(String key);

    byte[] serialize(Serializable value);

    byte[] serialize(Protein protein);

//    byte[] serialize(Match match);

    byte[]  serialize(HashSet<Match> matches);

    String asString(byte[] byteKey);
//
//    static String asDeserializedString(byte[] byteKey);
//
//    static HashSet<Match> asDeserializedMatchSet(byte[] byteMatchSet);
//
//    static Match asDeserializedMatch(byte[] byteMatch);
//
    Protein asProtein(byte[] byteProtein);

    Match asMatch(byte[] byteMatch);

    NucleotideSequence asNucleotideSequence(byte[] byteNucleotideSequence);

    Set<Match> asMatchSet(byte[] byteMatchSet);

    byte[] serialize(String value);

    Map<byte[], byte[]> getAllElements() throws Exception;

    Protein getProtein(String key);

    NucleotideSequence getNucleotideSequence(String key);

//    static byte[] bytes(String value);

    void addSignatureLibraryName(String signatureLibraryName);

    Set<String> getSignatureLibraryNames();

    void close();


    
}

