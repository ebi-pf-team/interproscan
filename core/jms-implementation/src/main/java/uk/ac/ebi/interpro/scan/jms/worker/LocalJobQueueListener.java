package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import uk.ac.ebi.interpro.scan.jms.activemq.StepExecutionTransaction;
import uk.ac.ebi.interpro.scan.jms.stats.Utilities;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This implementation receives responses on the destinationResponseQueue
 * and then propagates them to the super worker or master.
 *
 * @author nuka, scheremetjew
 * @version $Id: ResponseMonitorImpl.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class LocalJobQueueListener implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(LocalJobQueueListener.class.getName());
    private JmsTemplate localJmsTemplate;

    private Destination jobResponseQueue;

    private StepExecutionTransaction stepExecutor;

    /**
     * The distributed worker controller is in charge of calling 'stop' on the
     * Spring MonitorListenerContainer when the conditions are correct.
     *
     * @see uk.ac.ebi.interpro.scan.jms.worker.WorkerImpl
     */
    private WorkerImpl controller;

    private AtomicInteger jobCount = new AtomicInteger(0);

    public AtomicInteger getJobCount() {
        return jobCount;
    }

    public void setJobCount(int jobCount) {
        this.jobCount = new AtomicInteger (jobCount);
    }

    public void setLocalJmsTemplate(JmsTemplate localJmsTemplate) {
        this.localJmsTemplate = localJmsTemplate;
    }

    public void setController(WorkerImpl controller) {
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
    public void onMessage(final Message message) {
        jobCount.incrementAndGet();
        int localCount = jobCount.get();
        if(localCount == 1){
            String timeNow = Utilities.getTimeNow();
            System.out.println(timeNow + " first transaction ... ");
        }

        LOGGER.debug("Processing JobCount #: " + localCount);

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
            LOGGER.debug("Message received from queue.  JMS Message ID: " + message.getJMSMessageID() + " cmd:  " + message.toString());
            LOGGER.info("Message received from queue.  JMS Message ID: " + message.getJMSMessageID());

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


            // TODO - Need to add a dead-letter queue, so if this worker never gets as far as
            // acknoledging the message, the Master will know to re-run the StepInstance.
            try {
                final long now = System.currentTimeMillis();
                stepExecutor.executeInTransaction(stepExecution, message);
                final long executionTime =   System.currentTimeMillis() - now;
                LOGGER.debug("Execution Time (ms) for JobCount #: " + localCount + " stepId: " + stepExecution.getStepInstance().getStepId() + " time: " + executionTime);
            } catch (Exception e) {
//todo: reinstate self termination for remote workers. Disabled to make process more robust for local workers.
                //            running = false;
                LOGGER.error("Execution thrown when attempting to executeInTransaction the StepExecution.  All database activity rolled back.", e);
                // Something went wrong in the execution - try to send back failure
                // message to the broker.  This in turn may fail if it is the JMS connection
                // that failed during the execution.
                stepExecution.fail(e);

                localJmsTemplate.send(jobResponseQueue, new MessageCreator() {
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
                controller.workerState.addLocallyCompletedJob(message);
            }
        }

        LOGGER.debug("Finished Processing JobCount #: " + localCount);
    }
}
