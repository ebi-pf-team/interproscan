package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.io.TemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.jms.activemq.MasterMessageSender;
import uk.ac.ebi.interpro.scan.jms.activemq.UnrecoverableErrorStrategy;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.WorkerRunner;
import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pjones
 * Date: 26/07/12
 */
public abstract class AbstractMaster implements Master {

    private static final Logger LOGGER = Logger.getLogger(AbstractMaster.class.getName());
    protected Jobs jobs;
    protected StepInstanceDAO stepInstanceDAO;
    protected MasterMessageSender messageSender;
    protected volatile boolean shutdownCalled = false;
    protected String[] analyses;
    protected WorkerRunner workerRunner;
    protected WorkerRunner workerRunnerHighMemory;
    protected UnrecoverableErrorStrategy unrecoverableErrorStrategy;
    protected TemporaryDirectoryManager temporaryDirectoryManager;
    protected String baseDirectoryTemporaryFiles;
    protected String temporaryFileDirSuffix;

    //Misc
    protected boolean deleteWorkingDirectoryOnCompletion;

    public void setWorkerRunner(WorkerRunner workerRunner) {
        this.workerRunner = workerRunner;
    }

    public WorkerRunner getWorkerRunner() {
        return workerRunner;
    }

    public WorkerRunner getWorkerRunnerHighMemory() {
        return workerRunnerHighMemory;
    }

    public void setWorkerRunnerHighMemory(WorkerRunner workerRunnerHighMemory) {
        this.workerRunnerHighMemory = workerRunnerHighMemory;
    }

    public void setTemporaryDirectoryManager(TemporaryDirectoryManager temporaryDirectoryManager) {
        this.temporaryDirectoryManager = temporaryDirectoryManager;
    }

    public TemporaryDirectoryManager getTemporaryDirectoryManager() {
        return temporaryDirectoryManager;
    }

    public boolean isDeleteWorkingDirectoryOnCompletion() {
        return deleteWorkingDirectoryOnCompletion;
    }

    public void setDeleteWorkingDirectoryOnCompletion(boolean deleteWorkingDirectoryOnCompletion) {
        this.deleteWorkingDirectoryOnCompletion = deleteWorkingDirectoryOnCompletion;
    }

