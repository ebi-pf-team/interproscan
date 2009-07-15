package uk.ac.ebi.interpro.scan.persistence.transactiontracking;

import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.model.Model;
import uk.ac.ebi.interpro.scan.model.transactiontracking.TransactionSlice;
import uk.ac.ebi.interpro.scan.persistence.ProteinDAO;

import java.util.List;

/**
 * Developed using IntelliJ IDEA.
 * User: pjones
 * Date: 10-Jul-2009
 * Time: 11:04:16
 *
 * @author Phil Jones, EMBL-EBI
 */
public class RawTransactionSliceDAOImpl extends GenericDAOImpl<TransactionSlice, Long> implements RawTransactionSliceDAO {

    private ProteinDAO proteinDAO;

    private GenericDAO<Model, Long> modelDAO;


    public RawTransactionSliceDAOImpl(){
        super(TransactionSlice.class);
    }

    @Required
    public void setProteinDAO(ProteinDAO proteinDAO) {
        this.proteinDAO = proteinDAO;
    }

    @Required
    public void setModelDAO(GenericDAO<Model, Long> modelDAO) {
        this.modelDAO = modelDAO;
    }

    /**
     * This method returns a List of TransactionSlice objects that meet the criteria specified.
     * These TransactionSlices will all have TransactionState.NOT_STARTED.
     * <p/>
     * These can then be passed on to run...
     *
     * @param criteria describing the criteria for selection.
     * @return a List containing the transactions to run.
     */
    public List<TransactionSlice> selectNewSlices(SliceSelectionCriteria criteria) {

        // Retrieve the maximum boundary for both Proteins and Models.
        Long maxProteinId = proteinDAO.getMaximumPrimaryKey();
        Long maxModelId = modelDAO.getMaximumPrimaryKey();


        return null;
    }
}
