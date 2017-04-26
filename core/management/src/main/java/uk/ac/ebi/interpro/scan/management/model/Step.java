package uk.ac.ebi.interpro.scan.management.model;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Instances of this class describe / provide a template
 * for a step.  A single step corresponds to a single
 * message to a JMS queue.  Steps can be dependent upon
 * one another.
 * <p/>
 * Steps are always part of a Job, where a Job may comprise
 * one or more steps.
 * <p/>
 * To actually run
 * analyses against specific proteins (and perhaps specific models)
 * StepInstances are instantiated.  These instances are then
 * run as StepExecutions.  If a StepExecution fails, and the
 * Step is configured to be repeatable, then another attempt
 * to run the instance will be made.
 * <p/>
 * NOTE: Instances of Jobs and Steps are defined in Spring XML.  They
 * are NOT persisted to the database - only StepInstances and StepExecutions
 * are persisted.
 * <p/>
 * This class is abstract - it is expected that this will be
 * subclassed to allow additional parameters to be injected
 * and to implement the execute method.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public abstract class Step implements BeanNameAware {

    protected String id;

    protected Job job;

    protected String stepDescription;

    protected SerialGroup serialGroup;

    protected boolean requiresDatabaseAccess = true;


    /**
     * Number of retries
     */
    protected int retries;

    /**
     * Step which must be completed prior to this one.
     */
    protected List<Step> dependsUpon;

    /**
     * If not-null, this Step is run via Quartz using the cronSchedule
     * specified.  (Used for example to monitor new protein on UniParc.)
     */
    protected String cronSchedule;

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

    /**
     * The nfsDelayMilliseconds bean property can optionally be set to allow
     * a Step to delay starting, if there is a risk of stale NFS handles affecting
     * its operation.
     */
    private int nfsDelayMilliseconds;

    public String getId() {
        return id;
    }

    public void setBeanName(String s) {
        this.id = s;
    }

    public Job getJob() {
        return job;
    }

    @Required
    public void setJob(Job job) {
        this.job = job;
        job.addStep(this);
    }

    public List<Step> getDependsUpon() {
        return dependsUpon;
    }

    public void setDependsUpon(List<Step> dependsUpon) {
        this.dependsUpon = dependsUpon;
    }

    public boolean isCreateStepInstancesForNewProteins() {
        return createStepInstancesForNewProteins;
    }

    @Required
    public void setCreateStepInstancesForNewProteins(boolean createStepInstancesForNewProteins) {
        this.createStepInstancesForNewProteins = createStepInstancesForNewProteins;
    }

    public boolean isRequiresDatabaseAccess() {
        return requiresDatabaseAccess;
    }

    public void setRequiresDatabaseAccess(boolean requiresDatabaseAccess) {
        this.requiresDatabaseAccess = requiresDatabaseAccess;
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

    public SerialGroup getSerialGroup() {
        return serialGroup;
    }

    public void setSerialGroup(SerialGroup serialGroup) {
        this.serialGroup = serialGroup;
    }

    public int getRetries() {
        return retries;
    }

    public void setNfsDelayMilliseconds(int nfsDelayMilliseconds) {
        this.nfsDelayMilliseconds = nfsDelayMilliseconds;
    }

    @Required
    public void setRetries(int retries) {
        this.retries = retries;
    }

    /**
     * This convenience method allows a delay to be called on a Step implementation, with the duration
     * of the delay determined by the nfsDelayMilliseconds bean property.
     * <p/>
     * This method has to be called explicitly from the execute method, so it is not called willy-nilly when not
     * required.
     */
    protected void delayForNfs() {
        if (nfsDelayMilliseconds > 0) {
            try {
                Thread.sleep(nfsDelayMilliseconds);
            } catch (InterruptedException e) {
                throw new IllegalStateException("InterruptedException thrown when attempting to sleep for NFS delay.", e);
            }
        }
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     * <p/>
     * If an error occurs that cannot be immediately recovered from, the implementation
     * of this method <b>MUST</b> throw a suitable Exception, as the call
     * to execute is performed within a transaction with the reply to the JMSBroker.
     * <p/>
     * Implementations of this method MAY call <code>this.delayForNfs()</code> before starting, if, for example,
     * they are operating on file system resources.
     * <p/>
     * <h2>Notes:</h2>
     * <p/>
     * <p>The StepInstance parameter that is passed in provides the following useful methods that you may need to use
     * in your implementation:
     * <p/>
     * <p><code>stepInstance.buildFullyQualifiedFilePath(String temporaryFileDirectory, String fileNameTemplate)</code>
     * <p>should be used to ensure that temporary files are written to the appropriate location, with file names
     * filtered for the range of proteins / models being analysed.  Note that the parameter to this method
     * temporaryFileDirectory is also passed in to executions of this method.
     * <p/>
     * <p>To determine the range of proteins or models being analysed, call any of:
     * <p/>
     * <ul>
     * <li><code>stepInstance.getBottomProtein()</code></li>
     * <li><code>stepInstance.getTopProtein()</code></li>
     * <li><code>stepInstance.getBottomModel()</code></li>
     * <li><code>stepInstance.getTopModel()</code></li>
     * </ul>
     *
     * @param stepInstance           containing the parameters for executing. Provides utility methods as described
     *                               above.
     * @param temporaryFileDirectory which can be passed into the
     *                               stepInstance.buildFullyQualifiedFilePath(String temporaryFileDirectory, String fileNameTemplate) method
     *                               to build temporary file paths.
     */
    public abstract void execute(StepInstance stepInstance, String temporaryFileDirectory);


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Step)) return false;

        Step step = (Step) o;

        return id.equals(step.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Step");
        sb.append("{id='").append(id).append('\'');
        sb.append(", stepDescription='").append(stepDescription).append('\'');
        sb.append(", retries=").append(retries);
        sb.append(", cronSchedule='").append(cronSchedule).append('\'');
        sb.append(", createStepInstancesForNewProteins=").append(createStepInstancesForNewProteins);
        sb.append(", maxProteins=").append(maxProteins);
        sb.append(", maxModels=").append(maxModels);
        sb.append(", stepInstances=").append(stepInstances);
        sb.append('}');
        return sb.toString();
    }
}
