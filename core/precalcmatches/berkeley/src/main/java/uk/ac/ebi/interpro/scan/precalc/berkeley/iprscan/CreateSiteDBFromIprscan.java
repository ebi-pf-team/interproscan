package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.*;
import org.springframework.util.Assert;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.SignatureLibraryLookup;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleySite;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleySiteLocation;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.File;
import java.sql.*;
import java.util.*;

/**
 * Creates a Berkeley database of proteins for which matches have been calculated in IPRSCAN.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */


public class CreateSiteDBFromIprscan {

    private static final String databaseName = "IPRSCAN";

    //These indices go hand by hand with the 'berkley_tmp_tab' table

    private static final int SITE_COL_IDX_MATCH_ID = 1;
    private static final int SITE_COL_IDX_NUM_SITES = 2;
    private static final int SITE_COL_IDX_RESIDUE = 3;
    private static final int SITE_COL_IDX_RESIDUE_START = 4;
    private static final int SITE_COL_IDX_RESIDUE_END = 5;
    private static final int SITE_COL_IDX_DESCRIPTION = 6;

    private static final String QUERY_SITE_TEMPORARY_TABLE =
            "select  /*+ PARALLEL */ MATCH_ID, NUM_SITES, RESIDUE, RESIDUE_START, RESIDUE_END, DESCRIPTION " +
                    "       FROM  berkley_site_tmp_tab" +
                    "       order by MATCH_ID";
//                    "       WHERE  ROWNUM < 1000";
//                    "       order by MATCH_ID";

    private static final String TRUNCATE_TEMPORARY_TABLE =
            "truncate table berkley_tmp_tab";

    private static final String DROP_TEMPORARY_TABLE =
            "drop  table berkley_tmp_tab";

    private SecondaryIndex<Long, Long, BerkeleySite> secIDX = null;


    public static void main(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException("Please provide the following arguments:\n\npath to berkeleyDB directory\n" + databaseName + " DB URL (jdbc:oracle:thin:@host:port:SID)\n" + databaseName + " DB username\n" + databaseName + " DB password\nMaximum UPI");
        }
        String directoryPath = args[0];
        String databaseUrl = args[1];
        String username = args[2];
        String password = args[3];
//        String maxUPI = args[4];

        CreateSiteDBFromIprscan instance = new CreateSiteDBFromIprscan();

