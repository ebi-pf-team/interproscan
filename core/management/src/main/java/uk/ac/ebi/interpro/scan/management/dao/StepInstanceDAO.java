package uk.ac.ebi.interpro.scan.management.dao;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepExecutionState;

import java.util.List;

/**
 * Interface for DAO for StepInstance objects.  Used to retrieve
 * StepInstances that may be run.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
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
