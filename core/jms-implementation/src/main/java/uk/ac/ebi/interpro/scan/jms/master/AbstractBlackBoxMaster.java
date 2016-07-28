package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
import uk.ac.ebi.interpro.scan.io.FileOutputFormat;
import uk.ac.ebi.interpro.scan.jms.activemq.CleanRunDatabase;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteOutputStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.StepInstanceCreatingStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.nucleotide.RunGetOrfStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad.FastaFileLoadStep;
import uk.ac.ebi.interpro.scan.util.Utilities;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * @author: Phil Jones, Gift Nuka
 * Date: 26/07/12
 */
public abstract class AbstractBlackBoxMaster extends AbstractMaster implements BlackBoxMaster {

    private static final Logger LOGGER = Logger.getLogger(AbstractBlackBoxMaster.class.getName());


    protected String fastaFilePath;
    protected String outputBaseFilename;
    /* Default value, if no output format is specified */
    private String[] outputFormats;
    /**
     * Specifies the type of the I5 input sequences.
     * <p/>
     * p: Protein (DEFAULT)
     * n: nucleic acid (DNA or RNA)
     */
    protected String sequenceType = "p";
    /**
     * Minimum nucleotide size of ORF to report (Any integer value). Default value is 50.
     */
    private String minSize;
    protected String explicitFileName;
    private boolean useMatchLookupService = true;
    /**
     * This boolean allows configuration of whether or not the Master closes down when there are no more
     * runnable StepExecutions available.
     */
    protected CleanRunDatabase databaseCleaner;
    private boolean excludeSites = false;
    private boolean mapToInterPro = false;
    private boolean mapToGO = false;
    private boolean mapToPathway = false;

    protected boolean hasInVmWorker;

    private int concurrentInVmWorkerCount;

    private int maxConcurrentInVmWorkerCount;

    private int maxConcurrentInVmWorkerCountForWorkers;

    protected String userDir;

    protected boolean verboseLog;

    protected int verboseLogLevel;


    private final long startUpTime = System.currentTimeMillis();
    private long maximumLifeMillis = Long.MAX_VALUE;

    protected int gridCheckInterval = 60; //seconds

    protected static final int LOW_PRIORITY = 4;
    protected static final int HIGH_PRIORITY = 6;
    protected static final int HIGHER_PRIORITY = 8;
    protected static final int HIGHEST_PRIORITY = 9;

    @Required
    public void setHasInVmWorker(boolean hasInVmWorker) {
        this.hasInVmWorker = hasInVmWorker;
    }


    protected void loadInMemoryDatabase() throws InterruptedException {
        final Thread databaseLoaderThread = new Thread(databaseCleaner);
        Long timeStarted = System.currentTimeMillis();
        LOGGER.debug("Loading database into memory...");
        databaseLoaderThread.start();
        // Pause while the database is loaded from the zip backup
        while (databaseCleaner.stillLoading()) {
            // Takes about 1500 ms to load the database
            Thread.sleep(200);
        }
        Long timeSpentLoading = System.currentTimeMillis() - timeStarted;
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Database loaded in " + timeSpentLoading + " ms.");
    }

    /**
     * If a fastaFilePath has been passed in as an argument, then StepInstances are created
     * for the fasta file loading job.  Note that this also creates all of the necessary StepInstances
     * for analyses for the loaded proteins.
     */
    protected int createFastaFileLoadStepInstance() {
        int stepInstancesCreated = 0;
        if (fastaFilePath != null) {
            LOGGER.debug("Creating FASTA file load step.");
            Map<String, String> params = new HashMap<>();
            params.put(FastaFileLoadStep.FASTA_FILE_PATH_KEY, fastaFilePath);
            createBlackBoxParams(params);
            stepInstancesCreated = createStepInstancesForJob("jobLoadFromFasta", params);
            LOGGER.info("Fasta file load step instance has been created.");
        }
        return stepInstancesCreated;
    }

    protected int createStepInstances() {
        return ("n".equalsIgnoreCase(this.sequenceType))
                ? createNucleicAcidLoadStepInstance()
                : createFastaFileLoadStepInstance();
    }


