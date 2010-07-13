package uk.ac.ebi.interpro.scan.jms.hornetq;

import org.apache.log4j.Logger;
import org.hornetq.jms.client.HornetQDestination;

import javax.jms.*;
import java.io.Serializable;
import java.lang.IllegalStateException;

/**
 * Simple bean holding information about queues and the JMS Broker.
 * <p/>
 * NOT thread safe and CANNOT BE MADE THREAD SAFE as javax.jms.Session objects are not thread safe.
 * <p/>
 * Use in a single Thread only.
 *
 * @author Phil Jones
 * @version $Id: HornetQSessionHandler.java,v 1.3 2009/10/21 18:44:40 pjones Exp $
 * @since 1.0
 */
public class HornetQSessionHandler {

    private static final Logger LOGGER = Logger.getLogger(HornetQSessionHandler.class.getName());

    private Connection connection;
    private Session session;

    public HornetQSessionHandler(ConnectionFactory cf) throws JMSException {
        connection = cf.createConnection();
        try {
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        }
        catch (JMSException e) {
            LOGGER.error("JMSException thrown when attempting to instantiate a HornetQSessionHandler.  Closing the Session and Connection.", e);
            try {
                if (session != null) {
                    session.close();
                }
            }
            finally {
                connection.close();
            }
            throw new IllegalStateException("Unable to get a Connection to the JMS Broker.", e);
        }
    }

    public void start() throws JMSException {
        connection.start();
    }

    public MessageConsumer getMessageConsumer(String destinationName) throws JMSException {
        return getMessageConsumer(destinationName, null);
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

    public void close() throws JMSException {
        if (connection != null) {
            connection.close();
        }
    }

    public TextMessage createTextMessage(String message) throws JMSException {
        return session.createTextMessage(message);
    }

    public ObjectMessage createObjectMessage(Serializable objectMessage) throws JMSException {
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

