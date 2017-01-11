package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.util.Utilities;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStep;

import javax.jms.JMSException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Gift Nuka
 * @author  Phil Jones
 */
public class SingleSeqOptimisedBlackBoxMaster extends AbstractBlackBoxMaster {

    private static final Logger LOGGER = Logger.getLogger(SingleSeqOptimisedBlackBoxMaster.class.getName());

    private StatsUtil statsUtil;

    private String runId;

    private  int timeDelay = 10;

    private  int binaryRunDelay = 999;

    Long timeSinceLastBinaryRun = System.currentTimeMillis();

    private static final int MEGA = 1024 * 1024;

    int binaryStepCount = 0;
    int nonBinaryStepCount = 0;

    @Override
    public void run() {
        final long now = System.currentTimeMillis();
        super.run();

        Utilities.verboseLog = verboseLog;
        Utilities.verboseLogLevel = verboseLogLevel;
        Utilities.mode = "singleseq";

        int runStatus = 11;

        if(verboseLog){
            System.out.println(Utilities.getTimeNow() + " DEBUG " + "inVmWorkers min:" + getConcurrentInVmWorkerCount() + " max: " + getMaxConcurrentInVmWorkerCount());
            System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
            System.out.println("Memory free: " + Runtime.getRuntime().freeMemory() / MEGA + "MB total: " + Runtime.getRuntime().totalMemory() / MEGA + "MB max: " + Runtime.getRuntime().maxMemory() / MEGA + "MB");

            //display initial memory usage
        }


        try {
            loadInMemoryDatabase();
            runStatus = 21;
            if(verboseLog){
                Utilities.verboseLog(Utilities.getTimeNow() + " Loaded in memory database ");
            }
            int stepInstancesCreatedByLoadStep = createStepInstances();
            int inputSize = 1;

            int minimumStepsExpected = getMinimumStepsExpected();
            runStatus = 31;

            // If there is an embeddedWorkerFactory (i.e. this Master is running in stand-alone mode)
            // stop running if there are no StepInstances left to complete.
            boolean controlledLogging = false;

            //allow users to specify any job to run prioritising expensive jobs (pfam, tigrfam, hamap, pirsf)
            boolean firstPass = true;

            int stepInstanceSubmitCount = 0;
            while (!shutdownCalled) {
                long totalStepInstanceCount = stepInstanceDAO.count();
                List<StepInstance> unfinishedStepInstances = stepInstanceDAO.retrieveUnfinishedStepInstances();
                int totalUnfinishedStepInstances = unfinishedStepInstances.size();
                boolean runningFirstStep = (totalStepInstanceCount == totalUnfinishedStepInstances);

                boolean completed = true;
                runStatus = 41;

                if (stepInstanceSubmitCount == 1 && firstPass && (! isUseMatchLookupService())){
                    if (verboseLog && LOGGER.isDebugEnabled()) {
                        LOGGER.debug("First steps: " + firstPass);
                        LOGGER.debug("Steps left: " + totalUnfinishedStepInstances);
                    }
                    if(! runningFirstStep){
                        for (StepInstance stepInstance : unfinishedStepInstances) {
                            runStatus = 45;
                            Utilities.verboseLog("Single Seq mode: considering :" + stepInstance.getStepId());
                            if (isHighPriorityStep(stepInstance.getStep(jobs))){
                                stepInstanceSubmitCount += submitStepInstanceToRequestQueue(stepInstance);
                                controlledLogging = false;
                            }
                        }
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Steps left after first pass: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                        }
                        firstPass = false;
                        completed = false;
                        Thread.sleep(500);
                    }
                } else {
                    int submitted = 0;
                    for (StepInstance stepInstance : unfinishedStepInstances) {
                        runStatus = 51;
                        completed &= stepInstance.haveFinished(jobs);
                        submitted = submitStepInstanceToRequestQueue(stepInstance);
                        stepInstanceSubmitCount += submitted;
                    }
                    if (submitted > 0){
                        controlledLogging = false;
                    }
                }

                // Check what is still running
                totalStepInstanceCount = stepInstanceDAO.count();
                totalUnfinishedStepInstances = stepInstanceDAO.retrieveUnfinishedStepInstances().size();

                if(!controlledLogging) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("InterProScan Master has no stepInstances ready to run yet ... but soon ");
                        LOGGER.debug("Step instances left to run: " + totalUnfinishedStepInstances);
                        LOGGER.debug("Total StepInstances: " + totalStepInstanceCount);
                    }
//                    System.out.println(Utilities.getTimeNow() + " DEBUG stepInstanceSubmitCount (2): " + stepInstanceSubmitCount);
                    controlledLogging = true;
                }
                //report progress
                statsUtil.setTotalJobs(totalStepInstanceCount);
                statsUtil.setUnfinishedJobs(totalUnfinishedStepInstances);
//                final boolean statsAvailable = statsUtil.pollStatsBrokerJobQueue();
                statsUtil.displayMasterProgress();

