package uk.ac.ebi.interpro.scan.jms.stats;

import com.sun.management.HotSpotDiagnosticMXBean;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import uk.ac.ebi.interpro.scan.jms.worker.WorkerState;
import uk.ac.ebi.interpro.scan.management.model.StepInstance;
import uk.ac.ebi.interpro.scan.util.Utilities;

import javax.jms.*;
import javax.jms.Queue;
import javax.management.MBeanServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This util has several functions useful for the monitoring of the queues.
 * Its not thread safe, but should only be used in one thread
 *
 */
public class StatsUtil {

    private static final Logger LOGGER = Logger.getLogger(StatsUtil.class.getName());

    protected final String STATS_BROKER_DESTINATION = "ActiveMQ.Statistics.Destination.";

    private JmsTemplate jmsTemplate;

    private Destination jobRequestQueue;
    private Destination highMemJobRequestQueue;
    private Destination jobResponseQueue;
    private Destination workerManagerTopicQueue;
    private Destination statsQueue;

    private StatsMessageListener statsMessageListener;

    private static Long totalJobs;

    private static int unfinishedJobs;

    private int previousUnfinishedJobs = 0;

    private static boolean stopRemoteQueueJmsContainer = false;

    static AtomicBoolean RemoteQueueContainerStopped = new AtomicBoolean(false);

    private static int tier;

    private int progressCounter = 0;

    private Long progressReportTime = Long.MAX_VALUE;

    private static boolean forceDisplayProgress = false;

    static private AtomicInteger remoteJobsCompleted = new AtomicInteger(0);

    private AtomicLong remoteJobsCount = new AtomicLong(0);

    private AtomicLong totalStepInstanceCount = new AtomicLong(0);

    static private AtomicInteger localJobsCompleted = new AtomicInteger(0);

    private final List<String> runningJobs = Collections.synchronizedList(new ArrayList<String>());

    private final ConcurrentMap<Integer, String> submittedStepInstances = new ConcurrentHashMap<Integer, String> ();

    ConcurrentMap<UUID, WorkerState> workerStateMap = new ConcurrentHashMap<UUID, WorkerState>();

    private final Object jobListLock = new Object();

    private Long lastMessageReceivedTime = System.currentTimeMillis();
    private Long lastLocalMessageFinishedTime = System.currentTimeMillis();

    private long timeOfLastMemoryDisplay = System.currentTimeMillis();

    private  long startUpTime;
    private long maximumLifeMillis;

    private long currentMasterClockTime;
    private long currentMasterlifeSpanRemaining;

    int requestQueueConsumerCount = 0;

    private SystemInfo systemInfo;

    private Lock pollStatsLock = new ReentrantLock();

    public StatsUtil() {

    }

    static public int getRemoteJobsCompleted() {
        return remoteJobsCompleted.get();
    }

    static public void incRemoteJobsCompleted() {
        remoteJobsCompleted.incrementAndGet();
    }

    public static int getLocalJobsCompleted() {
        return localJobsCompleted.get();
    }

    public static void incLocalJobsCompleted() {
        localJobsCompleted.incrementAndGet();
    }

    public AtomicLong getRemoteJobsCount() {
        return remoteJobsCount;
    }

    public void setRemoteJobsCount(int remoteJobsCount) {
        this.remoteJobsCount.set(remoteJobsCount);
    }

    public AtomicLong getTotalStepInstanceCount() {
        return totalStepInstanceCount;
    }

    public void setTotalStepInstanceCount(Long totalStepInstanceCount) {
        this.totalStepInstanceCount.set(totalStepInstanceCount);
    }

    public synchronized int getTier() {
        return tier;
    }

    public synchronized  void setTier(int tier) {
        StatsUtil.tier = tier;
    }

    public synchronized boolean isStopRemoteQueueJmsContainer() {
        return stopRemoteQueueJmsContainer;
    }

