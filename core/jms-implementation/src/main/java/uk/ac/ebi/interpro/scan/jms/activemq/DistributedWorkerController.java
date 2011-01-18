package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This controller monitors the activity of the Worker.  If the worker is inactive and has
 * been idle for longer than maximumIdleTimeMillis, or if the worker is inactive and
 * has been running for longer than maximumLifeMillis then it is shut down and the
 * JVM closes.
 *
 * @author Phil Jones
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class DistributedWorkerController implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(DistributedWorkerController.class.getName());

    private final long startUpTime = new Date().getTime();

    private long lastMessageFinishedTime = new Date().getTime();

    private long maximumIdleTimeMillis = Long.MAX_VALUE;

    private long maximumLifeMillis = Long.MAX_VALUE;

    private final List<String> runningJobs = new ArrayList<String>();

    private final Object jobListLock = new Object();

    private DefaultMessageListenerContainer messageListenerContainer;

    private int priority;

    private boolean highMemory;

    /**
     * Constructor that requires a DefaultMessageListenerContainer - this needs to be set before anything else.
     *
     * @param messageListenerContainer that must be set before anything else.
     */
    public DistributedWorkerController(DefaultMessageListenerContainer messageListenerContainer) {
        if (messageListenerContainer == null) {
            throw new IllegalArgumentException("A DistributedWorkerController cannot be instantiated with a null DefaultMessageListenerContainer.");
        }
        this.messageListenerContainer = messageListenerContainer;
    }

    public void setMaximumIdleTimeSeconds(Long maximumIdleTime) {
        this.maximumIdleTimeMillis = maximumIdleTime * 1000;
    }

    public void setMaximumLifeSeconds(Long maximumLife) {
        this.maximumLifeMillis = maximumLife * 1000;
    }

    @Required
    public void setHighMemory(boolean highMemory) {
        this.highMemory = highMemory;
        setMessageSelector();
    }

    public void jobStarted(String jmsMessageId) {
        synchronized (jobListLock) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Job " + jmsMessageId + " added to DistributedWorkerController.runningJobs");
            }
            runningJobs.add(jmsMessageId);
        }
    }

    public void jobFinished(String jmsMessageId) {
        synchronized (jobListLock) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Job " + jmsMessageId + " removed from DistributedWorkerController.runningJobs");
            }
            if (!runningJobs.remove(jmsMessageId)) {
                LOGGER.error("DistributedWorkerController.jobFinished(jmsMessageId) has been called with a message ID that it does not recognise: " + jmsMessageId);
            }
            lastMessageFinishedTime = new Date().getTime();
        }
    }

    public boolean stopIfAppropriate() {
        synchronized (jobListLock) {
            final long now = new Date().getTime();
            final boolean exceededLifespan = now - startUpTime > maximumLifeMillis;
            final boolean exceededIdleTime = now - lastMessageFinishedTime > maximumIdleTimeMillis;
            if (runningJobs.size() == 0 && (exceededLifespan || exceededIdleTime)) {

                if (LOGGER.isInfoEnabled()) {
                    if (exceededLifespan) {
                        LOGGER.info("Stopping worker as exceeded maximum life span");
                    } else {
                        LOGGER.info("Stopping worker as idle for longer than max idle time");
                    }
                }

                messageListenerContainer.stop();
                return true;
            }
        }
        return false;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            while (!stopIfAppropriate()) {
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException thrown by DistributedWorkerController.  Stopping now.", e);
        }
    }

    public void setMinimumJmsPriority(int priority) {
        this.priority = priority;
        setMessageSelector();
    }

    /**
     * Gets called after either the priority of the messageListenerContainer get set,
     * to ensure that the priority being listened for is set (if required).
     */
    private void setMessageSelector() {
        StringBuilder messageSelector = new StringBuilder();

        messageSelector.append(MasterMessageSenderImpl.HIGH_MEMORY_PROPERTY)
                .append(" = ")
                .append(Boolean.toString(highMemory).toUpperCase());

        if (priority > 0) {
            LOGGER.info("This worker has been set to receive messages with priority >= " + priority);
            messageSelector.append(" and JMSPriority >= ").append(priority);
        }
        messageListenerContainer.setMessageSelector(messageSelector.toString());
    }
}



