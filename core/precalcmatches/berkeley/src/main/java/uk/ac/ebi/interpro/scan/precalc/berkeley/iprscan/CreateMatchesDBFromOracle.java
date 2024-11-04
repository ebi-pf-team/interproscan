package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.je.Environment;
import com.sleepycat.persist.StoreConfig;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.SignatureLibraryLookup;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntry;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupMatch;
import uk.ac.ebi.interpro.scan.util.Utilities;
import java.io.File;
import java.sql.*;
import java.util.*;


/**
 * Creates a Berkeley database of proteins for which matches have been calculated in IPRSCAN.
 *
 * @author Phil Jones
 * @author Maxim Scheremetjew
 * @author Matthias Blum
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class CreateMatchesDBFromOracle {
    private static final String USER = "IPRSCAN";
    private static final String QUERY =
            "SELECT MD5, SIGNATURE_LIBRARY_NAME, SIGNATURE_LIBRARY_RELEASE, SIGNATURE_ACCESSION, " +
                    "MODEL_ACCESSION, SEQ_START, SEQ_END, FRAGMENTS, SEQUENCE_SCORE, SEQUENCE_EVALUE, " +
                    "HMM_BOUNDS, HMM_START, HMM_END, HMM_LENGTH, ENVELOPE_START, ENVELOPE_END, SCORE, " +
                    "EVALUE, SEQ_FEATURE " +
                    "FROM " + USER + ".LOOKUP_MATCH PARTITION (?)";

    void buildDatabase(String url, String password, int fetchSize, File outputDirectory) {
        System.out.println(Utilities.getTimeAlt() + ": starting");

        Environment env = null;
        EntityStore store = null;

        try (Connection connection = DriverManager.getConnection(url, USER, password)) {
            List<String> partitions = getPartitions(connection);

            // Set up the environment
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);
            envConfig.setTransactional(false);
            env = new Environment(outputDirectory, envConfig);

            // Set up the entity store
            StoreConfig storeConfig = new StoreConfig();
            storeConfig.setAllowCreate(true);
            storeConfig.setTransactional(false);
            storeConfig.setDeferredWrite(true);
            store = new EntityStore(env, "EntityStore", storeConfig);

            PrimaryIndex<String, KVSequenceEntry> index = store.getPrimaryIndex(String.class, KVSequenceEntry.class);

            long proteinCount = 0;
            int partitionDone = 0;
            Map<String, KVSequenceEntry> matches = new HashMap<>();
            for (String partition: partitions) {
                String query = QUERY.replace("?", partition);

                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setFetchSize(fetchSize);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            final String signatureLibraryName = rs.getString(SimpleLookupMatch.COL_IDX_SIG_LIB_NAME);
                            if (rs.wasNull() || signatureLibraryName == null) continue;

                            SignatureLibrary signatureLibrary = SignatureLibraryLookup.lookupSignatureLibrary(signatureLibraryName);
                            if (signatureLibrary == null) continue;

                            final int sequenceStart = rs.getInt(SimpleLookupMatch.COL_IDX_SEQ_START);
                            if (rs.wasNull()) continue;

                            final int sequenceEnd = rs.getInt(SimpleLookupMatch.COL_IDX_SEQ_END);
                            if (rs.wasNull()) continue;

                            final String proteinMD5 = rs.getString(SimpleLookupMatch.COL_IDX_MD5);
                            if (proteinMD5 == null || proteinMD5.length() == 0) continue;

                            final String sigLibRelease = rs.getString(SimpleLookupMatch.COL_IDX_SIG_LIB_RELEASE);
                            if (sigLibRelease == null || sigLibRelease.length() == 0) continue;

                            final String signatureAccession = rs.getString(SimpleLookupMatch.COL_IDX_SIG_ACCESSION);
                            if (signatureAccession == null || signatureAccession.length() == 0) continue;

                            final String modelAccession = rs.getString(SimpleLookupMatch.COL_IDX_MODEL_ACCESSION);
                            if (modelAccession == null || modelAccession.length() == 0) continue;

                            Integer hmmStart = rs.getInt(SimpleLookupMatch.COL_IDX_HMM_START);
                            if (rs.wasNull()) hmmStart = null;

                            Integer hmmEnd = rs.getInt(SimpleLookupMatch.COL_IDX_HMM_END);
                            if (rs.wasNull()) hmmEnd = null;

                            Integer hmmLength = rs.getInt(SimpleLookupMatch.COL_IDX_HMM_LENGTH);
                            if (rs.wasNull()) hmmLength = null;

                            String hmmBounds = rs.getString(SimpleLookupMatch.COL_IDX_HMM_BOUNDS);

                            Double sequenceScore = rs.getDouble(SimpleLookupMatch.COL_IDX_SEQ_SCORE);
                            if (rs.wasNull()) sequenceScore = null;

                            Double sequenceEValue = rs.getDouble(SimpleLookupMatch.COL_IDX_SEQ_EVALUE);
                            if (rs.wasNull()) sequenceEValue = null;

                            Double locationScore = rs.getDouble(SimpleLookupMatch.COL_IDX_LOC_SCORE);
                            if (rs.wasNull()) locationScore = null;

                            Double locationEValue = rs.getDouble(SimpleLookupMatch.COL_IDX_LOC_EVALUE);
                            if (rs.wasNull()) locationEValue = null;

                            Integer envelopeStart = rs.getInt(SimpleLookupMatch.COL_IDX_ENV_START);
                            if (rs.wasNull()) envelopeStart = null;

                            Integer envelopeEnd = rs.getInt(SimpleLookupMatch.COL_IDX_ENV_END);
                            if (rs.wasNull()) envelopeEnd = null;

                            /*
                                Holds the CIGAR alignment from HAMAP and PROSITE
                                Holds the Graphscan value for PRINTS
                                Holds the ancestral node ID for PANTHER
                             */
                            String seqFeature = rs.getString(SimpleLookupMatch.COL_IDX_SEQ_FEATURE);

                            String fragments = rs.getString(SimpleLookupMatch.COL_IDX_FRAGMENTS);
                            fragments = fragments.replace(",", ";");

                            String columnDelimiter = ",";
                            StringJoiner kvMatchJoiner = new StringJoiner(columnDelimiter);
                            kvMatchJoiner.add(signatureLibraryName);
                            kvMatchJoiner.add(sigLibRelease);
                            kvMatchJoiner.add(signatureAccession);
                            kvMatchJoiner.add(modelAccession);
                            kvMatchJoiner.add(kvValueOf(sequenceStart));
                            kvMatchJoiner.add(kvValueOf(sequenceEnd));
                            kvMatchJoiner.add(fragments);
                            kvMatchJoiner.add(kvValueOf(sequenceScore));
                            kvMatchJoiner.add(kvValueOf(sequenceEValue));
                            kvMatchJoiner.add(kvValueOf(hmmBounds));
                            kvMatchJoiner.add(kvValueOf(hmmStart));
                            kvMatchJoiner.add(kvValueOf(hmmEnd));
                            kvMatchJoiner.add(kvValueOf(hmmLength));
                            kvMatchJoiner.add(kvValueOf(envelopeStart));
                            kvMatchJoiner.add(kvValueOf(envelopeEnd));
                            kvMatchJoiner.add(kvValueOf(locationScore));
                            kvMatchJoiner.add(kvValueOf(locationEValue));
                            kvMatchJoiner.add(kvValueOf(seqFeature));
                            String kvMatch = kvMatchJoiner.toString();

                            KVSequenceEntry entry = matches.computeIfAbsent(proteinMD5, k -> {
                                KVSequenceEntry newEntry = new KVSequenceEntry();
                                newEntry.setProteinMD5(k);
                                return newEntry;
                            });
                            entry.addMatch(kvMatch);
                        }
                    }
                }

                for (KVSequenceEntry entry: matches.values()) {
                    index.put(entry);
                    proteinCount++;
                }

                store.sync();
                matches.clear();
                partitionDone++;

                String msg = String.format("%s: %,d proteins processed (%d/%d)",
                        Utilities.getTimeAlt(),
                        proteinCount,
                        partitionDone,
                        partitions.size());
                System.out.println(msg);
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
    }

    private static String kvValueOf(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

    private static List<String> getPartitions(Connection connection) throws SQLException {
        List<String> partitions = new ArrayList<>();
        String query = "SELECT PARTITION_NAME FROM ALL_TAB_PARTITIONS WHERE TABLE_NAME = 'LOOKUP_MATCH' ORDER BY PARTITION_NAME";
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
