package uk.ac.ebi.interpro.scan.management.model.implementations;

import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.Protein;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import java.util.UUID;
import java.util.List;
import java.io.Serializable;

@Entity
@DiscriminatorValue("write_fasta_file")
public class WriteFastaFileStepInstance extends StepInstance<WriteFastaFileStep, WriteFastaFileStepExecution> implements Serializable {

    public WriteFastaFileStepInstance(WriteFastaFileStep step, long bottomProteinId, long topProteinId) {
        super(step, bottomProteinId, topProteinId);
        this.fastaFilePathName = filterFileNameProteinBounds(
                this.getStep().getFastaFilePathTemplate(),
                bottomProteinId, topProteinId
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
