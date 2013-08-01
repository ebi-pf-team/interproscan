package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import uk.ac.ebi.interpro.scan.io.TemporaryDirectoryManager;
import uk.ac.ebi.interpro.scan.jms.activemq.JMSExceptionListener;
import uk.ac.ebi.interpro.scan.jms.activemq.JMSTransportListener;
import uk.ac.ebi.interpro.scan.jms.activemq.MasterMessageSenderImpl;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.SubmissionWorkerRunner;
import uk.ac.ebi.interpro.scan.jms.master.queuejumper.platforms.WorkerRunner;
import uk.ac.ebi.interpro.scan.jms.stats.StatsMessageListener;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;
import uk.ac.ebi.interpro.scan.jms.stats.Utilities;

import javax.jms.*;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    private static final Logger LOGGER = Logger.getLogger(WorkerImpl.class.getName());

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

    private int completionFactor = 20;
    private int maxConsumerSize = 40;
    private int queueConsumerRatio = 99;

    private double lifeTimeContingency = 0.3;

    protected JmsTemplate localJmsTemplate;
    protected JmsTemplate remoteJmsTemplate;
    private JmsTemplate jmsTopicTemplate;

    protected final String STATS_BROKER = "ActiveMQ.Statistics.Destination.";

    private WorkerRunner workerRunner;
    private WorkerRunner workerRunnerHighMemory;

    private String projectId;

    private int tier = 1;

    private int maxTierDepth = 1;

    private boolean stopRemoteQueueJmsContainer = false;

    private boolean gridThrottle = true;

    private String gridName;

    private TemporaryDirectoryManager temporaryDirectoryManager;

    private int maxUnfinishedJobs;

    protected WorkerState workerState;

    boolean verboseFlag = false;

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

    public boolean isVerboseFlag() {
        return verboseFlag;
    }

    public void setVerboseFlag(boolean verboseFlag) {
        this.verboseFlag = verboseFlag;
    }


    public String getGridName() {
        return gridName;
    }

    @Required
    public void setGridName(String gridName) {
        this.gridName = gridName;
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
     * lifespan remaining =  maximumLifeMillis - (System.currentTimeMillis() - startUpTime)
     *
     * @return  lifeRemaining
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
            if (exceededLifespan || runningJobs.size() == 0 && (exceededLifespan || (exceededIdleTime && (!workersHaveRunningJobs())))) {

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

        LOGGER.warn("inVmWorkers min:" + getConcurrentInVmWorkerCount() + " max: " + getMaxConcurrentInVmWorkerCount());
        //setup connection to master
        Thread thread = new Thread(new Runnable() {
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
        thread.start();
        long masterBrokerStartUpTime = maximumIdleTimeMillis / 2;
        long endTimeMillis = System.currentTimeMillis() + masterBrokerStartUpTime;
        LOGGER.info("Wait for configureMasterBrokerConnection to finish, wait time:" + masterBrokerStartUpTime);
        while (thread.isAlive()) {
            if (System.currentTimeMillis() > endTimeMillis) {
                LOGGER.warn("configureMasterBrokerConnection did not finish in time (" + masterBrokerStartUpTime + ")ms. It will run in vain.");
                System.exit(0);
            }
            try {
                Thread.sleep(50);
                LOGGER.debug(Utilities.getTimeNow() + " Worker run() - configureMasterBrokerConnection thread.isAlive()");
            }
            catch (InterruptedException ex) {
                 ex.printStackTrace();
            }
        }
        if(isVerboseFlag()){
            System.out.println(Utilities.getTimeNow() + " Worker run() main loop");
        }
        if (statsUtil != null){
            LOGGER.debug("StatsUtil is okay");
        }else{
            LOGGER.debug("StatsUtil is not okay, it is null");
        }
        statsUtil.pollStatsBrokerJobQueue();
        LOGGER.debug("check statutils");
        try {
            while (!stopIfAppropriate()) {
                if (LOGGER.isTraceEnabled()) LOGGER.trace("State while running:");
                if(verboseFlag){
                    System.out.println(Utilities.getTimeNow() + "Worker  ");
                }
//                LOGGER.debug("Listening on: "+ remoteQueueJmsContainer.getDestinationName() +" or " + statsUtil.getQueueName(remoteQueueJmsContainer.getDestination()));
                //populate the statistics broker values
                statsUtil.pollStatsBrokerResponseQueue();
                LOGGER.debug("Worker Run() - Response Stats: " + statsUtil.getStatsMessageListener().getStats());
                if(verboseFlag){
                    System.out.println(Utilities.getTimeNow() + "Worker response stats ");
                    System.out.println("Worker Run() - Response Stats: " + statsUtil.getStatsMessageListener().getStats());
                }
                statsUtil.pollStatsBrokerJobQueue();
                LOGGER.debug("Worker Run() - RequestJobQueue Stats: " + statsUtil.getStatsMessageListener().getStats());
                if(verboseFlag){
                    System.out.println(Utilities.getTimeNow() + "Worker job request stats ");
                    System.out.println("Worker Run() - RequestJobQueue Stats: " + statsUtil.getStatsMessageListener().getStats());
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
                    if(verboseFlag){
                        System.out.println(Utilities.getTimeNow() + " Worker run() check if new worker is required");
                    }
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
            while (workersHaveRunningJobs() || runningJobs.size() > 0 || responseQueueListenerBusy) {
                //progress Report
                if(lifeRemaining() < 30 * 1000){
                    throw new IllegalStateException("The worker has exceeded its lifespan.");
                }
                statsUtil.displayWorkerProgress();
                Thread.sleep(waitingTime);
            }
            //send shutdown message
            try {
                LOGGER.debug("Send shutdown message to workers");
                //workerMessageSender.sendShutDownMessage();
                jmsTopicTemplate.send(workerManagerTopic, new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        return session.createObjectMessage();
                    }
                });
            } catch (Exception e){
                LOGGER.warn("Exception thrown while sending shutdown message");
                e.printStackTrace();
            }
            //sleep for 4 seconds and then exit
            Thread.sleep(10*1000);
            LOGGER.info("Worker Run(): completed tasks. Shutdown message sent. Stopping now.");
            //t- tier ws:workers spawned
            System.out.println(Utilities.getTimeNow() + " Worker has completed tasks -  t: " + tier + " ws: " + getNumberOfWorkersSpawnedString() + " jobcount: " + totalJobCount);
        } catch (InterruptedException e) {
            LOGGER.fatal("InterruptedException thrown by Worker.  Stopping now.", e);
            System.exit(999);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.fatal("The worker will now exit with a non-zero exit status.");
            System.exit(999);
        }
        //exit the worker

        System.exit(0);
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
            queueConsumerRatio = maxConcurrentInVmWorkerCount * 4 / tier;
            int idealRemoteWorkersCount =  statsMessageListener.getQueueSize() /  queueConsumerRatio;
            if(idealRemoteWorkersCount > currentRemoteWorkersCount ){
                numberOfNewWorkers = idealRemoteWorkersCount - currentRemoteWorkersCount;
                if(numberOfNewWorkers + currentRemoteWorkersCount > maxConsumerSize){
                    numberOfNewWorkers = maxConsumerSize -currentRemoteWorkersCount;
                }
            }
            if(verboseFlag){
//            numberOfNewWorkers = statsUtil.getUnfinishedJobs()/statsUtil.getStatsMessageListener().getConsumers();
                System.out.println("worker status: "
                    + " tier: " + tier
                    + " maxConsumerSize : " + maxConsumerSize
                    + " currentRemoteWorkersCount " + currentRemoteWorkersCount
                    + " queueConsumerRatio: " + queueConsumerRatio
                    + " idealRemoteWorkersCount: " + idealRemoteWorkersCount
                    + " numberOfNewWorkers: " + numberOfNewWorkers);
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

    /**
     * check if this worker can spawn any worker
     * @return
     */
    public boolean canSpawnWorkers(){

        boolean canSpawnWorker = false;

        if(isVerboseFlag()){
            System.out.println("canSpawnWorkers - gridName: "
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

    /**
     * set the unfinished jobs
     */
    public void updateStatsUtilJobCounts(){
        statsUtil.pollStatsBrokerResponseQueue();
        int responseDequeueCount =    statsUtil.getStatsMessageListener().getDequeueCount();
        statsUtil.pollStatsBrokerJobQueue();
        int requestEnqueueCount =    statsUtil.getStatsMessageListener().getEnqueueCount();

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
        int totalRemoteWorkerSpawned = getNumberOfWorkers();
        int consumerCount = statsMessageListener.getConsumers();
        int remoteWorkerCount = consumerCount - maxConcurrentInVmWorkerCount;
        int queueSize = statsMessageListener.getQueueSize();
        if(verboseFlag){
            System.out.println("isNewWorkersRequired: maxConsumerSize : "
                    +  maxConsumerSize
                    + " TotalConsumers: "  + consumerCount
                    + " QueueSize: "   + queueSize
                    + " totalRemoteWorkerSpawned: " + totalRemoteWorkerSpawned
                    + " remoteWorkerCount: " + remoteWorkerCount
                    + " queueConsumerRatio: " + queueConsumerRatio
                    + " lifeRemaining: " + lifeRemaining() );

        }
        if (consumerCount < queueSize){
            if(queueConsumerRatio == 99){
                queueConsumerRatio  =  maxConcurrentInVmWorkerCount * 2;
            }
            if(remoteWorkerCount > 0 ){
                quickSpawnMode =  (queueSize / remoteWorkerCount) > maxConcurrentInVmWorkerCount;
            }else{
                quickSpawnMode =  queueSize   > maxConcurrentInVmWorkerCount;
            }
            LOGGER.debug("New worker Required  quickSpawnMode: " + quickSpawnMode);
            if(verboseFlag){
                System.out.println("isNewWorkersRequired: maxConsumerSize : "
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

        final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("failover:"+masterUri);

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

        activeMQConnectionFactory.setTransportListener(jmsTransportListener);

        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory(activeMQConnectionFactory);
        connectionFactory.setSessionCacheSize(100);
        connectionFactory.setExceptionListener(jmsExceptionListener);

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
        LOGGER.debug("Start the listeners on the remote broker " );
//        try {
//            Connection connection = connectionFactory.createConnection();
//            connection.setClientID(whoAmI());
//        }catch (JMSException ex){
//            // maybe ActiveMQ is not running. Abort
//            if (ex.getLinkedException() instanceof IOException) {
//                LOGGER.error("JMS IOException: connection factory or connection problems.. aborting");
//            }else{
//                // Something seriously went wrong with the factory or connection
//                // creation. Abort the process here, as nothing can be done.
//                LOGGER.error("JMS Error: connection factory or connection problems.. aborting");
//            }
//            ex.printStackTrace();
//            return false;
////            System.exit(2);
//        }

        remoteQueueJmsContainer.start();
        managerTopicMessageListenerJmsContainer.start();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MessageListenerContainer started, connected to: " + masterUri);
        }
        return true;
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



