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
 * Created with IntelliJ IDEA.
 * User: pjones
 */
public class StandaloneBlackBoxMaster extends AbstractBlackBoxMaster {

    private static final Logger LOGGER = Logger.getLogger(StandaloneBlackBoxMaster.class.getName());

    private StatsUtil statsUtil;

    @Override
    public void run() {
        final long now = System.currentTimeMillis();
        super.run();

        if(verboseLog){
            System.out.println(Utilities.getTimeNow() + " verboseLog: " + verboseLog + " verboseLogLevel: " + verboseLogLevel);
            System.out.println(Utilities.getTimeNow() + " DEBUG inVmWorkers min:" + getConcurrentInVmWorkerCount() + " max: " + getMaxConcurrentInVmWorkerCount());
        }
        try {
            loadInMemoryDatabase();
            int stepInstancesCreatedByLoadStep = createStepInstances();
            if(verboseLog){
                System.out.println(Utilities.getTimeNow() + " DEBUG " +  " step instances: " + stepInstanceDAO.count());
            }
            // If there is an embeddedWorkerFactory (i.e. this Master is running in stand-alone mode)
            // stop running if there are no StepInstances left to complete.
            boolean controlledLogging = false;
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

                        final boolean highPriorityStep = false; //isHighPriorityStep(step);
                        final boolean lowPriorityStep  =  (! highPriorityStep) &&  (step.getSerialGroup() == null || step instanceof WriteFastaFileStep);

                        // Serial groups should be high priority, however exclude WriteFastaFileStep from this
                        // as they are very abundant.
                        final int priority = lowPriorityStep ? 4 : 8;


                        // Performed in a transaction.
                        LOGGER.debug("About to send a message for StepInstance: " + stepInstance);
                        messageSender.sendMessage(stepInstance, false, priority, false);
                        statsUtil.addToSubmittedStepInstances(stepInstance);
                        controlledLogging = false;
                    }
                }
                //check what is not completed
//                statsUtil.memoryMonitor();
                if(!controlledLogging){
                    LOGGER.debug("StandAlone Master has no jobs ready .. more Jobs will be made ready ");
                    LOGGER.debug("Step instances left to run: " + stepInstanceDAO.retrieveUnfinishedStepInstances().size());
                    LOGGER.debug("Total StepInstances: " + stepInstanceDAO.count());
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
                Thread.sleep(100);  // Make sure the Master thread is not hogging resources required by in-memory workers.
            }
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by StandaloneBlackBoxMaster: ", e);
            systemExit(999);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by StandaloneBlackBoxMaster: ", e);
            systemExit(999);
        }

        System.out.println(Utilities.getTimeNow() + " 100% done:  InterProScan analyses completed");

        if(verboseLog){
            final long executionTime =   System.currentTimeMillis() - now;
            System.out.println("Execution time : (" + TimeUnit.MILLISECONDS.toSeconds(executionTime)+ " s) => " + String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(executionTime),
                    TimeUnit.MILLISECONDS.toSeconds(executionTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime))
            ));
        }
        systemExit(0);
    }

    /**
     * Exit InterProScan 5 immediately with the supplied exit code.
     * @param status Exit code to use
     */
    private void systemExit(int status){
        try {
            databaseCleaner.closeDatabaseCleaner();
            LOGGER.debug("Ending");
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            cleanUpWorkingDirectory();
            // Always exit
            if(status != 0){
                System.err.println("InterProScan analysis failed. Exception thrown by StandaloneBlackBoxMaster. Check the log file for details");           
                System.exit(status);
            }
        }
	//let Run do the cleanup
        //System.exit(status);
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
        if(step.getId().toLowerCase().contains("pirsf".toLowerCase()) || step.getId().toLowerCase().contains("hamap".toLowerCase())){
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(" pirsf/hamap job: " + step.getId()+ " Should have high priority, but priority is normally 4");
            }
            return true;
        }
        return false;
    }
}
