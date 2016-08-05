package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.StepInstanceCreatingStep;
import uk.ac.ebi.interpro.scan.util.Utilities;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.prosite.RunPsScanStep;

import javax.jms.JMSException;

import java.util.List;
import java.util.concurrent.ThreadFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 */
public class StandaloneBlackBoxMaster extends AbstractBlackBoxMaster {

    private static final Logger LOGGER = Logger.getLogger(StandaloneBlackBoxMaster.class.getName());

    private StatsUtil statsUtil;

    @Override
    public void run() {
        final long now = System.currentTimeMillis();
        super.run();

        Utilities.verboseLog = verboseLog;
        Utilities.verboseLogLevel = verboseLogLevel;

        int runStatus = 11;
        if(verboseLog) {
            System.out.println(Utilities.getTimeNow() + " verboseLog: " + verboseLog + " verboseLogLevel: " + verboseLogLevel);
            System.out.println(Utilities.getTimeNow() + " DEBUG inVmWorkers min:" + getConcurrentInVmWorkerCount() + " max: " + getMaxConcurrentInVmWorkerCount());
            Utilities.verboseLog(10, "temp dir: " + getWorkingTemporaryDirectoryPath());
        }
        long nowAfterLoadingDatabase = now;
        try {
            loadInMemoryDatabase();
            runStatus = 21;
            nowAfterLoadingDatabase = System.currentTimeMillis();

            int stepInstancesCreatedByLoadStep = createStepInstances();


            //calculate minimum expected jobs

            int minimumStepsExpected = getMinimumStepsExpected();
            runStatus = 31;
            if(verboseLog) {
                System.out.println(Utilities.getTimeNow() + " DEBUG step instances: " + stepInstanceDAO.count());
            }
            // If there is an embeddedWorkerFactory (i.e. this Master is running in stand-alone mode)
            // stop running if there are no StepInstances left to complete.
            boolean controlledLogging = false;
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
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Step submitted:" + stepInstance);
                        }
                        final boolean resubmission = stepInstance.getExecutions().size() > 0;
                        if (resubmission) {
                            LOGGER.warn("StepInstance " + stepInstance.getId() + " is being re-run following a failure.");
                        }
                        final Step step = stepInstance.getStep(jobs);
                        // Only set up message selectors for high memory requirements if a suitable worker runner has been set up.

                        // Serial groups should be high priority, however exclude WriteFastaFileStep from this
                        // as they are very abundant.
                        // RunPsScanStep should have higher priority as it is slow
                        //isHighPriorityStep(step);
                        int priority;
                        if (step instanceof RunPsScanStep) {
                            priority = HIGHER_PRIORITY;
                        }else if (step.getSerialGroup() == null || step instanceof WriteFastaFileStep){
                            priority = LOW_PRIORITY;
                        }else {
                            priority = HIGH_PRIORITY;
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
                    }
                }
                //check what is not completed
                long totalStepInstances = stepInstanceDAO.count();
                int totalUnfinishedStepInstances = stepInstanceDAO.retrieveUnfinishedStepInstances().size();

//                statsUtil.memoryMonitor();
                if(!controlledLogging){
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("StandAlone Master has no jobs ready .. more Jobs will be made ready ");
                        LOGGER.debug("Step instances left to run: " + totalUnfinishedStepInstances);
                        LOGGER.debug("Total StepInstances: " + totalStepInstances);
                    }
                    controlledLogging = true;
                }
                //report progress
                statsUtil.setTotalJobs(totalStepInstances);
                statsUtil.setUnfinishedJobs(totalUnfinishedStepInstances);
//                final boolean statsAvailable = statsUtil.pollStatsBrokerJobQueue();
                statsUtil.displayMasterProgress();


                // Close down (break out of loop) if the analyses are all complete.
                // The final clause checks that the protein load steps have been created so
                // i5 doesn't finish prematurely.
                if (completed
                        && totalStepInstances == statsUtil.getSubmittedStepInstancesCount()
                        && statsUtil.getSubmittedStepInstancesCount() >= minimumStepsExpected
                        && totalUnfinishedStepInstances == 0
                        && totalStepInstances > stepInstancesCreatedByLoadStep
                        && totalStepInstances >= minimumStepsExpected) {
                    Utilities.verboseLog("stepInstanceDAO.count() " + totalStepInstances
                            + " stepInstancesCreatedByLoadStep : " + stepInstancesCreatedByLoadStep
                            + " minimumStepsExpected : " + minimumStepsExpected
                            + " SubmittedStepInstancesCount : " + statsUtil.getSubmittedStepInstancesCount()
                            +  " unfinishedSteps " + totalUnfinishedStepInstances);

                    runStatus = 0;
                    break;
                }
                //for standalone es mode this should be < 200
                Thread.sleep(100);  // Make sure the Master thread is not hogging resources required by in-memory workers.
            }
            runStatus = 0;
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by StandaloneBlackBoxMaster: ", e);
            systemExit(999);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by StandaloneBlackBoxMaster: ", e);
            systemExit(999);
        }

        if (runStatus == 0) {
            System.out.println(Utilities.getTimeNow() + " 100% done:  InterProScan analyses completed");
        }else{
            LOGGER.error("InterProScan analyses failed, check log details for the errors - " + runStatus);
        }

        if(verboseLog){
            final long executionTime =   System.currentTimeMillis() - now;
            final long executionTimeExclLoadDatabase =   System.currentTimeMillis() - nowAfterLoadingDatabase;
            System.out.println("Computation time : (" + TimeUnit.MILLISECONDS.toSeconds(executionTime)+ " s) => " + String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(executionTime),
                    TimeUnit.MILLISECONDS.toSeconds(executionTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime))
            ));
            System.out.println("Computation time exc ldb : (" + TimeUnit.MILLISECONDS.toSeconds(executionTimeExclLoadDatabase)+ " s) => " + String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(executionTimeExclLoadDatabase),
                    TimeUnit.MILLISECONDS.toSeconds(executionTimeExclLoadDatabase) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTimeExclLoadDatabase))
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
            Thread.sleep(500); // cool off, then exit
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            cleanUpWorkingDirectory();
            // Always exit
            if(status != 0){
                System.err.println("InterProScan analysis failed. Exception thrown by StandaloneBlackBoxMaster. Check the log file for details");           
                System.exit(status);
            }
        }
	    //let Run do the cleanup
        //System.exit(status);
    }


    /**
     *
     * @param statsUtil
     */
    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }



}
