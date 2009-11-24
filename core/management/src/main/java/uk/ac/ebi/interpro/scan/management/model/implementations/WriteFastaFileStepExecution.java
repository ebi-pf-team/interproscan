package uk.ac.ebi.interpro.scan.management.model.implementations;

import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.WriteFastaFile;

import java.util.UUID;
import java.io.Serializable;

public class WriteFastaFileStepExecution extends StepExecution<WriteFastaFileStepInstance>  implements Serializable {

    private final WriteFastaFile fastaFile = new WriteFastaFile();

    protected WriteFastaFileStepExecution(UUID id, WriteFastaFileStepInstance stepInstance) {
        super(id, stepInstance);
    }

    @Override
    public void execute() {
        this.setToRun();
        try{
            fastaFile.writeFastaFile(this.getStepInstance().getProteins(), this.getStepInstance().getFastaFilePathName());
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
                "protein count=" + this.getStepInstance().getProteins().size() +
                '}';
    }
}
