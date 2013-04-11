package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.jms.stats.Utilities;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStep;

import javax.jms.JMSException;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 */
public class StandaloneBlackBoxMaster extends AbstractBlackBoxMaster {

    private static final Logger LOGGER = Logger.getLogger(StandaloneBlackBoxMaster.class.getName());

    private StatsUtil statsUtil;

    @Override
    public void run() {
        super.run();
        try {
            loadInMemoryDatabase();
            int stepInstancesCreatedByLoadStep = createStepInstances();

            // If there is an embeddedWorkerFactory (i.e. this Master is running in stand-alone mode)
            // stop running if there are no StepInstances left to complete.
            while (!shutdownCalled) {
                boolean completed = true;

                for (StepInstance stepInstance : stepInstanceDAO.retrieveUnfinishedStepInstances()) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Iterating over StepInstances: Currently on " + stepInstance);
                    }
                    if (stepInstance.hasFailedPermanently(jobs)) {
                        unrecoverableErrorStrategy.failed(stepInstance, jobs);
                    }
                    completed &= stepInstance.haveFinished(jobs);
                    if (stepInstance.canBeSubmitted(jobs) && stepInstanceDAO.serialGroupCanRun(stepInstance, jobs)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Step submitted:" + stepInstance);
                        }
                        final boolean resubmission = stepInstance.getExecutions().size() > 0;
                        if (resubmission) {
                            LOGGER.warn("StepInstance " + stepInstance.getId() + " is being re-run following a failure.");
                        }
                        final Step step = stepInstance.getStep(jobs);
                        // Only set up message selectors for high memory requirements if a suitable worker runner has been set up.

                        // Serial groups should be high priority, however exclude WriteFastaFileStep from this
                        // as they are very abundant.
                        final int priority = step.getSerialGroup() == null || step instanceof WriteFastaFileStep
                                ? 4
                                : 8;

                        // Performed in a transaction.
                        LOGGER.debug("About to send a message for StepInstance: " + stepInstance);
                        messageSender.sendMessage(stepInstance, false, priority, false);
                    }
                }

                //report progress
                statsUtil.setTotalJobs(stepInstanceDAO.count());
                statsUtil.setUnfinishedJobs(stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                statsUtil.displayMasterProgress();

                // Close down (break out of loop) if the analyses are all complete.
                // The final clause checks that the protein load steps have been created so
                // i5 doesn't finish prematurely.
                if (completed &&
                        stepInstanceDAO.retrieveUnfinishedStepInstances().size() == 0
                        && stepInstanceDAO.count() > stepInstancesCreatedByLoadStep) {
                    break;
                }
                Thread.sleep(200);  // Make sure the Master thread is not hogging resources required by in-memory workers.
            }
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by DistributedBlackBoxMaster: ", e);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by DistributedBlackBoxMaster: ", e);
        }
        databaseCleaner.closeDatabaseCleaner();
        LOGGER.debug("Ending");
        System.out.println(Utilities.getTimeNow() + " 100% done:  InterProScan analyses completed");
    }

    /**
     *
     * @param statsUtil
     */
    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }
}
