package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.FinaliseInitialSetupTasks;
import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.StepInstanceCreatingStep;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.*;

/**
 * Lookup match service step
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0
 */
public class FinaliseInitialSetupStep extends Step implements StepInstanceCreatingStep {

    private static final Logger LOGGER = Logger.getLogger(FinaliseInitialSetupStep.class.getName());

    FinaliseInitialSetupTasks finaliseInitialSetupTasks;

    protected Jobs jobs;
    protected StepInstanceDAO stepInstanceDAO;

     @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    @Required
    public void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO) {
        this.stepInstanceDAO = stepInstanceDAO;
    }

    @Required
    public void setFinaliseInitialSetupTasks(FinaliseInitialSetupTasks finaliseInitialSetupTasks) {
        this.finaliseInitialSetupTasks = finaliseInitialSetupTasks;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        Long bottomProtein = stepInstance.getBottomProtein();
        Long topProtein =        stepInstance.getTopProtein();
        Utilities.verboseLog(10, " FinaliseInitialSetupStep Step  (" + bottomProtein + "-" + topProtein + ") - starting ");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("FinaliseInitialSetupStep Step (" + bottomProtein + "-" + topProtein + ")");
        }

        LOGGER.debug("stepInstance: " + stepInstance);
        boolean useMatchLookupService = true;
        if (stepInstance.getParameters().containsKey(USE_MATCH_LOOKUP_SERVICE)) {
            useMatchLookupService = Boolean.parseBoolean(stepInstance.getParameters().get(USE_MATCH_LOOKUP_SERVICE));
        }

        LOGGER.debug(" useMatchLookupService Step - "  + useMatchLookupService);


        String analysisJobNames = stepInstance.getParameters().get(ANALYSIS_JOB_NAMES_KEY);
        Map<String, SignatureLibraryRelease> analysisJobMap = new HashMap<>();
        Jobs analysisJobs;
        if (analysisJobNames == null) {
            Utilities.verboseLog(20," analysisJobNames is NULL - "  + analysisJobNames);
            analysisJobs = jobs.getActiveAnalysisJobs();
            List<String> analysisJobIdList = analysisJobs.getJobIdList();
            StringBuilder analysisJobNamesBuilder = new StringBuilder();
            for (String jobName : analysisJobIdList) {
                if (analysisJobNamesBuilder.length() > 0) {
                    analysisJobNamesBuilder.append(',');
                }
                analysisJobNamesBuilder.append(jobName);
            }
            analysisJobNames = analysisJobNamesBuilder.toString();
        } else {
            Utilities.verboseLog(20," analysisJobNames is NOT NULL - "  + analysisJobNames);
            analysisJobs = jobs.subset(StringUtils.commaDelimitedListToStringArray(analysisJobNames));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("analysisJobs: " + analysisJobs);
            LOGGER.debug("analysisJobNames: " + analysisJobNames);
        }
        for (Job analysisJob : analysisJobs.getJobList()){
            SignatureLibraryRelease signatureLibraryRelease = analysisJob.getLibraryRelease();
            if(signatureLibraryRelease != null) {
                //TODO - should the name always be in upppercase
                analysisJobMap.put(signatureLibraryRelease.getLibrary().getName().toUpperCase(), signatureLibraryRelease);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Name: " + signatureLibraryRelease.getLibrary().getName() + " version: " + signatureLibraryRelease.getVersion() + " name: " + signatureLibraryRelease.getLibrary().getName());
                }
                Utilities.verboseLog(20, "Name: " + signatureLibraryRelease.getLibrary().getName() + " version: " + signatureLibraryRelease.getVersion());
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("analysisJobMap:" + analysisJobMap);
        }

        String analysesPrintOutStr = " Running the following analyses:";
        String analysesDisplayStr =  "Running the following analyses:";

        StringBuilder analysesToRun = new StringBuilder();

//                StringBuilder analysesToDisplay = new StringBuilder();
        StringJoiner analysesToDisplay = new StringJoiner(",");

        //sort the keys
        List<String> analysisJobMapKeySet = new ArrayList(analysisJobMap.keySet());
        Collections.sort(analysisJobMapKeySet);

        for (String key: analysisJobMapKeySet){
            analysesToRun.append(analysisJobMap.get(key).getLibrary().getName() + "-" + analysisJobMap.get(key));
            analysesToDisplay.add(String.join("-", analysisJobMap.get(key).getLibrary().getName(),
                    analysisJobMap.get(key).getVersion()));
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(analysesPrintOutStr + Collections.singletonList(analysisJobNames));
            LOGGER.debug(analysesDisplayStr + analysesToDisplay.toString());
        }

        //TODO check if this should be disabled
        //System.out.println(analysesDisplayStr + "[" + analysesToDisplay.toString() +"]");

        final String completionJobName = stepInstance.getParameters().get(COMPLETION_JOB_NAME_KEY);
        Job completionJob = jobs.getJobById(completionJobName);
        Utilities.verboseLog("completionJobName: " + completionJobName + " completionJob in FinaliseInitialSteps: " + completionJob);

        final String prepareOutputJobName = stepInstance.getParameters().get(PREPARE_OUTPUT_JOB_NAME_KEY);
        Job prepareOutputJob = jobs.getJobById(prepareOutputJobName);

        Utilities.verboseLog("prepareOutputJobName: " + prepareOutputJobName + " prepareOutputJob: "
                + prepareOutputJob + " prepareOutputJob in FinaliseInitialSteps: " + prepareOutputJob);

        final String matchLookupJobName = stepInstance.getParameters().get(MATCH_LOOKUP_JOB_NAME_KEY);

        Job matchLookupJob = null;

        final String finalInitialJobName = stepInstance.getParameters().get(FINALISE_INITIAL_STEPS_JOB_NAME_KEY);
        Job finalInitialJob = null;

        boolean  initialSetupSteps = false;

        StepCreationSequenceLoadListener sequenceLoadListener =
                new StepCreationSequenceLoadListener(analysisJobs, completionJob, prepareOutputJob, matchLookupJob, finalInitialJob, initialSetupSteps, stepInstance.getParameters());
        sequenceLoadListener.setStepInstanceDAO(stepInstanceDAO);

        finaliseInitialSetupTasks.execute(sequenceLoadListener, analysisJobMap,  useMatchLookupService);

        Utilities.verboseLog(10, "  FinaliseInitialSetupStep Step - done");

        //should we sleep just to make sure changes that are  made on the filesystem kvstore are available for the next step

        if (topProtein > 16000) {
            int waitTime = (topProtein.intValue() / 32000 ) * 30 * 1000;
            if(  getNfsDelayMilliseconds() < waitTime) {
                if (waitTime > 120 * 1000) {
                    //leave as it is for now, but we might need to have a cut off
                    waitTime = 120 * 1000;

                }
                Utilities.sleep(waitTime);
            }else {
                delayForNfs();
            }
            Utilities.verboseLog(10, "  FinaliseStep - Slept for at least " + waitTime + " millis");
        }else{
            Utilities.verboseLog(10, " FinaliseStep - no waiting for the kvstore matchDB as protein count is < 16000: count = " + topProtein);
        }

    }

}
