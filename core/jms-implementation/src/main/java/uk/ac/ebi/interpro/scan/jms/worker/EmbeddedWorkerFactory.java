package uk.ac.ebi.interpro.scan.jms.worker;

import org.hornetq.jms.client.HornetQConnectionFactory;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.management.model.Jobs;

/**
 * TODO: Description
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public interface EmbeddedWorkerFactory {
    
    @Required
    void setConnectionFactory(HornetQConnectionFactory connectionFactory);

    @Required
    void setJobRequestQueueName(String jobRequestQueueName);

    @Required
    void setJobResponseQueueName(String jobResponseQueueName);

    @Required
    void setJobs(Jobs jobs);

    void setWorkerManagerTopicName(String workerManagerTopicName);

    void setWorkerManagerResponseQueueName(String workerManagerResponseQueueName);

    Worker getInstance();
}
