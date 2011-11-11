package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.MessageCreator;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

import javax.jms.*;
import java.util.UUID;

/**
 * ActiveMQ worker.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class AmqInterProScanWorker implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(AmqInterProScanWorker.class.getName());

    private JmsTemplateWrapper jmsTemplateWrapper;

    private Destination jobResponseQueue;

    private StepExecutionTransaction stepExecutor;

    private final UUID uniqueWorkerIdentification = UUID.randomUUID();

    /**
     * The distributed worker controller is in charge of calling 'stop' on the
     * Spring MonitorListenerContainer when the conditions are correct.
     *
     * @see uk.ac.ebi.interpro.scan.jms.activemq.DistributedWorkerController
     */
    private DistributedWorkerController controller;

    AmqInterProScanWorker() {
    }

    @Required
    public void setJmsTemplateWrapper(JmsTemplateWrapper jmsTemplateWrapper) {
        this.jmsTemplateWrapper = jmsTemplateWrapper;
    }

    public void setController(DistributedWorkerController controller) {
        this.controller = controller;
    }

    @Required
    public void setStepExecutor(StepExecutionTransaction stepExecutor) {
        this.stepExecutor = stepExecutor;
    }

    /**
     * Sets the JMS queue to which the Worker returns the results of a job.
     *
     * @param jobResponseQueue the name of the JMS queue to which the Worker returns the results of a job.
     */
    @Required
    public void setJobResponseQueue(Destination jobResponseQueue) {
        this.jobResponseQueue = jobResponseQueue;
    }

    @Override
    public void onMessage(Message message) {
        final String messageId;

        try {
            messageId = message.getJMSMessageID();
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown in MessageListener when attempting to get the JMS Message ID.", e);
            return;
        }

        try {
            if (controller != null) {
                controller.jobStarted(messageId);
            }

            LOGGER.debug("Message received from queue.  JMS Message ID: " + message.getJMSMessageID());

            if (!(message instanceof ObjectMessage)) {
                LOGGER.error("Received a message of an unknown type (non-ObjectMessage)");
                return;
            }
            final ObjectMessage stepExecutionMessage = (ObjectMessage) message;
            final StepExecution stepExecution = (StepExecution) stepExecutionMessage.getObject();
            if (stepExecution == null) {
                LOGGER.error("An ObjectMessage was received but had no contents.");
                return;
            }
            LOGGER.debug("Message received of queue - attempting to executeInTransaction");

            try {
                stepExecutor.executeInTransaction(stepExecution, message);
            } catch (Exception e) {
//todo: reinstate self termination for remote workers. Disabled to make process more robust for local workers.
                //            running = false;
                LOGGER.error("Execution thrown when attempting to executeInTransaction the StepExecution.  All database activity rolled back.", e);
                // Something went wrong in the execution - try to send back failure
                // message to the broker.  This in turn may fail if it is the JMS connection
                // that failed during the execution.
                stepExecution.fail(e);

                jmsTemplateWrapper.getTemplate().send(jobResponseQueue, new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        return session.createObjectMessage(stepExecution);
                    }
                });
                message.acknowledge(); // Acknowledge message following failure.

                LOGGER.debug("Message returned to the broker to indicate that the StepExecution has failed: " + stepExecution.getId());
            }

        } catch (JMSException e) {
            LOGGER.error("JMSException thrown in MessageListener.", e);
        } finally {
            if (controller != null) {
                controller.jobFinished(messageId);
            }
        }
    }

    public UUID getUniqueWorkerIdentification() {
        return uniqueWorkerIdentification;
    }
}
