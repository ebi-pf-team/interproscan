package uk.ac.ebi.interpro.scan.jms.monitoring;

import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.jms.*;

/**
 *
 * listen to the monitor queue
 *
 * if a message is received on the listener send it to the master.
 *
 * At the moment this listener expects WorkerState messaged only
 *    .
 */
public class WorkerMonitorQueueListener implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(MonitorQueueMessageListenerImpl.class.getName());

    private JmsTemplate remoteJmsTemplate;

    private Destination systemMonitorQueue;

    public void setRemoteJmsTemplate(JmsTemplate remoteJmsTemplate) {
        this.remoteJmsTemplate = remoteJmsTemplate;
    }

    public void setSystemMonitorQueue(Destination systemMonitorQueue) {
        this.systemMonitorQueue = systemMonitorQueue;
    }

    @Override
    public void onMessage(final Message message) {
        LOGGER.info("Worker received a message on the monitor queue...");
        if (message instanceof TextMessage) {
            try {
                TextMessage textMessage = (TextMessage) message;
                System.out.println("messageText = " + textMessage.getText());
            } catch (JMSException e) {
                LOGGER.warn("Cannot get text message!", e);
            }
        }else if (message instanceof ObjectMessage){
            try {
                remoteJmsTemplate.send(systemMonitorQueue, new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        return message;
                    }
                });
                Utilities.verboseLog(1100, "worker state received and forwarded: \n"
                        + message.getJMSMessageID());
            } catch (JMSException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }

    }
}