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
import java.io.File;
import java.lang.IllegalStateException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    /**
     * Testing concept of having a maximum life on a serial worker, which then expires and
     * asks for a new one.  This might work well in the context of LSF for example.
     *
     * TODO - this should default to null and be injected.
     */
    private Long timeToLive = null;

    private Long idleExpiryTime = null;

    private String jobRequestQueueName;

    private String jobResponseQueueName;

    private volatile boolean running = true;

    private Double workDone;

    private volatile String workerStatus = "Running";

    private volatile StepExecution currentStepExecution;

    private final UUID uniqueWorkerIdentification = UUID.randomUUID();

    private Jobs jobs;

    private ConnectionFactory connectionFactory;

    private Long startTime;

    private String workerManagerTopicName;

    private String workerManagerResponseQueueName;

    private Long lastActivityTime;

    private List<String> validatedDirectories = new ArrayList<String>();

    @Required
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Optional time in milliseconds for the server to live for.
     * Note that this is the time until that last JMS job is accepted -
     * currently running jobs will run to completion.
     * @param millisecondsToLive time until that last JMS job is accepted.
     */
    @Override
    public void setMillisecondsToLive(Long millisecondsToLive) {
        this.timeToLive = millisecondsToLive;
    }

    public void setMillisecondsMaxIdleTime(Long maxIdleTime) {
        this.idleExpiryTime = maxIdleTime;
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

    public void setWorkerManagerTopicName(String workerManagerTopicName) {
        this.workerManagerTopicName = workerManagerTopicName;
    }

    public void setWorkerManagerResponseQueueName(String workerManagerResponseQueueName) {
        this.workerManagerResponseQueueName = workerManagerResponseQueueName;
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


    public void run(){
        lastActivityTime = startTime = new Date().getTime();
        SessionHandler sessionHandler = null;
        try{
            sessionHandler = new SessionHandler(connectionFactory);
            MessageConsumer messageConsumer = sessionHandler.getMessageConsumer(jobRequestQueueName);
            MessageProducer messageProducer = sessionHandler.getMessageProducer(jobResponseQueueName);
            StepExecutionListener stepExecutionListener = new StepExecutionListener(messageProducer, sessionHandler);
            messageConsumer.setMessageListener(stepExecutionListener);
            InterProScanMonitorListener monitorListener = null;
            if (workerManagerTopicName != null && workerManagerResponseQueueName != null){
                MessageConsumer monitorMessageConsumer = sessionHandler.getMessageConsumer(workerManagerTopicName);
                MessageProducer monitorMessageProducer = sessionHandler.getMessageProducer(workerManagerResponseQueueName);
                monitorListener = new InterProScanMonitorListener(sessionHandler, monitorMessageProducer);
                monitorMessageConsumer.setMessageListener(monitorListener);
            }

            sessionHandler.start();
            while (running || stepExecutionListener.isBusy() || (monitorListener != null && monitorListener.isBusy())){
                Thread.sleep(2000);

                final long now = new Date().getTime();
                // Cleanly stop the server if it has exceeded it's "time to live".
                if (timeToLive != null && now - startTime > timeToLive){
                    running = false;
                }

                // Cleanly stop the server if has been idle for longer than its 'idleExpiryTime'
                if (running && idleExpiryTime != null && now - lastActivityTime > idleExpiryTime){
                    running = false;
                }

                // Cleanly stop the server if there is a risk of a memory leak.
//                if (running && possibleMemoryLeakDetected()){
//                    running = false;
//                }
            }
        }
        catch (JMSException e) {
            LOGGER.error("JMSException thrown by worker.  Worker closing.", e);
        }
        catch (InterruptedException e) {
            LOGGER.info ("InterruptedException thrown in InterProScanWorker. Worker closing.", e);
        } finally{
            running = false;  //To ensure that the Monitor thread exits.
            try {
                if (sessionHandler != null){
                    sessionHandler.close();
                }
            } catch (JMSException e) {
                LOGGER.error("JMSException when attempting to close session / connection to JMS broker.", e);
            }
        }
    }

    class StepExecutionListener implements MessageListener{

        private final MessageProducer messageProducer;

        private final SessionHandler sessionHandler;


        private boolean busy = false;

        StepExecutionListener(final MessageProducer messageProducer, final SessionHandler sessionHandler) {
            this.messageProducer = messageProducer;
            this.sessionHandler = sessionHandler;
        }

        @Override
        public void onMessage(Message message) {
            busy = true;
            lastActivityTime = new Date().getTime();
            try{
                if (message != null){
                    message.acknowledge();     // Acknowledge receipt of the message.
                    LOGGER.debug("Message received from queue.  JMS Message ID: " + message.getJMSMessageID());

                    if (! (message instanceof ObjectMessage)){
                        LOGGER.error ("Received a message of an unknown type (non-ObjectMessage)");
                        return;
                    }
                    ObjectMessage stepExecutionMessage = (ObjectMessage) message;
                    final StepExecution stepExecution = (StepExecution) stepExecutionMessage.getObject();
                    currentStepExecution = stepExecution;
                    if (stepExecution == null){
                        LOGGER.error ("An ObjectMessage was received but had no contents.");
                        return;
                    }
                    LOGGER.debug("Message received of queue - attempting to executeInTransaction");

                    try{
                        executeInTransaction(stepExecution, messageProducer, sessionHandler);
                    } catch (Exception e) {
//todo: reinstate self termination for remote workers. Disabled to make process more robust for local workers.                        
            //            running = false;
                        LOGGER.error ("Execution thrown when attempting to executeInTransaction the StepExecution.  All database activity rolled back.", e);
                        // Something went wrong in the execution - try to send back failure
                        // message to the broker.  This in turn may fail if it is the JMS connection
                        // that failed during the execution.
                        stepExecution.fail();
                        ObjectMessage responseMessage = sessionHandler.createObjectMessage(stepExecution);
                        messageProducer.send(responseMessage);
                        LOGGER.debug ("Message returned to the broker to indicate that the StepExecution has failed: " + stepExecution.getId());
                    }
                }
            }
            catch (JMSException e) {
                LOGGER.error ("JMSException thrown in MessageListener.", e);
            }
            finally{
                busy = false;
            }
        }

        public boolean isBusy() {
            return busy;
        }
    }

    class InterProScanMonitorListener implements MessageListener{

        private SessionHandler sessionHandler;

        private MessageProducer messageProducer;

        private boolean busy = false;

        InterProScanMonitorListener(SessionHandler sessionHandler, MessageProducer messageProducer) {
            this.sessionHandler = sessionHandler;
            this.messageProducer = messageProducer;
        }

        @Override
        public void onMessage(Message message) {
            busy = true;
            try{
                if (message instanceof TextMessage){
                    TextMessage managementRequest = (TextMessage) message;
                    managementRequest.acknowledge();     // TODO - when should message receipt be acknowledged?
                    // Just echo back the managementRequest with the name of the worker host to uk.ac.ebi.interpro.scan.jms the multicast topic.
                    WorkerState workerState = new WorkerState(
                            System.currentTimeMillis() - startTime,
                            java.net.InetAddress.getLocalHost().getHostName(),
                            getWorkerUniqueIdentification(),
                            false
                    );
                    workerState.setJobId("Unique Job ID as passed from the broker in the JMS header. (TODO)");
                    workerState.setProportionComplete(getProportionOfWorkDone());
                    workerState.setWorkerStatus((isRunning()) ? "Running" : "Not Running");
                    StepExecution stepExecution = getCurrentStepExecution();
                    if (stepExecution == null){
                        workerState.setStepExecutionState(null);
                        workerState.setJobId("-");
                        workerState.setJobDescription("-");
                    }
                    else {
                        workerState.setStepExecutionState(stepExecution.getState());
                        workerState.setJobId(stepExecution.getId().toString());
                        workerState.setJobDescription(stepExecution.getStepInstance().getStep(jobs).getStepDescription());
                    }
                    ObjectMessage responseObject = sessionHandler.createObjectMessage(workerState);

                    // Find out if there is a 'requestee' header, which should be added to the response message
                    // so the management responses can be filtered out by the client that sent them.
                    if (managementRequest.propertyExists(WorkerMonitor.REQUESTEE_PROPERTY)){
                        responseObject.setStringProperty(WorkerMonitor.REQUESTEE_PROPERTY,
                                managementRequest.getStringProperty(WorkerMonitor.REQUESTEE_PROPERTY));
                    }

                    messageProducer.send(responseObject);
                }

            } catch (JMSException e) {
                LOGGER.error("JMSException thrown by InterProScanMonitorListener", e);
            } catch (UnknownHostException e) {
                LOGGER.error("UnknownHostException thrown by InterProScanMonitorListener", e);
            }
            finally {
                busy = false;
            }
        }

        public boolean isBusy() {
            return busy;
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
    private void executeInTransaction(StepExecution stepExecution,
                         MessageProducer messageProducer,
                         SessionHandler sessionHandler){
        stepExecution.setToRun();
        final StepInstance stepInstance = stepExecution.getStepInstance();
        final Step step = stepInstance.getStep(jobs);
        LOGGER.debug("Step ID: "+ step.getId());
        LOGGER.debug("Step instance: " + stepInstance);
        LOGGER.debug("Step execution id: " + stepExecution.getId());
        step.execute(stepInstance, getValidWorkingDirectory(step));
        stepExecution.completeSuccessfully();
        LOGGER.debug ("Successful run of Step.executeInTransaction() method for StepExecution ID: " + stepExecution.getId());

        try{
            ObjectMessage responseMessage = sessionHandler.createObjectMessage(stepExecution);
            messageProducer.send(responseMessage);
        } catch (JMSException e) {
            throw new IllegalStateException ("JMSException thrown when attempting to communicate successful completion of step " + stepExecution.getId() + " to the broker.", e);
        }

        LOGGER.debug("Followed by successful reply to the JMS Broker and acknowledgement of the message.");
    }

    /**
     * Builds the path to the directory in which the work should be performed and checks that it
     * exists and is writable.  (Also attempts to create this directory if it does not exist.)
     * @param step being the step for which the directory should be returned
     * @return the path of the working directory.
     */
    private String getValidWorkingDirectory(Step step){
        final String directory = new StringBuilder()
                        .append(jobs.getBaseDirectoryTemporaryFiles())
                        .append('/')
                        .append(step.getJob().getId())
                        .toString();
        // Check (just the once) that the working directory exists.
        if (! validatedDirectories.contains(directory)){
            final File file = new File (directory);
            if (! file.exists()){
                if (! file.mkdirs()){
                    throw new IllegalStateException("Unable to create the working directory " + directory);
                }
            }
            else if (! file.isDirectory()){
                throw new IllegalStateException ("The path " + directory + " exists, but is not a directory.");
            }
            else if (! file.canWrite()){
                throw new IllegalStateException ("Unable to write to the directory " + directory);
            }
            validatedDirectories.add(directory);
        }
        return directory;
    }
    /**
     * Returns true if after calling System.gc, there is no
     * evidence of a memory leak.
     *
     * Conservative - looks for used memory being no more
     * than 1/3 of Xmx.
     * @return true if all is OK.
     */
    private boolean possibleMemoryLeakDetected() {

        System.gc();

        final long Xmx = Runtime.getRuntime().maxMemory();
        final long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        final boolean leaky = Xmx / used < 3;
        if (leaky){
            LOGGER.error("The JVM is full - closing down?");
        }
        return leaky;
    }
}
