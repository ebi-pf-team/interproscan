package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.SignatureLibraryLookup;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntry;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupSite;
import uk.ac.ebi.interpro.scan.util.Utilities;
import java.io.File;
import java.sql.*;
import java.util.*;


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
public class CreateSitesDBFromOracle {
    private static final String USER = "IPRSCAN";
    private static final String QUERY =
            "SELECT MD5, SIGNATURE_LIBRARY_NAME, SIGNATURE_LIBRARY_RELEASE, SIGNATURE_ACCESSION, " +
                    "LOC_START, LOC_END, NUM_SITES, RESIDUE, RESIDUE_START, RESIDUE_END, DESCRIPTION " +
                    "FROM " + USER + ".LOOKUP_SITE PARTITION (?)";

    void buildDatabase(String url, String password, int fetchSize, File outputDirectory, boolean verbose) {
        System.err.println(Utilities.getTimeAlt() + ": starting");

        try (BerkeleyDBJE bdbje = new BerkeleyDBJE(outputDirectory)) {
            EntityStore store = bdbje.getStore();
            PrimaryIndex<String, KVSequenceEntry> index = store.getPrimaryIndex(String.class, KVSequenceEntry.class);

            try (Connection connection = DriverManager.getConnection(url, USER, password)) {
                List<String> partitions = getPartitions(connection);

                int proteinCount = 0;
                int milestone = 10_000_000;
                int step = 10_000_000;
                int partitionDone = 0;
                Map<String, KVSequenceEntry> entries = new HashMap<>();

                for (int i = 0; i < partitions.size(); i++) {
                    String partition = partitions.get(i);
                    String query = QUERY.replace("?", partition);

                    try (PreparedStatement ps = connection.prepareStatement(query)) {
                        ps.setFetchSize(fetchSize);

                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                final String signatureLibraryName = rs.getString(SimpleLookupSite.COL_IDX_SIG_LIB_NAME);
                                if (rs.wasNull() || signatureLibraryName == null) continue;

                                SignatureLibrary signatureLibrary = SignatureLibraryLookup.lookupSignatureLibrary(signatureLibraryName);
                                if (signatureLibrary == null) continue;

                                final int locationStart = rs.getInt(SimpleLookupSite.COL_IDX_LOC_START);
                                if (rs.wasNull()) continue;

                                final int locationEnd = rs.getInt(SimpleLookupSite.COL_IDX_LOC_END);
                                if (rs.wasNull()) continue;

                                final String proteinMD5 = rs.getString(SimpleLookupSite.COL_IDX_MD5);
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

                                KVSequenceEntry entry = entries.computeIfAbsent(proteinMD5, k -> {
                                    KVSequenceEntry newEntry = new KVSequenceEntry();
                                    newEntry.setProteinMD5(k);
                                    return newEntry;
                                });
                                entry.addMatch(kvMatch);
                            }
                        }
                    }

                    List<String> keys = new ArrayList<>(entries.keySet());
                    Collections.sort(keys);
                    for (String key: keys) {
                        index.put(entries.get(key));
                        proteinCount++;
                    }

                    store.sync();
                    entries.clear();
                    partitionDone++;

                    if (verbose || i + 1 == partitions.size() || proteinCount >= milestone) {
                        String msg = String.format("%s: %,d proteins processed (%d/%d)",
                                Utilities.getTimeAlt(),
                                proteinCount,
                                partitionDone,
                                partitions.size());
                        System.err.println(msg);
                        milestone += step;
                    }
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Unable to connect to the database", e);
            }
        }
    }

    public static String kvValueOf(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

    private static List<String> getPartitions(Connection connection) throws SQLException {
        List<String> partitions = new ArrayList<>();
        String query = "SELECT PARTITION_NAME FROM ALL_TAB_PARTITIONS WHERE TABLE_NAME = 'LOOKUP_SITE' ORDER BY PARTITION_NAME";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String partition = rs.getString(1);
                    partitions.add(partition);
                }
            }
        }
        return partitions;
    }
}
