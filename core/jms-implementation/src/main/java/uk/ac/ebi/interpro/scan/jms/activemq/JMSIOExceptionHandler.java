package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.util.IOExceptionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import java.io.IOException;

/**
 * @author Gift Nuka
 */

@Component
public class JMSIOExceptionHandler implements IOExceptionHandler {

    private static final Logger LOGGER = LogManager.getLogger(JMSIOExceptionHandler.class.getName());

    @Override
    public void handle(IOException e) {
        //To change body of implemented methods use File | Settings | File Templates.
        e.printStackTrace();
        LOGGER.debug("IOException has occured: " + e);
    }

    @Override
    public void setBrokerService(BrokerService brokerService) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
//
//    void handle(IOException exception){
//
//
////    }
//
//    void setBrokerService(BrokerService brokerService){
//
//    }



}