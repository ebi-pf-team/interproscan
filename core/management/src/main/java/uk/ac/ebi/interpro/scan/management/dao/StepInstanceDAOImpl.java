package uk.ac.ebi.interpro.scan.management.dao;

import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.management.model.Step;
import uk.ac.ebi.interpro.scan.management.model.StepExecutionState;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DAO for StepInstance objects.  Used to retrieve
 * StepInstances that may be run.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class StepInstanceDAOImpl extends GenericDAOImpl<StepInstance, String> implements StepInstanceDAO{
    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public StepInstanceDAOImpl() {
        super(StepInstance.class);
    }

    /**
     * Retrieve the StepInstances from the database for a particular Step
     * that <b>MAY BE</b> candidates to be run.
     *
     * <b>NOTE: This returns all StepInstance objects that have not been
     * successfully run.  It does NOT filter out those that are
     * currently running - the calling code MUST call StepInstance.canBeSubmitted(Jobs jobs)
     * before creating a new StepExecution for the StepInstance.<b>
     *
     * @param step for which StepInstance objects should be obtained from the database.
     * @return a List of StepInstance objects that have not successfully completed yet.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<StepInstance> retrieveUnfinishedStepInstances(Step step) {
        Query query = entityManager.createQuery(
                        "select distinct i " +
                        "from StepInstance i " +
                        "where i.stepId = :stepId " +
                        "and i not in (" +
                                "select j " +
                                "from StepInstance j " +
                                "inner join j.executions e " +
                                "where e.state = :successful " +
                                "and j.stepId = :stepId) order by i.id desc");

        query.setParameter("stepId", step.getId());
        query.setParameter("successful", StepExecutionState.STEP_EXECUTION_SUCCESSFUL);
        return query.getResultList();
    }
}
