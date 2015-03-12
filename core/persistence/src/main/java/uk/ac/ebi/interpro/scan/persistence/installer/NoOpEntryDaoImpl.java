package uk.ac.ebi.interpro.scan.persistence.installer;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 * Date: 30/07/12
 * Time: 10:00
 * To change this template use File | Settings | File Templates.
 */
public class NoOpEntryDaoImpl implements JdbcEntryDao {
    /**
     * Loads entries and entry mappings from an InterPro database and stores them into I5 database.
     */
    public void loadEntriesAndMappings(Long releaseId) {
        //No-op
    }

    /**
     * Returns the latest InterPro database version.
     */
    public String getLatestDatabaseReleaseVersion() {
        return null;  // No-op
    }

    public boolean testConnection() {
        return true;
    }
}
