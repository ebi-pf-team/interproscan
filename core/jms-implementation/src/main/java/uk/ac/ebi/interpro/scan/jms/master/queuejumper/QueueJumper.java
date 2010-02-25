package uk.ac.ebi.interpro.scan.jms.master.queuejumper;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.WorkerRunner;
import uk.ac.ebi.interpro.scan.jms.worker.InterProScanWorker;

import javax.jms.*;
import java.io.Serializable;

/**
 * Runs on the broker in a separate thread, moving any
 * jobs from the job submission queue to the request queue,
 * starting up a Destination on demand for each job.
 *
 * @author Phil Jones
 * @version $Id: QueueJumper.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class QueueJumper implements Runnable{

    private static final Logger LOGGER = Logger.getLogger(QueueJumper.class);


    private String jmsBrokerHostName;

    private int jmsBrokerPort;

    private int workersStarted = 0;

    private WorkerRunner parallelWorkerRunner;

    private WorkerRunner serialWorkerRunner;



    private String jobSubmissionQueueName;

    private String workerJobRequestQueueName;

    private volatile boolean running = true;

    private String jmsMessageSelector;

    @Required
    public void setParallelWorkerRunner(WorkerRunner workerRunner) {
        this.parallelWorkerRunner = workerRunner;
    }

    @Required
    public void setJobSubmissionQueueName(String jobSubmissionQueueName) {
        this.jobSubmissionQueueName = jobSubmissionQueueName;
    }

    @Required
    public void setWorkerJobRequestQueueName(String workerJobRequestQueueName) {
        this.workerJobRequestQueueName = workerJobRequestQueueName;
    }

    @Required
    public void setSerialWorkerRunner(uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.WorkerRunner serialWorkerRunner) {
        this.serialWorkerRunner = serialWorkerRunner;
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
     * Allows clean shutdown of the QueueJumper thread.
     */
    void shutdown(){
        running = false;
    }

     /**
     * Optional setter to allow a JMS filter to be passed in.
     * <p/>
     * See JMS Version 1.1 documentation for building selector clauses.
     *
     * @param messageSelector JMS message selector clause.
     */
    public void setJmsMessageSelector(String messageSelector) {
        this.jmsMessageSelector = messageSelector;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run() {
        LOGGER.debug("In QueueJumper thread start method.");
        SessionHandler sessionHandler = null;
        try{
            sessionHandler = new SessionHandler(jmsBrokerHostName, jmsBrokerPort);
            MessageConsumer messageConsumer = null;
            if (jmsMessageSelector == null){
                messageConsumer = sessionHandler.getMessageConsumer(jobSubmissionQueueName);
            }
            else {
                messageConsumer = sessionHandler.getMessageConsumer(jobSubmissionQueueName, jmsMessageSelector);
            }
            MessageProducer producer = sessionHandler.getMessageProducer(workerJobRequestQueueName);

            while (running){
                LOGGER.debug("Waiting for message on submission queue...");
                Message message = messageConsumer.receive();
                if (message != null){
                    if (message instanceof ObjectMessage){
                        final Serializable serializedObject = ((ObjectMessage) message).getObject();
                        if (serializedObject != null){
                            LOGGER.debug("Forwarding message and creating parallel worker");
                            producer.send(sessionHandler.createObjectMessage(serializedObject));
                            parallelWorkerRunner.startupNewWorker();
                        }
                    }
                    else if (message instanceof TextMessage){
                        final String text = ((TextMessage) message).getText();
                        if (InterProScanWorker.NEW_SERIAL_WORKER_REQUEST.equals(text)){
                            LOGGER.debug("Received request to create new SerialWorker.");
                            serialWorkerRunner.startupNewWorker();
                        }
                    }
                    message.acknowledge();
                }
            }
        }
        catch (JMSException e) {
            LOGGER.error ("JMSException thrown by QueueJumper when forwarding / handling messages.", e);
        }
        finally{
            try {
                if (sessionHandler != null){
                    sessionHandler.close();
                }
            } catch (JMSException e) {
                LOGGER.error ("JMSException thrown when attempting to close Session / Connection to the JMS Broker.", e);
            }
        }

    }
}
