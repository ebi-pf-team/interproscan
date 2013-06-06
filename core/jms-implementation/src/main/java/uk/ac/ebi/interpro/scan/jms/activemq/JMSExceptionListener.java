package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.activemq.transport.TransportListener;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import java.io.IOException;

/**
 * @author Gift Nuka
 */

@Component
public class JMSExceptionListener implements ExceptionListener {

    private static final Logger LOGGER = Logger.getLogger(JMSExceptionListener.class.getName());

    public synchronized void onException(JMSException e) {

        Exception ex = e.getLinkedException();
        if (ex != null) {
            LOGGER.debug("Custom JMS Exception handler: ", e);
        }
    }

}