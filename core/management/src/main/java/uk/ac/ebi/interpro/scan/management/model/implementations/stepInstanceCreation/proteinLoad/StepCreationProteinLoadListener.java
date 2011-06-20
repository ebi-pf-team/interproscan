package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
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
                LOGGER.debug("Appear to be no proteins being analysed in this process - but this may be a bug.");
                return;
            }

            final Map<Step, List<StepInstance>> stepToStepInstances = new HashMap<Step, List<StepInstance>>();

            final List<StepInstance> completionStepInstances = new ArrayList<StepInstance>();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Completion Job:" + completionJob);

                if (parameters != null) {
                    for (String key : parameters.keySet()) {
                        LOGGER.debug("setparameter:" + key + " " + parameters.get(key));
                    }
                }

                LOGGER.debug("Range of protein database IDs for which analysis StepInstances need to be created: " + bottomNewProteinId + " - " + topNewProteinId);
                LOGGER.debug("Range of protein database IDs for which NO StepInstances need to be created: " + bottomPrecalculatedProteinId + " - " + topPrecalculatedProteinId);
                LOGGER.debug("Range of protein database IDs for which the COMPLETION StepInstances need to be created: " + bottomProteinId + " - " + topProteinId);
            }

            if (completionJob != null) {
                LOGGER.debug("Have a completion Job.");
                for (Step step : completionJob.getSteps()) {
                    StepInstance stepInstance = new StepInstance(step, bottomProteinId, topProteinId, null, null);
                    stepInstance.addParameters(parameters);
                    completionStepInstances.add(stepInstance);
                }
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Completion Steps:" + completionStepInstances.size());
            }
            if (bottomNewProteinId != null && topNewProteinId != null) {
                // Instantiate the StepInstances - no dependencies yet.
                for (Job job : jobs.getJobList()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Job for which StepInstances are being created: " + job.getId());
                    }
                    for (Step step : job.getSteps()) {
                        if (step.isCreateStepInstancesForNewProteins()) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Creating StepInstance for step " + step.getId() + " protein range " + bottomNewProteinId + " - " + topNewProteinId);
                            }
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
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Storing Completion StepInstances");
            }
            stepInstanceDAO.insert(completionStepInstances);
            stepInstanceDAO.flush();
        } catch (Exception e) {
            LOGGER.error("Exception thrown in createStepInstances() method: ", e);
            throw new IllegalStateException("Caught and logged Exception, re-thrown so things work properly.", e);
        }
    }
}
