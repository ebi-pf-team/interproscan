package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.interpro.scan.management.model.StepExecutionState;

import java.io.Serializable;
import java.util.UUID;

/**
 * The WorkerState class is used to transmit the state of
 * a Worker to a caller.
 *
 * @author Phil Jones
 * @version $Id: WorkerState.java,v 1.3 2009/10/16 15:40:30 pjones Exp $
 * @since 1.0
 */
public class WorkerState implements Serializable {

    /**
     * How long has the Worker been alive for?
     */
    private long timeAliveMillis;

    /**
     * A double value between 0 and 1 indicating the proportion of the
     * job completed.  This value CAN be null, indicating that the
     * completion status is unknown.
     */
    private Double proportionComplete;

    /**
     * Should be a human readable, useful description of the current job.
     * TODO - for multi-use workers, should they return a List of all the jobs
     * they have already done?
     */
    private String jobDescription;

    /**
     * Should be a unique identifier for the current job.
     * TODO - for multi-use workers, should the return a List of all the job IDs
     * they have already done?
     */
    private String jobId;

    /**
     * String with any content (at the moment!) describing
     * the status of the Worker.
     */
    private String workerStatus;

    /**
     * Last known state of the current StepExecution
     */
    private StepExecutionState stepExecutionStatus;

    /**
     * The host that the Worker is running on.
     */
    private final String hostName;

    /**
     * Indicates if the Worker is for single use only.
     */
    private final boolean singleUseOnly;

    private final UUID workerIdentification;

    private Throwable exceptionThrown;

//    private WorkerState(){
//
//    }

    public WorkerState(long timeAliveMillis, String hostName, UUID workerIdentification, boolean singleUseOnly) {
        this.timeAliveMillis = timeAliveMillis;
        this.hostName = hostName;
        this.workerIdentification = workerIdentification;
        this.singleUseOnly = singleUseOnly;
    }

    public void setTimeAliveMillis(long timeAliveMillis) {
        this.timeAliveMillis = timeAliveMillis;
    }

    public void setProportionComplete(Double proportionComplete) {
        this.proportionComplete = proportionComplete;
    }

    public void setWorkerStatus(String workerStatus) {
        this.workerStatus = workerStatus;
    }

    public void setExceptionThrown(Throwable exceptionThrown) {
        this.exceptionThrown = exceptionThrown;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public StepExecutionState getStepExecutionStatus() {
        return stepExecutionStatus;
    }

    public void setStepExecutionState(StepExecutionState stepExecutionStatus) {
        this.stepExecutionStatus = stepExecutionStatus;
    }

    public long getTimeAliveMillis() {
        return timeAliveMillis;
    }

    public Double getProportionComplete() {
        return proportionComplete;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public String getWorkerStatus() {
        return workerStatus;
    }

    public String getHostName() {
        return hostName;
    }

    public UUID getWorkerIdentification() {
        return workerIdentification;
    }

    public boolean isSingleUseOnly() {
        return singleUseOnly;
    }

    public Throwable getExceptionThrown() {
        return exceptionThrown;
    }

    public String getJobId() {
        return jobId;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerState)) return false;

        WorkerState that = (WorkerState) o;

        if (timeAliveMillis != that.timeAliveMillis) return false;
        if (exceptionThrown != null ? !exceptionThrown.equals(that.exceptionThrown) : that.exceptionThrown != null)
            return false;
        if (!hostName.equals(that.hostName)) return false;
        if (jobDescription != null ? !jobDescription.equals(that.jobDescription) : that.jobDescription != null)
            return false;
        if (jobId != null ? !jobId.equals(that.jobId) : that.jobId != null) return false;
        if (proportionComplete != null ? !proportionComplete.equals(that.proportionComplete) : that.proportionComplete != null)
            return false;
        if (workerStatus != null ? !workerStatus.equals(that.workerStatus) : that.workerStatus != null) return false;
        if (!workerIdentification.equals(that.workerIdentification)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (timeAliveMillis ^ (timeAliveMillis >>> 32));
        result = 31 * result + (proportionComplete != null ? proportionComplete.hashCode() : 0);
        result = 31 * result + (jobDescription != null ? jobDescription.hashCode() : 0);
        result = 31 * result + (jobId != null ? jobId.hashCode() : 0);
        result = 31 * result + (workerStatus != null ? workerStatus.hashCode() : 0);
        result = 31 * result + hostName.hashCode();
        result = 31 * result + workerIdentification.hashCode();
        result = 31 * result + (exceptionThrown != null ? exceptionThrown.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("timeAliveMillis", timeAliveMillis).
                append("proportionComplete", proportionComplete).
                append("jobDescription", jobDescription).
                append("jobId", jobId).
                append("workerStatus", workerStatus).
                append("hostName", hostName).
                append("singleUseOnly", singleUseOnly).
                append("workerIdentification", workerIdentification).
                append("exceptionThrown", exceptionThrown).
                toString();
    }
}
