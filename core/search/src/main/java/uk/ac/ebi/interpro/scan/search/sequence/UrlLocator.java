package uk.ac.ebi.interpro.scan.search.sequence;

/**
 * URL locator.
 *
 * @author  Antony Quinn
 * @version $Id$
 */
public interface UrlLocator {

    public String locateUrl(String query);

    // DBML implementation:

//    /**
//     * Returns URL of sequence match: either a pre-calculated InterPro protein page, or an InterProScan result page.
//     *
//     * @param sequence Protein sequence
//     * @return URL of sequence match
//     */
//    @Override public String locateUrl(String sequence) {
//
//        // UniParc database ID for SwissProt
//        private static final int DBID_SWISSPROT = 2;
//
//        // Get MD5 -- convert to upper-case because UniParc stores MD5 in upper-case
//        String md5 = SequenceHelper.calculateMd5(sequence).toUpperCase();
//
//        DatabaseConnection connection;
//
//        // Get protein accession
//        String ac = null;
//        if (connection != null) {
//            try {
//                ResultSet rs = connection.getSQL("md2pro").bind("md5", md5).query();
//                while (rs.next()) {
//                    ac = rs.getString("ac");
//                    // Exit loop if SwissProt -- we prefer to show SwissProt IDs instead of TrEMBL IDs
//                    if (rs.getInt("dbid") == DBID_SWISSPROT) {
//                        break;
//                    }
//                }
//            }
//            catch (SQLException e) {
//                // TODO: Use more specific exception
//                throw new RuntimeException("Could not find protein accession for MD5: " + md5, e);
//            }
//        }
//
//        // Return URL of protein page
//        // TODO: What if this is a new protein that's not been characterised in InterPro? => Check release cycles
//        if (ac != null) {
//            return "IProtein?ac=" + ac;
//        }
//
//    }

    // To run from main method, need database connection:
//    String configFile = "dev-config.xml";
//    if (System.getProperty("os.name").startsWith("Windows")) {
//        configFile = "dev-config-windows.xml";
//    }
//    InterProWeb.InterProWebConfig cfg = new InterProWeb.InterProWebConfig(configFile, null);
//    DatabaseConnection connection = cfg.databaseManager.getConnection(cfg.features.getFeature(null, Features.Name.DATABASE_UNIPARC));

}