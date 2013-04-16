package uk.ac.ebi.interpro.scan.jms.master;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
import uk.ac.ebi.interpro.scan.io.FileOutputFormat;
import uk.ac.ebi.interpro.scan.jms.activemq.CleanRunDatabase;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.SubmissionWorkerRunner;
import uk.ac.ebi.interpro.scan.management.model.implementations.WriteOutputStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.StepInstanceCreatingStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.nucleotide.RunGetOrfStep;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad.FastaFileLoadStep;

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
    private boolean mapToInterPro = false;
    private boolean mapToGO = false;
    private boolean mapToPathway = false;

    protected boolean hasInVmWorker;

    private String projectId;

    private String userDir;

    @Required
    public void setHasInVmWorker(boolean hasInVmWorker) {
        this.hasInVmWorker = hasInVmWorker;
    }

    public void setSubmissionWorkerRunnerProjectId(String projectId){
        //set this as soon as the masters starts running
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner) this.workerRunner).setProjectId(projectId);
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setProjectId(projectId);
        }
    }

    public void setSubmissionWorkerRunnerUserDir(String userDir){
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner) this.workerRunner).setUserDir(userDir);
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setUserDir(userDir);
        }
    }

    protected void loadInMemoryDatabase() throws InterruptedException {
        final Thread databaseLoaderThread = new Thread(databaseCleaner);
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Loading database into memory...");
        databaseLoaderThread.start();
        // Pause while the database is loaded from the zip backup
        while (databaseCleaner.stillLoading()) {
            // Takes about 1500 ms to load the database
            Thread.sleep(200);
        }
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Database loaded.");
    }

    /**
     * If a fastaFilePath has been passed in as an argument, then StepInstances are created
     * for the fasta file loading job.  Note that this also creates all of the necessary StepInstances
     * for analyses for the loaded proteins.
     */
    protected int createFastaFileLoadStepInstance() {
        int stepInstancesCreated = 0;
        if (fastaFilePath != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Creating FASTA file load step.");
            }
            Map<String, String> params = new HashMap<String, String>();
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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Creating nucleic acid load step.");
            }
            Map<String, String> params = new HashMap<String, String>();
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
            List<String> jobNameList = new ArrayList<String>();
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
        List<String> outputFormatList = new ArrayList<String>();
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
                //specify default output formats: TSV, XML and GFF3, but not SVG and HTML
                if (extension.equalsIgnoreCase(FileOutputFormat.SVG.getFileExtension()) || extension.equalsIgnoreCase(FileOutputFormat.HTML.getFileExtension())) {
                    // SVG and HTML formats are not part of the default formats
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

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setUserDir(String userDir) {
        this.userDir = userDir;
    }
}
