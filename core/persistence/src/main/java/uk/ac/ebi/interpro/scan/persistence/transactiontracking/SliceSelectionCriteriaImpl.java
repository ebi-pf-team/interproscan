package uk.ac.ebi.interpro.scan.persistence.transactiontracking;

import uk.ac.ebi.interpro.scan.model.SignatureDatabaseRelease;
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

    private SignatureDatabaseRelease signatureDatabaseRelease;

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

    public SignatureDatabaseRelease getSignatureDatabaseRelease() {
        return signatureDatabaseRelease;
    }

    public void setSignatureDatabaseRelease(SignatureDatabaseRelease signatureDatabaseRelease) {
        this.signatureDatabaseRelease = signatureDatabaseRelease;
    }

    public TransactionPriorityCriteria getFirstChoiceCriteria() {
        return transactionPriorityCriteria;
    }

    public void setFirstChoiceCriteria(TransactionPriorityCriteria transactionPriorityCriteria) {
        this.transactionPriorityCriteria = transactionPriorityCriteria;
    }
}
