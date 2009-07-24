package uk.ac.ebi.interpro.scan.model.transactiontracking;

import static uk.ac.ebi.interpro.scan.model.transactiontracking.TransactionState.*;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * provides the data structure to allow management of
 * match calculation transactions.  Each slice applies to a single
 * Model and indicates a range of Protein objects that are included in
 * the transaction for that Model.
 *
 * User: pjones
 * Date: 09-Jul-2009
 * Time: 15:36:40
 *
 * @author Phil Jones, EMBL-EBI
 */
@MappedSuperclass
public abstract class TransactionSlice {

    @Column (name="status", nullable = false, updatable = true)
    private TransactionState state;

    @Column (nullable = false, updatable = false)
    private Long bottom;

    @Column (nullable = false, updatable = false)
    private Long top;

    public TransactionSlice(Long bottom, Long top){
        if (top < bottom){
            throw new IllegalArgumentException("Invalid Transaction Slice Initialisation - it makes no sense for the bottom of the slice to be a higher value than the top.");
        }
        this.bottom = bottom;
        this.top = top;
    }

    protected TransactionSlice(){
        
    }

    /**
     * Returns the current status of this TransactionSlice.
     * @return the current status of this TransactionSlice.
     */
    public TransactionState getState() {
        return state;
    }

    /**
     * Returns the bottom of the TransactionSlice.  This corresponds to the
     * lowest Protein primary key in this slice.
     * @return the bottom of the TransactionSlice.
     */
    public Long getBottom() {
        return bottom;
    }

    /**
     * Returns the top of the TransactionSlice.  This corresponds to the
     * lowest Protein primary key in this slice.
     * @return the bottom of the TransactionSlice.
     */
    public Long getTop() {
        return top;
    }

    /**
     * Method called when the Transaction has been started.
     */
    public void started() {
        if (state != NOT_STARTED){
            throw new IllegalStateException ("The started method has been called on a transaction that has already started, existing state: "+ state);
        }
        this.state = RUNNING;
    }

    /**
     * Method called when the Transaction has finished successfully
     */
    public void finishedSuccessfully() {
        if (state != RUNNING){
            throw new IllegalStateException("The finishedSuccessfully method has been called on a transaction that was not running, existing state: "+ state);
        }
        this.state = FINISHED_SUCCESSFULLY;
    }

    /**
     * Method called with the Transaction has failed.
     */
    public void failed() {
        if (state != RUNNING){
            throw new IllegalStateException("The failed method has been called on a transaction that was not running, existing state: "+ state);
        }
        this.state = FAILED;
    }
}
