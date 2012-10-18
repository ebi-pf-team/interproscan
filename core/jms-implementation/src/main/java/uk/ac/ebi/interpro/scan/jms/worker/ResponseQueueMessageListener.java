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

    private WorkerMessageSender workerMessageSender;

    @Required
    public void setRemoteJmsTemplate(JmsTemplate remoteJmsTemplate) {
        this.remoteJmsTemplate = remoteJmsTemplate;
    }

    @Required
    public void setJobResponseQueue(Destination jobResponseQueue) {
        this.jobResponseQueue = jobResponseQueue;
    }


    public void setWorkerMessageSender(WorkerMessageSender workerMessageSender) {
        this.workerMessageSender = workerMessageSender;
    }

    public WorkerMessageSender getWorkerMessageSender() {
        return workerMessageSender;
    }

    @Override
    public void onMessage(final Message message) {

        //forward message to the remote responseQueue  should be a transaction!!
//        try {
//            workerMessageSender.sendMessage(jobResponseQueue,message,false);
//        } catch (JMSException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            LOGGER.warn("Worker: received message but failed to send it to the master/manager jobResponseQueue");
//        }
        remoteJmsTemplate.send(jobResponseQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return message;
            }
        });
        LOGGER.debug("Worker: received and sent a message on the jobResponseQueue");
    }
}
