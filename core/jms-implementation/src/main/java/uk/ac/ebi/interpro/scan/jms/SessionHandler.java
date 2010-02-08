package uk.ac.ebi.interpro.scan.jms;

import org.hornetq.core.config.TransportConfiguration;
import org.hornetq.integration.transports.netty.NettyConnectorFactory;
import org.hornetq.jms.HornetQDestination;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.jms.*;
import java.io.Serializable;
import java.lang.IllegalStateException;
import java.util.HashMap;
import java.util.Map;

import static org.hornetq.integration.transports.netty.TransportConstants.HOST_PROP_NAME;
import static org.hornetq.integration.transports.netty.TransportConstants.PORT_PROP_NAME;

/**
 * Simple bean holding information about queues and the JMS Broker.
 *
 * NOT thread safe and CANNOT BE MADE THREAD SAFE as javax.jms.Session objects are not thread safe.
 *
 * Use in a single Thread only.
 *
 * @author Phil Jones
 * @version $Id: SessionHandler.java,v 1.3 2009/10/21 18:44:40 pjones Exp $
 * @since 1.0
 */
public class SessionHandler {

    private Connection connection;
    private Session session;
    private String brokerHostUrl;
    private int brokerPort;

    @Required
    public void setBrokerHostUrl(String brokerHostUrl) {
        this.brokerHostUrl = brokerHostUrl;
    }

    @Required
    public void setBrokerPort(int brokerPort) {
        this.brokerPort = brokerPort;
    }


    public void init() throws JMSException {
        if (connection != null || session != null){
            throw new IllegalStateException("Possible programming error - SessionHandler.init() has been called more than once.");
        }
        Map<String, Object> connectionParams = new HashMap<String, Object>();
        connectionParams.put(HOST_PROP_NAME, brokerHostUrl);
        connectionParams.put(PORT_PROP_NAME, brokerPort);

        TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName(),
                connectionParams);

        ConnectionFactory cf = new HornetQConnectionFactory(transportConfiguration);

        connection = cf.createConnection();
        session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        connection.start();
    }


    public MessageConsumer getMessageConsumer (String destinationName) throws JMSException {
        return getMessageConsumer(destinationName, null);
    }

    public MessageConsumer getMessageConsumer (String destinationName, String messageSelector) throws JMSException {
        Destination destination = HornetQDestination.fromAddress (destinationName);
        if (messageSelector == null){
            return session.createConsumer(destination);
        }
        else {
            return session.createConsumer(destination, messageSelector);
        }
    }

    public MessageProducer getMessageProducer (String destinationName) throws JMSException {
        Destination destination = HornetQDestination.fromAddress (destinationName);
        return session.createProducer(destination);
    }

    public void close() throws JMSException {
        session.close();
        connection.close();
    }

    public TextMessage createTextMessage (String message) throws JMSException {
        return session.createTextMessage(message);
    }

    public ObjectMessage createObjectMessage (Serializable objectMessage) throws JMSException {
        ObjectMessage om = session.createObjectMessage();
        om.setObject(objectMessage);
        return om;
    }
}

