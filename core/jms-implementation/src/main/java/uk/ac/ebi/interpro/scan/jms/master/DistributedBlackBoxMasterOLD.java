package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStep;

import javax.jms.JMSException;

/**
 * Master Controller for InterProScan 5.
 * <p/>
 * This implementation works for both the "Black box" and "Onion mode" versions of InterProScan 5.
 * <p/>
 * Manages the scheduling of StepIntances, based upon the pattern of dependencies in the JobXML definitions.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class DistributedBlackBoxMasterOLD extends AbstractBlackBoxMaster{

    private String tcpUri;

    private static final Logger LOGGER = Logger.getLogger(DistributedBlackBoxMasterOLD.class.getName());

    /**
     * Run the Master Application.
     */
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
                        final boolean canRunRemotely = !step.isRequiresDatabaseAccess();
                        // Only set up message selectors for high memory requirements if a suitable worker runner has been set up.
                        final boolean highMemory = resubmission && workerRunnerHighMemory != null && canRunRemotely;
                        if (highMemory) {
                            LOGGER.warn("StepInstance " + stepInstance.getId() + " will be re-run in a high-memory worker.");
                        }

                        // Serial groups should be high priority, however exclude WriteFastaFileStep from this
                        // as they are very abundant.
                        final int priority = step.getSerialGroup() == null || step instanceof WriteFastaFileStep
                                ? 4
                                : 8;

                        // Performed in a transaction.
                        messageSender.sendMessage(stepInstance, highMemory, priority, canRunRemotely);

                        final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();
                        // Start up workers appropriately.
                        if (highMemory) {
                            // This execution has failed before so use the high-memory worker runner
                            LOGGER.warn("Starting a high memory worker.");
                            workerRunnerHighMemory.startupNewWorker(priority, tcpUri, temporaryDirectoryName);
                        } else if (canRunRemotely && workerRunner != null) { // Not mandatory (e.g. in single-jvm implementation)
                            workerRunner.startupNewWorker(priority, tcpUri, temporaryDirectoryName);
                        }
                    }
                }
                // Close down (break out of loop) if the analyses are all complete.
                if (completed && stepInstanceDAO.retrieveUnfinishedStepInstances().size() == 0) {
                    // This next 'if' ensures that StepInstances created as a result of loading proteins are
                    // visible.  This is safe, because in the "closeOnCompletion" mode, an "output results" step
                    // is created, so as an absolute minimum there should be one more StepInstance than those
                    // created in the createNucleicAcidLoadStepInstance() or createFastaFileLoadStepInstance() methods.
                    // First clause - checks that the load fasta file thread has finished.
                    // Second clause - if the fasta file thread has finished, checks that all the analysis steps and the output step have finished.
                    if (stepInstanceDAO.count() > stepInstancesCreatedByLoadStep && stepInstanceDAO.retrieveUnfinishedStepInstances().size() == 0) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("There are no step instances left to run, so about to break out of loop in Master.\n\nStatistics: ");
                            LOGGER.debug("Step instances left to run: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                            LOGGER.debug("Total StepInstances: " + stepInstanceDAO.count());
                        }
                        break;
                    } else {    // This else clause is for LOGGING ONLY - no  logic here.
                        LOGGER.info("Apparently have no more unfinished StepInstances, however it looks like there should be...");
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Step instances left to run: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                            LOGGER.debug("Total StepInstances: " + stepInstanceDAO.count());
                        }
                    }
                }
                Thread.sleep(50);
            }
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by DistributedBlackBoxMasterOLD: ", e);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by DistributedBlackBoxMasterOLD: ", e);
        }
        databaseCleaner.closeDatabaseCleaner();
        LOGGER.debug("Ending");
    }


    /**
     * If the Run class has created a TCP URI message transport
     * with a random port number, this method injects the URI
     * into the Master, so that the Master can create Workers
     * listening to the broker on this URI.
     *
     * @param tcpUri created by the Run class.
     */
    public void setTcpUri(String tcpUri) {
        this.tcpUri = tcpUri;
    }

    public String getTcpUri() {
        return tcpUri;
    }


}
