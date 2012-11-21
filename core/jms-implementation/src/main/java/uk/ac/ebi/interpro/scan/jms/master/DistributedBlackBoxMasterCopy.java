package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.jms.stats.StatsMessageListener;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStep;

import javax.jms.JMSException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
public class DistributedBlackBoxMasterCopy extends AbstractBlackBoxMaster {

    private static final Logger LOGGER = Logger.getLogger(DistributedBlackBoxMasterCopy.class.getName());

    private String tcpUri;

    private StatsUtil statsUtil;

    private int maxMessagesOnQueuePerConsumer = 4;

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
        super.run();
        try {
            loadInMemoryDatabase();

            int stepInstancesCreatedByLoadStep = createStepInstances();


            //this will start a new thread to create new workers
            startNewWorker();

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
                        countRegulator++;
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
                //check what is not completed
                LOGGER.debug("Distributed Master has no jobs but .. more Jobs may get generated ");
                LOGGER.debug("Step instances left to run: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                LOGGER.debug("Total StepInstances: " + stepInstanceDAO.count());
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

    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
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
                while (!shutdownCalled) {
                    //statsUtil.sendMessage();
                    final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();
                    final StatsMessageListener statsMessageListener = statsUtil.getStatsMessageListener();

                    LOGGER.debug("Poll Job Request Queue queue");
                    final boolean statsAvailable = statsUtil.pollStatsBrokerJobQueue();
                    if (statsAvailable) {
                        final boolean workerRequired = statsMessageListener.newWorkersRequired(completionTimeTarget);
                        if ((statsUtil.getStatsMessageListener().getConsumers() < 30 && statsUtil.getStatsMessageListener().getQueueSize() > maxMessagesOnQueuePerConsumer) ||
                                (workerRequired && statsUtil.getStatsMessageListener().getConsumers() < 30)) {
                            LOGGER.debug("Starting a normal worker.");
                            workerRunner.startupNewWorker(LOW_PRIORITY, tcpUri, temporaryDirectoryName);
                        }
                    }
                    //statsUtil.sendhighMemMessage();
                    LOGGER.debug("Poll High Memory Job Request queue");
                    final boolean highMemStatsAvailable = statsUtil.pollStatsBrokerHighMemJobQueue();
                    if (highMemStatsAvailable) {
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
                        Thread.sleep(1 * 10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

            }
        });
    }
}
