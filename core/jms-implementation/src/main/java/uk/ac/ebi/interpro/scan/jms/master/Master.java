package uk.ac.ebi.interpro.scan.jms.master;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.QueueJumper;

/**
 * Interface for the Master application.
 *
 * Expects there to be a queue on to which to place jobs (the task submission queue)
 * and a response monitor that is able to consume responses from workers and handle them
 * appropriately.
 *
 * The response monitor should be run in a separate thread.
 *
 * The injected SessionHandler object looks after connecting to the Broker.
 *
 * @author Phil Jones
 * @version $Id: Master.java,v 1.2 2009/10/16 12:05:10 pjones Exp $
 * @since 1.0
 */
public interface Master{

    /**
     * Sets the job submission queue name.  This is the queue that new
     * jobs are placed on to, prior to be pushed on to the requestQueue
     * from where they are taken by a worker node.
     * @param jobSubmissionQueueName
     */
    @Required
    void setJobSubmissionQueueName(String jobSubmissionQueueName);

    /**
     * Sets the name of the topic to which Worker management requests
     * should be sent, for multicast to all of the Worker clients.
     * @param managementRequestTopicName the name of the topic to which Worker management requests
     * should be sent, for multicast to all of the Worker clients.
     */
    @Required
    void setManagementRequestTopicName (String managementRequestTopicName);

    /**
     * Sets the ResponseMonitor which will handle any responses from
     * the Worker nodes.
     * @param responseMonitor which will handle any responses from
     * the Worker nodes.
     */
    @Required
    public void setResponseMonitor(ResponseMonitor responseMonitor);

    /**
     * Starts the Master application.
     */
    void start();

    @Required
    void setQueueJumper(QueueJumper queueJumper);
}
