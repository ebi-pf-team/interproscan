package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.io.model.Hmmer3ModelLoader;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.QueueJumper;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.WorkerRunner;
import uk.ac.ebi.interpro.scan.management.dao.StepExecutionDAO;
import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.*;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;
import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The InterProScan master application.
 *
 * @author Phil Jones
 * @version $Id: TestMaster.java,v 1.4 2009/10/28 15:04:00 pjones Exp $
 * @since 1.0
 */
public class InterProScanMaster implements Master {

    private static final Logger LOGGER = Logger.getLogger(InterProScanMaster.class);

    private String jmsBrokerHostName;

    private int jmsBrokerPort;

    private String jobSubmissionQueueName;

    private ResponseMonitor responseMonitor;

    private Jobs jobs;

//    private volatile Map<String, StepInstance> stepInstances = new HashMap<String, StepInstance>();
//
    private volatile Map<Long, StepExecution> stepExecutions = new HashMap<Long, StepExecution>();

    private String managementRequestTopicName;

    private StepInstanceDAO stepInstanceDAO;

    private StepExecutionDAO stepExecutionDAO;

    private GenericDAO signatureLibraryReleaseDAO;

    private QueueJumper queueJumper;

    private WorkerRunner serialWorkerRunner;

    private String pfamHMMfilePath;

    private MessageProducer producer;

    @Required
    public void setJmsBrokerHostName(String jmsBrokerHostName) {
        this.jmsBrokerHostName = jmsBrokerHostName;
    }

    @Required
    public void setJmsBrokerPort(int jmsBrokerPort) {
        this.jmsBrokerPort = jmsBrokerPort;
    }

    /**
     * Sets the task submission queue name.  This is the queue that new
     * jobs are placed on to, prior to be pushed on to the requestQueue
     * from where they are taken by a worker node.
     * @param jobSubmissionQueueName
     */
    @Required
    public void setJobSubmissionQueueName(String jobSubmissionQueueName) {
        this.jobSubmissionQueueName = jobSubmissionQueueName;
    }

    @Required
    public void setQueueJumper(QueueJumper queueJumper) {
        this.queueJumper = queueJumper;
    }

    @Required
    public void setSerialWorkerRunner(uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.WorkerRunner serialWorkerRunner) {
        this.serialWorkerRunner = serialWorkerRunner;
    }

    /**
     * Sets the name of the topic to which Worker management requests
     * should be sent, for multicast to all of the Worker clients.
     *
     * @param managementRequestTopicName the name of the topic to which Worker management requests
     *                                   should be sent, for multicast to all of the Worker clients.
     */
    @Override
    public void setManagementRequestTopicName(String managementRequestTopicName) {
        this.managementRequestTopicName = managementRequestTopicName;
    }

    /**
     * Sets the ResponseMonitor which will handle any responses from
     * the Worker nodes.
     * @param responseMonitor which will handle any responses from
     * the Worker nodes.
     */
    @Required
    public void setResponseMonitor(ResponseMonitor responseMonitor){
        this.responseMonitor = responseMonitor;
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
    public void setSignatureLibraryReleaseDAO(GenericDAO signatureLibraryReleaseDAO) {
        this.signatureLibraryReleaseDAO = signatureLibraryReleaseDAO;
    }

    public Jobs getJobs() {
        return jobs;
    }

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    @Required
    public void setPfamHMMfilePath(String pfamHMMfilePath) {
        this.pfamHMMfilePath = pfamHMMfilePath;
    }

    /**
     * Run the Master Application.
     */
    public void start(){
        SessionHandler sessionHandler = null;
        try {
            // Start the response monitor thread
            Thread responseMonitorThread = new Thread(responseMonitor);
            responseMonitor.setStepExecutionMap(stepExecutions);
            responseMonitorThread.start();

            // Start up the Thread that monitors the taskSubmission queue.
            Thread queueMonitorThread = new Thread (queueJumper);
            queueMonitorThread.start();

            // Start up the serial worker
            serialWorkerRunner.startupNewWorker();

            // Initialise the sessionHandler for the master thread
            sessionHandler = new SessionHandler(jmsBrokerHostName, jmsBrokerPort);

            producer = sessionHandler.getMessageProducer(jobSubmissionQueueName);
//            loadPfamModels();
            while(true){
                for (Job job : jobs.getJobList()){
                    for (Step step : job.getSteps()){
                        for (StepInstance stepInstance : stepInstanceDAO.retrieveUnfinishedStepInstances(step)){
                            if (stepInstance.canBeSubmitted(jobs)){
                                StepExecution stepExecution = stepInstance.createStepExecution();
                                stepExecutionDAO.insert(stepExecution);
                                stepExecutions.put(stepExecution.getId(), stepExecution);
                                sendMessage(stepExecution, sessionHandler);
                            }
                        }
                    }
                }
            }

        } catch (JMSException e) {
            LOGGER.error ("JMSException", e);
            System.exit(1);
        } catch (Exception e){
            LOGGER.error ("Exception", e);
            System.exit(1);
        }

        finally {
            if (sessionHandler != null){
                try {
                    sessionHandler.close();
                } catch (JMSException e) {
                    LOGGER.error ("JMSException thrown when attempting to close the SessionHandler.", e);
                }
            }
        }
    }

    /**
     * Called by quartz to load some more proteins.
     */
    public void createProteinLoadJob(){
        Job insertProteinJob = jobs.getJobById("jobLoadFromUniParc");
        for (Step step : insertProteinJob.getSteps()){
            StepInstance stepInstance = new StepInstance(
                    step,
                    null,
                    null,
                    null,
                    null
            );
            stepInstanceDAO.insert(stepInstance);
        }
    }


    private void loadPfamModels() {
        // Load the models into the database.

        // Parse and retrieve the signatures.
        Hmmer3ModelLoader modelLoader = new Hmmer3ModelLoader(SignatureLibrary.PFAM, "24.0");
        SignatureLibraryRelease release = null;
        try{
            release = modelLoader.parse(pfamHMMfilePath);
        } catch (IOException e) {
            LOGGER.debug("IOException thrown when parsing HMM file.");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.exit(1);
        }

        // And store the Models / Signatures to the database.
        LOGGER.debug("Storing SignatureLibraryRelease...");
        signatureLibraryReleaseDAO.insert(release);
        LOGGER.debug("Storing SignatureLibraryRelease...DONE");

    }

    /**
     * Just creates simple text messages to be sent to Worker nodes.
     * @param stepExecution being the StepExecution to send as a message
     * @throws JMSException in the event of a failure sending the message to the JMS Broker.
     */
    private void sendMessage(StepExecution stepExecution, SessionHandler sessionHandler) throws JMSException {
        stepExecution.submit(stepExecutionDAO);
        ObjectMessage message = sessionHandler.createObjectMessage(stepExecution);
        final StepInstance stepInstance = stepExecution.getStepInstance();
        assert stepInstance != null;
        message.setBooleanProperty("parallel", stepInstance.getStep(jobs).isParallel());
        producer.send(message);
    }

    private int runningStepExecutions(){
        int running = 0;
        for (StepExecution exec : stepExecutions.values()){
            if (exec.getState() == StepExecutionState.STEP_EXECUTION_SUBMITTED ||
                    exec.getState() == StepExecutionState.STEP_EXECUTION_RUNNING){
                running++;
            }
        }
        return running;
    }
}
