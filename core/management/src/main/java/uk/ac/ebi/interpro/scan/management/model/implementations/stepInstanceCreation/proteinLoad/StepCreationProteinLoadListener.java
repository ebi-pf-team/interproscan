package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.business.sequence.ProteinLoadListener;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.AbstractStepInstanceCreator;

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
public class StepCreationProteinLoadListener
        extends AbstractStepInstanceCreator
        implements ProteinLoadListener {

    private static final Logger LOGGER = Logger.getLogger(StepCreationProteinLoadListener.class.getName());


    private Job completionJob;


    public void setCompletionJob(Job completionJob) {
        this.completionJob = completionJob;
    }

    /**
     * Optional constructor for use by Spring - do not remove.
     */
    public StepCreationProteinLoadListener() {
    }

    public StepCreationProteinLoadListener(Jobs analysisJobs, Job completionJob, Map<String, String> parameters) {
        this.parameters = parameters;
        this.jobs = analysisJobs;
        this.completionJob = completionJob;
    }


    @Override
    public void proteinsLoaded(final Long bottomNewProteinId, final Long topNewProteinId,
                               final Long bottomPrecalculatedProteinId, final Long topPrecalculatedProteinId) {
        try {

            // These two variables capture the FULL RANGE of proteins that have been inserted into the database,
            // irrespective of whether they are new or precalculated.
            // This allows the whole range to be included in the 'completion job'.
            //TODO - Check this is correct behaviour - the full range may include proteins not intended to be included?
            final Long bottomProteinId = min(bottomNewProteinId, bottomPrecalculatedProteinId);
            final Long topProteinId = max(topNewProteinId, topPrecalculatedProteinId);

            if (bottomProteinId == null || topProteinId == null) {
                return;
            }
//            if (topProteinId < bottomProteinId){
//                throw new IllegalArgumentException ("The bounds make no sense - the top bound (" + topProteinId + ") is lower than the bottom bound (" + bottomProteinId + ").  Programming error?");
//            }


            final Map<Step, List<StepInstance>> stepToStepInstances = new HashMap<Step, List<StepInstance>>();

            final List<StepInstance> completionStepInstances = new ArrayList<StepInstance>();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Completion Job:" + completionJob);

                if (parameters != null) {
                    for (String key : parameters.keySet()) {
                        LOGGER.debug("setparameter:" + key + " " + parameters.get(key));
                    }
                }
            }

            if (completionJob != null) {
                for (Step step : completionJob.getSteps()) {
                    StepInstance stepInstance = new StepInstance(step, bottomProteinId, topProteinId, null, null);
                    stepInstance.addParameters(parameters);
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
                        final List<StepInstance> jobStepInstances = createStepInstances(step, bottomNewProteinId, topNewProteinId);
                        stepToStepInstances.put(step, jobStepInstances);
                        for (StepInstance jobStepInstance : jobStepInstances) {
                            for (StepInstance completionStepInstance : completionStepInstances) {
                                completionStepInstance.addDependentStepInstance(jobStepInstance);
                            }
                        }
                    }
                }
            }

            addDependenciesAndStore(stepToStepInstances);

            stepInstanceDAO.insert(completionStepInstances);
        } catch (Exception e) {
            LOGGER.error("Exception thrown in createStepInstances() method: ", e);
            throw new IllegalStateException("Caught and logged Exception, re-thrown so things work properly.", e);
        }
    }
}
