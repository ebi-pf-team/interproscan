package uk.ac.ebi.interpro.scan.precalc.berkeley.iprscan;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
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

    void buildDatabase(String url, String password, int fetchSize, File outputDirectory, int maxProteins) {
        System.err.println(Utilities.getTimeAlt() + ": starting");
        int proteinCount = 0;
        try (BerkeleyDBJE bdbje = new BerkeleyDBJE(outputDirectory)) {
            EntityStore store = bdbje.getStore();
            PrimaryIndex<String, BerkeleyConsideredProtein> index = store.getPrimaryIndex(String.class, BerkeleyConsideredProtein.class);

            try (Connection connection = DriverManager.getConnection(url, USER, password)) {
                try (PreparedStatement ps = connection.prepareStatement(QUERY)) {
                    ps.setFetchSize(fetchSize);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            final String proteinMD5 = rs.getString(1);
                            if (proteinMD5 == null || proteinMD5.length() == 0) continue;

                            BerkeleyConsideredProtein protein = new BerkeleyConsideredProtein(proteinMD5);
                            index.put(protein);

                            proteinCount++;
                            if (proteinCount == maxProteins) {
                                break
                            } else if (proteinCount % 10000000 == 0) {
                                store.sync();

                                String msg = String.format("%s: %,d proteins processed", Utilities.getTimeAlt(), proteinCount);
                                System.err.println(msg);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Unable to connect to the database", e);
            }
        }

        String msg = String.format("%s: %,d proteins processed", Utilities.getTimeAlt(), proteinCount);
        System.err.println(msg);
    }
}
