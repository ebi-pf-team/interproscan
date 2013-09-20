package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.SubmissionWorkerRunner;
import uk.ac.ebi.interpro.scan.jms.stats.StatsMessageListener;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.jms.stats.Utilities;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepExecutionState;
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
public class DistributedBlackBoxMaster extends AbstractBlackBoxMaster implements ClusterUser {

    private static final Logger LOGGER = Logger.getLogger(DistributedBlackBoxMaster.class.getName());

    private String tcpUri;

    private StatsUtil statsUtil;

    private int maxMessagesOnQueuePerConsumer = 8;

    private int maxConsumers;

    private String projectId;

    private String logDir;

    Long timeLastSpawnedWorkers = System.currentTimeMillis();

    private AtomicInteger remoteJobs = new AtomicInteger(0);

    private AtomicInteger localJobs = new AtomicInteger(0);

    private List<Message> failedJobs = new ArrayList<Message>();

    private boolean ftMode = false;

    private static final int MEGA = 1024 * 1024;


    private DefaultMessageListenerContainer localQueueJmsContainerFatMaster;

    private DefaultMessageListenerContainer localQueueJmsContainerThinMaster;


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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("inVmWorkers min:" + getConcurrentInVmWorkerCount() + " max: " + getMaxConcurrentInVmWorkerCount());
        }
        Long timeLastdisplayedStats = System.currentTimeMillis();
        boolean displayStats = true;

        if(verboseLog){
            Utilities.verboseLog("DEBUG " + "inVmWorkers min:" + getConcurrentInVmWorkerCount() + " max: " + getMaxConcurrentInVmWorkerCount());
            Utilities.verboseLog("DEBUG " + "Available processors: " + Runtime.getRuntime().availableProcessors());
            Utilities.verboseLog("DEBUG " + "Memory free: " + Runtime.getRuntime().freeMemory() / MEGA + "MB total: " + Runtime.getRuntime().totalMemory() / MEGA + "MB max: " + Runtime.getRuntime().maxMemory() / MEGA + "MB");
            Utilities.verboseLog("DEBUG " + "tcpUri: " + tcpUri);
        }
        try {
            loadInMemoryDatabase();

            int stepInstancesCreatedByLoadStep = createStepInstances();

            if(verboseLog){
                Utilities.verboseLog("Initial Step instance count: " + stepInstanceDAO.count());
            }
            //remoteJobs.incrementAndGet();
            //this will start a new thread to create new workers
            startNewWorker();

            boolean controlledLogging = false;
            // If there is an embeddedWorkerFactory (i.e. this Master is running in stand-alone mode)
            // stop running if there are no StepInstances left to complete.
            while (!shutdownCalled) {
                if(verboseLog){
                    Utilities.verboseLog("Distributed Master:  run()");
                }
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
                    //

                    completed &= stepInstance.haveFinished(jobs);
                    boolean canBeSubmitted = false;
                    if(ftMode){
                        canBeSubmitted =  stepInstance.canBeSubmitted(jobs) || stepInstance.canBeSubmittedAfterUnknownFailure(jobs);
                    }else{
                        canBeSubmitted =  stepInstance.canBeSubmitted(jobs);
                    }
                    if(ftMode && verboseLog && displayStats &&  stepInstanceDAO.retrieveUnfinishedStepInstances().size() < 50 ){
                        if (!canBeSubmitted){
                            String dependsOn = "";
                            if(stepInstance.getStep(jobs).getId().contains("stepWriteOutput")) {
                                dependsOn = "... all the stepInstances ...";
                            }else{
                                dependsOn = stepInstance.stepInstanceDependsUpon().toString();
                            }
                            Utilities.verboseLog("stepInstance considered:  " +  stepInstance.getId()
                                    + " Step Name: " + stepInstance.getStep(jobs).getId()
                                    + " canBeSubmitted : " + canBeSubmitted
                                    + " why: " + dependsOn
                                    + " Executions #: " + stepInstance.getExecutions().size());
                        }
                    }
                    if (canBeSubmitted && stepInstanceDAO.serialGroupCanRun(stepInstance, jobs)) {
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
                        statsUtil.addToSubmittedStepInstances(stepInstance);
                        if (canRunRemotely){
                            remoteJobs.incrementAndGet();
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Remote jobs: added one more:  " + remoteJobs.get());
                            }
                        }
                        else {
                            localJobs.incrementAndGet();
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Local jobs: added one more:  " + localJobs.get());
                            }
                        }
                        countRegulator++;
                        controlledLogging = false;
                    }
                }
                Long totalStepInstances = stepInstanceDAO.count();
                int totalUnfinishedStepInstances = stepInstanceDAO.retrieveUnfinishedStepInstances().size();
                if(verboseLog){
                    Utilities.verboseLog("Distributed Master:  ofl - ts: "
                            + totalStepInstances
                            +" sl: " + totalUnfinishedStepInstances);
                }
                // Close down (break out of loop) if the analyses are all complete.
                if (completed && totalUnfinishedStepInstances == 0) {
                    // This next 'if' ensures that StepInstances created as a result of loading proteins are
                    // visible.  This is safe, because in the "closeOnCompletion" mode, an "output results" step
                    // is created, so as an absolute minimum there should be one more StepInstance than those
                    // created in the createNucleicAcidLoadStepInstance() or createFastaFileLoadStepInstance() methods.
                    // First clause - checks that the load fasta file thread has finished.
                    // Second clause - if the fasta file thread has finished, checks that all the analysis steps and the output step have finished.
                    if (totalStepInstances > stepInstancesCreatedByLoadStep && totalUnfinishedStepInstances == 0) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("There are no step instances left to run, so about to break out of loop in Master.\n\nStatistics: ");
                            LOGGER.debug("Step instances left to run: " + totalUnfinishedStepInstances);
                            LOGGER.debug("Total StepInstances: " + totalStepInstances);
                        }
                        break;
                    } else {    // This else clause is for LOGGING ONLY - no  logic here.
                        LOGGER.info("Apparently have no more unfinished StepInstances, however it looks like there should be...");
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Step instances left to run: " + totalUnfinishedStepInstances);
                            LOGGER.debug("Total StepInstances: " + totalStepInstances);
                        }
                    }
                }
                if (System.currentTimeMillis() - timeLastdisplayedStats > 5 * 60 * 1000) {
                    displayStats = true;
                    timeLastdisplayedStats =  System.currentTimeMillis();
                }else{
                    displayStats = false;
                }
                if(!controlledLogging){
                    //check what is not completed
                    LOGGER.debug("Distributed Master waiting for step instances to complete ... more step instances may get scheduled ");
                    LOGGER.debug("Total Remote Step instances sent on the queue: " + remoteJobs.get());
                    LOGGER.debug("Total Local Step instances sent on the queue: " + localJobs.get());
                    LOGGER.debug("Total StepInstances to run: " + totalStepInstances);
                    LOGGER.debug("Step instances left to run: " + totalUnfinishedStepInstances);
                    controlledLogging = true;
                }
                if(verboseLog && displayStats){
                    //check what is not completed
                    Utilities.verboseLog("Distributed Master waiting for step instances to complete ... more step instances may get scheduled ");
                    Utilities.verboseLog("Total Remote Step instances sent on the queue: " + remoteJobs.get());
                    Utilities.verboseLog("Total Local Step instances sent on the queue: " + localJobs.get());
                    Utilities.verboseLog("Step instances currently running on master: " + statsUtil.getRunningJobs().size());
                    statsUtil.displayRunningJobs();
                    Utilities.verboseLog("Total StepInstances to run: " + totalStepInstances);
                    Utilities.verboseLog("Step instances left to run: " + totalUnfinishedStepInstances);

                }
                //update the statistics plugin
                if(verboseLog && stepInstanceDAO.retrieveUnfinishedStepInstances().size() == 0){
                    Utilities.verboseLog("There are no step instances left to run");
                }
                statsUtil.setTotalJobs(totalStepInstances);
                statsUtil.setUnfinishedJobs(totalUnfinishedStepInstances);
                statsUtil.displayMasterProgress();
                Thread.sleep(1 * 10 * 1000);   //   Thread.sleep(30*1000);
            }
            //force the worker creation to stop
            shutdownCalled = true;
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by DistributedBlackBoxMasterOLD: ", e);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by DistributedBlackBoxMasterOLD: ", e);
        }

        try {
            //send a shutdown message before exiting
            if(verboseLog){
                Utilities.verboseLog("Distributed Master:  all computations completed , entering shutdown mode");
            }
            messageSender.sendShutDownMessage();
            if(verboseLog){
                Utilities.verboseLog("Distributed Master: main loop: Shutdown mode ... ");
            }
            LOGGER.debug("Distributed Master:  sent shutdown message to workers");
            //statsUtil.pollStatsBrokerTopic();
            //final StatsMessageListener statsMessageListener = statsUtil.getStatsMessageListener();
            //LOGGER.debug("Topic stats: " +statsMessageListener.getStats());
            Thread.sleep(1*5*1000);
            messageSender.sendShutDownMessage();
            //LOGGER.debug("Topic stats 2: " +statsMessageListener.getStats());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        databaseCleaner.closeDatabaseCleaner();
        LOGGER.debug("Ending");
        Utilities.verboseLog("100% of analyses done:  InterProScan analyses completed");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Remote jobs: " + remoteJobs.get());
        }
        final long executionTime =   System.currentTimeMillis() - now;
        if(verboseLog){
            Utilities.verboseLog("Execution Time (s) for Master: " + String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(executionTime),
                    TimeUnit.MILLISECONDS.toSeconds(executionTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime))
            ));
        }
        if(ftMode){
            //statsUtil.getNonAcknowledgedSubmittedStepInstances();
        }
        System.exit(0);
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
        this.maxMessagesOnQueuePerConsumer = maxMessagesOnQueuePerConsumer/2;
    }

    public void setFtMode(boolean ftMode) {
        this.ftMode = ftMode;
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

    public void setSubmissionWorkerLogDir(String logDir){
        //set this as soon as the masters starts running
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner) this.workerRunner).setLogDir(logDir);
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setLogDir(logDir);
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
     * Resend failed/long running jobs
     */
    public void handleLongRunningJobs(){
        Utilities.verboseLog("Report: Unfinished StepInstances");
        Utilities.verboseLog("Unfinished StepInstances: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
        int unfinishedRemoteJobsCount = 0;
        int unfinishedLocalJobsCount = 0;
        int totalUnfinishedJobs = 0;
        for (StepInstance stepInstance : stepInstanceDAO.retrieveUnfinishedStepInstances()) {
            final Step step = stepInstance.getStep(jobs);
            final boolean canRunRemotely = !step.isRequiresDatabaseAccess();
            String StepInstanceId = step.getId() + ": " + stepInstance.getId();
            if (canRunRemotely){
                Utilities.verboseLog(totalUnfinishedJobs + ":" + StepInstanceId + " canRunRemotely ");
                unfinishedRemoteJobsCount++;
            }else{
                Utilities.verboseLog(totalUnfinishedJobs + ":" + StepInstanceId + " is local");
                unfinishedLocalJobsCount++;
                continue;
            }
            Utilities.verboseLog("   depends on : " + step.getDependsUpon());

            int size = stepInstance.getExecutions().size();
            int count = 0;
            Utilities.verboseLog("      Step executions for" + StepInstanceId);
            List <String> stepExecutions = new ArrayList<String>();
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

                count ++;
                if(count == size){
                    //try to force the step to be resubmitted
                    //stepExecution = exec;
                    //stepExecution.setState(StepExecutionState.STEP_EXECUTION_FAILED);
                    //stepExecution.fail();
                }
            }
            totalUnfinishedJobs =  unfinishedLocalJobsCount +  unfinishedRemoteJobsCount;
            Utilities.verboseLog("          " + stepExecutions.toString());
            if(candidateForResubmission){
                if(System.currentTimeMillis() - statsUtil.getLastMessageReceivedTime() > 60 * 60 * 1000){
                    LOGGER.warn("This stepInstance runstate is Execution state unknown, reset for resubmition : "
                            + stepInstance);
                    step.setRetries(6);
                    stepInstance.setStateUnknown(true);
                }
            }
        }

        if(ftMode){
            statsUtil.getNonAcknowledgedSubmittedStepInstances();
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
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Resending job after Major failure: " + stepExecution.getStepInstance().getId());
                                }
                            }catch (Exception e)  {
                                e.printStackTrace();
                            }
                        }
                    }else{
                        try {
                            Thread.sleep(1 * 10 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
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
                int totalRemoteWorkerCreated = 0;
                int normalWorkersCreated = 0;
                int highMemoryWorkersCreated = 0;

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
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("initial check - Remote jobs: " + actualRemoteJobs);
                    }
                    if(actualRemoteJobs < 1){
                        try {
                            Thread.sleep(waitMultiplier * 2 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Remote jobs still = " + actualRemoteJobs);
                        }
                    }
                    else{
                        long totalJobs =  stepInstanceDAO.count();

                        //TODO estimate the number of remote jobs needed per number of steps count
                        int remoteJobsEstimate =  (int) (totalJobs / 4);
                        //initialWorkersCount = Math.round(remoteJobsEstimate / maxMessagesOnQueuePerConsumer);
                        int initialWorkersCount = Math.round(remoteJobsEstimate / (2 * maxConcurrentInVmWorkerCountForWorkers));
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Remote jobs actual: " + actualRemoteJobs);
                            LOGGER.debug("Remote jobs estimate: " + remoteJobsEstimate);
                            LOGGER.debug("Initial Workers Count: " + initialWorkersCount);
                            LOGGER.debug("Total jobs (StepInstances): " + totalJobs);
                        }
                        if(verboseLog){
                            Utilities.verboseLog("Remote jobs actual: " + actualRemoteJobs);
                            Utilities.verboseLog("Remote jobs estimate: " + remoteJobsEstimate);
                            Utilities.verboseLog("Initial Workers Count: " + initialWorkersCount);
                            Utilities.verboseLog("Total jobs (StepInstances): " + totalJobs);
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
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Initial Workers created: " + initialWorkersCount);
                            }
                            if(verboseLog){
                                Utilities.verboseLog("Initial Workers created: " + initialWorkersCount);
                            }
                            setSubmissionWorkerRunnerMasterClockTime();
                            timeLastSpawnedWorkers = System.currentTimeMillis();
                            normalWorkersCreated = workerRunner.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName, initialWorkersCount);
                            totalRemoteWorkerCreated = normalWorkersCreated;
//                            for (int i=0;i< initialWorkersCount;i++){
//                                workerRunner.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName);
//                            }
                            firstWorkersSpawned = true;
                        }else{
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
                int queueConsumerRatio = maxConcurrentInVmWorkerCountForWorkers * 2;

                int remoteJobsNotCompletedEstimate = remoteJobs.get();

                long timeHighMemoryWorkerLastCreated = 0;
                long timeNormalWorkerLastCreated = 0;

                while (!shutdownCalled) {
                    //statsUtil.sendMessage();
                    int timeSinceLastVerboseDisplay =  (int) (System.currentTimeMillis() - timeLastdisplayedStats);
                    if (timeSinceLastVerboseDisplay > 10 * 60 *1000) {
                        displayStats = true;
                        timeLastdisplayedStats =  System.currentTimeMillis();
                    }else{
                        displayStats = false;
                    }
                    if(verboseLog && displayStats){
                        Utilities.verboseLog("Create workers loop: Current Total remoteJobs not completed: "  + remoteJobsNotCompletedEstimate);
                        Utilities.verboseLog("UnfinishedStepInstances: "  + stepInstanceDAO.retrieveUnfinishedStepInstances().size());

//                        Utilities.verboseLog("stats util check: ");
                    }


                    LOGGER.debug("Create workers loop: Current Total remoteJobs not completed: "  + remoteJobsNotCompletedEstimate);
                    final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();
                    final StatsMessageListener statsMessageListener = statsUtil.getStatsMessageListener();

                    final int remoteJobsNotCompleted = remoteJobs.get() - statsUtil.getRemoteJobsCompleted();
                    remoteJobsNotCompletedEstimate =  remoteJobsNotCompleted;
                    final int localJobsNotCompleted = localJobs.get() - statsUtil.getLocalJobsCompleted();
                    int queueSize = statsMessageListener.getQueueSize();
//                    if (verboseLog && displayStats) {
                    if (verboseLog && (timeSinceLastVerboseDisplay / 1000 ) % 10000 == 0){
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
                        Utilities.verboseLog("All Step instances left to run: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                        Utilities.verboseLog("Total StepInstances: " + stepInstanceDAO.count());
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
                    int consumerCountOnJobQueue = statsMessageListener.getConsumers();
                    int activeInVmWorkersOnFatMaster = localQueueJmsContainerFatMaster.getActiveConsumerCount();
                    int activeInVmWorkersOnThinMaster = localQueueJmsContainerThinMaster.getActiveConsumerCount();
                    int activeWorkers = activeInVmWorkersOnFatMaster + activeInVmWorkersOnThinMaster;

                    activeRemoteWorkerCountEstimate = consumerCountOnJobQueue - activeWorkers;

                    queueSize = statsMessageListener.getQueueSize();
                    int remoteJobsOntheQueue = queueSize - localJobsNotCompleted;
                    if (statsAvailable && queueSize > 0 && remoteJobsOntheQueue > activeInVmWorkersOnFatMaster && activeRemoteWorkerCountEstimate < (maxConsumers - 2)) {
                        LOGGER.debug("Check if we can start a normal worker.");
                        if (verboseLog) {
                            Utilities.verboseLog("remoteJobsOntheQueue: " + remoteJobsOntheQueue
                                    + " activeInVmWorkersOnFatMaster: " + activeInVmWorkersOnFatMaster);
                        }
                        //have a standard continency time for lifespan
                        quickSpawnMode = false;
                        if (System.currentTimeMillis() - timeLastSpawnedWorkers > getMaximumLifeMillis() * 0.7) {
                            quickSpawnMode = true;
                        } else if (activeRemoteWorkerCountEstimate > 0) {
                            quickSpawnMode = ((remoteJobsOntheQueue / activeRemoteWorkerCountEstimate) > maxConcurrentInVmWorkerCountForWorkers);
                        } else {
                            quickSpawnMode = false;
                        }

                        if (quickSpawnMode) {
                            LOGGER.debug("Starting a normal worker.");
                            setSubmissionWorkerRunnerMasterClockTime();
                            timeLastSpawnedWorkers = System.currentTimeMillis();
                            normalWorkersCreated = workerRunner.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName);
                            totalRemoteWorkerCreated += normalWorkersCreated;
                            timeNormalWorkerLastCreated = System.currentTimeMillis();
                        }
                    }

                    //statsUtil.sendhighMemMessage();
                    LOGGER.debug("Poll High Memory Job Request queue");
                    final boolean highMemStatsAvailable = statsUtil.pollStatsBrokerHighMemJobQueue();
                    remoteHighMemoryWorkerCountEstimate = statsMessageListener.getConsumers();
                    highMemoryQueueSize = statsMessageListener.getQueueSize();

                    if (verboseLog) {
                        Utilities.verboseLog("Polled High Memory Job Request queue QS : "
                                + highMemoryQueueSize);
                    }
                    highMemoryWorkerCount = ((SubmissionWorkerRunner) workerRunnerHighMemory).getWorkerCount();

                    activeRemoteWorkerCountEstimate += remoteHighMemoryWorkerCountEstimate;

                    if (highMemoryQueueSize > 0 && activeRemoteWorkerCountEstimate < maxConsumers) {
                        statsUtil.displayHighMemoryQueueStatistics();
                        if (verboseLog) {
                            Utilities.verboseLog("highMemoryQueueSize: " + highMemoryQueueSize
                                    + " activeRemoteWorkerCountEstimate: " + activeRemoteWorkerCountEstimate);
                        }
                        final boolean highMemWorkerRequired = statsMessageListener.newWorkersRequired(completionTimeTarget);
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
                        if (timeHighMemoryWorkerLastCreated > 0){
                            timeSinceLastCreatedHMWorker = System.currentTimeMillis() - timeHighMemoryWorkerLastCreated;
                        }
                        boolean waitForPreviousHighMemoryWorkerCreation = false;
                        if(highMemoryWorkersCreated > 0 && remoteHighMemoryWorkerCountEstimate < 0 && timeSinceLastCreatedHMWorker > 20 * 1000) {
                            waitForPreviousHighMemoryWorkerCreation = true;
                        }
                        if (!waitForPreviousHighMemoryWorkerCreation){
                            if ((highMemoryQueueSize / queueConsumerRatio) > remoteHighMemoryWorkerCountEstimate
                                    || quickSpawnModeHighMemory) {
                                LOGGER.debug("Starting a high memory worker.");
                                if (verboseLog) {
                                    Utilities.verboseLog("Starting a high memory worker2");
                                }
                                setSubmissionWorkerRunnerMasterClockTime();
                                highMemoryWorkersCreated = workerRunnerHighMemory.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName, true);
                                totalRemoteWorkerCreated += highMemoryWorkersCreated;
                                timeHighMemoryWorkerLastCreated = System.currentTimeMillis();

                                LOGGER.debug("remoteHighMemoryWorkerCountEstimate: " + remoteHighMemoryWorkerCountEstimate);
                                LOGGER.debug("TotalHighMemoryWorkerCount: " + highMemoryWorkerCount);
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
                        Thread.sleep(2 * 60 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //handle long running jobs
                    Long timeSinceLastMessage = System.currentTimeMillis() - statsUtil.getLastMessageReceivedTime();
                    if(ftMode && timeSinceLastMessage >  30 * 60 * 1000){
                        LOGGER.warn("Master has not received a job message response for > 1 hour");
                        handleLongRunningJobs();
                    }

                }

            }
        });
    }


    private void logJobQueueMessageListenerContainerState() {

        if(localQueueJmsContainerFatMaster != null){
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
