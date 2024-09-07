package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.precalc.berkeley.conversion.toi5.SignatureLibraryLookup;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.KVSequenceEntry;
import uk.ac.ebi.interpro.scan.precalc.berkeley.model.SimpleLookupSite;
import uk.ac.ebi.interpro.scan.util.Utilities;
import java.io.File;
import java.sql.*;
import java.util.StringJoiner;


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
    private static final String USER = "IPRSCAN";
    private static final String QUERY =
            "SELECT MD5, SIGNATURE_LIBRARY_NAME, SIGNATURE_LIBRARY_RELEASE, SIGNATURE_ACCESSION, " +
                    "LOC_START, LOC_END, NUM_SITES, RESIDUE, RESIDUE_START, RESIDUE_END, DESCRIPTION " +
                    "FROM " + USER + ".LOOKUP_SITE ORDER BY MD5";

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
            storeConfig.setDeferredWrite(true);
            store = new EntityStore(env, "EntityStore", storeConfig);

            PrimaryIndex<String, KVSequenceEntry> index = store.getPrimaryIndex(String.class, KVSequenceEntry.class);
            KVSequenceEntry match = null;
            try (PreparedStatement ps = connection.prepareStatement(QUERY)) {
                ps.setFetchSize(fetchSize);

                try (ResultSet rs = ps.executeQuery()) {
                    int proteinCount = 0;
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
                            index.put(match);
                            match = new KVSequenceEntry();
                            match.setProteinMD5(proteinMD5);
                            match.addMatch(kvMatch);

                            proteinCount++;
                            if (proteinCount % 100000 == 0) {
                                store.sync();

                                if (proteinCount % 10000000 == 0) {
                                    System.out.println(Utilities.getTimeAlt() + ": " + String.format("%,d", proteinCount) + " sequences processed");
                                }
                            }
                        }
                    }

                    System.out.println(Utilities.getTimeAlt() + ": " + String.format("%,d", proteinCount) + " sequences processed");
                }
            }

            if (match != null) {
                index.put(match);
            }

            store.sync();
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

    public static String kvValueOf(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }
}
