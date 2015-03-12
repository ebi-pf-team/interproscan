package uk.ac.ebi.interpro.scan.jms.worker;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.jms.master.ClusterState;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Interface for the message sender used by the worker implementation.
 *
 * @author Gift Nuka
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface WorkerMessageSender {

    /**
     * Creates messages to be sent to the Local queue for this worker and other worker nodes.
     * Does all of this in a transaction,
     *
     * @param message to send as a message
     * @throws javax.jms.JMSException in the event of a failure sending the message to the JMS Broker.
     */
    @Transactional
    void sendMessage(Destination destination, final Message message, boolean local) throws JMSException;

    /**
     * Creates messages to be sent to the any queue for this worker and other worker nodes.
     * Does all of this in a transaction,
     *
     * @param workerState to send as a message
     * @throws javax.jms.JMSException in the event of a failure sending the message to the JMS Broker.
     */
    @Transactional
    void sendMessage(Destination destination, final WorkerState workerState, boolean local) throws JMSException;

    /**
     * send a shutdown message to the other worker nodes
     * @param message
     */
    void sendShutDownMessage(final Message message) throws JMSException;

    /**
     * set the remote JMS template
     *
     * @param remoteJmsTemplate
     */
    void setRemoteJmsTemplate(JmsTemplate remoteJmsTemplate);

    /**
     * set the local JMS template
     *
     * @param localJmsTemplate
     */
    void setLocalJmsTemplate(JmsTemplate localJmsTemplate);

    /**
     * set the JMS topic template
     *
     * @param jmsTopicTemplate
     */
    void setJmsTopicTemplate(JmsTemplate jmsTopicTemplate);



    void sendTopicMessage(final ClusterState clusterState);
}
