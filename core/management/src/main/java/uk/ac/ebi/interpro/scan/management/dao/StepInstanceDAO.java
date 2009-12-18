package uk.ac.ebi.interpro.scan.management.dao;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepExecutionState;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pjones
 * Date: Dec 8, 2009
 * Time: 10:21:41 AM
 */
public interface StepInstanceDAO extends GenericDAO<StepInstance, String> {

    /**
     * Retrieve the StepInstances from the database for a particular Step.
     * @param step for which to retrieve StepInstance objects from the database.
     * @param optionalStates if none provided, then all StepInstances are returned.
     * If one or more provided, then limited to StepInstances in the specified state.
     * @return the List of StepInstance objects.
     */
    List<StepInstance> retrieveInstances (Step step, StepExecutionState... optionalStates);
}
