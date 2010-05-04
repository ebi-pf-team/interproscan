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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class StepExecutionTransactionImpl implements StepExecutionTransaction {

    private static final Logger LOGGER = Logger.getLogger(StepExecutionTransactionImpl.class.getName());

    private List<String> validatedDirectories = new ArrayList<String>();

    private final Object validDirectoryLock = new Object();

    private Jobs jobs;

    private JmsTemplate jmsTemplate;

    private Destination jobResponseQueue;

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    @Required
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Required
    public void setJobResponseQueue(Destination jobResponseQueue) {
        this.jobResponseQueue = jobResponseQueue;
    }

    /**
     * Executing the StepInstance and responding to the JMS Broker
     * if the execution is successful.
     * @param stepExecution          The StepExecution to run.
     * @param message
     */
    @Transactional
    public void executeInTransaction(final StepExecution stepExecution, Message message){
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

        try{
            message.acknowledge();
        } catch (JMSException e) {
            throw new IllegalStateException ("Unable to acknowledge message.");
        }
        LOGGER.debug("Followed by successful reply to the JMS Broker and acknowledgement of the message.");
    }

        /**
     * Builds the path to the directory in which the work should be performed and checks that it
     * exists and is writable.  (Also attempts to create this directory if it does not exist.)
     * @param step being the step for which the directory should be returned
     * @return the path of the working directory.
     */
    private String getValidWorkingDirectory(Step step){
        final String directory = new StringBuilder()
                .append(jobs.getBaseDirectoryTemporaryFiles())
                .append('/')
                .append(step.getJob().getId())
                .toString();
        // Check (just the once) that the working directory exists.
        synchronized(validDirectoryLock){
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
        }
        return directory;
    }
}