    public synchronized void setStopRemoteQueueJmsContainer(boolean stopRemoteQueueJmsContainer) {
        StatsUtil.stopRemoteQueueJmsContainer = stopRemoteQueueJmsContainer;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void setJobRequestQueue(Destination jobRequestQueue) {
        this.jobRequestQueue = jobRequestQueue;
    }

    public void setHighMemJobRequestQueue(Destination highMemJobRequestQueue) {
        this.highMemJobRequestQueue = highMemJobRequestQueue;
    }

    public void setJobResponseQueue(Destination jobResponseQueue) {
        this.jobResponseQueue = jobResponseQueue;
    }

    public void setStatsQueue(Destination statsQueue) {
        this.statsQueue = statsQueue;
    }

    public void setWorkerManagerTopicQueue(Destination workerManagerTopicQueue) {
        this.workerManagerTopicQueue = workerManagerTopicQueue;
    }

    public void setStatsMessageListener(StatsMessageListener statsMessageListener) {
        this.statsMessageListener = statsMessageListener;
    }

    public StatsMessageListener getStatsMessageListener() {
        return statsMessageListener;
    }

    public synchronized  Long getTotalJobs() {
        return totalJobs;
    }

    public synchronized void setTotalJobs(Long totalJobs) {
        StatsUtil.totalJobs = totalJobs;
    }

    public synchronized int getUnfinishedJobs() {
        return unfinishedJobs;
    }

    public synchronized void setUnfinishedJobs(int unfinishedJobs) {
        StatsUtil.unfinishedJobs = unfinishedJobs;
    }

    public long getStartUpTime() {
        return startUpTime;
    }

    public void setStartUpTime(long startUpTime) {
        this.startUpTime = startUpTime;
    }

    public long getMaximumLifeMillis() {
        return maximumLifeMillis;
    }

    public void setMaximumLifeMillis(long maximumLifeMillis) {
        this.maximumLifeMillis = maximumLifeMillis;
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

    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    public Long getLastLocalMessageFinishedTime() {
        return lastLocalMessageFinishedTime;
    }

    public Long getLastMessageReceivedTime() {
        return lastMessageReceivedTime;
    }

    public void setLastMessageReceivedTime(Long lastMessageReceivedTime) {
        this.lastMessageReceivedTime = lastMessageReceivedTime;
    }

    public int getRequestQueueConsumerCount() {
        return requestQueueConsumerCount;
    }

    public void setRequestQueueConsumerCount(int requestQueueConsumerCount) {
        this.requestQueueConsumerCount = requestQueueConsumerCount;
    }

    public static boolean isForceDisplayProgress() {
        return forceDisplayProgress;
    }

    public static void setForceDisplayProgress(boolean forceDisplayProgress) {
        StatsUtil.forceDisplayProgress = forceDisplayProgress;
    }

    public ConcurrentMap getAllStepInstances() {
        return submittedStepInstances;
    }

    public int getSubmittedStepInstancesCount(){
        return submittedStepInstances.size();
    }

    public void addToSubmittedStepInstances(StepInstance stepInstance){
        Integer key = getKey(stepInstance.getId(), stepInstance.getStepId());
        submittedStepInstances.put(key, stepInstance.toString());
    }

    public void updateSubmittedStepInstances(StepInstance stepInstance){

        Integer key = getKey(stepInstance.getId(), stepInstance.getStepId());
        Utilities.verboseLog("Update submittedStepInstance: " + key + " (" + stepInstance.getStepId() +")");
        submittedStepInstances.replace(key, "Done " + stepInstance.toString());
        if(Utilities.verboseLogLevel > 2 && ! submittedStepInstances.containsKey(key)){
            Utilities.verboseLog("stepInstance key  not found in submitted stepInstances: "
                    + stepInstance.getId() + " -  " + stepInstance.getStepId());
        }
    }

    public void removeFromSubmittedStepInstances(StepInstance stepInstance){
        submittedStepInstances.remove(stepInstance.getId());
    }

    public Integer getKey(Long id, String name){
        String keyString = name + id.toString();
        return keyString.hashCode();
    }

    public void printSubmittedStepInstances(){
        Utilities.verboseLog(" submittedStepInstances:");
        Set ids = submittedStepInstances.keySet();
        Utilities.verboseLog(" submittedStepInstances:" + ids.size());
        //Collections.sort((List<Comparable>) ids);

        for(Object stepInstanceId:ids){
            Long id = (Long) stepInstanceId;
            System.out.println(id + ":" + submittedStepInstances.get(id));
        }
    }

    public void printNonAcknowledgedSubmittedStepInstances(){
        Utilities.verboseLog(" getNonAcknowledgedSubmittedStepInstances:");
        Set ids = submittedStepInstances.entrySet();
        Utilities.verboseLog(" submittedStepInstances:" + ids.size()
        );
        //Collections.sort((List<Comparable>) ids);

        for (Object entry : submittedStepInstances.entrySet()) {
            if(! ((Map.Entry<Integer, String>) entry).getValue().contains("Done")){
                System.out.println(((Map.Entry<Integer, String>) entry).getKey() + ":" + ((Map.Entry<Integer, String>) entry).getValue());
            }
        }
    }

    public Set<String> getNonAcknowledgedSubmittedStepInstances(){
        Utilities.verboseLog(" getNonAcknowledgedSubmittedStepInstances:");
        Set ids = submittedStepInstances.entrySet();
        Set<String> nonAcknowledgedStepInstances = new TreeSet<String>();
        Utilities.verboseLog(" submittedStepInstances:" + ids.size());

        for (Object entry : submittedStepInstances.entrySet()){
            if(! ((Map.Entry<Integer, String>) entry).getValue().contains("Done")){
                nonAcknowledgedStepInstances.add(((Map.Entry<Integer, String>) entry).getValue());
            }
        }
        return nonAcknowledgedStepInstances;
    }


    /**
     * Time last message was received
     */
    public void updateLastMessageReceivedTime(){
        lastMessageReceivedTime = System.currentTimeMillis();
    }

    public void jobStarted(String stepId) {
        synchronized (jobListLock) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Job " + stepId + " added to Worker.runningJobs");
            }
            runningJobs.add(stepId);
        }
    }

