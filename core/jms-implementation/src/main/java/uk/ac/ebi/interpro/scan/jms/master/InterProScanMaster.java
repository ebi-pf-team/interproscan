package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.jms.broker.EmbeddedBroker;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.WorkerRunner;
import uk.ac.ebi.interpro.scan.jms.worker.EmbeddedWorkerFactory;
import uk.ac.ebi.interpro.scan.management.dao.StepExecutionDAO;
import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.*;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad.FastaFileLoadStep;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The InterProScan master application.
 *
 * @author Phil Jones
 * @version $Id: TestMaster.java,v 1.4 2009/10/28 15:04:00 pjones Exp $
 * @since 1.0
 */
public class InterProScanMaster implements Master {

    private static final Logger LOGGER = Logger.getLogger(InterProScanMaster.class);

    private Jobs jobs;

    private StepInstanceDAO stepInstanceDAO;

    private StepExecutionDAO stepExecutionDAO;

    private ConnectionFactory connectionFactory;

    private String workerJobResponseQueueName;

    private String workerJobRequestQueueName;

    private WorkerRunner parallelWorkerRunner;

    private EmbeddedBroker embeddedBroker;

    private EmbeddedWorkerFactory embeddedWorkerFactory;

    private Integer numberOfEmbeddedWorkers;

    private List<Thread> workerThreads;

    private boolean shutdownCalled = false;

    private String fastaFilePath;

    private String outputFile;

    private String outputFormat;

    private List<String> analyses;

    /**
     * This OPTIONAL bean method allows an Embedded JMS broker to be injected.
     * If not injected, the Master will make no attempt to runBroker a Broker, but
     * rely on an external one being present.
     *
     * @param embeddedBroker implementation, e.g. for HornetQ, ActiveMQ.
     */
    public void setEmbeddedBroker(EmbeddedBroker embeddedBroker) {
        this.embeddedBroker = embeddedBroker;
    }

    /**
     * This OPTIONAL bean method allows an embedded Worker to be injected.
     *
     * @param embeddedWorkerFactory to do all the work!?
     */
    public void setEmbeddedWorkerFactory(EmbeddedWorkerFactory embeddedWorkerFactory) {
        this.embeddedWorkerFactory = embeddedWorkerFactory;
    }

    public void setEmbeddedWorkerCount(Integer numberOfEmbeddedWorkers){
        this.numberOfEmbeddedWorkers = numberOfEmbeddedWorkers;
    }

    public void setParallelWorkerRunner(WorkerRunner workerRunner) {
        this.parallelWorkerRunner = workerRunner;
    }

    @Required
    public void setWorkerJobRequestQueueName(String workerJobRequestQueueName) {
        this.workerJobRequestQueueName = workerJobRequestQueueName;
    }

    /**
     * Sets the name of the destinationResponseQueue.
     * @param workerJobResponseQueueName the name of the destinationResponseQueue.
     */
    @Required
    public void setWorkerJobResponseQueueName(String workerJobResponseQueueName) {
        this.workerJobResponseQueueName = workerJobResponseQueueName;
    }

