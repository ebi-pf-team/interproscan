package uk.ac.ebi.interpro.scan.precalc.server.service.impl;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

import java.io.File;

/**
 * Initializes the SleepyCat database for read only.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class BerkeleyMatchDBService extends AbstractDBService {

    private static final Logger LOGGER = Logger.getLogger(BerkeleyMatchDBService.class.getName());

    private String databasePath;

    private SecondaryIndex<String, Long, BerkeleyMatch> secIDX = null;

    private int cacheSizeInBytes;

    Environment myEnv = null;
    EntityStore store = null;

    public BerkeleyMatchDBService(String databasePath, int cacheSizeInMegabytes) {
        Assert.notNull(databasePath, "The databasePath bean cannot be null.");
        this.cacheSizeInBytes = cacheSizeInMegabytes * 1024 * 1024;
        this.databasePath = setDeploymentPath(databasePath);
        System.out.println("Initializing BerkeleyDB Match Database (creating indexes): Please wait...");
        initializeMD5Index();
    }


    public BerkeleyMatchDBService(String databasePath) {
        // default cache memory set to  100 Mb to avoid using too much memory
        this(databasePath, 100);
    }


    /**
     * Clean up resources when this Service is finished with.
     *
     * @throws Throwable the <code>Exception</code> raised by this method
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        shutdown();
    }

    SecondaryIndex<String, Long, BerkeleyMatch> getMD5Index() {
        return secIDX;
    }

    private void initializeMD5Index() {
        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        myEnvConfig.setCacheSize(cacheSizeInBytes);
        myEnvConfig.setReadOnly(true);
        myEnvConfig.setAllowCreate(false);
        myEnvConfig.setLocking(false);

        storeConfig.setReadOnly(true);
        storeConfig.setAllowCreate(false);
        storeConfig.setTransactional(false);

        File file = new File(databasePath);
        // Open the environment and entity store
        myEnv = new Environment(file, myEnvConfig);
        store = new EntityStore(myEnv, "EntityStore", storeConfig);


        PrimaryIndex<Long, BerkeleyMatch> primIDX = store.getPrimaryIndex(Long.class, BerkeleyMatch.class);
        secIDX = store.getSecondaryIndex(primIDX, String.class, "proteinMD5");
    }

    public void shutdown() {
        if (store != null) {
            try {
                store.close();
                store = null;
            } catch (DatabaseException dbe) {
                LOGGER.error("Error closing store: " + dbe.toString());
            }
        }

        if (myEnv != null) {
            try {
                // Finally, close environment.
                myEnv.close();
                myEnv = null;
            } catch (DatabaseException dbe) {
                LOGGER.error("Error closing MyDbEnv: " + dbe.toString());
            }
        }
    }
}
