package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.jms.master.ClusterState;
import uk.ac.ebi.interpro.scan.jms.monitoring.*;
import uk.ac.ebi.interpro.scan.management.dao.StepExecutionDAO;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.util.Enumeration;

/**
 * Sends messages to the work queue on behalf of the master.
 *
 * @author Phil Jones
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class MasterMessageSenderImpl implements MasterMessageSender {

    private static final Logger LOGGER = Logger.getLogger(MasterMessageSenderImpl.class.getName());

    public static final String HIGH_MEMORY_PROPERTY = "highmem";

    public static final String CAN_RUN_REMOTELY_PROPERTY = "remote";

    public static final String TOPIC_MESSAGE_PROPERTY = "messagetype";


    private JmsTemplate jmsTemplate;
    private JmsTemplate jmsTopicTemplate;

    private static final Object JMS_TEMPLATE_LOCK = new Object();

    private StepExecutionDAO stepExecutionDAO;

    private Destination workerJobRequestQueue;

    private Destination normalWorkerJobRequestQueue;

    private Destination workerManagerTopic;

    private Destination highmemWorkerJobRequestQueue;

    public void setHighmemWorkerJobRequestQueue(Destination highmemWorkerJobRequestQueue) {
        this.highmemWorkerJobRequestQueue = highmemWorkerJobRequestQueue;
    }

    @Required
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void setJmsTopicTemplate(JmsTemplate jmsTopicTemplate) {
        this.jmsTopicTemplate = jmsTopicTemplate;
    }

    @Required
    public void setStepExecutionDAO(StepExecutionDAO stepExecutionDAO) {
        this.stepExecutionDAO = stepExecutionDAO;
    }

    @Required
    public void setWorkerJobRequestQueue(Destination workerJobRequestQueue) {
        this.workerJobRequestQueue = workerJobRequestQueue;
    }

    @Required
    public void setNormalWorkerJobRequestQueue(Destination normalWorkerJobRequestQueue) {
        this.normalWorkerJobRequestQueue = normalWorkerJobRequestQueue;
    }

    @Required
    public void setWorkerManagerTopic(Destination workerManagerTopic) {
        this.workerManagerTopic = workerManagerTopic;
    }

    public Destination getWorkerManagerTopic() {
        return workerManagerTopic;
    }

    /**
     * Sends shut down message to connected workers.
     */
    public void sendShutDownMessage() {
        LOGGER.debug("Sending a shutdown message onto the workerManagerTopicQueue ");

        jmsTopicTemplate.send(workerManagerTopic, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage(new Shutdown());
            }
        });
    }

    /**
     * Creates messages to be sent to Worker nodes.
     * Does all of this in a transaction, hence in this separate interface.
     *
     * @param stepInstance to send as a message
     * @throws javax.jms.JMSException in the event of a failure sending the message to the JMS Broker.
     */
    @Transactional
    public void sendMessage(StepInstance stepInstance, final boolean highMemory, final int priority, final boolean canRunRemotely) throws JMSException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Attempting to send message to queue - high memory: " + highMemory + "  priority: " + priority + " can run remotely: " + canRunRemotely);
        }
        final StepExecution stepExecution = stepInstance.createStepExecution();
        stepExecutionDAO.insert(stepExecution);
        stepExecution.submit(stepExecutionDAO);

        if (!jmsTemplate.isExplicitQosEnabled()) {
            throw new IllegalStateException("It is not possible to set the priority of the JMS message, as the JMSTemplate does not have explicitQosEnabled.");
        }

        //change the destination to cater for high memory job request queue, remember to set it back to the normal worker queue thereafter
        if (highMemory && canRunRemotely) {
            if (highmemWorkerJobRequestQueue != null) {
                setWorkerJobRequestQueue(highmemWorkerJobRequestQueue);
                LOGGER.info("Sending a high memory job on the request queue ");
            } else {
                LOGGER.warn("High memory job request queue (destination) isn't set up properly!");
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Using queue: " + ((Queue) workerJobRequestQueue).getQueueName());
        }
        synchronized (JMS_TEMPLATE_LOCK) {
            jmsTemplate.setPriority(priority);
            jmsTemplate.send(workerJobRequestQueue, new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    final ObjectMessage message = session.createObjectMessage(stepExecution);
                    message.setBooleanProperty(HIGH_MEMORY_PROPERTY, highMemory);
                    message.setBooleanProperty(CAN_RUN_REMOTELY_PROPERTY, canRunRemotely);


                    // Some detailed logging of messages that can be run remotely.
                    if (LOGGER.isDebugEnabled() && canRunRemotely) {
                        LOGGER.debug("Adding to queue Message with ID:" + message.getJMSMessageID() + " type: " + message.getJMSType() + " " + (highMemory ? "highmem" : "normal memory") + " StepExecution with priority " + priority + " that can run remotely: " + stepExecution.toString());
                        final StringBuilder buf = new StringBuilder("Message properties:\n\n");
                        final Enumeration propNames = message.getPropertyNames();
                        while (propNames.hasMoreElements()) {
                            final String propertyName = (String) propNames.nextElement();
                            buf
                                    .append(propertyName)
                                    .append(':')
                                    .append(message.getStringProperty(propertyName))
                                    .append('\n');
                        }
                        LOGGER.debug(buf);
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Adding to queue Message with ID:"+ message.getJMSMessageID()+
                            " highmem: "+message.getBooleanProperty(HIGH_MEMORY_PROPERTY) +" " + (highMemory ? "highmemWorker" : "normalMemoryWorker") +
                            " StepExecution with priority " + priority +
                            " that can run remotely: " + stepExecution.toString()  +
                            " on the queuue: "+ ((Queue)workerJobRequestQueue).getQueueName());
                    }

                    return message;
                }
            });
        }
        //set the queue back to normal job request queue
        if(highMemory && canRunRemotely){
            setWorkerJobRequestQueue(normalWorkerJobRequestQueue);
        }
    }


    @Override
    public void sendTopicMessage(final ClusterState clusterState) {
        LOGGER.debug("Sending a ClusterState message onto the workerManagerTopicQueue ");

        jmsTopicTemplate.send(workerManagerTopic, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                final ObjectMessage message = session.createObjectMessage(clusterState);
                message.setStringProperty(TOPIC_MESSAGE_PROPERTY, "clusterState");
                return message;
            }
        });
    }
}
