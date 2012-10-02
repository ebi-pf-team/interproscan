package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.log4j.Logger;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * This implementation receives messages on the workerManagerTopic
 * and then commands the controller to shutdown.
 *
 * @author nuka, scheremetjew
 * @version $Id: ResponseMonitorImpl.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class ManagerTopicMessageListener implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(ManagerTopicMessageListener.class.getName());


    private boolean shutdown = false;

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    @Override
    public void onMessage(final Message message) {
        LOGGER.debug("Worker: Received shut down command from the master.");
        setShutdown(true);
        //controller.setShutdown(true);
    }
}
