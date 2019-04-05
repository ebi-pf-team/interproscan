package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import com.sleepycat.persist.PrimaryIndex;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.SignatureLibraryLookup;
import uk.ac.ebi.interpro.scan.precalc.berkeley.dbstore.BerkeleyDBStore;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntry;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupSite;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.File;
import java.sql.*;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;


/**
 * Creates a Berkeley database of proteins for which matches have been calculated in IPRSCAN.
 *
 * @author Phil Jones
 * @author Maxim Scheremetjew
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */


public class CreateSiteDBFromIprscanBerkeleyDB {

    private static final String databaseName = "IPRSCAN";

    private static String QUERY_TEMPORARY_TABLE =
            "select  /*+ PARALLEL (6) */  " +
                    "PROTEIN_MD5, SIGNATURE_LIBRARY_NAME, SIGNATURE_LIBRARY_RELEASE, " +
                    "SIGNATURE_ACCESSION, LOC_START, LOC_END, NUM_SITES, RESIDUE, " +
                    "RESIDUE_START, RESIDUE_END, DESCRIPTION " +
                    "       from  LOOKUP_SITE_TMP_TAB  partition (partitionName) " +
                    "       where upi_range = ? " +
                    "       order by  PROTEIN_MD5, SIGNATURE_LIBRARY_NAME, SIGNATURE_LIBRARY_RELEASE, SIGNATURE_ACCESSION, LOC_START, LOC_END";

    private static String QUERY_ENABLE_DML = "alter session enable parallel dml";

    public static void main(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException("Please provide the following arguments:\n\npath to berkeleyDB directory\n" + databaseName + " DB URL (jdbc:oracle:thin:@host:port:SID)\n" + databaseName + " DB username\n" + databaseName + " DB password\nMaximum UPI");
        }
        String directoryPath = args[0];
        String databaseUrl = args[1];
        String username = args[2];
        String password = args[3];
        String maxUPI = args[4];
        int fetchSize = 100000;
        if (args.length >= 6) {
            fetchSize =  Integer.parseInt(args[5]);
        }

        CreateSiteDBFromIprscanBerkeleyDB instance = new CreateSiteDBFromIprscanBerkeleyDB();

