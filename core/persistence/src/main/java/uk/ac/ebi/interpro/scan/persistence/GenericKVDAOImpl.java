package uk.ac.ebi.interpro.scan.persistence;


import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.persistence.kvstore.KVDB;
import uk.ac.ebi.interpro.scan.persistence.kvstore.LevelDBStore;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

public class GenericKVDAOImpl<T extends Serializable> extends GenericDAOImpl<T, Long> implements GenericKVDAO<T>{

    protected KVDB dbStore;

    private String dbStoreType = "leveldb";

    public GenericKVDAOImpl(Class<T> modelClass) {
        super(modelClass);
    }

    @Required
    public void setDbStore(KVDB dbStore) {
        this.dbStore = dbStore;
        //default store is leveldb

    }

    public KVDB getDbStore() {
        return dbStore;
    }

    public void setDbStoreType(String dbStoreType) {
        this.dbStoreType = dbStoreType;
    }

    public byte[] serialize(T type) {
        byte[] data = SerializationUtils.serialize(type);
        return data;
    }

    @Transactional
    public void persist(String key, T value){
        byte[] byteKey = dbStore.serialize(key);
        byte[] byteValue = serialize(value);
        persist(byteKey, byteValue);
    }

    @Transactional
    public void persist(byte[] byteKey, byte[] byteValue){
        dbStore.put(byteKey, byteValue);
    }


    public T get(String key){
        T value = null;
        byte[] data = dbStore.get(key);
        if (data != null) {
           value = SerializationUtils.deserialize(data);
        }
        return value;
    }

}
