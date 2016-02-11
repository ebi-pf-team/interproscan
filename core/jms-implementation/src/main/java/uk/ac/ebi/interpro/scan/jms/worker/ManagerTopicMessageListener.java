package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.master.ClusterState;
import uk.ac.ebi.interpro.scan.jms.monitoring.*;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

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

    private WorkerMessageSender workerMessageSender;

    private int connectionLossCount = 0;

    private Long previousExceptionTime;

    private WorkerImpl controller;

    @Required
    public void setWorkerMessageSender(WorkerMessageSender workerMessageSender) {
        this.workerMessageSender = workerMessageSender;
    }

    public WorkerMessageSender getWorkerMessageSender() {
        return workerMessageSender;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    public void setConnectionLossCount(int connectionLossCount) {
        this.connectionLossCount = connectionLossCount;
    }

    public WorkerImpl getController() {
        return controller;
    }

    public void setController(WorkerImpl controller) {
        this.controller = controller;
    }

    @Override
    public void onMessage(final Message message) {
        LOGGER.debug("Worker: Received Shutdown or clusterState message from the master.");
        //set the shutdown flag for this worker
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) message;
                Object messageContents = null;
                messageContents = objectMessage.getObject();
                if (messageContents instanceof Shutdown) {
                    if (Utilities.verboseLogLevel > 4) {
                        Utilities.verboseLog("Worker Received Shutdown message: ");
                    }
                    setShutdown(true);
                    workerMessageSender.sendShutDownMessage(message);
                } else if (messageContents instanceof ClusterState) {
                    ClusterState clusterState = (ClusterState) messageContents;
                    if (Utilities.verboseLogLevel > 4) {
                        Utilities.verboseLog("Worker Received clusterState: " + clusterState.toString());
                    }
                    if (controller != null) {
                        controller.setSubmissionWorkerClusterState(clusterState);
                    }
                    workerMessageSender.sendTopicMessage(clusterState);

                } else {
                    LOGGER.warn("Received unknown message  " + messageContents.toString());
                }
            }
        } catch (JMSException e) {
            Long now = System.currentTimeMillis();
            if (connectionLossCount == 0) {
                LOGGER.error("JMSException thrown in TopicMessageListener. ", e);
            }
            connectionLossCount++;
            Long getConnectionLossTime;
            if (controller != null) {
                controller.handleFailure(ManagerTopicMessageListener.class.getName());
            }
        } catch (Exception e) {
            LOGGER.error("Exception thrown in TopicMessageListener.", e);
            if (controller != null) {
                controller.handleFailure(ManagerTopicMessageListener.class.getName());
            }
        }
    }
}
