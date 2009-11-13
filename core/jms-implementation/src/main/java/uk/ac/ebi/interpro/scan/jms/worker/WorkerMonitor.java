package uk.ac.ebi.interpro.scan.jms.worker;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;

/**
 * The WorkerManager monitors the WorkerManagerTopic, used to monitor
 * the progress of Workers and to send them management messages
 * (such as shutdown, kill etc).
 *
 * @author Phil Jones
 * @version $Id: WorkerMonitor.java,v 1.2 2009/10/21 18:44:40 pjones Exp $
 * @since 1.0
 */
public interface WorkerMonitor extends Runnable{

    public static final String REQUESTEE_PROPERTY = "requestee";
    
     /**
     * Sets a SessionHandler for the manager thread.
     * This looks after connecting to the
     * Broker and allowing messages to be put on the queue / taken off the queue.
     * @param sessionHandler for the manager thread.
     */
    @Required
    void setSessionHandler(SessionHandler sessionHandler);

    /**
     * Sets the name of the worker manager topic.  This is a topic
     * that is used to poll for the status of all Worker clients.
     * @param workerManagerTopicName the name of the worker manager topic.
     */
    @Required
    void setWorkerManagerTopicName(String workerManagerTopicName);

    /**
     * Sets the name of the worker manager response queue.
     *
     * This queue is used to return responses to requests for the
     * status of the Worker client.
     * @param workerManagerResponseQueueName the name of the worker manager response queue.
     */
    @Required
    void setWorkerManagerResponseQueueName (String workerManagerResponseQueueName);

    /**
     * Called by the Worker that this WorkerManager is injected into
     * so that the WorkerManager can run management tasks on the Worker.
     * @param worker being the Worker being monitored.
     */
    void setWorker (Worker worker);
}
