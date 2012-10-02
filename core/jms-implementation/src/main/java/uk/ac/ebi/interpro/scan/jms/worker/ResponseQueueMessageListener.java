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
public class ResponseQueueMessageListener implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(ResponseQueueMessageListener.class.getName());

    private JmsTemplate remoteJmsTemplate;

    private Destination jobResponseQueue;

    @Required
    public void setRemoteJmsTemplate(JmsTemplate remoteJmsTemplate) {
        this.remoteJmsTemplate = remoteJmsTemplate;
    }

    @Required
    public void setJobResponseQueue(Destination jobResponseQueue) {
        this.jobResponseQueue = jobResponseQueue;
    }

    @Override
    public void onMessage(final Message message) {
       remoteJmsTemplate.send(jobResponseQueue, new MessageCreator() {
                        public Message createMessage(Session session) throws JMSException {
                return message;
            }
        });
        LOGGER.debug("Worker: received and sent a message on the jobResponseQueue");
    }
}
