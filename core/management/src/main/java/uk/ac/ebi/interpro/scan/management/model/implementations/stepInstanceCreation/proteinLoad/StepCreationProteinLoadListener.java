package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.sequence.ProteinLoadListener;
import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Listener implementation that creates all required StepInstances
 * for the range of proteins passed in.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0
 */
public class StepCreationProteinLoadListener implements ProteinLoadListener {

    private static final Logger LOGGER = Logger.getLogger(StepCreationProteinLoadListener.class.getName());


    private StepInstanceDAO stepInstanceDAO;

    private Jobs jobs;
    private Job completionJob;
    private Map<String, String> stepParameters;


    @Required
    public void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO) {
        this.stepInstanceDAO = stepInstanceDAO;
    }

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    public void setCompletionJob(Job completionJob) {
        this.completionJob = completionJob;
    }

    public StepCreationProteinLoadListener() {
    }

    public StepCreationProteinLoadListener(Jobs analysisJobs, Job completionJob, Map<String, String> stepParameters) {
        this.stepParameters = stepParameters;
        this.jobs = analysisJobs;
        this.completionJob = completionJob;
    }


    private static Long min(Long l1, Long l2) {
        if (l1 == null && l2 == null) return null;
        if (l2 == null) return l1;
        if (l1 == null) return l2;
        return Math.min(l1, l2);
    }

    private static Long max(Long l1, Long l2) {
        if (l1 == null && l2 == null) return null;
        if (l2 == null) return l1;
        if (l1 == null) return l2;
        return Math.max(l1, l2);
    }

    @Override
    public void proteinsLoaded(Long bottomNewProteinId, Long topNewProteinId, Long bottomPrecalculatedProteinId, Long topPrecalculatedProteinId) {
        try {

            Long bottomProteinId = min(bottomNewProteinId, bottomPrecalculatedProteinId);
            Long topProteinId = max(topNewProteinId, topPrecalculatedProteinId);

            if (bottomProteinId == null || topProteinId == null) {
                return;
            }
//            if (topProteinId < bottomProteinId){
//                throw new IllegalArgumentException ("The bounds make no sense - the top bound (" + topProteinId + ") is lower than the bottom bound (" + bottomProteinId + ").  Programming error?");
//            }


            final Map<Step, List<StepInstance>> stepToStepInstances = new HashMap<Step, List<StepInstance>>();

            List<StepInstance> completionStepInstances = new ArrayList<StepInstance>();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Completion Job:" + completionJob);

                if (stepParameters != null) {
                    for (String key : stepParameters.keySet()) {
                        LOGGER.debug("setparameter:" + key + " " + stepParameters.get(key));
                    }
                }
            }

            if (completionJob != null) {
                for (Step step : completionJob.getSteps()) {
                    StepInstance stepInstance = new StepInstance(step, bottomProteinId, topProteinId, null, null);
                    stepInstance.addStepParameters(stepParameters);
                    completionStepInstances.add(stepInstance);
                }
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Completion Steps:" + completionStepInstances.size());
            }

            // Instantiate the StepInstances - no dependencies yet.
            for (Job job : jobs.getJobList()) {
                for (Step step : job.getSteps()) {
                    if (step.isCreateStepInstancesForNewProteins()) {
                        List<StepInstance> jobStepInstances = createStepInstances(step, bottomNewProteinId, topNewProteinId);
                        stepToStepInstances.put(step, jobStepInstances);
                        for (StepInstance jobStepInstance : jobStepInstances) {
                            for (StepInstance completionStepInstance : completionStepInstances) {
                                completionStepInstance.addDependentStepInstance(jobStepInstance);
                            }
                        }
                    }
                }
            }

            // Add the dependencies to the StepInstances.
            for (Step step : stepToStepInstances.keySet()) {
                for (StepInstance stepInstance : stepToStepInstances.get(step)) {
                    final List<Step> dependsUpon = stepInstance.getStep(jobs).getDependsUpon();
                    if (dependsUpon != null) {
                        for (Step stepRequired : dependsUpon) {
                            List<StepInstance> candidateStepInstances = stepToStepInstances.get(stepRequired);
                            if (candidateStepInstances != null) {
                                for (StepInstance candidate : candidateStepInstances) {
                                    if (stepInstance.proteinBoundsOverlap(candidate)) {
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
            stepInstanceDAO.insert(completionStepInstances);
        }
        catch (Exception e) {
            LOGGER.error("Exception thrown in createStepInstances() method: ", e);
            throw new IllegalStateException("Caught and logged Exception, re-thrown so things work properly.", e);
        }
    }

    /**
     * Should be private - but want to junit test as prone to boundary errors!
     *
     * @param step
     * @param bottomProteinId
     * @param topProteinId
     * @return
     */
    List<StepInstance> createStepInstances(Step step, Long bottomProteinId, Long topProteinId) {
        final List<StepInstance> stepInstances = new ArrayList<StepInstance>();
        final long sliceSize = (step.getMaxProteins() == null)
                ? topProteinId - bottomProteinId + 1
                : step.getMaxProteins();
        for (long bottom = bottomProteinId; bottom <= topProteinId; bottom += sliceSize) {
            final long top = Math.min(topProteinId, bottom + sliceSize - 1);
            stepInstances.add(new StepInstance(step, bottom, top, null, null));
        }
        return stepInstances;
    }


}
