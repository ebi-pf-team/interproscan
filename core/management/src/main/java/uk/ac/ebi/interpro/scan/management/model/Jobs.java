package uk.ac.ebi.interpro.scan.management.model;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;

/**
 * Simply holds a List of Jobs.  Only used so that the List can be
 * parameterized to hold only Job objects for use in Spring.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0
 */
public class Jobs {

    private static final Logger LOGGER = Logger.getLogger(Jobs.class);

    private String baseDirectoryTemporaryFiles;

    private Map<String, Job> jobMap;

    private Map<String, Step> stepMap;

    private final Object stepMapLocker = new Object();


    public List<Job> getJobList() {
        return new ArrayList<Job>( jobMap.values() );
    }

    @Required
    public void setJobList(List<Job> jobList) {
        this.jobMap = new HashMap<String, Job>(jobList.size());
        for (Job job : jobList){
            jobMap.put (job.getId(), job);
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
        synchronized (stepMapLocker){
            if (stepMap == null){
                this.stepMap = new HashMap<String, Step>();
                for (Job job : jobMap.values()){
                    for (Step step : job.getSteps()){
                        stepMap.put(step.getId(), step);
                    }
                }
            }
        }
        return stepMap.get(stepId);
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
