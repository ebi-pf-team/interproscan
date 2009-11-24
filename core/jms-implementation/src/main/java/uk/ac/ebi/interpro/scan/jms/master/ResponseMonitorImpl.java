package uk.ac.ebi.interpro.scan.jms.master;

import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import org.springframework.beans.factory.annotation.Required;

import javax.jms.MessageConsumer;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Map;

/**
 * This implementation recieves responses on the destinationResponseQueue
 * and then delegates to the injected Handler to respond to them.
 *
 * @author Phil Jones
 * @version $Id: ResponseMonitorImpl.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class ResponseMonitorImpl implements ResponseMonitor{

    /**
     * Handles the response from the worker thread.
     */
    private ResponseHandler handler;

    private SessionHandler sessionHandler;

    private String workerJobResponseQueueName;

    private volatile boolean running = true;

    private int monitorResponseTimeout;


    /**
     * Sets the ResponseHandler.  This is the implementation
     * that knows how to deal with responses on the destinationResponseQueue.
     * @param handler the implementation
     * that knows how to deal with responses on the destinationResponseQueue.
     */
    @Required
    public void setHandler(ResponseHandler handler) {
        this.handler = handler;
    }

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
     * Sets the name of the destinationResponseQueue.
     * @param workerJobResponseQueueName the name of the destinationResponseQueue.
     */
    @Required
    public void setWorkerJobResponseQueueName(String workerJobResponseQueueName) {
        this.workerJobResponseQueueName = workerJobResponseQueueName;
    }

    @Required
    public void setMonitorResponseTimeout(int monitorResponseTimeout) {
        if (monitorResponseTimeout <= 0){
            throw new IllegalArgumentException ("The timeout for the ResponseMonitor must be greater than 0.");
        }
        this.monitorResponseTimeout = monitorResponseTimeout;
    }

    /**
     * Method to gracefully shut down the ResponseMonitor. (i.e. it will finish
     * handling whatever response it is currently working on first.)
     */
    @Override
    public void shutDownMonitor() {
        // Sets the setToRun flag to false.  The monitor will finish handling
        // the current message prior to ending.
        running = false;
    }

    /**
     * Sets a reference to the Map of StepExecutions so that the
     * ResponseMonitor can update their state.
     * <p/>
     * Temporary only - will eventually update the StepExecutions
     * in the database.
     *
     * @param stepExecutions
     */
    @Override
    public void setStepExecutionMap(Map<String, StepExecution> stepExecutions) {
        this.handler.setStepExecutionMap(stepExecutions);
    }

    /**
     * This runnable monitors the destinationResponseQueue and then handles
     * responses from Workers using the injected ResponseHandler
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try{
            sessionHandler.init();
            MessageConsumer messageConsumer = sessionHandler.getMessageConsumer(workerJobResponseQueueName);
            while (running){
                // Consume messages off the response queue and handle them.
                Message message = messageConsumer.receive(monitorResponseTimeout);
                if (message != null) {
                    handler.handleResponse(message);
                    message.acknowledge();
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
        finally {
            try {
                sessionHandler.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
