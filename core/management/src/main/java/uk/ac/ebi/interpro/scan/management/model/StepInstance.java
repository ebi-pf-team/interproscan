package uk.ac.ebi.interpro.scan.management.model;

import javax.persistence.*;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A StepInstance is built from a Step (template) for (optionally)
 * specific ranges of protein IDs and / or specific ranges of
 * model IDs.  Instances of this class therefore represent a
 * specific piece of work that needs to be performed.
 *
 * Attempts to execute a StepInstance are represented as
 * StepExecution objects. The Step defines whether or not the
 * StepInstance can be re-run in the event of failure (how many
 * times it can be re-run.)  In the event of failure, and re-runs
 * being > 0, there may be multiple StepExecutions associated
 * with a single StepInstance.
 *
 * All things being equals, this class should be FINAL, but can't
 * be because of JPA.  (So don't subclass!)
 *
 * @see StepExecution
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Entity
@Table (name="step_instance")
public class StepInstance<S extends Step> implements Serializable {

    private static final String PROTEIN_BOTTOM_HOLDER = "\\[PROTSTART\\]";

    private static final String PROTEIN_TOP_HOLDER = "\\[PROTEND\\]";

    private static final String MODEL_BOTTOM_HOLDER = "\\[MODSTART\\]";

    private static final String MODEL_TOP_HOLDER = "\\[MODEND\\]";

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private String id;

    /**
     * Relationship has to be re-created on loading.
     * (The Step is defined in XML, not in the database.)
     */
    @Transient
    private S step;

    /**
     * For the purpose of persistance, this is the id of the Step that this
     * StepInstance is associated with.  Note that the Step is not persisted,
     * so this reference allows the Step / StepInstance / StepExecution structure
     * to be recreated.
     */
    @Column (nullable = false, name="step_id")
    private String stepId;

    @Column(nullable=true, name = "bottom_protein")
    private Long bottomProtein;

    @Column(nullable=true, name="top_protein")
    private Long topProtein;

    @Column(nullable=true, name="bottom_model")
    private Long bottomModel;

    @Column(nullable=true, name="top_model")
    private Long topModel;

    @ManyToMany (fetch = FetchType.EAGER, cascade = {})
    private List<StepInstance> dependsUpon = new ArrayList<StepInstance>();

    /**
     * List of all the executions of this StepInstance.
     * Set to transient so they don't all get shoved over the
     * wire when submitting new StepExecutions to the messaging system.
     */
    @OneToMany (targetEntity = StepExecution.class, fetch = FetchType.EAGER, mappedBy = "stepInstance", cascade = {})
    private Set<StepExecution> executions = new HashSet<StepExecution>();


    public StepInstance(S step, Long bottomProteinId, Long topProteinId, Long bottomModelId, Long topModelId) {
        this.step = step;            // This is NOT persisted.
        this.stepId = step.getId();  // This is persisted.
        this.bottomProtein = bottomProteinId;
        this.topProtein = topProteinId;
        this.bottomModel = bottomModelId;
        this.topModel = topModelId;
    }

    /**
     * Don't use this! Only here because required by JPA for persistence.
     */
    protected StepInstance() {
    }

    public void addDependentStepInstance(StepInstance dependentStepInstance){
        this.dependsUpon.add (dependentStepInstance);
    }

    public void addStepExecution(StepExecution stepExecution){
        // Sanity check
        for (StepExecution previousExecutions : executions){
            if (previousExecutions.getState() != StepExecutionState.STEP_EXECUTION_FAILED){
                throw new IllegalStateException ("Attempting to add a new StepExecution to step " + this + " when there is an existing (NON-STEP_EXECUTION_FAILED) step execution.");
            }
        }
        executions.add (stepExecution);
    }

    /**
     * Determines the state of this StepInstance from the states of all / any StepExecutions
     * @return the state of this StepInstance
     */
    public StepExecutionState getState(){
        if (executions.size() == 0){
            return StepExecutionState.NEW_STEP_INSTANCE;
        }
        for (StepExecution exec : executions){
            switch (exec.getState()){
                case NEW_STEP_EXECUTION:
                case STEP_EXECUTION_SUBMITTED:
                case STEP_EXECUTION_RUNNING:
                case STEP_EXECUTION_SUCCESSFUL:
                    return exec.getState();
                default:
                    break;
            }
        }
        return StepExecutionState.STEP_EXECUTION_FAILED;
    }

    public String getId() {
        return id;
    }

    public S getStep() {
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

    /**
     * This method returns true if this StepInstance is a candidate to be submitted.
     *
     * The requirements for re-submission are:
     * 1. This method has never been submitted before, OR it has failed previously and
     * the number of submissions does not exceed the retry count for this step.
     * 2. All of the dependencies of this stepInstance (StepInstances that must have
     * successfully completed prior to this StepInstance) have completed.
     * @return true if this StepInstance can be submitted.
     */
    public boolean canBeSubmitted(){
        // First, check if the state of this StepInstance allows it to be run...
        // (Not submitted or previously failed, and number of retries not exceeded)
        if (StepExecutionState.NEW_STEP_INSTANCE == getState()
                ||
            (StepExecutionState.STEP_EXECUTION_FAILED == getState() && this.getExecutions().size() < this.getStep().getRetries())){
            // Then check that all the dependencies have been completed successfully.
            if (dependsUpon != null){
                for (StepInstance dependency : dependsUpon){
                    if (dependency.getState() != StepExecutionState.STEP_EXECUTION_SUCCESSFUL){
                        return false;
                    }
                }
            }
            // All requirements met, so can submit.
            return true;
        }
        return false;
    }

    public Set<StepExecution> getExecutions() {
        return executions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StepInstance)) return false;

        StepInstance that = (StepInstance) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public StepExecution createStepExecution(){
        return new StepExecution(this);
    }


    /**
     * The format used for file names based upon integers
     * to ensure that they order correctly in the filesystem.
     */
    public static final NumberFormat TWELVE_DIGIT_INTEGER = new DecimalFormat("000000000000");

    public String filterFileNameProteinBounds(String fileNameTemplate){
        fileNameTemplate = filter(fileNameTemplate, PROTEIN_BOTTOM_HOLDER, this.bottomProtein);
        fileNameTemplate = filter(fileNameTemplate, PROTEIN_TOP_HOLDER, this.topProtein);
        fileNameTemplate = filter(fileNameTemplate, MODEL_BOTTOM_HOLDER, this.bottomModel);
        fileNameTemplate = filter(fileNameTemplate, MODEL_TOP_HOLDER, this.topModel);
        return fileNameTemplate;
    }

    private String filter(String template, String pattern, Long value){
        return (value == null)
                ? template
                : template.replaceAll(pattern, TWELVE_DIGIT_INTEGER.format(value));
    }

    public void setStep(S step) {
        if (! stepId.equals (step.getId())){
            throw new IllegalArgumentException ("Unexpected Step being set on this StepInstance - ID is incorrect.");
        }
        this.step = step;
    }
}
