package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.jms.master.Master;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.WorkerRunner;
import uk.ac.ebi.interpro.scan.jms.worker.EmbeddedWorkerFactory;
import uk.ac.ebi.interpro.scan.management.dao.StepExecutionDAO;
import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.*;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad.FastaFileLoadStep;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.*;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class AmqInterProScanMaster implements Master {

    private static final Logger LOGGER = Logger.getLogger(AmqInterProScanMaster.class);

    private JmsTemplate jmsTemplate;

    private final Object jmsTemplateLock = new Object();

    private Jobs jobs;

    private StepInstanceDAO stepInstanceDAO;

    private StepExecutionDAO stepExecutionDAO;
    
    private Destination workerJobRequestQueue;

    private WorkerRunner parallelWorkerRunner;

    private EmbeddedWorkerFactory embeddedWorkerFactory;

    private Integer numberOfEmbeddedWorkers;

    private List<Thread> workerThreads;

    private boolean shutdownCalled = false;

    private String fastaFilePath;

    private String outputFile;

    private String outputFormat;

    private List<String> analyses;

    /**
     * This boolean allows configuration of whether or not the Master closes down when there are no more
     * runnable StepExecutions available.
     */
    private boolean closeOnCompletion;

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
    public void setWorkerJobRequestQueue(Destination workerJobRequestQueue) {
        this.workerJobRequestQueue = workerJobRequestQueue;
    }

    @Required
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Required
    public void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO) {
        this.stepInstanceDAO = stepInstanceDAO;
    }

    @Required
    public void setStepExecutionDAO(StepExecutionDAO stepExecutionDAO) {
        this.stepExecutionDAO = stepExecutionDAO;
    }

    @Required
    public void setCloseOnCompletion(boolean closeOnCompletion) {
        this.closeOnCompletion = closeOnCompletion;
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
        try {
            startUpEmbeddedWorkers();
            createFastaFileLoadStepInstance();


            // If there is an embeddedWorkerFactory (i.e. this Master is running in stand-alone mode)
            // stop running if there are no StepInstances left to complete.
            while(! shutdownCalled){
                // TODO should be while(running) to allow shutdown.
                boolean completed=true;
                for (Job job : jobs.getJobList()){
                    // If the optional list of analyses has been passed in, only run those analyses.
                    // Otherwise, run all of them.

                    if (! job.isAnalysis() || analyses == null || analyses.contains(job.getId())){
                        for (Step step : job.getSteps()){
                            for (StepInstance stepInstance : stepInstanceDAO.retrieveUnfinishedStepInstances(step)){
                                completed=false;
                                LOGGER.debug("Step not yet completed:"+stepInstance+" "+stepInstance.getState());
                                if (stepInstance.canBeSubmitted(jobs) && stepInstanceDAO.serialGroupCanRun(stepInstance, jobs)){
                                    LOGGER.debug("Step submitted:"+stepInstance);
                                    sendMessage(stepInstance);
                                }
                            }
                        }
                    }
                }

                if (closeOnCompletion && completed) break;

                Thread.sleep (5000);  // Every 5 seconds, checks for any runnable StepInstances and runs them.
            }
        } catch (JMSException e) {
            LOGGER.error ("JMSException thrown by Master", e);
        } catch (Exception e){
            LOGGER.error ("Exception thrown by Master", e);
        }
        LOGGER.debug("Ending"); 
    }

    /**
     * If a fastaFilePath has been passed in as an argument, then StepInstances are created
     * for the fasta file loading job.  Note that this also creates all of the necessary StepInstances
     * for analyses for the loaded proteins.
     */
    private void createFastaFileLoadStepInstance() {
        if (fastaFilePath != null) {
            Map<String, String> params = new HashMap<String, String>(1);
            params.put(FastaFileLoadStep.FASTA_FILE_PATH_KEY, fastaFilePath);
            createStepInstancesForJob("jobLoadFromFasta", params);
            LOGGER.info ("Fasta file load step instance has been created.");
        }
    }

    /**
     * Called by quartz to load proteins from UniParc.
     */
    public void createProteinLoadJob(){
        createStepInstancesForJob("jobLoadFromUniParc", null);
    }

    private void createStepInstancesForJob (String jobId, Map<String, String> stepParameters){
        Job job = jobs.getJobById(jobId);
        for (Step step : job.getSteps()){
            StepInstance stepInstance = new StepInstance(step);
            if (stepParameters != null){
                for (String key : stepParameters.keySet()){
                    stepInstance.addStepParameter(key, stepParameters.get(key));
                }
            }
            stepInstanceDAO.insert(stepInstance);
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
     * @throws JMSException in the event of a failure sending the message to the JMS Broker.
     */
    @Transactional
    private void sendMessage(StepInstance stepInstance) throws JMSException {
        LOGGER.info ("Attempting to send message to queue.");
        final StepExecution stepExecution = stepInstance.createStepExecution();
        stepExecutionDAO.insert(stepExecution);
        stepExecution.submit(stepExecutionDAO);
        if (! jmsTemplate.isExplicitQosEnabled()){
            throw new IllegalStateException ("It is not possible to set the priority of the JMS message, as the JMSTemplate does not have explicitQosEnabled.");
        }

        final int priority = stepInstance.getStep(jobs).getSerialGroup() == null ? 4 : 8;

        synchronized (jmsTemplateLock){
            jmsTemplate.setPriority(priority);
            jmsTemplate.send(workerJobRequestQueue, new MessageCreator(){
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(stepExecution);
                }
            });
        }


        if (parallelWorkerRunner != null){ // Not mandatory (e.g. in single-jvm implementation)
            parallelWorkerRunner.startupNewWorker(priority);
        }
    }

    /**
     * If a fasta file path is set, load the proteins at start up and analyse them.
     * @param fastaFilePath from which to load the proteins at start up and analyse them.
     */
    @Override
    public void setFastaFilePath(String fastaFilePath) {
        this.fastaFilePath = fastaFilePath;
    }

    /**
     *
     * @param outputFile if set, then the results will be output to this file in the format specified in
     * the field outputFormat (defaulting to XML).
     */
    @Override
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * Allows the output format to be changed from the default XML.  If no value is specified for outputFile, this
     * value will be ignored.
     * @param outputFormat the output format.  If no value is specified for outputFile, this format
     * value will be ignored.
     */
    @Override
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * Optionally, set the analyses that should be run.  If not set (so analysisArray remains null),
     * all analyses will be run.
     * @param analyses the analyses that should be run.  If not set, all analyses will be run.
     */
    @Override
    public void setAnalyses(String analyses) {
        String[] analysisArray = analyses.split(":");
        this.analyses = Arrays.asList(analysisArray);
    }
}
