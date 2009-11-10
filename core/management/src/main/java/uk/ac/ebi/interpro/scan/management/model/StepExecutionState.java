package uk.ac.ebi.interpro.scan.management.model;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public enum StepExecutionState {

    /**
     * When triggered, new StepInstance can be created in a block.
     * It is then the job of the Scheduler to take valid
     * StepInstance's and create
     * StepExecutions from them.
     *
     * If these StepExecutions fail, then the StepInstance can
     * be given back to the scheduler to run again.
     */
    NEW,

    /**
     * The StepExecution has been submitted for completion by the scheduler.
     * (i.e. has been placed on to the JMS job submission queue.)
     */
    SUBMITTED,

    /**
     * The StepExecution has been successfully completed.
     */
    SUCCESSFUL,

    /**
     * The StepExecution has failed.  Try again.
     */
    FAILED
}
