package uk.ac.ebi.interpro.scan.management.model;

import org.springframework.beans.factory.annotation.Required;

import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class Job {

    private Long id;

    private String description;

    private List<Step> steps = new ArrayList<Step>();

    public Job() {
    }


    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    /**
     * A descriptive name for this job.
     * @param description a descriptive (and preferably unique)
     * name for this job.
     */
    @Required
    public void setDescription(String description) {
        this.description = description;
    }

    public List<Step> getSteps() {
        return steps;
    }

    void addStep(Step step) {
        steps.add(step);
    }
}
