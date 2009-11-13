package uk.ac.ebi.interpro.scan.management.model;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class StepInstance implements Serializable {

    private Long id;

    private Step step;

    private Long bottomProtein;

    private Long topProtein;

    private Long bottomModel;

    private Long topModel;

    private List<StepInstance> dependencies = new ArrayList<StepInstance>();

    private List<StepExecution> executions = new ArrayList<StepExecution>();

    public StepInstance(Long id, Step step) {
        this.id = id;
        this.step = step;
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
                case STEP_EXECUTION_SUCCESSFUL:
                    return exec.getState();
                default:
                    break;
            }
        }
        return StepExecutionState.STEP_EXECUTION_FAILED;
    }

    public Long getId() {
        return id;
    }

    public Step getStep() {
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

    public List<StepExecution> getExecutions() {
        return executions;
    }
}
