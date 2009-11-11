package uk.ac.ebi.interpro.scan.persistence.transactiontracking;

import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;
import static uk.ac.ebi.interpro.scan.persistence.transactiontracking.TransactionPriorityCriteria.LOWEST_PROTEIN_ID;

/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 10-Jul-2009
 * Time: 10:49:52
 *
 * @author Phil Jones, EMBL-EBI
 */
public class SliceSelectionCriteriaImpl implements SliceSelectionCriteria {

    private Long proteinCount;

    private Long modelCount;

    private SignatureLibraryRelease SignatureLibraryRelease;

    private TransactionPriorityCriteria transactionPriorityCriteria = LOWEST_PROTEIN_ID;

    public SliceSelectionCriteriaImpl() {
    }

    public Long getProteinCount() {
        return proteinCount;
    }

    public void setProteinCount(Long proteinCount) {
        this.proteinCount = proteinCount;
    }

    public Long getModelCount() {
        return modelCount;
    }

    public void setModelCount(Long modelCount) {
        this.modelCount = modelCount;
    }

    public SignatureLibraryRelease getSignatureLibraryRelease() {
        return SignatureLibraryRelease;
    }

    public void setSignatureLibraryRelease(SignatureLibraryRelease SignatureLibraryRelease) {
        this.SignatureLibraryRelease = SignatureLibraryRelease;
    }

    public TransactionPriorityCriteria getFirstChoiceCriteria() {
        return transactionPriorityCriteria;
    }

    public void setFirstChoiceCriteria(TransactionPriorityCriteria transactionPriorityCriteria) {
        this.transactionPriorityCriteria = transactionPriorityCriteria;
    }
}
