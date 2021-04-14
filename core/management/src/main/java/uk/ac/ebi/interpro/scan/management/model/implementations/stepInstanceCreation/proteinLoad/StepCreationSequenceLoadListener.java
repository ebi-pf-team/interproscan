package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.business.sequence.SequenceLoadListener;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.AbstractStepInstanceCreator;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.StepInstanceCreatingStep;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.util.Utilities;

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

    private static final Logger LOGGER = LogManager.getLogger(StepCreationSequenceLoadListener.class.getName());


    private Job completionJob;
    private Job prepareOutputJob;
    private Job matchLookupJob;
    private Job finaliseInitialStepsJob;
    private boolean initialSetupSteps;
    private int maxConcurrentThreadsForPrepareOutputStep = 1;

    public void setCompletionJob(Job completionJob) {
        this.completionJob = completionJob;
    }

    public void setPrepareOutputJob(Job prepareOutputJob) {
        this.prepareOutputJob = prepareOutputJob;
    }

    public void setMatchLookupJob(Job matchLookupJob) {
        this.matchLookupJob = matchLookupJob;
    }

    public void setFinaliseInitialStepsJob(Job finaliseInitialStepsJob) {
        this.finaliseInitialStepsJob = finaliseInitialStepsJob;
    }

    public void setInitialSetupSteps(boolean initialSetupSteps) {
        this.initialSetupSteps = initialSetupSteps;
    }

    public void setMaxConcurrentThreadsForPrepareOutputStep(int maxConcurrentThreadsForPrepareOutputStep) {
        this.maxConcurrentThreadsForPrepareOutputStep = maxConcurrentThreadsForPrepareOutputStep;
    }

    /**
     * Optional constructor for use by Spring - do not remove.
     */
    public StepCreationSequenceLoadListener() {
    }

    public StepCreationSequenceLoadListener(Jobs analysisJobs, Job completionJob, Job prepareOutputJob, Job matchLookupJob, Job finaliseInitialStepsJob, boolean initialSetupSteps, Map<String, String> parameters) {
        this.parameters = parameters;
        this.jobs = analysisJobs;
        this.completionJob = completionJob;
        this.prepareOutputJob = prepareOutputJob;
        this.matchLookupJob = matchLookupJob;
        this.finaliseInitialStepsJob = finaliseInitialStepsJob;
        this.initialSetupSteps = initialSetupSteps;
    }

    public StepCreationSequenceLoadListener(Jobs analysisJobs, Map<String, String> parameters) {
        this.parameters = parameters;
        this.jobs = analysisJobs;
        this.completionJob = null;
        this.prepareOutputJob = null;
        this.matchLookupJob = null;
        this.finaliseInitialStepsJob = null;
        this.initialSetupSteps = false;
    }

    @Override
    @Transactional
    public void sequencesLoaded(final Long bottomNewSequenceId, final Long topNewSequenceId,
                                final Long bottomPrecalculatedSequenceId, final Long topPrecalculatedSequenceId, boolean useMatchLookupService, List<Long> idsWithoutLookupHit) {
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

            final List<StepInstance> prepareOutputStepInstances = new ArrayList<StepInstance>();

            Utilities.verboseLog(1100, "Range of protein database IDs for which analysis StepInstances need to be created: " + bottomNewSequenceId + " - " + topNewSequenceId);
            Utilities.verboseLog(1100, "Range of protein database IDs for which NO StepInstances need to be created: " + bottomPrecalculatedSequenceId + " - " + topPrecalculatedSequenceId);
            Utilities.verboseLog(1100, "Range of protein database IDs for which the COMPLETION StepInstances need to be created: " + bottomProteinId + " - " + topProteinId);
            int idsWithoutLookupHitSize = 0;
            if (idsWithoutLookupHit != null) {
                idsWithoutLookupHitSize = idsWithoutLookupHit.size();
                Utilities.verboseLog(30, "Protein without Lookup Hit (" + idsWithoutLookupHit.size() + ") range: " + idsWithoutLookupHit.get(0) + " - "
                        + idsWithoutLookupHit.get(idsWithoutLookupHitSize - 1));
            } else {
                Utilities.verboseLog(30, "idsWithoutLookupHit is NULL");
            }

            Utilities.verboseLog(120, "topProteinId intValue(): - " + topProteinId.intValue());
            int percentageOfProteinsinLookup = (topProteinId.intValue() - idsWithoutLookupHitSize) * 100 / topProteinId.intValue();

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

            int workerNumber = Integer.parseInt(this.parameters.get(StepInstanceCreatingStep.WORKER_NUMBER_KEY));

            if (completionJob != null && !initialSetupSteps) {
                LOGGER.debug("We have a completion Job.");
                Utilities.verboseLog(1100, "Have a completionJob Job: " + completionJob);

                if(useMatchLookupService && topProteinId.intValue() > 40000){
                    //System.out.println("Lookup match service was used: ");
                    String extraLookupMessage = "This information can help you decide whether to use the lookup match service for your kind of input or switch the lookup match service off.";
                    if (percentageOfProteinsinLookup < 25 ) {
                        // extraLookupMessage = "Check www.ebi.ac.uk/interpro to see if the lookup up is efficient for your kind of input";
                        Utilities.verboseLog(10, " Match lookup info: " + percentageOfProteinsinLookup + "% of the " + topProteinId.intValue()
                                + " unique input sequences were found in the match lookup server. " + extraLookupMessage);
                    }
                }
                Utilities.verboseLog(10, " Match lookup info: " + percentageOfProteinsinLookup + "% of the " + topProteinId.intValue()
                        + " unique input sequences were found in the match lookup server. " );

                //TODO this is temp for now

                if (prepareOutputJob != null) {
                    Utilities.verboseLog(1100, "Have a PrepareOutputJob Job :" + prepareOutputJob);
                    //round this number to nearest thousand
                    int rawMaxProteins = (int) (topProteinId / workerNumber);

                    if (rawMaxProteins < 1) {
                        Utilities.verboseLog(120, "rawMaxProteins <= 1, rawMaxProteins for matchLookup:- " + rawMaxProteins); //info
                        rawMaxProteins = 1;
                    }
                    int maxProteins = (int) (Math.ceil(rawMaxProteins / 400.0) * 400);

                    if (maxConcurrentThreadsForPrepareOutputStep == 1) {
                        maxProteins = topProteinId.intValue();
                    }
                    Utilities.verboseLog(30, "workerNumber =  " + workerNumber + ", maxProteins for matchLookup:- " + maxProteins);
                    Utilities.verboseLog(1100, "Create prepare output jobs for this run ...");

                    for (Step step : prepareOutputJob.getSteps()) {
                        //StepInstance stepInstance = new StepInstance(step, bottomProteinId, topProteinId, null, null);
                        step.setMaxProteins(maxProteins);

                        final List<StepInstance> jobStepInstances = createStepInstances(step, bottomProteinId, topProteinId);
                        for (StepInstance jobStepInstance : jobStepInstances) {
//                        if(jobStepInstance.getParameters() == null){
//                            LOGGER.warn("MatchLookup Parameters is NULL : " );
//                        }
                            jobStepInstance.addParameters(parameters);
//                        LOGGER.warn("MatchLookup Parameters: " + jobStepInstance.getParameters().toString());
                        }
                        stepToStepInstances.put(step, jobStepInstances);
                        prepareOutputStepInstances.addAll(jobStepInstances);
                        Utilities.verboseLog(30, "Created " + prepareOutputStepInstances.size() + " prepareOutput StepInstances" +
                                " idsWithoutLookupHitSize: " + idsWithoutLookupHitSize );
                        Utilities.verboseLog(30, "idsWithoutLookupHitSize: " + idsWithoutLookupHitSize);
                    }
                    Utilities.prepareOutputInstances = prepareOutputStepInstances.size();
                } else {
                    Utilities.verboseLog(120, "PrepareOutputJob Job is NULL ");
                }


                for (Step step : completionJob.getSteps()) {
                    StepInstance stepInstance = new StepInstance(step, bottomProteinId, topProteinId, null, null);
                    stepInstance.addParameters(parameters);
                    completionStepInstances.add(stepInstance);
                }
                //add dependencies
                for (StepInstance jobStepInstance : prepareOutputStepInstances) {
                    for (StepInstance completionStepInstance : completionStepInstances) {
                        completionStepInstance.addDependentStepInstance(jobStepInstance);
                    }
                }


            }

            // We will always have the FiniliseStepsJobs
            final List<StepInstance> initialSetupStepInstances = new ArrayList<StepInstance>();

            if (matchLookupJob != null) {
                LOGGER.debug("Have a matchLookupJob Job.");
                Utilities.verboseLog(1100, "Have a matchLookupJob Job: " + matchLookupJob);

               //TODO to remove after the memory leak is fixed
//                if (workerNumber > 8){
//                    //until memoryleak is fixed restrict this to 8
                    Utilities.verboseLog(30, "Original workerNumber =  " + workerNumber + " for " + topProteinId + " proteins" );
//                    workerNumber = 8;
//                }

                int rawMaxProteins = (int) (topProteinId / workerNumber);

                if (rawMaxProteins < 1) {
                    Utilities.verboseLog(120, "rawMaxProteins <= 1, rawMaxProteins for matchLookup:- " + rawMaxProteins);
                    rawMaxProteins = 1;
                }
                //round this number to nearest 100
                int maxProteins = (int) (Math.ceil(rawMaxProteins / 100.0) * 100); // to the nearest 100
                Utilities.verboseLog(30, "workerNumber =  " + workerNumber + ", maxProteins for matchLookup:- " + maxProteins);
                for (Step step : matchLookupJob.getSteps()) {
                    //StepInstance stepInstance = new StepInstance(step, bottomProteinId, topProteinId, null, null);
                    step.setMaxProteins(maxProteins);

                    final List<StepInstance> jobStepInstances = createStepInstances(step, bottomProteinId, topProteinId);
                    for (StepInstance jobStepInstance : jobStepInstances) {
//                        if(jobStepInstance.getParameters() == null){
//                            LOGGER.warn("MatchLookup Parameters is NULL : " );
//                        }
                        jobStepInstance.addParameters(parameters);
//                        LOGGER.warn("MatchLookup Parameters: " + jobStepInstance.getParameters().toString());
                    }
                    stepToStepInstances.put(step, jobStepInstances);
                    initialSetupStepInstances.addAll(jobStepInstances);
                }
            } else {
                Utilities.verboseLog(120, "matchLookupJob is null.");
            }

            if (finaliseInitialStepsJob != null) {
                Utilities.verboseLog(120, "Have a finaliseInitialStepsJob Job: " + finaliseInitialStepsJob);
                for (Step step : finaliseInitialStepsJob.getSteps()) {
                    //StepInstance stepInstance = new StepInstance(step, bottomProteinId, topProteinId, null, null);
                    Utilities.verboseLog(120, "step : " + step.getMaxProteins());
                    Utilities.verboseLog(120, "step max proteins: " + step);
//                    final List<StepInstance> jobStepInstances = createStepInstances(step, bottomProteinId, topProteinId);
                    StepInstance jobStepInstance = new StepInstance(step, bottomProteinId, topProteinId, null, null);
                    jobStepInstance.addParameters(parameters);
//                    LOGGER.warn("FinaliseInitialStepsJob Parameters: " + parameters.toString());

                    Utilities.verboseLog(120, "step max proteins: " + step.getMaxProteins());
                    Utilities.verboseLog(120, "finaliseInitialStepsJob stepInstance " + jobStepInstance);
                    List<StepInstance> finaliseInitialStepsSingletonStepInstance = new ArrayList<>();
                    finaliseInitialStepsSingletonStepInstance.add(jobStepInstance);
                    stepToStepInstances.put(step, finaliseInitialStepsSingletonStepInstance);
                    initialSetupStepInstances.add(jobStepInstance);
                }
                Utilities.verboseLog(120, "initialSetupStepInstances:- " + initialSetupStepInstances);
                for (StepInstance stepInstance : initialSetupStepInstances) {
                    Utilities.verboseLog(120, "stepId: " + stepInstance.getStepId());
                }
            } else {
                Utilities.verboseLog(120, "finaliseInitialStepsJob is null.");
            }

            Utilities.verboseLog(120, "initialSetupStepInstances Steps: " + initialSetupStepInstances.size());

            if (!stepToStepInstances.isEmpty()) {
                addDependenciesAndStore(stepToStepInstances);
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Completion Steps:" + completionStepInstances.size());
            }

            if (initialSetupSteps) {
                stepInstanceDAO.flush();
                return;
            }
            //else
            Utilities.verboseLog(10, "Now create StepInstances for the regular jobs ... : useMatchLookupService: " + useMatchLookupService
                    + " idsWithoutLookupHit: " + (idsWithoutLookupHit != null));

            Utilities.verboseLog(1100, "Range of protein database IDs for which analysis StepInstances need to be created: " + bottomNewSequenceId + " - " + topNewSequenceId);
            Utilities.verboseLog(1100, "Range of protein database IDs for which NO StepInstances need to be created: " + bottomPrecalculatedSequenceId + " - " + topPrecalculatedSequenceId);
            Utilities.verboseLog(1100, "Range of protein database IDs for which the COMPLETION StepInstances need to be created: " + bottomProteinId + " - " + topProteinId);

            Utilities.verboseLog(10, "bottomProteinId : " + bottomProteinId + " topProteinId : " + topProteinId  +
                   " bottomNewSequenceId: " + bottomNewSequenceId + " topNewSequenceId: " +  topNewSequenceId +
                   " bottomPrecalculatedSequenceId: " + bottomPrecalculatedSequenceId + " topPrecalculatedSequenceId: " + topPrecalculatedSequenceId);
            double analysisMaxCountMultiplier = 1;
            if (bottomNewSequenceId != null && topNewSequenceId != null) {
                if (!useMatchLookupService) {
                    Utilities.verboseLog(120, "Not useMatchLookupService  so create jobs for all analyses.");

                }
                int newSlicePercentage = 0;
                if (idsWithoutLookupHit != null) {
                    Utilities.verboseLog(110, "idsWithoutLookupHit is NOT NULL, so create jobs for all analyses.");
                    int idsWithoutLookupHitCount = idsWithoutLookupHit.size();
                    int percentageOfProteinsinLookupRounded = 0;
                    int allProteinCount = topProteinId.intValue();
                    percentageOfProteinsinLookupRounded = (percentageOfProteinsinLookup / 10) * 10;
                    int percentageOfProteinsNotinLookup = 100 - percentageOfProteinsinLookupRounded;

                    if (percentageOfProteinsNotinLookup < 80) {
                        newSlicePercentage = 100 + percentageOfProteinsinLookupRounded - 20; //arbitrary figure
                    } else {
                        newSlicePercentage = 100;
                    }

                    if (idsWithoutLookupHitSize <= 0) {
                        LOGGER.error("Ids without lookup hist count <= 0, something went wrong or there are no sequences to calculate locally");
                    } else {
                        Utilities.verboseLog(120,"analysisMaxCountMultiplier :  topProteinId " + topProteinId + "/ " + idsWithoutLookupHitSize );
                        analysisMaxCountMultiplier = round(topProteinId.doubleValue() / Double.valueOf(idsWithoutLookupHitSize), 1);
                        Utilities.verboseLog(120,"analysisMaxCountMultiplier non rounded :  " + (topProteinId.doubleValue() / Double.valueOf(idsWithoutLookupHitSize)) );
                    }

                    //TODO remove this temp test
                    //analysisMaxCountMultiplier = 1.1; for testing purposes

                    Utilities.verboseLog(120, "newSlicePercentage :  " + newSlicePercentage);
                    Utilities.verboseLog(120, "percentageOfProteinsNotinLookup :  " + percentageOfProteinsNotinLookup);
                    Utilities.verboseLog(120,"analysisMaxCountMultiplier :  " + analysisMaxCountMultiplier + " idsWithoutLookupHitSize: " + idsWithoutLookupHitSize);
                }

                Utilities.verboseLog(30, "Loop through the list of jobs  :  " + jobs.getJobList().size());
                Utilities.verboseLog(30, "Loop through the list of jobs verbose  :  " + jobs.getJobList().toString());
                for (Job job : jobs.getJobList()) {
                    //Only create new step instances for analysis which aren't integrated in the lookup service
                    //These jobs are flagged with 'doRunLocally'=TRUE
                    //or when we have idsWithoutLookupHit
                    //or when we are not using the lookup service

                    Utilities.verboseLog(30, "Considering " + job.getLibraryRelease().getLibrary().getName() + " do runlocally: " +
                            job.isDoRunLocally());
                    if (job.isDoRunLocally() || idsWithoutLookupHit != null || (!useMatchLookupService)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Job for which StepInstances are being created: " + job.getId());
                        }

                        Utilities.verboseLog(30, "if job.isDoRunLocally() || idsWithoutLookupHit != null || (!useMatchLookupService) : " + job.getId());
                        Utilities.verboseLog(1100, "Job for which StepInstances are being created: " + job.getId());
                        for (Step step : job.getSteps()) {
                            if (step.isCreateStepInstancesForNewProteins()) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Creating StepInstance for step " + step.getId() + " protein range " + bottomNewSequenceId + " - " + topNewSequenceId);
                                }
                                Utilities.verboseLog(1100, "Creating StepInstance for step " + step.getId() + " protein range " + bottomNewSequenceId + " - " + topNewSequenceId);
                                step.setUseMatchLookupService(useMatchLookupService);
                                Utilities.verboseLog(120, "Old protein slice : " + step.getMaxProteins());
                                int maxProteins = step.getMaxProteins() * newSlicePercentage / 100;
                                Utilities.verboseLog(120, "New protein slice : " + step.getMaxProteins() * newSlicePercentage / 100);


                                Utilities.verboseLog(120, "analysisMaxCountMultiplier : " + analysisMaxCountMultiplier);
                                Double newMaxProteinsDouble = (step.getMaxProteins() * analysisMaxCountMultiplier);
                                int newMaxProteins = newMaxProteinsDouble.intValue();
                                Utilities.verboseLog(120, "newMaxProteins : " + newMaxProteins);
                                boolean changeMaxProteins = false;
                                if ((!job.isDoRunLocally()) && (useMatchLookupService && idsWithoutLookupHit != null)) {
                                    Utilities.verboseLog(120, "New protein slice : " + newSlicePercentage + ", newMaxProteins: " + newMaxProteins);
                                    //if (newSlicePercentage >= 120 && newMaxProteins >= step.getMaxProteins()) {
                                    if (newMaxProteins >= step.getMaxProteins()) {
                                        changeMaxProteins = true;
                                        Utilities.verboseLog(140, "newMaxProteins :" + newMaxProteins);
                                        step.setMaxProteins(newMaxProteins);
                                    }
                                }
                                if (!changeMaxProteins) {
                                    Utilities.verboseLog(120, "newMaxProteins NOT changed as not all conditions were met ");
                                }
                                final List<StepInstance> jobStepInstances = createStepInstances(step, bottomNewSequenceId, topNewSequenceId);
                                stepToStepInstances.put(step, jobStepInstances);
                                for (StepInstance jobStepInstance : jobStepInstances) {
//                                    for (StepInstance completionStepInstance : completionStepInstances) {
//                                        completionStepInstance.addDependentStepInstance(jobStepInstance);
//                                    }
                                    for (StepInstance prepareOutputStepInstance : prepareOutputStepInstances) {
                                        prepareOutputStepInstance.addDependentStepInstance(jobStepInstance);
                                    }
                                }
                            }
                        }
                    }
                }
                if (stepToStepInstances.isEmpty()) {
                    Utilities.verboseLog(1100, "stepToStepInstances is emnpty");
                    //return; //is there anything else to do??
                } else {
                    Utilities.verboseLog(1100, "stepToStepInstances  size:" + stepToStepInstances.size());
                }
                LOGGER.debug("1. stepToStepInstances.keySet() size:" + stepToStepInstances.keySet().size());
                Utilities.verboseLog(1100, "1. stepToStepInstances.keySet() size:" + stepToStepInstances.keySet().size());
                addDependenciesAndStore(stepToStepInstances);
            }
            /* old way
            if (bottomNewSequenceId != null && topNewSequenceId != null) {
                // Instantiate the StepInstances - no dependencies yet.
                for (Job job : jobs.getJobList()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Job for which StepInstances are being created: " + job.getId());
                    }
                    Utilities.verboseLog(1100, "Job for which StepInstances are being created: " + job.getId());
                    for (Step step : job.getSteps()) {
                        if (step.isCreateStepInstancesForNewProteins()) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Creating StepInstance for step " + step.getId() + " protein range " + bottomNewSequenceId + " - " + topNewSequenceId);
                            }
                            Utilities.verboseLog(1100, "Creating StepInstance for step " + step.getId() + " protein range " + bottomNewSequenceId + " - " + topNewSequenceId);
                            final long newSliceSize = getNewSliceSize(bottomProteinId,  topProteinId, step.getMaxProteins());

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
                if(stepToStepInstances.isEmpty()){
                    Utilities.verboseLog(1100, "stepToStepInstances is emnpty");
                    //return; //is there anything else to do??
                }else{
                    Utilities.verboseLog(1100, "stepToStepInstances  size:" + stepToStepInstances.size());
                }
                LOGGER.debug("1. stepToStepInstances.keySet() size:" + stepToStepInstances.keySet().size());
                Utilities.verboseLog(1100, "1. stepToStepInstances.keySet() size:" + stepToStepInstances.keySet().size());
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
            */
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Storing Completion StepInstances");
            }
            Utilities.verboseLog(1100, "Storing Completion StepInstances");
            stepInstanceDAO.insert(completionStepInstances);
            stepInstanceDAO.flush();
        } catch (Exception e) {
            LOGGER.error("Exception thrown in createStepInstances() method: ", e);
            throw new IllegalStateException("Caught and logged Exception, re-thrown so things work properly.", e);
        }
    }

    private long getNewSliceSize(long bottomProteinId, long topProteinId, long maxProteins) {
        long newSlice = 1l;

        return newSlice;
    }

    private boolean checkSignalPSequenceCounts(int sliceSize, long topProteinId) {
        int count = 0;
        long bottomProteinId = 1;

        for (long bottom = bottomProteinId; bottom <= topProteinId; bottom += sliceSize) {
            final long top = Math.min(topProteinId, bottom + sliceSize - 1);

            for (long proteinId = bottom; proteinId <= top; proteinId++) {
                //Protein proteinNotInLookup = proteinDAO.getProteinNotInLookup(Long.toString(proteinId));
                Protein proteinNotInLookup = null;
                if (proteinNotInLookup != null) {
                    count++;
                }
            }
        }

        return true;
    }

    /**
     * round a double to a specified precision
     *
     * @param value
     * @param precision
     * @return
     */
    private static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}
