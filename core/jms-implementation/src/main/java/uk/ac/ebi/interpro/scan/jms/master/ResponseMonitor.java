package uk.ac.ebi.interpro.scan.jms.master;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;

/**
 * This interface defines the contract for a ResponseMonitor, which runs in
 * it's own thread, picking up responses from Worker nodes and handling them
 * appropriately.
 *
 * @author Phil Jones
 * @version $Id: ResponseMonitor.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public interface ResponseMonitor extends Runnable{
    /**
     * Sets the ResponseHandler.  This is the implementation
     * that knows how to deal with responses on the destinationResponseQueue.
     * @param handler the implementation
     * that knows how to deal with responses on the destinationResponseQueue.
     */
    @Required
    void setHandler(ResponseHandler handler);

    /**
     * Sets the SessionHandler.  This looks after connecting to the
     * Broker and allowing messages to be put on the queue / taken off the queue.
     * @param sessionHandler  looks after connecting to the
     * Broker and allowing messages to be put on the queue / taken off the queue.
     */
    @Required
    void setSessionHandler(SessionHandler sessionHandler);

    /**
     * Sets the name of the destinationResponseQueue.
     * @param workerJobResponseQueueName the name of the destinationResponseQueue.
     */
    @Required
    void setWorkerJobResponseQueueName(String workerJobResponseQueueName);

    /**
     * Method to gracefully shut down the ResponseMonitor. (i.e. it will finish
     * handling whatever response it is currently working on first.)
     */
    void shutDownMonitor();

}
