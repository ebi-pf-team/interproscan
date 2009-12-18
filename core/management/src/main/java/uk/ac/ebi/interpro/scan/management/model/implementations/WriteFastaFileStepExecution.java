package uk.ac.ebi.interpro.scan.management.model.implementations;

import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.WriteFastaFile;
import uk.ac.ebi.interpro.scan.model.Protein;
import uk.ac.ebi.interpro.scan.persistence.DAOManager;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;
import java.util.List;
import java.util.UUID;
import java.io.Serializable;

@Entity
@DiscriminatorValue("write_fasta_file")
public class WriteFastaFileStepExecution extends StepExecution<WriteFastaFileStepInstance>  implements Serializable {

    @Transient
    private final WriteFastaFile fastaFile = new WriteFastaFile();

    protected WriteFastaFileStepExecution(WriteFastaFileStepInstance stepInstance) {
        super(stepInstance);
    }

    /**
     * DO NOT USE - For JPA only.
     */
    protected WriteFastaFileStepExecution() {
    }

    @Override
    public void execute(DAOManager daoManager) {
        this.setToRun();
        try{
            WriteFastaFileStepInstance stepInstance = this.getStepInstance();
            List<Protein> proteins = daoManager.getProteinDAO().getProteinsBetweenIds(stepInstance.getBottomProtein(), stepInstance.getTopProtein());
            fastaFile.writeFastaFile(proteins, stepInstance.getFastaFilePathName());
            this.completeSuccessfully();
        } catch (Exception e) {
            this.fail();
            LOGGER.error ("Exception thrown when attempting to write out a Fasta file to path " , e);
        }
    }

    @Override
    public String toString() {
        return "WriteFastaFileStepExecution{" +
                "fastaFile=" + fastaFile +
                super.toString() +
                '}';
    }
}
