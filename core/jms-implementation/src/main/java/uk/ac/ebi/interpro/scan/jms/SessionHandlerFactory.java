package uk.ac.ebi.interpro.scan.jms;

import org.hornetq.core.config.TransportConfiguration;
import org.hornetq.integration.transports.netty.NettyConnectorFactory;
import static org.hornetq.integration.transports.netty.TransportConstants.HOST_PROP_NAME;
import static org.hornetq.integration.transports.netty.TransportConstants.PORT_PROP_NAME;
import org.hornetq.jms.HornetQDestination;
import org.hornetq.jms.client.HornetQConnectionFactory;

import javax.jms.*;
import java.io.Serializable;
import java.lang.IllegalStateException;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple bean holding information about queues and the JMS Broker.
 * <p/>
 * NOT thread safe and CANNOT BE MADE THREAD SAFE as javax.jms.Session objects are not thread safe.
 * <p/>
 * Use in a single Thread only.
 *
 * @author John Maslen
 * @version $Id: SessionHandlerFactory .java,v 1.0 2009/10/21 18:44:40 maslen Exp $
 * @since 1.0
 */

public class SessionHandlerFactory {

    private Connection connection;
    private Session session;

//    public Connection getConnection() {
//        return connection;
//    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Session getSession(String brokerHostUrl, int brokerPort) throws JMSException {
        if (session != null) {
            throw new IllegalStateException("Possible programming error - attempt to initalise same session more than once.");
        }

        Map<String, Object> connectionParams = new HashMap<String, Object>();
        connectionParams.put(HOST_PROP_NAME, brokerHostUrl);
        connectionParams.put(PORT_PROP_NAME, brokerPort);

        TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName(),
                connectionParams);

        ConnectionFactory cf = new HornetQConnectionFactory(transportConfiguration);

        connection = cf.createConnection();

        connection.start();
        session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        return session;
    }

    public SessionHandlerFactory() {
    }

    public MessageConsumer getMessageConsumer(String destinationName, String messageSelector) throws JMSException {
        Destination destination = HornetQDestination.fromAddress(destinationName);
        if (messageSelector == null) {
            return session.createConsumer(destination);
        } else {
            return session.createConsumer(destination, messageSelector);
        }
    }

    public MessageProducer getMessageProducer(String destinationName) throws JMSException {
        Destination destination = HornetQDestination.fromAddress(destinationName);
        return session.createProducer(destination);
    }

    public TextMessage createTextMessage(String message) throws JMSException {
        return session.createTextMessage(message);
    }

    public ObjectMessage createObjectMessage(Serializable objectMessage) throws JMSException {
        return session.createObjectMessage(objectMessage);
    }

    public void close() throws JMSException {
        session.close();
        connection.close();
    }

//    public void closeConnection () throws JMSException {
//        connection.close();
//    }

}
