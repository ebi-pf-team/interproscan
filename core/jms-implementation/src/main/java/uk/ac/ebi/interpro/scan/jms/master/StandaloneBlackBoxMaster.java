package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.management.model.SerialGroup;
import uk.ac.ebi.interpro.scan.management.model.implementations.RunBinaryStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.StepInstanceCreatingStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteOutputStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad.FinaliseInitialSetupStep;
import uk.ac.ebi.interpro.scan.util.Utilities;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.prosite.RunPsScanStep;

import javax.jms.JMSException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 */
public class StandaloneBlackBoxMaster extends AbstractBlackBoxMaster {

    private static final Logger LOGGER = LogManager.getLogger(StandaloneBlackBoxMaster.class.getName());

    Double pantherBinaryControlFactor = 1.0;

    private StatsUtil statsUtil;

    private String runId;

    private DefaultMessageListenerContainer workerQueueJmsContainer;

    private static final int MEGA = 1024 * 1024;

    public StandaloneBlackBoxMaster(DefaultMessageListenerContainer workerQueueJmsContainer) {
        this.workerQueueJmsContainer = workerQueueJmsContainer;
    }

    @Override
    public void run() {
        final long now = System.currentTimeMillis();
        super.run();

        Utilities.verboseLog = verboseLog;
        Utilities.verboseLogLevel = verboseLogLevel;
        Utilities.periodicGCCall = periodicGCCall;

        Utilities.verboseLog(20, " verboseLog: " + Utilities.verboseLog
                + " verboseLogLevel: " + Utilities.verboseLogLevel);

        if (runId == null) {
            String[] fileTokens = getWorkingTemporaryDirectoryPath().split("\\/");
            this.runId = fileTokens[fileTokens.length - 1];
        }
        System.out.println(Utilities.getTimeNow() + " RunID: " + runId);

        int runStatus = 11;
        if (verboseLogLevel >= 110) {
            Utilities.verboseLog(110, "DEBUG " + "Available processors: " + Runtime.getRuntime().availableProcessors());
            Utilities.verboseLog(110, "DEBUG " + "Memory free: " + Runtime.getRuntime().freeMemory() / MEGA + "MB total: " + Runtime.getRuntime().totalMemory() / MEGA + "MB max: " + Runtime.getRuntime().maxMemory() / MEGA + "MB");

            System.out.println(Utilities.getTimeNow() + " verboseLog: " + verboseLog + " verboseLogLevel: " + verboseLogLevel);
            System.out.println(Utilities.getTimeNow() + " DEBUG inVmWorkers min:" + getConcurrentInVmWorkerCount() + " max: " + getMaxConcurrentInVmWorkerCount());
        }
        Utilities.verboseLog(20, "temp dir: " + getWorkingTemporaryDirectoryPath());

        Utilities.verboseLog(110, "Old values - inVmWorkers min: " + workerQueueJmsContainer.getConcurrentConsumers() + " max: " + workerQueueJmsContainer.getMaxConcurrentConsumers());

        //if user has specified CPU value

        //need more testing

        if (!(getMaxConcurrentInVmWorkerCount() == workerQueueJmsContainer.getMaxConcurrentConsumers())) {
            int minNumberOfCPUCores = getMaxConcurrentInVmWorkerCount();

            workerQueueJmsContainer.setConcurrentConsumers(minNumberOfCPUCores);
            workerQueueJmsContainer.setMaxConcurrentConsumers(getMaxConcurrentInVmWorkerCount());
            setConcurrentInVmWorkerCount(minNumberOfCPUCores); // set the mini int the master the same as provided

            Utilities.verboseLog(30, "minNumberOfCPUCores: " + minNumberOfCPUCores
                    + " MaxConcurrentInVmWorkerCount: " + getMaxConcurrentInVmWorkerCount());

            //update the parameters
            //params.put(StepInstanceCreatingStep.WORKER_NUMBER_KEY, Integer.toString(getConcurrentInVmWorkerCount()));
        } else {
            //set the minconsumercount to value given by user in the properties file
            //TODO check if this is necessary as the container should handle dynamic scaling
            //workerQueueJmsContainer.setConcurrentConsumers(getMaxConcurrentInVmWorkerCount());

            /*
            //the following doesnt work as expected so we will just set max = min
            int minNumberOfCPUCores = getConcurrentInVmWorkerCount();
            if (getMaxConcurrentInVmWorkerCount() > 4){
                minNumberOfCPUCores = getMaxConcurrentInVmWorkerCount() / 2;
                workerQueueJmsContainer.setConcurrentConsumers(minNumberOfCPUCores);
            }
            */
        }


        Utilities.verboseLog(1100, "New values - inVmWorkers min: " + workerQueueJmsContainer.getConcurrentConsumers()
                + " max: " + workerQueueJmsContainer.getMaxConcurrentConsumers()
                + " schedlued: " + workerQueueJmsContainer.getScheduledConsumerCount()
                + " active: " + workerQueueJmsContainer.getActiveConsumerCount());

        workerQueueJmsContainer.shutdown();
        if (!workerQueueJmsContainer.isRunning()) {
            Utilities.verboseLog(1100, " the workerQueueJmsContainer is shutdown ...");
        }
        workerQueueJmsContainer.afterPropertiesSet();
        workerQueueJmsContainer.start();

        Utilities.verboseLog(1100, "After Stop Start --- inVmWorkers min: " + workerQueueJmsContainer.getConcurrentConsumers()
                + " max: " + workerQueueJmsContainer.getMaxConcurrentConsumers()
                + " schedlued: " + workerQueueJmsContainer.getScheduledConsumerCount()
                + " active: " + workerQueueJmsContainer.getActiveConsumerCount());

        long nowAfterLoadingDatabase = now;

        Utilities.cpuCount = workerQueueJmsContainer.getMaxConcurrentConsumers();

        Utilities.verboseLog(30, "Working TemporaryDirectory: " + getWorkingTemporaryDirectoryPath());

        try {
            loadInMemoryDatabase();
            runStatus = 21;
            nowAfterLoadingDatabase = System.currentTimeMillis();

            int stepInstancesCreatedByLoadStep = createStepInstances();


            //calculate minimum expected jobs

            int minimumStepsExpected = getMinimumStepsExpected();
            runStatus = 31;
            Utilities.verboseLog(110, " DEBUG step instances: " + stepInstanceDAO.count());

            //initialise slow steps
            List<String> slowSteps = new ArrayList<String>();
            //slowSteps.add("stepPantherRunHmmer3");
            slowSteps.add("stepSMARTRunBinary");
            //slowSteps.add("stepPrositeProfilesRunBinary");
            // If there is an embeddedWorkerFactory (i.e. this Master is running in stand-alone mode)
            // stop running if there are no StepInstances left to complete.
            Long scheduleGCStart = System.currentTimeMillis();
            int allowedWaitTimeMultiplier = 0;
            boolean controlledLogging = false;
            boolean submittedFinaliseStep = false;
            while (!shutdownCalled) {
                boolean completed = true;
                runStatus = 41;
                List<StepInstance> unfinshedStepInstances = stepInstanceDAO.retrieveUnfinishedStepInstances();
                for (StepInstance stepInstance : unfinshedStepInstances) {
                    runStatus = 51;

                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Iterating over StepInstances: Currently on " + stepInstance);
                    }
                    if (stepInstance.hasFailedPermanently(jobs)) {
                        unrecoverableErrorStrategy.failed(stepInstance, jobs);
                    }
                    completed &= stepInstance.haveFinished(jobs);
                    if (stepInstance.canBeSubmitted(jobs) && stepInstanceDAO.serialGroupCanRun(stepInstance, jobs)) {
                        final Step step = stepInstance.getStep(jobs);

                        final SerialGroup serialGroup = stepInstance.getStep(jobs).getSerialGroup();

                        if (SerialGroup.PANTHER_BINARY.equals(serialGroup)) {
                            //deal with panther binary
                            List<StepInstance> serialGroupInstances = stepInstanceDAO.getSerialGroupInstances(stepInstance, jobs);
                            if(! pantherBinaryCanRun(serialGroup, serialGroupInstances)) {
                                continue;
                            }
                        }

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Step submitted:" + stepInstance);
                        }
                        final boolean resubmission = stepInstance.getExecutions().size() > 0;
                        if (resubmission) {
                            LOGGER.warn("StepInstance " + stepInstance.getId() + " is being re-run following a failure.");
                        }

                        // Only set up message selectors for high memory requirements if a suitable worker runner has been set up.

                        // Serial groups should be high priority, however exclude WriteFastaFileStep from this
                        // as they are very abundant.
                        // RunPsScanStep should have higher priority as it is slow
                        //isHighPriorityStep(step);
                        int priority;
                        if (step instanceof RunPsScanStep) {
                            priority = HIGHER_PRIORITY;
                        } else if (step.getSerialGroup() == null || step instanceof WriteFastaFileStep) {
                            priority = LOW_PRIORITY;
                        } else {
                            priority = HIGH_PRIORITY;
                        }

                        //different rules for priority
                        if (step instanceof WriteFastaFileStep) {
                            priority = HIGHEST_PRIORITY;
                        } else if (slowSteps.contains(step.getId())) {
                            priority = HIGHER_PRIORITY;
                        } else if (step instanceof RunBinaryStep) {
                            priority = HIGH_PRIORITY;
                        } else if (step.getSerialGroup() == null) {
                            priority = LOW_PRIORITY;
                        } else {
                            priority = LOW_PRIORITY;
                        }

                        //when lookup is happening progres report may not be accurate
                        if (step instanceof FinaliseInitialSetupStep && !(unfinshedStepInstances.contains(step))) {
                            submittedFinaliseStep = true;
                        }
                        //if interproscan is on the last step, watermark this point
                        if (step instanceof WriteOutputStep) {
                            Utilities.verboseLog(1100, "Processing WriteOutputStep ...");
                            StatsUtil.setForceDisplayProgress(true);
                            statsUtil.displayMasterProgress();
                            StatsUtil.setForceDisplayProgress(false);
                        }

                        // Performed in a transaction.
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("About to send a message for StepInstance: " + stepInstance);
                        }
                        if (this.isExcludeSites()) {
                            stepInstance.addParameter(StepInstanceCreatingStep.EXCLUDE_SITES, Boolean.TRUE.toString());
                        }
                        messageSender.sendMessage(stepInstance, false, priority, false);
                        statsUtil.addToSubmittedStepInstances(stepInstance);
                        controlledLogging = false;
                        statsUtil.addToAllAvailableJobs(stepInstance, "submitted");
                    } else {
                        statsUtil.addToAllAvailableJobs(stepInstance, "considered");
                    }
                    Long scheduleGCTime = System.currentTimeMillis() - scheduleGCStart;
                    if (scheduleGCTime >= 30 * 60 * 1000) {
                        scheduleGCStart = System.currentTimeMillis();
                        Utilities.verboseLog(20,
                                "stepInstanceDAO.count() " + stepInstanceDAO.count()
                                        + " stepInstancesCreatedByLoadStep : " + stepInstancesCreatedByLoadStep
                                        + " minimumStepsExpected : " + minimumStepsExpected
                                        + " SubmittedStepInstancesCount : " + statsUtil.getSubmittedStepInstancesCount()
                                        + " totalUnfinishedStepInstances: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                        Utilities.printMemoryUsage("StandaloneBlackBoxMaster - loop - unfinshedStepInstances ");
                    }
                }
                //Utilities.verboseLog(1100, "runStatus:" + runStatus);
                //check what is not completed
                long totalStepInstances = stepInstanceDAO.count();
                int totalUnfinishedStepInstances = stepInstanceDAO.retrieveUnfinishedStepInstances().size();

//                statsUtil.memoryMonitor();
                if (!controlledLogging) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("StandAlone Master has no jobs ready .. more Jobs will be made ready ");
                        LOGGER.debug("Step instances left to run: " + totalUnfinishedStepInstances);
                        LOGGER.debug("Total StepInstances: " + totalStepInstances);
                    }
                    controlledLogging = true;
                }
