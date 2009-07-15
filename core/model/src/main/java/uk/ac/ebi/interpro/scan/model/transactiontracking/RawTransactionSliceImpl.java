package uk.ac.ebi.interpro.scan.model.transactiontracking;

import uk.ac.ebi.interpro.scan.model.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 09-Jul-2009
 * Time: 15:36:16
 *
 * @author Phil Jones, EMBL-EBI
 */
@Entity
public class RawTransactionSliceImpl extends AbstractTransactionSlice implements RawTransactionSlice{

    /**
     * public constructor that takes the lowest and highest primary key values for the proteins
     * in the slice as arguments.
     * @param bottom the lowest primary key values for the proteins
     * in the slice
     * @param top the highest primary key values for the proteins
     * in the slice
     */
    public RawTransactionSliceImpl(long bottom, long top){
        super(bottom, top);
    }

    protected RawTransactionSliceImpl(){
        super ();
    }

    @ManyToOne (optional = false)
    private Model model;

    /**
     * Returns the Model to which this Slice applies.
     * @return the Model to which this Slice applies.
     */
    public Model getModel(){
        return model;
    }
}
