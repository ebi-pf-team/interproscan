package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import com.sleepycat.je.*;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import java.io.File;


public class BerkeleyDBJE implements AutoCloseable{
    private Environment env = null;
    private EntityStore store = null;
    private StatsConfig config = null;

    public BerkeleyDBJE(File directory) {
        // Set up the environment
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setCacheSize(24L * 1024 * 1024 * 1024);
        envConfig.setTransactional(false);

        // *.jdb files target size (200M)
        envConfig.setConfigParam("je.log.fileMax", Integer.toString(200 * 1024 * 1024));
        //
        envConfig.setConfigParam("je.log.useODSYNC", Boolean.toString(true));
        // Do not start checkpointer thread
        envConfig.setConfigParam("je.env.runCheckpointer", Boolean.toString(false));
        // Do not start cleaner thread
        envConfig.setConfigParam("je.env.runCleaner", Boolean.toString(false));
        //
        envConfig.setConfigParam("je.env.verifyBtreeBatchSize", Integer.toString(10000));
        this.env = new Environment(directory, envConfig);

        // Set up the entity store
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setTransactional(false);
        storeConfig.setDeferredWrite(true);
        this.store = new EntityStore(this.env, "EntityStore", storeConfig);

        config = new StatsConfig();
        config.setClear(true);
    }

    public void close(){
        if (this.store != null) {
            try {
                this.store.close();
            } catch (DatabaseException dbe) {
                throw new IllegalStateException("Unable to close the BerkeleyDBJE connection.", dbe);
            }
        }

        if (this.env != null) {
            try {
                this.env.close();
            } catch (DatabaseException dbe) {
                throw new IllegalStateException("Unable to close the BerkeleyDBJE environment.", dbe);
            }
        }
    }

    protected EntityStore getStore() {
        return this.store;
    }

    protected EnvironmentStats getStats() {
        return this.env.getStats(config);
    }
}
