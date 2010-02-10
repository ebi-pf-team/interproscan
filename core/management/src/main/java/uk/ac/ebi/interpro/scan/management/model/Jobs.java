package uk.ac.ebi.interpro.scan.management.model;

import org.springframework.beans.factory.annotation.Required;

import java.util.List;

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

    private List<Job> jobList;

    public List<Job> getJobList() {
        return jobList;
    }

    @Required
    public void setJobList(List<Job> jobList) {
        this.jobList = jobList;
    }
}
