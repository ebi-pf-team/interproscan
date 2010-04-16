package uk.ac.ebi.interpro.scan.jms.worker;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

import javax.jms.ConnectionFactory;
import java.util.UUID;

/**
 * Interface for a Worker.
 *
 * @author Phil Jones
 * @version $Id: Worker.java,v 1.3 2009/10/16 12:19:19 pjones Exp $
 * @since 1.0
 */
public interface Worker extends Runnable {

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
     * Inject the JMS ConnectionFactory
     * @param connectionFactory the JMS ConnectionFactory
     */
    @Required
    public void setConnectionFactory(ConnectionFactory connectionFactory);

    @Required
    void setWorkerManagerTopicName(String workerManagerTopicName);

    @Required
    void setWorkerManagerResponseQueueName(String workerManagerResponseQueueName);

    void setMillisecondsToLive(Long millisecondsToLive);
}
