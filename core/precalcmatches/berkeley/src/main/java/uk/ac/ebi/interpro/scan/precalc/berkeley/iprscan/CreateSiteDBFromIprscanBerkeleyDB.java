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
 * @author Matthias Blum
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */


public class CreateSiteDBFromIprscanBerkeleyDB {
    private static String QUERY_TABLE =
            "SELECT MD5, SIGNATURE_LIBRARY_NAME, SIGNATURE_LIBRARY_RELEASE, SIGNATURE_ACCESSION, " +
                    "LOC_START, LOC_END, NUM_SITES, RESIDUE, RESIDUE_START, RESIDUE_END, DESCRIPTION " +
                    "FROM LOOKUP_SITE ORDER BY MD5";

    void buildDatabase(String directoryPath, String databaseUrl, String username, String password, int fetchSize) {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException cex) {
            cex.printStackTrace();
        }
        try (Connection connection = DriverManager.getConnection(databaseUrl, username, password)) {
            System.out.println(Utilities.getTimeNow() + " Start the lookup match servive data build @ " + directoryPath);

            final File lookupMatchDBDirectory = new File(directoryPath);
            if (lookupMatchDBDirectory.exists()) {
                if (!lookupMatchDBDirectory.isDirectory()) {
                    throw new IllegalStateException("The path " + directoryPath + " already exists and is not a directory, as required for a KV Database.");
                }
                File[] directoryContents = lookupMatchDBDirectory.listFiles();
                if (directoryContents != null && directoryContents.length > 0) {
                    throw new IllegalStateException("The directory " + directoryPath + " already has some contents.  The " + CreateSiteDBFromIprscanBerkeleyDB.class.getSimpleName() + " class is expecting an empty directory path name as argument.");
                } else if (!lookupMatchDBDirectory.canWrite()) {
                    throw new IllegalStateException("The directory " + directoryPath + " is not writable.");
                }
            } else if (!(lookupMatchDBDirectory.mkdirs())) {
                throw new IllegalStateException("Unable to create KV database directory " + directoryPath);
            }

            System.out.println("Create the Berkeley DB Store and populate ... ");
            PrimaryIndex<Long, KVSequenceEntry> primIDX = null;
            try (BerkeleyDBStore lookupMatchDB = new BerkeleyDBStore()){
                lookupMatchDB.create(directoryPath, lookupMatchDBDirectory);
                if (primIDX == null) {
                    primIDX = lookupMatchDB.getEntityStore().getPrimaryIndex(Long.class, KVSequenceEntry.class);
                }

                long startPartition = System.currentTimeMillis();
                long proteinMD5Count = 0, matchCount = 0;
                try (PreparedStatement ps = connection.prepareStatement(QUERY_TABLE)) {
                    ps.setFetchSize(fetchSize);

                    try (ResultSet rs = ps.executeQuery()) {
                        long endExecuteQueryMillis = System.currentTimeMillis();
                        System.out.println(Utilities.getTimeNow() + "  " + String.valueOf((endExecuteQueryMillis - startPartition) / 1000) + " seconds to process query");
                        KVSequenceEntry match = null;

                        while (rs.next()) {
                            final String signatureLibraryName = rs.getString(SimpleLookupSite.COL_IDX_SIG_LIB_NAME);
                            if (rs.wasNull() || signatureLibraryName == null) continue;

                            SignatureLibrary signatureLibrary = SignatureLibraryLookup.lookupSignatureLibrary(signatureLibraryName);
                            if (signatureLibrary == null) continue;

                            final int locationStart = rs.getInt(SimpleLookupSite.COL_IDX_LOC_START);
                            if (rs.wasNull()) continue;

                            final int locationEnd = rs.getInt(SimpleLookupSite.COL_IDX_LOC_END);
                            if (rs.wasNull()) continue;

                            final String proteinMD5 = rs.getString(SimpleLookupSite.COL_IDX_MD5).toUpperCase();
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
                            kvMatchJoiner.add(kvValueOf(description));
                            String kvMatch = kvMatchJoiner.toString();

                            if (match == null) {
                                match = new KVSequenceEntry();
                                match.setProteinMD5(proteinMD5);
                                match.addMatch(kvMatch);
                            } else if (proteinMD5.equals(match.getProteinMD5())) {
                                match.addMatch(kvMatch);
                            } else {
                                primIDX.put(match);
                                proteinMD5Count++;
                                match = new KVSequenceEntry();
                                match.setProteinMD5(proteinMD5);
                                match.addMatch(kvMatch);
                            }
                            matchCount++;
                            if (matchCount % 1000000 == 0) {
                                lookupMatchDB.getEntityStore().sync();
                            }
                        }

                        if (match != null) {
                            primIDX.put(match);
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new IllegalStateException("SQLException thrown by IPRSCAN", e);
                    }

                  lookupMatchDB.getEntityStore().sync();

                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new IllegalStateException("SQLException thrown by IPRSCAN", e);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("SQLException thrown by IPRSCAN", e);
        }
    }

    public static String kvValueOf(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }
}
