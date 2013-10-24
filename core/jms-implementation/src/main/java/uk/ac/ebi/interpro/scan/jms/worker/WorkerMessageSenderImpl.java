package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.jms.master.ClusterState;
import uk.ac.ebi.interpro.scan.jms.stats.Utilities;

import javax.jms.*;
import java.io.Serializable;
import java.lang.IllegalStateException;

/**
 * Worker Message Sender handles all the jmsTemplate message sending for the worker
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class WorkerMessageSenderImpl implements Serializable, WorkerMessageSender {

    private static final Logger LOGGER = Logger.getLogger(WorkerMessageSenderImpl.class.getName());


    public static final String MESSAGE_PROPERTY = "messagetype";


    public static final String TOPIC_MESSAGE_PROPERTY = "messagetype";


    private JmsTemplate localJmsTemplate;
    private JmsTemplate remoteJmsTemplate;

    private JmsTemplate jmsTopicTemplate;

    private final Object localJmsTemplateLock = new Object();

    private final Object remoteJmsTemplateLock = new Object();

    private Destination workerJobRequestQueue;

    private Destination workerManagerTopic;

    private Destination highmemWorkerJobRequestQueue;

    @Required
    public void setLocalJmsTemplate(JmsTemplate localJmsTemplate) {
        this.localJmsTemplate = localJmsTemplate;
    }

    @Required
    public void setRemoteJmsTemplate(JmsTemplate remoteJmsTemplate) {
        this.remoteJmsTemplate = remoteJmsTemplate;
    }

    @Required
    public void setJmsTopicTemplate(JmsTemplate jmsTopicTemplate) {
        this.jmsTopicTemplate = jmsTopicTemplate;
    }

    @Required
    public void setWorkerJobRequestQueue(Destination workerJobRequestQueue) {
        this.workerJobRequestQueue = workerJobRequestQueue;
    }

    @Required
    public void setWorkerManagerTopic(Destination workerManagerTopic) {
        this.workerManagerTopic = workerManagerTopic;
    }

    @Required
    public void setHighmemWorkerJobRequestQueue(Destination highmemWorkerJobRequestQueue) {
        this.highmemWorkerJobRequestQueue = highmemWorkerJobRequestQueue;
    }

    /**
     * Sends shut down message to connected workers.
     * @param message
     */
    public void sendShutDownMessage(final Message message) throws JMSException{
        LOGGER.debug("Sending a shutdown message to the workerManagerTopicQueue ");
        jmsTopicTemplate.send(workerManagerTopic, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return message;
            }
        });
    }


    @Override
    public void sendTopicMessage(final ClusterState clusterState) {
        LOGGER.debug("Sending a ClusterState message to the workerManagerTopicQueue ");

        jmsTopicTemplate.send(workerManagerTopic, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                final ObjectMessage message = session.createObjectMessage(clusterState);
                message.setStringProperty(TOPIC_MESSAGE_PROPERTY, "clusterState");
                return message;
            }
        });
    }

    /**
     * Creates messages to be sent to Worker nodes.
     * Does all of this in a transaction,
     *
     * @param destination
     * @param message     to send
     * @param local
     * @throws javax.jms.JMSException in the event of a failure sending the message to the JMS Broker.
     */
    @Transactional
    public void sendMessage(Destination destination, final Message message, boolean local) throws JMSException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Attempting to send message to queue: " + destination);
        }

        if (local) {
            if (!localJmsTemplate.isExplicitQosEnabled()) {
                throw new IllegalStateException("It is not possible to set the priority of the JMS message, as the JMSTemplate does not have explicitQosEnabled.");
            }

            synchronized (localJmsTemplateLock) {
                localJmsTemplate.send(destination, new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        return message;
                    }
                });
            }
        } else {
            if (!remoteJmsTemplate.isExplicitQosEnabled()) {
                throw new IllegalStateException("It is not possible to set the priority of the JMS message, as the JMSTemplate does not have explicitQosEnabled.");
            }

            synchronized (remoteJmsTemplateLock) {
                remoteJmsTemplate.send(destination, new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        return message;
                    }
                });
            }
        }
        //return true
    }

    @Transactional
    public void sendMessage(Destination destination, final WorkerState workerState, boolean local) throws JMSException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Attempting to send message to queue: " + destination);
        }
        Utilities.verboseLog("Attempting to send message to queue: " + destination);

        if (local) {
            if (!localJmsTemplate.isExplicitQosEnabled()) {
                throw new IllegalStateException("It is not possible to set the priority of the JMS message, as the JMSTemplate does not have explicitQosEnabled.");
            }

            synchronized (localJmsTemplateLock) {
                localJmsTemplate.send(destination, new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        final ObjectMessage message = session.createObjectMessage(workerState);
                        message.setStringProperty(MESSAGE_PROPERTY, "workerState");

                        return  message;
                    }
                });
            }
        } else {
            if (!remoteJmsTemplate.isExplicitQosEnabled()) {
                throw new IllegalStateException("It is not possible to set the priority of the JMS message, as the JMSTemplate does not have explicitQosEnabled.");
            }

            synchronized (remoteJmsTemplateLock) {
                remoteJmsTemplate.send(destination, new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        final ObjectMessage message = session.createObjectMessage(workerState);
                        message.setStringProperty(MESSAGE_PROPERTY, "workerState");

                        return message;
                    }
                });
            }
        }
        //return true

    }
}
