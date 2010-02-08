package uk.ac.ebi.interpro.scan.jms.worker;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.persistence.DAOManager;

import javax.jms.*;
import java.util.UUID;

/**
 * Test class - destination client that should run on one of the LSF nodes
 * and wait for messages to be sent.
 *
 * @author Phil Jones
 * @version $Id: TestWorker.java,v 1.4 2009/10/29 17:58:00 pjones Exp $
 * @since 1.0
 */
public class InterProScanWorker implements Worker {

    private SessionHandler sessionHandler;

    private String jobRequestQueueName;

    private String jobResponseQueueName;

    private volatile boolean running = true;

    private WorkerMonitor workerManager;

    private long receiveTimeout;

    private boolean singleUseOnly;

    private Double workDone;

    private String workerStatus = "Running";

    private volatile StepExecution currentStepExecution;

    private final UUID uniqueWorkerIdentification = UUID.randomUUID();

    private String jmsMessageSelector;

    private DAOManager daoManager;

    /**
     * Sets the timeout on the Worker.  This should be set reasonably low (a few seconds perhaps)
     * Otherwise it will be difficult to close down the worker gracefully.
     *
     * @param timeout being the argument to MessageConsumer.receive(long timeout) method.
     */
    @Override
    public void setReceiveTimeoutMillis(long timeout) {
        this.receiveTimeout = timeout;
    }

    /**
     * @param singleUseOnly if true, the Worker accepts and processes one job, then closes down.  If false, the
     * worker continues to take jobs from the queue until it is explicity shut down.
     */
    @Required
    public void setSingleUseOnly(boolean singleUseOnly) {
        this.singleUseOnly = singleUseOnly;
    }

    /**
     * Sets the name of the JMS queue from which the Worker takes a job to do.
     * @param jobRequestQueueName the name of the JMS queue from which the Worker takes a job to do.
     */
    @Required
    public void setJobRequestQueueName(String jobRequestQueueName) {
        this.jobRequestQueueName = jobRequestQueueName;
    }

    /**
     * Sets the name of the JMS queue to which the Worker returns the results of a job.
     * @param jobResponseQueueName  the name of the JMS queue to which the Worker returns the results of a job.
     */
    @Required
    public void setJobResponseQueueName(String jobResponseQueueName) {
        this.jobResponseQueueName = jobResponseQueueName;
    }

    /**
     * Sets the SessionHandler.  This looks after connecting to the
     * Broker and allowing messages to be put on the queue / taken off the queue.
     * @param sessionHandler  looks after connecting to the
     * Broker and allowing messages to be put on the queue / taken off the queue.
     */
    @Required
    public void setMainSessionHandler(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }

    /**
     * Optional DAOManager - it is likely this will only
     * be injected into the Serial worker.
     * @param daoManager optional handle on the DAO objects
     * for database access.
     */
    public void setDaoManager(DAOManager daoManager) {
        this.daoManager = daoManager;
    }

    /**
     * OPTIONALLY a workerManager runnable may be injected.
     * If this is injected, it should be run in a high priority thread
     * (will block most of the time, so should not interfere with the
     * main activity).
     * <p/>
     * The worker manager than subscribes to the workerManagerResponseQueue
     *
     * @param workerManager
     */
    @Override
    public void setWorkerManager(WorkerMonitor workerManager) {
        this.workerManager = workerManager;
    }

    /**
     * Optional JMS message selector.
     * @param jmsMessageSelector optional JMS message selector.
     */
    public void setJmsMessageSelector(String jmsMessageSelector) {
        this.jmsMessageSelector = jmsMessageSelector;
    }

    /**
     * Tells the Worker to shut down gracefully (finish whatever it's doing, then shut down.)
     */
    public void shutdown() {
        running = false;
    }

    /**
     * Indicates if the Worker is still running.
     *
     * @return true if the Worker is still running.
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns a message describing the state of the worker.
     * The message can have any contents, or can be null.
     *
     * @return a message describing the state of the worker
     */
    @Override
    public String getStatusMessage() {
        return "Worker: " + workerStatus + "";
    }

    /**
     * Returns true if this Worker is configured to only service a single
     * job message and then close down.  Returns false if the worker is
     * configured to run indefinitely (until it is explicitly shut down).
     *
     * @return true if this Worker is configured to only service a single
     *         job message.
     */
    @Override
    public boolean isSingleUseOnly() {
        return singleUseOnly;
    }

    /**
     * This method should return:
     * null, if the amount of work done is unknown
     * a value between 0 and 1 if the proportion of work done is known.
     *
     * @return null, if the amount of work done is unknown
     *         a value between 0 and 1 if the proportion of work done is known.
     */
    @Override
    public Double getProportionOfWorkDone() {
        return workDone;
    }

    /**
     * Returns a java.util.UUID object which should be instantiated
     * when the Worker is contructed as a final field.
     *
     * @return a unique identifier for the Worker (using java.util.UUID)
     */
    @Override
    public UUID getWorkerUniqueIdentification() {
        return uniqueWorkerIdentification;
    }


    public StepExecution getCurrentStepExecution() {
        return currentStepExecution;
    }

    public void start(){
        try{
            if (workerManager != null){
                // Worker manager has been injected.  Start in a high priority Thread.
                // This should be fine as it will configure itself and then
                // block, while it waits for any messages to be broadcast.
                workerManager.setWorker(this);
                Thread managerThread = new Thread(workerManager);
                managerThread.setDaemon(true);
                managerThread.setPriority(Thread.MAX_PRIORITY);
                managerThread.start();
            }

            sessionHandler.init();
            MessageConsumer messageConsumer = null;
            if (jmsMessageSelector == null){
                messageConsumer = sessionHandler.getMessageConsumer(jobRequestQueueName);
            }
            else {
                messageConsumer = sessionHandler.getMessageConsumer(jobRequestQueueName, jmsMessageSelector);
            }

            MessageProducer messageProducer = sessionHandler.getMessageProducer(jobResponseQueueName);
            while (running){
                System.out.println("About to invoke receive()");
                Message message = messageConsumer.receive(receiveTimeout);
                if (message != null){
                    System.out.println("Message was not null");
                    if (message instanceof ObjectMessage){
                        System.out.println("Message was a non-null ObjectMessage");
                        ObjectMessage stepExecutionMessage = (ObjectMessage) message;
                        System.out.println("stepExecutionMessage.toString() = " + stepExecutionMessage);
                        final StepExecution stepExecution = (StepExecution) stepExecutionMessage.getObject();
                        currentStepExecution = stepExecution;
                        System.out.println("Got currentStepExecution, which is...");
                        if (stepExecution != null){
                            System.out.println("Not null!");
                            Step step = stepExecution.getStepInstance().getStep();
                            step.execute(daoManager, stepExecution);
                            ObjectMessage responseMessage = sessionHandler.createObjectMessage(stepExecution);
                            messageProducer.send(responseMessage);
                            stepExecutionMessage.acknowledge();
                        }
                    }
                }
                if (singleUseOnly){
                    break;
                }
            }
        }
        catch (JMSException e) {
            e.printStackTrace();
        }
        finally{
            try {
                sessionHandler.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
