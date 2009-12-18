package uk.ac.ebi.interpro.scan.management.dao;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: Dec 7, 2009
 * Time: 9:34:41 PM
 */
public interface StepExecutionDAO extends GenericDAO<StepExecution, String> {

    /**
     * Accepts a StepExecution object that has been returned over the wire
     * and uses it to refresh the StepExecution with the same primary key
     * that is held in the database.
     * @param freshStepExecution being the non-persistent serialized
     * StepExecution used to update this persisted StepExecution.
     */
    void refreshStepExecution(StepExecution freshStepExecution);
}