//                Utilities.verboseLog(1100, "Total StepInstances: " + totalStepInstances +
//                        ", left to run: " + totalUnfinishedStepInstances);
//                Utilities.verboseLog(1100, "MaxConcurrentConsumers: " + workerQueueJmsContainer.getMaxConcurrentConsumers()
//                       + " min ConsumerCount: " + workerQueueJmsContainer.getConcurrentConsumers()
//                        +  " ActiveConsumerCount: " + workerQueueJmsContainer.getActiveConsumerCount()
//                         +  " ScheduledConsumerCount: " + workerQueueJmsContainer.getScheduledConsumerCount());

                //report progress
                statsUtil.setTotalJobs(totalStepInstances);
                statsUtil.setUnfinishedJobs(totalUnfinishedStepInstances);
//                final boolean statsAvailable = statsUtil.pollStatsBrokerJobQueue();
                if (submittedFinaliseStep) {
                    statsUtil.displayMasterProgress();
                }


                // Close down (break out of loop) if the analyses are all complete.
                // The final clause checks that the protein load steps have been created so
                // i5 doesn't finish prematurely.
                boolean writeOutputStepCompleted = false;
                writeOutputStepCompleted = Utilities.isWriteOutputStepCompleted();


                int submittedStepInstancesCount = statsUtil.getSubmittedStepInstancesCount();
                if (completed
                        && totalStepInstances == submittedStepInstancesCount
                        && submittedStepInstancesCount >= minimumStepsExpected
                        && totalUnfinishedStepInstances == 0
                        && totalStepInstances > stepInstancesCreatedByLoadStep
                        && totalStepInstances >= minimumStepsExpected
                        && writeOutputStepCompleted) {
                    Utilities.verboseLog(1100, "stepInstanceDAO.count() " + totalStepInstances
                            + " stepInstancesCreatedByLoadStep : " + stepInstancesCreatedByLoadStep
                            + " minimumStepsExpected : " + minimumStepsExpected
                            + " SubmittedStepInstancesCount : " + submittedStepInstancesCount
                            + " unfinishedSteps " + totalUnfinishedStepInstances);

                    runStatus = 0;
                    break;
                }
                if (completed
                        && totalUnfinishedStepInstances == 0
                        && writeOutputStepCompleted) {
                    if (allowedWaitTimeMultiplier % 25 == 0) {
                        Utilities.verboseLog(1100, "Should be finished: stepInstanceDAO.count() " + totalStepInstances
                                + " stepInstancesCreatedByLoadStep : " + stepInstancesCreatedByLoadStep
                                + " minimumStepsExpected : " + minimumStepsExpected
                                + " SubmittedStepInstancesCount : " + statsUtil.getSubmittedStepInstancesCount()
                                + " unfinishedSteps " + totalUnfinishedStepInstances);
                    }
                    if (totalStepInstances > stepInstancesCreatedByLoadStep) {
                        //Utilities.verboseLog(1100, "chances are all the steps have been completed, so wait for five second then break");
                        allowedWaitTimeMultiplier++;
                    } else {
                        Utilities.verboseLog(1100, "Should be finished: ...but rest");
                        allowedWaitTimeMultiplier = 0;
                    }

                    if (allowedWaitTimeMultiplier > 50) {
                        Utilities.verboseLog(1100, "chances are all the steps have been completed, so wait for five second then break .. allowedWaitTimeMultiplier" + allowedWaitTimeMultiplier);
                        break;
                    }
                }
