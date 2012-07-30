package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStep;

import javax.jms.JMSException;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 * Date: 26/07/12
 */
public class ProductionMaster extends AbstractMaster {

    private static final Logger LOGGER = Logger.getLogger(ProductionMaster.class.getName());

    /**
     * Run the Production Master Application.
     */
    public void run() {
        super.run();
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Started Production Master run() method.");
        try {

            // If there is an embeddedWorkerFactory (i.e. this Master is running in stand-alone mode)
            // stop running if there are no StepInstances left to complete.
            while (!shutdownCalled) {
                for (StepInstance stepInstance : stepInstanceDAO.retrieveUnfinishedStepInstances()) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Iterating over StepInstances: Currently on " + stepInstance);
                    }
                    if (stepInstance.hasFailedPermanently(jobs)) {
                        unrecoverableErrorStrategy.failed(stepInstance, jobs);
                    }
                    if (stepInstance.canBeSubmitted(jobs) && stepInstanceDAO.serialGroupCanRun(stepInstance, jobs)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Step submitted:" + stepInstance);
                        }
                        final boolean resubmission = stepInstance.getExecutions().size() > 0;
                        if (resubmission) {
                            LOGGER.warn("StepInstance " + stepInstance.getId() + " is being re-run following a failure.");
                        }
                        final Step step = stepInstance.getStep(jobs);


                        // Serial groups should be high priority, however exclude WriteFastaFileStep from this
                        // as they are very abundant.
                        final int priority = step.getSerialGroup() == null || step instanceof WriteFastaFileStep
                                ? 4
                                : 8;

                        // Only set up message selectors for high memory requirements if a suitable worker runner has been set up.
                        final boolean useHighMemoryWorker = resubmission && workerRunnerHighMemory != null;

                        // Performed in a transaction.
                        messageSender.sendMessage(stepInstance, useHighMemoryWorker, priority, true);

                        final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();
                        // Start up workers appropriately.
                        if (useHighMemoryWorker) {
                            // This execution has failed before so use the high-memory worker runner
                            LOGGER.warn("StepInstance " + stepInstance.getId() + " will be re-run in a high-memory worker.");
                            workerRunnerHighMemory.startupNewWorker(priority, null, temporaryDirectoryName);
                        } else {
                            workerRunner.startupNewWorker(priority, null, temporaryDirectoryName);
                        }
                    }
                }
                Thread.sleep(1000);   // Don't want to hammer Oracle with requests for step instances that can be run.
            }
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by ProductionMaster: ", e);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by ProductionMaster: ", e);
        }
        LOGGER.debug("Ending");
    }

    /**
     * Called by quartz to load proteins from UniParc.
     */
    public void createProteinLoadJob() {
        createStepInstancesForJob("jobLoadFromUniParc", null);
    }
}
