package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.jms.stats.Utilities;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteFastaFileStep;

import javax.jms.JMSException;
import java.util.concurrent.TimeUnit;

/**
 * @author Gift Nuka
 * @author  Phil Jones
 */
public class SSOptimisedBlackBoxMaster extends AbstractBlackBoxMaster {

    private static final Logger LOGGER = Logger.getLogger(SSOptimisedBlackBoxMaster.class.getName());

    private StatsUtil statsUtil;

    private static final int MEGA = 1024 * 1024;

    @Override
    public void run() {
        final long now = System.currentTimeMillis();
        super.run();
        System.out.println(Utilities.getTimeNow() + " DEBUG ssDebug: "  + ssDebug);
        if(ssDebug){
            System.out.println(Utilities.getTimeNow() + " DEBUG " + "inVmWorkers min:" + getConcurrentInVmWorkerCount() + " max: " + getMaxConcurrentInVmWorkerCount());
            System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
            System.out.println("Memory free: " + Runtime.getRuntime().freeMemory() / MEGA + "MB total: " + Runtime.getRuntime().totalMemory() / MEGA + "MB max: " + Runtime.getRuntime().maxMemory() / MEGA + "MB");
        }
        try {
            loadInMemoryDatabase();
            int stepInstancesCreatedByLoadStep = createStepInstances();
            int inputSize = 1;

            // If there is an embeddedWorkerFactory (i.e. this Master is running in stand-alone mode)
            // stop running if there are no StepInstances left to complete.
            boolean controlledLogging = false;
            int stepInstanceSubmitCount = 0;
            while (!shutdownCalled) {
                boolean completed = true;
                if (stepInstanceSubmitCount == 1 && (! isUseMatchLookupService())){
                    for (StepInstance stepInstance : stepInstanceDAO.retrieveUnfinishedStepInstances()) {
                        if (isHighPriorityStep(stepInstance.getStep(jobs))){
                            completed &= stepInstance.haveFinished(jobs);
                            stepInstanceSubmitCount += submitStepInstanceToRequestQueue(stepInstance);
                            if(ssDebug){
                                System.out.println("step-submitted: " + stepInstance.getStep(jobs).getId());
                            }
                        }
                    }
                    if(ssDebug){
//                        System.out.println(Utilities.getTimeNow() + " DEBUG stepInstanceSubmitCount (1): " + stepInstanceSubmitCount);
//                        System.out.println(Utilities.getTimeNow() + " DEBUG unifinished jobs (1): " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                    }
                    controlledLogging = false;
                }else{
                    for (StepInstance stepInstance : stepInstanceDAO.retrieveUnfinishedStepInstances()) {
                        completed &= stepInstance.haveFinished(jobs);
                        stepInstanceSubmitCount += submitStepInstanceToRequestQueue(stepInstance);
                    }
                    controlledLogging = false;
                }
                //check what is not completed
//                statsUtil.memoryMonitor();
                if(!controlledLogging){
                    LOGGER.debug("InterProScan Master has no stepInstances ready .. more may be ready soon ");
                    LOGGER.debug("Step instances left to run: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                    LOGGER.debug("Total StepInstances: " + stepInstanceDAO.count());
//                    System.out.println(Utilities.getTimeNow() + " DEBUG stepInstanceSubmitCount (2): " + stepInstanceSubmitCount);
                    controlledLogging = true;
                }
                //report progress
                statsUtil.setTotalJobs(stepInstanceDAO.count());
                statsUtil.setUnfinishedJobs(stepInstanceDAO.retrieveUnfinishedStepInstances().size());
//                final boolean statsAvailable = statsUtil.pollStatsBrokerJobQueue();
                statsUtil.displayMasterProgress();

                // Close down (break out of loop) if the analyses are all complete.
                // The final clause checks that the protein load steps have been created so
                // i5 doesn't finish prematurely.
                if (completed &&
                        stepInstanceDAO.retrieveUnfinishedStepInstances().size() == 0
                        && stepInstanceDAO.count() > stepInstancesCreatedByLoadStep) {
                    break;
                }
                //for standalone es mode this should be < 200
                Thread.sleep(50);  // Make sure the Master thread is not hogging resources required by in-memory workers.
            }
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by FastResponseBlackBoxMaster: ", e);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by FastResponseBlackBoxMaster: ", e);
        }
        databaseCleaner.closeDatabaseCleaner();
        LOGGER.debug("Ending");
        System.out.println(Utilities.getTimeNow() + " 100% done:  InterProScan analyses completed");
        if(ssDebug){
            final long executionTime =   System.currentTimeMillis() - now;
            System.out.println("Execution time (s) for Master: " + String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(executionTime),
                    TimeUnit.MILLISECONDS.toSeconds(executionTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime))
            ));
        }
    }


    public int submitStepInstanceToRequestQueue(StepInstance stepInstance) throws JMSException{
        try {
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
                // Only set up message selectors for high memory requirements if a suitable worker runner has been set up.

                final boolean highPriorityStep = false; //isHighPriorityStep(step);
                final boolean lowPriorityStep  =  (! highPriorityStep) &&  (step.getSerialGroup() == null || step instanceof WriteFastaFileStep);

                // Serial groups should be high priority, however exclude WriteFastaFileStep from this
                // as they are very abundant.
                final int priority = lowPriorityStep ? 4 : 8;


                // Performed in a transaction.
                LOGGER.debug("About to send a message for StepInstance: " + stepInstance);
                messageSender.sendMessage(stepInstance, false, priority, false);
                return 1;
            }
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by FastResponseBlackBoxMaster: ", e);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by FastResponseBlackBoxMaster: ", e);
        }
        return 0;
    }
    /**
     *
     * @param statsUtil
     */
    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }

    /**
     * * check if the job is hamap or prosite
     *  then assign it higher priority
     *
     * @param step
     * @return
     */
    public boolean  isHighPriorityStep(Step step){
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(" Step Id for pirsf, hamap : " + step.getId());
        }
        if(step.getId().toLowerCase().contains("pirsf".toLowerCase())
                || step.getId().toLowerCase().contains("hamap".toLowerCase())
                || step.getId().toLowerCase().contains("pfama".toLowerCase())
                || step.getId().toLowerCase().contains("gene3d".toLowerCase())
                || step.getId().toLowerCase().contains("tigrfam".toLowerCase())
                ){
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(" pirsf/hamap/pfam/gene3d/tigrfam job: " + step.getId()+ " Should have high priority");
            }
            return true;
        }
        if(step.getId().toLowerCase().contains("".toLowerCase())
                || step.getId().toLowerCase().contains("hamap".toLowerCase())
                || step.getId().toLowerCase().contains("pfama".toLowerCase())
                || step.getId().toLowerCase().contains("gene3d".toLowerCase())
                || step.getId().toLowerCase().contains("tigrfam".toLowerCase())
                ){
        }
        return false;
    }
}
