package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;

/**
 * This implementation receives responses on the destinationResponseQueue
 * and then propagates them to the super worker or master.
 *
 * @author nuka, scheremetjew
 * @version $Id: ResponseMonitorImpl.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class RemoteJobQueueListener implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(RemoteJobQueueListener.class.getName());

    private JmsTemplate localJmsTemplate;

    private Destination jobRequestQueue;

    @Required
    public void setLocalJmsTemplate(JmsTemplate localJmsTemplate) {
        this.localJmsTemplate = localJmsTemplate;
    }

    @Required
    public void setJobRequestQueue(Destination jobRequestQueue) {
        this.jobRequestQueue = jobRequestQueue;
    }

    @Override
    public void onMessage(final Message message) {

        if (!(message instanceof ObjectMessage)) {
            LOGGER.error("RemoteQueue Message Listener: Received a message of an unknown type (non-ObjectMessage)");
            try {
                LOGGER.debug("Message type of the unknown type message="+message.getJMSType());
                LOGGER.debug("Message ID of the unknown type message="+message.getJMSMessageID());
            } catch (JMSException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
        localJmsTemplate.send(jobRequestQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return message;
            }
        });
        LOGGER.debug("Worker: received a message from the remote request queue and forwarded it onto the local jobRequestQueue");
    }
}
