package uk.ac.ebi.interpro.scan.business.sequence.uniparcdb;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowCallbackHandler;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoadListener;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoader;

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

    private SequenceLoader sequenceLoader;

    private SequenceLoadListener sequenceLoadListener;

    /**
     * Helper class that performs common protein
     * loading functionality
     *
     * @param sequenceLoader Helper class that performs common protein
     *                       loading functionality
     */
    @Required
    public void setProteinLoader(SequenceLoader sequenceLoader) {
        this.sequenceLoader = sequenceLoader;
    }

    @Required
    public void setSequenceLoadListener(SequenceLoadListener sequenceLoadListener) {
        this.sequenceLoadListener = sequenceLoadListener;
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
        if (sequence == null || sequence.length() == 0) {
            sequence = resultSet.getString(3);
        }
        if (sequence == null) {
            throw new IllegalArgumentException("Ugh - found a UPI with no associated sequence!");
        }
        sequenceLoader.store(sequence, null, upi);
    }

    /**
     * Call persist on the underlying proteinLoader at the end of the transaction.
     */
    public void persist() {
        sequenceLoader.persist(sequenceLoadListener, null);
    }
}
