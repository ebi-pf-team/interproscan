package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import javax.jms.*;
import java.io.File;
import java.lang.IllegalStateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class AmqInterProScanWorker implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(AmqInterProScanWorker.class);

    private JmsTemplate jmsTemplate;

    private Destination jobResponseQueue;

    private Jobs jobs;

    private List<String> validatedDirectories = new ArrayList<String>();

    private Long startTime;

    private Long lastActivityTime;

    private Double workDone;

    private StepExecution currentStepExecution;

    private final UUID uniqueWorkerIdentification = UUID.randomUUID();

    AmqInterProScanWorker() {
    }

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    @Required
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Sets the JMS queue to which the Worker returns the results of a job.
     * @param jobResponseQueue  the name of the JMS queue to which the Worker returns the results of a job.
     */
    @Required
    public void setJobResponseQueue(Destination jobResponseQueue) {
        this.jobResponseQueue = jobResponseQueue;
    }

    /**
     * This method should return:
     * null, if the amount of work done is unknown
     * a value between 0 and 1 if the proportion of work done is known.
     *
     * @return null, if the amount of work done is unknown
     *         a value between 0 and 1 if the proportion of work done is known.
     */
    public Double getProportionOfWorkDone() {
        return workDone;
    }

    @Override
    public void onMessage(Message message) {
        lastActivityTime = new Date().getTime();
        try{
            if (message != null){
                message.acknowledge();     // Acknowledge receipt of the message.
                LOGGER.debug("Message received from queue.  JMS Message ID: " + message.getJMSMessageID());

                if (! (message instanceof ObjectMessage)){
                    LOGGER.error ("Received a message of an unknown type (non-ObjectMessage)");
                    return;
                }
                ObjectMessage stepExecutionMessage = (ObjectMessage) message;
                final StepExecution stepExecution = (StepExecution) stepExecutionMessage.getObject();
                currentStepExecution = stepExecution;
                if (stepExecution == null){
                    LOGGER.error ("An ObjectMessage was received but had no contents.");
                    return;
                }
                LOGGER.debug("Message received of queue - attempting to executeInTransaction");

                try{
                    executeInTransaction(stepExecution);
                } catch (Exception e) {
//todo: reinstate self termination for remote workers. Disabled to make process more robust for local workers.
                    //            running = false;
                    LOGGER.error ("Execution thrown when attempting to executeInTransaction the StepExecution.  All database activity rolled back.", e);
                    // Something went wrong in the execution - try to send back failure
                    // message to the broker.  This in turn may fail if it is the JMS connection
                    // that failed during the execution.
                    stepExecution.fail();

                    jmsTemplate.send(jobResponseQueue, new MessageCreator(){
                        public Message createMessage(Session session) throws JMSException {
                            return session.createObjectMessage(stepExecution);
                        }
                    });

                    LOGGER.debug ("Message returned to the broker to indicate that the StepExecution has failed: " + stepExecution.getId());
                }
            }
        }
        catch (JMSException e) {
            LOGGER.error ("JMSException thrown in MessageListener.", e);
        }
    }

    /**
     * Executing the StepInstance and responding to the JMS Broker
     * if the execution is successful.
     * @param stepExecution          The StepExecution to run.
     */
    @Transactional
    private void executeInTransaction(final StepExecution stepExecution){
        stepExecution.setToRun();
        final StepInstance stepInstance = stepExecution.getStepInstance();
        final Step step = stepInstance.getStep(jobs);
        LOGGER.debug("Step ID: "+ step.getId());
        LOGGER.debug("Step instance: " + stepInstance);
        LOGGER.debug("Step execution id: " + stepExecution.getId());
        step.execute(stepInstance, getValidWorkingDirectory(step));
        stepExecution.completeSuccessfully();
        LOGGER.debug ("Successful run of Step.executeInTransaction() method for StepExecution ID: " + stepExecution.getId());

        jmsTemplate.send(jobResponseQueue, new MessageCreator(){
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage(stepExecution);
            }
        });

        LOGGER.debug("Followed by successful reply to the JMS Broker and acknowledgement of the message.");
    }

    /**
     * Builds the path to the directory in which the work should be performed and checks that it
     * exists and is writable.  (Also attempts to create this directory if it does not exist.)
     * @param step being the step for which the directory should be returned
     * @return the path of the working directory.
     */
    String getValidWorkingDirectory(Step step){
        final String directory = new StringBuilder()
                .append(jobs.getBaseDirectoryTemporaryFiles())
                .append('/')
                .append(step.getJob().getId())
                .toString();
        // Check (just the once) that the working directory exists.
        if (! validatedDirectories.contains(directory)){
            final File file = new File (directory);
            if (! file.exists()){
                if (! file.mkdirs()){
                    throw new IllegalStateException("Unable to create the working directory " + directory);
                }
            }
            else if (! file.isDirectory()){
                throw new IllegalStateException ("The path " + directory + " exists, but is not a directory.");
            }
            else if (! file.canWrite()){
                throw new IllegalStateException("Unable to write to the directory " + directory);
            }
            validatedDirectories.add(directory);
        }
        return directory;
    }

    /**
     * Returns true if after calling System.gc, there is no
     * evidence of a memory leak.
     *
     * Conservative - looks for used memory being no more
     * than 1/3 of Xmx.
     * @return true if all is OK.
     */
    private boolean possibleMemoryLeakDetected() {

        System.gc();

        final long Xmx = Runtime.getRuntime().maxMemory();
        final long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        final boolean leaky = Xmx / used < 3;
        if (leaky){
            LOGGER.error("The JVM is full - closing down?");
        }
        return leaky;
    }

    public Long getStartTime() {
        return startTime;
    }

    public UUID getUniqueWorkerIdentification() {
        return uniqueWorkerIdentification;
    }

    public StepExecution getCurrentStepExecution() {
        return currentStepExecution;
    }
}

