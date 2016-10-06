package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyConsideredProtein;

import java.io.File;
import java.sql.*;

/**
 * Creates a Berkeley database of proteins for which matches have been calculated in IPRSCAN.
 *
 * @author Phil Jones
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class CreateMD5ListFromIprscan {

    private static final int COL_IDX_MD5 = 1;

    private static final String databaseName = "IPRSCAN";

//    private static final String MD5_QUERY =
//            "select * from (select p.md5 as protein_md5 " +
//                    "  from iprscan.uniparc_protein p " +
//                    "  where p.UPI <= ?) " +
//                    "  order by protein_md5";

    private static final String MD5_QUERY =
            "select md5 as protein_md5 from berkerly_tmp_upi_md5 order by protein_md5";


    public static void main(String[] args) {

        if (args.length < 5) {
            throw new IllegalArgumentException("Please provide the following arguments:\n\npath to berkeleyDB directory\n" + databaseName + " DB URL (jdbc:oracle:thin:@host:port:SID)\n" + databaseName + " DB username\n" + databaseName + " DB password\nMaximum UPI");
        }
        String directoryPath = args[0];
        String databaseUrl = args[1];
        String databaseUsername = args[2];
        String databasePassword = args[3];
        String maxUPI = args[4];
        CreateMD5ListFromIprscan instance = new CreateMD5ListFromIprscan();

        instance.buildDatabase(directoryPath,
                databaseUrl,
                databaseUsername,
                databasePassword,
                maxUPI
        );


    }

    void buildDatabase(String directoryPath, String databaseUrl, String databaseUsername, String databasePassword, String maxUPI) {
        Environment myEnv = null;
        EntityStore store = null;
        Connection connection = null;

        try {
            // Start off making sure that the berkeley database directory is present and writable.
            File berkeleyDBDirectory = new File(directoryPath);
            if (berkeleyDBDirectory.exists()) {
                if (!berkeleyDBDirectory.isDirectory()) {
                    throw new IllegalStateException("The path " + directoryPath + " already exists and is not a directory, as required for a Berkeley Database.");
                }
                File[] directoryContents = berkeleyDBDirectory.listFiles();
                if (directoryContents != null && directoryContents.length > 0) {
                    throw new IllegalStateException("The directory " + directoryPath + " already has some contents.  The " + CreateMD5ListFromIprscan.class.getSimpleName() + " class is expecting an empty directory path name as argument.");
                }
                if (!berkeleyDBDirectory.canWrite()) {
                    throw new IllegalStateException("The directory " + directoryPath + " is not writable.");
                }
            } else if (!(berkeleyDBDirectory.mkdirs())) {
                throw new IllegalStateException("Unable to create Berkeley database directory " + directoryPath);
            }

            // Open up the Berkeley Database
            EnvironmentConfig myEnvConfig = new EnvironmentConfig();
            StoreConfig storeConfig = new StoreConfig();

            myEnvConfig.setAllowCreate(true);
            storeConfig.setAllowCreate(true);
            storeConfig.setTransactional(false);
            // Open the environment and entity store
            myEnv = new Environment(berkeleyDBDirectory, myEnvConfig);
            store = new EntityStore(myEnv, "EntityStore", storeConfig);

            PrimaryIndex<String, BerkeleyConsideredProtein> primIDX = store.getPrimaryIndex(String.class, BerkeleyConsideredProtein.class);

            // Connect to the database.
            Class.forName("oracle.jdbc.OracleDriver");
            connection = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);

            PreparedStatement ps = connection.prepareStatement(MD5_QUERY);
//            ps.setString(1, maxUPI);
            ResultSet rs = ps.executeQuery();

            int proteinCount = 0;

            while (rs.next()) {
                final String proteinMD5 = rs.getString(COL_IDX_MD5);
                if (proteinMD5 == null || proteinMD5.length() == 0) continue;
                ///TODO:?? arrgggh!  The IPRSCAN table stores PRINTS Graphscan values in the hmmBounds column...
                BerkeleyConsideredProtein protein = new BerkeleyConsideredProtein(proteinMD5);

                // Store last protein
                primIDX.put(protein);
                proteinCount++;
                if (proteinCount % 100000 == 0) {
                    System.out.println("Stored " + proteinCount + " considered proteins.");
                }
            }
        } catch (DatabaseException dbe) {
            throw new IllegalStateException("Error opening the BerkeleyDB environment", dbe);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load the oracle.jdbc.OracleDriver class", e);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to connect to the database", e);
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (DatabaseException dbe) {
                    System.out.println("Unable to close the BerkeleyDB connection.");
                }
            }

            if (myEnv != null) {
                try {
                    // Finally, close environment.
                    myEnv.close();
                } catch (DatabaseException dbe) {
                    System.out.println("Unable to close the BerkeleyDB environment.");
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.out.println("Unable to close the database connection.");
                }
            }
        }
    }
}
