package uk.ac.ebi.interpro.scan.jms.monitoring;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.transport.TransportListener;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;

/**
 * Represents a simple application, which connects to the monitoring master by the given broker URL (see ActiveMQConnectionFactory) and
 * sends a shutdown message to the master's shutdown queue.
 *
 * @author Maxim Scheremetjew
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class MasterControllerApplication implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(MasterControllerApplication.class.getName());

    private TransportListener jmsTransportListener;
    private ExceptionListener jmsExceptionListener;

    private Destination monitorQueue;

    private String brokerURL;

    @Required
    public void setMonitorQueue(Destination monitorQueue) {
        this.monitorQueue = monitorQueue;
    }

    @Required
    public void setJmsTransportListener(TransportListener jmsTransportListener) {
        this.jmsTransportListener = jmsTransportListener;
    }

    @Required
    public void setJmsExceptionListener(ExceptionListener jmsExceptionListener) {
        this.jmsExceptionListener = jmsExceptionListener;
    }

    @Override
    public void run() {
        LOGGER.info("Running InterProScan's master controller application...");
        if (this.brokerURL == null) {
            LOGGER.warn("No broker URL specified! Shutting down the application.");
            System.exit(0);
        }
        ConnectionFactory connectionFactory = createAndConfigActiveMQConnectionFactory(this.brokerURL);
        //Create and start activeMQ connection using JmsTemplate
        LOGGER.debug("Set remoteJMS template ");
        final JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);

        ActiveMQQueue activeMQMonitorQueue = (ActiveMQQueue) monitorQueue;
        try {
            LOGGER.info("Sending 150 messages to the " + activeMQMonitorQueue.getQueueName());
        } catch (JMSException e) {
            LOGGER.error("Cannot get the monitor queue name!", e);
        }
        for (int i = 0; i < 150; i++) {
            jmsTemplate.send(activeMQMonitorQueue, new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    TextMessage textMessage = new ActiveMQTextMessage();
                    textMessage.setText("Hallo! Jemand da");
                    return textMessage;
                }
            });
        }
        System.exit(0);
    }


    public void setBrokerURL(String brokerURL) {
        this.brokerURL = brokerURL;
    }

    private ConnectionFactory createAndConfigActiveMQConnectionFactory(final String brokerURL) {
        final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerURL);

        activeMQConnectionFactory.setOptimizeAcknowledge(true);
        activeMQConnectionFactory.setUseCompression(true);
        activeMQConnectionFactory.setAlwaysSessionAsync(false);
        activeMQConnectionFactory.getPrefetchPolicy().setQueuePrefetch(0);

        //set the RedeliveryPolicy
        RedeliveryPolicy queuePolicy = activeMQConnectionFactory.getRedeliveryPolicy();
        queuePolicy.setInitialRedeliveryDelay(0);
        queuePolicy.setRedeliveryDelay(1 * 1000);
        queuePolicy.setUseExponentialBackOff(false);
        queuePolicy.setMaximumRedeliveries(4);

        activeMQConnectionFactory.setRedeliveryPolicy(queuePolicy);

        activeMQConnectionFactory.setTransportListener(jmsTransportListener);

        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory(activeMQConnectionFactory);
        connectionFactory.setSessionCacheSize(100);
        connectionFactory.setExceptionListener(jmsExceptionListener);
        return connectionFactory;
    }
}