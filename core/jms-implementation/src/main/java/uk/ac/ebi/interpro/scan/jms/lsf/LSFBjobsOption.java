package uk.ac.ebi.interpro.scan.jms.lsf;

/**
 * Represents a valid LSF option.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id: LSFBjobsOption.java,v 1.2 2012/08/13 16:02:35 craigm Exp $
 * @since 1.0-SNAPSHOT
 */
public enum LSFBjobsOption implements IJobOption {
    QUEUE_NAME("-q", "Name of the queue",true),
    JOB_NAME("-J", "job_name",true),
    JOB_DESC("-Jd", "job_description",true),
    PROJECT_NAME("-P", "project_name",true),
    SHOW_ALL_STATES("-a", "Displays information about jobs in all states",false),
    WIDE_FORMAT("-w", "Displays job information without truncating fields",false),
    RESOURCE_USAGE_INFORMATION("-W", "Provides resource usage information for: " +
                                     "PROJ_NAME, CPU_USED, MEM, SWAP, PIDS, START_TIME, "+
                                     "FINISH_TIME",false);

    private String shortOpt;

    private String description;

    private boolean argumentRequired;

    private LSFBjobsOption(
            String shortOpt,
            String description,
            boolean argumentRequired
    ) {
        this.shortOpt = shortOpt;
        this.description = description;
        this.argumentRequired = argumentRequired;
    }

    public String getShortOpt() {
        return shortOpt;
    }

    public boolean isArgumentRequired() {
        return argumentRequired;
    }

    public String getDescription() {
        return description;
    }
}