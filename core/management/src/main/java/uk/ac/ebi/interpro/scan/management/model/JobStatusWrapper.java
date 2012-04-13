package uk.ac.ebi.interpro.scan.management.model;

/**
 * Wraps job status with a warning.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class JobStatusWrapper {
    private JobStatus jobStatus;

    //Describes the reason for a status.
    private String warning;

    public JobStatusWrapper() {
        this(JobStatus.ACTIVE, "No warning. Analysis is active.");
    }

    public JobStatusWrapper(JobStatus jobStatus, String warning) {
        this.jobStatus = jobStatus;
        this.warning = warning;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public String getWarning() {
        return warning;
    }

    public enum JobStatus {
        ACTIVE, DEACTIVATED, UNKNOWN
    }
}
