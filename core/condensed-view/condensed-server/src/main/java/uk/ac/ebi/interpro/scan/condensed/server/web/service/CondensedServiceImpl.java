package uk.ac.ebi.interpro.scan.condensed.server.web.service;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;
import uk.ac.ebi.interpro.scan.condensed.berkeley.model.BerkeleyCondensedMarkup;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Phil Jones
 */
public class CondensedServiceImpl extends AbstractDBService implements CondensedService {

    private static final Logger LOGGER = Logger.getLogger(CondensedServiceImpl.class.getName());

    private String databasePath;

    private PrimaryIndex<String, BerkeleyCondensedMarkup> primIDX = null;

    private SecondaryIndex<String, String, BerkeleyCondensedMarkup> secIDX = null;

    Environment myEnv = null;
    EntityStore store = null;

    public CondensedServiceImpl(String databasePath) {
        Assert.notNull(databasePath, "The databasePath bean cannot be null.");
        this.databasePath = setPath(databasePath);
        System.out.println("Initializing BerkeleyDB MD5 Database (creating indexes): Please wait...");
        initializeIndex();
    }

    private void initializeIndex() {
        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

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


        primIDX = store.getPrimaryIndex(String.class, BerkeleyCondensedMarkup.class);
        secIDX = store.getSecondaryIndex(primIDX, String.class, "uniprot_ac");
    }

    /**
     * Attempts to find the condensed HTML snippet for the protein
     * specified by MD5 or UniProt AC.
     *
     * @param id sequence MD5 or (current) UniProt AC.
     * @return the relevant HTML snippet, or an empty String if no snippet can be found.
     */
    public String getCondensedHtml(String id) {
        if (primIDX.contains(id)) {
            return primIDX.get(id).getHtml();
        } else if (secIDX.contains(id)) {
            return secIDX.get(id).getHtml();
        } else return "";
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
