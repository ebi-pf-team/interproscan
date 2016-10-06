package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import uk.ac.ebi.interpro.scan.model.PersistenceConversion;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.SignatureLibraryLookup;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyLocation;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.BerkeleyMatch;

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


public class CreateMatchDBFromIprscan {

    private static final String databaseName = "IPRSCAN";

    //These indices go hand by hand with the 'berkley_tmp_tab' table
    private static final int COL_IDX_MD5 = 1;
    private static final int COL_IDX_SIG_LIB_NAME = 2;
    private static final int COL_IDX_SIG_LIB_RELEASE = 3;
    private static final int COL_IDX_SIG_ACCESSION = 4;
    private static final int COL_IDX_SCORE = 5;
    private static final int COL_IDX_SEQ_SCORE = 6;
    private static final int COL_IDX_SEQ_EVALUE = 7;
    private static final int COL_IDX_EVALUE = 8;
    private static final int COL_IDX_SEQ_START = 9;
    private static final int COL_IDX_SEQ_END = 10;
    private static final int COL_IDX_HMM_START = 11;
    private static final int COL_IDX_HMM_END = 12;
//    Uncomment because I5 doesn't calculate them at the moment
    private static final int COL_IDX_HMM_BOUNDS = 13;

    private static final String CREATE_TEMP_TABLE =
            "create global temporary table  berkley_tmp_tab " +
                    "on commit preserve rows " +
                    "as " +
                    "select  /*+ PARALLEL */ p.md5 as protein_md5, " +
                    "        l.library as signature_library_name, " +
                    "        l.version as signature_library_release, " +
                    "        m.method_ac as signature_accession, " +
                    "        m.score as score, " +
                    "        m.seqscore as sequence_score, " +
                    "        m.seqevalue as sequence_evalue, " +
                    "        m.evalue, " +
                    "        m.seq_start, " +
                    "        m.seq_end, " +
                    "        m.hmm_start, " +
                    "        m.hmm_end, " +
                    //    Uncomment because I5 doesn't calculate them at the moment
                    "        m.hmm_bounds " +
//                    "        m.hmm_length " +
                    "   from (select upi,md5 from uniparc_protein where upi<='MAX_UPI') p," +
                    "        mv_iprscan m," +
                    "        INTERPRO.iprscan2dbcode r," +
                    "        mv_signature_library_release l" +
                    "  where m.upi = p.upi " +
                    "        AND r.iprscan_sig_lib_rel_id = m.analysis_id " +
                    "        AND r.iprscan_sig_lib_rel_id=l.id";

    private static final String QUERY_TEMPORARY_TABLE =
            "select  /*+ PARALLEL */ PROTEIN_MD5, SIGNATURE_LIBRARY_NAME, SIGNATURE_LIBRARY_RELEASE, SIGNATURE_ACCESSION, SCORE, " +
                    "       SEQUENCE_SCORE, SEQUENCE_EVALUE, EVALUE, SEQ_START, SEQ_END, HMM_START, HMM_END, HMM_BOUNDS " +
                    "       from  berkley_tmp_tab " +
                    "       order by  PROTEIN_MD5, SIGNATURE_LIBRARY_NAME, SIGNATURE_LIBRARY_RELEASE, SIGNATURE_ACCESSION, " +
                    "       SEQUENCE_SCORE";

    private static final String TRUNCATE_TEMPORARY_TABLE =
            "truncate table berkley_tmp_tab";

    private static final String DROP_TEMPORARY_TABLE =
            "drop  table berkley_tmp_tab";


    public static void main(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException("Please provide the following arguments:\n\npath to berkeleyDB directory\n" + databaseName + " DB URL (jdbc:oracle:thin:@host:port:SID)\n" + databaseName + " DB username\n" + databaseName + " DB password\nMaximum UPI");
        }
        String directoryPath = args[0];
        String databaseUrl = args[1];
        String username = args[2];
        String password = args[3];
        String maxUPI = args[4];

        CreateMatchDBFromIprscan instance = new CreateMatchDBFromIprscan();

