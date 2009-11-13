package uk.ac.ebi.interpro.scan.jms.broker;


import org.springframework.beans.factory.annotation.Required;

import javax.jms.*;

import uk.ac.ebi.interpro.scan.jms.broker.platforms.WorkerRunner;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;

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

    private int workersStarted = 0;

    private WorkerRunner workerRunner;

    private SessionHandler sessionHandler;

    private String jobSubmissionQueueName;

    private String workerJobRequestQueueName;

    private volatile boolean running = true;

    @Required
    public void setWorkerRunner(WorkerRunner workerRunner) {
        this.workerRunner = workerRunner;
    }

    @Required
    public void setConnectionConfiguration(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
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
        System.out.println("In QueueJumper thread start method.");

        try{
            sessionHandler.init();
            MessageConsumer messageConsumer = sessionHandler.getMessageConsumer(jobSubmissionQueueName);
            System.out.println("Got message consumer....");
            MessageProducer producer = sessionHandler.getMessageProducer(workerJobRequestQueueName);
            System.out.println("Got message producer...");

            while (running){
                System.out.println("Waiting for message on submission queue...");
                Message message = messageConsumer.receive();
                System.out.println("Message recieved on submission queue - routing to worker request queue.");
                if (((workersStarted++) % 5) == 0){
                    // Start up an extra worker every now and then...
                    workerRunner.startupNewWorker();
                }
                workerRunner.startupNewWorker();
                producer.send(message);
                message.acknowledge();
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
