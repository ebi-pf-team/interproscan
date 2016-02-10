package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import uk.ac.ebi.interpro.scan.util.Utilities;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

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

    private WorkerImpl controller;

    @Required
    public void setRemoteJmsTemplate(JmsTemplate remoteJmsTemplate) {
        this.remoteJmsTemplate = remoteJmsTemplate;
        if(workerMessageSender != null){
            workerMessageSender.setRemoteJmsTemplate(remoteJmsTemplate);
        }
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

    public void setController(WorkerImpl controller) {
        this.controller = controller;
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
        try{
            if (controller != null) {
                controller.jobResponseReceived();
                LOGGER.debug("Worker: received ... send message to jobResponseQueue");
            }
            //what if the send action fails?
            remoteJmsTemplate.send(jobResponseQueue, new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    return message;
                }
            });

            Long stepId  = 0l;
            String stepName = "dummy";
            final ObjectMessage stepExecutionMessage = (ObjectMessage) message;
            final StepExecution stepExecution = (StepExecution) stepExecutionMessage.getObject();
            stepName =  stepExecution.getStepInstance().getStepId();
            stepId = stepExecution.getStepInstance().getId();

            LOGGER.debug("Worker: received and sent a message on the jobResponseQueue: "
                    + stepId + " - " + stepName);
            Utilities.verboseLog("Worker: received and sent a message on the jobResponseQueue: "
                    + stepId + " - " + stepName);
            //send a message that the response has been sent to the master
            if (controller != null) {
                controller.jobResponseProcessed();
                controller.removeJobFromWorkerState(message);

            }
        } catch (JMSException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            if (controller != null) {
                controller.handleFailure(ResponseQueueMessageListener.class.getName());
            }
        } catch (Exception e){
            e.printStackTrace();
            if (controller != null) {
                controller.handleFailure(ResponseQueueMessageListener.class.getName());
            }
        }
    }
}
