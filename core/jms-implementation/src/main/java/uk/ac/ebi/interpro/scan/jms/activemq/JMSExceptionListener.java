package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.activemq.transport.TransportListener;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import uk.ac.ebi.interpro.scan.util.Utilities;
import uk.ac.ebi.interpro.scan.jms.worker.WorkerImpl;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import java.io.IOException;

/**
 * @author Gift Nuka
 */

@Component
public class JMSExceptionListener implements ExceptionListener {

    private static final Logger LOGGER = Logger.getLogger(JMSExceptionListener.class.getName());

    private WorkerImpl controller;

    private int exceptionCount = 0;

    Long timeFirstException = System.currentTimeMillis();
    Long timePreviousExceptionReceived = System.currentTimeMillis();

    String contextName = "general";
    JMSExceptionListener(String contextName){
        this.contextName = contextName;
    }

    public synchronized void onException(JMSException e) {
        Utilities.verboseLog("JMSExceptionListener-  JMSExceptions thrown : " + e);
        timePreviousExceptionReceived = System.currentTimeMillis();
        if(exceptionCount == 0){
            timeFirstException = System.currentTimeMillis();
        }
        exceptionCount++;
        Exception ex = e.getLinkedException();
        if (ex != null) {
            if (contextName.equals("workerExceptionListener") && (controller != null)) {
                LOGGER.debug("masterURL: " + controller.getMasterUri()
                        + " thisWorkerURL: " + controller.getTcpUri());
                if (controller.getTcpUri() != null || controller.getMasterUri() != null) {
                    LOGGER.warn("context: [" + contextName + "] Custom JMS Exception handler: ", e);
                }else{
                    LOGGER.debug("context: [" + contextName + "] Maybe the controller bean is still not yet set up");
                }
            } else if (controller == null) {
                LOGGER.debug("context: [" + contextName + "] Maybe the controller bean is still not yet set up");
            }else {
                LOGGER.warn("context: [" + contextName + "] Custom JMS Exception handler: ", e);
            }
        }

        Long timeSinceFirstException = System.currentTimeMillis()  - timeFirstException;
        if((System.currentTimeMillis() - timePreviousExceptionReceived) < 5 * 60 * 1000){
            //We may have a connection problem
            long currentConnectionRetryTimeOut = 5 * 1000; //5 seconds
            Long exceptionFrequency = timeSinceFirstException / exceptionCount;
            //if we are receiving exceptions every 20 min or so, we may have a problem
            //make this number confugurable
            if(timeSinceFirstException > 20 * 60 * 1000 &&
                exceptionFrequency < currentConnectionRetryTimeOut + 7250){
                //we may have a problem, report to control and shut down
                LOGGER.warn("Custom JMS Exception handler: Receiving exceptions every " + exceptionFrequency.intValue() + "ms");
                if (controller != null) {
                    controller.systemExit(99);
                }else{
                    LOGGER.fatal("Custom JMS Exception handler: Receiving exceptions every " + exceptionFrequency.intValue() + "ms");
                    System.exit(99);
                }
            }
        }else{
            //reset
            timeFirstException = System.currentTimeMillis();
        }

    }

    public WorkerImpl getController() {
        return controller;
    }

    public void setController(WorkerImpl controller) {
        this.controller = controller;
    }
}