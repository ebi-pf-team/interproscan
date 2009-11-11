package uk.ac.ebi.interpro.scan.persistence.transactiontracking;

import uk.ac.ebi.interpro.scan.model.SignatureLibraryRelease;

/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 10-Jul-2009
 * Time: 11:07:56
 *
 * @author Phil Jones, EMBL-EBI
 */
public interface SliceSelectionCriteria {

    Long getProteinCount();

    void setProteinCount(Long proteinCount);

    Long getModelCount();

    void setModelCount(Long modelCount);

    SignatureLibraryRelease getSignatureLibraryRelease();

    void setSignatureLibraryRelease(SignatureLibraryRelease SignatureLibraryRelease);

    TransactionPriorityCriteria getFirstChoiceCriteria();

    void setFirstChoiceCriteria(TransactionPriorityCriteria transactionPriorityCriteria);
}
