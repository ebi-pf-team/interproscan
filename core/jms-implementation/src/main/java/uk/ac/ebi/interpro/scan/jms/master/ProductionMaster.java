package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.SubmissionWorkerRunner;
import uk.ac.ebi.interpro.scan.jms.stats.StatsMessageListener;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStep;

import javax.jms.JMSException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 * Date: 26/07/12
 */
public class ProductionMaster extends AbstractMaster {

    private String tcpUri;

    private static final Logger LOGGER = Logger.getLogger(ProductionMaster.class.getName());

    private StatsUtil statsUtil;

    private String projectId;


    private int maxMessagesOnQueuePerConsumer = 4;
    private int completionTimeTarget = 2 * 60 * 60 * 1000;
    private static final int LOW_PRIORITY = 4;
    private static final int HIGH_PRIORITY = 8;
    private int maxConsumers;


    public void setSubmissionWorkerRunnerProjectId(String projectId){
        //set this as soon as the masters starts running
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner) this.workerRunner).setProjectId(projectId);
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setProjectId(projectId);
        }
    }


    public void setSubmissionWorkerRunnerUserDir(String userDir){
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner) this.workerRunner).setUserDir(userDir);
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setUserDir(userDir);
        }
    }

    /**
     * Run the Production Master Application.
     */
    public void run() {
        super.run();
        LOGGER.debug("Started Production Master run() method.");
        try {

            startNewWorker();

            while (!shutdownCalled) {
                for (StepInstance stepInstance : stepInstanceDAO.retrieveUnfinishedStepInstances()) {
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


                        final boolean resubmission = stepInstance.getExecutions().size() > 0;
                        if (resubmission) {
                            LOGGER.warn("StepInstance " + stepInstance.getId() + " is being re-run following a failure.");
                        }
                        final Step step = stepInstance.getStep(jobs);

                        //final boolean canRunRemotely = !step.isRequiresDatabaseAccess();
                        final boolean canRunRemotely = true;

                        // Serial groups should be high priority, however exclude WriteFastaFileStep from this
                        // as they are very abundant.
                        final int priority = step.getSerialGroup() == null || step instanceof WriteFastaFileStep
                                ? 4
                                : 8;

                        // Only set up message selectors for high memory requirements if a suitable worker runner has been set up.
                        // TODO - make the stepInstance fail more than once before using a highmem worker
                        final boolean useHighMemoryWorker = resubmission && workerRunnerHighMemory != null && canRunRemotely;

                        // Performed in a transaction.
                        messageSender.sendMessage(stepInstance, useHighMemoryWorker, priority, canRunRemotely);
                        statsUtil.addToSubmittedStepInstances(stepInstance);
                    }
                }
                progressReport();
                Thread.sleep(1000);   // Don't want to hammer Oracle with requests for step instances that can be run.
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Step instance statistics");
                    LOGGER.debug("Step instances left to run: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                    LOGGER.debug("Total StepInstances: " + stepInstanceDAO.count());
                }
                //update the statistics plugin
                statsUtil.setTotalJobs(stepInstanceDAO.count());
                statsUtil.setUnfinishedJobs(stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                statsUtil.displayMasterProgress();
            }
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by ProductionMaster: ", e);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by ProductionMaster: ", e);
        }
        LOGGER.debug("Ending");
    }

    /**
     * Called by quartz to load proteins from UniParc.
     */
    public void createProteinLoadJob() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("StatsUtil class =" + statsUtil.getClass().toString());
            LOGGER.debug("WorkerRunner class=" + workerRunner.getClass().toString());
            LOGGER.debug("StepInstanceDAO class=" + stepInstanceDAO.getClass().toString());
        }
        createStepInstancesForJob("jobLoadFromUniParc", null);
        LOGGER.debug("Finished creating uniparc step instance");
    }


    public StatsUtil getStatsUtil() {
        return statsUtil;
    }

    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }

    private void progressReport() {
        final StatsMessageListener statsMessageListener = statsUtil.getStatsMessageListener();
        LOGGER.debug("Poll Job Request Queue queue");

        final boolean highMemStatsAvailable = statsUtil.pollStatsBrokerHighMemJobQueue();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Production Run() - Response Stats: " + statsUtil.getStatsMessageListener().getStats());
        }


        statsUtil.pollStatsBrokerJobQueue();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Production Run() - RequestJobQueue Stats: " + statsUtil.getStatsMessageListener().getStats());
        }

        statsUtil.pollStatsBrokerResponseQueue();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Production Run() - Response Stats: " + statsUtil.getStatsMessageListener().getStats());
        }
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


    // A hack to try and get the quartz jobs running!
    // Necessary as only the master can pass on a tcpUri
    public  void startNewWorkerForQuartzJob() {

        LOGGER.debug("Quartz scheduler starting new worker:");
        final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();
        final int priority = (Math.random() < 0.5) ? 4 : 8;
        workerRunner.startupNewWorker(priority, tcpUri, temporaryDirectoryName);
    }

    /**
     * Mechanism to start a new worker
     *
     */
    private void startNewWorker(){
        //we want an efficient way of creating workers
        //create two workers : one high memory and one non high memory
        final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();

        LOGGER.debug("Starting the first FOUR normal worker.");
        for (int i=0;i<3;i++){
            workerRunner.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName);
        }
        LOGGER.debug("Starting the first high memory worker...");
        // high memory do have a higher priority compared to low memory workers
        workerRunnerHighMemory.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName,true);

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            public void run() {
                //start new workers
                int workerCount = 0;
                boolean quickSpawnMode = false;
                while (!shutdownCalled) {
                    //statsUtil.sendMessage();
                    final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();
                    final StatsMessageListener statsMessageListener = statsUtil.getStatsMessageListener();

                    LOGGER.debug("Poll Job Request queue");
                    final boolean statsAvailable = statsUtil.pollStatsBrokerJobQueue();
                    if (statsAvailable) {
                        workerCount = ((SubmissionWorkerRunner) workerRunner).getWorkerCount();
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Worker count=" + workerCount);
                        }
                        if(statsMessageListener.getConsumers() > 0){
                            quickSpawnMode =  ((statsMessageListener.getQueueSize()/ statsMessageListener.getConsumers()) > 4);
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Quickspawn mode=" + quickSpawnMode);
                            }
                        }
                        final boolean workerRequired = statsMessageListener.newWorkersRequired(completionTimeTarget);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Worker required=" + workerRequired);
                            LOGGER.debug("StatsUtil.getConsumers=" + statsUtil.getStatsMessageListener().getConsumers());
                            LOGGER.debug("Max consumers=" + maxConsumers);
                            LOGGER.debug("StatsUtil.getQueueSize=" + statsUtil.getStatsMessageListener().getQueueSize());
                            LOGGER.debug("Max messages on queue per consumer=" + maxMessagesOnQueuePerConsumer);
                        }

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
                    if (LOGGER.isDebugEnabled()) {
                        if (!responseQueueStatsAvailable) {
                            LOGGER.debug("The Job Response queue is not initialised");
                        } else {
                            LOGGER.debug("JobResponseQueue:  " + statsMessageListener.getStats().toString());
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

    @Required
    public void setMaxConsumers(int maxConsumers) {
        this.maxConsumers = maxConsumers;
    }


}