        instance.buildDatabase(directoryPath,
                databaseUrl,
                username,
                password
        );
    }

    void buildDatabase(String directoryPath, String databaseUrl, String username, String password) {
        long startMillis = System.currentTimeMillis();
        Environment myEnv = null;
        EntityStore store = null;
        Connection connection = null;

        try {
            // Connect to the database.
            Class.forName("oracle.jdbc.OracleDriver");
            connection = DriverManager.getConnection(databaseUrl, username, password);

            // get the sites
            long now = System.currentTimeMillis();
            System.out.println(Utilities.getTimeNow() + " Starting to process site data.");
            startMillis = now;

            PreparedStatement ps = null;
            ResultSet rs = null;
            int siteCount = 0;
            int siteLocationCount = 0;

            BerkeleySite site = null;

            //get the matches
            PrimaryIndex<Long, BerkeleySite> primIDX = null;

            try {
                ps = connection.prepareStatement(QUERY_SITE_TEMPORARY_TABLE);
                rs = ps.executeQuery();

                while (rs.next()) {
                    // Open the BerkeleyDB at the VERY LAST MOMENT - prevent timeouts.
                    if (primIDX == null) {
                        // Now create the berkeley database directory is present and writable.
                        File berkeleyDBDirectory = new File(directoryPath);
                        if (berkeleyDBDirectory.exists()) {
                            if (!berkeleyDBDirectory.isDirectory()) {
                                throw new IllegalStateException("The path " + directoryPath + " already exists and is not a directory, as required for a Berkeley Database.");
                            }
                            File[] directoryContents = berkeleyDBDirectory.listFiles();
                            if (directoryContents != null && directoryContents.length > 0) {
                                throw new IllegalStateException("The directory " + directoryPath + " already has some contents.  The " + CreateSiteDBFromIprscan.class.getSimpleName() + " class is expecting an empty directory path name as argument.");
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

                        primIDX = store.getPrimaryIndex(Long.class, BerkeleySite.class);

                    }

                    final Long matchId = rs.getLong(SITE_COL_IDX_MATCH_ID);
                    if (rs.wasNull()) continue;

                    final Integer numSites = rs.getInt(SITE_COL_IDX_NUM_SITES);
                    if (rs.wasNull()) continue;

                    final String residue = rs.getString(SITE_COL_IDX_RESIDUE);
                    if (rs.wasNull()) continue;

                    final Integer residueStart = rs.getInt(SITE_COL_IDX_RESIDUE_START);
                    if (rs.wasNull()) continue;

                    final Integer residueEnd = rs.getInt(SITE_COL_IDX_RESIDUE_END);
                    if (rs.wasNull()) continue;

                    final String description = rs.getString(SITE_COL_IDX_DESCRIPTION);
                    if (rs.wasNull()) continue;

                    final BerkeleySiteLocation siteLocation = new BerkeleySiteLocation();
                    siteLocation.setResidue(residue);
                    siteLocation.setStart(residueStart);
                    siteLocation.setEnd(residueEnd);
                    siteLocation.setDescription(description);
                    siteLocationCount ++;

                    if (site != null) {
                        if (matchId.equals(site.getMatchId()))  {
                            // Same Match Site  as previous, so just add a new BerkeleySiteLocation
                            site.addSiteLocation(siteLocation);
                        } else {
                            // Store last match
                            primIDX.put(site);
                            siteCount++;
                            if (siteCount % 100000 == 0) {
                                System.out.println(Utilities.getTimeNow() +  " "
                                        + "Stored " + siteCount + " sites, with a total of " + siteLocationCount + " site locations.");
                            }

                            // Create new match and add location to it
                            site = new BerkeleySite();
                            site.setMatchId(matchId);

                            site.addSiteLocation(siteLocation);
                        }
                    } else {
                        // Create new match and add location to it
                        site = new BerkeleySite();
                        site.setMatchId(matchId);

                        site.addSiteLocation(siteLocation);
                    }

//                    final BerkeleySite site = new BerkeleySite();
//                    site.setResidue(residue);
//                    site.setStart(residueStart);
//                    site.setEnd(residueEnd);
//                    site.setNumSites(numSites);
//                    site.setDescription(description);
//                    Set<BerkeleySite> berkeleySites = matchSites.get(matchId);
//                    if(berkeleySites == null){
//                        berkeleySites = new HashSet<>();
//                    }
//                    berkeleySites.add(site);
//                    primIDX.put(site);
//                    matchSites.put(matchId, berkeleySites);

                    siteCount ++;
                    if (siteCount % 200000 == 0) {
                        System.out.println(Utilities.getTimeNow() + " Processed " + siteCount + " sites.");
                    }
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
            now = System.currentTimeMillis();
            System.out.println(Utilities.getTimeNow() +  " " + (now - startMillis) + " milliseconds to query the temporary site table and create the site map for "
                    + siteCount + " sites .");
            startMillis = now;

            System.out.println("Finished building the Sites BerkeleyDB.");


        } catch (DatabaseException dbe) {
            throw new IllegalStateException("Error opening the BerkeleyDB environment", dbe);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load the oracle.jdbc.OracleDriver class", e);
        } catch (SQLException e) {
            throw new IllegalStateException("SQLException thrown by IPRSCAN", e);
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
                    System.out.println("Unable to close the Onion database connection.");
                }
            }
        }
    }

    SecondaryIndex<Long, Long, BerkeleySite> getMatchIdIndex() {
        //secIDX = store.getSecondaryIndex(primIDX, String.class, "proteinMD5");
        return secIDX;
    }

   ;

    public List<BerkeleySite> getSites(List<Long> matchIds) {
        Assert.notNull(getMatchIdIndex(), "The MD5 index must not be null.");
        List<BerkeleySite> sites = new ArrayList<BerkeleySite>();

        for (Long matchId : matchIds) {
            EntityCursor<BerkeleySite> siteCursor = null;
            try {
                siteCursor = getMatchIdIndex().entities(matchId, true, matchId, true);

                BerkeleySite currentSite;
                while ((currentSite = siteCursor.next()) != null) {
                    sites.add(currentSite);
                }
            } finally {
                if (siteCursor != null) {
                    siteCursor.close();
                }
            }
        }

        return sites;
    }

}
