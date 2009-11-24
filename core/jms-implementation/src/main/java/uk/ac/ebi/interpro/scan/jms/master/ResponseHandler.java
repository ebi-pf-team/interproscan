package uk.ac.ebi.interpro.scan.jms.master;

import uk.ac.ebi.interpro.scan.management.model.StepExecution;

import javax.jms.Message;
import java.util.Map;

/**
 * Interface for a Handler that can respond appropriately to any message
 * returned from a Worker node.  This handler is intended to run in the
 * Master application.
 *
 * @author Phil Jones
 * @version $Id: ResponseHandler.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public interface ResponseHandler {

    /**
     * Recieves a JMS Message and does whatever is needed.
     * @param message being a JMS message returned from the worker.
     */
    void handleResponse (Message message);

    /**
     * Sets a reference to the stepExecutions so that they can be
     * updated from the response to the Master.
     * @param stepExecutions that have been completed on remote nodes.
     */
    void setStepExecutionMap(Map<String, StepExecution> stepExecutions);
}
