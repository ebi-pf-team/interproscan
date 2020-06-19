package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.pool.PooledConnectionFactory;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import uk.ac.ebi.interpro.scan.io.TemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.jms.activemq.JMSExceptionListener;
import uk.ac.ebi.interpro.scan.jms.activemq.JMSTransportListener;
import uk.ac.ebi.interpro.scan.jms.activemq.MasterMessageSenderImpl;
import uk.ac.ebi.interpro.scan.jms.master.ClusterState;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.SubmissionWorkerRunner;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.WorkerRunner;
import uk.ac.ebi.interpro.scan.jms.monitoring.*;
import uk.ac.ebi.interpro.scan.jms.stats.StatsMessageListener;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.*;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;



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

    private static final Logger LOGGER = LogManager.getLogger(WorkerImpl.class.getName());

    private long lastMessageFinishedTime = System.currentTimeMillis(); //new Date().getTime();

    private final List<String> runningJobs = new ArrayList<String>();

    private int totalJobCount = 0;
    private AtomicInteger inVmWorkerCount = new AtomicInteger(0);
    private int concurrentInVmWorkerCount;

    private int maxConcurrentInVmWorkerCount;

    private boolean responseQueueListenerBusy = false;

    private final Object jobListLock = new Object();

    private DefaultMessageListenerContainer remoteQueueJmsContainer;
    private DefaultMessageListenerContainer statsListenerContainer;
    private DefaultMessageListenerContainer managerTopicMessageListenerJmsContainer;

    private ResponseQueueMessageListener responseQueueMessageListener;
    private WorkerMonitorQueueListener workerMonitorQueueListener;
    private ManagerTopicMessageListener managerTopicMessageListener;

    private WorkerMessageSender workerMessageSender;

    private boolean shutdown = false;


    private StatsUtil statsUtil;

    private JMSTransportListener jmsTransportListener;
    private JMSExceptionListener jmsExceptionListener;


    private Destination statsQueue;
    /* Local job request queue */
    private Destination jobRequestQueue;
    private Destination highMemJobRequestQueue;
    private Destination workerManagerTopic;

    private Destination jobResponseQueue;

    private Destination systemMonitorQueue;

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

    private long currentMasterClockTime;
    private long currentMasterlifeSpanRemaining;

    private double lifeTimeContingency = 0.3;

    private int completionFactor = 20;
    private int maxConsumerSize = 40;
    private int queueConsumerRatio = 99;

    protected JmsTemplate localJmsTemplate;
    protected JmsTemplate remoteJmsTemplate;
    private JmsTemplate jmsTopicTemplate;

    protected final String STATS_BROKER = "ActiveMQ.Statistics.Destination.";

    private WorkerRunner workerRunner;
    private WorkerRunner workerRunnerHighMemory;

    private String projectId;

    private String logDir;

    private int tier = 1;

    private int maxTierDepth = 1;

    private boolean stopRemoteQueueJmsContainer = false;

    private boolean gridThrottle = true;

    private String gridName;

    private TemporaryDirectoryManager temporaryDirectoryManager;

    private int maxUnfinishedJobs;

    protected WorkerState workerState;

    private boolean verboseLog = false;

    private int verboseLogLevel = 0;

    private TimeKeeper timeKeeper;

    private ClusterState clusterState;

    protected int gridCheckInterval = 60; //seconds

    Long timeLastUpdatedClusterState = System.currentTimeMillis();

    private boolean jmsRelatedExceptionReceived = false;

    private int jmsRelatedExceptionCount = 0;

    private int queuePrefetchLimit = 0;

    public int sequenceCount = 0;

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

        //initiallise the JobMonitor
        //this.workerState = new WorkerState(111, UUID.fromString(tcpUri), tcpUri,false);
    }


    public int getQueuePrefetchLimit() {
        return queuePrefetchLimit;
    }

    @Required
    public void setQueuePrefetchLimit(int queuePrefetchLimit) {
        this.queuePrefetchLimit = queuePrefetchLimit;
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
     *   Set the time for the submission worker runner
     */
    public void setSubmissionWorkerRunnerClockTime(){
        final long currentClockTime = System.currentTimeMillis();
        final long lifeRemaining =  maximumLifeMillis - (currentClockTime - startUpTime);
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner) this.workerRunner).setCurrentMasterClockTime(currentClockTime);
            ((SubmissionWorkerRunner) this.workerRunner).setLifeSpanRemaining(lifeRemaining);
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setCurrentMasterClockTime(currentClockTime);
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setLifeSpanRemaining(lifeRemaining);
        }
    }


    public void setSubmissionWorkerLogDir(String logDir){
        //set this as soon as the masters starts running
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner) this.workerRunner).setLogDir(logDir);
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setLogDir(logDir);
        }
    }

    /**
     * update the clustestate in the submission worker runner
     * @param clusterState
     */
    public void setSubmissionWorkerClusterState(ClusterState clusterState){
        //set this as soon as the masters starts running
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner) this.workerRunner).setClusterState(clusterState);
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).setClusterState(clusterState);
        }
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
    public void setWorkerMonitorQueueListener(WorkerMonitorQueueListener workerMonitorQueueListener) {
        this.workerMonitorQueueListener = workerMonitorQueueListener;
    }

    @Required
    public void setWorkerMessageSender(WorkerMessageSender workerMessageSender) {
        this.workerMessageSender = workerMessageSender;
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
    public void setManagerTopicMessageListener(ManagerTopicMessageListener managerTopicMessageListener) {
        this.managerTopicMessageListener = managerTopicMessageListener;
    }

    public void setJmsExceptionListener(JMSExceptionListener jmsExceptionListener) {
        this.jmsExceptionListener = jmsExceptionListener;
    }

    @Required
    public void setWorkerRunnerHighMemory(WorkerRunner workerRunnerHighMemory) {
        this.workerRunnerHighMemory = workerRunnerHighMemory;
    }

    public void setQueueConsumerRatio(int queueConsumerRatio) {
        this.queueConsumerRatio = queueConsumerRatio;
    }

    @Required
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

    public void setSystemMonitorQueue(Destination systemMonitorQueue) {
        this.systemMonitorQueue = systemMonitorQueue;
    }

    public void setMaximumIdleTimeSeconds(Long maximumIdleTime) {
        this.maximumIdleTimeMillis = maximumIdleTime * 1000;
    }

    public void setMaximumLifeSeconds(Long maximumLife) {
        this.maximumLifeMillis = maximumLife * 1000;

    }

    public long getMaximumLifeMillis() {
        return maximumLifeMillis;
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
    public void setJmsTopicTemplate(JmsTemplate jmsTopicTemplate) {
        this.jmsTopicTemplate = jmsTopicTemplate;
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

    @Required
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
    public void setJmsTransportListener(JMSTransportListener jmsTransportListener) {
        this.jmsTransportListener = jmsTransportListener;
    }


    public int getInVmWorkeNumber() {
        return inVmWorkerCount.incrementAndGet();
    }

    public int getConcurrentInVmWorkerCount() {
        return concurrentInVmWorkerCount;
    }

    @Required
    public void setConcurrentInVmWorkerCount(int concurrentInVmWorkerCount) {
        this.concurrentInVmWorkerCount = concurrentInVmWorkerCount;
    }

    public int getMaxConcurrentInVmWorkerCount() {
        return maxConcurrentInVmWorkerCount;
    }

    @Required
    public void setMaxConcurrentInVmWorkerCount(int maxConcurrentInVmWorkerCount) {
        this.maxConcurrentInVmWorkerCount = maxConcurrentInVmWorkerCount;
    }

    public long getCurrentMasterClockTime() {
        return currentMasterClockTime;
    }

    public void setCurrentMasterClockTime(long currentMasterClockTime) {
        this.currentMasterClockTime = currentMasterClockTime;
    }

    public long getCurrentMasterlifeSpanRemaining() {
        return currentMasterlifeSpanRemaining;
    }

    public void setCurrentMasterlifeSpanRemaining(long currentMasterlifeSpanRemaining) {
        this.currentMasterlifeSpanRemaining = currentMasterlifeSpanRemaining;
    }

    public boolean isVerboseLog() {
        return verboseLog;
    }

    public void setVerboseLog(boolean verboseLog) {
        this.verboseLog = verboseLog;
    }

    public int getVerboseLogLevel() {
        return verboseLogLevel;
    }

    public void setVerboseLogLevel(int verboseLogLevel) {
        this.verboseLogLevel = verboseLogLevel;
    }

    public String getGridName() {
        return gridName;
    }

    @Required
    public void setGridName(String gridName) {
        this.gridName = gridName;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }


    public void setWorkerState(WorkerState workerState) {
        this.workerState = workerState;
    }

    public void setTimeKeeper(TimeKeeper timeKeeper) {
        this.timeKeeper = timeKeeper;
    }

    public ClusterState getClusterState() {
        return clusterState;
    }

    public void setClusterState(ClusterState clusterState) {
        this.clusterState = clusterState;
    }

    public int getGridCheckInterval() {
        return gridCheckInterval;
    }

    public void setGridCheckInterval(int gridCheckInterval) {
        this.gridCheckInterval = gridCheckInterval;
    }

    public int getSequenceCount() {
        return sequenceCount;
    }

    public void setSequenceCount(int sequenceCount) {
        this.sequenceCount = sequenceCount;
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

    /**
     * update worker state to show job has been processed and
     * record that a response has been sent to the master
     *
     */
    public void removeJobFromWorkerState(Message message){
        workerState.removeFromNonFinishedJobs(message);
    }

    /**
     * lifespan remaining =  maximumLifeMillis - (System.currentTimeMillis() - startUpTime)
     *
     * @return
     */
    private Long lifeRemaining() {
        return maximumLifeMillis - (System.currentTimeMillis() - startUpTime);
    }

    /**
     *
     * @return  exceededLifespan
     */
    private boolean exceededLifespan(){
        final long now = System.currentTimeMillis();
        final boolean exceededLifespan = lifeRemaining() < (maximumLifeMillis * lifeTimeContingency);
        return exceededLifespan;
    }

    /**
     *
     * @return   exceededIdleTime
     */
    private boolean exceededIdleTime(){
        final long now = System.currentTimeMillis();
        final boolean exceededIdleTime = (now - lastMessageFinishedTime) > maximumIdleTimeMillis;
        return exceededIdleTime;
    }
    /**
     * worker life is 20* less than that of the master
     *  -- this makes sure that we have workers who can finish their jobs and die and other workers created in their place if
     * needed.
     * -- we also make sure that the master always outlives the workers
     */
    private void setMaximumLifeUsingMasterClockTimes(){
        long remainingLifeForMaster = currentMasterlifeSpanRemaining - (System.currentTimeMillis() - currentMasterClockTime);
        if(maximumLifeMillis > remainingLifeForMaster){
            long idealWorkerLifeSpan = (long) (remainingLifeForMaster * 0.7);
            setMaximumLifeSeconds(Long.valueOf(idealWorkerLifeSpan));
        }
    }

    public boolean stopIfAppropriate() {
        synchronized (jobListLock) {
            final long now = System.currentTimeMillis();
            //start shutting down when you have 20* of lifespan remaining
            final boolean exceededLifespan = exceededLifespan();
            final boolean exceededIdleTime = exceededIdleTime();
            LOGGER.debug("Now: "+ now+ " lastMessageFinished: " + lastMessageFinishedTime +" IdleTime: " +(now - lastMessageFinishedTime) + " maxIdleTime: " + maximumIdleTimeMillis);

            //if exceededIdleTime check if workers have running jobs
            boolean thisWorkerIsIdle = runningJobs.size() == 0 && exceededIdleTime;
            boolean childWorkersAreIdle = ! workersHaveRunningJobs();
            if(verboseLogLevel > 4){
                Utilities.verboseLog(1100, "lastMessageFinished: "      + lastMessageFinishedTime
                        +" IdleTime: "  +(now - lastMessageFinishedTime)
                        + " maxIdleTime: "  + maximumIdleTimeMillis);
                Utilities.verboseLog(1100, "thisWorkerIsIdle : " + thisWorkerIsIdle
                        + " childWorkersAreIdle : " + childWorkersAreIdle);
            }
            if (exceededLifespan
                    || (thisWorkerIsIdle && childWorkersAreIdle)) {
                if (LOGGER.isInfoEnabled()) {
                    if (exceededLifespan) {
                        LOGGER.info("Stopping worker as exceeded maximum life span, lifespan:" + maximumLifeMillis + " lifespan remaming: " + lifeRemaining());
                    } else {
                        LOGGER.info("Stopping worker as idle for longer than max idle time, idle time: " + (now - lastMessageFinishedTime));
                    }
                }
                remoteQueueJmsContainer.stop();
                return true;
            }
            if (managerTopicMessageListener.isShutdown()){
                LOGGER.info("stopIfAppropriate():  worker has received shutdown message from master ");
                shutdown = true;
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
//        Utilities.verboseLog(Utilities.getTimeNow() + " Running InterProScan worker  ...");
        LOGGER.debug("Running InterProScan worker run() ... whoAmI: " + whoAmI() + " Throttle is " + gridThrottle + " Tier: " + tier);

        //start the timekeepr, it will cause the worker to exit at the expiry of lifespan
        timeKeeper.start(startUpTime, maximumLifeMillis);

        Utilities.verboseLog = verboseLog;
        Utilities.verboseLogLevel = verboseLogLevel;
        Utilities.mode = "distributedWorker";

        if(verboseLogLevel > 2){
            Utilities.verboseLog(1100, "inVmWorkers min:" + getConcurrentInVmWorkerCount() + " max: " + getMaxConcurrentInVmWorkerCount());
            Utilities.verboseLog(1100, "DEBUG " + "Available processors: " + Runtime.getRuntime().availableProcessors());
            Utilities.verboseLog(1100, "DEBUG " + "master URL: " + tcpUri + " Tier: " + tier);
        }

        //setup connection to master
        Thread configureMasterBrokerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LOGGER.info("Started configureMasterBrokerConnection");
                    boolean connecctedToMasterBroker = configureMasterBrokerConnection();
                    LOGGER.info("Finished configureMasterBrokerConnection");
                } catch (Exception e) {
                    LOGGER.error("configureMasterBrokerConnection exception", e);
                }
            }
        });
        configureMasterBrokerThread.start();
        long masterBrokerStartUpTime = maximumIdleTimeMillis / 2;
        long endTimeMillis = System.currentTimeMillis() + masterBrokerStartUpTime;
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Wait for configureMasterBrokerConnection to finish, wait time:" + masterBrokerStartUpTime);
        }
        while (configureMasterBrokerThread.isAlive()) {
            if (System.currentTimeMillis() > endTimeMillis) {
                LOGGER.warn("configureMasterBrokerConnection did not finish in time (" + masterBrokerStartUpTime + ")ms. It will run in vain.");
                System.exit(0);
            }
            try {
                Thread.sleep(50);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(Utilities.getTimeNow() + " Worker run() - configureMasterBrokerConnection thread.isAlive()");
                }
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        if(isVerboseLog()){
            Utilities.verboseLog(1100, "Worker run() main loop");
        }
        //initiallise the WorkerStateMonitor
        initialWorkerState();
        if (LOGGER.isDebugEnabled()) {
            if (statsUtil != null){
                LOGGER.debug("StatsUtil is okay");
            }
            else{
                LOGGER.debug("StatsUtil is not okay, it is null");
            }
        }
        statsUtil.pollStatsBrokerJobQueue();
        LOGGER.debug("check statutils");

        //set the consumer ratio
        if(queueConsumerRatio == 99 || queueConsumerRatio == 0){
            queueConsumerRatio = (maxConcurrentInVmWorkerCount * 2) - tier;
        }else{
            queueConsumerRatio = queueConsumerRatio / tier;
        }
        //we want avoid creating workers that do nothing
        if(queueConsumerRatio < maxConcurrentInVmWorkerCount){
            queueConsumerRatio = maxConcurrentInVmWorkerCount
                    +  maxConcurrentInVmWorkerCount / 2;
        }
        try {
            Long displayWorkerStateTime = System.currentTimeMillis();

            while (!stopIfAppropriate()) {
                if (LOGGER.isTraceEnabled()) LOGGER.trace("State while running:");
                if(verboseLog){

                    Long timeSinceLastDisplayWorkerState = System.currentTimeMillis() - displayWorkerStateTime;
                    if(verboseLogLevel > 5){
                        Utilities.verboseLog(1100, "Worker : timeSinceLastDisplayWorkerState - "
                            + TimeUnit.MILLISECONDS.toSeconds(timeSinceLastDisplayWorkerState) + "s");
                    }
                    if( TimeUnit.MILLISECONDS.toSeconds(timeSinceLastDisplayWorkerState) > 20 * 60) {
                        sendWorkerStateMessage(workerState);
//                        Utilities.verboseLog(1100, "Worker :  sendWorkerStateMessage(workerState) - dummy");
                        displayWorkerStateTime = System.currentTimeMillis();
                    }
                }
//                LOGGER.debug("Listening on: "+ remoteQueueJmsContainer.getDestinationName() +" or " + statsUtil.getQueueName(remoteQueueJmsContainer.getDestination()));
                //populate the statistics broker values
                statsUtil.pollStatsBrokerResponseQueue();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Worker Run() - Response Stats: " + statsUtil.getStatsMessageListener().getStats());
                }
                if(verboseLogLevel > 5){
                    Utilities.verboseLog(1100, "Worker response stats ");
                    Utilities.verboseLog(1100, "Worker Run() - Response Stats: " + statsUtil.getStatsMessageListener().getStats());
                }
                statsUtil.pollStatsBrokerJobQueue();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Worker Run() - RequestJobQueue Stats: " + statsUtil.getStatsMessageListener().getStats());
                }
                if(verboseLogLevel > 5){
                    Utilities.verboseLog(1100, "Worker job request stats ");
                    Utilities.verboseLog(1100, "Worker Run() - RequestJobQueue Stats: " + statsUtil.getStatsMessageListener().getStats());
                }

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
                    if(verboseLogLevel > 5){
                        Utilities.verboseLog(1100, " Worker run() check if new worker is required");
                    }
                    if (isNewWorkersRequired()) {
                        createWorker();
                    }
                }
                logMessageListenerContainerState();
                if (managerTopicMessageListener.isShutdown()) {
                    LOGGER.debug("Worker Run(): Received shutdown message.  message already sent to child workers.");
                    shutdown = true;
                    break;
                }
                Thread.sleep(2 * 1000);

            }
            //shutdown the message listener container
            LOGGER.debug("Worker Run(): shutdown the message listener container");
            // using shutdown() and not stop() as stop() still allows the container to receive messages afterwards

            //TODO check, maybe this shutdown of the container is not necessary
	    // may be use stop
            
            if (! shutdown){
                long startContainerShutdown = System.currentTimeMillis();

                //remoteQueueJmsContainer.shutdown();
                remoteQueueJmsContainer.stop();
	        long timeToShutdownContainer = System.currentTimeMillis() - startContainerShutdown;
                Utilities.verboseLog(1100, "timeToShutdownContainer: using stop()" + timeToShutdownContainer );
            }
            long waitingTime = 10 * 1000;
            //if shutdown is activated reduce waiting time
            if (shutdown) {
                waitingTime = 1000;
            }
            //send shutdown message if not done so already
            if (! managerTopicMessageListener.isShutdown()){
                sendShutdownMessage();
            }

            //statsUtil.getStatsMessageListener().hasConsumers()
            LOGGER.debug("Worker Run(): in Shutdown mode.");

            while ((! managerTopicMessageListener.isShutdown()) &&
                    (workersHaveRunningJobs() || responseQueueListenerBusy || runningJobs.size() > 0 )) {
                //
                LOGGER.debug("workersHaveRunningJobs() may be true"
                        + " responseQueueListenerBusy : " +  responseQueueListenerBusy
                        + " runningJobs.size() " + runningJobs.size() );
                //progress Report
                if(lifeRemaining() < 5 * 1000){
                    LOGGER.warn("The worker has exceeded its lifespan,  life remaining: " + lifeRemaining() + "ms" );
                    workerState.setWorkerStatus("EXITED");
                    sendWorkerStateMessage(workerState);
                    Long timeAlive = System.currentTimeMillis() - startUpTime;
                    if(verboseLogLevel > 5){
                        Utilities.verboseLog(1100, " Worker been alive " + String.format("%d min, %d sec",
                                TimeUnit.MILLISECONDS.toMinutes(timeAlive),
                                TimeUnit.MILLISECONDS.toSeconds(timeAlive) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeAlive))));
                    }
                    throw new IllegalStateException("The worker has exceeded its lifespan.");
                }
                Thread.sleep(waitingTime);
                statsUtil.displayWorkerProgress();
                if(remoteQueueJmsContainer.isRunning()){
                    remoteQueueJmsContainer.shutdown();
                }
            }
            LOGGER.info("Worker Run(): completed tasks. Shutdown message sent. Stopping now.");

            //t- tier ws:workers spawned
            Long timeAlive = System.currentTimeMillis() - startUpTime;
            if(verboseLogLevel > 5){
                Utilities.verboseLog(1100, " Worker has completed tasks -  t: " + tier + " ws: " + getNumberOfWorkersSpawnedString() + " jobcount: " + totalJobCount);
                Utilities.verboseLog(1100, " Worker been alive ("
                        + TimeUnit.MILLISECONDS.toSeconds(timeAlive) + ") "
                        + String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes(timeAlive),
                        TimeUnit.MILLISECONDS.toSeconds(timeAlive) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeAlive))));
            }
            statsUtil.displayFinalWorkerProgress();

        } catch (InterruptedException e) {
            LOGGER.fatal("InterruptedException thrown by Worker.  Stopping now.", e);
            System.exit(999);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.fatal("Exception thrown. The worker will now exit with a non-zero exit status.");
            System.exit(999);
        }
        //exit the worker
        Long timeAlive = System.currentTimeMillis() - startUpTime;
        workerState.setTimeAliveMillis(timeAlive);
        workerState.setWorkerStatus("COMPLETED");
        sendWorkerStateMessage(workerState);
        Utilities.verboseLog(workerState.toString());
        System.exit(0);
    }

    /**
     * send shutdown message to the workers
     *
     */
    public void sendShutdownMessage(){
        //send shutdown message
        try {
            if(Utilities.verboseLogLevel > 2){
                Utilities.verboseLog(1100, "Send shutdown message to workers");
            }
            //workerMessageSender.sendShutDownMessage();
            jmsTopicTemplate.send(workerManagerTopic, new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(new Shutdown());
                }
            });
        } catch (Exception e){
            LOGGER.warn("Exception thrown while sending shutdown message");
            e.printStackTrace();
        }
    }

    public void sendWorkerStateMessage(final WorkerState workerState){
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Attempting to send message to queue: " + systemMonitorQueue);
        }
        if(verboseLogLevel > 5){
            Utilities.verboseLog(1100, "Attempting to send message to queue: " + systemMonitorQueue);
        }

        try {
            if (!remoteJmsTemplate.isExplicitQosEnabled()) {
                throw new IllegalStateException("It is not possible to set the priority of the JMS message, as the JMSTemplate does not have explicitQosEnabled.");
            }

            remoteJmsTemplate.send(systemMonitorQueue, new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    final ObjectMessage message = session.createObjectMessage(workerState);
                    message.setStringProperty("MESSAGE_PROPERTY", "workerState");
                    return  message;
                }
            });
        }catch (Exception e){
            LOGGER.warn("Exception thrown while sending workerstate message");
            e.printStackTrace();
        }
        //return true
    }

    /**
     * exit interproscan 5  worker
     * @param status
     */
    public void systemExit(int status){
        //wait for 20 seconds before shutting to get the stats from the remaining workers
        try {
            sendShutdownMessage();
            Thread.sleep(1 * 20 * 1000);
            Long timeAlive = System.currentTimeMillis() - startUpTime;
            workerState.setTimeAliveMillis(timeAlive);
            workerState.setWorkerStatus("COMPLETED");
            sendWorkerStateMessage(workerState);
            System.out.println(workerState.toString());
            LOGGER.debug("Ending WorkerImpl run");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }finally{
            //always exit
            if(status != 0){
                System.err.println("Interproscan worker failed. Exception thrown by WorkerImpl. Check the log file for details");
            }
            System.exit(status);
        }
        System.exit(status);
    }

    public synchronized void handleFailure(String className){
        jmsRelatedExceptionReceived = true;
        jmsRelatedExceptionCount ++;
        Utilities.verboseLog(1100, "Handle JMSExceptions (" + className + "): #" + jmsRelatedExceptionCount);
    }
    /**
     * Create a worker depending on the conditions specified
     */
    private void createWorker() {
        LOGGER.debug("Creating a worker.");
        final String temporaryDirectoryName = (temporaryDirectoryManager == null) ? null : temporaryDirectoryManager.getReplacement();
        int numberOfNewWorkers = 1;
        if(statsUtil.getStatsMessageListener().getConsumers() > 0){
            final StatsMessageListener statsMessageListener = statsUtil.getStatsMessageListener();
            int currentRemoteWorkersCount = statsMessageListener.getConsumers() - maxConcurrentInVmWorkerCount;

            int idealRemoteWorkersCount =  statsMessageListener.getQueueSize() /  queueConsumerRatio;
            if(idealRemoteWorkersCount > currentRemoteWorkersCount ){
                numberOfNewWorkers = idealRemoteWorkersCount - currentRemoteWorkersCount;
                if(numberOfNewWorkers + currentRemoteWorkersCount > maxConsumerSize){
                    numberOfNewWorkers = maxConsumerSize -currentRemoteWorkersCount;
                }
            }
            if(verboseLog){
//            numberOfNewWorkers = statsUtil.getUnfinishedJobs()/statsUtil.getStatsMessageListener().getConsumers();
                if(verboseLogLevel > 5){
                    Utilities.verboseLog(1100, "worker status: "
                            + " tier: " + tier
                            + " maxConsumerSize : " + maxConsumerSize
                            + " currentRemoteWorkersCount " + currentRemoteWorkersCount
                            + " queueConsumerRatio: " + queueConsumerRatio
                            + " idealRemoteWorkersCount: " + idealRemoteWorkersCount
                            + " numberOfNewWorkers: " + numberOfNewWorkers);
                }
            }
        }

        setSubmissionWorkerRunnerClockTime();

        if (highMemory) {
            LOGGER.debug("Starting high memory workers.");
            workerRunnerHighMemory.startupNewWorker(priority, tcpUri, temporaryDirectoryName, numberOfNewWorkers);
        } else {
            LOGGER.debug("Starting normal workers.");
            workerRunner.startupNewWorker(priority, tcpUri, temporaryDirectoryName, numberOfNewWorkers);
        }

    }

    public String getNumberOfWorkersSpawnedString(){
        final String  numberOfWorkers;
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            return ((SubmissionWorkerRunner) this.workerRunner).getWorkerCountString();
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            return  ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).getWorkerCountString();
        }
        return "00";
    }

    public int getNumberOfWorkers(){
        final int  numberOfWorkers = 0;
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            return ((SubmissionWorkerRunner) this.workerRunner).getWorkerCount();
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            return  ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).getWorkerCount();
        }
        return numberOfWorkers;
    }

    public ClusterState getWorkerRunnerClusterState(){
        final ClusterState  clusterState = null;
        if (this.workerRunner instanceof SubmissionWorkerRunner){
            return ((SubmissionWorkerRunner) this.workerRunner).getClusterState();
        }
        if ( this.workerRunnerHighMemory  instanceof SubmissionWorkerRunner){
            return  ((SubmissionWorkerRunner)  this.workerRunnerHighMemory ).getClusterState();
        }
        return clusterState;
    }

    /**
     * check if this worker can spawn any worker
     * @return
     */
    public boolean canSpawnWorkers(){

        boolean canSpawnWorker = false;

        if(verboseLogLevel > 5){
            Utilities.verboseLog(1100, "canSpawnWorkers - gridName: "
                    +  gridName
                    + " tier : " + tier
                    + " maxTierDepth: " + maxTierDepth
                    + " lifeRemaing: " + lifeRemaining()
                    + " maximumLifeMillis: " + maximumLifeMillis
            );
        }
        if(!gridName.equals("lsf")){
            return false;
        }
        if(clusterState != null){
            Long timeSinceClusterLastUpdatedClusterState = System.currentTimeMillis()  - clusterState.getLastUpdated();
            //TODO move to a controller bean
            Utilities.verboseLog(1100, "timeSinceClusterLastUpdatedClusterState: " + timeSinceClusterLastUpdatedClusterState);
            if(timeSinceClusterLastUpdatedClusterState > 2 * gridCheckInterval * 1000){
                Utilities.verboseLog(1100, "ClusterState is not uptodate:" + clusterState.toString());
                return false;
            }
        }else{
            return false;
        }
            if(tier == maxTierDepth){
            return false;
        }
        if(lifeRemaining() < (maximumLifeMillis * 0.25) ){
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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("workersHaveRunningJobs RequestQueue Stats: " + statsUtil.getStatsMessageListener().getStats());
            }
        }else{
            statsUtil.pollStatsBrokerHighMemJobQueue();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("workersHaveRunningJobs High Memory RequestQueue Stats: " + statsUtil.getStatsMessageListener().getStats());
            }
        }
        int requestEnqueueCount =    statsUtil.getStatsMessageListener().getEnqueueCount();
        //set the values for the statsUtil
        updateStatsUtilJobCounts();

        int unfinishedJobs = requestEnqueueCount - responseDequeueCount;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("manageRemoteQueueListenerContainer - unfinishedJobs : " + unfinishedJobs);
        }
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
                        if(unfinishedJobs < maxUnfinishedJobs / (Math.pow(2, tier))){
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

    /**
     * set the unfinished jobs
     */
    public void updateStatsUtilJobCounts(){
        statsUtil.pollStatsBrokerResponseQueue();
        int responseDequeueCount =    statsUtil.getStatsMessageListener().getDequeueCount();
        statsUtil.pollStatsBrokerJobQueue();
        int requestEnqueueCount =    statsUtil.getStatsMessageListener().getEnqueueCount();
        int queueConsumerCount =    statsUtil.getStatsMessageListener().getConsumers();
        //unfinishedJobs = requestEnqueueCount - responseDequeueCount;
        statsUtil.setUnfinishedJobs(requestEnqueueCount - responseDequeueCount);
        statsUtil.setTotalJobs(Long.valueOf(requestEnqueueCount));

    }
    /**
     *   check if child workers have running jobs
     *
     *   @return workersHaveRunningJobs
     */
    private boolean  workersHaveRunningJobs(){
        statsUtil.pollStatsBrokerResponseQueue();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("workersHaveRunningJobs Response Stats: " + statsUtil.getStatsMessageListener().getStats());
        }
        boolean  isResponseQueueEmpty =  (statsUtil.getStatsMessageListener().getQueueSize() == 0);
        int responseDequeueCount =    statsUtil.getStatsMessageListener().getDequeueCount();
        if(!highMemory) {
            statsUtil.pollStatsBrokerJobQueue();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("workersHaveRunningJobs RequestQueue Stats: " + statsUtil.getStatsMessageListener().getStats());
            }
        }else{
            statsUtil.pollStatsBrokerHighMemJobQueue();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("workersHaveRunningJobs High Memory RequestQueue Stats: " + statsUtil.getStatsMessageListener().getStats());
            }
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("New worker Required - RequestQueue Stats: " + statsMessageListener.getStats());
        }

        boolean quickSpawnMode = false;
        int totalRemoteWorkerSpawned = getNumberOfWorkers();
        int consumerCount = statsMessageListener.getConsumers();
        int remoteWorkerCount = consumerCount - maxConcurrentInVmWorkerCount;
        int queueSize = statsMessageListener.getQueueSize();
        if(verboseLogLevel > 5){
            Utilities.verboseLog(1100, "isNewWorkersRequired: maxConsumerSize : "
                    +  maxConsumerSize
                    + " TotalConsumers: "  + consumerCount
                    + " QueueSize: "   + queueSize
                    + " totalRemoteWorkerSpawned: " + totalRemoteWorkerSpawned
                    + " remoteWorkerCount: " + remoteWorkerCount
                    + " queueConsumerRatio: " + queueConsumerRatio
                    + " lifeRemaining: " + lifeRemaining() );

        }
        if (consumerCount < queueSize){

            if(remoteWorkerCount > 0 ){
                quickSpawnMode =  (queueSize / remoteWorkerCount) > maxConcurrentInVmWorkerCount;
            }else{
                quickSpawnMode =  queueSize   > maxConcurrentInVmWorkerCount;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("New worker Required  quickSpawnMode: " + quickSpawnMode);
            }
            if(verboseLogLevel > 5){
                Utilities.verboseLog(1100, "isNewWorkersRequired: maxConsumerSize : "
                        +  maxConsumerSize
                        + " currentRemoteWorkersCount " + remoteWorkerCount
                        + " queueConsumerRatio: " + queueConsumerRatio
                        + " quickSpawnMode: " + quickSpawnMode
                        + " completionTargetMillis: " + lifeRemaining() / completionFactor);
            }
            return (statsMessageListener.newWorkersRequired((int) (lifeRemaining() / completionFactor)) &&
                    (consumerCount < maxConsumerSize) &&
                    (consumerCount < queueSize / queueConsumerRatio))
                    || (quickSpawnMode);
        }
        return false;
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
        LOGGER.debug("Message selector is set on this worker");
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
        //configureMasterBrokerConnection();
    }

    /**
     * configures the remote connection on this worker
     * - also sets the configuration for the master worker
     * - then finally starts the message listener on this worker
     */
    private boolean configureMasterBrokerConnection(){


        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Master URI passed in to Controller: " + masterUri);
            LOGGER.debug("Configure master broker connection");
        }
        if (remoteJmsTemplate == null) {
            throw new IllegalStateException("This DistributeWorkerController does not have a reference to the JmsTemplateWrapper, needed to configure the connection.");
        }
        int failoverTimeout = 5 * 1000;
        int maxReconnectAttempts = 5;
        final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("failover:(" + masterUri + ")"
                + "?timeout=" + failoverTimeout
                + "&maxReconnectAttempts=" + maxReconnectAttempts);

        activeMQConnectionFactory.setOptimizeAcknowledge(true);
        activeMQConnectionFactory.setUseCompression(true);
        activeMQConnectionFactory.setAlwaysSessionAsync(false);
        activeMQConnectionFactory.getPrefetchPolicy().setQueuePrefetch(getQueuePrefetchLimit()); //TODO monitor

        // <!--<property name="trustedPackages" value="uk.ac.ebi.interpro.scan.*" />-->
        List<String> trustedPackages = new ArrayList();
        trustedPackages.add("uk.ac.ebi.interpro.scan.*");
        //activeMQConnectionFactory.setTrustedPackages(trustedPackages);
        activeMQConnectionFactory.setTrustAllPackages(true);

        //set the RedeliveryPolicy
        RedeliveryPolicy queuePolicy =  activeMQConnectionFactory.getRedeliveryPolicy();
        queuePolicy.setInitialRedeliveryDelay(0);
        queuePolicy.setRedeliveryDelay(1 * 1000);
        queuePolicy.setUseExponentialBackOff(false);
        queuePolicy.setMaximumRedeliveries(4);

        activeMQConnectionFactory.setRedeliveryPolicy(queuePolicy);

        activeMQConnectionFactory.setTransportListener(jmsTransportListener);
        activeMQConnectionFactory.setExceptionListener(jmsExceptionListener);



        final CachingConnectionFactory oldCachingconnectionFactory = new CachingConnectionFactory(activeMQConnectionFactory);
        //the following works for the cachingFactory
        //connectionFactory..setSessionCacheSize(100);
        //connectionFactory.setExceptionListener(jmsExceptionListener);

        final PooledConnectionFactory connectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);

        //the following works for the pooledFactory
        connectionFactory.setMaxConnections(100);
        //connectionFactory.setExceptionListener(jmsExceptionListener);

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
        workerMonitorQueueListener.setRemoteJmsTemplate(jmsTemplate);
        //set the remote jms template for the worker message sender
        workerMessageSender.setRemoteJmsTemplate(jmsTemplate);

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
        LOGGER.debug("Start the listeners on the remote broker " );

        remoteQueueJmsContainer.start();
        managerTopicMessageListenerJmsContainer.start();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MessageListenerContainer started, connected to: " + masterUri);
        }
        //check if remote is reachable
        String address = masterUri.split(":")[1].substring(2);
        int port = Integer.parseInt(masterUri.split(":")[2]);
        LOGGER.debug("address: " + address + ":  port" + port);
        if (isReachable(address, port, 5000)){
            LOGGER.debug("The master process is reachable, master url: " + masterUri +  " (" + address + ": " + port + ")");
        }else{
            LOGGER.warn("The master process is not reachable, url: " + masterUri + " (" + address + ": " + port + ")");
        }
        return true;
    }

    /**
     *  check if remote master is reachable
     * @param addr
     * @param openPort
     * @param timeOutMillis
     * @return
     */
    private static boolean isReachable(String addr, int openPort, int timeOutMillis) {
        // Any Open port on other machine
        // openPort =  22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.

        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
            }
            return true;
        } catch (IOException ex) {
            LOGGER.warn("socket connection failed:  ", ex);
            return false;
        }
    }


    /**
     * initialise the workerState
     *
     */
    public void initialWorkerState(){
        //this.workerState = new WorkerState(111, UUID.fromString(tcpUri), tcpUri,false);
        Long timeAlive = System.currentTimeMillis() - startUpTime;
        String localhostname = "";
        String fullHostName = "";
        try {
            localhostname = java.net.InetAddress.getLocalHost().getHostName();
            fullHostName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
            LOGGER.debug("localhostname: " + localhostname);
            LOGGER.debug("fullHostName: " + fullHostName);
        } catch (UnknownHostException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        workerState.setTimeAliveMillis(timeAlive);
        workerState.setHostName(localhostname);
        workerState.setTcpUri(tcpUri);
        workerState.setProjectId(projectId);
        workerState.setWorkerIdentification(UUID.randomUUID());

        workerState.setMasterTcpUri(masterUri);
        workerState.setLogDir(logDir);
        workerState.setTier(tier);
        workerState.setProcessors(Runtime.getRuntime().availableProcessors());
        workerState.setWorkersSpawned(getNumberOfWorkers());
        workerState.setWorkerStatus("RUNNING");

        StringBuffer buf = new StringBuffer("The following [ ");
        if(tcpUri == null){
            buf.append("tcpUri,");
        }
        if(masterUri == null){
            buf.append("masterUri,");
        }
        if(projectId == null){
            buf.append("projectId,");
        }
        if(logDir == null){
            buf.append("logDir,");
        }
        buf.append("] are null \n");
        Utilities.verboseLog(buf.toString());
    }

    /**
     *   error recovery ??
     *   
     * @return
     */
    public void handleLostCopnnections(){


    }

    public String whoAmI(){
        String workerType = highMemory ? "hm" : "nw";
        if (projectId != null) {
            return projectId + "_" + masterUri.hashCode() + "_" + workerType;
        } else {
            return "worker_unid" + "_" + workerType;
        }
    }
}



