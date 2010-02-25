package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

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

    private static final Logger LOGGER = Logger.getLogger(InterProScanWorker.class);

    private String jmsBrokerHostName;

    private int jmsBrokerPort;

    private String jobRequestQueueName;

    private String jobResponseQueueName;

    private volatile boolean running = true;

    private volatile WorkerMonitor workerManager;

    private long receiveTimeout;

    private boolean stopWhenIdle;

    private Double workDone;

    private volatile String workerStatus = "Running";

    private volatile StepExecution currentStepExecution;

    private final UUID uniqueWorkerIdentification = UUID.randomUUID();

    private String jmsMessageSelector;

    private boolean isSerialWorker;

    private Jobs jobs;
    public static final String NEW_SERIAL_WORKER_REQUEST = "new-serial-worker-request";

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
     * @param stopWhenIdle if true, the Worker accepts and processes one job, then closes down.  If false, the
     * worker continues to take jobs from the queue until it is explicity shut down.
     */
    @Required
    public void setStopWhenIdle(boolean stopWhenIdle) {
        this.stopWhenIdle = stopWhenIdle;
    }

    @Required
    public void setSerialWorker(boolean serialWorker) {
        isSerialWorker = serialWorker;
    }

    /**
     * Sets the name of the JMS queue from which the Worker takes a job to do.
     * @param jobRequestQueueName the name of the JMS queue from which the Worker takes a job to do.
     */
    @Required
    public void setJobRequestQueueName(String jobRequestQueueName) {
        this.jobRequestQueueName = jobRequestQueueName;
    }

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    /**
     * Sets the name of the JMS queue to which the Worker returns the results of a job.
     * @param jobResponseQueueName  the name of the JMS queue to which the Worker returns the results of a job.
     */
    @Required
    public void setJobResponseQueueName(String jobResponseQueueName) {
        this.jobResponseQueueName = jobResponseQueueName;
    }

    @Required
    public void setJmsBrokerHostName(String jmsBrokerHostName) {
        this.jmsBrokerHostName = jmsBrokerHostName;
    }

    @Required
    public void setJmsBrokerPort(int jmsBrokerPort) {
        this.jmsBrokerPort = jmsBrokerPort;
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
    public boolean isStopWhenIdle() {
        return stopWhenIdle;
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
        SessionHandler sessionHandler = null;
        try{
            if (workerManager != null){
                // Worker manager has been injected.  Start in a high priority Thread.
                // This should be fine as it will configure itself and then
                // block, while it waits for any messages to be broadcast.
                workerManager.setWorker(this);
                Thread managerThread = new Thread(workerManager);
                managerThread.setDaemon(true);    // Make sure the manager thread end thread ends when the main thread ends.
                managerThread.start();
            }

            sessionHandler = new SessionHandler(jmsBrokerHostName, jmsBrokerPort);
            MessageConsumer messageConsumer;
            if (jmsMessageSelector == null){
                messageConsumer = sessionHandler.getMessageConsumer(jobRequestQueueName);
            }
            else {
                messageConsumer = sessionHandler.getMessageConsumer(jobRequestQueueName, jmsMessageSelector);
            }

            MessageProducer messageProducer = sessionHandler.getMessageProducer(jobResponseQueueName);
            while (running && noMemoryLeak()){
                LOGGER.debug("Waiting for a message...");
                Message message = messageConsumer.receive(receiveTimeout);
                if (message != null){
                    LOGGER.debug("Message received from queue.  JMS Message ID: " + message.getJMSMessageID());
                    message.acknowledge();     // Acknowledge receipt of the message before doing anything else.
                    // Acknowledgement does NOT indicate a successful outcome in this system - just successful
                    // receipt of the message.
                    if (message instanceof ObjectMessage){
                        ObjectMessage stepExecutionMessage = (ObjectMessage) message;
                        final StepExecution stepExecution = (StepExecution) stepExecutionMessage.getObject();
                        currentStepExecution = stepExecution;
                        if (stepExecution != null){
                            LOGGER.debug("Message received of queue - attempting to execute");

                            try{
                                execute(stepExecution, messageProducer, sessionHandler);
                            } catch (Exception e) {
                                LOGGER.error ("Execution thrown when attempting to execute the StepExecution.  All database activity rolled back.");
                                // Something went wrong in the execution - try to send back failure
                                // message to the broker.  This in turn may fail if it is the JMS connection
                                // that failed during the execution.
                                stepExecution.fail();
                                ObjectMessage responseMessage = sessionHandler.createObjectMessage(stepExecution);
                                messageProducer.send(responseMessage);
                                LOGGER.debug ("Message returned to the broker to indicate that the StepExecution has failed: " + stepExecution.getId());
                            }
                        }
                        else {
                            LOGGER.debug("Message received, but has not contents.");
                        }
                    }
                }
                else {
                    LOGGER.debug("No message received.");
                    if (stopWhenIdle){
                        break;
                    }
                }
                LOGGER.debug("...waiting for another message.");
            }
            if (isSerialWorker){
                // Signal to the Master that a new
                // SerialWorker is required.
                LOGGER.debug("Going to close down now... requesting new Serial Worker is started.");
                TextMessage newMasterMessage = sessionHandler.createTextMessage(NEW_SERIAL_WORKER_REQUEST);
                messageProducer.send(newMasterMessage);
            }
            LOGGER.debug("...exiting");
        }
        catch (JMSException e) {
            LOGGER.error("JMSException thrown by worker.  Exiting.", e);
        }
        finally{
            try {
                if (sessionHandler != null){
                    sessionHandler.close();
                }
            } catch (JMSException e) {
                LOGGER.error("JMSException when attempting to close session / connection to JMS broker.", e);
            }
        }
    }

    /**
     * Executing the StepInstance and responding to the JMS Broker
     * if the execution is successful.
     * @param stepExecution          The StepExecution to run.
     * @param messageProducer        For building the reply to the Broker
     * @param sessionHandler         to send the reply to the broker
     */
    @Transactional
    private void execute(StepExecution stepExecution,
                         MessageProducer messageProducer,
                         SessionHandler sessionHandler) throws Exception{
        stepExecution.setToRun();
        final StepInstance stepInstance = stepExecution.getStepInstance();
        final Step step = stepInstance.getStep(jobs);
        LOGGER.debug("Step ID: "+ step.getId());
        LOGGER.debug("Step instance: " + stepInstance);
        LOGGER.debug("Step execution id: " + stepExecution.getId());
        step.execute(stepInstance);
        stepExecution.completeSuccessfully();
        LOGGER.debug ("Successful run of Step.execute() method for StepExecution ID: " + stepExecution.getId());

        ObjectMessage responseMessage = sessionHandler.createObjectMessage(stepExecution);
        messageProducer.send(responseMessage);
        LOGGER.debug("Followed by successful reply to the JMS Broker and acknowledgement of the message.");
    }

    /**
     * Returns true if after calling System.gc, there is no
     * evidence of a memory leak.
     *
     * Conservative - looks for used memory being no more
     * than 1/3 of Xmx.
     * @return true if all is OK.
     */
    private boolean noMemoryLeak() {

        System.gc();System.gc();System.gc();System.gc();
        System.gc();System.gc();System.gc();System.gc();
        System.gc();System.gc();System.gc();System.gc();
        System.gc();System.gc();System.gc();System.gc();

        final long Xmx = Runtime.getRuntime().maxMemory();
        final long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        final boolean ok = Xmx / used > 3;
        if (LOGGER.isDebugEnabled()){
            LOGGER.debug ("Finished StepExecution - Checking for memory leak.");
            LOGGER.debug ("Used memory: " + used / (1024 * 1024 * 1024) + " GB");
            LOGGER.debug ("Xmx: " + Xmx / (1024 * 1024 * 1024) + " GB");
            LOGGER.debug ((ok) ? "OK" : "Leaking.");
        }
        return ok;
    }
}
