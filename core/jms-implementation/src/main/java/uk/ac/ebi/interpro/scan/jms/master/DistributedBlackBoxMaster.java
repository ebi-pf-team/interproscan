package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import uk.ac.ebi.interpro.scan.util.Utilities;
import uk.ac.ebi.interpro.scan.jms.lsf.LSFMonitor;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.SubmissionWorkerRunner;
import uk.ac.ebi.interpro.scan.jms.stats.StatsMessageListener;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
//import uk.ac.ebi.interpro.scan.jms.stats.Utilities;
import uk.ac.ebi.interpro.scan.jms.worker.WorkerState;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepExecutionState;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteOutputStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.prints.RunFingerPrintScanStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.prosite.RunPsScanStep;

import javax.jms.JMSException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Master Controller for InterProScan 5.
 * <p/>
 * This implementation works for both the "Black box" and "Onion mode" versions of InterProScan 5.
 * <p/>
 * Manages the scheduling of StepIntances, based upon the pattern of dependencies in the JobXML definitions.
 *
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class DistributedBlackBoxMaster extends AbstractBlackBoxMaster implements ClusterUser {

    private static final Logger LOGGER = LogManager.getLogger(DistributedBlackBoxMaster.class.getName());

    private String tcpUri;

    private String gridName;

    private int gridLimit;

    private StatsUtil statsUtil;

    private int maxMessagesOnQueuePerConsumer = 8;

    private int maxConsumers;

    private String projectId;

    private String logDir;

    private Long timeLastSpawnedWorkers = System.currentTimeMillis();

    private AtomicInteger remoteJobs = new AtomicInteger(0);

    private AtomicInteger localJobs = new AtomicInteger(0);

    private ConcurrentMap<Long, StepInstance> failedStepExecutions = new ConcurrentHashMap<Long, StepInstance>();

    private boolean ftMode = false;

    private static final int MEGA = 1024 * 1024;

    private DefaultMessageListenerContainer localQueueJmsContainerFatMaster;

    private DefaultMessageListenerContainer localQueueJmsContainerThinMaster;


    private boolean printWorkerSummary = false;

    private LSFMonitor lsfMonitor;

    /**
     * completion time target for worker creation by the Master
     * should be  less than worker max lifetime  =  7*24*60*60*1000;
     */

    private int completionTimeTarget = 2 * 60 * 60 * 1000;
    private int queueConsumerRatio = 20;

    private boolean masterCanRunBinaries = true;

    /**
     * Run the Master Application.
     */
    public void run() {
        final long now = System.currentTimeMillis();
        int runStatus = 99999;
        super.run();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("inVmWorkers min:" + getConcurrentInVmWorkerCount() + " max: " + getMaxConcurrentInVmWorkerCount());
        }
        System.out.println("interproscan cwd = " + System.getProperty("user.dir"));

        String threadName = "[masterRun] ";
        System.out.println(Utilities.getTimeNow() + " Interproscan 5 Master process running on: " + tcpUri);
        Long timeLastDisplayedStatsAndUpdatedClusterState = System.currentTimeMillis();
        boolean displayStats = true;

        Utilities.verboseLog = verboseLog;
        Utilities.verboseLogLevel = verboseLogLevel;
        Utilities.mode = "distributed";

        runStatus = 11;

        if (Utilities.verboseLog) {
            Utilities.verboseLog(1100, "DEBUG " + "inVmWorkers min:" + getConcurrentInVmWorkerCount() + " max: " + getMaxConcurrentInVmWorkerCount());
            Utilities.verboseLog(1100, "DEBUG " + "Available processors: " + Runtime.getRuntime().availableProcessors());
            Utilities.verboseLog(1100, "DEBUG " + "Memory free: " + Runtime.getRuntime().freeMemory() / MEGA + "MB total: " + Runtime.getRuntime().totalMemory() / MEGA + "MB max: " + Runtime.getRuntime().maxMemory() / MEGA + "MB");
            Utilities.verboseLog(1100, "DEBUG " + "tcpUri: " + tcpUri);
        }
        //handle concurrent workers configuration
        if (! (getMaxConcurrentInVmWorkerCount() == localQueueJmsContainerFatMaster.getMaxConcurrentConsumers())){
            int minNumberOfCPUCores = getMaxConcurrentInVmWorkerCount();
            localQueueJmsContainerFatMaster.setConcurrentConsumers(minNumberOfCPUCores);
            localQueueJmsContainerFatMaster.setMaxConcurrentConsumers(getMaxConcurrentInVmWorkerCount());
            Utilities.verboseLog(1100, "minNumberOfCPUCores: " + minNumberOfCPUCores
                    + " MaxConcurrentInVmWorkerCount: " + getMaxConcurrentInVmWorkerCount() );
        }
        Utilities.verboseLog(1100, "New values --- inVmWorkers min: " + localQueueJmsContainerFatMaster.getConcurrentConsumers()
                + " max: " + localQueueJmsContainerFatMaster.getMaxConcurrentConsumers()
                + " schedlued: " + localQueueJmsContainerFatMaster.getScheduledConsumerCount()
                + " active: " + localQueueJmsContainerFatMaster.getActiveConsumerCount()  );

        localQueueJmsContainerFatMaster.shutdown();
        if(! localQueueJmsContainerFatMaster.isRunning()){
            Utilities.verboseLog(1100, " the localQueueJmsContainerFatMaster is shutdown ...");
        }
        localQueueJmsContainerFatMaster.afterPropertiesSet();
        localQueueJmsContainerFatMaster.start();

        Utilities.verboseLog(1100, "After Stop Start --- inVmWorkers min: " + localQueueJmsContainerFatMaster.getConcurrentConsumers()
                + " max: " + localQueueJmsContainerFatMaster.getMaxConcurrentConsumers()
                + " schedlued: " + localQueueJmsContainerFatMaster.getScheduledConsumerCount()
                + " active: " + localQueueJmsContainerFatMaster.getActiveConsumerCount()  );

        try {
            loadInMemoryDatabase();

            runStatus = 21;
            int stepInstancesCreatedByLoadStep = createStepInstances();

            int minimumStepsExpected = getMinimumStepsExpected();
            runStatus = 31;
            if (verboseLog) {
                Utilities.verboseLog(1100, "Initial Step instance count: " + stepInstanceDAO.count());
            }
            //remoteJobs.incrementAndGet();
            //this will start a new thread to create new workers
            startNewWorker();
            //update the cluster stats
            ScheduledExecutorService scheduledExecutorService = updateClusterState();
            //monior the workers
            if (ftMode) { // only run monitoring if we want to receover from some failures
                monitorFailedJobs();
            }


            boolean controlledLogging = false;
            boolean isRemoteJobsCountSet = false;
            int printSerialGroups = 0;
            // If there is an embeddedWorkerFactory (i.e. this Master is running in stand-alone mode)
            // stop running if there are no StepInstances left to complete.
            while (!shutdownCalled) {
                if (verboseLogLevel >= 10) {
                    Utilities.verboseLog(1100, "[Distributed Master] [main loop]:  run() - start of main loop");
                }
                final long timeNowInMainLoop = System.currentTimeMillis();
                boolean completed = true;
                runStatus = 41;

                Map<Long, String> submittedSteps = new ConcurrentHashMap<>();
                List<StepInstance> unfinishedStepInstances = stepInstanceDAO.retrieveUnfinishedStepInstances();
                if (verboseLogLevel >= 10) {
                    Utilities.verboseLog(threadName + "[Distributed Master] [main loop]  totalUnfinishedStepInstances: "
                            + unfinishedStepInstances.size());
                }
                for (StepInstance stepInstance : unfinishedStepInstances) {
                    Utilities.verboseLog(110, "[Distributed Master] [main loop] [Iterate over unfinished StepInstances]: Currently on "
                            + stepInstance);
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Iterating over StepInstances: Currently on " + stepInstance);
                    }
                    runStatus = 51;
                    if (stepInstance.hasFailedPermanently(jobs)) {
                        //shutdown the workers then exit the system
                        messageSender.sendShutDownMessage();
                        Thread.sleep(1 * 10 * 1000);
                        unrecoverableErrorStrategy.failed(stepInstance, jobs);
                    }
                    //
                    completed &= stepInstance.haveFinished(jobs);

                    final boolean canBeSubmitted = stepInstance.canBeSubmitted(jobs);
                    final boolean canBeSubmittedAfterUnknownfailure = isCandidateForResubmission(stepInstance);

                    //serial group has no running step instance
                    final boolean serialGroupCanRun = stepInstanceDAO.serialGroupCanRun(stepInstance, jobs);

                    if (canBeSubmittedAfterUnknownfailure) {
                        LOGGER.warn("Step being considered for submitting after unkown failure:" + stepInstance);
                    }
                    if ((canBeSubmitted || canBeSubmittedAfterUnknownfailure)
                            && serialGroupCanRun) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Step submitted:" + stepInstance);
                        }
                        runStatus = 61;
                        final boolean resubmission = stepInstance.getExecutions().size() > 0;
                        if (resubmission) {
                            LOGGER.warn("StepInstance " + stepInstance.getId() + " is being re-run following a failure.");
                        }
                        final Step step = stepInstance.getStep(jobs);
                        final boolean canRunRemotely = !step.isRequiresDatabaseAccess();

                        boolean debugSubmission = true;

                        //prints should be sent to a higher memory queue as it requires more memory
                        //disable this feature until its possible to guarantee highmemory worker creation
                        //final boolean isPrintsBinaryStep = (step instanceof RunFingerPrintScanStep) ? false : false;

                        //resubmission = debugSubmission;
                        // Only set up message selectors for high memory requirements if a suitable worker runner has been set up.
                        //final boolean highMemory = resubmission && workerRunnerHighMemory != null && canRunRemotely;
//                        final boolean highMemory = (resubmission || isPrintsBinaryStep)
                        final boolean highMemory = (resubmission)
                                && workerRunnerHighMemory != null && canRunRemotely;

                        if (highMemory && resubmission) {
                            LOGGER.warn("StepInstance " + stepInstance.getId() + " (" + step.getId() + ") will be re-run in a high-memory worker.");
                        }

                        // Serial groups should be high priority, however exclude WriteFastaFileStep from this
                        // as they are very abundant.
                        // RunPsScanStep should have higher priority as it is slow
                        int priority = LOW_PRIORITY;
                        if (step instanceof RunPsScanStep) {
                            priority = HIGHER_PRIORITY;
                        } else if (step.getSerialGroup() == null || step instanceof WriteFastaFileStep) {
                            priority = LOW_PRIORITY;
                        } else {
                            priority = HIGH_PRIORITY;
                        }

                        if (canBeSubmittedAfterUnknownfailure) {
                            stepInstance.setStateUnknown(true);
                        }
                        // Performed in a transaction.
                        messageSender.sendMessage(stepInstance, highMemory, priority, canRunRemotely);
                        statsUtil.addToSubmittedStepInstances(stepInstance);
                        submittedSteps.put(stepInstance.getId(), stepInstance.getStepId());
                        if (canBeSubmittedAfterUnknownfailure) {
                            LOGGER.warn("Step submitted after unkown failure:" + stepInstance);
                            stepInstance.setStateUnknown(false);
                            failedStepExecutions.remove(stepInstance.getId());
                        }
                        runStatus = 71;
                        if (canRunRemotely) {
                            //update remotejobscount for the creation of new workers
                            if (!isRemoteJobsCountSet) {
                                updateRemoteJobsCount();
                                isRemoteJobsCountSet = true;
                            }
                            remoteJobs.incrementAndGet();
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Remote jobs: added one more:  " + remoteJobs.get());
                            }
                        } else {
                            localJobs.incrementAndGet();
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Local jobs: added one more:  " + localJobs.get());
                            }
                        }
                        controlledLogging = false;
                    } // end if canbesubmitted and serialgroupcanrun
                    else {
                        //print why
                        printStepInstanceState(stepInstance, canBeSubmitted, serialGroupCanRun, unfinishedStepInstances.size());
                    }
                    Utilities.verboseLog(110, "[Distributed Master] [main loop] [End of Iterate over unfinished StepInstances]: "
                            + " step instance canbesubmitted: " + canBeSubmitted
                            + " serialGroupCanRun: " + serialGroupCanRun);

                }

                runStatus = 91;
                Utilities.verboseLog(1100, "[main loop] time taken to loop over instances : "
                        + (System.currentTimeMillis() - timeNowInMainLoop) + " mills");
                Long totalStepInstances = stepInstanceDAO.count();
                int totalUnfinishedStepInstances = stepInstanceDAO.retrieveUnfinishedStepInstances().size();
                if (verboseLogLevel >= 10) {
                    Utilities.verboseLog(threadName + "[Distributed Master] [main loop] unfinishedStepInstances -- : "
                            + totalUnfinishedStepInstances);
                    Utilities.verboseLog(1100, "Submitted the following jobs ");
                    for (Long jobId : submittedSteps.keySet()) {
                        Utilities.verboseLog(1100, "Job jobId:, " + jobId + " : " + submittedSteps.get(jobId));
                    }
                }

                //update stats
                statsUtil.setTotalJobs(totalStepInstances);
                statsUtil.setUnfinishedJobs(totalUnfinishedStepInstances);

                if (verboseLog && verboseLogLevel > 4) {
                    int submitted = statsUtil.getSubmittedStepInstancesCount();
                    int notAcknowledged = statsUtil.getNonAcknowledgedSubmittedStepInstances().size();
                    Utilities.verboseLog(1100, "Distributed Master:  ofl - totalSteps: "
                            + totalStepInstances
                            + " steps left: " + totalUnfinishedStepInstances
                            + " submitted: " + submitted
                            + " notFinished: " + statsUtil.getUnfinishedJobs()
                            + " notAcknowledged: " + notAcknowledged);
                }

                // Close down (break out of loop) if the analyses are all complete.
                if (completed
                        && totalStepInstances == statsUtil.getSubmittedStepInstancesCount()
                        && statsUtil.getSubmittedStepInstancesCount() >= minimumStepsExpected
                        && totalUnfinishedStepInstances == 0
                        && totalStepInstances > stepInstancesCreatedByLoadStep
                        && totalStepInstances >= minimumStepsExpected) {

                    Utilities.verboseLog(1100, "stepInstanceDAO.count() " + totalStepInstances
                            + " stepInstancesCreatedByLoadStep : " + stepInstancesCreatedByLoadStep
                            + " minimumStepsExpected : " + minimumStepsExpected
                            + " SubmittedStepInstancesCount : " + statsUtil.getSubmittedStepInstancesCount()
                            +  " unfinishedSteps " + totalUnfinishedStepInstances);
                    // This next 'if' ensures that StepInstances created as a result of loading proteins are
                    // visible.  This is safe, because in the "closeOnCompletion" mode, an "output results" step
                    // is created, so as an absolute minimum there should be one more StepInstance than those
                    // created in the createNucleicAcidLoadStepInstance() or createFastaFileLoadStepInstance() methods.
                    // First clause - checks that the load fasta file thread has finished.
                    // Second clause - if the fasta file thread has finished, checks that all the analysis steps and the output step have finished.
                    // TODO: The following condition is redundant - refactor
                    if (totalStepInstances > stepInstancesCreatedByLoadStep && totalUnfinishedStepInstances == 0) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("There are no step instances left to run, so about to break out of loop in Master.\n\nStatistics: ");
                            LOGGER.debug("Step instances left to run: " + totalUnfinishedStepInstances);
                            LOGGER.debug("Total StepInstances: " + totalStepInstances);
                        }
                        if (verboseLogLevel >= 10) {
                            Utilities.verboseLog(threadName + "completed && totalUnfinishedStepInstances == 0 "
                                    + " -- exit master loop");
                        }
                        runStatus = 0;
                        break;
                    } else {    // This else clause is for LOGGING ONLY - no  logic here.
                        LOGGER.info("Apparently have no more unfinished StepInstances, however it looks like there should be...");
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Step instances left to run: " + totalUnfinishedStepInstances);
                            LOGGER.debug("Total StepInstances: " + totalStepInstances);
                        }
                    }
                }
                if (System.currentTimeMillis() - timeLastDisplayedStatsAndUpdatedClusterState > 30 * 60 * 1000) {
                    displayStats = true;
                    timeLastDisplayedStatsAndUpdatedClusterState = System.currentTimeMillis();
                    if (gridName.equals("lsf") && !clusterStateUpdatedRecently()) {
                        //shutdown the previous executor task and start a new task
                        scheduledExecutorService.shutdownNow();
                        scheduledExecutorService = updateClusterState();
                        LOGGER.debug("Restarted scheduledExecutorService for updating ClusterState");
                        Utilities.verboseLog(threadName + "Restarted scheduledExecutorService for updating ClusterState");
                    }
                } else {
                    displayStats = false;
                }
                if (!controlledLogging) {
                    //check what is not completed
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Distributed Master waiting for step instances to complete ... more step instances may get scheduled ");
                        LOGGER.debug("Total Remote Step instances sent on the queue: " + remoteJobs.get());
                        LOGGER.debug("Total Local Step instances sent on the queue: " + localJobs.get());
                        LOGGER.debug("Total StepInstances to run: " + totalStepInstances);
                        LOGGER.debug("Step instances left to run: " + totalUnfinishedStepInstances);
                    }
                    controlledLogging = true;
                }
                if (verboseLog && displayStats) {
                    //check what is not completed
                    Utilities.verboseLog(1100, "Distributed Master waiting for step instances to complete ... more step instances may get scheduled ");
                    Utilities.verboseLog(1100, "Total Remote Step instances sent on the queue: " + remoteJobs.get());
                    Utilities.verboseLog(1100, "Total Local Step instances sent on the queue: " + localJobs.get());
                    Utilities.verboseLog(1100, "Step instances currently running on master: " + statsUtil.getRunningJobs().size());
                    statsUtil.displayRunningJobs();
                    Utilities.verboseLog(1100, "Total StepInstances to run: " + totalStepInstances);
                    Utilities.verboseLog(1100, "Step instances left to run: " + totalUnfinishedStepInstances);

                }
                //update the statistics plugin
                if (verboseLog && totalUnfinishedStepInstances == 0) {
                    Utilities.verboseLog(1100, "There are no step instances left to run");
                }

                statsUtil.displayMasterProgress();
                Thread.sleep(1 * 1 * 500);   //   Thread.sleep(30*1000);

                if (verboseLogLevel >= 10) {
                    Utilities.verboseLog(1100, "[Distributed Master] [[main loop]:  run() - end of main loop");
                }
            } // end of while (!shutdownCalled)
            //force the worker creation to stop
            shutdownCalled = true;
            messageSender.sendShutDownMessage();
            if (verboseLog) {
                Utilities.verboseLog(1100, "Distributed Master:  all computations completed , entering shutdown mode");
            }

            if (runStatus == 0) {
                System.out.println(Utilities.getTimeNow() + " 100% done:  InterProScan analyses completed");
            } else {
                LOGGER.error("InterProScan analyses failed, check log details for the errors - " + runStatus);
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Remote jobs: " + remoteJobs.get());
            }
            final long executionTime = System.currentTimeMillis() - now;
            if (verboseLog) {
                Utilities.verboseLog(1100, "Computation Time (s) for Master: " + String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes(executionTime),
                        TimeUnit.MILLISECONDS.toSeconds(executionTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime))
                ));
            }
            runStatus = 0;
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by DistributedBlackBoxMaster: ", e);
            systemExit(999);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by DistributedBlackBoxMaster: ", e);
            systemExit(999);
        }

        //finally make the exit
        systemExit(runStatus);
    }

    /**
     * Exit InterProScan 5 immediately with the supplied exit code.
     *
     * @param status Exit code to use
     */
    private void systemExit(int status) {
        //wait for 30 seconds before shutting to get the stats from the remaining workers
        try {
            LOGGER.debug("Send shutdown message to workers ");
            messageSender.sendShutDownMessage();
            Thread.sleep(1 * 30 * 1000);
            LOGGER.debug("Shutdown message was sent to workers .. ");
            //if required print the worker states
            if (printWorkerSummary) {
                printWorkerSummary();
            }
            databaseCleaner.closeDatabaseCleaner();
            LOGGER.debug("Ending");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanUpWorkingDirectory();
            // Always exit
            if (status != 0) {
                System.err.println("InterProScan analysis failed. Exception thrown by DistributedBlackBoxMaster. Check the log file for details");
            }
            System.exit(status);
        }
        System.exit(status);
    }


    public void setLocalQueueJmsContainerThinMaster(DefaultMessageListenerContainer localQueueJmsContainerThinMaster) {
        this.localQueueJmsContainerThinMaster = localQueueJmsContainerThinMaster;
    }

    public void setLocalQueueJmsContainerFatMaster(DefaultMessageListenerContainer localQueueJmsContainerFatMaster) {
        this.localQueueJmsContainerFatMaster = localQueueJmsContainerFatMaster;
    }

    /**
     * If the Run class has created a TCP URI message transport
     * with a random port number, this method injects the URI
     * into the Master, so that the Master can create Workers
     * listening to the broker on this URI.
     *
     * @param tcpUri created by the Run class.
     */
    public void setTcpUri(String tcpUri) {
        this.tcpUri = tcpUri;
    }

    public String getTcpUri() {
        return tcpUri;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectId() {
        return this.projectId;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    @Required
    public void setMaxConsumers(int maxConsumers) {
        this.maxConsumers = maxConsumers;
    }

    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }

    public void setMaxMessagesOnQueuePerConsumer(int maxMessagesOnQueuePerConsumer) {
        //worker max unfinished jobs / 2
        this.maxMessagesOnQueuePerConsumer = maxMessagesOnQueuePerConsumer / 2;
    }

    public void setFtMode(boolean ftMode) {
        this.ftMode = ftMode;
    }

    public void setQueueConsumerRatio(int queueConsumerRatio) {
        this.queueConsumerRatio = queueConsumerRatio;
    }

    public void setPrintWorkerSummary(boolean printWorkerSummary) {
        this.printWorkerSummary = printWorkerSummary;
    }

    public void setLsfMonitor(LSFMonitor lsfMonitor) {
        this.lsfMonitor = lsfMonitor;
    }

    @Required
    public void setGridName(String gridName) {
        this.gridName = gridName;
    }

    @Required
    public void setGridLimit(int gridLimit) {
        this.gridLimit = gridLimit;
    }

    public boolean isMasterCanRunBinaries() {
        return masterCanRunBinaries;
    }

    public void setMasterCanRunBinaries(boolean masterCanRunBinaries) {
        this.masterCanRunBinaries = masterCanRunBinaries;
    }

    @Override
    public void setSubmissionWorkerRunnerProjectId(String projectId) {
        //set this as soon as the masters starts running
        if (this.workerRunner instanceof SubmissionWorkerRunner) {
            ((SubmissionWorkerRunner) this.workerRunner).setProjectId(projectId);
        }
        if (this.workerRunnerHighMemory instanceof SubmissionWorkerRunner) {
            ((SubmissionWorkerRunner) this.workerRunnerHighMemory).setProjectId(projectId);
        }
    }

    public void setSubmissionWorkerLogDir(String logDir) {
        //set this as soon as the masters starts running
        if (this.workerRunner instanceof SubmissionWorkerRunner) {
            ((SubmissionWorkerRunner) this.workerRunner).setLogDir(logDir);
        }
        if (this.workerRunnerHighMemory instanceof SubmissionWorkerRunner) {
            ((SubmissionWorkerRunner) this.workerRunnerHighMemory).setLogDir(logDir);
        }
    }

    @Override
    public void setSubmissionWorkerRunnerUserDir(String userDir) {
        if (this.workerRunner instanceof SubmissionWorkerRunner) {
            ((SubmissionWorkerRunner) this.workerRunner).setUserDir(userDir);
        }
        if (this.workerRunnerHighMemory instanceof SubmissionWorkerRunner) {
            ((SubmissionWorkerRunner) this.workerRunnerHighMemory).setUserDir(userDir);
        }
    }

    /**
     * Set the time for the submission worker runner
     */
    public void setSubmissionWorkerRunnerMasterClockTime() {
        final long currentClockTime = System.currentTimeMillis();
        final long lifeRemaining = 6 * 60 * 60 * 1000 - (currentClockTime - getStartUpTime());
        if (this.workerRunner instanceof SubmissionWorkerRunner) {
            ((SubmissionWorkerRunner) this.workerRunner).setCurrentMasterClockTime(currentClockTime);
            ((SubmissionWorkerRunner) this.workerRunner).setLifeSpanRemaining(lifeRemaining);
        }
        if (this.workerRunnerHighMemory instanceof SubmissionWorkerRunner) {
            ((SubmissionWorkerRunner) this.workerRunnerHighMemory).setCurrentMasterClockTime(currentClockTime);
            ((SubmissionWorkerRunner) this.workerRunnerHighMemory).setLifeSpanRemaining(lifeRemaining);
        }
    }

    /**
     * update the clustestate in the submission worker runner
     *
     * @param clusterState
     */
    public void setSubmissionWorkerClusterState(ClusterState clusterState) {
        //set this as soon as the masters starts running
        if (this.workerRunner instanceof SubmissionWorkerRunner) {
            ((SubmissionWorkerRunner) this.workerRunner).setClusterState(clusterState);
        }
        if (this.workerRunnerHighMemory instanceof SubmissionWorkerRunner) {
            ((SubmissionWorkerRunner) this.workerRunnerHighMemory).setClusterState(clusterState);
        }
    }

    public void updateRemoteJobsCount() {
        int remoteJobsCount = 0;
        for (StepInstance stepInstance : stepInstanceDAO.retrieveUnfinishedStepInstances()) {
            final Step step = stepInstance.getStep(jobs);
            final boolean canRunRemotely = !step.isRequiresDatabaseAccess();
            if (canRunRemotely) {
                remoteJobsCount++;
            }
        }
        statsUtil.setRemoteJobsCount(remoteJobsCount);
        statsUtil.setTotalStepInstanceCount(stepInstanceDAO.count());
    }

    /**
     * print the workers states
     */
    public void printWorkerSummary() {
        Collection<WorkerState> workerStateCollection = statsUtil.getWorkerStateMap().values();
        if (workerStateCollection.size() > 0) {
            for (WorkerState worker : workerStateCollection) {
                String logPath = "logDirPath: " + logDir + File.separator + projectId
                        + "_" + worker.getMasterTcpUri().hashCode();
                StringBuffer workerStateSummary = new StringBuffer("Workers Summary - " + statsUtil.getWorkerStateMap().size() + " workers").append("\n");
                workerStateSummary.append(worker.getWorkerIdentification() + ":"
                        + worker.getHostName() + ":"
                        + worker.getTier() + ":"
                        + worker.getMasterTcpUri() + ":"
                        + worker.getWorkerStatus() + ":"
                        + worker.getProcessors() + ":"
                        + worker.getWorkersSpawned() + ":"
                        + worker.getTotalStepCount() + ":"
                        + worker.getUnfinishedStepCount() + ":"
                        + logPath
                ).append("\n");
                System.out.print(workerStateSummary.toString());
            }
        }
    }

    /**
     * Resend failed/long running jobs
     */
    public void handleLongRunningJobs() {
        Utilities.verboseLog(1100, "Report: Unfinished StepInstances");
        Utilities.verboseLog(1100, "Unfinished StepInstances: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
        int unfinishedRemoteJobsCount = 0;
        int unfinishedLocalJobsCount = 0;
        int totalUnfinishedJobs = 0;
        for (StepInstance stepInstance : stepInstanceDAO.retrieveUnfinishedStepInstances()) {
            final Step step = stepInstance.getStep(jobs);
            final boolean canRunRemotely = !step.isRequiresDatabaseAccess();
            String StepInstanceId = step.getId() + ": " + stepInstance.getId();
            if (canRunRemotely) {
                Utilities.verboseLog(totalUnfinishedJobs + ":" + StepInstanceId + " canRunRemotely ");
                unfinishedRemoteJobsCount++;
            } else {
                Utilities.verboseLog(totalUnfinishedJobs + ":" + StepInstanceId + " is local");
                unfinishedLocalJobsCount++;
                continue;
            }
            Utilities.verboseLog(1100, "   depends on : " + step.getDependsUpon());

            int size = stepInstance.getExecutions().size();
            int count = 0;
            Utilities.verboseLog(1100, "      Step executions for" + StepInstanceId);
            List<String> stepExecutions = new ArrayList<String>();
            boolean candidateForResubmission = false;
            for (StepExecution exec : stepInstance.getExecutions()) {
                final StepExecutionState executionState = exec.getState();
                stepExecutions.add(executionState.name());
                switch (executionState) {
                    case NEW_STEP_EXECUTION:
                        LOGGER.debug("This step is a new step execution : " + stepInstance.getStepId());
                        break;
                    case STEP_EXECUTION_SUBMITTED:
                        LOGGER.debug("This job has been submitted : " + stepInstance.getStepId());
                        candidateForResubmission = true;
                        break;
                    case STEP_EXECUTION_RUNNING:
                        LOGGER.debug("This step is running : " + stepInstance.getStepId());
                        candidateForResubmission = true;
                        break;
                    case STEP_EXECUTION_SUCCESSFUL:
                        LOGGER.debug("This step has run and was successful: " + stepInstance.getStepId());
                        break;
                    default:
                        LOGGER.debug("This step has unknown state: " + executionState);
//                            candidateForResubmission = true;
                        break;
                }

                count++;
                if (count == size) {
                    //try to force the step to be resubmitted
                    //stepExecution = exec;
                    //stepExecution.setState(StepExecutionState.STEP_EXECUTION_FAILED);
                    //stepExecution.fail();
                }
            }
            totalUnfinishedJobs = unfinishedLocalJobsCount + unfinishedRemoteJobsCount;
            Utilities.verboseLog(1100, "          " + stepExecutions.toString());
            if (candidateForResubmission) {
                if (System.currentTimeMillis() - statsUtil.getLastMessageReceivedTime() > 120 * 60 * 1000) {
                    LOGGER.warn("This stepInstance runstate is Execution state unknown, reset for resubmition : "
                            + stepInstance);
                    //put this stepInstance into the failedJObs
                    failedStepExecutions.putIfAbsent(stepInstance.getId(), stepInstance);
                }
            }
        }

        if (ftMode) {
            statsUtil.displayNonAcknowledgedSubmittedStepInstances();
        }
    }

    /**
     * print stepInstance info
     *
     * @param stepInstance
     * @return
     */
    private void printStepInstanceState(StepInstance stepInstance, boolean canBeSubmitted, boolean serialGroupCanRun, int unfinishedStepInstances) {
        if (!Utilities.verboseLog) {
            return;
        }

        StepExecutionState state = stepInstance.getStepInstanceState();
        if (stepInstance.getStepInstanceState() == StepExecutionState.STEP_EXECUTION_SUBMITTED) {
            Utilities.verboseLog(1100, "Job: " + stepInstance.getStepId() + " id: " + stepInstance.getId() + " state: " + state);
            return;
        }
        if (verboseLogLevel >= 10 && unfinishedStepInstances < 50) { //&& (! stepInstance.getStep(jobs).isRequiresDatabaseAccess()) ){

            String dependsOn = "";
            if (stepInstance.getStep(jobs).getId().contains("stepWriteOutput")) {
                dependsOn = "... all the stepInstances ...";
            } else {
                List<StepInstance> dependeOnStepInstances = stepInstance.stepInstanceDependsUpon();
                for (StepInstance dependsOnSteInstance : dependeOnStepInstances) {
                    dependsOn += getStepInstanceState(dependsOnSteInstance) + " ... ";
                }
            }

            Utilities.verboseLog(1100, "-------------------------------\n"
                    + "stepInstance considered:  " + stepInstance.getId()
                    + " Step Name: " + stepInstance.getStep(jobs).getId()
                    + " canBeSubmitted : " + canBeSubmitted
                    + " why? "
                    + " dependsOn: " + dependsOn
                    + " Executions #: " + stepInstance.getExecutions().size()
                    + " serialGroupCanRun: " + serialGroupCanRun
                    + " serialgroup: " + stepInstance.getStep(jobs).getSerialGroup()
                    + " stepInstance actual: " + getStepInstanceState(stepInstance));
        }

        List<StepInstance> serialGroupStepInstances = stepInstanceDAO.getSerialGroupInstances(stepInstance, jobs);
        int serialGroupStepInstancesSize = 0;
        if (serialGroupStepInstances != null) {
            serialGroupStepInstancesSize = serialGroupStepInstances.size();
        }
        Utilities.verboseLog(1100, "Steps still running for the serial group: " + stepInstance.getStep(jobs).getSerialGroup()
                + " (" + serialGroupStepInstancesSize + ")");

        if (serialGroupStepInstancesSize > 0) {
            for (StepInstance serialGroupStepInstance : serialGroupStepInstances) {
                Utilities.verboseLog(1100, "Serial group check: " + serialGroupStepInstance.getStepId() + " - " + serialGroupStepInstance.getId() + " "
                        + serialGroupStepInstance.getExecutions());
            }
        }
        Utilities.verboseLog(getDependencyInfo(stepInstance));
    }

    /**
     * print the execution state of this stepInstance
     *
     * @param stepInstance
     * @return
     */
    private String getStepInstanceState(StepInstance stepInstance) {
        StringBuffer buffer = new StringBuffer(stepInstance.toString());
        for (StepExecution exec : stepInstance.getExecutions()) {
            final StepExecutionState executionState = exec.getState();
            buffer.append(" - state: " + executionState);
        }
        buffer.append(" - final state: " + stepInstance.getStepInstanceState());
        return buffer.toString();
    }


    private String getDependencyInfo(StepInstance stepInstance) {
        StringBuffer buffer = new StringBuffer("");
        final StepExecutionState state = stepInstance.getStepInstanceState();

        if (stepInstance.getStep(jobs) instanceof WriteOutputStep) {
            buffer.append("Job: " + stepInstance.getStepId() + " id: " + stepInstance.getId() + " state: " + state
                    + " can be submitted dependency step: ALL STEPS");
            return buffer.toString();
        }

        if (StepExecutionState.NEW_STEP_INSTANCE == state
                ||
                (StepExecutionState.STEP_EXECUTION_FAILED == state && stepInstance.getExecutions().size() < stepInstance.getStep(jobs).getRetries())) {
            // Then check that all the dependencies have been completed successfully.
            List<StepInstance> dependsUpon = stepInstance.stepInstanceDependsUpon();

            if (dependsUpon != null) {
                for (StepInstance dependency : dependsUpon) {
                    // The state of the dependencies already checked may change during this loop,
                    // however this is not a problem - the worst that can happen, is that the StepInstance is not
                    // executed now.
                    if (dependency.getStepInstanceState() != StepExecutionState.STEP_EXECUTION_SUCCESSFUL) {

                        //TODO
                        buffer.append("Job: " + stepInstance.getStepId() + " id: " + stepInstance.getId() + " state: " + state
                                + " can be submitted dependency step: " + dependency.getStepId()
                                + " id: " + dependency.getId()
                                + " state: " + dependency.getStepInstanceState()

                        );
                        break;
                    }
                    buffer.append("Job: " + stepInstance.getStepId() + " id: " + stepInstance.getId() + " state: " + state
                            + " can be submitted dependency step: " + dependency.getStepId()
                            + " id: " + dependency.getId()
                            + " state: " + dependency.getStepInstanceState());
                }
            }
            // All requirements met, so can submit.
            return buffer.toString();
        }
        buffer.append("Job: " + stepInstance.getStepId() + " id: " + stepInstance.getId() + " state: " + state);
        return buffer.toString();
    }

    /**
     * monitor the failedJobs/uncomplted Jobs from the workers and resend the jobs
     */
    private void monitorFailedJobs() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            boolean highMemory = true;
            boolean canRunRemotely = true;

            public void run() {
                while (!shutdownCalled) {
                    Utilities.verboseLog(1100, "MonitorFailedJobs -  Workers #: " + statsUtil.getWorkerStateMap().size());
                    int unfinishedStepsCount = 0;
                    int unfinishedStepsCountInExitedWorkers = 0;
                    int totalSteps = 0;
                    List<StepExecution> unfinishedStepsInExitedWorkers = new ArrayList<StepExecution>();
                    Collection<WorkerState> workerStateCollection = statsUtil.getWorkerStateMap().values();
                    if (workerStateCollection.size() > 0) {
                        for (WorkerState worker : workerStateCollection) {
                            if (worker.getWorkerStatus().equals("EXITED") && !worker.isProcessedByMaster()) {
                                unfinishedStepsCountInExitedWorkers += worker.getUnfinishedStepCount();
                                unfinishedStepsInExitedWorkers.addAll(worker.getNonFinishedJobs().values());
                                worker.setProcessedByMaster(true);
                            }
                            unfinishedStepsCount += worker.getUnfinishedStepCount();
                            totalSteps += worker.getTotalStepCount();
                        }
                        Utilities.verboseLog(1100, "MonitorFailedJobs: Total remote steps reported: " + totalSteps
                                + " remote steps left: " + unfinishedStepsCount);
                        Utilities.verboseLog(1100, "MonitorFailedJobs: Total remote steps for resubmission: "
                                + unfinishedStepsCountInExitedWorkers);

                        if (unfinishedStepsInExitedWorkers.size() > 0) {
                            for (StepExecution stepExecution : unfinishedStepsInExitedWorkers) {
                                final StepInstance workerStepInstance = stepExecution.getStepInstance();
                                failedStepExecutions.putIfAbsent(workerStepInstance.getId(), workerStepInstance);
                                LOGGER.warn("This stepInstance is reset for resubmission after Worker failure "
                                        + workerStepInstance);
                            }
                        }
                    }

                    //sleep for 20 minutes
                    try {
                        Thread.sleep(2 * 60 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * @param stepInstance
     * @return
     */
    public boolean isCandidateForResubmission(StepInstance stepInstance) {
        if (failedStepExecutions.containsKey(stepInstance.getId())) {
            return true;
        }
        return false;
    }

    /**
     * Mechanism to start a new worker
     */
    private void startNewWorker() {
        //we want an efficient way of creating workers
        //create two workers : one high memory and one non high memory
        final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();

        String threadName = "[StartNewWorker] ";
        Executor executor = Executors.newSingleThreadExecutor();

        LOGGER.debug(threadName + "start New Workers");

        executor.execute(new Runnable() {
            public void run() {
                //start new workers
                int totalRemoteWorkerCreated = 0;
                int normalWorkersCreated = 0;
                int highMemoryWorkersCreated = 0;

                int workerCount = 0;
                LOGGER.debug("Starting the first N normal workers.");

                boolean firstWorkersSpawned = false;
                int waitMultiplier = 1;
                if (isUseMatchLookupService()) {
                    //wait longer  : 10 times normal waiting time
                    waitMultiplier = 10;
                }

                int maxConcurrentInVmWorkerCountForWorkers = getMaxConcurrentInVmWorkerCountForWorkers();
                while (!firstWorkersSpawned) {
                    final int actualRemoteJobs = remoteJobs.get();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("initial check - Remote jobs: " + actualRemoteJobs);
                    }
                    if (actualRemoteJobs < 1) {
                        try {
                            Thread.sleep(waitMultiplier * 2 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Remote jobs still = " + actualRemoteJobs);
                        }
                    } else {
                        long totalJobCount = statsUtil.getTotalStepInstanceCount().longValue();
                        long expectedRemoteJobCount = statsUtil.getRemoteJobsCount().longValue();

                        //TODO estimate the number of remote jobs needed per number of steps count
                        int remoteJobsEstimate = (int) (totalJobCount / 4);
                        //initialWorkersCount = Math.ceil(remoteJobsEstimate / maxMessagesOnQueuePerConsumer);
                        int initialWorkersCount = (int)  Math.ceil(expectedRemoteJobCount / queueConsumerRatio);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Remote jobs actual: " + actualRemoteJobs);
                            LOGGER.debug("Remote jobs estimate: " + remoteJobsEstimate);
                            LOGGER.debug("Actual Remote jobs expected: " + expectedRemoteJobCount);
                            LOGGER.debug("Initial Workers Count: " + initialWorkersCount);
                            LOGGER.debug("Total jobs (StepInstances): " + totalJobCount);
                        }
                        if (verboseLog) {
                            Utilities.verboseLog(1100, "Remote jobs actual: " + actualRemoteJobs);
                            Utilities.verboseLog(1100, "Remote jobs estimate: " + remoteJobsEstimate);
                            Utilities.verboseLog(1100, "Remote jobs estimate: " + remoteJobsEstimate);
                            Utilities.verboseLog(1100, "Queue Consume rRatio: " + queueConsumerRatio);
                            Utilities.verboseLog(1100, "Total jobs (StepInstances): " + totalJobCount);
                        }
                        if (initialWorkersCount < 1 && expectedRemoteJobCount < maxConcurrentInVmWorkerCountForWorkers) {
                            initialWorkersCount = 1;
                        } else if (initialWorkersCount < 2 && expectedRemoteJobCount > maxConcurrentInVmWorkerCountForWorkers) {
                            initialWorkersCount = 2;
                        } else if (initialWorkersCount > (maxConsumers)) {
                            initialWorkersCount = (maxConsumers * 8 / 10);
                        }
                        //if the master cannot run binaries always create a remote worker
                        if (initialWorkersCount < 1 && ! masterCanRunBinaries){
                            initialWorkersCount = 1;
                        }
                        //for small set of sequences
                        if (totalJobCount < 2000 && initialWorkersCount > 10) {
                            initialWorkersCount = initialWorkersCount * 9 / 10;
                        }
                        if (initialWorkersCount > 0) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Initial Workers created: " + initialWorkersCount);
                            }
                            if (verboseLog) {
                                Utilities.verboseLog(1100, "Initial Workers created: " + initialWorkersCount);
                            }
                            setSubmissionWorkerRunnerMasterClockTime();
                            timeLastSpawnedWorkers = System.currentTimeMillis();
                            //first create one high memory worker
//                            highMemoryWorkersCreated = workerRunnerHighMemory.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName, true);
//                            totalRemoteWorkerCreated += highMemoryWorkersCreated;
                            //create the normal workers
                            normalWorkersCreated = workerRunner.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName, initialWorkersCount);
                            totalRemoteWorkerCreated = normalWorkersCreated;
//                            for (int i=0;i< initialWorkersCount;i++){
//                                workerRunner.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName);
//                            }
                            firstWorkersSpawned = true;
                        } else {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Remote jobs still = " + actualRemoteJobs);
                            }
                            try {
                                Thread.sleep(waitMultiplier * 4 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                //then you may sleep for a while to allow workers to setup
                try {
                    Thread.sleep(1 * 120 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Long timeLastdisplayedStats = System.currentTimeMillis();
                boolean displayStats = true;
                boolean quickSpawnMode = false;
                if (queueConsumerRatio == 0) {
                    queueConsumerRatio = maxConcurrentInVmWorkerCountForWorkers * 2;
                }

                int remoteJobsNotCompletedEstimate = remoteJobs.get();

                long timeHighMemoryWorkerLastCreated = 0;
                long timeNormalWorkerLastCreated = 0;

                long totalJobCount = statsUtil.getTotalStepInstanceCount().longValue();
                long expectedRemoteJobCount = statsUtil.getRemoteJobsCount().longValue();

                Long lastHandledLongRunningJobs = System.currentTimeMillis();
                while (!shutdownCalled) {
                    //statsUtil.sendMessage();
                    Utilities.verboseLog(threadName + " at start of while (!shutdownCalled)");
                    int timeSinceLastVerboseDisplay = (int) (System.currentTimeMillis() - timeLastdisplayedStats);
                    if (timeSinceLastVerboseDisplay > 2 * 60 * 1000) {
                        displayStats = true;
                        timeLastdisplayedStats = System.currentTimeMillis();
                    } else {
                        displayStats = false;
                    }
                    if (verboseLog && displayStats) {
                        Utilities.verboseLog(threadName + "Create workers loop: Current Total remoteJobs not completed: " + remoteJobsNotCompletedEstimate);
                        Utilities.verboseLog(threadName + "unfinishedStepInstances: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
//                      Utilities.verboseLog(1100, "threadName +stats util check: ");
                    }


                    LOGGER.debug("Create workers loop: Current Total remoteJobs not completed: " + remoteJobsNotCompletedEstimate);
                    final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();

                    final int remoteJobsNotCompleted = remoteJobs.get() - statsUtil.getRemoteJobsCompleted();
                    remoteJobsNotCompletedEstimate = remoteJobsNotCompleted;
                    final int localJobsNotCompleted = localJobs.get() - statsUtil.getLocalJobsCompleted();
                    final int unfinishedStepInstancesCount = stepInstanceDAO.retrieveUnfinishedStepInstances().size();
                    int queueSize = statsUtil.getRequestQueueSize();
                    Utilities.verboseLog(threadName + "Job Request queuesize " + queueSize);
                    Utilities.verboseLog(threadName + "Job Request enqueue count " + statsUtil.getStatsMessageListener().getEnqueueCount());
                    Utilities.verboseLog(threadName + "Job Request dispatch count " + statsUtil.getStatsMessageListener().getDispatchCount());
                    Utilities.verboseLog(threadName + "Job Request Stats: " + statsUtil.getStatsMessageListener().getStats());

//                    if (verboseLog && displayStats) {
                    if (verboseLog && (timeSinceLastVerboseDisplay / 1000) % 10000 == 0) {
                        Utilities.verboseLog(1100, "displayStats : " + displayStats + " timePassed: " + (timeSinceLastVerboseDisplay) / 1000 + " seconds");
                    }
                    if (verboseLog && displayStats) {
                        Utilities.verboseLog(1100, "Jobs report: ");
                        Utilities.verboseLog(1100, "Remote jobs: " + remoteJobs.get());
                        Utilities.verboseLog(1100, "Remote jobs completed: " + statsUtil.getRemoteJobsCompleted());
                        Utilities.verboseLog(1100, "Remote jobs not completed: " + remoteJobsNotCompleted);
                        Utilities.verboseLog(1100, "Local jobs: " + localJobs.get());
                        Utilities.verboseLog(1100, "Local jobs completed: " + statsUtil.getLocalJobsCompleted());
                        Utilities.verboseLog(1100, "Local jobs not completed: " + localJobsNotCompleted);
                        Utilities.verboseLog(1100, "job Request queuesize " + queueSize);
                        Utilities.verboseLog(1100, "All Step instances left to run:" + unfinishedStepInstancesCount);
                        Utilities.verboseLog(1100, "Total StepInstances: " + totalJobCount);
                        Utilities.verboseLog(1100, "LastMessageRecevived by ResponseMonitor at " + statsUtil.getLastMessageReceivedTime());

                        statsUtil.displayRunningJobs();
                        statsUtil.displayQueueStatistics();
                        logJobQueueMessageListenerContainerState();
                    }

                    int remoteWorkerCountEstimate = 0;
                    int activeRemoteWorkerCountEstimate = 0;
                    int remoteWorkerCount = 0;
                    int remoteHighMemoryWorkerCountEstimate = 0;

                    int highMemoryWorkerCount = 0;
                    int highMemoryQueueSize = 0;


                    setSubmissionWorkerRunnerMasterClockTime();
                    if (verboseLog) {
                        Utilities.verboseLog(1100, "Poll Job Request Queue queue QS: " + queueSize);
                    }
                    LOGGER.debug("Poll Job Request Queue queue");
                    final boolean statsAvailable = statsUtil.pollStatsBrokerJobQueue();
                    remoteWorkerCount = ((SubmissionWorkerRunner) workerRunner).getWorkerCount();
                    int consumerCountOnJobQueue = statsUtil.getStatsMessageListener().getConsumers();
                    int activeInVmWorkersOnFatMaster = localQueueJmsContainerFatMaster.getActiveConsumerCount();
                    int activeInVmWorkersOnThinMaster = localQueueJmsContainerThinMaster.getActiveConsumerCount();
                    int activeWorkers = activeInVmWorkersOnFatMaster + activeInVmWorkersOnThinMaster;

                    activeRemoteWorkerCountEstimate = consumerCountOnJobQueue - activeWorkers;

                    //TODO use statsUtil instead of statsmessenger
                    queueSize = statsUtil.getRequestQueueSize();

                    Utilities.verboseLog(threadName + "Job Request queuesize " + queueSize);
                    Utilities.verboseLog(threadName + "Job Request enqueue count " + statsUtil.getStatsMessageListener().getEnqueueCount());
                    Utilities.verboseLog(threadName + "Job Request dispatch count " + statsUtil.getStatsMessageListener().getDispatchCount());
                    Utilities.verboseLog(threadName + "ActiveRemoteWorkerCountEstimate: " + activeRemoteWorkerCountEstimate);

                    boolean masterCanRunRemainingJobs = false;

                    int remoteJobsOntheQueue = queueSize - localJobsNotCompleted;
                    if (masterCanRunBinaries) {
                        masterCanRunRemainingJobs = remoteJobsOntheQueue < activeInVmWorkersOnFatMaster;
                    }
                    Utilities.verboseLog(110, threadName + "masterCanRunRemainingBinaryJobs: " + masterCanRunRemainingJobs);
                    Utilities.verboseLog(110, threadName + "statsAvailable: " + statsAvailable);
                    //if statsAvailable
                    if (queueSize > 0 && (!masterCanRunRemainingJobs)
                            && activeRemoteWorkerCountEstimate < (maxConsumers - 1)) {
                        LOGGER.debug("Check if we can start a normal worker.");
                        Utilities.verboseLog(threadName + "remoteJobsOntheQueue: " + remoteJobsOntheQueue
                                    + " activeInVmWorkersOnFatMaster: " + activeInVmWorkersOnFatMaster);

                        //have a standard continency time for lifespan
                        quickSpawnMode = false;
                        Long intervalSinceLastSpawnedWorkers = System.currentTimeMillis() - timeLastSpawnedWorkers;
                        Long intervalSinceLastMessageReceived = System.currentTimeMillis() - statsUtil.getLastMessageReceivedTime();
                        if (activeRemoteWorkerCountEstimate < 1) {
                            quickSpawnMode = true;
                        } else if (intervalSinceLastSpawnedWorkers > 5 * 60 * 1000
                                && intervalSinceLastMessageReceived > 60 * 60 * 1000) {
                            quickSpawnMode = true;
                        } else if (intervalSinceLastSpawnedWorkers > getMaximumLifeMillis() * 0.7) {
                            quickSpawnMode = true;
                        } else if (activeRemoteWorkerCountEstimate > 0) {
                            //if jobs:queue ratio is greater than 1/2 queueconsumer ration create a worker
                            quickSpawnMode = ((remoteJobsOntheQueue / activeRemoteWorkerCountEstimate) > (queueConsumerRatio / 2));
                        } else {
                            quickSpawnMode = false;
                        }

                        //calculate the number of new workers to create
                        int newWorkerCount = 1;
                        int idealWorkerCount = 1;
                        if (activeRemoteWorkerCountEstimate >= 1) {
                            idealWorkerCount = remoteJobsOntheQueue / (activeRemoteWorkerCountEstimate *
                                    getMaxConcurrentInVmWorkerCountForWorkers() * queueConsumerRatio);
                        }
                        int estimatedWorkerCount = (idealWorkerCount - activeRemoteWorkerCountEstimate);
                        if (maxConsumers > (estimatedWorkerCount + activeRemoteWorkerCountEstimate)) {
                            newWorkerCount = estimatedWorkerCount;
                        } else {
                            newWorkerCount = maxConsumers - activeRemoteWorkerCountEstimate;
                        }
                        if (newWorkerCount < 1) {
                            //always create a new worker is required to do so
                            newWorkerCount = 1;
                        }
                        if (verboseLog) {
                            Utilities.verboseLog(threadName + "newWorkerCount: " + newWorkerCount
                                    + "idealWorkerCount: " + idealWorkerCount
                                    + " estimatedWorkerCount: " + estimatedWorkerCount
                                    + " remoteJobsOntheQueue: " + remoteJobsOntheQueue
                                    + " activeRemoteWorkerCountEstimate: " + activeRemoteWorkerCountEstimate);
                        }

                        //if we havent had any messages on the responsequeu start a new worker

                        if (quickSpawnMode) {
                            LOGGER.debug("Starting a normal worker.");
                            Utilities.verboseLog(threadName + " Number of workers to create: " + newWorkerCount);
                            setSubmissionWorkerRunnerMasterClockTime();
                            timeLastSpawnedWorkers = System.currentTimeMillis();
                            normalWorkersCreated = workerRunner.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName, newWorkerCount);
                            totalRemoteWorkerCreated += normalWorkersCreated;
                            timeNormalWorkerLastCreated = System.currentTimeMillis();
                        }
                    }

                    //statsUtil.sendhighMemMessage();
                    LOGGER.debug("Poll High Memory Job Request queue");
                    //final boolean highMemStatsAvailable = statsUtil.pollStatsBrokerHighMemJobQueue();
                    highMemoryQueueSize = statsUtil.getHighMemRequestQueueSize(); //this will also poll the highmem queue
                    remoteHighMemoryWorkerCountEstimate = statsUtil.getStatsMessageListener().getConsumers();

                    if (verboseLog) {
                        Utilities.verboseLog(1100, "Polled High Memory Job Request queue QS : "
                                + highMemoryQueueSize);
//                        statsUtil.displayHighMemoryQueueStatistics();
                    }
                    highMemoryWorkerCount = ((SubmissionWorkerRunner) workerRunnerHighMemory).getWorkerCount();

                    activeRemoteWorkerCountEstimate += remoteHighMemoryWorkerCountEstimate;

                    if (highMemoryQueueSize > 0 && activeRemoteWorkerCountEstimate < maxConsumers) {
                        statsUtil.displayHighMemoryQueueStatistics();
                        if (verboseLog) {
                            Utilities.verboseLog(1100, "highMemoryQueueSize: " + highMemoryQueueSize
                                    + " activeRemoteWorkerCountEstimate: " + activeRemoteWorkerCountEstimate);
                        }
                        final boolean highMemWorkerRequired = statsUtil.getStatsMessageListener().newWorkersRequired(completionTimeTarget);
                        boolean quickSpawnModeHighMemory = false;
                        if (remoteHighMemoryWorkerCountEstimate < 1 ||
                                (highMemWorkerRequired && remoteHighMemoryWorkerCountEstimate < 1)) {
                            quickSpawnModeHighMemory = true;
                            if (verboseLog) {
                                Utilities.verboseLog(1100, "quickSpawnModeHighMemory : " + quickSpawnModeHighMemory
                                        + " remoteHighMemoryWorkerCountEstimate: " + remoteHighMemoryWorkerCountEstimate);
                            }
                        }

                        //TODO check if lastCreatedWorker is pending
                        long timeSinceLastCreatedHMWorker = 99 * 60 * 1000;
                        if (timeHighMemoryWorkerLastCreated > 0) {
                            timeSinceLastCreatedHMWorker = System.currentTimeMillis() - timeHighMemoryWorkerLastCreated;
                        }
                        boolean waitForPreviousHighMemoryWorkerCreation = false;
                        if (highMemoryWorkersCreated > 0 && remoteHighMemoryWorkerCountEstimate < 0 &&
                                timeSinceLastCreatedHMWorker > 2 * 60 * 1000) {
                            waitForPreviousHighMemoryWorkerCreation = true;
                        }
                        if (!waitForPreviousHighMemoryWorkerCreation) {
                            if ((highMemoryQueueSize / queueConsumerRatio) > remoteHighMemoryWorkerCountEstimate
                                    || quickSpawnModeHighMemory) {
                                LOGGER.debug("Starting a high memory worker.");
                                if (verboseLog) {
                                    Utilities.verboseLog(1100, "Starting a high memory worker 2");
                                }
                                setSubmissionWorkerRunnerMasterClockTime();
                                highMemoryWorkersCreated = workerRunnerHighMemory.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName, true);
                                totalRemoteWorkerCreated += highMemoryWorkersCreated;
                                timeHighMemoryWorkerLastCreated = System.currentTimeMillis();

                                LOGGER.debug("remoteHighMemoryWorkerCountEstimate: " + remoteHighMemoryWorkerCountEstimate);
                                LOGGER.debug("TotalHighMemoryWorkerCount: " + highMemoryWorkerCount);
                                LOGGER.debug("highMemoryWorkersCreated: " + highMemoryWorkersCreated);
                                LOGGER.debug("highMemoryQueueSize: " + highMemoryQueueSize);

                            }
                        }
                    }

                    //display queue statistics
                    if (LOGGER.isDebugEnabled()) {
                        statsUtil.displayQueueStatistics();
                    }


//                    if (displayStats && remoteJobsNotCompleted > 0) {
                    if (verboseLog && displayStats) {
                        Utilities.verboseLog(1100, "Workers report: ");
                        Utilities.verboseLog(1100, "NormalRemoteWorkerCountEstimate: " + remoteWorkerCountEstimate);
                        Utilities.verboseLog(1100, "ActiveRemoteWorkerCountEstimate: " + activeRemoteWorkerCountEstimate);
                        Utilities.verboseLog(1100, "ActiveInVmWorkerCountEstimate: " + localQueueJmsContainerFatMaster.getActiveConsumerCount());

                        Utilities.verboseLog(1100, "remoteHighMemoryWorkerCountEstimate: " + remoteHighMemoryWorkerCountEstimate);
                        Utilities.verboseLog(1100, "TotalRemoteWorkerCreated: " + totalRemoteWorkerCreated);
                        Utilities.verboseLog(1100, "TotalNormalWorkerCount: " + totalRemoteWorkerCreated);
                        Utilities.verboseLog(1100, "TotalHighMemoryWorkerCount: " + highMemoryWorkerCount);
                        Utilities.verboseLog(1100, "highMemoryQueueSize: " + highMemoryQueueSize);
                        Utilities.verboseLog(1100, "Normal Job Request queuesize " + queueSize);
                    }

                    try {
                        //sleep for 2 minutes
                        Thread.sleep(2 * 60 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //handle long running jobs
                    Long timeSinceLastMessage = System.currentTimeMillis() - statsUtil.getLastMessageReceivedTime();
                    Long timeSinceLastHandledLongRunningJobs = System.currentTimeMillis() - lastHandledLongRunningJobs;
                    if (ftMode && timeSinceLastMessage > 120 * 60 * 1000) {
                        if (timeSinceLastHandledLongRunningJobs > 120 * 60 * 1000) {
                            LOGGER.warn("Master has not received a job message response for > 1 hour");
                            handleLongRunningJobs();
                            lastHandledLongRunningJobs = System.currentTimeMillis();
                        }
                    }

                } // end of while(! shutdowncalled)
                LOGGER.debug(threadName + "Shutdown has been called on the master, start new worker thread will end");

            }
        });

        LOGGER.debug(threadName + "Start New Worker thread has ended");
    }


    public ScheduledExecutorService updateClusterState() {
        //execute every 2 min after a 10 sec delay
        ScheduledExecutorService SERVICE = Executors.newSingleThreadScheduledExecutor();
        SERVICE.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Utilities.verboseLog(1100, "start lsf monitor ");
                Long startTimeForMonitor = System.currentTimeMillis();
                if (gridName.equals("lsf")) {
                    try {
                        int activeJobs = lsfMonitor.activeJobs(projectId);
                        int pendingJobs = lsfMonitor.pendingJobs(projectId);
                        int runningJobs = activeJobs - pendingJobs;
                        ClusterState clusterState = new ClusterState(gridLimit, activeJobs, pendingJobs);
                        if (verboseLogLevel > 3) {
                            Utilities.verboseLog(1100, "Grid Job Control: "
                                    + " gridLimit: " + gridLimit
                                    + " activeJobs: " + activeJobs
                                    + " runningJobs: " + runningJobs
                                    + " runningJobs: * 10%: " + +runningJobs * 0.1
                                    + " pendingJobs:" + pendingJobs);
                        }
                        setSubmissionWorkerClusterState(clusterState);
                        messageSender.sendTopicMessage(clusterState);
                        Utilities.verboseLog(clusterState.toString());
                    } catch (Exception e) {
                        LOGGER.warn("Problems accessing cluster stats");
                    }
                }
                Long timePassed = System.currentTimeMillis() - startTimeForMonitor;
                Utilities.verboseLog(1100, "End  lsf monitor : Took " + timePassed + " ms");

            }
        }, 1, gridCheckInterval, TimeUnit.SECONDS);

        return SERVICE;
    }


    private boolean clusterStateUpdatedRecently() {
        ClusterState clusterState = ((SubmissionWorkerRunner) this.workerRunner).getClusterState();
        if (clusterState != null) {
            Long timeSinceClusterLastUpdatedClusterState = System.currentTimeMillis() - clusterState.getLastUpdated();
            //TODO move to a controller bean
            Utilities.verboseLog(1100, "timeSinceClusterLastUpdatedClusterState: " + timeSinceClusterLastUpdatedClusterState);
            if (timeSinceClusterLastUpdatedClusterState > 2 * gridCheckInterval * 1000) {
                Utilities.verboseLog(1100, "ClusterState is not uptodate:" + clusterState.toString());
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    private void logJobQueueMessageListenerContainerState() {

        if (localQueueJmsContainerFatMaster != null) {
            StringBuffer buf = new StringBuffer(" JobQueueMessageListenerContainerFatMasterState state:\n\n");
            buf.append("isRunning: ").append(localQueueJmsContainerFatMaster.isRunning()).append("\n");
            buf.append("isActive: ").append(localQueueJmsContainerFatMaster.isActive()).append("\n");
            buf.append("isActive: ").append(localQueueJmsContainerFatMaster.isActive()).append("\n");
            buf.append("getDestinationName: ").append(localQueueJmsContainerFatMaster.getDestinationName()).append("\n");
            buf.append("isRegisteredWithDestination: ").append(localQueueJmsContainerFatMaster.isRegisteredWithDestination()).append("\n");
            buf.append("getActiveConsumerCount: ").append(localQueueJmsContainerFatMaster.getActiveConsumerCount()).append("\n");
            buf.append("getCacheLevel: ").append(localQueueJmsContainerFatMaster.getCacheLevel()).append("\n");
            buf.append("getConcurrentConsumers: ").append(localQueueJmsContainerFatMaster.getConcurrentConsumers()).append("\n");
            buf.append("getIdleConsumerLimit: ").append(localQueueJmsContainerFatMaster.getIdleConsumerLimit()).append("\n");
            buf.append("getIdleTaskExecutionLimit: ").append(localQueueJmsContainerFatMaster.getIdleTaskExecutionLimit()).append("\n");
            buf.append("getMaxConcurrentConsumers: ").append(localQueueJmsContainerFatMaster.getMaxConcurrentConsumers()).append("\n");
            buf.append("getScheduledConsumerCount: ").append(localQueueJmsContainerFatMaster.getScheduledConsumerCount()).append("\n");
            buf.append("getClientId: ").append(localQueueJmsContainerFatMaster.getClientId()).append("\n");
            buf.append("getDurableSubscriptionName: ").append(localQueueJmsContainerFatMaster.getDurableSubscriptionName()).append("\n");
            buf.append("getMessageSelector: ").append(localQueueJmsContainerFatMaster.getMessageSelector()).append("\n");
            Utilities.verboseLog(buf.toString());

            StringBuffer bufThinMaster = new StringBuffer(" JobQueueMessageListenerContainerThinMasterState state:\n\n");
            bufThinMaster.append("isRunning: ").append(localQueueJmsContainerThinMaster.isRunning()).append("\n");
            bufThinMaster.append("getActiveConsumerCount: ").append(localQueueJmsContainerThinMaster.getActiveConsumerCount()).append("\n");

            Utilities.verboseLog(bufThinMaster.toString());


        }
    }
}
