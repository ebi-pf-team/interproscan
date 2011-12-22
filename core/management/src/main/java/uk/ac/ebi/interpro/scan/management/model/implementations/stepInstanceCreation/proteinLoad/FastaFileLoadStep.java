package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.LoadFastaFile;
import uk.ac.ebi.interpro.scan.management.dao.StepInstanceDAO;
import uk.ac.ebi.interpro.scan.management.model.Job;
import uk.ac.ebi.interpro.scan.management.model.Jobs;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.StepInstanceCreatingStep;

import java.io.*;

/**
 * Loads a Fasta file into the database, creating new protein instance
 * where necessary and schedules new StepInstances for these new proteins.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0
 */
public class FastaFileLoadStep extends Step implements StepInstanceCreatingStep {

    public static final String FASTA_FILE_PATH_KEY = "FASTA_FILE_PATH";

    private static final Logger LOGGER = Logger.getLogger(FastaFileLoadStep.class.getName());

    private LoadFastaFile fastaFileLoader;

    /**
     * If this is set, this file name will override that passed in by the user as
     * a parameteter.  This is for use in the genomics sequence analysis pipeline,
     * where this Step loads a generated protein sequence file, rather than the
     * nucleic acid sequence file passed in by the user.
     */
    private String overridingFastaFileName;

    protected Jobs jobs;
    protected StepInstanceDAO stepInstanceDAO;

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    @Required
    public void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO) {
        this.stepInstanceDAO = stepInstanceDAO;
    }

    @Required
    public void setFastaFileLoader(LoadFastaFile fastaFileLoader) {
        this.fastaFileLoader = fastaFileLoader;
    }

    public void setOverridingFastaFileName(String overridingFastaFileName) {
        this.overridingFastaFileName = overridingFastaFileName;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("FastaFileLoadStep.fastaFileLoader : " + fastaFileLoader);
        }
        String providedPath;
        if (overridingFastaFileName == null || overridingFastaFileName.isEmpty()) {
            providedPath = stepInstance.getParameters().get(FASTA_FILE_PATH_KEY);
        } else {
            providedPath = stepInstance.buildFullyQualifiedFilePath(temporaryFileDirectory, overridingFastaFileName);
        }
        final String analysisJobNames = stepInstance.getParameters().get(ANALYSIS_JOB_NAMES_KEY);
        final String completionJobName = stepInstance.getParameters().get(COMPLETION_JOB_NAME_KEY);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fasta file path to be loaded: " + providedPath);
        }
        if (providedPath != null) {
            // Try resolving the fasta file as an absolute file path
            InputStream fastaFileInputStream = null;
            try {
                File file = new File(providedPath);
                if (file.exists()) {
                    if (file.canRead()) {
                        try {
                            fastaFileInputStream = new FileInputStream(file);
                        } catch (FileNotFoundException e) {
                            throw new IllegalStateException("FileNotFoundException thrown when attempting to load a fasta file located at " + file.getAbsolutePath(), e);
                        }
                    } else {
                        throw new IllegalArgumentException("The fasta file " + providedPath + " is visible but cannot be read.  Please check file permissions.");
                    }
                } else {
                    // Absolute file path did not resolve, so try using the class loader.
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("The file " + providedPath + " does not exist.  Attempting to access this file using the ClassLoader.");
                    }
                    fastaFileInputStream = FastaFileLoadStep.class.getClassLoader().getResourceAsStream(providedPath);
                }
                if (fastaFileInputStream == null) {
                    throw new IllegalArgumentException("Cannot find the fasta file located at " + providedPath);
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("FastaFileLoaderStep.jobs is null? " + jobs == null);
                }
                Jobs analysisJobs;
                if (analysisJobNames == null) {
                    analysisJobs = jobs.getAnalysisJobs();
                } else {
                    analysisJobs = jobs.subset(StringUtils.commaDelimitedListToStringArray(analysisJobNames));
                }
                Job completionJob = jobs.getJobById(completionJobName);

                StepCreationSequenceLoadListener sequenceLoadListener =
                        new StepCreationSequenceLoadListener(analysisJobs, completionJob, stepInstance.getParameters());
                sequenceLoadListener.setStepInstanceDAO(stepInstanceDAO);

                fastaFileLoader.loadSequences(fastaFileInputStream, sequenceLoadListener);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Finished loading sequences and creating step instances.");
                }
            } finally {
                if (fastaFileInputStream != null) {
                    try {
                        fastaFileInputStream.close();
                    } catch (IOException e) {
                        LOGGER.warn("Unable to cleanly close the InputStream from FASTA file " + providedPath);
                    }
                }
            }
        }
    }
}
