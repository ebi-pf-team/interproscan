package uk.ac.ebi.interpro.scan.management.model;

import org.springframework.beans.factory.annotation.Required;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class describes a Job, which is constructed from N steps.
 * Jobs and Steps are templates for analyses.  To actually run
 * analyses against specific proteins (and perhaps specific models)
 * StepInstances are instantiated.  These instances are then
 * run as StepExecutions.  If a StepExecution fails, and the
 * Step is configured to be repeatable, then another attempt
 * to run the instance will be made.
 *
 * NOTE: Instances of Jobs and Steps are defined in Spring XML.  They
 * are NOT persisted to the database - only StepInstances and StepExecutions
 * are persisted.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class Job implements Serializable {

    private String description;

    
    /**
     * List of steps.  this is transient so they don't all get shoved
     * over the wire when each StepExecution is run.
     */
    private transient List<Step> steps = new ArrayList<Step>();

    public Job() {
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
