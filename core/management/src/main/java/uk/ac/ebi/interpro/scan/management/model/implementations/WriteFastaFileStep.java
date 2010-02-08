package uk.ac.ebi.interpro.scan.management.model.implementations;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.WriteFastaFile;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.persistence.DAOManager;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;

/**
 * This Step write Fasta files for the range of proteins requested.
 *
 * @author Phil Jones
 * @author David Binns
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class WriteFastaFileStep extends Step implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(WriteFastaFileStep.class);

    @Transient
    private final WriteFastaFile fastaFile = new WriteFastaFile();

    private String fastaFilePathTemplate;

    @Required
    public void setFastaFilePathTemplate(String fastaFilePathTemplate) {
        this.fastaFilePathTemplate = fastaFilePathTemplate;
    }

    public String getFastaFilePathTemplate() {
        return fastaFilePathTemplate;
    }

    /**
     * This method is called to execute the action that the StepExecution must perform.
     * This method should typically perform its activity in a try / catch / finally block
     * that sets the state of the step execution appropriately.
     * <p/>
     * Note that the implementation DOES have access to the protected stepInstance,
     * and from their to the protected Step, to allow it to access parameters for execution.
     *
     * @param daoManager    for DAO processes.
     * @param stepExecution record of execution
     */
    @Override
    public void execute(DAOManager daoManager, StepExecution stepExecution) {
        stepExecution.setToRun();
        try{
            String fastaFilePathName = stepExecution.getStepInstance().filterFileNameProteinBounds(
                    this.getFastaFilePathTemplate()
            );
            StepInstance stepInstance = stepExecution.getStepInstance();
            List<Protein> proteins = daoManager.getProteinDAO().getProteinsBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
            fastaFile.writeFastaFile(proteins, fastaFilePathName);
            stepExecution.completeSuccessfully();
        } catch (Exception e) {
            stepExecution.fail();
            LOGGER.error ("Exception thrown when attempting to write out a Fasta file to path " , e);
        }
    }
}

