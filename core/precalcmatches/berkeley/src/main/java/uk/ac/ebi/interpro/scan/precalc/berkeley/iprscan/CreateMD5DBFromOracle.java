package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyConsideredProtein;
import uk.ac.ebi.interpro.scan.util.Utilities;
import java.io.File;
import java.sql.*;

/**
 * Creates a Berkeley database of proteins for which matches have been calculated in IPRSCAN.
 *
 * @author Phil Jones
 * @author Maxim Scheremetjew
 * @author Matthias Blum
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class CreateMD5DBFromOracle {
    private static final String USER = "IPRSCAN";
    private static final String QUERY = "SELECT MD5 FROM " + USER + ".LOOKUP_MD5 ORDER BY MD5";

    void buildDatabase(String url, String password, int fetchSize, File outputDirectory) {
        Environment env = null;
        EntityStore store = null;

        System.out.println(Utilities.getTimeAlt() + ": starting");

        try (Connection connection = DriverManager.getConnection(url, USER, password)) {
            // Set up the environment
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);
            envConfig.setTransactional(false);
            env = new Environment(outputDirectory, envConfig);

            // Set up the entity store
            StoreConfig storeConfig = new StoreConfig();
            storeConfig.setAllowCreate(true);
            storeConfig.setTransactional(false);
            store = new EntityStore(env, "EntityStore", storeConfig);

            PrimaryIndex<String, BerkeleyConsideredProtein> index = store.getPrimaryIndex(String.class, BerkeleyConsideredProtein.class);

            try (PreparedStatement ps = connection.prepareStatement(QUERY)) {
                ps.setFetchSize(fetchSize);

                try (ResultSet rs = ps.executeQuery()) {
                    int proteinCount = 0;
                    while (rs.next()) {
                        final String proteinMD5 = rs.getString(1);
                        if (proteinMD5 == null || proteinMD5.length() == 0) continue;

                        BerkeleyConsideredProtein protein = new BerkeleyConsideredProtein(proteinMD5);
                        index.put(protein);

                        proteinCount++;
                        if (proteinCount % 10000000 == 0) {
                            System.out.println(Utilities.getTimeAlt() + ": " + String.format("%,d", proteinCount) + " sequences processed");
                        }
                    }

                    System.out.println(Utilities.getTimeAlt() + ": " + String.format("%,d", proteinCount) + " sequences processed");
                }
            }
        } catch (DatabaseException dbe) {
            throw new IllegalStateException("Error opening the BerkeleyDB environment", dbe);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to connect to the database", e);
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }

            if (env != null) {
                try {
                    env.close();
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println(Utilities.getTimeAlt() + ": done");
    }
}
