package uk.ac.ebi.interpro.scan.jms.master;

import javax.jms.Message;

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

}
