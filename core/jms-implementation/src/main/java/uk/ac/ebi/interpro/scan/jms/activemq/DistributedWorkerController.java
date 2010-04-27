package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Extends the Spring DefaultMessageListenerContainer to provide a ListenerContainer that knows when to
 * close itself down.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class DistributedWorkerController {

    private static final Logger LOGGER = Logger.getLogger(DistributedWorkerController.class.getName());

    private final long startUpTime          = new Date().getTime();

    private long lastMessageFinishedTime    = new Date().getTime();

    private long maximumIdleTimeMillis      = Long.MAX_VALUE;

    private long maximumLifeMillis          = Long.MAX_VALUE;

    private final List<String> runningJobs  = new ArrayList<String>();

    private final Object jobListLock        = new Object();

    private DefaultMessageListenerContainer messageListenerContainer;

    public void setMaximumIdleTimeSeconds(Long maximumIdleTime) {
        this.maximumIdleTimeMillis = maximumIdleTime * 1000;
    }

    public void setMaximumLifeSeconds(Long maximumLife) {
        this.maximumLifeMillis = maximumLife * 1000;
    }

    @Required
    public void setMessageListenerContainer(DefaultMessageListenerContainer messageListenerContainer) {
        this.messageListenerContainer = messageListenerContainer;
    }

    public void jobStarted(String jmsMessageId){
        synchronized (jobListLock){
            runningJobs.add(jmsMessageId);
        }
    }

    public void jobFinished(String jmsMessageId){
        synchronized (jobListLock){
            runningJobs.remove(jmsMessageId);
            lastMessageFinishedTime = new Date().getTime();
        }
    }

    public void stopIfAppropriate(){
        LOGGER.debug ("Called DistributedWorkerController.stopIfAppropriate()");
        synchronized (jobListLock){
            final long now = new Date().getTime();
            final boolean exceededLifespan = now - startUpTime > maximumLifeMillis;
            final boolean exceededIdleTime = now - lastMessageFinishedTime > maximumIdleTimeMillis;
            if (runningJobs.size() == 0 && ( exceededLifespan || exceededIdleTime )){

                if (LOGGER.isInfoEnabled()){
                    if (exceededLifespan){
                        LOGGER.info("Stopping worker as exceeded maximum life span");
                    }
                    else if (exceededIdleTime){
                        LOGGER.info("Stopping worker as idle for longer than max idle time");
                    }
                }

                messageListenerContainer.stop();
            }
        }
    }
}



