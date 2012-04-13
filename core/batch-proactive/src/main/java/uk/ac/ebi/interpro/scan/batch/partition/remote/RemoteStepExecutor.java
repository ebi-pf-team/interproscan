package uk.ac.ebi.interpro.scan.batch.partition.remote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;

/**
 * Executes step on remote node.
 *
 * @author Antony Quinn
 * @version $Id$
 * @since 1.0
 */
public class RemoteStepExecutor implements Serializable {

    private static final Log logger = LogFactory.getLog(RemoteStepExecutor.class);

    private final StepExecution stepExecution;
    private final String stepName;
    private final String appContextLocation;

    public RemoteStepExecutor(String appContextLocation, String stepName, StepExecution stepExecution) {
        this.appContextLocation = appContextLocation;
        this.stepName = stepName;
        this.stepExecution = stepExecution;
    }

    public StepExecution execute() throws JobInterruptedException {
        // Log stepExecution parameters
        logger.info(stepExecution);
        logger.info(stepExecution.getJobExecution().getJobInstance());
        // Get step to run
        ApplicationContext context = new ClassPathXmlApplicationContext(appContextLocation);
        Step step = context.getBean(stepName, Step.class);
        try {
            step.execute(stepExecution);
        } catch (JobInterruptedException e) {
            stepExecution.getJobExecution().setStatus(BatchStatus.STOPPING);
            throw (e);
        }
        return stepExecution;
    }

    public String getStepName() {
        return stepName;
    }

}