    public void jobFinished(String stepId) {
        synchronized (jobListLock) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Job " + stepId + " removed from Worker.runningJobs");
            }
            if (!runningJobs.remove(stepId)) {
                LOGGER.error("Worker.jobFinished(jmsMessageId) has been called with a message ID that it does not recognise: " + stepId);
            }
            lastLocalMessageFinishedTime = System.currentTimeMillis(); //new Date().getTime();
        }
    }

    //
    public void displayMemoryAndRunningJobs(){
        Long now = System.currentTimeMillis();
        Long timeSinceLastMemoryDisplay = now - timeOfLastMemoryDisplay;
        if(timeSinceLastMemoryDisplay > 5 * 1000){
            displayMemInfo();

            System.out.println(Utilities.getTimeNow() + " Current active Jobs" );
            List <String> currentRunningJobs =  getRunningJobs();
            for(String runningJob:currentRunningJobs){
                System.out.println(runningJob);
            }
            timeOfLastMemoryDisplay = System.currentTimeMillis();
        }

    }

    public void displayRunningJobs(){
        Utilities.verboseLog("Current active Jobs" );
        List <String> currentRunningJobs =  getRunningJobs();
        for(String runningJob:currentRunningJobs){
            Utilities.verboseLog(String.format("%" + 26 + "s", runningJob));
        }
    }

    public List<String> getRunningJobs() {
        synchronized(runningJobs){
            return Collections.unmodifiableList(new ArrayList<String>(runningJobs));
        }
    }

    public ConcurrentMap<UUID, WorkerState> getWorkerStateMap() {
        return workerStateMap;
    }

    /**
     * insert or replace worker state
     * @param workerState
     */
    public void updateWorkerStateMap(WorkerState workerState){
        if(workerStateMap.replace(workerState.getWorkerIdentification(), workerState) == null){
            workerStateMap.putIfAbsent(workerState.getWorkerIdentification(), workerState);

        }
    }

    /**
     * poll  the statistics broker plugin
     * @param queue
     * @return
     */

    private synchronized  boolean  pollStatsBroker(Destination queue){
        statsMessageListener.setDestination(queue);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LOGGER.debug("Setting the destination to "+getQueueName(queue) +" at " +timestamp);
        jmsTemplate.execute(STATS_BROKER_DESTINATION +  getQueueName(queue), new ProducerCallbackImpl(statsQueue));
        //wait for a second to receive the message
        try {
            Thread.sleep(3*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return  statsMessageListener.getStats()!=null;
    }

    public boolean  pollStatsBrokerJobQueue(){

        return  pollStatsBroker(jobRequestQueue);
    }

    public boolean  pollStatsBrokerHighMemJobQueue(){
        return  pollStatsBroker(highMemJobRequestQueue);
    }

    public boolean  pollStatsBrokerResponseQueue(){
        return  pollStatsBroker(jobResponseQueue);
    }

    public boolean  pollStatsBrokerTopic(){
        return  pollStatsBroker(workerManagerTopicQueue);
    }

    public String getQueueName(Destination queue) {
        try {
            if(queue!=null){
                return ((Queue) queue).getQueueName();
            }
        } catch (JMSException e) {
            e.printStackTrace();
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("There is a problem with the queue name "+queue.toString());
            }
        }
        return "*";
    }

    public String getTopicName(Destination topic) {
        try {
            if(topic!=null){
                return ((Topic) topic).getTopicName();
            }
        } catch (JMSException e) {
            e.printStackTrace();
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("There is a problem with the queue name "+topic.toString());
            }
        }
        return "*";
    }
    public  void sendMessage(){
        jmsTemplate.send(jobRequestQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage("test");
            }
        });
    }

    public  void sendhighMemMessage(){
        jmsTemplate.send(highMemJobRequestQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage("test2");
            }
        });
    }

    /**
     *  poll the topic for statistics
     * @param topic
     * @return
     */
    private boolean  pollStatsBrokerTopic(Destination topic){
        statsMessageListener.setDestination(topic);
        LOGGER.info("Setting the destination to "+getQueueName(topic));
        jmsTemplate.execute(STATS_BROKER_DESTINATION +  getTopicName(topic), new ProducerCallbackImpl(statsQueue));
        //wait for a second to receive the message
        try {
            Thread.sleep(1*500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return  statsMessageListener.getStats()!=null;
    }

    /**
     * display queue statistics for debugging
     *
     */
    public void displayQueueStatistics(){
        final boolean  requestQueueStatsAvailable = pollStatsBrokerJobQueue();
        if (!requestQueueStatsAvailable) {
            System.out.println("JobRequestQueue: not initialised");
        } else {
            System.out.println("JobRequestQueue:  " + statsMessageListener.getStats());
            setRequestQueueConsumerCount(statsMessageListener.getConsumers());
        }
        final boolean responseQueueStatsAvailable = pollStatsBrokerResponseQueue();
        if (!responseQueueStatsAvailable) {
            System.out.println("JobResponseQueue: not initialised");
        } else {
            System.out.println("JobResponseQueue:  " + statsMessageListener.getStats());
        }

    }

    /**
     * get the request queue size
     * @return
     */
    public int getRequestQueueSize(){
        final boolean  requestQueueStatsAvailable = pollStatsBrokerJobQueue();
        if (!requestQueueStatsAvailable) {
            LOGGER.warn("JobRequestQueue: not initialised");
            return -99;
        }
        setRequestQueueConsumerCount(statsMessageListener.getConsumers());
        return   statsMessageListener.getEnqueueCount() - statsMessageListener.getDispatchCount();
    }

    /**
     * get highmem queuesie
     * @return
     */
    public int getHighMemRequestQueueSize(){
        final boolean  requestQueueStatsAvailable = pollStatsBrokerHighMemJobQueue();
        if (!requestQueueStatsAvailable) {
            Utilities.verboseLog(5, "HighMemJobRequestQueue: not initialised");
            return -99;
        }
        return   statsMessageListener.getEnqueueCount() - statsMessageListener.getDispatchCount();
    }

    public void displayHighMemoryQueueStatistics(){
        final boolean  requestQueueStatsAvailable = pollStatsBrokerHighMemJobQueue();
        if (!requestQueueStatsAvailable) {
            Utilities.verboseLog(5,"JobRequestQueue: not initialised");
        } else {
            Utilities.verboseLog(5,"HighMemoryJobRequestQueue:  " + statsMessageListener.getStats());
        }

    }

    /**
     * set the unfinished jobs
     */
    public void updateStatsUtilJobCounts(){
        pollStatsBrokerResponseQueue();
        int responseDequeueCount =    statsMessageListener.getDequeueCount();
        pollStatsBrokerJobQueue();
        int requestEnqueueCount =    statsMessageListener.getEnqueueCount();

        //unfinishedJobs = requestEnqueueCount - responseDequeueCount;
        setUnfinishedJobs(requestEnqueueCount - responseDequeueCount);
        setTotalJobs((long) requestEnqueueCount);
        pollStatsBrokerJobQueue();
    }

    /**
     *   Display master job progress report based on the number of jobs left to run
     */
    public void displayMasterProgress(){
//        System.out.println("#un:to - " + unfinishedJobs + ":" + totalJobs);
        Long masterTotalJobs = totalJobs;
        if(unfinishedJobs > 0 && masterTotalJobs > 5.0){
            Double progress = (double)(masterTotalJobs - unfinishedJobs) / (double) masterTotalJobs;
//            System.out.println(" Progress:  " + progress + ":" + progressCounter + "  ");
            boolean displayProgress = false;
            Double actualProgress;
            if (progress > 0.25 && progress < 0.5 && progressCounter < 1){
                displayProgress = true;
                progressCounter = 1;
            }else if  (progress > 0.5 && progress < 0.75 && progressCounter < 2){
                displayProgress = true;
                progressCounter = 2;
            }else if  (progress > 0.75 && progress < 0.9 && progressCounter < 3){
                displayProgress = true;
                progressCounter = 3;
            }else if  (progress > 0.9 && progressCounter < 4){
                displayProgress = true;
                progressCounter = 4;
            }else if  (progress > 0.9 && progressCounter  >= 4){
                Long now = System.currentTimeMillis();
                Long timeSinceLastReport = now - progressReportTime;
                int changeSinceLastReport = previousUnfinishedJobs - unfinishedJobs;
                if(timeSinceLastReport > 1800000 && changeSinceLastReport > 0){
                    displayProgress = true;
                    previousUnfinishedJobs = unfinishedJobs;
                    progressCounter ++;
                }
            }
            if (forceDisplayProgress){
                displayProgress = true;
            }
            if(displayProgress){
                progressReportTime = System.currentTimeMillis();
                // Round down, to avoid confusion with 99.5% being rounded to 100% complete!
                actualProgress = Math.floor(progress * 100);
                System.out.println(Utilities.getTimeNow() + " " + String.format("%.0f%%", actualProgress) + " completed");


                int connectionCount = 9999; //statsMessageListener.getConsumers();
                String debugProgressString = " #:t" + masterTotalJobs + ":l" + unfinishedJobs + ":c" + connectionCount;
//                LOGGER.debug(statsMessageListener.getStats());
            }
        }
    }



    /**
     *   Display worker job progress report based on the number of jobs left to run
     */
    public void displayWorkerProgress(){
        if(progressCounter ==  0){
            progressReportTime = System.currentTimeMillis();
        }
        Long now = System.currentTimeMillis();
        Long timeSinceLastReport = now - progressReportTime;
        float progressPercent = 0;
        Long workerTotalJobs = totalJobs;
        if(workerTotalJobs > 5.0){
            progressPercent = (workerTotalJobs - unfinishedJobs) * 100 / workerTotalJobs;
        }
        //display every hour 60 * 60 * 1000
        if(timeSinceLastReport > 3600000){
            int connectionCount = statsMessageListener.getConsumers();
            int changeSinceLastReport = previousUnfinishedJobs - unfinishedJobs;
            int finishedJobs = workerTotalJobs.intValue() - unfinishedJobs;
//            System.out.println(Utilities.getTimeNow() + " " + String.format("%.0f%%",progressPercent)  + " of analyses done");
            System.out.println(Utilities.getTimeNow() + " " + finishedJobs + " of " + workerTotalJobs + " completed");

            String debugProgressString = " #:t" + workerTotalJobs + ":l" + unfinishedJobs + ":c" + connectionCount;

            LOGGER.debug(statsMessageListener.getStats());
            progressReportTime = System.currentTimeMillis();
            previousUnfinishedJobs = unfinishedJobs;
        }
        progressCounter ++;
    }

    /**
     *   Display final worker job progress report based on the number of jobs left to run
     */
    public void displayFinalWorkerProgress(){
        Long workerTotalJobs = totalJobs;
        int finishedJobs = workerTotalJobs.intValue() - unfinishedJobs;
        System.out.println(Utilities.getTimeNow() + " Completed " + finishedJobs + " of " + workerTotalJobs + " jobs");
        LOGGER.debug(statsMessageListener.getStats());
    }


    /**
     *
     */

    public int getAvailableProcessors(){
        int processors = Runtime.getRuntime().availableProcessors();
        LOGGER.debug(Utilities.getTimeNow() + " Processors available: " + processors);
        return processors;
    }

    public void memoryDisplay(){
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memBean.getHeapMemoryUsage();
        MemoryUsage nonheap = memBean.getNonHeapMemoryUsage();

        if(systemInfo == null){
            systemInfo = new SystemInfo();
        }
        System.out.println("SystemInfo \n " + systemInfo.Info());
    }

    public void displaySystemInfo(){
        if(systemInfo == null){
            systemInfo = new SystemInfo();
        }
        System.out.println(Utilities.getTimeNow() + " " + "SystemInfo \n " + systemInfo.Info());
    }

    public void displayMemInfo(){
        if(systemInfo == null){
            systemInfo = new SystemInfo();
        }
        System.out.println(Utilities.getTimeNow() + " Stats from the JVM ");
        System.out.println(systemInfo.MemInfo());
        System.out.println(getHeapNonHeapUsage());
        // get virtual memory etc
        String PID = "";
        try{
            PID = Utilities.getPid();
            System.out.println(Utilities.getTimeNow() + " " + Utilities.getSwapMemoryDetailsCLC(PID));
            Utilities.runFreeCmd();
            Utilities.runVmstatCmd();
        }catch (Exception ex ){
            LOGGER.debug("Error in getting process PID" + ex);
            System.out.println(Utilities.getTimeNow() + " Failed to get other memory stats - PID : " + PID + " " + ex);
            ex.printStackTrace();
        }
    }

    /**
     * get memory usage for the JVM
     * @return
     */
    public String getHeapNonHeapUsage(){
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memBean.getHeapMemoryUsage();
        MemoryUsage nonheap = memBean.getNonHeapMemoryUsage();

        StringBuilder sb = new StringBuilder();

        sb.append("heap: ");
        sb.append(heap);
        sb.append("\n");
        sb.append("nonheap: ");
        sb.append(nonheap);
        return sb.toString();

    }

    /**
     * get memeory utilisation from the JvM
     */
    public void getJVMMemory(){

        // init code
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        HotSpotDiagnosticMXBean diagBean;
        try {
            diagBean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            // loop code
            // add some code to figure if we have passed some threshold, then
            LOGGER.debug("Memory diagnostic options: " + diagBean.getDiagnosticOptions().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }


//        File heapFile = new File(outputDir, "heap-" + curThreshold + ".hprof");
//        log.info("Dumping heap file " + heapFile.getAbsolutePath());
//        diagBean.dumpHeap(heapFile.getAbsolutePath(), true);
    }

}