        instance.buildDatabase(directoryPath,
                databaseUrl,
                username,
                password,
                maxUPI,
                fetchSize
        );
    }

    void buildDatabase(String directoryPath, String databaseUrl, String username, String password, String maxUPI, int fetchSize) {
        long startMillis = System.currentTimeMillis();

        try {
            // Connect to the database.
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException cex) {
            cex.printStackTrace();
        }
        try (Connection connection = DriverManager.getConnection(databaseUrl, username, password)) {

            long now = System.currentTimeMillis();
            System.out.println(Utilities.getTimeNow() + " Start the lookup match servive data build @ " + directoryPath);
            startMillis = now;

            PrimaryIndex<Long, KVSequenceEntry> primIDX = null;

            Set <String>  partitionNames = getPartitionNames(connection);

            //prepare the db directory
            final File lookupMatchDBDirectory = new File(directoryPath);
            if (lookupMatchDBDirectory.exists()) {
                if (!lookupMatchDBDirectory.isDirectory()) {
                    throw new IllegalStateException("The path " + directoryPath + " already exists and is not a directory, as required for a KV Database.");
                }
                File[] directoryContents = lookupMatchDBDirectory.listFiles();
                if (directoryContents != null && directoryContents.length > 0) {
                    //System.out.println("The directory " + directoryPath + " already has some contents.  The " + CreateMatchDBFromIprscanBerkeleyDB.class.getSimpleName() + " class is expecting an empty directory path name as argument.");
                    throw new IllegalStateException("The directory " + directoryPath + " already has some contents.  The " + CreateSiteDBFromIprscanBerkeleyDB.class.getSimpleName() + " class is expecting an empty directory path name as argument.");
                }
                if (!lookupMatchDBDirectory.canWrite()) {
                    throw new IllegalStateException("The directory " + directoryPath + " is not writable.");
                }
            } else if (!(lookupMatchDBDirectory.mkdirs())) {
                throw new IllegalStateException("Unable to create KV database directory " + directoryPath);
            }

            String dbStoreName = directoryPath;

            System.out.println("Create the Berkeley DB Store and populate ... ");
            try (BerkeleyDBStore lookupMatchDB = new BerkeleyDBStore()){
                lookupMatchDB.create(dbStoreName, lookupMatchDBDirectory);
                if (primIDX == null) {
                    primIDX = lookupMatchDB.getEntityStore().getPrimaryIndex(Long.class, KVSequenceEntry.class);
                }

                long locationFragmentCount = 0, proteinMD5Count = 0, matchCount = 0;
                int partitionCount = 0;


                try (PreparedStatement psParallelDML = connection.prepareStatement(QUERY_ENABLE_DML)) {
                    boolean executeParallelDML = psParallelDML.execute();
                }
                for (String partitionName : partitionNames) {
                    partitionCount++;
//                    if (partitionName.compareTo("UPI00085") <= 0){
//                        continue;
//                    }
                    long startPartition = System.currentTimeMillis();
                    int partitionMatchCount = 0;
                    System.out.println(Utilities.getTimeNow() + " Now processing partition #" + partitionCount + " :-  " + partitionName);
                    String partitionQueryLookupTable = QUERY_TEMPORARY_TABLE.replace("partitionName", partitionName);
                    System.out.println(Utilities.getTimeNow() + " sql for this partition: " + partitionQueryLookupTable);
                    try (PreparedStatement ps = connection.prepareStatement(partitionQueryLookupTable)) {
                        //should we play witht eh featch array size
                        System.out.println(Utilities.getTimeNow() + " old FetchSize: " + ps.getFetchSize());
                        ps.setFetchSize(fetchSize);
                        System.out.println(Utilities.getTimeNow() + "  new FetchSize: " + ps.getFetchSize());
                        ps.setString(1, partitionName);
                        //ps.setString(2, partitionName);
                        //System.out.println(Utilities.getTimeNow() + "sql:" + ps.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            long endExecuteQueryMillis = System.currentTimeMillis();
                            System.out.println(Utilities.getTimeNow() + "  " + String.valueOf((endExecuteQueryMillis - startPartition) / 1000) + " seconds to process query");
                            //BerkeleyMatch match = null;
                            KVSequenceEntry match = null;


                            while (rs.next()) {

                                // Only process if the SignatureLibraryName is recognised.
                                final String signatureLibraryName = rs.getString(SimpleLookupSite.COL_IDX_SIG_LIB_NAME);
//                        System.out.println(Utilities.getTimeNow() + " signatureLibraryName : # " + COL_IDX_SIG_LIB_NAME + " - " +  signatureLibraryName);
                                if (rs.wasNull() || signatureLibraryName == null) continue;
                                SignatureLibrary signatureLibrary = SignatureLibraryLookup.lookupSignatureLibrary(signatureLibraryName);
                                if (signatureLibrary == null) {
				/*
                                || signatureLibrary.getName().equals(SignatureLibrary.PHOBIUS.getName())
                                || signatureLibrary.getName().equals(SignatureLibrary.TMHMM.getName())
                                || signatureLibrary.getName().equals(SignatureLibrary.SIGNALP_EUK.getName())
                                || signatureLibrary.getName().equals(SignatureLibrary.SIGNALP_GRAM_POSITIVE.getName())
                                || signatureLibrary.getName().equals(SignatureLibrary.SIGNALP_GRAM_NEGATIVE.getName())
                                ){
				*/
                                    continue;
                                }

                                // Now collect rest of the data and test for mandatory fields.
                                final int locationStart = rs.getInt(SimpleLookupSite.COL_IDX_LOC_START);
//                        System.out.println(Utilities.getTimeNow() + " sequenceStart : # " + COL_IDX_SEQ_START + " - " +  sequenceStart);
                                if (rs.wasNull()) continue;

                                final int locationEnd = rs.getInt(SimpleLookupSite.COL_IDX_LOC_END);
//                        System.out.println(Utilities.getTimeNow() + " sequenceEnd : # " + COL_IDX_SEQ_END + " - " +  sequenceEnd);
                                if (rs.wasNull()) continue;

                                // have the md5 in uppercase
                                final String proteinMD5 = rs.getString(SimpleLookupSite.COL_IDX_MD5).toUpperCase();
//                        System.out.println(Utilities.getTimeNow() + " proteinMD5 : # " + COL_IDX_MD5 + " - " +  proteinMD5);
                                if (proteinMD5 == null || proteinMD5.length() == 0) continue;

                                final String sigLibRelease = rs.getString(SimpleLookupSite.COL_IDX_SIG_LIB_RELEASE);
                                if (sigLibRelease == null || sigLibRelease.length() == 0) continue;

                                final String signatureAccession = rs.getString(SimpleLookupSite.COL_IDX_SIG_ACCESSION);
                                if (signatureAccession == null || signatureAccession.length() == 0) continue;


                                Integer numSites = rs.getInt(SimpleLookupSite.COL_IDX_NUM_SITES);
                                if (rs.wasNull()) numSites = null;

                                String residue = rs.getString(SimpleLookupSite.COL_IDX_RESIDUE);
                                if (rs.wasNull()) residue = null;

                                Integer residueStart = rs.getInt(SimpleLookupSite.COL_IDX_RESIDUE_START);
                                if (rs.wasNull()) residueStart = null;

                                Integer residueEnd = rs.getInt(SimpleLookupSite.COL_IDX_RESIDUE_END);
                                if (rs.wasNull()) residueEnd = null;

                                String description = rs.getString(SimpleLookupSite.COL_IDX_DESCRIPTION);
                                if (rs.wasNull()) description = null;


                                String columnDelimiter = ",";
                                StringJoiner kvMatchJoiner = new StringJoiner(columnDelimiter);

                                kvMatchJoiner.add(signatureLibraryName);
                                kvMatchJoiner.add(sigLibRelease);
                                kvMatchJoiner.add(signatureAccession);
                                kvMatchJoiner.add(kvValueOf(locationStart));
                                kvMatchJoiner.add(kvValueOf(locationEnd));
                                kvMatchJoiner.add(kvValueOf(numSites));
                                kvMatchJoiner.add(kvValueOf(residue));
                                kvMatchJoiner.add(kvValueOf(residueStart));
                                kvMatchJoiner.add(kvValueOf(residueEnd));

                                String kvMatch = kvMatchJoiner.toString();

                                if (matchCount == 0) {
                                    System.out.println(Utilities.getTimeNow() + " match 0:  " + kvMatch.toString());
                                }
                                if (match == null) {
                                    match = new KVSequenceEntry();
                                    match.setProteinMD5(proteinMD5);
                                    match.addMatch(kvMatch);
                                } else {
                                    if (proteinMD5.equals(match.getProteinMD5())) {
                                        match.addMatch(kvMatch);
                                    } else {
                                        primIDX.put(match);
                                        proteinMD5Count++;
                                        match = new KVSequenceEntry();
                                        match.setProteinMD5(proteinMD5);
                                        match.addMatch(kvMatch);
                                    }
                                }
                                matchCount++;
                                partitionMatchCount++;
                                if (partitionMatchCount == 1) {
                                    System.out.println(Utilities.getTimeNow() + " match 1:  " + match.toString());
                                }
                                if (matchCount % 2000000 == 0) {
                                    if (matchCount % 6000000 == 0) {
                                        long startSync = System.currentTimeMillis();
                                        System.out.println(Utilities.getTimeNow() + " Start Sync to disk ");
                                        lookupMatchDB.getEntityStore().sync();
                                        long syncTime = System.currentTimeMillis() - startSync;
                                        Integer syncTimeSeconds = (int) syncTime / 1000;
                                        System.out.println(Utilities.getTimeNow() + " Sync to disk " + proteinMD5Count + " protein MD5s, with a total of " + matchCount + " matches.-- syncTimeSeconds: " + syncTimeSeconds);
                                    } else {
                                        System.out.println(Utilities.getTimeNow() + " Stored " + proteinMD5Count + " protein MD5s, with a total of " + matchCount + " matches.");
                                    }
                                }
                            }
                            // Don't forget the last match!
                            if (match != null) {
                                primIDX.put(match);
                            }
                            // partition statistics
                            long timeProcessingPartition = System.currentTimeMillis() - startPartition;
                            Integer timeProcessingPartitionSeconds = (int) timeProcessingPartition / 1000;
                            System.out.println(Utilities.getTimeNow() + " Stored " + partitionMatchCount + " partition matches ( " + proteinMD5Count + " total md5s) in " + timeProcessingPartitionSeconds + " seconds");
                            if (timeProcessingPartitionSeconds < 1) {
                                System.out.println(Utilities.getTimeNow() + " timeProcessingPartitionSeconds =  " + timeProcessingPartitionSeconds);
                                timeProcessingPartitionSeconds = 1;
                            }
                            Integer throughputPerSecond = partitionMatchCount / timeProcessingPartitionSeconds;
                            System.out.println(Utilities.getTimeNow() + " ThroughputPerSecond =  " + throughputPerSecond + "  .... o ......");
                            //break;  //lets look at one partition now
                        } catch (SQLException e) {
                            e.printStackTrace();
                            throw new IllegalStateException("SQLException thrown by IPRSCAN", e);
                        }
                        System.out.println(Utilities.getTimeNow() + " Stored " + matchCount + " matches");
                        //lookupMatchDB.close();  not needed as used the autocloseable
                        lookupMatchDB.getEntityStore().sync();

                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new IllegalStateException("SQLException thrown by IPRSCAN", e);
                    }

                    System.out.println("Finished building BerkeleyDB.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("SQLException thrown by IPRSCAN", e);
        }
    }

    public Set <String>  getPartitionNames(Connection connection){
        Set <String> partitionNames = new TreeSet<>();

        //tmp_lookup_tmp_tab_part
        String partitionQuery = "SELECT PARTITION_NAME  FROM ALL_TAB_PARTITIONS     where table_name = 'LOOKUP_SITE_TMP_TAB' ORDER BY PARTITION_NAME";
//        String partitionQuery = "SELECT PARTITION_NAME  FROM ALL_TAB_PARTITIONS     where table_name = 'LOOKUP_TMP_TAB' and PARTITION_NAME <= 'UPI00002' ORDER BY PARTITION_NAME";

        int count = 0;
        try {
            PreparedStatement ps = connection.prepareStatement(partitionQuery);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String partitionName = rs.getString(1);
                partitionNames.add(partitionName);
//                if (partitionName.startsWith("UPI0001")){
//                    partitionNames.add(partitionName);;
//                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("SQLException thrown by IPRSCAN", e);
        }
        System.out.println("partitionNames size :" + partitionNames.size());
        return partitionNames;
    }

    public static String kvValueOf(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

}
