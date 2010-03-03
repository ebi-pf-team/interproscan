package uk.ac.ebi.interpro.scan.jms.master.queuejumper;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
public class
        QueueJumper implements Runnable{

    private static final Logger LOGGER = Logger.getLogger(QueueJumper.class);



    private int workersStarted = 0;

    private WorkerRunner parallelWorkerRunner;

    private String jobSubmissionQueueName;

    private String workerJobRequestQueueName;

    private volatile boolean running = true;

    private ConnectionFactory connectionFactory;

    @Required
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

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

    /**
     * Allows clean shutdown of the QueueJumper thread.
     */
    void shutdown(){
        running = false;
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
            sessionHandler = new SessionHandler(connectionFactory);
            MessageConsumer messageConsumer = sessionHandler.getMessageConsumer(jobSubmissionQueueName);
            MessageProducer producer = sessionHandler.getMessageProducer(workerJobRequestQueueName);
            QueueJumperListener listener = new QueueJumperListener(sessionHandler, producer);
            messageConsumer.setMessageListener(listener);
            sessionHandler.start();
            while (running){
                Thread.sleep(10000);
            }
        }
        catch (JMSException e) {
            LOGGER.error ("JMSException thrown by QueueJumper when forwarding / handling messages.", e);
        }
        catch (InterruptedException e) {
            LOGGER.error ("InterruptedException thrown by QueueJumper class.", e);
        } finally{
            try {
                if (sessionHandler != null){
                    sessionHandler.close();
                }
            } catch (JMSException e) {
                LOGGER.error ("JMSException thrown when attempting to close Session / Connection to the JMS Broker.", e);
            }
        }

    }

    class QueueJumperListener implements MessageListener{

        private SessionHandler sessionHandler;

        private MessageProducer producer;

        QueueJumperListener(SessionHandler sessionHandler, MessageProducer producer) {
            this.sessionHandler = sessionHandler;
            this.producer = producer;
        }

        @Override
        public void onMessage(Message message) {
            try{
                if (message instanceof ObjectMessage){
                    final Serializable serializedObject = ((ObjectMessage) message).getObject();
                    if (serializedObject != null){
                        LOGGER.debug("Forwarding message and creating parallel worker");
                        ObjectMessage objectMessage = sessionHandler.createObjectMessage(serializedObject);
                        producer.send(objectMessage);
                        parallelWorkerRunner.startupNewWorker();
                    }
                }
                message.acknowledge();
            } catch (JMSException e) {
                LOGGER.error("Something went wrong in the QueueJumperListener.", e);
            }
        }
    }
}
