package uk.ac.ebi.interpro.scan.jms.worker;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

import java.util.UUID;

/**
 * Interface for a Worker.
 *
 * @author Phil Jones
 * @version $Id: Worker.java,v 1.3 2009/10/16 12:19:19 pjones Exp $
 * @since 1.0
 */
public interface Worker {

    /**
     * Sets the timeout on the Worker.  This should be set reasonably low (a few seconds perhaps)
     * Otherwise it will be difficult to close down the worker gracefully.
     * @param timeout being the argument to MessageConsumer.receive(long timeout) method.
     */
    @Required
    void setReceiveTimeoutMillis (long timeout);

    /**
     * Sets the name of the JMS queue from which the Worker takes a job to do.
      * @param jobRequestQueueName the name of the JMS queue from which the Worker takes a job to do.
      */
    @Required
    void setJobRequestQueueName(String jobRequestQueueName);

    /**
     * Sets the name of the JMS queue to which the Worker returns the results of a job.
      * @param jobResponseQueueName  the name of the JMS queue to which the Worker returns the results of a job.
      */
    @Required
    void setJobResponseQueueName(String jobResponseQueueName);

    /**
     * Sets the SessionHandler.  This looks after connecting to the
      * Broker and allowing messages to be put on the queue / taken off the queue.
      * @param sessionHandler  looks after connecting to the
      * Broker and allowing messages to be put on the queue / taken off the queue.
      */
    @Required
    void setMainSessionHandler(SessionHandler sessionHandler);

    /**
     * OPTIONALLY a workerManager runnable may be injected.
     * If this is injected, it should be run in a high priority thread
     * (will block most of the time, so should not interfere with the
     * main activity).
     *
     * Implementation note: WorkerManager.setWorker(this) MUST be called
     * before starting the Thread that the WorkerManager is running in.
     *
     * The worker manager than subscribes to the workerManagerResponseQueue
     * @param workerManager
     */
    void setWorkerManager (WorkerMonitor workerManager);


    /**
     * @param singleUseOnly if true, the Worker accepts and processes one job, then closes down.  If false, the
     * worker continues to take jobs from the queue until it is explicity shut down.
     */
    @Required
    void setStopWhenIdle(boolean singleUseOnly);

    /**
     * Start the Worker running.
     */
    void start();

    /**
     * Tells the Worker to shut down gracefully (finish whatever it's doing, then shut down.)
     * If the Worker was started with oneJobOnly set to true, this method call has no effect.
     */
    void shutdown();


    /**
     * Indicates if the Worker is still running.
     * @return true if the Worker is still running.
     */
    boolean isRunning();

    /**
     * Returns a message describing the state of the worker.
     * The message can have any contents, or can be null.
     * @return a message describing the state of the worker
     */
    String getStatusMessage();

    /**
     * Returns true if this Worker is configured to only service a single
     * job message and then close down.  Returns false if the worker is
     * configured to run indefinitely (until it is explicitly shut down).
     * @return true if this Worker is configured to only service a single
     * job message.
     */
    boolean isStopWhenIdle();

    /**
     * This method should return:
     * null, if the amount of work done is unknown
     * a value between 0 and 1 if the proportion of work done is known.
     * @return null, if the amount of work done is unknown
     * a value between 0 and 1 if the proportion of work done is known.
     */
    Double getProportionOfWorkDone();

    /**
     * Returns a java.util.UUID object which should be instantiated
     * when the Worker is contructed as a final field.
     * @return a unique identifier for the Worker (using java.util.UUID)
     */
    UUID getWorkerUniqueIdentification();

    /**
     * Returns the current StepExecution.
     * Note that this may return null, if the Worker currently has
     * no StepExecution to run.
     * @return the current StepExecution.
     */
    StepExecution getCurrentStepExecution();

    /**
     * Optional JMS message selector.
     * @param jmsMessageSelector optional JMS message selector.
     */
    void setJmsMessageSelector(String jmsMessageSelector);
}
