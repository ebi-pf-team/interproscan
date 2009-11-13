package uk.ac.ebi.interpro.scan.jms.master;

import uk.ac.ebi.interpro.scan.jms.SessionHandler;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Required;

/**
 * Pretending to be the InterProScan master application.
 *
 * @author Phil Jones
 * @version $Id: TestMaster.java,v 1.4 2009/10/28 15:04:00 pjones Exp $
 * @since 1.0
 */
public class TestMaster implements Master {

    private SessionHandler sessionHandler;

    private String jobSubmissionQueueName;

    private ResponseMonitor responseMonitor;

    private String managementRequestTopicName;

    /**
     * Sets the SessionHandler.  This looks after connecting to the
     * Broker and allowing messages to be put on the queue / taken off the queue.
     * @param sessionHandler  looks after connecting to the
     * Broker and allowing messages to be put on the queue / taken off the queue.
     */
    @Required
    public void setSessionHandler(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    /**
     * Sets the task submission queue name.  This is the queue that new
     * jobs are placed on to, prior to be pushed on to the requestQueue
     * from where they are taken by a worker node.
     * @param jobSubmissionQueueName
     */
    @Required
    public void setJobSubmissionQueueName(String jobSubmissionQueueName) {
        this.jobSubmissionQueueName = jobSubmissionQueueName;
    }

    /**
     * Sets the name of the topic to which Worker management requests
     * should be sent, for multicast to all of the Worker clients.
     *
     * @param managementRequestTopicName the name of the topic to which Worker management requests
     *                                   should be sent, for multicast to all of the Worker clients.
     */
    @Override
    public void setManagementRequestTopicName(String managementRequestTopicName) {
        this.managementRequestTopicName = managementRequestTopicName;
    }

    /**
     * Sets the ResponseMonitor which will handle any responses from
     * the Worker nodes.
     * @param responseMonitor which will handle any responses from
     * the Worker nodes.
     */
    @Required
    public void setResponseMonitor(ResponseMonitor responseMonitor){
        this.responseMonitor = responseMonitor;
    }

    /**
     * Run the Master Application.
     */
    public void start(){
        try {
            // Start the response monitor thread
            Thread responseMonitorThread = new Thread(responseMonitor);
            responseMonitorThread.start();

            // Initialise the sessionHandler for the master thread
            sessionHandler.init();

            // And do some stuff.
            for (int i = 0; i < 50; i++){
                sendMessage(jobSubmissionQueueName, "Message number " + i);  // Send a message every second or so.
                Thread.sleep(100);
            }

        } catch (JMSException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            if (sessionHandler != null){
                try {
                    sessionHandler.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Just creates simple text messages to be sent to Worker nodes.
     * @param messageString being the String to send as a message
     * @throws JMSException in the event of a failure sending the message to the JMS Broker.
     */
    private void sendMessage(String destination, String messageString) throws JMSException {
        MessageProducer producer = sessionHandler.getMessageProducer(destination);
        TextMessage message = sessionHandler.createTextMessage(messageString);

        producer.send(message);
        System.out.println("Sent message: " + message.getText());
    }
}
