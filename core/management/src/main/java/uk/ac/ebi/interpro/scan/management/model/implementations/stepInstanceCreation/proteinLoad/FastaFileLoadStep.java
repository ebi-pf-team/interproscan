package uk.ac.ebi.interpro.scan.management.model.implementations.stepInstanceCreation.proteinLoad;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.LoadFastaFile;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

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

    Logger LOGGER = Logger.getLogger(FastaFileLoadStep.class);

    private LoadFastaFile fastaFileLoader;

    private Resource fastaFile;

    @Required
    public void setFastaFileLoader(LoadFastaFile fastaFileLoader) {
        this.fastaFileLoader = fastaFileLoader;
    }

    @Required
    public void setFastaFile(Resource fastaFile) {
        this.fastaFile = fastaFile;
    }

    /**
     * This method is called to execute the action that the StepExecution must perform.
     * This method should typically perform its activity in a try / catch / finally block
     * that sets the state of the step execution appropriately.
     * <p/>
     * Note that the implementation DOES have access to the protected stepInstance,
     * and from their to the protected Step, to allow it to access parameters for execution.
     * <p/>
     * (For example, constructing file names based upon lower and upper protein IDs or
     * model IDs).
     * <p/>
     *
     * @param stepExecution record of execution
     */
    @Override
    public void execute(StepExecution stepExecution) {
        LOGGER.debug("Entered execute() method of FastaFileLoadStep");
        stepExecution.setToRun();
        try{
            LOGGER.debug("LoadFastaFile object: " + fastaFileLoader);
            LOGGER.debug("Resource object (fasta file): " + fastaFile);
            fastaFileLoader.loadSequences(fastaFile);
            stepExecution.completeSuccessfully();
        }
        catch (Exception e){
            stepExecution.fail();
        }
    }
}
