package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

/**
 * This UnrecoverableErrorStrategy is intended for use in production (distributed) mode,
 * where the Master should continue to run despite failures, but the system maintainer
 * should be informed of the failures.  This implementation will send an email
 * to describe the failure.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class EmailErrorAndContinueErrorStrategy implements UnrecoverableErrorStrategy {

    private static final Logger LOGGER = LogManager.getLogger(EmailErrorAndContinueErrorStrategy.class.getName());

    @Override
    public void failed(StepInstance stepInstance, Jobs jobs) {
        // TODO - Send an email, not just logging!
        LOGGER.error(new StringBuilder()
                .append("Analysis step ")
                .append(stepInstance.getId())
                .append(" : ")
                .append(stepInstance.getStep(jobs).getStepDescription())
                .append(" for proteins ")
                .append(stepInstance.getBottomProtein())
                .append(" to ")
                .append(stepInstance.getTopProtein())
                .append(" has failed irretrievably.  Available StackTraces follow.")
                .toString());
        int execCount = 1;
        for (StepExecution execution : stepInstance.getExecutions()) {
            if (execution != null && execution.getException() != null) {
                LOGGER.error("Execution " + execCount++ + " returned StackTrace: \n" + execution.getException());
            }
        }
    }
}
