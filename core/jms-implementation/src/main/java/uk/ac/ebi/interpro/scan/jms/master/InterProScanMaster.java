package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.io.model.Hmmer3ModelLoader;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
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
 * Pretending to be the InterProScan master application.
 *
 * @author Phil Jones
 * @version $Id: TestMaster.java,v 1.4 2009/10/28 15:04:00 pjones Exp $
 * @since 1.0
 */
public class InterProScanMaster implements Master {

    private static final Logger LOGGER = Logger.getLogger(InterProScanMaster.class);

    private SessionHandler sessionHandler;

    private String jobSubmissionQueueName;

    private ResponseMonitor responseMonitor;

    private Jobs jobs;

//    private volatile Map<String, StepInstance> stepInstances = new HashMap<String, StepInstance>();
//
    private volatile Map<String, StepExecution> stepExecutions = new HashMap<String, StepExecution>();

    private String managementRequestTopicName;

    private StepInstanceDAO stepInstanceDAO;

    private StepExecutionDAO stepExecutionDAO;

    private GenericDAO signatureLibraryReleaseDAO;



    private String pfamHMMfilePath;

    private MessageProducer producer;

    /**
     * Sets the SessionHandler.  This looks after connecting to the
     * Broker and allowing messages to be put on the queue / taken off the queue.
     * @param sessionHandler  looks after connecting to the
     * Broker and allowing messages to be put on the queue / taken off the queue.
     */
    @Required
    public void setSessionHandler(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
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
        try {
            // Start the response monitor thread
            Thread responseMonitorThread = new Thread(responseMonitor);
            responseMonitor.setStepExecutionMap(stepExecutions);
            responseMonitorThread.start();
            // Initialise the sessionHandler for the master thread
            sessionHandler.init();

            producer = sessionHandler.getMessageProducer(jobSubmissionQueueName);
            buildStepInstancesTheStupidWay();
            while(true){
                for (Job job : jobs.getJobList()){
                    for (Step step : job.getSteps()){
                        LOGGER.debug("In InterProScanMaster.start().  About to retrieve step instances from the database.");
                        for (StepInstance stepInstance : stepInstanceDAO.retrieveInstances(step)){
                            if (stepInstance.canBeSubmitted(jobs)){
                                StepExecution stepExecution = stepInstance.createStepExecution();
                                stepExecutionDAO.insert(stepExecution);
                                stepExecutions.put(stepExecution.getId(), stepExecution);
                                sendMessage(stepExecution);
                            }
                        }
                    }
                }
                Thread.sleep(2000);
            }

        } catch (JMSException e) {
            LOGGER.error ("JMSException", e);
            System.exit(1);
        } catch (InterruptedException e) {
            LOGGER.error ("InterruptedException", e);
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
                    e.printStackTrace();
                }
            }
        }
    }

    private void buildStepInstancesTheStupidWay() {
        // TODO - Note that this method is just a HACK to create a structure of steps that
        // TODO - can be run for the demonstration - the mechanism to build a real set of
        // TODO - steps, following the addition of new proteins has yet to be written.
        Job insertProteinJob = jobs.getJobById("jobLoadFromFasta");
        for (Step step : insertProteinJob.getSteps()){
            System.out.println("step being iterated by buildStepInstancesTheStupidWay.  = " + step);
            StepInstance stepInstance = new StepInstance(
                    step,
                    null,
                    null,
                    null,
                    null
            );
            stepInstance.addStepParameter("testKey", "testValue");
            stepInstanceDAO.insert(stepInstance);
        }

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
    private void sendMessage(StepExecution stepExecution) throws JMSException {
        stepExecution.submit(stepExecutionDAO);
        ObjectMessage message = sessionHandler.createObjectMessage(stepExecution);
        final StepInstance stepInstance = stepExecution.getStepInstance();
        assert stepInstance != null;
        message.setBooleanProperty("parallel", stepInstance.getStep(jobs).isParallel());
        producer.send(message);
    }
}
