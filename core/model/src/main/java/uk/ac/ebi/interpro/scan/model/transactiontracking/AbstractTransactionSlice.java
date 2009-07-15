package uk.ac.ebi.interpro.scan.model.transactiontracking;

import static uk.ac.ebi.interpro.scan.model.transactiontracking.TransactionState.*;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 09-Jul-2009
 * Time: 15:36:40
 *
 * @author Phil Jones, EMBL-EBI
 */
@MappedSuperclass
public abstract class AbstractTransactionSlice implements TransactionSlice {

    @Column (name="status", nullable = false, updatable = true)
    private TransactionState state;

    @Column (nullable = false, updatable = false)
    private Long bottom;

    @Column (nullable = false, updatable = false)
    private Long top;

    public AbstractTransactionSlice(Long bottom, Long top){
        if (top < bottom){
            throw new IllegalArgumentException("Invalid Transaction Slice Initialisation - it makes no sense for the bottom of the slice to be a higher value than the top.");
        }
        this.bottom = bottom;
        this.top = top;
    }

    protected AbstractTransactionSlice(){
        
    }

    public TransactionState getState() {
        return state;
    }

    public Long getBottom() {
        return bottom;
    }

    public Long getTop() {
        return top;
    }

    public void started() {
        if (state != NOT_STARTED){
            throw new IllegalStateException ("The started method has been called on a transaction that has already started, existing state: "+ state);
        }
        this.state = RUNNING;
    }

    public void finishedSuccessfully() {
        if (state != RUNNING){
            throw new IllegalStateException("The finishedSuccessfully method has been called on a transaction that was not running, existing state: "+ state);
        }
        this.state = FINISHED_SUCCESSFULLY;
    }

    public void failed() {
        if (state != RUNNING){
            throw new IllegalStateException("The failed method has been called on a transaction that was not running, existing state: "+ state);
        }
        this.state = FAILED;
    }
}
