package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import uk.ac.ebi.interpro.scan.io.TemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.jms.activemq.MasterMessageSenderImpl;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.SubmissionWorkerRunner;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.WorkerRunner;
import uk.ac.ebi.interpro.scan.jms.stats.StatsMessageListener;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.jms.stats.Utilities;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.ArrayList;
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
public class WorkerImpl implements Worker {

    private static final Logger LOGGER = Logger.getLogger(WorkerImpl.class.getName());

    private long lastMessageFinishedTime = System.currentTimeMillis(); //new Date().getTime();

    private final List<String> runningJobs = new ArrayList<String>();
    private int totalJobCount = 0;
    private boolean responseQueueListenerBusy = false;

    private final Object jobListLock = new Object();

    private DefaultMessageListenerContainer remoteQueueJmsContainer;
    private DefaultMessageListenerContainer statsListenerContainer;
    private DefaultMessageListenerContainer managerTopicMessageListenerJmsContainer;

    private ResponseQueueMessageListener responseQueueMessageListener;
    private ManagerTopicMessageListener managerTopicMessageListener;


    private boolean shutdown = false;


    private StatsUtil statsUtil;

    private JMSTransportListener JMSTransportListener;

    private Destination statsQueue;
    /* Local job request queue */
    private Destination jobRequestQueue;
    private Destination highMemJobRequestQueue;
    private Destination workerManagerTopic;

    private Destination jobResponseQueue;

    /**
     * True if the worker was created by the master, otherwise false.
     */
    private boolean masterWorker = false;
    private int priority;

    private boolean highMemory;

    private boolean remoteWorker;

    private String masterUri;
    private String tcpUri;

    protected final long startUpTime = System.currentTimeMillis();
    protected long maximumIdleTimeMillis = Long.MAX_VALUE;
    protected long maximumLifeMillis = Long.MAX_VALUE;

    private int completionFactor = 20;
    private int maxConsumerSize = 40;
    private int queueConsumerRatio = 20;

    protected JmsTemplate localJmsTemplate;
    protected JmsTemplate remoteJmsTemplate;

    protected final String STATS_BROKER = "ActiveMQ.Statistics.Destination.";

    private WorkerRunner workerRunner;
    private WorkerRunner workerRunnerHighMemory;

    private String projectId;

    private int tier = 1;

    private int maxTierDepth = 1;

    private boolean stopRemoteQueueJmsContainer = false;

    private boolean gridThrottle = true;

    private TemporaryDirectoryManager temporaryDirectoryManager;

    private int maxUnfinishedJobs;

