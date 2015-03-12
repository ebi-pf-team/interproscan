package uk.ac.ebi.interpro.scan.persistence.installer;

/**
 * Represents the data access object interface for a Entry.
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface JdbcEntryDao {
    /**
     * Loads entries and entry mappings from an InterPro database and stores them into I5 database.
     */
    void loadEntriesAndMappings(Long releaseId);

    /**
     * Returns the latest InterPro database version.
     */
    String getLatestDatabaseReleaseVersion();

    /**
     * Just used to test the connection to the database.
     *
     * @return true if OK.
     */
    boolean testConnection();
}
