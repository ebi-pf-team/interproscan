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
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class  DistributedBlackBoxMasterCopy extends AbstractBlackBoxMaster implements ClusterUser{

    private static final Logger LOGGER = Logger.getLogger(DistributedBlackBoxMasterCopy.class.getName());

    private String tcpUri;

    private StatsUtil statsUtil;

    private int maxMessagesOnQueuePerConsumer = 8;

    private int maxConsumers;

    private String projectId;

    private AtomicInteger remoteJobs = new AtomicInteger(0);

    List<Message> failedJobs = new ArrayList<Message>();

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
        try {
            loadInMemoryDatabase();

            int stepInstancesCreatedByLoadStep = createStepInstances();

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
                            LOGGER.warn("StepInstance " + stepInstance.getId() + " will be re-run in a high-memory worker.");
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
                    LOGGER.debug("Distributed Master has no jobs but .. more Jobs may get generated ");
                    LOGGER.debug("Remote jobs: " + remoteJobs.get());
                    LOGGER.debug("Step instances left to run: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                    LOGGER.debug("Total StepInstances: " + stepInstanceDAO.count());
                    controlledLogging = false;
                }
                //update the statistics plugin
                statsUtil.setTotalJobs(stepInstanceDAO.count());
                statsUtil.setUnfinishedJobs(stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                statsUtil.displayMasterProgress();
                Thread.sleep(50);   //   Thread.sleep(30*1000);
            }
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by DistributedBlackBoxMaster: ", e);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by DistributedBlackBoxMaster: ", e);
        }

        //send a shutdown message before exiting
        messageSender.sendShutDownMessage();
        try {
            LOGGER.debug("Distributed Master:  sent shutdown message to workers");
            //statsUtil.pollStatsBrokerTopic();
            //final StatsMessageListener statsMessageListener = statsUtil.getStatsMessageListener();
            //LOGGER.debug("Topic stats: " +statsMessageListener.getStats());
            Thread.sleep(15*1000);
            messageSender.sendShutDownMessage();
            //LOGGER.debug("Topic stats 2: " +statsMessageListener.getStats());
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        databaseCleaner.closeDatabaseCleaner();
        LOGGER.debug("Ending");
        System.out.println(Utilities.getTimeNow() + " 100% of analyses done:  InterProScan analyses completed");
        LOGGER.debug("Remote jobs: " + remoteJobs.get());
        final long executionTime =   System.currentTimeMillis() - now;
        LOGGER.debug("Execution Time (s) for Master: " + String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(executionTime),
                TimeUnit.MILLISECONDS.toSeconds(executionTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime))
        ));
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
                while(!firstWorkersSpawned) {
                    final int actualRemoteJobs =   remoteJobs.get();
                    LOGGER.debug("initial check - Remote jobs: " + actualRemoteJobs);
                    if(actualRemoteJobs < 1){
                        try {
                            Thread.sleep(1 * 5 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        LOGGER.debug("Remote jobs still = " + actualRemoteJobs);
                    }else{
                        long totalJobs =  stepInstanceDAO.count();
                        //TODO estimate the number of remote jobs needed per number of steps count
                        int remoteJobsEstimate =  (int) (totalJobs/6);
                        int initialWorkersCount = Math.round(remoteJobsEstimate / maxMessagesOnQueuePerConsumer);
                        LOGGER.debug("Remote jobs actual: " + actualRemoteJobs);
                        LOGGER.debug("Remote jobs estimate: " + remoteJobsEstimate);
                        LOGGER.debug("Initial Workers Count: " + initialWorkersCount);
                        LOGGER.debug("Total jobs (StepInstances): " + totalJobs);
                        if(initialWorkersCount < 1 && remoteJobsEstimate > 10){
                            initialWorkersCount = 1;
                        }
                        if(initialWorkersCount > 0){
                            LOGGER.debug("Initial Workers created: " + initialWorkersCount);
                            for (int i=0;i< initialWorkersCount;i++){
                                workerRunner.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName);
                            }
                            firstWorkersSpawned = true;
                        }
                    }
                }
                //then you may sleep for a while to allow workers to setup
                try {
                    Thread.sleep(1 * 120 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boolean quickSpawnMode = false;
                while (!shutdownCalled) {
                    //statsUtil.sendMessage();
                    final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();
                    final StatsMessageListener statsMessageListener = statsUtil.getStatsMessageListener();

                    LOGGER.debug("Poll Job Request Queue queue");
                    final boolean statsAvailable = statsUtil.pollStatsBrokerJobQueue();
                    if (statsAvailable) {
                        workerCount = ((SubmissionWorkerRunner) workerRunner).getWorkerCount();
                        if(statsMessageListener.getConsumers() > 0){
                            quickSpawnMode =  ((statsMessageListener.getQueueSize()/ statsMessageListener.getConsumers()) > 4);
                        }
                        final boolean workerRequired = statsMessageListener.newWorkersRequired(completionTimeTarget);
                        if ((statsUtil.getStatsMessageListener().getConsumers() < maxConsumers && statsUtil.getStatsMessageListener().getQueueSize() > maxMessagesOnQueuePerConsumer &&
                                quickSpawnMode) ||
                                (workerRequired && statsUtil.getStatsMessageListener().getConsumers() < maxConsumers)) {
                            LOGGER.debug("Starting a normal worker.");
                            workerRunner.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName);
                        }
                    }
                    //statsUtil.sendhighMemMessage();
                    LOGGER.debug("Poll High Memory Job Request queue");
                    final boolean highMemStatsAvailable = statsUtil.pollStatsBrokerHighMemJobQueue();
                    if (highMemStatsAvailable) {
                        workerCount += ((SubmissionWorkerRunner) workerRunnerHighMemory).getWorkerCount();
                        if(statsMessageListener.getConsumers() > 0){
                            quickSpawnMode =  ((statsMessageListener.getQueueSize()/ statsMessageListener.getConsumers()) > 4);
                        }
                        final boolean highMemWorkerRequired = statsMessageListener.newWorkersRequired(completionTimeTarget);
                        if ((statsMessageListener.getConsumers() < 5 && statsMessageListener.getQueueSize() > 0) ||
                                (highMemWorkerRequired && statsMessageListener.getConsumers() < 10)) {
                            LOGGER.debug("Starting a high memory worker.");
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
