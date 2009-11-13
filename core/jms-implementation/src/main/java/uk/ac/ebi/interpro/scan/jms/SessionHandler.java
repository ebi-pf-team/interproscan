package uk.ac.ebi.interpro.scan.jms;

import org.springframework.beans.factory.annotation.Required;

import javax.jms.*;
import java.io.Serializable;

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

    private String brokerHostUrl;
    private int brokerPort;
    private SessionHandlerFactory factory = new SessionHandlerFactory();

    @Required
    public void setBrokerHostUrl(String brokerHostUrl) {
        this.brokerHostUrl = brokerHostUrl;
    }

    @Required
    public void setBrokerPort(int brokerPort) {
        this.brokerPort = brokerPort;
    }

    public void init() throws JMSException {
        factory.getSession(brokerHostUrl, brokerPort);
    }

    public MessageConsumer getMessageConsumer (String destinationName) throws JMSException {
        return getMessageConsumer(destinationName, null);
    }

    public MessageConsumer getMessageConsumer (String destinationName, String messageSelector) throws JMSException {
        return factory.getMessageConsumer (destinationName, messageSelector);
    }

    public MessageProducer getMessageProducer (String destinationName) throws JMSException {
        return factory.getMessageProducer(destinationName);
    }

    public SessionHandler() {
    }

    public void close() throws JMSException {
        factory.close();
    }

    public TextMessage createTextMessage (String message) throws JMSException {
        return factory.createTextMessage(message);
    }

    public ObjectMessage createObjectMessage (Serializable objectMessage) throws JMSException {
        return factory.createObjectMessage(objectMessage);
    }
}
