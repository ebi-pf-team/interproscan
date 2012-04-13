package uk.ac.ebi.interpro.scan.precalc.server.service.impl;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import java.io.File;

/**
 * @author Phil Jones
 *         Date: 13/04/12
 */
public abstract class AbstractDBService {

    private static final Logger LOGGER = Logger.getLogger(AbstractDBService.class.getName());

    protected static final String DATA_PATH_JAVA_OPTION = "berkely.db.data";

    private String databasePath;

    private Environment myEnv = null;

    protected EntityStore store = null;

    protected AbstractDBService(final String databasePath) {
        Assert.notNull(databasePath, "The databasePath bean cannot be null.");
        this.databasePath = setPath(databasePath);
        System.out.println("Initializing BerkeleyDB Database at " + this.databasePath + " (creating indexes): Please wait...");
        initializeIndex();
    }

    protected abstract void initializeIndex();

    protected String setPath(String pathProperty) {
        if (pathProperty.contains(DATA_PATH_JAVA_OPTION)) {
            String dataPath = System.getProperty(DATA_PATH_JAVA_OPTION);
            if (dataPath == null || dataPath.isEmpty()) {
                throw new IllegalStateException("This server has been configured to lookup a Java option " + DATA_PATH_JAVA_OPTION + " however this option has not been set.  This should be set in the Tomcat Controller Script, e.g. JAVA_OPTS=\"$JAVA_OPTS -D" + DATA_PATH_JAVA_OPTION + "=${CATALINA_BASE}/deploy/data:/${TOMCAT_HOSTNAME##*-}\"");
            }
            return pathProperty.replace(DATA_PATH_JAVA_OPTION, dataPath);
        } else return pathProperty;
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

    protected void initializeIndexCommon() {
        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        myEnvConfig.setAllowCreate(false);
        storeConfig.setAllowCreate(false);
        storeConfig.setTransactional(false);

        File file = new File(databasePath);
        // Open the environment and entity store
        myEnv = new Environment(file, myEnvConfig);
        store = new EntityStore(myEnv, "EntityStore", storeConfig);
    }
}
