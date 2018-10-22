package uk.ac.ebi.interpro.scan.precalc.berkeley.dbstore;


import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.StoreConfig;

import java.io.File;
import java.io.UnsupportedEncodingException;


/**
 * level DB store
 */
public class BerkeleyDBStore implements AutoCloseable{

    Environment myEnv = null;
    EntityStore entityStore = null;

    public BerkeleyDBStore() {

    }

    public void create(String dbStore, File berkeleyDBDirectory) {
        String directoryPath = dbStore;

        final int numSubDirs = 256;
        //mkdir data{001..256}
        for (int i = 1; i <= numSubDirs; i++) {
            File subDir = new File(directoryPath + File.separator + "data" + String.format("%03d", i));
            if (!subDir.exists()) {
                subDir.mkdir();
            }
        }
        // Open up the Berkeley Database
        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        myEnvConfig.setAllowCreate(true);
        // Split *.jdb log files into subdirectories in the env home dir
        myEnvConfig.setConfigParam("je.log.nDataDirectories", Integer.toString(numSubDirs));
        storeConfig.setAllowCreate(true);
        storeConfig.setTransactional(false);
        // Open the environment and entity store
        myEnv = new Environment(berkeleyDBDirectory, myEnvConfig);
        entityStore = new EntityStore(myEnv, "EntityStore", storeConfig);

    }


    public void close(){
        if (entityStore != null) {
            try {
                entityStore.close();
            } catch (DatabaseException dbe) {
                throw new IllegalStateException("Unable to close the BerkeleyDB connection.", dbe);
            }
        }

        if (myEnv != null) {
            try {
                // Finally, close environment.
                myEnv.close();
            } catch (DatabaseException dbe) {
                throw new IllegalStateException("Unable to close the BerkeleyDB environment.", dbe);
            }
        }
    }

    public EntityStore getEntityStore() {
        return entityStore;
    }
}

