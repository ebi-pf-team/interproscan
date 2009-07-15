package uk.ac.ebi.interpro.scan.model.transactiontracking;

/**
 * This enum documents the possible states of an analysis transaction.
 *
 * User: pjones
 * Date: 09-Jul-2009
 * Time: 15:09:23
 *
 * @author Phil Jones, EMBL-EBI
 */
public enum TransactionState {

    // TODO - Consider whether these states can / should be renamed / modified /
    // removed in lieue of corresponding states in Spring Batch.

    // TODO - Alternatively look at whether the State pattern can be applied,
    // however this is probably not applicable as behaviour of this model
    // does not change with state...?

    /**
     * The transaction has just been created, but not yet submitted.
     */
    NOT_STARTED,

    /**
     * The transaction is currently running.
     */
    RUNNING,

    /**
     * The transaction has succesfully completed.
     */
    FINISHED_SUCCESSFULLY,

    /**
     * The transaction has failed.  Needs to be re-run in the future. Failure must be logged / reported.
     */
    FAILED

}
