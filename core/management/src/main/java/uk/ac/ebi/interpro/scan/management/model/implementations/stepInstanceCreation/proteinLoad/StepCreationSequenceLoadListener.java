package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoadListener;
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
public class StepCreationSequenceLoadListener
        extends AbstractStepInstanceCreator
        implements SequenceLoadListener {

    private static final Logger LOGGER = Logger.getLogger(StepCreationSequenceLoadListener.class.getName());


    private Job completionJob;


    public void setCompletionJob(Job completionJob) {
        this.completionJob = completionJob;
    }

    /**
     * Optional constructor for use by Spring - do not remove.
     */
    public StepCreationSequenceLoadListener() {
    }

    public StepCreationSequenceLoadListener(Jobs analysisJobs, Job completionJob, Map<String, String> parameters) {
        this.parameters = parameters;
        this.jobs = analysisJobs;
        this.completionJob = completionJob;
    }

    public StepCreationSequenceLoadListener(Jobs analysisJobs,  Map<String, String> parameters) {
        this.parameters = parameters;
        this.jobs = analysisJobs;
        this.completionJob = null;
    }

    @Override
    @Transactional
    public void sequencesLoaded(final Long bottomNewSequenceId, final Long topNewSequenceId,
                                final Long bottomPrecalculatedSequenceId, final Long topPrecalculatedSequenceId) {
        try {

            // These two variables capture the FULL RANGE of proteins that have been inserted into the database,
            // irrespective of whether they are new or precalculated.
            // This allows the whole range to be included in the 'completion job'.
            //TODO - Check this is correct behaviour - the full range may include proteins not intended to be included?
            final Long bottomProteinId = min(bottomNewSequenceId, bottomPrecalculatedSequenceId);
            final Long topProteinId = max(topNewSequenceId, topPrecalculatedSequenceId);

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

                LOGGER.debug("Range of protein database IDs for which analysis StepInstances need to be created: " + bottomNewSequenceId + " - " + topNewSequenceId);
                LOGGER.debug("Range of protein database IDs for which NO StepInstances need to be created: " + bottomPrecalculatedSequenceId + " - " + topPrecalculatedSequenceId);
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
            if (bottomNewSequenceId != null && topNewSequenceId != null) {
                // Instantiate the StepInstances - no dependencies yet.
                for (Job job : jobs.getJobList()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Job for which StepInstances are being created: " + job.getId());
                    }
                    for (Step step : job.getSteps()) {
                        if (step.isCreateStepInstancesForNewProteins()) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Creating StepInstance for step " + step.getId() + " protein range " + bottomNewSequenceId + " - " + topNewSequenceId);
                            }
                            final List<StepInstance> jobStepInstances = createStepInstances(step, bottomNewSequenceId, topNewSequenceId);
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

            //TODO: Quick fix solution, which allows to run analysis locally for analysis which aren't integrated in the lookup service or are licensed
            //At the moment these are SignalP, TMHMM, Coils and  Phobius
            if (bottomPrecalculatedSequenceId != null && topPrecalculatedSequenceId != null) {
                // Instantiate the StepInstances - no dependencies yet.
                for (Job job : jobs.getJobList()) {
                    //Only create new step instances for analysis which aren't integrated in the lookup service
                    //These jobs are flagged with 'doRunLocally'=TRUE
                    if (job.isDoRunLocally()) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Job for which StepInstances are being created: " + job.getId());
                        }
                        for (Step step : job.getSteps()) {
                            if (step.isCreateStepInstancesForNewProteins()) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Creating StepInstance for step " + step.getId() + " protein range " + bottomPrecalculatedSequenceId + " - " + topPrecalculatedSequenceId);
                                }
                                final List<StepInstance> jobStepInstances = createStepInstances(step, bottomPrecalculatedSequenceId, topPrecalculatedSequenceId);
                                stepToStepInstances.put(step, jobStepInstances);
                                for (StepInstance jobStepInstance : jobStepInstances) {
                                    for (StepInstance completionStepInstance : completionStepInstances) {
                                        completionStepInstance.addDependentStepInstance(jobStepInstance);
                                    }
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
