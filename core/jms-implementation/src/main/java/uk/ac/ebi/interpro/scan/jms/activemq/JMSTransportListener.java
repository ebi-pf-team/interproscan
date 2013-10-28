package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.activemq.transport.TransportListener;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import uk.ac.ebi.interpro.scan.jms.stats.Utilities;

import java.io.IOException;

/**
 * @author Gift Nuka
 */

@Component
public class JMSTransportListener implements TransportListener {

    private static final Logger LOGGER = Logger.getLogger(JMSTransportListener.class.getName());

    private int exceptionsCount = 0;

    private Long previousIOExceptionTime =0l;

    private Long lastInterrupt = 0l;

    private boolean brokenConnection = false;
    private boolean interruptedConnection = false;

    public int getExceptionsCount() {
        return exceptionsCount;
    }

    public void setExceptionsCount(int exceptionsCount) {
        this.exceptionsCount = exceptionsCount;
    }

    public boolean isBrokenConnection() {
        return brokenConnection;
    }

    public void setBrokenConnection(boolean brokenConnection) {
        this.brokenConnection = brokenConnection;
    }


    @Override
    public void onCommand(Object o) {

    }

    @Override
    public void onException(IOException e) {
        if(exceptionsCount == 0){
            previousIOExceptionTime = System.currentTimeMillis();
            LOGGER.debug("Custom Transport IO exception: ", e);
            if(Utilities.verboseLogLevel > 4) {
                Utilities.verboseLog("Custom Transport IO exception: " + e);
            }
            exceptionsCount++;
        }else{
            Long now = System.currentTimeMillis();
            Long timePassed = now - previousIOExceptionTime;
            if (timePassed > 600000)  {
                if (exceptionsCount > 15){
                    previousIOExceptionTime = System.currentTimeMillis();
                    brokenConnection = true;
                    LOGGER.debug("Custom Transport connection IO exception: ", e);
                    if(Utilities.verboseLogLevel > 4) {
                        Utilities.verboseLog("Custom Transport connection IO exception: " + e);
                    }
//                    e.printStackTrace();
                }
            }
            exceptionsCount++;
        }

    }

    @Override
    public void transportInterupted() {
        Long now = System.currentTimeMillis();
        if(Utilities.verboseLogLevel > 4) {
            Utilities.verboseLog("Transport interrupted: ...");
        }
        Long timePassed = 0l;
        if(!interruptedConnection){
            LOGGER.debug("Transport interrupted: ");
        }else{
            timePassed = now - previousIOExceptionTime;
            if (timePassed > 600000){
                LOGGER.warn("Transport interrupted  for > 10 min");
            }
        }
        lastInterrupt = System.currentTimeMillis();

        interruptedConnection = true;
    }

    @Override
    public void transportResumed() {
        //To change body of implemented methods use File | Settings | File Templates.
        interruptedConnection = false;
        if(Utilities.verboseLogLevel > 4) {
            Utilities.verboseLog("Custom Transport resumed ...");
        }
    }
}
