package uk.ac.ebi.interpro.scan.management.model;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.persistence.DAOManager;

import java.io.Serializable;
import java.util.List;

/**
 * Instances of this class describe / provide a template
 * for a step.  A single step corresponds to a single
 * message to a JMS queue.  Steps can be dependent upon
 * one another.
 *
 * Steps are always part of a Job, where a Job may comprise
 * one or more steps.
 *
 * To actually run
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
 * This class is abstract - it is expected that this will be
 * subclassed to allow additional parameters to be injected
 * and to implement the execute method.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public abstract class Step implements Serializable {

    protected String id;

    protected Job job;

    protected String stepDescription;

    protected boolean parallel;


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

    /**
     * List of instances of this Step.
     */
    protected transient List<StepInstance> stepInstances;

    public String getId() {
        return id;
    }

    /**
     * Must be set in configuration to unique value.
     * Note that if this is changed between one run of the master node and another,
     * the step instances / executions will no longer be associated with this step.
     * @param id being any unique String.
     */
    @Required
    public void setId(String id) {
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

    public boolean isParallel() {
        return parallel;
    }

    @Required
    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    public int getRetries() {
        return retries;
    }

    @Required
    public void setRetries(int retries) {
        this.retries = retries;
    }

    /**
     * This method is called to execute the action that the StepExecution must perform.
     * This method should typically perform its activity in a try / catch / finally block
     * that sets the state of the step execution appropriately.
     *
     * Note that the implementation DOES have access to the protected stepInstance,
     * and from their to the protected Step, to allow it to access parameters for execution.
     *
     * (For example, constructing file names based upon lower and upper protein IDs or
     * model IDs).
     *
     * TODO - Possibly generify so things other than 'DAOManager' can be passed in.
     * @param daoManager    for DAO processes.
     * @param stepExecution record of execution
     */
    public abstract void execute(DAOManager daoManager, StepExecution stepExecution);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Step)) return false;

        Step step = (Step) o;

        if (!id.equals(step.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


}
