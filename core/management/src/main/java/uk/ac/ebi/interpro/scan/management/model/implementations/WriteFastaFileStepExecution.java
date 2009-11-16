package uk.ac.ebi.interpro.scan.management.model.implementations;

import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.business.sequence.fasta.WriteFastaFile;

import java.util.UUID;
import java.io.File;

public class WriteFastaFileStepExecution extends StepExecution<WriteFastaFileStepInstance> {

    private final WriteFastaFile fastaFile = new WriteFastaFile();

    protected WriteFastaFileStepExecution(UUID id, WriteFastaFileStepInstance stepInstance) {
        super(id, stepInstance);
    }

    @Override
    public void execute() {
        this.running();
        try{
            File file = new File(this.getStepInstance().getStep().getFastaFilePathTemplate());
            // Only write the file if it has not been written before.
            if (! file.exists()){
                fastaFile.writeFastaFile(this.getStepInstance().getProteins(), this.getStepInstance().getFastaFilePathName());
            }
            this.completeSuccessfully();
        } catch (Exception e) {
            this.fail();
            LOGGER.error ("Exception thrown when attempting to write out a Fasta file to path " , e);
        }
    }
}
