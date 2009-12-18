package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3;

import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Required;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Column;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 23-Nov-2009
 * Time: 15:33:55
 */

@Entity
@DiscriminatorValue("run_hmmer3")
public class RunHmmer3StepInstance extends StepInstance<RunHmmer3Step, RunHmmer3StepExecution> implements Serializable {

    @Column (nullable=true)
    private String hmmerOutputFileName;
    @Column (nullable=true)
    private String fastaFilePathName;

    /**
     * DO NOT USE - For JPA only.
     */
    protected RunHmmer3StepInstance() {
    }

    public String getHmmerOutputFileName() {
        return hmmerOutputFileName;
    }

    public String getFastaFilePathName() {
        return fastaFilePathName;
    }

    public RunHmmer3StepInstance(RunHmmer3Step step, long bottomProteinId, long topProteinId) {
        super(step, bottomProteinId, topProteinId);
        this.fastaFilePathName = this.filterFileNameProteinBounds(this.getStep().getFastaFilePathNameTemplate(), bottomProteinId, topProteinId);
        this.hmmerOutputFileName = this.filterFileNameProteinBounds(this.getStep().getHmmerOutputFilePathTemplate(), bottomProteinId, topProteinId);
    }

    @Override
    public RunHmmer3StepExecution createStepExecution() {
        return new RunHmmer3StepExecution(this);
    }
}
