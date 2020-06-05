package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.io.TemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import javax.jms.*;
import java.io.File;
import java.lang.IllegalStateException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class performs the execution in a transaction.  This has been factored out of the WorkerListener class
 * to ensure that the transaction semantics work...
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class StepExecutionTransactionImpl implements StepExecutionTransaction {

    private static final Logger LOGGER = LogManager.getLogger(StepExecutionTransactionImpl.class.getName());

    private List<String> validatedDirectories = new ArrayList<String>();

    private static final Object validDirectoryLock = new Object();

    public static final String CAN_RUN_REMOTELY_PROPERTY = "remote";

    private Jobs jobs;

    private JmsTemplate jmsTemplate;

    private Destination jobResponseQueue;

    private TemporaryDirectoryManager directoryManager;

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    //    @Required
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Required
    public void setJobResponseQueue(Destination jobResponseQueue) {
        this.jobResponseQueue = jobResponseQueue;
    }

    @Required
    public void setDirectoryManager(TemporaryDirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }

    /**
     * Executing the StepInstance and responding to the JMS Broker
     * if the execution is successful.
     *
     * @param stepExecution The StepExecution to run.
     * @param message
     */
    @Transactional
    public void executeInTransaction(final StepExecution stepExecution, Message message) {
        stepExecution.setToRun();
        final StepInstance stepInstance = stepExecution.getStepInstance();
        final Step step = stepInstance.getStep(jobs);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Step ID: " + step.getId());
            LOGGER.debug("Step instance: " + stepInstance);
            LOGGER.debug("Step execution id: " + stepExecution.getId());
            LOGGER.debug("Step: " + stepExecution.toString());
        }
        final boolean canRunRemotely = !step.isRequiresDatabaseAccess();

        step.execute(stepInstance, getValidWorkingDirectory(step));
        stepExecution.completeSuccessfully();

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Successful run of Step.executeInTransaction() method for StepExecution ID: " + stepExecution.getId());

        jmsTemplate.send(jobResponseQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                final ObjectMessage message = session.createObjectMessage(stepExecution);
                message.setBooleanProperty(CAN_RUN_REMOTELY_PROPERTY, canRunRemotely);
                return message;
            }
        });

        try {
            message.acknowledge();
        } catch (JMSException e) {
            throw new IllegalStateException("Unable to acknowledge message.");
        }

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Followed by successful reply to the JMS Broker and acknowledgement of the message.");
    }

    /**
     * Builds the path to the directory in which the work should be performed and checks that it
     * exists and is writable.  (Also attempts to create this directory if it does not exist.)
     *
     * @param step being the step for which the directory should be returned
     * @return the path of the working directory.
     */
    private String getValidWorkingDirectory(Step step) {
        final String workingDirectory = directoryManager.replacePath(jobs.getBaseDirectoryTemporaryFiles());
        final String directory = new StringBuilder()
                .append(workingDirectory)
                .append(File.separatorChar)
                .append(step.getJob().getId())
                .toString();
        // Check (just the once) that the working directory exists.
        synchronized (validDirectoryLock) {
            if (!validatedDirectories.contains(directory)) {
                final File file = new File(directory);
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        throw new IllegalStateException("Unable to create the working directory " + directory);
                    }
                } else if (!file.isDirectory()) {
                    throw new IllegalStateException("The path " + directory + " exists, but is not a directory.");
                } else if (!file.canWrite()) {
                    throw new IllegalStateException("Unable to write to the directory " + directory);
                }
            }
        }
        return directory;
    }
}
