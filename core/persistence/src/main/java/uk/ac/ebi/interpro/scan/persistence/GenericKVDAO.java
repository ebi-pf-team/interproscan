package uk.ac.ebi.interpro.scan.persistence;

import org.springframework.transaction.annotation.Transactional;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.persistence.kvstore.KVDB;

public interface GenericKVDAO <T>  extends GenericDAO<T, Long>  {

    @Transactional
    void persist(String key,  T value);

    @Transactional
    void persist(byte[] byteKey,  byte[] byteValue);

    T get(String key);

    KVDB getDbStore();

}