    @Required
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }


    @Required
    public void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO) {
        this.stepInstanceDAO = stepInstanceDAO;
    }

    @Required
    public void setStepExecutionDAO(StepExecutionDAO stepExecutionDAO) {
        this.stepExecutionDAO = stepExecutionDAO;
    }



    public Jobs getJobs() {
        return jobs;
    }

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }


    /**
     * Run the Master Application.
     */
    public void run(){
//        ShutdownHook shutDownHook = new ShutdownHook(this);
//        Runtime.getRuntime().addShutdownHook(shutDownHook);

        SessionHandler sessionHandler = null;
        try {
            startUpEmbeddedBroker();
            startUpEmbeddedWorkers();
            createFastaFileLoadStepInstance();
            // Initialise the sessionHandler for the master thread
            sessionHandler = new SessionHandler(connectionFactory);

            final MessageConsumer jobResponseMessageConsumer = sessionHandler.getMessageConsumer(workerJobResponseQueueName);
            final MessageProducer jobRequestMessageProducer = sessionHandler.getMessageProducer(workerJobRequestQueueName);

            ResponseMonitorImpl responseMonitor = new ResponseMonitorImpl(stepExecutionDAO);
            jobResponseMessageConsumer.setMessageListener(responseMonitor);

            sessionHandler.start();

            // If there is an embeddedWorkerFactory (i.e. this Master is running in stand-alone mode)
            // stop running if there are no StepInstances left to complete.
            while(! shutdownCalled && (embeddedWorkerFactory == null || stepInstanceDAO.futureStepsAvailable())){       // TODO should be while(running) to allow shutdown.
                for (Job job : jobs.getJobList()){
                    // If the optional list of analyses has been passed in, only run those analyses.
                    // Otherwise, run all of them.
                    if (! job.isAnalysis() || analyses == null || analyses.contains(job.getId())){
                        for (Step step : job.getSteps()){
                            for (StepInstance stepInstance : stepInstanceDAO.retrieveUnfinishedStepInstances(step)){
                                if (stepInstance.canBeSubmitted(jobs) && stepInstanceDAO.serialGroupCanRun(stepInstance, jobs)){
                                    sendMessage(stepInstance, sessionHandler, jobRequestMessageProducer);
                                }
                            }
                        }
                    }
                }
                Thread.sleep (5000);  // Every 5 seconds, checks for any runnable StepInstances and runs them.
            }
        } catch (JMSException e) {
            LOGGER.error ("JMSException thrown by Master", e);
        } catch (Exception e){
            LOGGER.error ("Exception thrown by Master", e);
        }

        finally {
            // Shut down session.
            if (sessionHandler != null){
                try {
                    sessionHandler.close();
                } catch (JMSException e) {
                    LOGGER.error ("JMSException thrown when attempting to close the SessionHandler.", e);
                }
            }
            shutdown();
        }
    }

    /**
     * If a fastaFilePath has been passed in as an argument, then StepInstances are created
     * for the fasta file loading job.  Note that this also creates all of the necessary StepInstances
     * for analyses for the loaded proteins.
     */
    private void createFastaFileLoadStepInstance() {
        if (fastaFilePath == null) return;

        Job insertProteinJob = jobs.getJobById("jobLoadFromFasta");
        for (Step step : insertProteinJob.getSteps()){
            StepInstance stepInstance = new StepInstance(step);
            stepInstance.addStepParameter(FastaFileLoadStep.FASTA_FILE_PATH_KEY, fastaFilePath);
            stepInstanceDAO.insert(stepInstance);
        }

    }

    /**
     * Called by quartz to load some more proteins.
     */
    public void createProteinLoadJob(){
        Job insertProteinJob = jobs.getJobById("jobLoadFromUniParc");
        for (Step step : insertProteinJob.getSteps()){
            StepInstance stepInstance = new StepInstance(step);
            stepInstanceDAO.insert(stepInstance);
        }
    }

    private void startUpEmbeddedBroker(){
        if (embeddedBroker != null){
            embeddedBroker.runBroker();
            LOGGER.info("Embedded broker started.");
        }
    }

    private void startUpEmbeddedWorkers(){
        if (embeddedWorkerFactory != null){
            if (workerThreads == null){
                workerThreads = new ArrayList<Thread>(numberOfEmbeddedWorkers);
            }
            // Has the number of embedded workers been set?
            final int processorCount = Runtime.getRuntime().availableProcessors();
            if (numberOfEmbeddedWorkers == null){
                // Default to the number of processors on this box.
                numberOfEmbeddedWorkers = processorCount;
                LOGGER.info ("Embedded worker count defaulting to processor count: " + numberOfEmbeddedWorkers);
            }
            else {
                if (numberOfEmbeddedWorkers > processorCount){
                    LOGGER.warn("WARNING: This stand-alone I5 installation has been configured to start up " + numberOfEmbeddedWorkers + " workers, however there are only " + numberOfEmbeddedWorkers + " available on this machine.  This is likely to be detrimental to performance.  Consider reducing the number of workers.");
                }
            }
            for (int i = 0; i < numberOfEmbeddedWorkers; i++){
                Thread workerThread = new Thread(embeddedWorkerFactory.getInstance());
                workerThreads.add(workerThread);
                workerThread.start();
                LOGGER.info ("Worker thread started.");
            }
        }
    }

    /**
     * Creates messages to be sent to Worker nodes.
     * Does all of this in a transaction, hence in this separate method.
     * @param stepInstance to send as a message
     * @param sessionHandler used to create the ObjectMessage.
     * @param producer being the MessageProducer upon which to send the message.
     * @throws JMSException in the event of a failure sending the message to the JMS Broker.
     */
    @Transactional
    private void sendMessage(StepInstance stepInstance, SessionHandler sessionHandler, MessageProducer producer) throws JMSException {
        final StepExecution stepExecution = stepInstance.createStepExecution();
        stepExecutionDAO.insert(stepExecution);
        stepExecution.submit(stepExecutionDAO);
        ObjectMessage message = sessionHandler.createObjectMessage(stepExecution);
        producer.setPriority(stepInstance.getStep(jobs).getSerialGroup() == null ? 4 : 7);
        LOGGER.debug ("Placing message on to destination " + producer.getDestination().toString());
        producer.send(message);
        if (parallelWorkerRunner != null){ // Not mandatory (e.g. in single-jvm implementation)
            parallelWorkerRunner.startupNewWorker();
        }
    }

    private void shutdown(){
        // Shut down broker.
        if (embeddedBroker != null){
            embeddedBroker.shutDownBroker();
        }
    }

    /**
     * If a fasta file path is set, load the proteins at start up and analyse them.
     * @param fastaFilePath from which to load the proteins at start up and analyse them.
     */
    public void setFastaFilePath(String fastaFilePath) {
        this.fastaFilePath = fastaFilePath;
    }

    /**
     *
     * @param outputFile if set, then the results will be output to this file in the format specified in
     * the field outputFormat (defaulting to XML).
     */
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * Allows the output format to be changed from the default XML.  If no value is specified for outputFile, this
     * value will be ignored.
     * @param outputFormat the output format.  If no value is specified for outputFile, this format
     * value will be ignored.
     */
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * Optionally, set the analyses that should be run.  If not set (so analysisArray remains null),
     * all analyses will be run.
     * @param analyses the analyses that should be run.  If not set, all analyses will be run.
     */
    public void setAnalyses(String analyses) {
        String[] analysisArray = analyses.split(":");
        this.analyses = Arrays.asList(analysisArray);
    }
}
