package uk.ac.ebi.interpro.scan.business.sequence.uniparcdb;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.persistence.ProteinXrefDAO;

/**
 * Implementation of interface to load proteins
 * from UniParc.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0
 */
public class LoadUniParcFromDBImpl implements LoadUniParcFromDB {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named "PeptideQuery".
     */
    private static final Logger LOGGER = LogManager.getLogger(LoadUniParcFromDBImpl.class.getName());

    /**
     * Spring documentation claims that this object is thread safe (contains no state -
     * singleton) so can be used in a multi-threaded environment.
     */
    private JdbcTemplate jdbcTemplate;

    private UniParcDBRowCallbackHandler rowCallbackHandlerTemplate;

    private ProteinXrefDAO proteinXrefDao;

    private Integer maximumProteins;

    @Required
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Required
    public void setRowCallbackHandlerTemplate(UniParcDBRowCallbackHandler rowCallbackHandlerTemplate) {
        this.rowCallbackHandlerTemplate = rowCallbackHandlerTemplate;
    }

    @Required
    public void setXrefDao(ProteinXrefDAO proteinXrefDao) {
        this.proteinXrefDao = proteinXrefDao;
    }

    /**
     * Sets the maximum number of proteins loaded
     * from UniParc in a single transaction.
     *
     * @param maximumProteins the maximum number of proteins loaded
     *                        from UniParc in a single transaction.
     */
    @Required
    public void setMaximumProteins(Integer maximumProteins) {
        this.maximumProteins = maximumProteins;
    }

    /**
     * Check for new protein sequences in the UniParc
     * database and load them (up to a maximum number)
     */
    @Transactional
    public void loadNewSequences() {
        // Get the 'high water mark' UPI in I5
        // TODO - change table name after testing
        String highWaterMark = proteinXrefDao.getMaxUniparcId();
        LOGGER.debug("The high water mark UPI is " + highWaterMark);
        /*
         The query below has to be written as a select within a select
         in order for the upis to be returned sequentially
         */

        final String sql = "select * from ( " +
                           "select  upi, seq_short, seq_long " +
                           "from uniparc.protein " +
                           "where upi > ? " +
                           "order by upi ASC ) " +
                           "where rownum <= ? ";
        jdbcTemplate.query(sql, new Object[]{highWaterMark, maximumProteins}, rowCallbackHandlerTemplate);
        rowCallbackHandlerTemplate.persist();
    }


}
