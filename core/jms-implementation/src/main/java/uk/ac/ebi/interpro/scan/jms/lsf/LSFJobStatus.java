package uk.ac.ebi.interpro.scan.jms.lsf;

/**
 * Represents a set of possible values for the status of a job.
 * <p/>
 * Most of the description is copied from the manual.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id: LSFJobStatus.java,v 1.4 2012/09/18 12:11:18 craigm Exp $
 * @since 1.0-SNAPSHOT
 */
public enum LSFJobStatus {
    PEND("The job is pending. That is, it has not yet been started."),
    PSUSP("The job has been suspended, either by its owner or the LSF administrator, while pending."),
    RUN("The job is currently running."),
    USUSP("The job has been suspended, either by its owner or the LSF administrator, while running."),
    SSUSP("The job has been suspended by LSF."),
    DONE("The job has terminated with status of 0."),
    EXIT("The job has terminated with a non-zero status it may have been aborted due to an error in its execution, " +
            "or killed by its owner or the LSF administrator."),
    UNKWN("mbatchd has lost contact with the sbatchd on the host on which the job runs."),
    WAIT("For jobs submitted to a chunk job queue, members of a chunk job that are waiting to run."),
    ZOMBI("A job becomes ZOMBI if: " +
            "1. A non-rerunnable job is killed by bkill while the sbatchd on the execution host is unreachable and the job is shown as UNKWN." +
            "2. The host on which a rerunnable job is running is unavailable and the job has been requeued by LSF with a new job ID, as if the " +
            "job were submitted as a new job." +
            "3. After the execution host becomes available, LSF tries to kill the ZOMBI job. Upon successful termination of the ZOMBI job, " +
            "the jobs status is changed to EXIT.");

    private String description;

    public static LSFJobStatus getJobStatus(String status) {
        LSFJobStatus returnStatus = null;
        for (LSFJobStatus jobStatus: LSFJobStatus.values()) {
            if (status.equals(jobStatus.name())) {
                returnStatus = jobStatus;
            }
        }
        if (returnStatus == null) {
            throw new IllegalArgumentException("Could not find an LSF jobs stauts of " + status);

        }
        return returnStatus;
    }

    private LSFJobStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}