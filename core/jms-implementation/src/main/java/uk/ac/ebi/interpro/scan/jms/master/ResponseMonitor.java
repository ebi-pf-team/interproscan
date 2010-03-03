package uk.ac.ebi.interpro.scan.jms.master;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

import javax.jms.ConnectionFactory;
import java.util.Map;

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

    /**
     * Sets a reference to the Map of StepExecutions so that the
     * ResponseMonitor can update their state.
     *
     * Temporary only - will eventually update the StepExecutions
     * in the database.
     * @param stepExecutions
     */
    void setStepExecutionMap(Map<Long, StepExecution> stepExecutions);

    @Required
    public void setConnectionFactory(ConnectionFactory connectionFactory);
}
