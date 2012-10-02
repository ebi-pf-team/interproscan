package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.management.dao.StepExecutionDAO;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.util.Enumeration;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class MasterMessageSenderImpl implements MasterMessageSender {

    private static final Logger LOGGER = Logger.getLogger(MasterMessageSenderImpl.class.getName());

    public static final String HIGH_MEMORY_PROPERTY = "highmem";

    public static final String CAN_RUN_REMOTELY_PROPERTY = "remote";

    private JmsTemplate jmsTemplate;
    private JmsTemplate jmsTopicTemplate;

    private final Object jmsTemplateLock = new Object();

    private StepExecutionDAO stepExecutionDAO;

    private Destination workerJobRequestQueue;

    private Destination workerManagerTopic;

    private Destination highmemWorkerJobRequestQueue;

    public void setHighmemWorkerJobRequestQueue(Destination highmemWorkerJobRequestQueue) {
        this.highmemWorkerJobRequestQueue = highmemWorkerJobRequestQueue;
    }

    @Required
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void setJmsTopicTemplate(JmsTemplate jmsTopicTemplate) {
        this.jmsTopicTemplate = jmsTopicTemplate;
    }

    @Required
    public void setStepExecutionDAO(StepExecutionDAO stepExecutionDAO) {
        this.stepExecutionDAO = stepExecutionDAO;
    }

    @Required
    public void setWorkerJobRequestQueue(Destination workerJobRequestQueue) {
        this.workerJobRequestQueue = workerJobRequestQueue;
    }

    @Required
    public void setWorkerManagerTopic(Destination workerManagerTopic) {
        this.workerManagerTopic = workerManagerTopic;
    }

    public Destination getWorkerManagerTopic() {
        return workerManagerTopic;
    }

    /**
     * Sends shut down message to connected workers.
     */
    public void sendShutDownMessage(){
        LOGGER.debug("Sending a shutdown message to the workerManagerTopicQueue ");
        jmsTopicTemplate.send(workerManagerTopic, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage();
            }
        });
    }

    /**
     * Creates messages to be sent to Worker nodes.
     * Does all of this in a transaction, hence in this separate interface.
     *
     * @param stepInstance to send as a message
     * @throws javax.jms.JMSException in the event of a failure sending the message to the JMS Broker.
     */
    @Transactional
    public void sendMessage(StepInstance stepInstance, final boolean highMemory, final int priority, final boolean canRunRemotely) throws JMSException {
            LOGGER.debug("Attempting to send message to queue - high memory: "+highMemory+ "  priority: "+priority+" can run remotely: "+canRunRemotely);
        final StepExecution stepExecution = stepInstance.createStepExecution();
        stepExecutionDAO.insert(stepExecution);
        stepExecution.submit(stepExecutionDAO);

        if (!jmsTemplate.isExplicitQosEnabled()) {
            throw new IllegalStateException("It is not possible to set the priority of the JMS message, as the JMSTemplate does not have explicitQosEnabled.");
        }

        //change the destination to cater for high memory job request queue
        if(highMemory && canRunRemotely){
            if(highmemWorkerJobRequestQueue!=null){
                setWorkerJobRequestQueue(highmemWorkerJobRequestQueue);
                LOGGER.info("Sending a high memory job on the request queue ");
            }
            else {
                LOGGER.warn("High memory job request queue (destination) isn't set up properly!");
            }
        }
        synchronized (jmsTemplateLock) {
            jmsTemplate.setPriority(priority);
            jmsTemplate.send(workerJobRequestQueue, new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    final ObjectMessage message = session.createObjectMessage(stepExecution);
                    message.setBooleanProperty(HIGH_MEMORY_PROPERTY, highMemory);
                    message.setBooleanProperty(CAN_RUN_REMOTELY_PROPERTY, canRunRemotely);


                    // Some detailed logging of messages that can be run remotely.
                    if (LOGGER.isDebugEnabled() && canRunRemotely) {
                        LOGGER.debug("Adding to queue Message with ID:"+ message.getJMSMessageID()+ " type: "+message.getJMSType() +" " + (highMemory ? "highmem" : "normal memory") + " StepExecution with priority " + priority + " that can run remotely: " + stepExecution.toString());
                        final StringBuilder buf = new StringBuilder("Message properties:\n\n");
                        final Enumeration propNames = message.getPropertyNames();
                        while (propNames.hasMoreElements()) {
                            final String propertyName = (String) propNames.nextElement();
                            buf
                                    .append(propertyName)
                                    .append(':')
                                    .append(message.getStringProperty(propertyName))
                                    .append('\n');
                        }
                        LOGGER.debug(buf);
                    }
                    LOGGER.debug("Adding to queue Message with ID:"+ message.getJMSMessageID()+ " type: "+message.getJMSType() +" " + (highMemory ? "highmem" : "normal memory") + " StepExecution with priority " + priority + " that can run remotely: " + stepExecution.toString());


                    return message;
                }
            });
        }
    }
}
