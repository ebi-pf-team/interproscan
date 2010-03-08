package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.LoadFastaFile;
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

    private static final Logger LOGGER = Logger.getLogger(FastaFileLoadStep.class);

    private LoadFastaFile fastaFileLoader;

    @Required
    public void setFastaFileLoader(LoadFastaFile fastaFileLoader) {
        this.fastaFileLoader = fastaFileLoader;
    }

    /**
     * This method is called to execute the action that the StepInstance must perform.
     *
     * @param stepInstance containing the parameters for executing.
     * @param temporaryFileDirectory
     */
    @Override
    public void execute(StepInstance stepInstance, String temporaryFileDirectory) throws FileNotFoundException {
        LOGGER.debug("Entered execute() method of FastaFileLoadStep");
        LOGGER.debug("LoadFastaFile object: " + fastaFileLoader);
        final String providedPath = stepInstance.getStepParameters().get(FASTA_FILE_PATH_KEY);
        LOGGER.debug("Fasta file path from step parameters; " + providedPath);

        // Try resolving the fasta file as an absolute file path
        InputStream fastaFileInputStream;
        File file = new File (providedPath);
        if (file.exists()){
            if (file.canRead()) {
                fastaFileInputStream = new FileInputStream(file);
            }
            else {
                throw new IllegalArgumentException ("The fasta file " + providedPath + " is visible but cannot be read.  Please check file permissions.");
            }
        }
        else {
            // Absolute file path did not resolve, so try using the class loader.
            fastaFileInputStream = FastaFileLoadStep.class.getClassLoader().getResourceAsStream(providedPath);
        }
        if (fastaFileInputStream == null){
            throw new IllegalArgumentException ("Cannot find the fasta file located at " + providedPath);
        }

        fastaFileLoader.loadSequences(fastaFileInputStream);
    }
}
