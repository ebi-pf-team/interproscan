package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.SubmissionWorkerRunner;
import uk.ac.ebi.interpro.scan.jms.stats.StatsMessageListener;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.jms.stats.Utilities;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStep;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
public class DistributedBlackBoxMaster extends AbstractBlackBoxMaster implements ClusterUser{

    private static final Logger LOGGER = Logger.getLogger(DistributedBlackBoxMaster.class.getName());

    private String tcpUri;

    private StatsUtil statsUtil;

    private int maxMessagesOnQueuePerConsumer = 8;

    private int maxConsumers;

    private String projectId;

    Long timeLastSpawnedWorkers = System.currentTimeMillis();

    private AtomicInteger remoteJobs = new AtomicInteger(0);

    private AtomicInteger localJobs = new AtomicInteger(0);

    List<Message> failedJobs = new ArrayList<Message>();

    private static final int MEGA = 1024 * 1024;

    /**
     * completion time target for worker creation by the Master
     * should be  less than worker max lifetime  =  7*24*60*60*1000;
     */

    private int completionTimeTarget = 2 * 60 * 60 * 1000;
    private static final int LOW_PRIORITY = 4;
    private static final int HIGH_PRIORITY = 8;

    /**
    * Run the Master Application.
    */
    public void run() {
        final long now = System.currentTimeMillis();
        super.run();
        LOGGER.debug("inVmWorkers min:" + getConcurrentInVmWorkerCount() + " max: " + getMaxConcurrentInVmWorkerCount());
        if(verboseFlag){
            System.out.println(Utilities.getTimeNow() + " DEBUG " + "inVmWorkers min:" + getConcurrentInVmWorkerCount() + " max: " + getMaxConcurrentInVmWorkerCount());
            System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
            System.out.println("Memory free: " + Runtime.getRuntime().freeMemory() / MEGA + "MB total: " + Runtime.getRuntime().totalMemory() / MEGA + "MB max: " + Runtime.getRuntime().maxMemory() / MEGA + "MB");
            System.out.println("tcpUri: " + tcpUri);
        }
        try {
            loadInMemoryDatabase();

            int stepInstancesCreatedByLoadStep = createStepInstances();

            if(verboseFlag){
                System.out.println(Utilities.getTimeNow() + " DEBUG " +  " step instances: " + stepInstanceDAO.count());
            }
            //remoteJobs.incrementAndGet();
            //this will start a new thread to create new workers
            startNewWorker();

            boolean controlledLogging = false;
            // If there is an embeddedWorkerFactory (i.e. this Master is running in stand-alone mode)
            // stop running if there are no StepInstances left to complete.
            while (!shutdownCalled) {
                boolean completed = true;
                int countRegulator = 0;
                for (StepInstance stepInstance : stepInstanceDAO.retrieveUnfinishedStepInstances()) {

                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Iterating over StepInstances: Currently on " + stepInstance);
                    }
                    if (stepInstance.hasFailedPermanently(jobs)) {
                        //shutdown the workers then exit the system
                        messageSender.sendShutDownMessage();
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
                        final boolean canRunRemotely = !step.isRequiresDatabaseAccess();

                        boolean debugSubmission = true;
                        //resubmission = debugSubmission;
                        // Only set up message selectors for high memory requirements if a suitable worker runner has been set up.
                        //final boolean highMemory = resubmission && workerRunnerHighMemory != null && canRunRemotely;
                        final boolean highMemory = resubmission && workerRunnerHighMemory != null && canRunRemotely;

                        if (highMemory) {
                            LOGGER.warn("StepInstance " + stepInstance.getId() + " ("+ step.getId() + ") will be re-run in a high-memory worker.");
                        }

                        // Serial groups should be high priority, however exclude WriteFastaFileStep from this
                        // as they are very abundant.
                        final int priority = step.getSerialGroup() == null || step instanceof WriteFastaFileStep
                                ? LOW_PRIORITY
                                : HIGH_PRIORITY;

                        // Performed in a transaction.
                        messageSender.sendMessage(stepInstance, highMemory, priority, canRunRemotely);
                        if (canRunRemotely){
                            remoteJobs.incrementAndGet();
                            LOGGER.debug("Remote jobs: added one more:  " + remoteJobs.get());
                        }else{
                            localJobs.incrementAndGet();
                            LOGGER.debug("Local jobs: added one more:  " + localJobs.get());
                        }
                        countRegulator++;
                        controlledLogging = false;
                    }
                }

                // Close down (break out of loop) if the analyses are all complete.
                if (completed && stepInstanceDAO.retrieveUnfinishedStepInstances().size() == 0) {
                    // This next 'if' ensures that StepInstances created as a result of loading proteins are
                    // visible.  This is safe, because in the "closeOnCompletion" mode, an "output results" step
                    // is created, so as an absolute minimum there should be one more StepInstance than those
                    // created in the createNucleicAcidLoadStepInstance() or createFastaFileLoadStepInstance() methods.
                    // First clause - checks that the load fasta file thread has finished.
                    // Second clause - if the fasta file thread has finished, checks that all the analysis steps and the output step have finished.
                    if (stepInstanceDAO.count() > stepInstancesCreatedByLoadStep && stepInstanceDAO.retrieveUnfinishedStepInstances().size() == 0) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("There are no step instances left to run, so about to break out of loop in Master.\n\nStatistics: ");
                            LOGGER.debug("Step instances left to run: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                            LOGGER.debug("Total StepInstances: " + stepInstanceDAO.count());
                        }
                        break;
                    } else {    // This else clause is for LOGGING ONLY - no  logic here.
                        LOGGER.info("Apparently have no more unfinished StepInstances, however it looks like there should be...");
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Step instances left to run: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                            LOGGER.debug("Total StepInstances: " + stepInstanceDAO.count());
                        }
                    }
                }
                if(!controlledLogging){
                    //check what is not completed
                    LOGGER.debug("Distributed Master has no jobs scheduled .. more Jobs may get scheduled ");
                    LOGGER.debug("Remote jobs: " + remoteJobs.get());
                    LOGGER.debug("Local jobs: " + localJobs.get());
                    LOGGER.debug("Step instances left to run: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                    LOGGER.debug("Total StepInstances: " + stepInstanceDAO.count());

                    controlledLogging = true;
                }
                if(verboseFlag){
                    //check what is not completed
                    System.out.println("Distributed Master has no step instances scheduled ... more step instances may get scheduled ");
                    System.out.println("Remote jobs: " + remoteJobs.get());
                    System.out.println("Local jobs: " + localJobs.get());
                    System.out.println("Step instances left to run: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                    System.out.println("Total StepInstances: " + stepInstanceDAO.count());
                }
                //update the statistics plugin
                statsUtil.setTotalJobs(stepInstanceDAO.count());
                statsUtil.setUnfinishedJobs(stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                statsUtil.displayMasterProgress();
                Thread.sleep(1*2*1000);   //   Thread.sleep(30*1000);
            }
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by DistributedBlackBoxMasterOLD: ", e);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by DistributedBlackBoxMasterOLD: ", e);
        }

