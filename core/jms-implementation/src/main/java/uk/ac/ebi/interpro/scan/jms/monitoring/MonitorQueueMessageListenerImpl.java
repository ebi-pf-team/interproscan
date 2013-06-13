package uk.ac.ebi.interpro.scan.jms.monitoring;

import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Listener implementation for the monitor queue (activeMQ identifier: monitorQueue). See file activemq-queue-config-context.xml.
 *
 * @author Maxim Scheremetjew
 * @since 1.0
 */
public class MonitorQueueMessageListenerImpl implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(MonitorQueueMessageListenerImpl.class.getName());


    @Override
    public void onMessage(Message message) {
        LOGGER.info("Master received a message on the monitor queue...");
        if (message instanceof TextMessage) {
            try {
                TextMessage textMessage = (TextMessage) message;
                System.out.println("messageText = " + textMessage.getText());
            } catch (JMSException e) {
                LOGGER.warn("Cannot get text message!", e);
            }
        }
    }
}