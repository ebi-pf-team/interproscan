package uk.ac.ebi.interpro.scan.jms.master;

/**
 * Interface for the Master application.
 *
 * Expects there to be a queue on to which to place jobs (the task submission queue)
 * and a response monitor that is able to consume responses from workers and handle them
 * appropriately.
 *
 * The response monitor should be run in a separate thread.
 *
 * The injected SessionHandler object looks after connecting to the Broker.
 *
 * @author Phil Jones
 * @version $Id: Master.java,v 1.2 2009/10/16 12:05:10 pjones Exp $
 * @since 1.0
 */
public interface Master{

    /**
     * Starts the Master application.
     */
    void start();

}
