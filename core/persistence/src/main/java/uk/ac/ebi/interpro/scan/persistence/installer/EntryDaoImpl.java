package uk.ac.ebi.interpro.scan.persistence.installer;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO
 *
 * @author Matthew Fraser, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Repository
public class EntryDaoImpl implements EntryDao {

    private JdbcTemplate jdbcTemplate;
    private EntryRowCallbackHandler entryRowCallbackHandler;

    /**
     * Set the JDBC template. Used for Spring setter injection.
     * @param jdbcTemplate JDBC template
     */
    @Required
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Set this row callback handler - code that handles a query result. Used for Spring setter injection.
     * @param entryRowCallbackHandler The row callback handler implementation
     */
    @Required
    public void setEntryRowCallbackHandler(EntryRowCallbackHandler entryRowCallbackHandler) {
        this.entryRowCallbackHandler = entryRowCallbackHandler;
    }

    /**
     * Query for all entries in the configured database and then copy them into this I5 database.
     */
    @Transactional
    public void queryEntries()
    {
        if(jdbcTemplate!=null)
        {
            jdbcTemplate.query("select * from interpro.entry e where e.entry_ac='IPR000001'", entryRowCallbackHandler);
            //entryRowCallbackHandler.processFinalRows();
        }

    }
}
