package uk.ac.ebi.interpro.scan.management.dao;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.interpro.scan.genericjpadao.GenericDAOImpl;
import uk.ac.ebi.interpro.scan.management.model.StepExecution;

/**
 * Allows StepExecutions to be refreshed in the database, following execution.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class StepExecutionDAOImpl extends GenericDAOImpl<StepExecution, String> implements StepExecutionDAO {

    private static final Logger LOGGER = Logger.getLogger(StepExecutionDAOImpl.class.getName());

    /**
     * Sets the class of the model that the DOA instance handles.
     * Note that this has been set up to use constructor injection
     * because it makes it easy to sub-class GenericDAOImpl in a robust
     * manner.
     * <p/>
     * Model class specific sub-classes should define a no-argument constructor
     * that calls this constructor with the appropriate class.
     */
    public StepExecutionDAOImpl() {
        super(StepExecution.class);
    }

    /**
     * Accepts a StepExecution object that has been returned over the wire
     * and uses it to refresh the StepExecution with the same primary key
     * that is held in the database.
     *
     * @param freshStepExecution being the non-persistent serialized
     *                           StepExecution used to update this persisted StepExecution.
     */
    @Transactional
    public void refreshStepExecution(StepExecution freshStepExecution) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Refreshing StepExecution with ID " + freshStepExecution.getId());
        }
        // Retrieve dirty step execution from the database.
        StepExecution dirtyStepExec = null;

        // It's quite possible for the worker to reply so quickly (for a simple task like file deletion)
        // that this refreshStepExecution method is called before the MasterMessageSender has even had a
        // chance to commit the StepExecution to the database (note it is running in a separate thread.)
        // This loop makes sure that the StepExecution is committed to the database, before refreshing it.
        while (dirtyStepExec == null) {
            dirtyStepExec = entityManager.find(StepExecution.class, freshStepExecution.getId());
            if (dirtyStepExec == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Waiting for StepExecution ID " + freshStepExecution.getId() + " to be committed prior to refreshing it.");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new IllegalStateException("InterruptedException thrown when waiting for StepExecution to be refreshed.");
                }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Retrieved Dirty StepExecution.");
        }
        dirtyStepExec.refresh(freshStepExecution);
        entityManager.merge(dirtyStepExec);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Updated Dirty StepExection.");
        }
    }
}
