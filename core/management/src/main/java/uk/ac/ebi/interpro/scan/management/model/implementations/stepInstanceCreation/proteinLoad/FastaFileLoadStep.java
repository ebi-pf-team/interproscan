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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Loads a Fasta file into the database, creating new protein instance
 * where necessary and schedules new StepInstances for these new proteins.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0
 */
public class FastaFileLoadStep extends Step {

    public static final String FASTA_FILE_PATH_KEY = "FASTA_FILE_PATH";
    public static final String ANALYSIS_JOB_NAMES_KEY = "ANALYSIS_JOB_NAMES";
    public static final String COMPLETION_JOB_NAME_KEY = "COMPLETION_JOB_NAME";

    private static final Logger LOGGER = Logger.getLogger(FastaFileLoadStep.class.getName());

    private LoadFastaFile fastaFileLoader;

    @Required
    public void setFastaFileLoader(LoadFastaFile fastaFileLoader) {
        this.fastaFileLoader = fastaFileLoader;
    }

    private Jobs jobs;

    @Required
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    private StepInstanceDAO stepInstanceDAO;


    @Required
    public void setStepInstanceDAO(StepInstanceDAO stepInstanceDAO) {
        this.stepInstanceDAO = stepInstanceDAO;
    }


    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance           containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) {
        LOGGER.debug("FastaFileLoadStep.fastaFileLoader : " + fastaFileLoader);
        final String providedPath = stepInstance.getStepParameters().get(FASTA_FILE_PATH_KEY);
        final String analysisJobNames = stepInstance.getStepParameters().get(ANALYSIS_JOB_NAMES_KEY);
        final String completionJobName = stepInstance.getStepParameters().get(COMPLETION_JOB_NAME_KEY);
        LOGGER.debug("Fasta file path from step parameters; " + providedPath);
        if (providedPath != null) {
            // Try resolving the fasta file as an absolute file path
            InputStream fastaFileInputStream = null;
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
                fastaFileInputStream = FastaFileLoadStep.class.getClassLoader().getResourceAsStream(providedPath);
            }
            if (fastaFileInputStream == null) {
                throw new IllegalArgumentException("Cannot find the fasta file located at " + providedPath);
            }


            Jobs analysisJobs = analysisJobNames == null
                    ? jobs.getAnalysisJobs()
                    : jobs.subset(StringUtils.commaDelimitedListToStringArray(analysisJobNames));
            Job completionJob = jobs.getJobById(completionJobName);

            StepCreationProteinLoadListener proteinLoaderListener =
                    new StepCreationProteinLoadListener(analysisJobs, completionJob, stepInstance.getStepParameters());
            proteinLoaderListener.setStepInstanceDAO(stepInstanceDAO);

            fastaFileLoader.loadSequences(fastaFileInputStream, proteinLoaderListener);
        }
    }
}
