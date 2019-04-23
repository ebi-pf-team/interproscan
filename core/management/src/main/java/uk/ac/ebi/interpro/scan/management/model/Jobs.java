package uk.ac.ebi.interpro.scan.management.model;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simply holds a List of Jobs.  Only used so that the List can be
 * parameterized to hold only Job objects for use in Spring.
 *
 * @author Phil Jones
 * @author David Binns
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0
 */
public class Jobs {

    private static final Logger LOGGER = Logger.getLogger(Jobs.class.getName());

    private String baseDirectoryTemporaryFiles;

    /* Represents all active (non deactivated) jobs. */
    private Map<String, Job> jobMap;

    /* Represents all deactivated jobs (most of the time licensed analysis with no specified path to the binary) */
    final Map<Job, JobStatusWrapper> deactivatedJobs = new HashMap<Job, JobStatusWrapper>();

    private Map<String, Step> stepMap;

    private final Object stepMapLocker = new Object();

    /**
     * Returns all of the jobs of type "analysis".
     *
     * @return all of the jobs of type "analysis".
     */
    public Jobs getAnalysisJobs() {
        List<Job> analysisJobs = new ArrayList<Job>();
        for (Job job : jobMap.values()) {
            if (job.isAnalysis()) {
                analysisJobs.add(job);
            }
        }
        return new Jobs(analysisJobs);
    }

    /**
     * Returns all of the jobs of type "analysis", but only the latest (active) version.
     *
     * @return All the latest/active analysis jobs.
     */
    public Jobs getActiveAnalysisJobs() {
        List<Job> analysisJobs = new ArrayList<Job>();
        for (Job job : jobMap.values()) {
            if (job.isAnalysis() && job.isActive()) {
                analysisJobs.add(job);
            }
        }
        return new Jobs(analysisJobs);
    }

    public Jobs getActiveNonDeprecatedAnalysisJobs() {
        List<Job> analysisJobs = new ArrayList<Job>();
        for (Job job : jobMap.values()) {
            if (job.isAnalysis() && job.isActive() &&  ! job.isDeprecated()) {
                analysisJobs.add(job);
            }
        }
        return new Jobs(analysisJobs);
    }


    public List<Job> getJobList() {
        return new ArrayList<Job>(jobMap.values());
    }

    public List<String> getJobIdList() {
        return new ArrayList<String>(jobMap.keySet());
    }

    public Jobs getAllJobs() {
        List<Job> allJobs = new ArrayList<Job>();
        if (jobMap != null) {
            allJobs.addAll(jobMap.values());
        }
        if (deactivatedJobs != null) {
            allJobs.addAll(deactivatedJobs.keySet());
        }
        return new Jobs(allJobs);
    }

    /**
     * Spring constructor
     */
    public Jobs() {
    }

    public Jobs(List<Job> jobList) {
        this(jobList, false);
    }

    public Jobs(List<Job> jobList, boolean doPreProcessing) {
        if (jobList != null) {
            this.jobMap = new HashMap<String, Job>(jobList.size());
            if (doPreProcessing) {
                preProcessJobs(jobList);
            } else {
                for (Job jobListItem : jobList) {
                    jobMap.put(jobListItem.getId(), jobListItem);
                }
            }
        }
    }


    /**
     * Divides jobs into active and deactivated jobs.
     *
     * @param jobList
     */
    @Required
    public void setJobList(List<Job> jobList) {
        if (jobList != null) {
            this.jobMap = new HashMap<String, Job>(jobList.size());
            preProcessJobs(jobList);
        }
    }


    public Map<Job, JobStatusWrapper> getDeactivatedJobs() {
        return deactivatedJobs;
    }


    private void preProcessJobs(List<Job> jobList) {
        for (Job jobListItem : jobList) {
            checkJobStatusAndAddToMap(jobListItem);
        }
    }

    /**
     * Checks if the job status is ACTIVE or DEACTIVATED.
     *
     * @param job Job to check.
     */
    private void checkJobStatusAndAddToMap(Job job) {
        JobStatusWrapper jobStatusWrapper = JobStatusChecker.getJobStatus(job);
        //Check which jobs are active
        if (jobStatusWrapper.getJobStatus().equals(JobStatusWrapper.JobStatus.ACTIVE)) {
            jobMap.put(job.getId(), job);
        }
        //Check which jobs are deactivated
        else if (jobStatusWrapper.getJobStatus().equals(JobStatusWrapper.JobStatus.DEACTIVATED)) {
            deactivatedJobs.put(job, jobStatusWrapper);
        }
    }

    public String getBaseDirectoryTemporaryFiles() {
        return baseDirectoryTemporaryFiles;
    }

    @Required
    public void setBaseDirectoryTemporaryFiles(String baseDirectoryTemporaryFiles) {
        this.baseDirectoryTemporaryFiles = baseDirectoryTemporaryFiles;
    }

    public Job getJobById(String id) {
        return jobMap.get(id);
    }

    public Step getStepById(String stepId) {
        if (stepMap == null) {
            synchronized (stepMapLocker) {
                if (stepMap == null) {
                    this.stepMap = new HashMap<String, Step>();
                    for (Job job : jobMap.values()) {
                        for (Step step : job.getSteps()) {
                            stepMap.put(step.getId(), step);
                        }
                    }
                }
            }
        }
        return stepMap.get(stepId);
    }

    /**
     * Returns a subset of all of the defined jobs
     * that are restricted by the jobIds (bean IDs) passed in.
     * <p/>
     * This is done on the basis that the actual jobId contains the id passed in.
     * <p/>
     * Allows analyses to be restricted, e.g. by command line arguments.
     *
     * @param jobIds being an array of bean IDs to be included.
     * @return a new Jobs object, containing the restricted set of Jobs.
     */
    public Jobs subset(String[] jobIds) {
        final List<Job> subsetResult = new ArrayList<Job>();
        //Existence check was already done in a very early stage
        //So no need to check existence again
        for (String jobId : jobIds) {
            Job job = jobMap.get(jobId);
            if (job.isAnalysis()) {
                subsetResult.add(job);
            }
        }
        return new Jobs(subsetResult);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Jobs");
        sb.append("{jobMap=").append(jobMap);
        sb.append(", stepMap=").append(stepMap);
        sb.append('}');
        return sb.toString();
    }
}
