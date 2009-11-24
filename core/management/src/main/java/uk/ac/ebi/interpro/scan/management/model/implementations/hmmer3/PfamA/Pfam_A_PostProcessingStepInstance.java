package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.PfamA;

import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

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
public class Pfam_A_PostProcessingStepInstance extends StepInstance<Pfam_A_PostProcessingStep, Pfam_A_PostProcessingStepExecution> implements Serializable {

    Map<String, List<PfamHmmer3RawMatch>> proteinAcToRawMatchMap;

    public void setProteinAcToRawMatchMap(Map<String, List<PfamHmmer3RawMatch>> proteinAcToRawMatchMap) {
        this.proteinAcToRawMatchMap = proteinAcToRawMatchMap;
    }

    public Map<String, List<PfamHmmer3RawMatch>> getProteinAcToRawMatchMap() {
        return proteinAcToRawMatchMap;
    }

    /**
     * Constructor that accepts a UUID as a unique identifier
     * for the StepInstance and a reference to the Step
     * to allow access to Step configuration.
     *
     * @param id   being a unique ID (UUID) object.
     * @param step the Step template used to create this StepInstance.
     */
    public Pfam_A_PostProcessingStepInstance(UUID id, Pfam_A_PostProcessingStep step, long bottomProteinId, long topProteinId) {
        super(id, step, bottomProteinId, topProteinId);
    }

    @Override
    public Pfam_A_PostProcessingStepExecution createStepExecution() {
        return new Pfam_A_PostProcessingStepExecution(UUID.randomUUID(), this);
    }

}