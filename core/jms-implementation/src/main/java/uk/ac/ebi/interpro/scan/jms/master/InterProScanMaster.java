package uk.ac.ebi.interpro.scan.jms.master;

import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.management.model.*;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStepInstance;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.LoadFastaFile;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Pretending to be the InterProScan master application.
 *
 * @author Phil Jones
 * @version $Id: TestMaster.java,v 1.4 2009/10/28 15:04:00 pjones Exp $
 * @since 1.0
 */
public class InterProScanMaster implements Master {

    private SessionHandler sessionHandler;

    private String jobSubmissionQueueName;

    private ResponseMonitor responseMonitor;

    private List<Job> jobs;

    private String managementRequestTopicName;

    private LoadFastaFile loader;

    private ProteinDAO proteinDAO;

    @Required
    public void setLoader(LoadFastaFile loader) {
        this.loader = loader;
    }

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

    public List<Job> getJobs() {
        return jobs;
    }

    @Required
    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public ProteinDAO getProteinDAO() {
        return proteinDAO;
    }

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    /**
     * Run the Master Application.
     */
    public void start(){
        try {
            // Start the response monitor thread
            Thread responseMonitorThread = new Thread(responseMonitor);
            responseMonitorThread.start();

            // Initialise the sessionHandler for the master thread
            sessionHandler.init();

            List<StepInstance> stepInstances = buildStepInstancesTheStupidWay();

            while(true){
//                sendMessage(jobSubmissionQueueName, "Message number " + i);  // Send a message every second or so.


                for (StepInstance stepInstance : stepInstances){
                    if (stepInstance.getState() == StepExecutionState.NEW_STEP_EXECUTION){
                        // Check if dependcies exist...
                        boolean canRun = stepInstance.stepInstanceDependsUpon() == null;
                        // If they do exist, check if they are (all) complete...
                        if (! canRun){
                            boolean allFinished = true;
                            for (Object obj : stepInstance.stepInstanceDependsUpon()){
                                 StepInstance preStep = (StepInstance) obj;
                                if (preStep.getState() != StepExecutionState.STEP_EXECUTION_SUCCESSFUL){
                                    allFinished = false;
                                    break;
                                }
                            }
                            canRun = allFinished;
                        }
                        if (canRun){
                            StepExecution stepExecution = stepInstance.createStepExecution();
//                            sendMessage(stepExecution.getStepInstance().getStep().getQueue().getName(), stepExecution);
                            // TODO - for the moment, just sending to the default job submission queue.
                            sendMessage(jobSubmissionQueueName, stepExecution);
                        }
                    }
                }
                Thread.sleep(2000);
            }

        } catch (JMSException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    private List<StepInstance> buildStepInstancesTheStupidWay() {
        List<StepInstance> stepInstances = new ArrayList<StepInstance>();
        Job job = jobs.iterator().next();

        // Load some proteins into the database.
        loader.loadSequences();

        // Then retrieve the first 99.
        List<Protein> proteins = proteinDAO.getProteinsBetweenIds(1l, 100l);

        for (Step step : job.getSteps()){
            if (step instanceof WriteFastaFileStep){
                stepInstances.add(new WriteFastaFileStepInstance(
                        UUID.randomUUID(),
                        (WriteFastaFileStep)step,
                        proteins,
                        1l,
                        100l
                ));
            }
        }
        return stepInstances;
    }

    /**
     * Just creates simple text messages to be sent to Worker nodes.
     * @param stepExecution being the StepExecution to send as a message
     * @throws JMSException in the event of a failure sending the message to the JMS Broker.
     */
    private void sendMessage(String destination, StepExecution stepExecution) throws JMSException {
        stepExecution.submit();
        MessageProducer producer = sessionHandler.getMessageProducer(destination);
        ObjectMessage message = sessionHandler.createObjectMessage(stepExecution);
        producer.send(message);
    }
}
