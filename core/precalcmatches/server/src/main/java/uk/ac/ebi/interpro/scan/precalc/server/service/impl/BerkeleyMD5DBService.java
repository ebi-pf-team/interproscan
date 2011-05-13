package uk.ac.ebi.interpro.scan.precalc.server.service.impl;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyConsideredProtein;

import java.io.File;

/**
 * Initializes the SleepyCat database for read only.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class BerkeleyMD5DBService {

    private static final Logger LOGGER = Logger.getLogger(BerkeleyMD5DBService.class.getName());

    private String databasePath;

    private PrimaryIndex<String, BerkeleyConsideredProtein> primIDX = null;

    Environment myEnv = null;
    EntityStore store = null;

    public BerkeleyMD5DBService(String databasePath) {
        Assert.notNull(databasePath, "The databasePath bean cannot be null.");
        this.databasePath = databasePath;
        System.out.println("Initializing BerkeleyDB MD5 Database (creating indexes): Please wait...");
        initializeMD5Index();
    }

    /**
     * Clean up resources when this Service is finished with.
     *
     * @throws Throwable the <code>Exception</code> raised by this method
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if (store != null) {
            try {
                store.close();
            } catch (DatabaseException dbe) {
                LOGGER.error("Error closing store: " + dbe.toString());
            }
        }

        if (myEnv != null) {
            try {
                // Finally, close environment.
                myEnv.close();
            } catch (DatabaseException dbe) {
                LOGGER.error("Error closing MyDbEnv: " + dbe.toString());
            }
        }
    }

    public PrimaryIndex<String, BerkeleyConsideredProtein> getPrimIDX() {
        return primIDX;
    }

    private void initializeMD5Index() {
        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        myEnvConfig.setAllowCreate(false);
        storeConfig.setAllowCreate(false);
        storeConfig.setTransactional(false);

        File file = new File(databasePath);
        // Open the environment and entity store
        myEnv = new Environment(file, myEnvConfig);
        store = new EntityStore(myEnv, "EntityStore", storeConfig);


        primIDX = store.getPrimaryIndex(String.class, BerkeleyConsideredProtein.class);
    }
}
