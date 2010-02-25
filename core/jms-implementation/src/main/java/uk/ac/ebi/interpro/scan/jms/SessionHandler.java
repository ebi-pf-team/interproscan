package uk.ac.ebi.interpro.scan.jms;

import org.apache.log4j.Logger;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.integration.transports.netty.NettyConnectorFactory;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.hornetq.jms.client.HornetQDestination;
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

    private static final Logger LOGGER = Logger.getLogger(SessionHandler.class);

    private Connection connection;
    private Session session;

    public SessionHandler(String brokerHostUrl, int brokerPort) throws JMSException {
        Map<String, Object> connectionParams = new HashMap<String, Object>();
        connectionParams.put(HOST_PROP_NAME, brokerHostUrl);
        connectionParams.put(PORT_PROP_NAME, brokerPort);

        TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName(),
                connectionParams);

        ConnectionFactory cf = new HornetQConnectionFactory(transportConfiguration);

        connection = cf.createConnection();
        try{
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            connection.start();
        }
        catch (JMSException e){
            LOGGER.error ("JMSException thrown when attempting to instantiate a SessionHandler.  Closing the Session and Connection.", e);
            try{
                if (session != null){
                    session.close();
                }
            }
            finally {
                connection.close();
            }
            throw new IllegalStateException ("Unable to get a Connection to the JMS Broker.", e);
        }
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
        try{
            if (session != null){
                session.close();
            }
        }
        finally {
            if (connection != null){
                connection.close();
            }
        }
    }

    public TextMessage createTextMessage (String message) throws JMSException {
        return session.createTextMessage(message);
    }

    public ObjectMessage createObjectMessage (Serializable objectMessage) throws JMSException {
        ObjectMessage om = session.createObjectMessage();
        om.setObject(objectMessage);
        return om;
    }

    /**
     * This is belt-and braces check that close() is being called.  Note that
     * this should NOT be relied upon and close() should be called in a finally block
     * of the code using this object.  (This is present just in case the try/finally block
     * never gets run! - This object is being injected
     *
     * @throws Throwable the <code>Exception</code> raised by this method
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}

