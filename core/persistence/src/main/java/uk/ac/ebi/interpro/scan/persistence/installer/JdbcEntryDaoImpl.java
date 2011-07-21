package uk.ac.ebi.interpro.scan.persistence.installer;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Repository
public class JdbcEntryDaoImpl implements JdbcEntryDao {

    private JdbcTemplate jdbcTemplate;
    private EntryRowCallbackHandler entryRowCallbackHandler;

    /**
     * Set the JDBC template. Used for Spring setter injection.
     *
     * @param jdbcTemplate JDBC template
     */
    @Required
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Set this row callback handler - code that handles a query result. Used for Spring setter injection.
     *
     * @param entryRowCallbackHandler The row callback handler implementation
     */
    @Required
    public void setEntryRowCallbackHandler(EntryRowCallbackHandler entryRowCallbackHandler) {
        this.entryRowCallbackHandler = entryRowCallbackHandler;
    }

    /**
     * Loads entries and entry mappings from an InterPro database and stores them into H2 database.
     * TODO: Release ID thing needs to be discussed, because there isn't any release id associated to the InterPro entry.
     */
    public void loadEntriesAndMappings() {
        if (jdbcTemplate != null) {
            String releaseVersion = getLatestDatabaseReleaseVersion();
            entryRowCallbackHandler.setReleaseVersion(releaseVersion);
            jdbcTemplate.query("select * from interpro.entry e where e.checked='Y'", entryRowCallbackHandler);
            entryRowCallbackHandler.processFinalRows();
        }
    }

    public String getLatestDatabaseReleaseVersion() {
        if (jdbcTemplate != null) {
            return jdbcTemplate.queryForObject("select v.version from INTERPRO.db_version v where v.dbcode='I'", String.class);
        }
        return null;
    }
}