//                if(writeOutputStepCompleted){
//                    Utilities.verboseLog(1100, "2 Should be finished: stepInstanceDAO.count() " + totalStepInstances
//                            + " stepInstancesCreatedByLoadStep : " + stepInstancesCreatedByLoadStep
//                            + " minimumStepsExpected : " + minimumStepsExpected
//                            + " SubmittedStepInstancesCount : " + submittedStepInstancesCount
//                            +  " unfinishedSteps " + totalUnfinishedStepInstances);
//                    Thread.sleep(30* 1000);
//                }
                //for standalone es mode this should be < 200
                Thread.sleep(100);  // Make sure the Master thread is not hogging resources required by in-memory workers.
                Long scheduleGCTime = System.currentTimeMillis() - scheduleGCStart;
                if (scheduleGCTime >= 30 * 60 * 1000) {
                    scheduleGCStart = System.currentTimeMillis();
                    Utilities.verboseLog(20,
                            "stepInstanceDAO.count() " + stepInstanceDAO.count()
                                    + " stepInstancesCreatedByLoadStep : " + stepInstancesCreatedByLoadStep
                                    + " minimumStepsExpected : " + minimumStepsExpected
                                    + " SubmittedStepInstancesCount : " + statsUtil.getSubmittedStepInstancesCount()
                                    + " totalUnfinishedStepInstances: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                    Utilities.printMemoryUsage("StandaloneBlackBoxMaster - loop - !shutdownCalled ");
                }
            }
            runStatus = 0;
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by StandaloneBlackBoxMaster: ", e);
            systemExit(999);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by StandaloneBlackBoxMaster: ", e);
            e.printStackTrace(); //TODO fro debug only remove in production
            systemExit(999);
        }

        if (runStatus == 0) {
            System.out.println(Utilities.getTimeNow() + " 100% done:  InterProScan analyses completed \n");
        } else {
            LOGGER.error("InterProScan analyses failed, check log details for the errors - " + runStatus);
        }

        if (Utilities.verboseLogLevel >= 10) {
            final long executionTime = System.currentTimeMillis() - now;
            final long executionTimeExclLoadDatabase = System.currentTimeMillis() - nowAfterLoadingDatabase;
            System.out.println("Computation time : (" + TimeUnit.MILLISECONDS.toSeconds(executionTime) + " s) => " + String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(executionTime),
                    TimeUnit.MILLISECONDS.toSeconds(executionTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime))
            ));
            System.out.println("Computation time exc ldb : (" + TimeUnit.MILLISECONDS.toSeconds(executionTimeExclLoadDatabase) + " s) => " + String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(executionTimeExclLoadDatabase),
                    TimeUnit.MILLISECONDS.toSeconds(executionTimeExclLoadDatabase) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTimeExclLoadDatabase))
            ));
        }
        systemExit(runStatus);
    }


    /**
     * Exit InterProScan 5 immediately with the supplied exit code.
     *
     * @param status Exit code to use
     */
    private void systemExit(int status) {
        Utilities.verboseLog(" Exit status: " + status);
        try {
            databaseCleaner.closeDatabaseCleaner();
            LOGGER.debug("Ending");
            Thread.sleep(500); // cool off, then exit
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("InterProScan analysis failed.");
        } finally {
            cleanUpWorkingDirectory();
            // Always exit
            if (status != 0) {
                System.err.println("InterProScan analysis failed. Exception thrown by StandaloneBlackBoxMaster. Check the log file for details");
                System.exit(status);
            }
        }
        //let Run do the cleanup
        //System.exit(status);
    }

    boolean pantherBinaryCanRun(SerialGroup serialGroup, List<StepInstance> serialGroupInstances){
            if (SerialGroup.PANTHER_BINARY.equals(serialGroup)) {
                //FOr other purposes may be 1 step per pantherBinary group would suffice
                //Panther Bianry is memory intensive, so a hack to reduce the RAM requirments for production

                if (serialGroupInstances != null && pantherBinaryControlFactor > 1) {
                    int pantherBinaryStepsSubmitted = serialGroupInstances.size();
                    if (pantherBinaryStepsSubmitted >= (Utilities.cpuCount / pantherBinaryControlFactor)) {
                        Long pantherSerialGroupCheckStart = System.currentTimeMillis();
                        Utilities.verboseLog(30, serialGroup + " - pantherBinaryStepsSumitted > (Utilities.cpuCount / "
                                + pantherBinaryControlFactor
                                + ") : " + pantherBinaryStepsSubmitted + " vs cpuCount:" + Utilities.cpuCount);
//                        for (StepInstance stepInstanceSerialCheck : serialGroupInstances) {
//                            Utilities.verboseLog(50, stepInstanceSerialCheck.getStepId() + " " + stepInstanceSerialCheck.getStepInstanceState().toString());
//                        }
                        return false;
                    }
                }

            }
            return true;
    }

    public void setPantherBinaryControlFactor(Double pantherBinaryControlFactor) {
        this.pantherBinaryControlFactor = pantherBinaryControlFactor;
    }

    /**
     * @param statsUtil
     */
    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

}
