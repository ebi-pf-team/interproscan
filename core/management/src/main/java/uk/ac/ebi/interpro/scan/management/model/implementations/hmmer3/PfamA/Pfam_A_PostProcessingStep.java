package uk.ac.ebi.interpro.scan.management.model.implementations.hmmer3.PfamA;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.business.postprocessing.pfam_A.PfamHMMER3PostProcessing;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.model.SignatureLibrary;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: mumdad
 * Date: 15-Nov-2009
 * Time: 14:48:18
 */
public class Pfam_A_PostProcessingStep extends Step<Pfam_A_PostProcessingStepInstance, Pfam_A_PostProcessingStepExecution> implements Serializable {

    private PfamHMMER3PostProcessing postProcessor;

    private SignatureLibrary signatureLibrary;

    private String signatureLibraryRelease;

    @Required
    public void setSignatureLibrary(SignatureLibrary signatureLibrary) {
        this.signatureLibrary = signatureLibrary;
    }

    @Required
    public void setSignatureLibraryRelease(String signatureLibraryRelease) {
        this.signatureLibraryRelease = signatureLibraryRelease;
    }

    public PfamHMMER3PostProcessing getPostProcessor() {
        return postProcessor;
    }

    @Required
    public void setPostProcessor(PfamHMMER3PostProcessing postProcessor) {
        this.postProcessor = postProcessor;
    }

    public SignatureLibrary getSignatureLibrary() {
        return signatureLibrary;
    }

    public String getSignatureLibraryRelease() {
        return signatureLibraryRelease;
    }
}