package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.io.TemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.SubmissionWorkerRunner;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Gift Nuka
 *         <p/>
 *         we create a class that handles creating new workers, highmemory or normal worker
 */
public class WorkerManager implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(WorkerManager.class.getName());

    protected TemporaryDirectoryManager temporaryDirectoryManager;

    private int maxConcurrentInVmWorkerCountForWorkers;

    /**
     * Mechanism to start a new worker
     */
    public void run() {
        //we want an efficient way of creating workers
        //create two workers : one high memory and one non high memory


        //TODO master should inject the temporaryDirectoryManager
        final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();

        String threadName = "[WorkerManager] ";
        //start new workers
        int totalRemoteWorkerCreated = 0;
        int normalWorkersCreated = 0;
        int highMemoryWorkersCreated = 0;

        int workerCount = 0;
        LOGGER.debug("Starting the first N normal workers.");

        boolean firstWorkersSpawned = false;
        int waitMultiplier = 1;


        //TODO maybe this check is not necessary
//        if (isUseMatchLookupService()) {
//            //wait longer  : 10 times normal waiting time
//            waitMultiplier = 10;
//        }


        int maxConcurrentInVmWorkerCountForWorkers = getMaxConcurrentInVmWorkerCountForWorkers();
        while (!firstWorkersSpawned) {

            //TODO get masterState.remoteJobs.get()
            final int actualRemoteJobs = 99999; //remoteJobs.get();
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
                /*
                long totalJobCount = statsUtil.getTotalStepInstanceCount().longValue();
                long expectedRemoteJobCount = statsUtil.getRemoteJobsCount().longValue();

                //TODO estimate the number of remote jobs needed per number of steps count
                int remoteJobsEstimate = (int) (totalJobCount / 4);
                //initialWorkersCount = Math.ceil(remoteJobsEstimate / maxMessagesOnQueuePerConsumer);
                int initialWorkersCount = (int) Math.ceil(expectedRemoteJobCount / queueConsumerRatio);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Remote jobs actual: " + actualRemoteJobs);
                    LOGGER.debug("Remote jobs estimate: " + remoteJobsEstimate);
                    LOGGER.debug("Actual Remote jobs expected: " + expectedRemoteJobCount);
                    LOGGER.debug("Initial Workers Count: " + initialWorkersCount);
                    LOGGER.debug("Total jobs (StepInstances): " + totalJobCount);
                }
                if (verboseLog) {
                    Utilities.verboseLog("Remote jobs actual: " + actualRemoteJobs);
                    Utilities.verboseLog("Remote jobs estimate: " + remoteJobsEstimate);
                    Utilities.verboseLog("Remote jobs estimate: " + remoteJobsEstimate);
                    Utilities.verboseLog("Queue Consume rRatio: " + queueConsumerRatio);
                    Utilities.verboseLog("Total jobs (StepInstances): " + totalJobCount);
                }
                if (initialWorkersCount < 1 && expectedRemoteJobCount < maxConcurrentInVmWorkerCountForWorkers) {
                    initialWorkersCount = 1;
                } else if (initialWorkersCount < 2 && expectedRemoteJobCount > maxConcurrentInVmWorkerCountForWorkers) {
                    initialWorkersCount = 2;
                } else if (initialWorkersCount > (maxConsumers)) {
                    initialWorkersCount = (maxConsumers * 8 / 10);
                }
                //if the master cannot run binaries always create a remote worker
                if (initialWorkersCount < 1 && !masterCanRunBinaries) {
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
                        Utilities.verboseLog("Initial Workers created: " + initialWorkersCount);
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
                */
            }
        }

        /*
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
//                      Utilities.verboseLog("threadName +stats util check: ");
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
                Utilities.verboseLog("displayStats : " + displayStats + " timePassed: " + (timeSinceLastVerboseDisplay) / 1000 + " seconds");
            }
            if (verboseLog && displayStats) {
                Utilities.verboseLog("Jobs report: ");
                Utilities.verboseLog("Remote jobs: " + remoteJobs.get());
                Utilities.verboseLog("Remote jobs completed: " + statsUtil.getRemoteJobsCompleted());
                Utilities.verboseLog("Remote jobs not completed: " + remoteJobsNotCompleted);
                Utilities.verboseLog("Local jobs: " + localJobs.get());
                Utilities.verboseLog("Local jobs completed: " + statsUtil.getLocalJobsCompleted());
                Utilities.verboseLog("Local jobs not completed: " + localJobsNotCompleted);
                Utilities.verboseLog("job Request queuesize " + queueSize);
                Utilities.verboseLog("All Step instances left to run:" + unfinishedStepInstancesCount);
                Utilities.verboseLog("Total StepInstances: " + totalJobCount);
                Utilities.verboseLog("LastMessageRecevived by ResponseMonitor at " + statsUtil.getLastMessageReceivedTime());

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
                Utilities.verboseLog("Poll Job Request Queue queue QS: " + queueSize);
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
            Utilities.verboseLog(10, threadName + "masterCanRunRemainingBinaryJobs: " + masterCanRunRemainingJobs);
            Utilities.verboseLog(10, threadName + "statsAvailable: " + statsAvailable);
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
                Utilities.verboseLog("Polled High Memory Job Request queue QS : "
                        + highMemoryQueueSize);
//                        statsUtil.displayHighMemoryQueueStatistics();
            }
            highMemoryWorkerCount = ((SubmissionWorkerRunner) workerRunnerHighMemory).getWorkerCount();

            activeRemoteWorkerCountEstimate += remoteHighMemoryWorkerCountEstimate;

            if (highMemoryQueueSize > 0 && activeRemoteWorkerCountEstimate < maxConsumers) {
                statsUtil.displayHighMemoryQueueStatistics();
                if (verboseLog) {
                    Utilities.verboseLog("highMemoryQueueSize: " + highMemoryQueueSize
                            + " activeRemoteWorkerCountEstimate: " + activeRemoteWorkerCountEstimate);
                }
                final boolean highMemWorkerRequired = statsUtil.getStatsMessageListener().newWorkersRequired(completionTimeTarget);
                boolean quickSpawnModeHighMemory = false;
                if (remoteHighMemoryWorkerCountEstimate < 1 ||
                        (highMemWorkerRequired && remoteHighMemoryWorkerCountEstimate < 1)) {
                    quickSpawnModeHighMemory = true;
                    if (verboseLog) {
                        Utilities.verboseLog("quickSpawnModeHighMemory : " + quickSpawnModeHighMemory
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
                            Utilities.verboseLog("Starting a high memory worker 2");
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
                Utilities.verboseLog("Workers report: ");
                Utilities.verboseLog("NormalRemoteWorkerCountEstimate: " + remoteWorkerCountEstimate);
                Utilities.verboseLog("ActiveRemoteWorkerCountEstimate: " + activeRemoteWorkerCountEstimate);
                Utilities.verboseLog("ActiveInVmWorkerCountEstimate: " + localQueueJmsContainerFatMaster.getActiveConsumerCount());

                Utilities.verboseLog("remoteHighMemoryWorkerCountEstimate: " + remoteHighMemoryWorkerCountEstimate);
                Utilities.verboseLog("TotalRemoteWorkerCreated: " + totalRemoteWorkerCreated);
                Utilities.verboseLog("TotalNormalWorkerCount: " + totalRemoteWorkerCreated);
                Utilities.verboseLog("TotalHighMemoryWorkerCount: " + highMemoryWorkerCount);
                Utilities.verboseLog("highMemoryQueueSize: " + highMemoryQueueSize);
                Utilities.verboseLog("Normal Job Request queuesize " + queueSize);
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

 */
        } // end of while(! shutdowncalled)
        LOGGER.debug(threadName + "Shutdown has been called on the master, start new worker thread will end");

        LOGGER.debug(threadName+"WorkerManager thread has ended");



    }

    public TemporaryDirectoryManager getTemporaryDirectoryManager() {
        return temporaryDirectoryManager;
    }

    public int getMaxConcurrentInVmWorkerCountForWorkers() {
        return maxConcurrentInVmWorkerCountForWorkers;
    }

    public void setTemporaryDirectoryManager(TemporaryDirectoryManager temporaryDirectoryManager) {
        this.temporaryDirectoryManager = temporaryDirectoryManager;
    }

    public void setMaxConcurrentInVmWorkerCountForWorkers(int maxConcurrentInVmWorkerCountForWorkers) {
        this.maxConcurrentInVmWorkerCountForWorkers = maxConcurrentInVmWorkerCountForWorkers;
    }
}
