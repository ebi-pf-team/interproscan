package uk.ac.ebi.interpro.scan.persistence.transactiontracking;

import uk.ac.ebi.interpro.scan.model.transactiontracking.TransactionSlice;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;

import java.util.List;

/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 10-Jul-2009
 * Time: 11:03:51
 *
 * @author Phil Jones, EMBL-EBI
 */
public interface RawTransactionSliceDAO extends GenericDAO<TransactionSlice, Long> {

    /**
     * This method returns a List of TransactionSlice objects that meet the criteria specified.
     * These TransactionSlices will all have TransactionState.NOT_STARTED.
     *
     * These can then be passed on to run...
     *
     * @param criteria describing the criteria for selection.
     * @return a List containing the transactions to run.
     */
    public List<TransactionSlice> selectNewSlices(SliceSelectionCriteria criteria);
}
