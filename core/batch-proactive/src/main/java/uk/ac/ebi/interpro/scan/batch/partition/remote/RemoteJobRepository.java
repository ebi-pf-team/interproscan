package uk.ac.ebi.interpro.scan.batch.partition.remote;

import org.apache.log4j.Logger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;

/**
 * Implementation of {@link JobRepository} that allows partitioned steps to run on remote nodes with no access
 * to the job repository on the master node.
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
public class RemoteJobRepository implements JobRepository {

    private static final Logger LOGGER = Logger.getLogger(RemoteJobRepository.class.getName());

    /**
     * Provide default constructor with low visibility in case user wants to use
     * use aop:proxy-target-class="true" for AOP interceptor.
     */
    RemoteJobRepository() {
    }

    public boolean isJobInstanceExists(String jobName, JobParameters jobParameters) {
        throw new UnsupportedOperationException();
    }

    public JobExecution createJobExecution(String jobName, JobParameters jobParameters)
            throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        throw new UnsupportedOperationException();
    }

    public void update(JobExecution jobExecution) {
        throw new UnsupportedOperationException();
    }

    public void add(StepExecution stepExecution) {
        throw new UnsupportedOperationException();
    }

    public void update(StepExecution stepExecution) {
        LOGGER.info("update: " + stepExecution.toString());
    }

    public void updateExecutionContext(StepExecution stepExecution) {
        LOGGER.info("updateExecutionContext: " + stepExecution.getExecutionContext().toString());
    }

    public void updateExecutionContext(JobExecution jobExecution) {
        throw new UnsupportedOperationException();
    }

    public StepExecution getLastStepExecution(JobInstance jobInstance, String stepName) {
        throw new UnsupportedOperationException();
    }

    public int getStepExecutionCount(JobInstance jobInstance, String stepName) {
        throw new UnsupportedOperationException();
    }

    public JobExecution getLastJobExecution(String jobName, JobParameters jobParameters) {
        throw new UnsupportedOperationException();
    }

}