        try {
            //send a shutdown message before exiting
            messageSender.sendShutDownMessage();
            if(verboseFlag){
                System.out.println("main loop: Shutdown mode ... ");
            }
            LOGGER.debug("Distributed Master:  sent shutdown message to workers");
            //statsUtil.pollStatsBrokerTopic();
            //final StatsMessageListener statsMessageListener = statsUtil.getStatsMessageListener();
            //LOGGER.debug("Topic stats: " +statsMessageListener.getStats());
            Thread.sleep(1*5*1000);
            messageSender.sendShutDownMessage();
            //LOGGER.debug("Topic stats 2: " +statsMessageListener.getStats());
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();
        }
        databaseCleaner.closeDatabaseCleaner();
        LOGGER.debug("Ending");
        System.out.println(Utilities.getTimeNow() + " 100% of analyses done:  InterProScan analyses completed");
        LOGGER.debug("Remote jobs: " + remoteJobs.get());
        final long executionTime =   System.currentTimeMillis() - now;
        if(verboseFlag){
            System.out.println("Execution Time (s) for Master: " + String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(executionTime),
                    TimeUnit.MILLISECONDS.toSeconds(executionTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime))
            ));
        }
        System.exit(0);
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

    @Required
    public void setMaxConsumers(int maxConsumers) {
        this.maxConsumers = maxConsumers;
    }

    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }

    public void setMaxMessagesOnQueuePerConsumer(int maxMessagesOnQueuePerConsumer) {
        //worker max unfinished jobs / 2
        this.maxMessagesOnQueuePerConsumer = maxMessagesOnQueuePerConsumer/2;
    }

    @Override
    public void setSubmissionWorkerRunnerProjectId(String projectId){
        //set this as soon as the masters starts running
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner) this.workerRunner).setProjectId(projectId);
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setProjectId(projectId);
        }
    }

    @Override
    public void setSubmissionWorkerRunnerUserDir(String userDir){
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner) this.workerRunner).setUserDir(userDir);
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setUserDir(userDir);
        }
    }

    /**
     *   Set the time for the submission worker runner
     */
    public void setSubmissionWorkerRunnerMasterClockTime(){
        final long currentClockTime = System.currentTimeMillis();
        final long lifeRemaining =  6*60*60*1000 - (currentClockTime - getStartUpTime());
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner) this.workerRunner).setCurrentMasterClockTime(currentClockTime);
            ((SubmissionWorkerRunner) this.workerRunner).setLifeSpanRemaining(lifeRemaining);
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setCurrentMasterClockTime(currentClockTime);
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setLifeSpanRemaining(lifeRemaining);
        }
    }

    /**
     * monitor the failedJobs Queue and resend the jobs
     *
     */
    private void monitorFailedJobs(){
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            boolean highMemory = true;
            boolean canRunRemotely = true;
            public void run() {
                while (!shutdownCalled) {
                    if(failedJobs.size() > 0){
                        for(Message message:failedJobs){
                            final ObjectMessage stepExecutionMessage = (ObjectMessage) message;
                            try {
                                final StepExecution stepExecution = (StepExecution) stepExecutionMessage.getObject();
                                messageSender.sendMessage(stepExecution.getStepInstance(), highMemory, HIGH_PRIORITY, canRunRemotely);
                                LOGGER.debug("Resending job after Major failure: " + stepExecution.getStepInstance().getId());
                            }catch (Exception e)  {
                                e.printStackTrace();
                            }
                        }
                    }else{
                        try {
                            Thread.sleep(1 * 10 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                }
            }
        });
    }

    /**
     * Mechanism to start a new worker
     *
     */
    private void startNewWorker(){
        //we want an efficient way of creating workers
        //create two workers : one high memory and one non high memory
        final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            public void run() {
                //start new workers
                int workerCount = 0;
                LOGGER.debug("Starting the first N normal workers.");

                boolean firstWorkersSpawned = false;
                int waitMultiplier = 1;
                if(isUseMatchLookupService()) {
                    //wait longer  : 10 times normal waiting time
                    waitMultiplier = 10;
                }
                int maxConcurrentInVmWorkerCountForWorkers = getMaxConcurrentInVmWorkerCountForWorkers();
                while(!firstWorkersSpawned) {
                    final int actualRemoteJobs =   remoteJobs.get();
                    LOGGER.debug("initial check - Remote jobs: " + actualRemoteJobs);
                    if(actualRemoteJobs < 1){
                        try {
                            Thread.sleep(waitMultiplier * 2 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        LOGGER.debug("Remote jobs still = " + actualRemoteJobs);
                    }else{
                        long totalJobs =  stepInstanceDAO.count();

                        //TODO estimate the number of remote jobs needed per number of steps count
                        int remoteJobsEstimate =  (int) (totalJobs / 4);
                        //initialWorkersCount = Math.round(remoteJobsEstimate / maxMessagesOnQueuePerConsumer);
                        int initialWorkersCount = Math.round(remoteJobsEstimate / (2 * maxConcurrentInVmWorkerCountForWorkers));
                        LOGGER.debug("Remote jobs actual: " + actualRemoteJobs);
                        LOGGER.debug("Remote jobs estimate: " + remoteJobsEstimate);
                        LOGGER.debug("Initial Workers Count: " + initialWorkersCount);
                        LOGGER.debug("Total jobs (StepInstances): " + totalJobs);
                        if(verboseFlag){
                            System.out.println("Remote jobs actual: " + actualRemoteJobs);
                            System.out.println("Remote jobs estimate: " + remoteJobsEstimate);
                            System.out.println("Initial Workers Count: " + initialWorkersCount);
                            System.out.println("Total jobs (StepInstances): " + totalJobs);
                        }
                        if(initialWorkersCount < 1 && remoteJobsEstimate > 10){
                            initialWorkersCount = 1;
                        }else if(initialWorkersCount > (maxConsumers)){
                            initialWorkersCount = (maxConsumers * 8 / 10);
                        }
                        //for small set of sequences
                        if(totalJobs < 2000 ){
                            initialWorkersCount = initialWorkersCount * 6 /10;
                        }
                        if(initialWorkersCount > 0){
                            LOGGER.debug("Initial Workers created: " + initialWorkersCount);
                            if(verboseFlag){
                                System.out.println("Initial Workers created: " + initialWorkersCount);
                            }
                            setSubmissionWorkerRunnerMasterClockTime();
                            timeLastSpawnedWorkers = System.currentTimeMillis();
                            workerRunner.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName, initialWorkersCount);
//                            for (int i=0;i< initialWorkersCount;i++){
//                                workerRunner.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName);
//                            }
                            firstWorkersSpawned = true;
                        }else{
                            LOGGER.debug("Remote jobs still = " + actualRemoteJobs);
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
                int queueConsumerRatio = maxConcurrentInVmWorkerCountForWorkers * 2;


                while (!shutdownCalled) {
                    //statsUtil.sendMessage();
                    System.out.println("Create workers loop: Current Total remoteJobs: "  + remoteJobs.get());
                    final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();
                    final StatsMessageListener statsMessageListener = statsUtil.getStatsMessageListener();

                    final int remoteJobsNotCompleted = remoteJobs.get() - StatsUtil.getRemoteJobsCompleted();
                    final int localJobsNotCompleted = localJobs.get() - StatsUtil.getLocalJobsCompleted();
                    int queueSize = statsMessageListener.getQueueSize();

                    if (System.currentTimeMillis() - timeLastdisplayedStats > 10 * 60 *1000) {
                        displayStats = true;
                        timeLastdisplayedStats =  System.currentTimeMillis();
                    }
                    if (verboseFlag && displayStats && remoteJobsNotCompleted > 0) {
                        System.out.println("Remote jobs: " + remoteJobs.get());
                        System.out.println("Remote jobs completed: " + StatsUtil.getRemoteJobsCompleted());
                        System.out.println("Remote jobs not completed: " + remoteJobsNotCompleted);
                        System.out.println("Local jobs: " + localJobs.get());
                        System.out.println("Local jobs completed: " + StatsUtil.getLocalJobsCompleted());
                        System.out.println("Local jobs not completed: " + localJobsNotCompleted);
                        System.out.println("job Request queuesize " + queueSize);
                        System.out.println("All Step instances left to run: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                        System.out.println("Total StepInstances: " + stepInstanceDAO.count());
                    }
                    if (remoteJobsNotCompleted > 0) {
                        int remoteWorkerCount = 0;
                        setSubmissionWorkerRunnerMasterClockTime();
                        LOGGER.debug("Poll Job Request Queue queue");
                        final boolean statsAvailable = statsUtil.pollStatsBrokerJobQueue();
                        workerCount = ((SubmissionWorkerRunner) workerRunner).getWorkerCount();
                        int remoteWorkerCountEstimate = statsMessageListener.getConsumers() - getMaxConcurrentInVmWorkerCount();
                        remoteWorkerCount = remoteWorkerCountEstimate;
                        queueSize = statsMessageListener.getQueueSize();
                        int remoteJobsOntheQueue = queueSize - localJobsNotCompleted;
                        if (statsAvailable && remoteJobsOntheQueue > 1 && remoteWorkerCountEstimate < (maxConsumers - 2)) {
                            LOGGER.debug("Check if we can start a normal worker.");
                            //have a standard continency time for lifespan
                            if(System.currentTimeMillis() - timeLastSpawnedWorkers  > getMaximumLifeMillis() * 0.7){
                                quickSpawnMode = true;
                            }else if (remoteWorkerCountEstimate > 0) {
                                quickSpawnMode = ((remoteJobsOntheQueue / remoteWorkerCountEstimate) > maxConcurrentInVmWorkerCountForWorkers);
                            } else {
                                quickSpawnMode = true;
                            }

                            if (quickSpawnMode) {
                                LOGGER.debug("Starting a normal worker.");
                                setSubmissionWorkerRunnerMasterClockTime();
                                timeLastSpawnedWorkers = System.currentTimeMillis();
                                workerRunner.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName);
                            }
                        }
                        //statsUtil.sendhighMemMessage();
                        LOGGER.debug("Poll High Memory Job Request queue");
                        final boolean highMemStatsAvailable = statsUtil.pollStatsBrokerHighMemJobQueue();
                        int highMemoryWorkerCount = ((SubmissionWorkerRunner) workerRunnerHighMemory).getWorkerCount();
                        remoteWorkerCount += highMemoryWorkerCount;

                        int remoteHighMemoryWorkerCountEstimate = statsMessageListener.getConsumers();
                        int highMemoryQueueSize = statsMessageListener.getQueueSize();
                        if (highMemStatsAvailable && remoteWorkerCount < maxConsumers) {
                            final boolean highMemWorkerRequired = statsMessageListener.newWorkersRequired(completionTimeTarget);
                            if (highMemWorkerRequired && remoteHighMemoryWorkerCountEstimate < 1) {
                                quickSpawnMode = true;
                            }
                            if ((highMemoryQueueSize / queueConsumerRatio) > remoteHighMemoryWorkerCountEstimate
                                    || quickSpawnMode) {
                                LOGGER.debug("Starting a high memory worker.");
                                setSubmissionWorkerRunnerMasterClockTime();
                                workerRunnerHighMemory.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName, true);
                            }
                        }

                        LOGGER.debug("Poll the Job Response queue ");
                        final boolean responseQueueStatsAvailable = statsUtil.pollStatsBrokerResponseQueue();
                        if (!responseQueueStatsAvailable) {
                            LOGGER.debug("The Job Response queue is not initialised");
                        } else {
                            LOGGER.debug("JobResponseQueue:  " + statsMessageListener.getStats().toString());
                        }
                        if (verboseFlag && displayStats && remoteJobsNotCompleted > 0) {
                            System.out.println("normalRemoteWorkerCountEstimate: " + remoteWorkerCountEstimate);
                            System.out.println("remoteHighMemoryWorkerCountEstimate: " + remoteHighMemoryWorkerCountEstimate);
                            System.out.println("remoteWorkerCount: " + remoteWorkerCount);
                            System.out.println("AllNormalWorkerCount: " + workerCount);
                            System.out.println("AllHighMemoryWorkerCount: " + highMemoryWorkerCount);
                            System.out.println("highMemoryQueueSize: " + highMemoryQueueSize);
                        }
                    }else{
                        if (verboseFlag  && displayStats ) {
                            System.out.println("Remote jobs: " + remoteJobs.get());
                            System.out.println("Remote jobs not completed: " + remoteJobsNotCompleted);
                            System.out.println("Local jobs: " + localJobs.get());
                            System.out.println("Local jobs completed: " + StatsUtil.getLocalJobsCompleted());
                            System.out.println("Local jobs not completed: " + localJobsNotCompleted);
                            System.out.println("job Request queuesize " + queueSize);
                            System.out.println("All Step instances left to run: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                            System.out.println("Total StepInstances: " + stepInstanceDAO.count());
                        }
                    }

                    try {
                        Thread.sleep(1 * 20 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

            }
        });
    }
}
