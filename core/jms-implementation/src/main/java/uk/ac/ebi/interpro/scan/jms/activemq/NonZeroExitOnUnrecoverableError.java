package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

/**
 * Strategy to employ in the black-box version of I5 in the event that
 * an error occurs from which no recovery can be made.  This strategy
 * logs the error and then causes the JVM to exit with a non-zero exit
 * status.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class NonZeroExitOnUnrecoverableError implements UnrecoverableErrorStrategy {

    private static final Logger LOGGER = LogManager.getLogger(NonZeroExitOnUnrecoverableError.class.getName());

    @Override
    public void failed(StepInstance stepInstance, Jobs jobs) {
        LOGGER.fatal(new StringBuilder()
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
                LOGGER.fatal("Execution " + execCount++ + " returned StackTrace: \n" + execution.getException());
            }
        }
        LOGGER.fatal("The JVM will now exit with a non-zero exit status.");
        throw new IllegalStateException("InterProScan exiting with non-zero status, see logs for further information.");
    }
}
