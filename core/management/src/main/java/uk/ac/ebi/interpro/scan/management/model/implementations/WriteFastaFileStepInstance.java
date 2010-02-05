package uk.ac.ebi.interpro.scan.management.model.implementations;

import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import java.io.Serializable;

@Entity
@DiscriminatorValue("write_fasta_file")
public class WriteFastaFileStepInstance extends StepInstance<WriteFastaFileStep, WriteFastaFileStepExecution> implements Serializable {

    public WriteFastaFileStepInstance(WriteFastaFileStep step, long bottomProteinId, long topProteinId, Long bottomModelId, Long topModelId) {
        super(step, bottomProteinId, topProteinId, bottomModelId, topModelId);
        this.fastaFilePathName = filterFileNameProteinBounds(
                this.getStep().getFastaFilePathTemplate()
        );
    }

    @Column (nullable=true)
    private String fastaFilePathName;

    /**
     * DO NOT USE!  Only for use by JPA.
     */
    protected WriteFastaFileStepInstance() {
    }

    public String getFastaFilePathName() {
        return fastaFilePathName;
    }

    @Override
    public WriteFastaFileStepExecution createStepExecution() {
        return new WriteFastaFileStepExecution(this);
    }
}
