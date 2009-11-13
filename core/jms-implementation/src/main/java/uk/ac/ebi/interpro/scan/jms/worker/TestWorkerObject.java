package uk.ac.ebi.interpro.scan.jms.worker;

import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.jms.dumb_object.DumbObject;

import java.util.UUID;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Required;

import javax.jms.*;

/**
 * Created by IntelliJ IDEA.
 * User: maslen
 * Date: 09-Nov-2009
 * Time: 13:34:07
 */

/**
 * Test class - destination client that should run on one of the LSF nodes
 * and wait for messages to be sent.
 *
 * @author Phil Jones
 * @version $Id: TestWorker.java,v 1.4 2009/10/29 17:58:00 pjones Exp $
 * @since 1.0
 */
public class TestWorkerObject implements Worker {

    private SessionHandler sessionHandler;

    private String jobRequestQueueName;

    private String jobResponseQueueName;

    private volatile boolean running = true;

    private WorkerMonitor workerManager;

    private long receiveTimeout;

    private boolean singleUseOnly;

    private Double workDone;

    private final UUID uniqueWorkerIdentification = UUID.randomUUID();

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
        return (running)
                ? "Well, I'm doing something!"
                : "I've finished whatever it was I was doing.";
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
            MessageConsumer messageConsumer = sessionHandler.getMessageConsumer(jobRequestQueueName);
            MessageProducer messageProducer = sessionHandler.getMessageProducer(jobResponseQueueName);
            while (running){
                //TextMessage requestMessage = (TextMessage) messageConsumer.receive(receiveTimeout);
                ObjectMessage requestMessage = (ObjectMessage) messageConsumer.receive(receiveTimeout);
                if (requestMessage != null){   // receive has received a message before timing out.

                    // Wait a couple of seconds (doing something worthwhile of course!)
                    // And make a note of how much 'work' has been done.
                    final int steps = 40;
                    for (int i = 0; i < steps; i++){
                        workDone = (double)i / (double)steps;
                        try{
                            Thread.sleep(200);
                        }
                        catch (InterruptedException ie){
                            // I was at the Java Master's course, honest!
                            ie.printStackTrace();
                        }
                    }

                    ObjectMessage responseMessage;
                    String hostName = null;
                    try{
                        hostName = java.net.InetAddress.getLocalHost().getHostName();
                    }
                    catch (UnknownHostException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                    DumbObject messageObject = (DumbObject)requestMessage.getObject();
                    //String messageString = "I am " + hostName + " and I received message: " + requestMessage.getObject().getText();
                    System.out.println("Sending message back to broker: " + messageObject.getSize());
                    responseMessage = sessionHandler.createObjectMessage(messageObject);
                    messageProducer.send(responseMessage); // Reply to the response queue on the broker, to be picked up by the master application.
                    requestMessage.acknowledge();   // Explicitly acknowledge the message, when it has been successfully dealt with.
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
