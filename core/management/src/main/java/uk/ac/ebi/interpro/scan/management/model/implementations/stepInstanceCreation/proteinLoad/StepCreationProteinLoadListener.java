package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.sequence.ProteinLoadListener;
import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import java.util.*;

/**
 * Listener implementation that creates all required StepInstances
 * for the range of proteins passed in.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class StepCreationProteinLoadListener implements ProteinLoadListener {


    private StepInstanceDAO stepInstanceDAO;

    private Jobs jobs;

    @Required
    public void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO) {
        this.stepInstanceDAO = stepInstanceDAO;
    }

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    /**
     * Implementations of this method will create StepInstances for the
     * range of proteins passed in as parameters.  If either of these
     * values are null, then no action is taken.
     *
     *
     * @param bottomProteinId bottom protein primary key, inclusive.
     * @param topProteinId    top protein primary key, inclusive.
     */
    @Override
    public void createStepInstances(Long bottomProteinId, Long topProteinId) {
        if (bottomProteinId == null || topProteinId == null){
            return;
        }
        if (topProteinId < bottomProteinId){
            throw new IllegalArgumentException ("The bounds make no sense - the top bound (" + topProteinId + ") is lower than the bottom bound (" + bottomProteinId + ").  Programming error?");
        }
        final Map<Step, Set<StepInstance>> stepToStepInstances = new HashMap<Step, Set<StepInstance>>();

        // Instantiate the StepInstances - no dependencies yet.
        for (Job job : jobs.getJobList()){
            for (Step step : job.getSteps()){
                if (step.isCreateStepInstancesForNewProteins()){
                    stepToStepInstances.put (step, createStepInstances(step, bottomProteinId, topProteinId));
                }
            }
        }

        // Add the dependencies to the StepInstances.
        for (Step step : stepToStepInstances.keySet()){
            for (StepInstance stepInstance : stepToStepInstances.get(step)){
                final List<Step> dependsUpon = stepInstance.getStep().getDependsUpon();
                if (dependsUpon != null){
                    for (Step stepRequired : dependsUpon){
                        Set<StepInstance> candidateStepInstances = stepToStepInstances.get(stepRequired);
                        if (candidateStepInstances != null){
                            for (StepInstance candidate : candidateStepInstances){
                                if (stepInstance.proteinBoundsOverlap(candidate)){
                                    stepInstance.addDependentStepInstance(candidate);
                                }
                            }
                        }
                    }
                }
            }
            // Persist the StepInstances that now have their dependencies added.
            stepInstanceDAO.insert(stepToStepInstances.get(step));
        }
    }

    /**
     * Should be private - but want to junit test as prone to boundary errors!
     * @param step
     * @param bottomProteinId
     * @param topProteinId
     * @return
     */
    Set<StepInstance> createStepInstances(Step step, Long bottomProteinId, Long topProteinId){
        final Set<StepInstance> stepInstances = new HashSet<StepInstance>();
        final long sliceSize = (step.getMaxProteins() == null)
                ? topProteinId - bottomProteinId + 1
                : step.getMaxProteins();
        for (long bottom = bottomProteinId; bottom <= topProteinId; bottom += sliceSize){
            final long top = Math.min(topProteinId, bottom + sliceSize - 1);
            stepInstances.add(new StepInstance(step, bottom, top, null, null));
        }
        return stepInstances;
    }

}
