package uk.ac.ebi.interpro.scan.management.dao;

import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAO;
import uk.ac.ebi.interpro.scan.management.model.*;

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
     * @return the List of StepInstance objects.
     */
    List<StepInstance> retrieveUnfinishedStepInstances(Step step);

    /**
     * Returns true if the SerialGroup passed in as argument
     * does not currently have a running instance. (submitted but not failed or completed)
     * instance.
     * @param stepInstance which is in the group to test for.
     * @param jobs being the List of defined jobs.
     * @return true if the SerialGroup passed in as argument
     * does not currently have a running instance.
     */
    boolean serialGroupCanRun(StepInstance stepInstance, Jobs jobs);

    /**
     * Returns true if there are steps left to run
     * @return true if there are steps left to run
     */
    @Deprecated
    boolean futureStepsAvailable();
}
