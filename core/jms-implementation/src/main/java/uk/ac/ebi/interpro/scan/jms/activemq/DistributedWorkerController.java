package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.Session;
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

    private JmsTemplateWrapper jmsTemplateWrapper;

    private int priority;

    private boolean highMemory;

    private boolean remoteWorker;

    private String masterUri;

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

    public void setJmsTemplateWrapper(JmsTemplateWrapper jmsTemplateWrapper) {
        this.jmsTemplateWrapper = jmsTemplateWrapper;
    }

    @Required
    public void setHighMemory(boolean highMemory) {
        this.highMemory = highMemory;
        setMessageSelector();
    }

    @Required
    public void setRemoteWorker(boolean remoteWorker) {
        this.remoteWorker = remoteWorker;
    }

    /**
     * If a Master URI has been set on this DistributedWorkerController, this method will return it,
     * otherwise will return null.
     *
     * @return If a Master URI has been set on this DistributedWorkerController, this method will return it,
     *         otherwise will return null.
     */
    public String getMasterUri() {
        return masterUri;
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
                if (LOGGER.isTraceEnabled()) LOGGER.trace("State while running:");
                logMessageListenerContainerState();
                Thread.sleep(1000);
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
        messageSelector
                .append(MasterMessageSenderImpl.HIGH_MEMORY_PROPERTY)
                .append(" = ")
                .append(Boolean.toString(highMemory).toUpperCase())
                .append(" AND ")
                .append(MasterMessageSenderImpl.CAN_RUN_REMOTELY_PROPERTY)
                .append(" = TRUE");
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("This worker has been set to receive messages with priority >= " + priority);
        }
        messageSelector.append(" AND JMSPriority >= ").append(priority);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Message selector: '" + messageSelector + "'");
        }
        messageListenerContainer.setMessageSelector(messageSelector.toString());
        if (LOGGER.isTraceEnabled()) {
            LOGGER.debug("State following setMessageSelector call:");
            logMessageListenerContainerState();
        }
    }

    /*

    <bean id="remoteJmsTemplate" class="org.springframework.jms.core.JmsTemplate">
        <property name="connectionFactory">
            <!-- lets wrap in a pool to avoid creating a connection per send -->
            <!--<bean class="org.apache.activemq.pool.PooledConnectionFactory" destroy-method="stop">-->
                <!--<property name="connectionFactory">-->
                    <!--<ref local="remoteJmsFactory" />-->
                <!--</property>-->
            <!--</bean>-->

            <bean class="org.springframework.jms.connection.CachingConnectionFactory">
                <constructor-arg ref="remoteJmsFactory" />
                <property name="sessionCacheSize" value="100" />
            </bean>
        </property>
        <property name="explicitQosEnabled" value="true"/>
        <property name="sessionAcknowledgeModeName" value="CLIENT_ACKNOWLEDGE"/>
    </bean>


     */

    private void logMessageListenerContainerState() {
        if (LOGGER.isTraceEnabled()) {
            if (messageListenerContainer == null) {
                LOGGER.trace("MessageListenerContainer is null.");
            } else {
                StringBuffer buf = new StringBuffer("DefaultMessageListenerContainer state:\n\n");
                buf.append("isRunning: ").append(messageListenerContainer.isRunning()).append("\n");
                buf.append("isActive: ").append(messageListenerContainer.isActive()).append("\n");
                buf.append("getDestinationName: ").append(messageListenerContainer.getDestinationName()).append("\n");
                buf.append("isRegisteredWithDestination: ").append(messageListenerContainer.isRegisteredWithDestination()).append("\n");
                buf.append("getActiveConsumerCount: ").append(messageListenerContainer.getActiveConsumerCount()).append("\n");
                buf.append("getCacheLevel: ").append(messageListenerContainer.getCacheLevel()).append("\n");
                buf.append("getConcurrentConsumers: ").append(messageListenerContainer.getConcurrentConsumers()).append("\n");
                buf.append("getIdleConsumerLimit: ").append(messageListenerContainer.getIdleConsumerLimit()).append("\n");
                buf.append("getIdleTaskExecutionLimit: ").append(messageListenerContainer.getIdleTaskExecutionLimit()).append("\n");
                buf.append("getMaxConcurrentConsumers: ").append(messageListenerContainer.getMaxConcurrentConsumers()).append("\n");
                buf.append("getScheduledConsumerCount: ").append(messageListenerContainer.getScheduledConsumerCount()).append("\n");
                buf.append("getClientId: ").append(messageListenerContainer.getClientId()).append("\n");
                buf.append("getDurableSubscriptionName: ").append(messageListenerContainer.getDurableSubscriptionName()).append("\n");
                buf.append("getMessageSelector: ").append(messageListenerContainer.getMessageSelector()).append("\n");
                buf.append("getPausedTaskCount: ").append(messageListenerContainer.getPausedTaskCount()).append("\n");
                buf.append("getPhase: ").append(messageListenerContainer.getPhase()).append("\n");
                buf.append("getSessionAcknowledgeMode: ").append(messageListenerContainer.getSessionAcknowledgeMode()).append("\n");
                buf.append("isAcceptMessagesWhileStopping: ").append(messageListenerContainer.isAcceptMessagesWhileStopping()).append("\n");
                buf.append("isAutoStartup: ").append(messageListenerContainer.isAutoStartup()).append("\n");
                buf.append("isExposeListenerSession: ").append(messageListenerContainer.isExposeListenerSession()).append("\n");
                buf.append("isPubSubDomain: ").append(messageListenerContainer.isPubSubDomain()).append("\n");
                buf.append("isSessionTransacted: ").append(messageListenerContainer.isSessionTransacted()).append("\n");
                buf.append("isSubscriptionDurable: ").append(messageListenerContainer.isSubscriptionDurable()).append("\n");
                LOGGER.trace(buf);
            }
        }
    }

    public void setMasterUri(String masterUri) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Master URI passed in to Controller: " + masterUri);
        }
        if (jmsTemplateWrapper == null) {
            throw new IllegalStateException("This DistributeWorkerController does not have a reference to the JmsTemplateWrapper, needed to configure the connection.");
        }

        this.masterUri = masterUri;
        final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(masterUri);
        activeMQConnectionFactory.setOptimizeAcknowledge(true);
        activeMQConnectionFactory.setUseCompression(true);
        activeMQConnectionFactory.setAlwaysSessionAsync(false);
        activeMQConnectionFactory.getPrefetchPolicy().setQueuePrefetch(0);

        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory(activeMQConnectionFactory);
        connectionFactory.setSessionCacheSize(100);
        messageListenerContainer.setConnectionFactory(connectionFactory);

        final JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setExplicitQosEnabled(true);
        template.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        jmsTemplateWrapper.setTemplate(template); // Give all other components access to the correctly configured JmsTemplate.
//        messageListenerContainer.initialize();
        messageListenerContainer.start();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MessageListenerContainer started, connected to: " + masterUri);
        }
    }
}



