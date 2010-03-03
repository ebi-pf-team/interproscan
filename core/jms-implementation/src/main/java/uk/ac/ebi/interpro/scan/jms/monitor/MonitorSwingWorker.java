package uk.ac.ebi.interpro.scan.jms.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import uk.ac.ebi.interpro.scan.jms.SessionHandler;
import uk.ac.ebi.interpro.scan.jms.worker.WorkerMonitor;
import uk.ac.ebi.interpro.scan.jms.worker.WorkerState;

import javax.jms.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This StringWorker does the job of retrieving the worker state
 * at the interval set.
 *
 * @author Phil Jones
 * @version $Id: MonitorSwingWorker.java,v 1.1 2009/10/21 18:44:40 pjones Exp $
 * @since 1.0
 */
public class MonitorSwingWorker extends SwingWorker<Void, List<WorkerState>> {


    private volatile long refreshInterval;

    private volatile WorkerMonitorController controller;

    private String workerManagerTopicName;

    private String workerManagerResponseQueueName;

    private UUID monitorID;

    private ConnectionFactory connectionFactory;

    @Required
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setController(WorkerMonitorController controller) {
        this.controller = controller;
        this.monitorID = controller.getMonitorId();
        setRefreshInterval(controller.getRefreshInterval());
    }

    /**
     * Allows the GUI controller to change the refresh interval.
     * @param refreshInterval
     */
    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    @Required
    public void setWorkerManagerTopicName(String workerManagerTopicName) {
        this.workerManagerTopicName = workerManagerTopicName;
    }

    @Required
    public void setWorkerManagerResponseQueueName(String workerManagerResponseQueueName) {
        this.workerManagerResponseQueueName = workerManagerResponseQueueName;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     * <p/>
     * <p/>
     * Note that this method is executed only once.
     * <p/>
     * <p/>
     * Note: this method is executed in a background thread.
     *
     * @return the computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    protected Void doInBackground() throws Exception {

        SessionHandler sessionHandler = null;
        try{
            sessionHandler = new SessionHandler(connectionFactory);
            sessionHandler.start();
            MessageProducer requestStatusMessageProducer = sessionHandler.getMessageProducer(workerManagerTopicName);


            // Work out a receive timeout - set to (fairly arbitrarily) one twentieth of the refresh interval.
            // This should give the user a faily consistent experience.
            final long recieveTimeout = (refreshInterval / 20L == 0) ? 1 : refreshInterval / 20L;

            // Keep going until the Worker thread is explicitly stopped.
            while (! isCancelled()){

                // Unique identifier for THIS REQUEST.
//            final String requestId = monitorID.toString() + '_' + UUID.randomUUID();
                final String requestId = UUID.randomUUID().toString();

                String selector = new StringBuilder()
                        .append('(')
                        .append(WorkerMonitor.REQUESTEE_PROPERTY)
                        .append(" = '")
                        .append(requestId)
                        .append("')")
                        .toString();
                // The selector ensures that this monitor application instance only receives messages in response
                // to its own monitor requests.
                final MessageConsumer workerResponsemessageConsumer = sessionHandler.getMessageConsumer(workerManagerResponseQueueName, selector);

                List<WorkerState> workerStates = new ArrayList<WorkerState>();

                // Send the status request to the workerManagerTopic.
                TextMessage statusRequestMessage =  sessionHandler.createTextMessage("status");
                statusRequestMessage.setStringProperty(WorkerMonitor.REQUESTEE_PROPERTY, requestId);
                requestStatusMessageProducer.send(statusRequestMessage);

                final long listenUntil = System.currentTimeMillis() + refreshInterval;
                // Take results of the workerManagerResponseQueue for the period of the refreshInterval.
                while (System.currentTimeMillis() < listenUntil){
                    // Take messages of the workerManagerResponseQueue
                    // and add the WorkerState to the Collection..
                    ObjectMessage responseMessage = (ObjectMessage) workerResponsemessageConsumer.receive(recieveTimeout);
                    if (responseMessage != null){
                        workerStates.add ((WorkerState) responseMessage.getObject());
                        responseMessage.acknowledge();
                    }
                }
                // Stick out the current Collection of WorkerState objects to the GUI
                // for display.
                controller.setStatus(workerStates);
            }
            return null;
        }
        finally {
            if (sessionHandler != null){
                sessionHandler.close();
            }
        }
    }
}
