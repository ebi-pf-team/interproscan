package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.PfamA;

import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: 23-Nov-2009
 * Time: 15:40:59
 */
@Entity
@DiscriminatorValue("pfam_A_postproc")
public class Pfam_A_PostProcessingStepInstance extends StepInstance<Pfam_A_PostProcessingStep, Pfam_A_PostProcessingStepExecution> implements Serializable {
    /**
     * DO NOT USE - For JPA only.
     */
    protected Pfam_A_PostProcessingStepInstance() {
    }

    /**
     * Constructor that accepts a UUID as a unique identifier
     * for the StepInstance and a reference to the Step
     * to allow access to Step configuration.
     *
     * @param step the Step template used to create this StepInstance.
     * @param bottomProteinId being the lowest inclusive protein ID to include
     * @param topProteinId being the highest inclusive protein ID to include.
     */
    public Pfam_A_PostProcessingStepInstance(Pfam_A_PostProcessingStep step, long bottomProteinId, long topProteinId) {
        super(step, bottomProteinId, topProteinId);
    }

    @Override
    public Pfam_A_PostProcessingStepExecution createStepExecution() {
        return new Pfam_A_PostProcessingStepExecution(this);
    }

}