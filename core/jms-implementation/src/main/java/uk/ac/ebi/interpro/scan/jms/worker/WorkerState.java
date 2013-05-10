package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.interpro.scan.management.model.StepExecutionState;

import javax.jms.Message;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The WorkerState class is used to transmit the state of
 * a Worker to a caller.
 *
 * @author Phil Jones
 * @version $Id: WorkerState.java,v 1.3 2009/10/16 15:40:30 pjones Exp $
 * @since 1.0
 */
public class WorkerState implements Serializable, Comparable {

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
     * All the jobs this worker has handled
     */
    private  List<Message> allJobs = new ArrayList<Message>();

    private  List<Message>  allCompletedJobs = new ArrayList<Message>();

    private  List<Message>  allNonAcknowledgedJobs = new ArrayList<Message>();

    private  List<Message>  locallyCompletedJobs = new ArrayList<Message>();

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

    public List<Message> getAllJobs() {
        return allJobs;
    }

    public void addNewJob(Message message) {
        this.allJobs.add(message);
    }

    public List<Message> getAllCompletedJobs() {
        return allCompletedJobs;
    }

    public void addCompletedJob(Message message){
        this.allCompletedJobs.add(message);
    }

    public List<Message> getAllNonAcknowledgedJobs() {
        return allNonAcknowledgedJobs;
    }

    public void addNonAcknowledgedJob(Message message) {
        this.allNonAcknowledgedJobs = allNonAcknowledgedJobs;
    }

    public List<Message> getLocallyCompletedJobs() {
        return locallyCompletedJobs;
    }

    public void addLocallyCompletedJob(Message message) {
        this.locallyCompletedJobs.add(message);
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


    /**
     * all jobs not completed are failed jobs
     *
     * @return   failedJobs
     */
    public final List<Message> getFailedJobs(){
        final List<Message> failedJobs = new ArrayList<Message>();
        for(Message job: allJobs){
            if (! allCompletedJobs.contains(job)) {
                failedJobs.add(job);
            }
        }
        return failedJobs;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkerState)) return false;

        WorkerState that = (WorkerState) o;
        // If its the same JVM, its the same worker.
        if (this.getWorkerIdentification().equals(that.getWorkerIdentification())) {
            return true;
        }

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

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p/>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     * <p/>
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p/>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     * <p/>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     * <p/>
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(Object o) {
        WorkerState other = (WorkerState) o;
        if (this == other || this.equals(other)) {
            return 0;
        }
        int thisSingle = (this.isSingleUseOnly()) ? 0 : 1;
        int otherSingle = (other.isSingleUseOnly()) ? 0 : 1;
        int comparator = otherSingle - thisSingle;
        if (comparator == 0) {
            comparator = this.getHostName().compareTo(other.getHostName());
        }
        if (comparator == 0) {
            comparator = (this.getWorkerIdentification().compareTo(other.getWorkerIdentification()));
        }
        if (comparator == 0) {
            comparator = this.getJobId().compareTo(other.getJobId());
        }
        return comparator;
    }
}
