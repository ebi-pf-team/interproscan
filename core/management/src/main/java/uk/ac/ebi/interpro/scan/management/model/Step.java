package uk.ac.ebi.interpro.scan.management.model;

import org.springframework.beans.factory.annotation.Required;

/**
 * Instances of this class describe / provide a template
 * for a step.  A single step corresponds to a single
 * message to a JMS queue.  Steps can be dependent upon
 * one another.
 *
 * Steps are always part of a Job, where a Job may comprise
 * one or more steps.
 *
 * This class is abstract - it is expected that this will be
 * subclassed to allow additional parameters to be injected
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public abstract class Step {

    protected Long id;

    protected Job job;

    protected String stepDescription;

    protected Queue queue;

    /**
     * Number of retries
     */
    protected int retries;

    /**
     * Step which must be completed prior to this one.
     */
    protected Step dependsUpon;

    /**
     * Whenever new proteins are added to the database,
     * a routine should be called that iterates over all
     * Steps and creates StepTransactions appropriately.
     */
    protected boolean createStepInstancesForNewProteins = false;

    /**
     * Optional field indicating the maximum number of proteins
     * that a single StepTransaction should handle.
     * If null, then unlimited.
     */
    protected Integer maxProteins;

    /**
     * Optional field indicating the maximum number of models
     * that a single StepTransaction should handle.
     * If null, then unlimited.
     */
    protected Integer maxModels;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    @Required
    public void setJob(Job job) {
        this.job = job;
        job.addStep(this);
    }

    public Step getDependsUpon() {
        return dependsUpon;
    }

    public void setDependsUpon(Step dependsUpon) {
        this.dependsUpon = dependsUpon;
    }

    public boolean isCreateStepInstancesForNewProteins() {
        return createStepInstancesForNewProteins;
    }

    @Required
    public void setCreateStepInstancesForNewProteins(boolean createStepInstancesForNewProteins) {
        this.createStepInstancesForNewProteins = createStepInstancesForNewProteins;
    }

    public Integer getMaxProteins() {
        return maxProteins;
    }

    public void setMaxProteins(Integer maxProteins) {
        this.maxProteins = maxProteins;
    }

    public Integer getMaxModels() {
        return maxModels;
    }

    public void setMaxModels(Integer maxModels) {
        this.maxModels = maxModels;
    }

    public String getStepDescription() {
        return stepDescription;
    }

    @Required
    public void setStepDescription(String stepDescription) {
        this.stepDescription = stepDescription;
    }

    public Queue getQueue() {
        return queue;
    }

    @Required
    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public int getRetries() {
        return retries;
    }

    @Required
    public void setRetries(int retries) {
        this.retries = retries;
    }
}
