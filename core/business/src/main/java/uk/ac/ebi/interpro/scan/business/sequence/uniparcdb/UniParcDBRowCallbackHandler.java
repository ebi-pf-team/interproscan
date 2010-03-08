package uk.ac.ebi.interpro.scan.business.sequence.uniparcdb;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowCallbackHandler;
import uk.ac.ebi.interpro.scan.business.sequence.ProteinLoader;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RowCallbackHandler for one row of data from the UniParc database, containing
 * a UPI and a sequence.  Uses the ProteinLoader object to insert this
 * into the database, creating StepInstances as required.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0
 */
public class UniParcDBRowCallbackHandler implements RowCallbackHandler {

    private static final Logger LOGGER = Logger.getLogger(UniParcDBRowCallbackHandler.class);

    private ProteinLoader proteinLoader;

    private int counter = 0;

    /**
     * Helper class that performs common protein
     * loading functionality
     *
     * @param proteinLoader Helper class that performs common protein
     *                      loading functionality
     */
    @Required
    public void setProteinLoader(ProteinLoader proteinLoader) {
        this.proteinLoader = proteinLoader;
    }

    /**
     * Processes one row returned from:
     * select upi, seq_short, seq_long from uniparc.protein and upi > ? and rownum <= ? order by upi ASC
     *
     * @param resultSet
     * @throws SQLException
     */
    @Override
    public void processRow(ResultSet resultSet) throws SQLException {
        String upi = resultSet.getString(1);
        String sequence = resultSet.getString(2);
        if (sequence == null || sequence.length() == 0){
            sequence = resultSet.getString(3);
        }
        if (sequence == null) {
            throw new IllegalArgumentException ("Ugh - found a UPI with no associated sequence!");
        }
        if (LOGGER.isDebugEnabled()){
            if (counter++ % 2000 == 0){
                LOGGER.debug("Storing " + upi + " with sequence length "+ sequence.length());
            }
        }
        proteinLoader.store(sequence, upi);
    }

    /**
     * Call persist on the underlying proteinLoader at the end of the transaction.
     */
    public void persist(){
        proteinLoader.persist();
    }
}
