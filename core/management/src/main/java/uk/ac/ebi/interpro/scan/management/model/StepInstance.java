package uk.ac.ebi.interpro.scan.management.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.io.Serializable;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public abstract class StepInstance<S extends Step, E extends StepExecution> implements Serializable {

    private static final String PROTEIN_BOTTOM_HOLDER = "\\[PROTSTART\\]";

    private static final String PROTEIN_TOP_HOLDER = "\\[PROTEND\\]";

    private static final String MODEL_BOTTOM_HOLDER = "\\[MODSTART\\]";

    private static final String MODEL_TOP_HOLDER = "\\[MODEND\\]";

    private String id;

    private S step;

    private Long bottomProtein;

    private Long topProtein;

    private Long bottomModel;

    private Long topModel;

    private List<StepInstance> dependencies = new ArrayList<StepInstance>();

    /**
     * List of all the executions of this StepInstance.
     * Set to transient so they don't all get shoved over the
     * wire when submitting new StepExecutions to the messaging system.
     */
    private transient List<E> executions = new ArrayList<E>();

    public StepInstance(UUID id, S step) {
        this.id = id.toString();
        this.step = step;
    }

    public StepInstance(UUID id, S step, long bottomProteinId, long topProteinId) {
        this.id = id.toString();
        this.step = step;
        this.bottomProtein = bottomProteinId;
        this.topProtein = topProteinId;
    }

    public void setBottomProtein(Long bottomProtein) {
        this.bottomProtein = bottomProtein;
    }

    public void setTopProtein(Long topProtein) {
        this.topProtein = topProtein;
    }

    public void setBottomModel(Long bottomModel) {
        this.bottomModel = bottomModel;
    }

    public void setTopModel(Long topModel) {
        this.topModel = topModel;
    }

    public void addDependentStepInstance(StepInstance dependentStepInstance){
        this.dependencies.add (dependentStepInstance);
    }

    public void addStepExecution(E stepExecution){
        // Sanity check
        for (E previousExecutions : executions){
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
        for (E exec : executions){
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
        return dependencies;
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
            if (dependencies != null){
                for (StepInstance dependency : dependencies){
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

    public List<E> getExecutions() {
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

    public abstract E createStepExecution();

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
