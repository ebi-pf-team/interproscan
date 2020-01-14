package uk.ac.ebi.interpro.scan.precalc.server.service.impl;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.StatsConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntry;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.File;

/**
 * Initializes the SleepyCat database for read only.
 *
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class BerkeleySiteDBService extends AbstractDBService {

    private static final Logger LOGGER = Logger.getLogger(BerkeleySiteDBService.class.getName());

    private String databasePath;

    private SecondaryIndex<String, Long, KVSequenceEntry> secIDX = null;

    private int cacheSizeInBytes;

    private int cachePercentInt;

    Environment myEnv = null;
    EntityStore store = null;

    public BerkeleySiteDBService(String databasePath, int cacheSizeInMegabytes) {
        Assert.notNull(databasePath, "The databasePath bean cannot be null.");
        this.cacheSizeInBytes = cacheSizeInMegabytes * 1024 * 1024;
        this.cachePercentInt = 60;
        this.databasePath = setDeploymentPath(databasePath);
        System.out.println("Initializing BerkeleyDB Site Database (creating indexes): Please wait...");
        initializeMD5Index();
    }


    public BerkeleySiteDBService(String databasePath) {
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

    SecondaryIndex<String, Long, KVSequenceEntry> getMD5Index() {
        return secIDX;
    }

    private void initializeMD5Index() {
        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        //myEnvConfig.setCacheSize(cacheSizeInBytes);  //maybe we should use the setCachePercent i.e. EnvironmentConfig.MAX_MEMORY_PERCENT.
        myEnvConfig.setCachePercent(cachePercentInt);
        myEnvConfig.setReadOnly(true);
        myEnvConfig.setAllowCreate(false);
        myEnvConfig.setLocking(false);
        //myEnvConfig.setConfigParam("je.log.nDataDirectories", Integer.toString(256));

        storeConfig.setReadOnly(true);
        storeConfig.setAllowCreate(false);
        storeConfig.setTransactional(false);

        File file = new File(databasePath);
        // Open the environment and entity store
        myEnv = new Environment(file, myEnvConfig);
        store = new EntityStore(myEnv, "EntityStore", storeConfig);


        PrimaryIndex<Long, KVSequenceEntry> primIDX = store.getPrimaryIndex(Long.class, KVSequenceEntry.class);
        secIDX = store.getSecondaryIndex(primIDX, String.class, "proteinMD5");
    }

    public void displayServerStats(){
        StatsConfig config = new StatsConfig();
        config.setClear(true);

        System.err.println(Utilities.getTimeNow() + " SiteDB " + myEnv.getStats(config));

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
