package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
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

    private static final Logger LOGGER = Logger.getLogger(ResponseMonitorImpl.class);

    /**
     * Handles the response from the worker thread.
     */
    private ResponseHandler handler;

    private String workerJobResponseQueueName;

    private volatile boolean running = true;

    private int monitorResponseTimeout;

    private String jmsBrokerHostName;

    private int jmsBrokerPort;


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
     * Host name of the JMS broker
     *
     * @param jmsBrokerHostName Host name of the JMS broker
     */
    @Override
    public void setJmsBrokerHostName(String jmsBrokerHostName) {
        this.jmsBrokerHostName = jmsBrokerHostName;
    }

    /**
     * Port number of the JMS broker.
     *
     * @param jmsBrokerPort Port number of the JMS broker.
     */
    @Override
    public void setJmsBrokerPort(int jmsBrokerPort) {
        this.jmsBrokerPort = jmsBrokerPort;
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
    public void setStepExecutionMap(Map<Long, StepExecution> stepExecutions) {
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
        SessionHandler sessionHandler = null;
        try{
            sessionHandler = new SessionHandler(jmsBrokerHostName, jmsBrokerPort);
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
            LOGGER.error ("JMSException thrown by ResponseMonitorImpl", e);
        }
        finally {
            try {
                if (sessionHandler != null){
                    sessionHandler.close();
                }
            } catch (JMSException e) {
                LOGGER.error ("JMSException thrown by ResponseMonitorImpl when attempting to close JMS Session & Connection.", e);
            }
        }
    }
}
