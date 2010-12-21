package uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A;

import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class UniParcMD5LookupCallbackHandler implements RowCallbackHandler {
    /**
     * Implementations must implement this method to process each row of data
     * in the ResultSet. This method should not call <code>next()</code> on
     * the ResultSet; it is only supposed to extract values of the current row.
     * <p>Exactly what the implementation chooses to do is up to it:
     * A trivial implementation might simply count rows, while another
     * implementation might build an XML document.
     *
     * @param rs the ResultSet to process (pre-initialized for the current row)
     * @throws java.sql.SQLException if a SQLException is encountered getting
     *                               column values (that is, there's no need to catch SQLException)
     */
    @Override
    public void processRow(ResultSet rs) throws SQLException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