    /**
     * Constructor that requires a DefaultMessageListenerContainer - this needs to be set before anything else.
     *
     * @param remoteQueueJmsContainer that must be set before anything else.
     */
    public WorkerImpl(DefaultMessageListenerContainer remoteQueueJmsContainer, DefaultMessageListenerContainer statsListenerContainer,
                      DefaultMessageListenerContainer managerTopicMessageListenerJmsContainer) {
        if (remoteQueueJmsContainer == null) {
            throw new IllegalArgumentException("A Worker cannot be instantiated with a null Worker DefaultMessageListenerContainer.");
        }
        this.remoteQueueJmsContainer = remoteQueueJmsContainer;
        if (statsListenerContainer == null) {
            throw new IllegalArgumentException("A Worker cannot be instantiated with a null Stats DefaultMessageListenerContainer.");
        }
        this.statsListenerContainer = statsListenerContainer;
        if (managerTopicMessageListenerJmsContainer == null) {
            throw new IllegalArgumentException("A Worker cannot be instantiated with a null managerTopic DefaultMessageListenerContainer .");
        }
        this.managerTopicMessageListenerJmsContainer = managerTopicMessageListenerJmsContainer;

    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner) this.workerRunner).setProjectId(projectId);
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setProjectId(projectId);
        }
    }

    public void setTier(int tier) {
        this.tier = tier;
        final int workerTier = this.tier + 1;
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner) this.workerRunner).setTier(workerTier);
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setTier(workerTier);
        }
        statsUtil.setTier(tier);
    }

    /**
     * set the maximum tier dept for the network
     * @param maxTierDepth
     */
    public void setMaxTierDepth(int maxTierDepth) {
        this.maxTierDepth = maxTierDepth;
    }

    @Required
    public void setResponseQueueMessageListener(ResponseQueueMessageListener responseQueueMessageListener) {
        this.responseQueueMessageListener = responseQueueMessageListener;
    }

    @Required
    public void setManagerTopicMessageListener(ManagerTopicMessageListener managerTopicMessageListener) {
        this.managerTopicMessageListener = managerTopicMessageListener;
    }

    @Required
    public void setTemporaryDirectoryManager(TemporaryDirectoryManager temporaryDirectoryManager) {
        this.temporaryDirectoryManager = temporaryDirectoryManager;
    }

    @Required
    public void setWorkerRunner(WorkerRunner workerRunner) {
        this.workerRunner = workerRunner;
    }

    @Required
    public void setWorkerRunnerHighMemory(WorkerRunner workerRunnerHighMemory) {
        this.workerRunnerHighMemory = workerRunnerHighMemory;
    }

    public void setQueueConsumerRatio(int queueConsumerRatio) {
        this.queueConsumerRatio = queueConsumerRatio;
    }

    public void setMaxConsumerSize(int maxConsumerSize) {
        this.maxConsumerSize = maxConsumerSize;
    }

    public void setCompletionFactor(int completionFactor) {
        this.completionFactor = completionFactor;
    }

    @Required
    public void setJobRequestQueue(Destination jobRequestQueue) {
        this.jobRequestQueue = jobRequestQueue;
    }


    public Destination getJobRequestQueue() {
        return jobRequestQueue;
    }

    @Required
    public void setStatsQueue(Destination statsQueue) {
        this.statsQueue = statsQueue;
    }

    public Destination getStatsQueue() {
        return statsQueue;
    }

    public Destination getJobResponseQueue() {
        return jobResponseQueue;
    }

    public void setHighMemJobRequestQueue(Destination highMemJobRequestQueue) {
        this.highMemJobRequestQueue = highMemJobRequestQueue;
    }

    @Required
    public void setJobResponseQueue(Destination jobResponseQueue) {
        this.jobResponseQueue = jobResponseQueue;
    }

    //used to send the shutdown message
    public void setWorkerManagerTopic(Destination workerManagerTopic) {
        this.workerManagerTopic = workerManagerTopic;
    }

    public void setMaximumIdleTimeSeconds(Long maximumIdleTime) {
        this.maximumIdleTimeMillis = maximumIdleTime * 1000;
    }

    public void setMaximumLifeSeconds(Long maximumLife) {
        this.maximumLifeMillis = maximumLife * 1000;
    }

    @Required
    public void setLocalJmsTemplate(JmsTemplate localJmsTemplate) {
        this.localJmsTemplate = localJmsTemplate;
    }

    public void setRemoteJmsTemplate(JmsTemplate remoteJmsTemplate) {
        this.remoteJmsTemplate = remoteJmsTemplate;
    }

    public JmsTemplate getRemoteJmsTemplate() {
        return remoteJmsTemplate;
    }

    @Required
    public void setHighMemory(boolean highMemory) {
        this.highMemory = highMemory;

        setMessageSelector();
        //TODO: remove if tier1 arg works
//        setMasterWorker(true);
    }

    @Required
    public void setRemoteWorker(boolean remoteWorker) {
        this.remoteWorker = remoteWorker;
    }

    public void setTcpUri(String tcpUri) {
        this.tcpUri = tcpUri;
    }

    public String getTcpUri() {
        return tcpUri;
    }

    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }

    public StatsUtil getStatsUtil() {
        return statsUtil;
    }

    public void setMasterWorker(boolean masterWorker) {
        this.masterWorker = masterWorker;
        //set high memory to true as well  if not set
//        if (masterWorker) setHighMemory(true);

    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    public void setTotalJobCount(int totalJobCount) {
        this.totalJobCount = totalJobCount;
    }

    /**
     * If a Master URI has been set on this Worker, this method will return it,
     * otherwise will return null.
     *
     * @return If a Master URI has been set on this Worker, this method will return it,
     *         otherwise will return null.
     */
    public String getMasterUri() {
        return masterUri;
    }

    @Required
    public void setGridThrottle(boolean gridThrottle) {
        this.gridThrottle = gridThrottle;
    }

    @Required
    public void setMaxUnfinishedJobs(int maxUnfinishedJobs) {
        this.maxUnfinishedJobs = maxUnfinishedJobs;
    }

    @Required
    public void setJMSTransportListener(JMSTransportListener JMSTransportListener) {
        this.JMSTransportListener = JMSTransportListener;
    }

    public void jobStarted(String jmsMessageId) {
        synchronized (jobListLock) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Job " + jmsMessageId + " added to Worker.runningJobs");
            }
            runningJobs.add(jmsMessageId);
        }
    }

    public void jobFinished(String jmsMessageId) {
        synchronized (jobListLock) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Job " + jmsMessageId + " removed from Worker.runningJobs");
            }
            if (!runningJobs.remove(jmsMessageId)) {
                LOGGER.error("Worker.jobFinished(jmsMessageId) has been called with a message ID that it does not recognise: " + jmsMessageId);
            }
            lastMessageFinishedTime = System.currentTimeMillis(); //new Date().getTime();

            totalJobCount ++;

        }
    }

    /**
     *  record that a job has been received on the response Monitor
     *
     */
    public void jobResponseReceived(){
        responseQueueListenerBusy = true;
    }

    /**
     * record that a response has been sent to the master
     */
    public void jobResponseProcessed(){
        responseQueueListenerBusy = false;
    }

    private Long lifeRemaining() {
        return System.currentTimeMillis() - startUpTime;
    }

    public boolean stopIfAppropriate() {
        synchronized (jobListLock) {
            final long now = System.currentTimeMillis();
            final boolean exceededLifespan = (now - startUpTime) > maximumLifeMillis;
            final boolean exceededIdleTime = (now - lastMessageFinishedTime) > maximumIdleTimeMillis;
            LOGGER.debug("Now: "+ now+ " lastMessageFinished: " +lastMessageFinishedTime +" IdleTime: " +(now - lastMessageFinishedTime) + " maxIdleTime: "+maximumIdleTimeMillis);
            //if exceededIdleTime check if workers have running jobs
            if (runningJobs.size() == 0 && (exceededLifespan || (exceededIdleTime && (!workersHaveRunningJobs())))) {

                if (LOGGER.isInfoEnabled()) {
                    if (exceededLifespan) {
                        LOGGER.info("Stopping worker as exceeded maximum life span");
                    } else {
                        LOGGER.info("Stopping worker as idle for longer than max idle time");
                    }
                }
                remoteQueueJmsContainer.stop();
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
//        System.out.println(Utilities.getTimeNow() + " Running InterProScan worker  ...");
        LOGGER.debug("Running InterProScan worker run() ... whoAmI: " + whoAmI() + " Throttle is " + gridThrottle + " Tier: " + tier);
        //startStatsMessageListener();
        statsUtil.pollStatsBrokerJobQueue();

        try {
            while (!stopIfAppropriate()) {
                if (LOGGER.isTraceEnabled()) LOGGER.trace("State while running:");

//                LOGGER.debug("Listening on: "+ remoteQueueJmsContainer.getDestinationName() +" or " + statsUtil.getQueueName(remoteQueueJmsContainer.getDestination()));
                //populate the statistics broker values
                statsUtil.pollStatsBrokerResponseQueue();
                LOGGER.debug("Worker Run() - Response Stats: " + statsUtil.getStatsMessageListener().getStats());

                statsUtil.pollStatsBrokerJobQueue();
                LOGGER.debug("Worker Run() - RequestJobQueue Stats: " + statsUtil.getStatsMessageListener().getStats());


                //check/manage remoteQueueListenerContainer
                if(gridThrottle){
                    manageRemoteQueueListenerContainer();
                }else{
                    //set the values for the statsUtil
                    updateStatsUtilJobCounts();
                }
                //progress Report
                statsUtil.displayWorkerProgress();

                //create worker is necessary
                if(canSpawnWorkers()){
                    if (isNewWorkersRequired()) {
                        createWorker();
                    }
                }
                logMessageListenerContainerState();
                if (managerTopicMessageListener.isShutdown()) {
                    LOGGER.debug("Worker Run(): Received shutdown message.  message already sent to child workers.");
                    break;
                }
                Thread.sleep(5*1000);
            }
            //stop the message listener
            remoteQueueJmsContainer.stop();
            long waitingTime=10 * 1000;
            //if shutdown is activated reduce waiting time
            if (shutdown) {
                waitingTime=1000;
            }
            //statsUtil.getStatsMessageListener().hasConsumers()
            LOGGER.debug("Worker Run(): in Shutdown mode.");
            //dont exit until the workers have completed all the tasks and the responseMonitor has completed sending response messages to the master
            while (workersHaveRunningJobs() || responseQueueListenerBusy) {
                //progress Report
                statsUtil.displayWorkerProgress();
                Thread.sleep(waitingTime);
            }
            //send shutdown message
            try {
                managerTopicMessageListener.getWorkerMessageSender().sendShutDownMessage();
            } catch (JMSException e){
                LOGGER.warn("JMSException thrown. Unable to connect to remote workers", e);
            }
            //sleep for 4 seconds and then exit
            Thread.sleep(10*1000);
            LOGGER.info("Worker Run(): completed tasks. Shutdown message sent. Stopping now.");
            //t- tier ws:workers spawned
            System.out.println(Utilities.getTimeNow() + " Worker has completed tasks -  t: " + tier + " ws: " + getNumberOfWorkersSpawned() + " jobcount: " + totalJobCount);
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException thrown by Worker.  Stopping now.", e);
        }
    }

    /**
     * Create a worker depending on the conditions specified
     */
    private void createWorker() {
        LOGGER.debug("Creating a worker.");
        final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();
        int numberOfWorkers = 0;
        if(statsUtil.getStatsMessageListener().getConsumers() > 0){
            numberOfWorkers = statsUtil.getUnfinishedJobs()/statsUtil.getStatsMessageListener().getConsumers();
        }
        if (highMemory) {
            LOGGER.debug("Starting a high memory worker.");
            workerRunnerHighMemory.startupNewWorker(priority, tcpUri, temporaryDirectoryName);
        } else {
            LOGGER.debug("Starting a  worker.");
            workerRunner.startupNewWorker(priority, tcpUri, temporaryDirectoryName);
        }
    }

    public String getNumberOfWorkersSpawned(){
        final String  numberOfWorkers;
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            return ((SubmissionWorkerRunner) this.workerRunner).getWorkerCountString();
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            return  ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).getWorkerCountString();
        }
        return "00";
    }

    /**
     * check if this worker can spawn any worker
     * @return
     */
    public boolean canSpawnWorkers(){

        boolean canSpawnWorker = false;
        if(!((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).getGridName().equals("lsf")){
            return false;
        }
        if(tier == maxTierDepth){
            return false;
        }
        switch (tier) {
            case 1:
                canSpawnWorker = true;
                break;
            case 2:
                canSpawnWorker = true;
                break;
            case 3:
                canSpawnWorker = true;
                break;
            case 4:
                canSpawnWorker = true;
                break;
            default:
                if(gridThrottle){
                    canSpawnWorker = false;
                }else{
                    canSpawnWorker = true;
                }
        }
        return  canSpawnWorker;

    }

    public void manageRemoteQueueListenerContainer(){
        statsUtil.pollStatsBrokerResponseQueue();
        int responseDequeueCount =    statsUtil.getStatsMessageListener().getDequeueCount();
        if(!highMemory) {
            statsUtil.pollStatsBrokerJobQueue();
            LOGGER.debug("workersHaveRunningJobs RequestQueue Stats: " + statsUtil.getStatsMessageListener().getStats());
        }else{
            statsUtil.pollStatsBrokerHighMemJobQueue();
            LOGGER.debug("workersHaveRunningJobs High Memory RequestQueue Stats: " + statsUtil.getStatsMessageListener().getStats());
        }
        int requestEnqueueCount =    statsUtil.getStatsMessageListener().getEnqueueCount();
        //set the values for the statsUtil
        updateStatsUtilJobCounts();

        int unfinishedJobs = requestEnqueueCount - responseDequeueCount;
        LOGGER.debug("manageRemoteQueueListenerContainer - unfinishedJobs : " + unfinishedJobs);
        if(statsUtil.isStopRemoteQueueJmsContainer()){
            LOGGER.debug("manageRemoteQueueListenerContainer - Stopping remote listener ");
            remoteQueueJmsContainer.stop();
            statsUtil.setStopRemoteQueueJmsContainer(false);
        }else{
            LOGGER.debug("manageRemoteQueueListenerContainer - start remote listener if not running");
            if(!remoteQueueJmsContainer.isRunning()){
                boolean startRemoteQueue = false;
                switch(tier){
                    case 1:
                        if(unfinishedJobs < maxUnfinishedJobs){
                            startRemoteQueue = true;
                        }
                        break;
                    case 2:
                        if(unfinishedJobs < maxUnfinishedJobs / 2){
                            startRemoteQueue = true;
                        }
                        break;
                    default:
                        if(unfinishedJobs < maxUnfinishedJobs / (Math.pow(2, tier - 1 ))){
                            startRemoteQueue = true;
                        }
                        break;
                }
                //start the remoteQueue if required
                if (startRemoteQueue){
                    LOGGER.debug("canSpawnWorkers - Start remote listener ");
                    remoteQueueJmsContainer.start();
                    statsUtil.setStopRemoteQueueJmsContainer(false);
                }
            }
        }
    }

    public void updateStatsUtilJobCounts(){
        statsUtil.setUnfinishedJobs(statsUtil.getStatsMessageListener().getEnqueueCount() - statsUtil.getStatsMessageListener().getDequeueCount());
        statsUtil.setTotalJobs(Long.valueOf(statsUtil.getStatsMessageListener().getEnqueueCount()));
    }
    /**
     *   check if child workers have running jobs
     *
     *   @return workersHaveRunningJobs
     */
    private boolean  workersHaveRunningJobs(){
        statsUtil.pollStatsBrokerResponseQueue();
        LOGGER.debug("workersHaveRunningJobs Response Stats: " + statsUtil.getStatsMessageListener().getStats());
        boolean  isResponseQueueEmpty =  (statsUtil.getStatsMessageListener().getQueueSize() == 0);
        int responseDequeueCount =    statsUtil.getStatsMessageListener().getDequeueCount();
        if(!highMemory) {
            statsUtil.pollStatsBrokerJobQueue();
            LOGGER.debug("workersHaveRunningJobs RequestQueue Stats: " + statsUtil.getStatsMessageListener().getStats());
        }else{
            statsUtil.pollStatsBrokerHighMemJobQueue();
            LOGGER.debug("workersHaveRunningJobs High Memory RequestQueue Stats: " + statsUtil.getStatsMessageListener().getStats());
        }

        boolean  islocalQueueEmpty =  (statsUtil.getStatsMessageListener().getQueueSize() == 0);
        int requestEnqueueCount =    statsUtil.getStatsMessageListener().getEnqueueCount();
        //set the values for the statsUtil
        statsUtil.setUnfinishedJobs(requestEnqueueCount - responseDequeueCount);

        statsUtil.setTotalJobs(Long.valueOf(requestEnqueueCount));
        return (requestEnqueueCount > responseDequeueCount  || ((!islocalQueueEmpty) || (!isResponseQueueEmpty)));
    }

    /**
     * statsMessageListener.newWorkersRequired = expectedCompletionTime() > Liferemaining/completionTargetFactor
     * check if number of remoteConsumers < maxConsumerSize
     * check if number of consumers <  getQueueSize()/queueConsumerRatio
     *
     * @return   newWorkersRequired
     */
    private boolean isNewWorkersRequired() {
        statsUtil.pollStatsBrokerJobQueue();
        final StatsMessageListener statsMessageListener = statsUtil.getStatsMessageListener();
        LOGGER.debug("New worker Required - RequestQueue Stats: " + statsMessageListener.getStats());

        boolean quickSpawnMode = false;
        if(statsMessageListener.getConsumers() > 0){
            quickSpawnMode =  ((statsMessageListener.getQueueSize()/ statsMessageListener.getConsumers()) > 4);
        }
        LOGGER.debug("New worker Required  quickSpawnMode: " + quickSpawnMode);
        return (statsMessageListener.newWorkersRequired((int) (lifeRemaining() / completionFactor)) &&
                (statsMessageListener.getConsumers() < maxConsumerSize) &&
                (statsMessageListener.getConsumers() < statsMessageListener.getQueueSize() / queueConsumerRatio))
                || (quickSpawnMode);
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
            LOGGER.info("This worker has been set to receive messages with highMemory: " + highMemory + " AND  priority >= " + priority);
        }
        messageSelector.append(" AND JMSPriority >= ").append(priority);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Message selector: '" + messageSelector + "'");
        }
        remoteQueueJmsContainer.setMessageSelector(messageSelector.toString());
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
            if (remoteQueueJmsContainer == null) {
                LOGGER.trace("DefaultMessageListenerContainer is null.");
            } else {
                StringBuffer buf = new StringBuffer("DefaultMessageListenerContainer state:\n\n");
                buf.append("isRunning: ").append(remoteQueueJmsContainer.isRunning()).append("\n");
                buf.append("isActive: ").append(remoteQueueJmsContainer.isActive()).append("\n");
                buf.append("getDestinationName: ").append(remoteQueueJmsContainer.getDestinationName()).append("\n");
                buf.append("isRegisteredWithDestination: ").append(remoteQueueJmsContainer.isRegisteredWithDestination()).append("\n");
                buf.append("getActiveConsumerCount: ").append(remoteQueueJmsContainer.getActiveConsumerCount()).append("\n");
                buf.append("getCacheLevel: ").append(remoteQueueJmsContainer.getCacheLevel()).append("\n");
                buf.append("getConcurrentConsumers: ").append(remoteQueueJmsContainer.getConcurrentConsumers()).append("\n");
                buf.append("getIdleConsumerLimit: ").append(remoteQueueJmsContainer.getIdleConsumerLimit()).append("\n");
                buf.append("getIdleTaskExecutionLimit: ").append(remoteQueueJmsContainer.getIdleTaskExecutionLimit()).append("\n");
                buf.append("getMaxConcurrentConsumers: ").append(remoteQueueJmsContainer.getMaxConcurrentConsumers()).append("\n");
                buf.append("getScheduledConsumerCount: ").append(remoteQueueJmsContainer.getScheduledConsumerCount()).append("\n");
                buf.append("getClientId: ").append(remoteQueueJmsContainer.getClientId()).append("\n");
                buf.append("getDurableSubscriptionName: ").append(remoteQueueJmsContainer.getDurableSubscriptionName()).append("\n");
                buf.append("getMessageSelector: ").append(remoteQueueJmsContainer.getMessageSelector()).append("\n");
                buf.append("getPausedTaskCount: ").append(remoteQueueJmsContainer.getPausedTaskCount()).append("\n");
                buf.append("getPhase: ").append(remoteQueueJmsContainer.getPhase()).append("\n");
                buf.append("getSessionAcknowledgeMode: ").append(remoteQueueJmsContainer.getSessionAcknowledgeMode()).append("\n");
                buf.append("isAcceptMessagesWhileStopping: ").append(remoteQueueJmsContainer.isAcceptMessagesWhileStopping()).append("\n");
                buf.append("isAutoStartup: ").append(remoteQueueJmsContainer.isAutoStartup()).append("\n");
                buf.append("isExposeListenerSession: ").append(remoteQueueJmsContainer.isExposeListenerSession()).append("\n");
                buf.append("isPubSubDomain: ").append(remoteQueueJmsContainer.isPubSubDomain()).append("\n");
                buf.append("isSessionTransacted: ").append(remoteQueueJmsContainer.isSessionTransacted()).append("\n");
                buf.append("isSubscriptionDurable: ").append(remoteQueueJmsContainer.isSubscriptionDurable()).append("\n");
                LOGGER.trace(buf);
            }
        }
    }


    /**
     * sets the masterUri  and configures the remote connection on this worker
     * - also sets the configuration for the master worker
     * - then finally starts the message listener on this worker
     *
     * @param masterUri
     */
    public void setMasterUri(String masterUri) {
        this.masterUri = masterUri;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Master URI passed in to Controller: " + masterUri);
        }
        if (remoteJmsTemplate == null) {
            throw new IllegalStateException("This DistributeWorkerController does not have a reference to the JmsTemplateWrapper, needed to configure the connection.");
        }

        final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(masterUri);

        activeMQConnectionFactory.setOptimizeAcknowledge(true);
        activeMQConnectionFactory.setUseCompression(true);
        activeMQConnectionFactory.setAlwaysSessionAsync(false);
        activeMQConnectionFactory.getPrefetchPolicy().setQueuePrefetch(0);

        //set the RedeliveryPolicy
        RedeliveryPolicy queuePolicy =  activeMQConnectionFactory.getRedeliveryPolicy();
        queuePolicy.setInitialRedeliveryDelay(0);
        queuePolicy.setRedeliveryDelay(1*1000);
        queuePolicy.setUseExponentialBackOff(false);
        queuePolicy.setMaximumRedeliveries(4);

        activeMQConnectionFactory.setRedeliveryPolicy(queuePolicy);

        activeMQConnectionFactory.setTransportListener(JMSTransportListener);
        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory(activeMQConnectionFactory);
        connectionFactory.setSessionCacheSize(100);

        remoteQueueJmsContainer.setConnectionFactory(connectionFactory);
        managerTopicMessageListenerJmsContainer.setConnectionFactory(connectionFactory);

        LOGGER.debug("Set remoteJMS template " );
        final JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        //The new JMS Template with the new broker URI needs to be passed to both the response listener and the controller itself.

        setRemoteJmsTemplate(jmsTemplate); // may not be necessary
        //set the remoteJmsTemplates for the responseQueue
        responseQueueMessageListener.setRemoteJmsTemplate(jmsTemplate);
        //((WorkerMessageSenderImpl) responseQueueMessageListener.getWorkerMessageSender()).setRemoteJmsTemplate(jmsTemplate);
        //set the remoteJmsTemplate for the workerMessageSender
//        remoteQueueJmsContainer.initialize();
        if (highMemory && masterWorker) {
            LOGGER.debug("High Memory Remote Worker setup ***");
            if (highMemJobRequestQueue != null) {
                remoteQueueJmsContainer.setDestination(highMemJobRequestQueue);
                LOGGER.debug("Worker: masterworker - this worker is a child of the master and queue set to highMemJobRequestQueue");
            } else {
                throw new IllegalStateException("The highMemJobRequestQueue can not be null .");
            }
        }
        //start the listeners
        remoteQueueJmsContainer.start();
        managerTopicMessageListenerJmsContainer.start();


        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MessageListenerContainer started, connected to: " + masterUri);
        }
    }


    /**
     *
     * @return
     */
    public void handleLostCopnnections(){


    }

    public String whoAmI(){
        String myIdentity;
        String workerType = highMemory ? "hm" : "nw";
        if (projectId != null) {
            return projectId + "_" + masterUri.hashCode() + "_" + workerType;
        } else {
            return "worker_unid" + "_" + workerType;
        }
    }
}



