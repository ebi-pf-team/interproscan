package uk.ac.ebi.interpro.scan.management.model;

import uk.ac.ebi.interpro.scan.model.KeyGen;

import javax.persistence.*;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * A StepInstance is built from a Step (template) for (optionally)
 * specific ranges of protein IDs and / or specific ranges of
 * model IDs.  Instances of this class therefore represent a
 * specific piece of work that needs to be performed.
 * <p/>
 * Attempts to execute a StepInstance are represented as
 * StepExecution objects. The Step defines whether or not the
 * StepInstance can be re-run in the event of failure (how many
 * times it can be re-run.)  In the event of failure, and re-runs
 * being > 0, there may be multiple StepExecutions associated
 * with a single StepInstance.
 * <p/>
 * All things being equals, this class should be FINAL, but can't
 * be because of JPA.  (So don't subclass!)
 * <p/>
 * NOTE: Removed (name="step_instance") for the moment...
 *
 * @author Phil Jones
 * @version $Id$
 * @see StepExecution
 * @since 1.0-SNAPSHOT
 */

@Entity
@Table
public class StepInstance implements Serializable {

    private static final String PROTEIN_BOTTOM_HOLDER = "\\[PROTSTART\\]";

    private static final String PROTEIN_TOP_HOLDER = "\\[PROTEND\\]";

    private static final String MODEL_BOTTOM_HOLDER = "\\[MODSTART\\]";

    private static final String MODEL_TOP_HOLDER = "\\[MODEND\\]";

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "STEP_INS_IDGEN")
    @TableGenerator(name = "STEP_INS_IDGEN", table = KeyGen.KEY_GEN_TABLE, pkColumnValue = "step_instance", initialValue = 0, allocationSize = 50)
    private Long id;

    /**
     * Relationship is lazy-loaded
     * (The Step is defined in XML, not in the database.)
     */
    @Transient
    private transient Step step;

    /**
     * For the purpose of persistance, this is the id of the Step that this
     * StepInstance is associated with.  Note that the Step is not persisted,
     * so this reference allows the Step / StepInstance / StepExecution structure
     * to be recreated.
     */
    @Column(nullable = false, name = "step_id")
    private String stepId;

    @Column(nullable = true, name = "bottom_protein")
    private Long bottomProtein;

    @Column(nullable = true, name = "top_protein")
    private Long topProtein;

    @Column(nullable = true, name = "bottom_model")
    private Long bottomModel;

    @Column(nullable = true, name = "top_model")
    private Long topModel;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST})
    private List<StepInstance> dependsUpon = new ArrayList<StepInstance>();

    // Full list of analyses for -appl parameter can be more than the 255 character String default!
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @Column(length = 350)
//    @MapKeyJoinColumn ()
//    @ElementCollection(targetElement = String.class, fetch = FetchType.EAGER)
//    @MapKeyManyToMany(targetEntity = String.class)
    private Map<String, String> parameters;

    @Column(nullable = false, name = "time_created")
    private Date timeCreated;

    /**
     * List of all the executions of this StepInstance.
     * Set to transient so they don't all get shoved over the
     * wire when submitting new StepExecutions to the messaging system.
     */
    @OneToMany(targetEntity = StepExecution.class, fetch = FetchType.EAGER, mappedBy = "stepInstance", cascade = {})
    private Set<StepExecution> executions = new TreeSet<StepExecution>();

    @Transient
    private transient boolean stateUnknown =false;

    public StepInstance(Step step) {
        this(step, null, null, null, null);
    }

    public StepInstance(Step step, Long bottomProteinId, Long topProteinId, Long bottomModelId, Long topModelId) {
        this.step = step;            // This is NOT persisted.
        this.stepId = step.getId();  // This is persisted.
        this.bottomProtein = bottomProteinId;
        this.topProtein = topProteinId;
        this.bottomModel = bottomModelId;
        this.topModel = topModelId;
        timeCreated = new Date();
    }

    public void addParameter(String key, String value) {
        if (this.parameters == null) {
            parameters = new HashMap<String, String>();
        }
        parameters.put(key, value);
    }

    public void addParameters(Map<String, String> parameters) {

        if (parameters == null) return;

        if (this.parameters == null) {
            this.parameters = new HashMap<String, String>();
        }
        this.parameters.putAll(parameters);
    }

    /**
     * Retrieve arbitrary step parameters.
     *
     * @return Map of parameters (key, value pairs in a Map).
     */
    public Map<String, String> getParameters() {
        return parameters;
    }


    /**
     * Don't use this! Only here because required by JPA for persistence.
     */
    protected StepInstance() {
    }

    public void addDependentStepInstance(StepInstance dependentStepInstance) {
        this.dependsUpon.add(dependentStepInstance);
    }

    public void addStepExecution(StepExecution stepExecution) {
        // Sanity check
        if (!stateUnknown) {
            for (StepExecution previousExecutions : executions) {
                if (previousExecutions.getState() != StepExecutionState.STEP_EXECUTION_FAILED) {
                    throw new IllegalStateException("Attempting to add a new StepExecution to step " + this + " when there is an existing (NON-STEP_EXECUTION_FAILED) step execution.");
                }
            }
        }
        executions.add(stepExecution);
    }

    /**
     * Determines the state of this StepInstance from the states of all / any StepExecutions
     *
     * @return the state of this StepInstance
     */
    private StepExecutionState getState() {
        if (executions.size() == 0) {
            return StepExecutionState.NEW_STEP_INSTANCE;
        }
        for (StepExecution exec : executions) {
            final StepExecutionState executionState = exec.getState();
            switch (executionState) {
                case NEW_STEP_EXECUTION:
                case STEP_EXECUTION_SUBMITTED:
                case STEP_EXECUTION_RUNNING:
                case STEP_EXECUTION_SUCCESSFUL:
                    return executionState;
                default:
                    break;
            }
        }
        return StepExecutionState.STEP_EXECUTION_FAILED;
    }

    /**
     * get the stepInstance State
     * @return
     */
    public StepExecutionState getStepInstanceState(){
        return getState();
    }

    public Long getId() {
        return id;
    }

    public Step getStep(Jobs jobs) {
        if (step == null) {
            step = jobs.getStepById(stepId);
        }
        return step;
    }

    public Long getBottomProtein() {
        return bottomProtein;
    }

    public Long getTopProtein() {
        return topProtein;
    }

    public Long getBottomModel() {
        return bottomModel;
    }

    public Long getTopModel() {
        return topModel;
    }

    public List<StepInstance> stepInstanceDependsUpon() {
        return dependsUpon;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public String getStepId() {
        return stepId;
    }

    /**
     * This method returns true if this StepInstance is a candidate to be submitted.
     * <p/>
     * The requirements for re-submission are:
     * 1. This method has never been submitted before, OR it has failed previously and
     * the number of submissions does not exceed the retry count for this step.
     * 2. All of the dependencies of this stepInstance (StepInstances that must have
     * successfully completed prior to this StepInstance) have completed.
     *
     * @param jobs
     * @return true if this StepInstance can be submitted.
     */
    public boolean canBeSubmitted(Jobs jobs) {
        // First, check if the state of this StepInstance allows it to be run...
        // (Not submitted or previously failed, and number of retries not exceeded)
        final StepExecutionState state = getState();

        if (StepExecutionState.NEW_STEP_INSTANCE == state
                ||
                (StepExecutionState.STEP_EXECUTION_FAILED == state && this.getExecutions().size() < this.getStep(jobs).getRetries())) {
            // Then check that all the dependencies have been completed successfully.
            if (dependsUpon != null) {
                for (StepInstance dependency : dependsUpon) {
                    // The state of the dependencies already checked may change during this loop,
                    // however this is not a problem - the worst that can happen, is that the StepInstance is not
                    // executed now.
                    if (dependency.getState() != StepExecutionState.STEP_EXECUTION_SUCCESSFUL) {
                        return false;
                    }
                }
            }
            // All requirements met, so can submit.
            return true;
        }
        return false;
    }

    /**
     * Called by MASTER (Single thread) and also by this object...?
     *
     * @param jobs
     * @return
     */
    public boolean haveFinished(Jobs jobs) {
        final StepExecutionState executionState = getState();
        return StepExecutionState.STEP_EXECUTION_SUCCESSFUL == executionState ||
                StepExecutionState.STEP_EXECUTION_FAILED == executionState &&
                        this.getExecutions().size() >= this.getStep(jobs).getRetries();
    }


    public Set<StepExecution> getExecutions() {
        return executions;
    }


    /**
     * Called by MasterMessageSenderImpl, dependent of Master.
     *
     * @return a new StepExecution.
     */
    public StepExecution createStepExecution() {
        return new StepExecution(this);
    }


    /**
     * The format used for file names based upon integers
     * to ensure that they order correctly in the filesystem.
     */
    public static final NumberFormat TWELVE_DIGIT_INTEGER = new DecimalFormat("000000000000");

    public String buildFullyQualifiedFilePath(String temporaryFileDirectory, String fileNameTemplate) {
        fileNameTemplate = filter(fileNameTemplate, PROTEIN_BOTTOM_HOLDER, this.bottomProtein);
        fileNameTemplate = filter(fileNameTemplate, PROTEIN_TOP_HOLDER, this.topProtein);
        fileNameTemplate = filter(fileNameTemplate, MODEL_BOTTOM_HOLDER, this.bottomModel);
        fileNameTemplate = filter(fileNameTemplate, MODEL_TOP_HOLDER, this.topModel);

        return new StringBuilder()
                .append(temporaryFileDirectory)
                .append('/')
                .append(fileNameTemplate)
                .toString();
    }

    private String filter(String template, String pattern, Long value) {
        return (value == null)
                ? template
                : template.replaceAll(pattern, TWELVE_DIGIT_INTEGER.format(value));
    }

    /**
     * Simple method to indicate if this StepInstance has protein bounds.
     *
     * @return true if this StepInstance has protein bounds.
     */
    public boolean hasProteinBounds() {
        return this.getBottomProtein() != null && this.getTopProtein() != null;
    }

    /**
     * Called by MASTER. MASTER runs in single thread... so not synchronized.
     *
     * @param jobs set of all Jobs
     * @return true, if this StepInstance has failed an no chance of any repeats.
     */
    public boolean hasFailedPermanently(Jobs jobs) {
        return StepExecutionState.STEP_EXECUTION_FAILED == getState()
                &&
                haveFinished(jobs);
    }

    /**
     *
     * set state to unknown for adding new stepExecutions
     *
     */

    public void setStateUnknown(boolean stateUnknown) {
        this.stateUnknown = stateUnknown;
    }

    /**
     * called by the master if the stepState is unknown, the message might have been lost
     *
     * if something went wrong and we have no record, resubmit this step
     */
    public boolean canBeSubmittedAfterUnknownFailure(Jobs jobs){
        if(stateUnknown ){
            if( this.getExecutions().size() < this.getStep(jobs).getRetries() ){
                this.stateUnknown = false;
                return true;
            }
        }
        return false;
    }


    /**
     * Returns true if the protein bounds of this StepInstance overlap
     * with the protein bounds of the StepInstance passed in
     *
     * @param that being the StepInstance to compare with.
     * @return true if the protein bounds of this StepInstance overlap
     *         with the protein bounds of the StepInstance passed in
     */
    public boolean proteinBoundsOverlap(StepInstance that) {
        return
                this.hasProteinBounds() && that.hasProteinBounds()
                        && !
                        ((this.getBottomProtein() > that.getTopProtein()) ||
                                (that.getBottomProtein() > this.getTopProtein()));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StepInstance");
        sb.append("{id='").append(id).append('\'');
        sb.append(", timeCreated='").append(timeCreated).append('\'');
        sb.append(", stepId='").append(stepId).append('\'');
        sb.append(", bottomProtein=").append(bottomProtein);
        sb.append(", topProtein=").append(topProtein);
//        sb.append(", bottomModel=").append(bottomModel);
//        sb.append(", topModel=").append(topModel);
//        sb.append(", dependsUpon=").append(dependsUpon);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StepInstance that = (StepInstance) o;

        if (bottomModel != null ? !bottomModel.equals(that.bottomModel) : that.bottomModel != null) return false;
        if (bottomProtein != null ? !bottomProtein.equals(that.bottomProtein) : that.bottomProtein != null)
            return false;
        if (dependsUpon != null ? !dependsUpon.equals(that.dependsUpon) : that.dependsUpon != null) return false;
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;
        if (!stepId.equals(that.stepId)) return false;
        if (!timeCreated.equals(that.timeCreated)) return false;
        if (topModel != null ? !topModel.equals(that.topModel) : that.topModel != null) return false;
        if (topProtein != null ? !topProtein.equals(that.topProtein) : that.topProtein != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = stepId.hashCode();
        result = 31 * result + (bottomProtein != null ? bottomProtein.hashCode() : 0);
        result = 31 * result + (topProtein != null ? topProtein.hashCode() : 0);
        result = 31 * result + (bottomModel != null ? bottomModel.hashCode() : 0);
        result = 31 * result + (topModel != null ? topModel.hashCode() : 0);
        result = 31 * result + (dependsUpon != null ? dependsUpon.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        result = 31 * result + timeCreated.hashCode();
        return result;
    }
}