        instance.buildDatabase(directoryPath,
                databaseUrl,
                username,
                password,
                maxUPI
        );
    }

    void buildDatabase(String directoryPath, String databaseUrl, String username, String password, String maxUPI) {
        long startMillis = System.currentTimeMillis();
        Environment myEnv = null;
        EntityStore store = null;
        Connection connection = null;

        try {
            // Connect to the database.
            Class.forName("oracle.jdbc.OracleDriver");
            connection = DriverManager.getConnection(databaseUrl, username, password);

            // First, create the populate the temporary table before create the BerkeleyDB, to prevent timeouts.
            // we now create the table outside this process
            /*

            Statement statement = null;
            try {
                statement = connection.createStatement();
                statement.execute(CREATE_TEMP_TABLE.replace("MAX_UPI", maxUPI));
            } finally {
                if (statement != null) {
                    statement.close();
                }
            }

            */
            long now = System.currentTimeMillis();
            System.out.println((now - startMillis) + " milliseconds to create the temporary table.");
            startMillis = now;

            PrimaryIndex<Long, BerkeleyMatch> primIDX = null;

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = connection.prepareStatement(QUERY_TEMPORARY_TABLE);
                rs = ps.executeQuery();
                BerkeleyMatch match = null;

                int locationCount = 0, matchCount = 0;

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
                                throw new IllegalStateException("The directory " + directoryPath + " already has some contents.  The " + CreateMatchDBFromIprscan.class.getSimpleName() + " class is expecting an empty directory path name as argument.");
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

                        primIDX = store.getPrimaryIndex(Long.class, BerkeleyMatch.class);

                    }
                    // Only process if the SignatureLibraryName is recognised.
                    final String signatureLibraryName = rs.getString(COL_IDX_SIG_LIB_NAME);
                    if (rs.wasNull() || signatureLibraryName == null) continue;
                    if (SignatureLibraryLookup.lookupSignatureLibrary(signatureLibraryName) == null) continue;

                    // Now collect rest of the data and test for mandatory fields.
                    final int sequenceStart = rs.getInt(COL_IDX_SEQ_START);
                    if (rs.wasNull()) continue;

                    final int sequenceEnd = rs.getInt(COL_IDX_SEQ_END);
                    if (rs.wasNull()) continue;

                    final String proteinMD5 = rs.getString(COL_IDX_MD5);
                    if (proteinMD5 == null || proteinMD5.length() == 0) continue;

                    final String sigLibRelease = rs.getString(COL_IDX_SIG_LIB_RELEASE);
                    if (sigLibRelease == null || sigLibRelease.length() == 0) continue;

                    final String signatureAccession = rs.getString(COL_IDX_SIG_ACCESSION);
                    if (signatureAccession == null || signatureAccession.length() == 0) continue;

                    Integer hmmStart = rs.getInt(COL_IDX_HMM_START);
                    if (rs.wasNull()) hmmStart = null;

                    Integer hmmEnd = rs.getInt(COL_IDX_HMM_END);
                    if (rs.wasNull()) hmmEnd = null;

                    String hmmBounds = rs.getString(COL_IDX_HMM_BOUNDS);

                    Double sequenceScore = rs.getDouble(COL_IDX_SEQ_SCORE);
                    if (rs.wasNull()) sequenceScore = null;

                    Double sequenceEValue = rs.getDouble(COL_IDX_SEQ_EVALUE);
                    if (rs.wasNull()) sequenceEValue = null;

                    Double locationScore = rs.getDouble(COL_IDX_SCORE);
                    if (rs.wasNull()) locationScore = null;

                    Double eValue = rs.getDouble(COL_IDX_EVALUE);
                    if (rs.wasNull()) {
                        eValue = null;
                    }
                    // arrgggh!  The IPRSCAN table stores PRINTS Graphscan values in the hmmBounds column...

                    final BerkeleyLocation location = new BerkeleyLocation();
                    location.setStart(sequenceStart);
                    location.setEnd(sequenceEnd);
                    location.setHmmStart(hmmStart);
                    location.setHmmEnd(hmmEnd);
                    location.setHmmBounds(hmmBounds);
                    location.seteValue(eValue);
                    location.setScore(locationScore);
                    locationCount++;

                    if (match != null) {
                        if (
                                proteinMD5.equals(match.getProteinMD5()) &&
                                        signatureLibraryName.equals(match.getSignatureLibraryName()) &&
                                        sigLibRelease.equals(match.getSignatureLibraryRelease()) &&
                                        signatureAccession.equals(match.getSignatureAccession()) &&
                                        (match.getSequenceEValue() == null && sequenceEValue == null || (sequenceEValue != null && sequenceEValue.equals(match.getSequenceEValue()))) &&
                                        (match.getSequenceScore() == null && sequenceScore == null || (sequenceScore != null && sequenceScore.equals(match.getSequenceScore())))) {
                            // Same Match as previous, so just add a new BerkeleyLocation
                            match.addLocation(location);
                        } else {
                            // Store last match
                            primIDX.put(match);
                            matchCount++;
                            if (matchCount % 100000 == 0) {
                                System.out.println("Stored " + matchCount + " matches, with a total of " + locationCount + " locations.");
                            }

                            // Create new match and add location to it
                            match = new BerkeleyMatch();
                            match.setProteinMD5(proteinMD5);
                            match.setSignatureLibraryName(signatureLibraryName);
                            match.setSignatureLibraryRelease(sigLibRelease);
                            match.setSignatureAccession(signatureAccession);
                            match.setSequenceScore(sequenceScore);
                            match.setSequenceEValue(sequenceEValue);
                            match.addLocation(location);
                        }
                    } else {
                        // Create new match and add location to it
                        match = new BerkeleyMatch();
                        match.setProteinMD5(proteinMD5);
                        match.setSignatureLibraryName(signatureLibraryName);
                        match.setSignatureLibraryRelease(sigLibRelease);
                        match.setSignatureAccession(signatureAccession);
                        match.setSequenceScore(sequenceScore);
                        match.setSequenceEValue(sequenceEValue);
                        match.addLocation(location);
                    }
                }
                // Don't forget the last match!
                if (match != null) {
                    primIDX.put(match);
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
            now = System.currentTimeMillis();
            System.out.println((now - startMillis) + " milliseconds to query the temporary table and create the BerkeleyDB.");
            startMillis = now;


            // Truncate the temporary table
            // Then add the additional SignalP data
            //
            /*
            statement = null;
            try {
                statement = connection.createStatement();
                statement.execute(TRUNCATE_TEMPORARY_TABLE);
            } finally {
                if (statement != null) {
                    statement.close();
                }
            }
            */

            now = System.currentTimeMillis();
            System.out.println((now - startMillis) + " milliseconds to truncate the temporary table.");
            startMillis = now;

            // And drop the table
            // Then add the additional SignalP data
            //
            /*
            statement = null;
            try {
                statement = connection.createStatement();
                statement.execute(DROP_TEMPORARY_TABLE);
            } finally {
                if (statement != null) {
                    statement.close();
                }
            }
            */

            now = System.currentTimeMillis();
            System.out.println((now - startMillis) + " milliseconds to drop the temporary table.");

            System.out.println("Finished building BerkeleyDB.");


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
}
