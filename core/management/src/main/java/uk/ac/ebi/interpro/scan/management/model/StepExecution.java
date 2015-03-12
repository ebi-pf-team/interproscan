package uk.ac.ebi.interpro.scan.management.model;

import org.apache.log4j.Logger;
import org.hibernate.annotations.IndexColumn;
import uk.ac.ebi.interpro.scan.management.dao.StepExecutionDAO;
import uk.ac.ebi.interpro.scan.model.Chunker;
import uk.ac.ebi.interpro.scan.model.ChunkerSingleton;
import uk.ac.ebi.interpro.scan.model.KeyGen;

import javax.persistence.*;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

/**
 * Abstract class for executing a StepInstance.
 * <p/>
 * This class has two functions - firstly to control StepExecution
 * and allow the Master to determine if a StepInstance is currently
 * running, has failed or has been completed.
 * <p/>
 * Secondly it stores auditing information about the execution in the
 * database, allowing (for example) a user to determine the cost
 * of specific pieces of processing.
 * <p/>
 * All things being equals, this class should be FINAL, but can't
 * be because of JPA.  (So don't subclass!)
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
@Table(name = "step_execution")
public class StepExecution implements Serializable, Comparable<StepExecution> {

    protected static final Logger LOGGER = Logger.getLogger(StepExecution.class.getName());

    @Transient
    private static final Chunker CHUNKER = ChunkerSingleton.getInstance();


    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "STEP_EXE_IDGEN")
    @TableGenerator(name = "STEP_EXE_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "step_execution", initialValue = 0, allocationSize = 50)
    private Long id;

    @ManyToOne(targetEntity = StepInstance.class, cascade = {}, optional = false)
    protected StepInstance stepInstance;

    @Enumerated(javax.persistence.EnumType.STRING)
    private StepExecutionState state = StepExecutionState.NEW_STEP_EXECUTION;

    @Column(nullable = true, name = "time_created")
    private Date createdTime;

    @Column(nullable = true, name = "time_started_running")
    private Date startedRunningTime;

    @Column(nullable = true, name = "time_submitted")
    private Date submittedTime;

    @Column(nullable = true, name = "time_completed")
    private Date completedTime;

    @Column(nullable = true, name = "proportion_completed")
    private Double proportionCompleted;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "exception_chunk")
    @IndexColumn(name = "chunk_index")
    @Column(length = Chunker.CHUNK_SIZE, nullable = true)
    private List<String> exceptionChunks;

    @Column(nullable = true, updatable = true, length = Chunker.CHUNK_SIZE)
    private String exceptionFirstChunk;

    @Transient
    private String exception;

    protected StepExecution(StepInstance stepInstance) {
        this.stepInstance = stepInstance;
        this.stepInstance.addStepExecution(this);
        createdTime = new Date();
    }

    /**
     * Don't use! Only here because required by JPA.
     */
    protected StepExecution() {
    }

    public String getException() {
        if (exception == null) {
            exception = CHUNKER.concatenate(exceptionFirstChunk, exceptionChunks);
        }
        return exception;
    }

    // Private for Hibernate (see http://www.javalobby.org/java/forums/t49288.html)

    private void setException(String exception) {
        this.exception = exception;
        List<String> chunks = CHUNKER.chunkIntoList(exception);
        this.exceptionFirstChunk = CHUNKER.firstChunk(chunks);
        this.exceptionChunks = CHUNKER.latterChunks(chunks);
    }

    public void setStepInstance(StepInstance stepInstance) {
        this.stepInstance = stepInstance;
    }

    public void setState(StepExecutionState state) {
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public StepInstance getStepInstance() {
        return stepInstance;
    }

    public StepExecutionState getState() {
        return state;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public Date getSubmittedTime() {
        return submittedTime;
    }

    public Date getStartedRunningTime() {
        return startedRunningTime;
    }

    public Date getCompletedTime() {
        return completedTime;
    }

    public Double getProportionCompleted() {
        return proportionCompleted;
    }

    /**
     * If this method is called, the proportion complete is set and
     * all listeners to this StepExecution are informed that the
     * state has changed.
     *
     * @param proportionCompleted between 0.0d and 1.0d to indicate
     *                            how much of the job has been completed.
     */
    protected void setProportionCompleted(Double proportionCompleted) {
        this.proportionCompleted = proportionCompleted;
    }

    /**
     * This method is called to execute the action that the StepExecution must perform.
     * This method should typically perform its activity in a try / catch / finally block
     * that sets the state of the step execution appropriately.
     * <p/>
     * Note that the implementation DOES have access to the protected stepInstance,
     * and from their to the protected Step, to allow it to access parameters for execution.
     */
//    public abstract void execute(DAOManager daoManager);
    public void submit(StepExecutionDAO stepExecutionDAO) {
        if (state != StepExecutionState.NEW_STEP_EXECUTION) {
            throw new IllegalStateException("Attempting to submit a StepExecution to a queue, which is not in state 'NEW_STEP_EXECUTION'.");
        }
        state = StepExecutionState.STEP_EXECUTION_SUBMITTED;
        submittedTime = new Date();
        stepExecutionDAO.update(this);
    }

    public void setToRun() {
        if (state == StepExecutionState.STEP_EXECUTION_SUCCESSFUL || state == StepExecutionState.STEP_EXECUTION_FAILED) {
            throw new IllegalStateException("Attempting to set the state of this stepExecution to 'RUNNING', however it has already been completed.");
        }
        state = StepExecutionState.STEP_EXECUTION_RUNNING;
        startedRunningTime = new Date();
    }

    /**
     * Called by the execute() method implementation to indicate successful completion.
     */
    public void completeSuccessfully() {
        if (state == StepExecutionState.STEP_EXECUTION_FAILED) {
            throw new IllegalStateException("Try to set the state of this StepExecution to 'STEP_EXECUTION_SUCCESSFUL', however has previously been set to 'FAILED'.");
        }
        state = StepExecutionState.STEP_EXECUTION_SUCCESSFUL;
        completedTime = new Date();
    }

    /**
     * Called by the execute() method implementation to indicate a failure of execution.
     * Logs any Exceptions thrown (StackTrace) to be returned to the Master for recording (and action, if necessary).
     *
     * @param throwable if a Throwable (e.g. Exception) has been thrown during execution.  Stores and returns the
     *                  stack trace.
     */
    public void fail(Throwable throwable) {
        if (throwable != null) {
            PrintWriter pw = null;
            try {
                StringWriter sw = new StringWriter();
                pw = new PrintWriter(new StringWriter());
                throwable.printStackTrace(pw);
                pw.flush();
                this.setException(sw.toString());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Exception StackTrace recorded by the failed StepExecution, to be returned to the Master and stored: " + this.getException());
                }
            } finally {
                if (pw != null) {
                    pw.close();
                }
            }
        }
        state = StepExecutionState.STEP_EXECUTION_FAILED;
        completedTime = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StepExecution)) return false;

        StepExecution that = (StepExecution) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        } else {
            return super.hashCode();
        }
    }

    @Override
    public String toString() {
        return "StepExecution{" +
                "id='" + id + '\'' +
                ", stepInstance=" + stepInstance +
                ", state=" + state +
                ", createdTime=" + createdTime +
                ", startedRunningTime=" + startedRunningTime +
                ", submittedTime=" + submittedTime +
                ", completedTime=" + completedTime +
                ", proportionCompleted=" + proportionCompleted +
                '}';
    }

    /**
     * Updates the state of this StepExecution based upon the state
     * of the freshStepExecution that has been returned from the
     * worker process.
     *
     * @param freshStepExecution being the StepExecution that has been serialized back to the Master.
     */
    public void refresh(StepExecution freshStepExecution) {
        if (!this.getId().equals(freshStepExecution.getId())) {
            throw new IllegalArgumentException("Coding error - calling StepExecution.refresh (freshStepExecution) with a StepExecution object with the wrong id.");
        }
        assert (this != freshStepExecution);  // Doesn't break anything if it is the same instance - but makes no sense, so just assertion.
        this.completedTime = freshStepExecution.completedTime;
        this.createdTime = freshStepExecution.createdTime;
        this.proportionCompleted = freshStepExecution.proportionCompleted;
        this.startedRunningTime = freshStepExecution.startedRunningTime;
        this.submittedTime = freshStepExecution.submittedTime;
        this.state = freshStepExecution.state;
        this.setException(freshStepExecution.getException());
    }

    /**
     * The natural order of StepExecutions is the order in which they were created.
     *
     * @param that the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(StepExecution that) {
        if (this == that) {
            return 0;
        }
        final long thisTime = this.getCreatedTime().getTime();
        final long thatTime = that.getCreatedTime().getTime();
        if (thisTime < thatTime) {
            return -1;
        }
        if (thatTime < thisTime) {
            return 1;
        }
        if (this.getId() != null && that.getId() == null) {
            return -1;
        }
        if (that.getId() != null && this.getId() == null) {
            return 1;
        }
        if (this.getId() != null && that.getId() != null) {
            if (this.getId() < that.getId()) {
                return -1;
            }
            if (that.getId() < this.getId()) {
                return 1;
            }
        }
        if (this.hashCode() < that.hashCode()) {
            return -1;
        }
        if (that.hashCode() < this.hashCode()) {
            return 1;
        }
        return 0;
    }
}