    @Required
    public void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO) {
        this.stepInstanceDAO = stepInstanceDAO;
    }

    public Jobs getJobs() {
        return jobs;
    }

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    @Required
    public void setMessageSender(MasterMessageSender messageSender) {
        this.messageSender = messageSender;
    }


    /**
     * Depending upon the mode of usage, I5 should handle failures appropriately / gracefully.
     * The black box version for example, should log the error and exit with a non-zero
     * exit status.  The production pipeline version should send an email, but continue
     * to operate.
     *
     * @param unrecoverableErrorStrategy implementing the correct behaviour when an unrecoverable
     *                                   error occurs.
     */
    @Required
    public void setUnrecoverableErrorStrategy(UnrecoverableErrorStrategy unrecoverableErrorStrategy) {
        this.unrecoverableErrorStrategy = unrecoverableErrorStrategy;
    }

    @Required
    public void setTemporaryFileDirSuffix(String temporaryFileDirSuffix) {
        this.temporaryFileDirSuffix = temporaryFileDirSuffix;
    }

    public void setupTemporaryDirectory() {
        //Change base dir temp directory if
        if (baseDirectoryTemporaryFiles != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("1. baseDirectoryTemporaryFiles:- " + baseDirectoryTemporaryFiles);
            }
            if (!baseDirectoryTemporaryFiles.endsWith(File.separator)) {
                setTemporaryDirectory(baseDirectoryTemporaryFiles + File.separator);
            }
            if (baseDirectoryTemporaryFiles.endsWith(temporaryFileDirSuffix + File.separator)) {
                // The [UNIQUE] was already added (use temp directory as specified in interproscan.properties)
                jobs.setBaseDirectoryTemporaryFiles(baseDirectoryTemporaryFiles);
            }
            else {
                // The [UNIQUE] directory needs adding now (temp directory was probably specified by the user on the command line)
                jobs.setBaseDirectoryTemporaryFiles(baseDirectoryTemporaryFiles + temporaryFileDirSuffix);
                setTemporaryDirectory(jobs.getBaseDirectoryTemporaryFiles());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("2. baseDirectoryTemporaryFiles:- " + baseDirectoryTemporaryFiles);
            }
        } else {
            LOGGER.debug("baseDirectoryTemporaryFiles  is null ");
        }
    }



    public void run() {
        LOGGER.debug("Started Master run() method.");
        setupTemporaryDirectory();
    }

    /**
     * This method creates the required step instances for the specified job and
     * set of parameters, returning the number of StepInstances created.
     *
     * @param jobId      to specify the job
     * @param parameters to the analysis.
     * @return the number of StepInstances created
     */
    protected int createStepInstancesForJob(String jobId, Map<String, String> parameters) {
        int stepInstancesCreatedCount = 0;
        Job job = jobs.getJobById(jobId);
        final Map<Step, List<StepInstance>> stepToStepInstances = new HashMap<>();
        for (Step step : job.getSteps()) {
            stepInstancesCreatedCount++;
            StepInstance stepInstance = new StepInstance(step);
            stepInstance.addParameters(parameters);
            List<StepInstance> mappedStepInstance = stepToStepInstances.get(step);
            if (mappedStepInstance == null) {
                mappedStepInstance = new ArrayList<>();
                stepToStepInstances.put(step, mappedStepInstance);
            }
            mappedStepInstance.add(stepInstance);
        }
        addDependenciesAndStore(stepToStepInstances);
        return stepInstancesCreatedCount;

    }

    /**
     * Takes a list of newly created StepInstance objects in a Map<Step, List<StepInstance>>
     * and sets up the dependencies between them.  Then stores the StepInstance objects to the database.
     *
     * @param stepToStepInstances a Map<Step, List<StepInstance>> to allow the dependencies to be efficiently set up.
     */
    private void addDependenciesAndStore(Map<Step, List<StepInstance>> stepToStepInstances) {
        // Add the dependencies to the StepInstances.
        for (Step step : stepToStepInstances.keySet()) {
            for (StepInstance stepInstance : stepToStepInstances.get(step)) {
                final List<Step> dependsUpon = stepInstance.getStep(jobs).getDependsUpon();
                if (dependsUpon != null) {
                    for (Step stepRequired : dependsUpon) {
                        List<StepInstance> candidateStepInstances = stepToStepInstances.get(stepRequired);
                        if (candidateStepInstances != null) {
                            for (StepInstance candidate : candidateStepInstances) {
                                // If the two StepInstance are dealing with overlapping proteins,
                                // OR if they are not dealing with proteins ranges, then they
                                // are dependent as defined in the Job XML file.
                                if (stepInstance.proteinBoundsOverlap(candidate) || (candidate.getBottomProtein() == null && candidate.getTopProtein() == null)) {
                                    stepInstance.addDependentStepInstance(candidate);
                                }
                            }
                        }
                    }
                }
            }
            // Persist the StepInstances that now have their dependencies added.
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Persisting " + stepToStepInstances.get(step).size() + " StepInstances for Step " + step.getId());
                for (StepInstance stepInstance : stepToStepInstances.get(step)) {
                    LOGGER.debug("About to attempt to persist stepInstance: " + stepInstance.getId() + " with " + stepInstance.stepInstanceDependsUpon().size() + " dependent steps.");
                }
            }
        }
        try {
            stepInstanceDAO.insert(stepToStepInstances);
        } catch (Throwable t) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error thrown by stepInstance DAO");
                LOGGER.debug(t.getMessage());
            }
        }


    }

    public String getTemporaryDirectory() {
        return this.baseDirectoryTemporaryFiles;
    }

    @Required
    public void setTemporaryDirectory(String temporaryDirectory) {
        this.baseDirectoryTemporaryFiles = temporaryDirectory;
    }

    /**
     * get the absolute path the temporary working directory
     *
     * @return
     */
    public String getWorkingTemporaryDirectoryPath(){
        return temporaryDirectoryManager.replacePath(baseDirectoryTemporaryFiles);
    }

    private void deleteWorkingTemporaryDirectory(String dirPath) throws IOException {
        File dir = new File(dirPath);
        try {
            FileUtils.deleteDirectory(dir);
        }catch (IOException e) {
            LOGGER.warn("Unable to delete temporary directory " + dir.getAbsolutePath());
        }
    }

    public void cleanUpWorkingDirectory(){
        if(isDeleteWorkingDirectoryOnCompletion()) {
            final String temporaryDirectoryName  = getWorkingTemporaryDirectoryPath();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Clean temporaryDirectoryName : " + temporaryDirectoryName);
            }
            try {
                deleteWorkingTemporaryDirectory(temporaryDirectoryName);
            } catch (IOException e) {
                LOGGER.warn("At run completion, unable to delete temporary working directory " + temporaryDirectoryName);
                e.printStackTrace();
            }
        }
    }





    /**
     * Optionally, set the analyses that should be run.
     * If not set, or set to null, all analyses will be run.
     *
     * @param analyses a comma separated list of analyses (job names) that should be run. Null for all jobs.
     */
    public void setAnalyses(String[] analyses) {
        this.analyses = analyses;
    }


}
