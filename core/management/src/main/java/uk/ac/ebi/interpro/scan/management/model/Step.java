package uk.ac.ebi.interpro.scan.management.model;

import org.springframework.beans.factory.annotation.Required;

import java.io.Serializable;
import java.util.UUID;
import java.util.List;
import java.text.NumberFormat;
import java.text.DecimalFormat;

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
public abstract class Step<I extends StepInstance, E extends StepExecution> implements Serializable {

    protected String id;

    protected Job job;

    protected String stepDescription;

    protected Queue queue;

    private static final String PROTEIN_BOTTOM_HOLDER = "\\[PROTSTART\\]";

    private static final String PROTEIN_TOP_HOLDER = "\\[PROTEND\\]";

    private static final String MODEL_BOTTOM_HOLDER = "\\[MODSTART\\]";

    private static final String MODEL_TOP_HOLDER = "\\[MODEND\\]";

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
    protected List<I> stepInstances;

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

    /**
     * The format used for file names based upon integers
     * to ensure that they order correctly in the filesystem.
     */
    public static final NumberFormat TWELVE_DIGIT_INTEGER = new DecimalFormat("000000000000");

    public String filterFileNameProteinBounds (String fileNameTemplate, long bottomProteinId, long topProteinId){
        fileNameTemplate = fileNameTemplate.replaceAll(PROTEIN_BOTTOM_HOLDER, TWELVE_DIGIT_INTEGER.format(bottomProteinId));
        fileNameTemplate = fileNameTemplate.replaceAll(PROTEIN_TOP_HOLDER, TWELVE_DIGIT_INTEGER.format(topProteinId));
        return fileNameTemplate;
    }

     public String filterFileNameModelBounds (String fileNameTemplate, long bottomModelId, long topModelId){
        fileNameTemplate = fileNameTemplate.replaceAll(MODEL_BOTTOM_HOLDER, TWELVE_DIGIT_INTEGER.format(bottomModelId));
        fileNameTemplate = fileNameTemplate.replaceAll(MODEL_TOP_HOLDER, TWELVE_DIGIT_INTEGER.format(topModelId));
        return fileNameTemplate;
    }
}
