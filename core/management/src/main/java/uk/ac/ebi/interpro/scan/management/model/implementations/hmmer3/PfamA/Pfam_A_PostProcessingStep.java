package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.PfamA;

import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.PfamHMMER3PostProcessing;
import uk.ac.ebi.interpro.scan.model.raw.PfamHmmer3RawMatch;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

import org.springframework.beans.factory.annotation.Required;

/**
 * Created by IntelliJ IDEA.
 * User: mumdad
 * Date: 15-Nov-2009
 * Time: 14:48:18
 */
public class Pfam_A_PostProcessingStep extends Step<Pfam_A_PostProcessingStepInstance, Pfam_A_PostProcessingStepExecution> implements Serializable {

    private PfamHMMER3PostProcessing postProcessor;

    public PfamHMMER3PostProcessing getPostProcessor() {
        return postProcessor;
    }

    @Required
    public void setPostProcessor(PfamHMMER3PostProcessing postProcessor) {
        this.postProcessor = postProcessor;
    }
}