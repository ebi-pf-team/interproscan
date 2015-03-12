package uk.ac.ebi.interpro.scan.management.model;

import java.io.Serializable;

/**
 * This enum captures all of the possible states that a
 * StepInstance or a StepExecution may be in.
 * (Valid transitions between StepExecutionStates
 * are constrained in StepExecution.)
 *
 * @author Phil Jones
 * @version $Id$
 * @see StepInstance
 * @see StepExecution
 * @since 1.0-SNAPSHOT
 */
public enum StepExecutionState implements Serializable {

    /**
     * Condition where a new StepInstance has been created,
     * but new StepExecutions have been created.
     */
    NEW_STEP_INSTANCE,

    /**
     * When triggered, new StepInstance can be created in a block.
     * It is then the job of the Scheduler to take valid
     * StepInstance's and create
     * StepExecutions from them.
     * <p/>
     * If these StepExecutions fail, then the StepInstance can
     * be given back to the scheduler to run again.
     */
    NEW_STEP_EXECUTION,

    /**
     * The StepExecution has been submitted for completion by the scheduler.
     * (i.e. has been placed on to the JMS job submission queue.)
     */
    STEP_EXECUTION_SUBMITTED,

    /**
     * The StepExecution is now running.
     */
    STEP_EXECUTION_RUNNING,

    /**
     * The StepExecution has been successfully completed.
     */
    STEP_EXECUTION_SUCCESSFUL,

    /**
     * The StepExecution has failed.  Try again.
     */
    STEP_EXECUTION_FAILED


}
