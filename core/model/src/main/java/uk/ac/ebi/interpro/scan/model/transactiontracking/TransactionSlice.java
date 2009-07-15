package uk.ac.ebi.interpro.scan.model.transactiontracking;

/**
 * This interface provides the data structure to allow management of
 * match calculation transactions.  Each slice applies to a single
 * Model and indicates a range of Protein objects that are included in
 * the transaction for that Model.
 *
 * At any one time the Transaction 
 *
 * User: pjones
 * Date: 09-Jul-2009
 * Time: 15:08:58
 *
 * @author Phil Jones, EMBL-EBI
 */
public interface TransactionSlice {

    /**
     * Returns the current status of this TransactionSlice.
     * @return the current status of this TransactionSlice.
     */
    public TransactionState getState();

    /**
     * Returns the bottom of the TransactionSlice.  This corresponds to the
     * lowest Protein primary key in this slice.
     * @return the bottom of the TransactionSlice.
     */
    public Long getBottom();

    /**
     * Returns the top of the TransactionSlice.  This corresponds to the
     * lowest Protein primary key in this slice.
     * @return the bottom of the TransactionSlice.
     */
    public Long getTop();

    /**
     * Method called when the Transaction has been started.
     */
    public void started();

    /**
     * Method called when the Transaction has finished successfully
     */
    public void finishedSuccessfully();

    /**
     * Method called with the Transaction has failed.
     */
    public void failed();


}