                // Close down (break out of loop) if the analyses are all complete.
                // The final clause checks that the protein load steps have been created so
                // i5 doesn't finish prematurely.
                if (completed
                        && totalStepInstanceCount == statsUtil.getSubmittedStepInstancesCount()
                        && statsUtil.getSubmittedStepInstancesCount() >= minimumStepsExpected
                        && totalUnfinishedStepInstances == 0
                        && totalStepInstanceCount > stepInstancesCreatedByLoadStep
                        && totalStepInstanceCount >= minimumStepsExpected) {
                    Utilities.verboseLog("stepInstanceDAO.count() " + totalStepInstanceCount
                            + " stepInstancesCreatedByLoadStep : " + stepInstancesCreatedByLoadStep
                            + " minimumStepsExpected : " + minimumStepsExpected
                            + " SubmittedStepInstancesCount : " + statsUtil.getSubmittedStepInstancesCount()
                            +  " unfinishedSteps " + totalUnfinishedStepInstances);
                    runStatus = 0;
                    break;
                }

                //for standalone es mode this should be < 200
                Thread.sleep(50);  // Make sure the Master thread is not hogging resources required by in-memory workers.
            }
            runStatus = 0;
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by SingleSeqOptimisedBlackBoxMaster: ", e);
            systemExit(999);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by SingleSeqOptimisedBlackBoxMaster: ", e);
            systemExit(999);
        }


        if (runStatus == 0) {
            System.out.println(Utilities.getTimeNow() + " 100% done:  InterProScan analyses completed");
        }else{
            LOGGER.error("InterProScan analyses failed, check log details for the errors - status " + runStatus);
        }

        if(verboseLog){
            final long executionTime =   System.currentTimeMillis() - now;
            System.out.println("Computation time (s): (" + TimeUnit.MILLISECONDS.toSeconds(executionTime)+ " s) => " + String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(executionTime),
                    TimeUnit.MILLISECONDS.toSeconds(executionTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime))
            ));
        }

        systemExit(runStatus);
    }

    /**
     * Exit InterProScan 5 immediately with the supplied exit code.
     * @param status Exit code to use
     */
    private void systemExit(int status){
        try {
            databaseCleaner.closeDatabaseCleaner();
            LOGGER.debug("Ending");
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            cleanUpWorkingDirectory();
            // Always exit
            if(status != 0){
                System.err.println("InterProScan analysis failed. Exception thrown by SingleSeqOptimisedBlackBoxMaster. Check the log file for details");
            }
            System.exit(status);
        }
        System.exit(status);
    }


    public int submitStepInstanceToRequestQueue(StepInstance stepInstance) throws Exception{
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Iterating over StepInstances: Currently on " + stepInstance);
        }
        if (stepInstance.hasFailedPermanently(jobs)) {
            unrecoverableErrorStrategy.failed(stepInstance, jobs);
        }

        if (stepInstance.canBeSubmitted(jobs) && stepInstanceDAO.serialGroupCanRun(stepInstance, jobs)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Step submitted:" + stepInstance);
            }
            String stepInstanceId = stepInstance.getStepId();
            final boolean resubmission = stepInstance.getExecutions().size() > 0;
            if (resubmission) {
                LOGGER.warn("StepInstance " + stepInstanceId + " is being re-run following a failure.");
            }
            final Step step = stepInstance.getStep(jobs);
            // Only set up message selectors for high memory requirements if a suitable worker runner has been set up.

            final boolean highPriorityStep = false; //isHighPriorityStep(step);
            final boolean lowPriorityStep = (!highPriorityStep) && (step.getSerialGroup() == null || step instanceof WriteFastaFileStep);

            // Serial groups should be high priority, however exclude WriteFastaFileStep from this
            // as they are very abundant.
            final int priority = lowPriorityStep ? 4 : 8;

            // Performed in a transaction.
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("About to send a message for StepInstance: " + stepInstance);
            }
            messageSender.sendMessage(stepInstance, false, priority, false);
            statsUtil.addToSubmittedStepInstances(stepInstance);
            return 1;
        }
        return 0;
    }
    /**
     *
     * @param statsUtil
     */
    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public void setTimeDelay(int timeDelay) {
        this.timeDelay = timeDelay;
    }

    public void setBinaryRunDelay(int binaryRunDelay) {
        this.binaryRunDelay = binaryRunDelay;
    }

    /**
     * * check if the job is hamap or prosite
     *  then assign it higher priority
     *
     * @param step
     * @return
     */
    public boolean  isHighPriorityStep(Step step){
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(" Step Id : " + step.getId());
        }
        if(step.getId().toLowerCase().contains("stepLoadFromFasta".toLowerCase())
                || step.getId().toLowerCase().contains("panther".toLowerCase())
                || step.getId().toLowerCase().contains("prositeprofiles".toLowerCase())
                || step.getId().toLowerCase().contains("pfam".toLowerCase())
                ){
            Utilities.verboseLog(" panther/prositeprofiles/pfam job: " + step.getId() + " Should have high priority");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(" panther/prositeprofiles/pfam job: " + step.getId() + " Should have high priority");
            }
            return true;
        }

        return false;
    }

}
