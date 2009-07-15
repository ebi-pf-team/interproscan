package uk.ac.ebi.interpro.scan.persistence;

import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.transactiontracking.RawTransactionSlice;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;

import java.util.List;

/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 10-Jul-2009
 * Time: 11:28:24
 *
 * @author Phil Jones, EMBL-EBI
 */
public class ModelDAOImpl extends GenericDAOImpl<Model, Long> implements ModelDAO{

    /**
     * Default constructor that initialises the GenericDAOImpl to handle Model objects.
     */
    public ModelDAOImpl(){
        super (Model.class);
    }

    /**
     * Returns a List containing all of the Models that correspond to the
     * RawTransactionSlice passed in as argument.
     *
     * @param slice which contains a range of Models to include in the List.
     * @return a List containing all of the Models that correspond to the
     *         RawTransactionSlice passed in as argument.
     */
    public List<Model> getModelsForTransactionSlice(RawTransactionSlice slice) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