    protected int createNucleicAcidLoadStepInstance() {
        int stepInstancesCreated = 0;
        if (fastaFilePath != null) {
            LOGGER.debug("Creating nucleic acid load step.");
            Map<String, String> params = new HashMap<>();
            params.put(RunGetOrfStep.SEQUENCE_FILE_PATH_KEY, fastaFilePath);
            params.put(FastaFileLoadStep.FASTA_FILE_PATH_KEY, fastaFilePath);
            createBlackBoxParams(params);
            stepInstancesCreated = createStepInstancesForJob("jobLoadNucleicAcidSequence", params);
        } else {
            LOGGER.error("No nucleic acid sequence file path has been provided to load.");
        }
        return stepInstancesCreated;
    }

    private void createBlackBoxParams(final Map<String, String> params) {
        // Analyses as a comma separated list
        if (analyses != null && analyses.length > 0) {
            List<String> jobNameList = new ArrayList<>();
            Collections.addAll(jobNameList, analyses);
            params.put(StepInstanceCreatingStep.ANALYSIS_JOB_NAMES_KEY, StringUtils.collectionToCommaDelimitedString(jobNameList));
        }

        processOutputFormats(params, this.outputFormats);
        params.put(StepInstanceCreatingStep.COMPLETION_JOB_NAME_KEY, "jobWriteOutput");

        String outputBaseName;
        if (outputBaseFilename == null || outputBaseFilename.isEmpty()) {
            // If no output base file name provided just use the same name as the input fasta file (extension will be added later)
            outputBaseName = fastaFilePath;
        } else {
            // Use the output base file name provided (extension will be added later)
            outputBaseName = outputBaseFilename;
        }
        if (explicitFileName == null) {
            params.put(WriteOutputStep.OUTPUT_FILE_PATH_KEY, outputBaseName);
        } else {
            params.put(WriteOutputStep.OUTPUT_EXPLICIT_FILE_PATH_KEY, explicitFileName);
        }

        params.put(WriteOutputStep.EXCLUDE_SITES, Boolean.toString(excludeSites));
        params.put(WriteOutputStep.MAP_TO_INTERPRO_ENTRIES, Boolean.toString(mapToInterPro));
        params.put(WriteOutputStep.MAP_TO_GO, Boolean.toString(mapToGO));
        params.put(StepInstanceCreatingStep.USE_MATCH_LOOKUP_SERVICE, Boolean.toString(useMatchLookupService));
        params.put(WriteOutputStep.MAP_TO_PATHWAY, Boolean.toString(mapToPathway));
        params.put(WriteOutputStep.SEQUENCE_TYPE, this.sequenceType);
        params.put(RunGetOrfStep.MIN_NUCLEOTIDE_SIZE, this.minSize);
    }

