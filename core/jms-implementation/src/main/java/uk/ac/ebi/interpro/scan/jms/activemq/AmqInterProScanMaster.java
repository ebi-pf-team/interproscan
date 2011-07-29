package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
import uk.ac.ebi.interpro.scan.jms.master.Master;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.WorkerRunner;
import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteOutputStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.StepCreatingStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.nucleotide.RunGetOrfStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad.FastaFileLoadStep;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class AmqInterProScanMaster implements Master {

    private static final Logger LOGGER = Logger.getLogger(AmqInterProScanMaster.class.getName());

    private Jobs jobs;

    private StepInstanceDAO stepInstanceDAO;

    private MasterMessageSender messageSender;

    private boolean shutdownCalled = false;

    private String fastaFilePath;

    private String outputFile;

    private String outputFormat = "tsv";

    private String[] analyses;

    private WorkerRunner workerRunner;

    private WorkerRunner workerRunnerHighMemory;

    private UnrecoverableErrorStrategy unrecoverableErrorStrategy;
    private String sequenceType;


    public void setWorkerRunner(WorkerRunner workerRunner) {
        this.workerRunner = workerRunner;
    }

    public void setWorkerRunnerHighMemory(WorkerRunner workerRunnerHighMemory) {
        this.workerRunnerHighMemory = workerRunnerHighMemory;
    }

    /**
     * This boolean allows configuration of whether or not the Master closes down when there are no more
     * runnable StepExecutions available.
     */
    private boolean closeOnCompletion;

    private CleanRunDatabase databaseCleaner;

    private boolean cleanDatabase = false;
    private boolean mapToInterPro = false;
    private boolean mapToGO = false;
    private boolean mapToPathway = false;

    @Required
    public void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO) {
        this.stepInstanceDAO = stepInstanceDAO;
    }

    @Required
    public void setCloseOnCompletion(boolean closeOnCompletion) {
        this.closeOnCompletion = closeOnCompletion;
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

    /**
     * Run the Master Application.
     */
    public void run() {
        try {
            if (cleanDatabase) {
                Thread databaseLoaderThread = new Thread(databaseCleaner);
                LOGGER.debug("Loading database into memory...");
                databaseLoaderThread.start();
                // Pause while the database is loaded from the zip backup
                while (databaseCleaner.stillLoading()) {
                    // Takes about 1500 ms to load the database
                    Thread.sleep(200);
                }
                LOGGER.debug("Database loaded.");
            }

            if ("n".equalsIgnoreCase(this.sequenceType)) {
                createNucleicAcidLoadStepInstance();
            } else {
                createFastaFileLoadStepInstance();
            }


            // If there is an embeddedWorkerFactory (i.e. this Master is running in stand-alone mode)
            // stop running if there are no StepInstances left to complete.
            while (!shutdownCalled) {
                boolean completed = true;

                for (StepInstance stepInstance : stepInstanceDAO.retrieveUnfinishedStepInstances()) {
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
                        // Only set up message selectors for high memory requirements if a suitable worker runner has been set up.
                        final boolean highMemory = resubmission && workerRunnerHighMemory != null;
                        if (highMemory) {
                            LOGGER.warn("StepInstance " + stepInstance.getId() + " will be re-run in a high-memory worker.");
                        }

                        final int priority = stepInstance.getStep(jobs).getSerialGroup() == null ? 4 : 8;

                        // Performed in a transaction.
                        messageSender.sendMessage(stepInstance, highMemory, priority);

                        // Start up workers appropriately.
                        if (highMemory) {
                            // This execution has failed before so use the high-memory worker runner
                            LOGGER.warn("Starting a high memory worker.");
                            workerRunnerHighMemory.startupNewWorker(priority);
                        } else if (workerRunner != null) { // Not mandatory (e.g. in single-jvm implementation)
                            workerRunner.startupNewWorker(priority);
                        }
                    }
                }


                if (closeOnCompletion && completed) break;
                Thread.sleep(200);  // Every half second, checks for any runnable StepInstances and runs them.
            }
        } catch (JMSException e) {
            LOGGER.error("JMSException thrown by Master", e);
        } catch (Exception e) {
            LOGGER.error("Exception thrown by Master", e);
        }
        if (cleanDatabase) {
            databaseCleaner.closeDatabaseCleaner();
        }
        LOGGER.debug("Ending");
    }

    /**
     * If a fastaFilePath has been passed in as an argument, then StepInstances are created
     * for the fasta file loading job.  Note that this also creates all of the necessary StepInstances
     * for analyses for the loaded proteins.
     */
    private void createFastaFileLoadStepInstance() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating FASTA file load step.");
        }
        if (fastaFilePath != null) {
            Map<String, String> params = new HashMap<String, String>();
            params.put(FastaFileLoadStep.FASTA_FILE_PATH_KEY, fastaFilePath);
            createBlackBoxParams(params);
            createStepInstancesForJob("jobLoadFromFasta", params);
            LOGGER.info("Fasta file load step instance has been created.");
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("No fasta file path to load.");
        }
    }

    private void createNucleicAcidLoadStepInstance() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("creating nucleic acid load step.");
        }
        if (fastaFilePath != null) {
            Map<String, String> params = new HashMap<String, String>();
            params.put(RunGetOrfStep.SEQUENCE_FILE_PATH_KEY, fastaFilePath);
            params.put(FastaFileLoadStep.FASTA_FILE_PATH_KEY, fastaFilePath);
            createBlackBoxParams(params);
            createStepInstancesForJob("jobLoadNucleicAcidSequence", params);
        } else {
            LOGGER.error("No nucleic acid sequence file path has been provided to load.");
        }
    }

    private void createBlackBoxParams(final Map<String, String> params) {
        if (analyses != null) {
            List<String> jobNameList = new ArrayList<String>();
            for (String analysisName : analyses) {
                jobNameList.add("job" + analysisName);
            }
            params.put(StepCreatingStep.ANALYSIS_JOB_NAMES_KEY, StringUtils.collectionToCommaDelimitedString(jobNameList));
        }
        params.put(StepCreatingStep.COMPLETION_JOB_NAME_KEY, "jobWriteOutput");

        String outputFilePath = outputFile;
        if (outputFilePath == null) {
            outputFilePath = fastaFilePath.replaceAll("\\.fasta", "") + "." + outputFormat.toLowerCase();
        }
        params.put(WriteOutputStep.OUTPUT_FILE_PATH_KEY, outputFilePath);
        params.put(WriteOutputStep.OUTPUT_FILE_FORMAT, outputFormat);
        params.put(WriteOutputStep.MAP_TO_INTERPRO_ENTRIES, Boolean.toString(mapToInterPro));
        params.put(WriteOutputStep.MAP_TO_GO, Boolean.toString(mapToGO));
        params.put(WriteOutputStep.MAP_TO_PATHWAY, Boolean.toString(mapToPathway));
    }

    /**
     * Called by quartz to load proteins from UniParc.
     */
    public void createProteinLoadJob() {
        createStepInstancesForJob("jobLoadFromUniParc", null);
    }

    private void createStepInstancesForJob(String jobId, Map<String, String> parameters) {
        Job job = jobs.getJobById(jobId);
        final Map<Step, List<StepInstance>> stepToStepInstances = new HashMap<Step, List<StepInstance>>();
        for (Step step : job.getSteps()) {
            StepInstance stepInstance = new StepInstance(step);
            stepInstance.addParameters(parameters);
            List<StepInstance> mappedStepInstance = stepToStepInstances.get(step);
            if (mappedStepInstance == null) {
                mappedStepInstance = new ArrayList<StepInstance>();
                stepToStepInstances.put(step, mappedStepInstance);
            }
            mappedStepInstance.add(stepInstance);
        }
        addDependenciesAndStore(stepToStepInstances);
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
                                if (stepInstance.proteinBoundsOverlap(candidate)) {
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
            stepInstanceDAO.insert(stepToStepInstances.get(step));
        }
    }

    /**
     * If a fasta file path is set, load the proteins at start up and analyse them.
     *
     * @param fastaFilePath from which to load the proteins at start up and analyse them.
     */
    @Override
    public void setFastaFilePath(String fastaFilePath) {
        this.fastaFilePath = fastaFilePath;
    }

    /**
     * Parameter passed in on command line to set kind of input sequence
     * p: Protein
     * n: nucleic acid (DNA or RNA)
     *
     * @param sequenceType the kind of input sequence
     */
    @Override
    public void setSequenceType(String sequenceType) {
        this.sequenceType = sequenceType;
    }

    /**
     * @param outputFile if set, then the results will be output to this file in the format specified in
     *                   the field outputFormat (defaulting to XML).
     */
    @Override
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * Allows the output format to be changed from the default XML.  If no value is specified for outputFile, this
     * value will be ignored.
     *
     * @param outputFormat the output format.  If no value is specified for outputFile, this format
     *                     value will be ignored.
     */
    @Override
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * Optionally, set the analyses that should be run.
     * If not set, or set to null, all analyses will be run.
     *
     * @param analyses a comma separated list of analyses (job names) that should be run. Null for all jobs.
     */
    @Override
    public void setAnalyses(String[] analyses) {
        this.analyses = analyses;
    }

    @Override
    public void setMapToInterProEntries(boolean mapToInterPro) {
        this.mapToInterPro = mapToInterPro;
    }

    @Override
    public void setMapToGOAnnotations(boolean mapToGO) {
        this.mapToGO = mapToGO;
    }

    public void setMapToPathway(boolean mapToPathway) {
        this.mapToPathway = mapToPathway;
    }

    public void setCleanDatabase(boolean clean) {
        cleanDatabase = clean;
    }


    public void setDatabaseCleaner(CleanRunDatabase databaseCleaner) {
        this.databaseCleaner = databaseCleaner;
    }
}
