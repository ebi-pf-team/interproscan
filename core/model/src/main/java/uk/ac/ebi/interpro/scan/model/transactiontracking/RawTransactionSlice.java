package uk.ac.ebi.interpro.scan.model.transactiontracking;

import uk.ac.ebi.interpro.scan.model.Model;

/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 09-Jul-2009
 * Time: 17:28:02
 *
 * @author Phil Jones, EMBL-EBI
 */
public interface RawTransactionSlice extends TransactionSlice{

    /**
     * Returns the Model to which this Slice applies.
     * @return the Model to which this Slice applies.
     */
    public Model getModel();
}
