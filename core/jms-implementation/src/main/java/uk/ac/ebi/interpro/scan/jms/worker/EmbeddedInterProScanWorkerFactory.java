package uk.ac.ebi.interpro.scan.jms.worker;

import org.hornetq.jms.client.HornetQConnectionFactory;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.Jobs;

/**
 * For Embedded Workers, allows any number of workers to be created
 * as required.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class EmbeddedInterProScanWorkerFactory implements EmbeddedWorkerFactory {

    private HornetQConnectionFactory connectionFactory;
    private String jobRequestQueueName;
    private String jobResponseQueueName;
    private Jobs jobs;
    private String workerManagerTopicName;
    private String workerManagerResponseQueueName;

    @Required
    public void setConnectionFactory(HornetQConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Required
    public void setJobRequestQueueName(String jobRequestQueueName) {
        this.jobRequestQueueName = jobRequestQueueName;
    }

    @Required
    public void setJobResponseQueueName(String jobResponseQueueName) {
        this.jobResponseQueueName = jobResponseQueueName;
    }

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    public void setWorkerManagerTopicName(String workerManagerTopicName) {
        this.workerManagerTopicName = workerManagerTopicName;
    }

    public void setWorkerManagerResponseQueueName(String workerManagerResponseQueueName) {
        this.workerManagerResponseQueueName = workerManagerResponseQueueName;
    }

    public Worker getInstance(){
        InterProScanWorker worker = new InterProScanWorker();
        worker.setConnectionFactory(connectionFactory);
        worker.setJobRequestQueueName(jobRequestQueueName);
        worker.setJobResponseQueueName(jobResponseQueueName);
        worker.setJobs(jobs);
        worker.setWorkerManagerResponseQueueName(workerManagerResponseQueueName);
        worker.setWorkerManagerTopicName(workerManagerTopicName);
        return worker;
    }
}