    /**
     * Outputs formats as a comma separated list.
     *
     * @param params
     */
    public void processOutputFormats(final Map<String, String> params, final String[] outputFormats) {
        List<String> outputFormatList = new ArrayList<>();
        if (outputFormats != null && outputFormats.length > 0) {
            Collections.addAll(outputFormatList, outputFormats);
        }
        // It seems that no valid output formats were specified, so just default to all
        else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("No valid output formats specified, therefore use the default (all for sequence type " + this.sequenceType + ")");
            }
            for (FileOutputFormat outputFormat : FileOutputFormat.values()) {
                String extension = outputFormat.getFileExtension();
                //specify default output formats: TSV, XML and GFF3, but not SVG, HTML, GFF3 partial or XML slim
                if (extension.equalsIgnoreCase(FileOutputFormat.SVG.getFileExtension()) ||
                        extension.equalsIgnoreCase(FileOutputFormat.HTML.getFileExtension()) ||
                        extension.equalsIgnoreCase(FileOutputFormat.RAW.getFileExtension()) ||
                        extension.equalsIgnoreCase(FileOutputFormat.GFF3_PARTIAL.getFileExtension()) ||
                        extension.equalsIgnoreCase(FileOutputFormat.XML_SLIM.getFileExtension())) {
                    // SVG, HTML and RAW formats are not part of the default formats
                    continue;
                }
                outputFormatList.add(extension);
            }
        }
        params.put(WriteOutputStep.OUTPUT_FILE_FORMATS, StringUtils.collectionToCommaDelimitedString(outputFormatList));
    }


    /**
     * Called by quartz to load proteins from UniParc.
     */
    public void createProteinLoadJob() {
        createStepInstancesForJob("jobLoadFromUniParc", null);
    }

    /**
     *  return the minimum steps expected to run
     *
     * @return
     */
    public int getMinimumStepsExpected(){
        int analysesCount = 1;
        if (analyses != null) {
            analysesCount = analyses.length;
        }else{
            analysesCount = jobs.getActiveAnalysisJobs().getJobIdList().size();
        }
        Utilities.verboseLog("analysesCount :  " + analysesCount);
        int minimumStepForEachAnalysis = 0;
        int minimumSteps = 2;
        if (! isUseMatchLookupService()){
            minimumStepForEachAnalysis = 4; //writefasta, runbinary, deletefasta, parseoutput
        }

        minimumSteps = minimumSteps + (analysesCount * minimumStepForEachAnalysis);

        return minimumSteps;
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
     * Parameter passed in on command line to set minimum nucleotide size of ORF to report (EMBOSS getorf parameter).
     * Default size for InterProScan is 50 nucleic acids (which overwrites the getorf default value of 30).<br>
     * This option is also configurable within the interproscan.properties file, but will be overwritten by the command value if specified.
     *
     * @param minSize Minimum nucleotide size of ORF to report (EMBOSS getorf parameter).
     */
    public void setMinSize(String minSize) {
        this.minSize = minSize;
    }

    @Override
    public void setExplicitOutputFilename(String explicitFileName) {
        this.explicitFileName = explicitFileName;
    }

    public boolean isUseMatchLookupService() {
        return useMatchLookupService;
    }

    /**
     * Called to turn off the use of the precalculated match lookup service on this run.
     */
    public void disablePrecalc() {
        this.useMatchLookupService = false;
    }

    /**
     * @param outputBaseFilename If set, then the results will be output to this file in the format specified in
     *                           the field outputFormats. The application will apply the appropriate file extension automatically.
     */
    @Override
    public void setOutputBaseFilename(String outputBaseFilename) {
        this.outputBaseFilename = outputBaseFilename;
    }

    /**
     * Allows the output format to be changed from the default (all available formats for that sequence type).
     *
     * @param outputFormats The comma separated list of output formats.
     */
    @Override
    public void setOutputFormats(String[] outputFormats) {
        this.outputFormats = outputFormats;
    }

    @Override
    public void setExcludeSites(boolean excludeSites) {
        this.excludeSites = excludeSites;
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

    public void setDatabaseCleaner(CleanRunDatabase databaseCleaner) {
        this.databaseCleaner = databaseCleaner;
    }

    public void setUserDir(String userDir) {
        this.userDir = userDir;
    }

    public int getConcurrentInVmWorkerCount() {
        return concurrentInVmWorkerCount;
    }

    public void setConcurrentInVmWorkerCount(int concurrentInVmWorkerCount) {
        this.concurrentInVmWorkerCount = concurrentInVmWorkerCount;
    }

    public int getMaxConcurrentInVmWorkerCount() {
        return maxConcurrentInVmWorkerCount;
    }

    public void setMaxConcurrentInVmWorkerCount(int maxConcurrentInVmWorkerCount) {
        this.maxConcurrentInVmWorkerCount = maxConcurrentInVmWorkerCount;
    }

    public int getMaxConcurrentInVmWorkerCountForWorkers() {
        return maxConcurrentInVmWorkerCountForWorkers;
    }

    public void setMaxConcurrentInVmWorkerCountForWorkers(int maxConcurrentInVmWorkerCountForWorkers) {
        this.maxConcurrentInVmWorkerCountForWorkers = maxConcurrentInVmWorkerCountForWorkers;
    }

    public void setVerboseLog(boolean verboseLog) {
        this.verboseLog = verboseLog;
    }

    public void setVerboseLogLevel(int verboseLogLevel) {
        this.verboseLogLevel = verboseLogLevel;
    }

    public long getMaximumLifeMillis() {
        return maximumLifeMillis;
    }

    public void setMaximumLifeMillis(long maximumLifeMillis) {
        this.maximumLifeMillis = maximumLifeMillis;
    }

    public long getStartUpTime() {
        return startUpTime;
    }

    public long getMasterLifeRemaining(){
        return System.currentTimeMillis() - startUpTime;
    }

    public int getGridCheckInterval() {
        return gridCheckInterval;
    }

    public void setGridCheckInterval(int gridCheckInterval) {
        this.gridCheckInterval = gridCheckInterval;
    }
}
