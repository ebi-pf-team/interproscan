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

    private final ConcurrentMap<String, Map<String, String>> allAvailableJobs = new ConcurrentHashMap<>();

    private final List<String> runningJobs = Collections.synchronizedList(new ArrayList<String>());

    private final ConcurrentMap<String, Map<String, String>> submittedStepInstances = new ConcurrentHashMap();

    ConcurrentMap<UUID, WorkerState> workerStateMap = new ConcurrentHashMap<UUID, WorkerState>();

    private final Object jobListLock = new Object();

    private Long lastMessageReceivedTime = System.currentTimeMillis();
    private Long lastLocalMessageFinishedTime = System.currentTimeMillis();

    private long timeOfLastMemoryDisplay = System.currentTimeMillis();

    private long startUpTime;
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

    public synchronized void setTier(int tier) {
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

    public synchronized Long getTotalJobs() {
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

    public void addToAllAvailableJobs(StepInstance stepInstance, String status) {
        String key = stepInstance.getStepId();
        String proteinRange = "[" + stepInstance.getBottomProtein() + "-" + stepInstance.getTopProtein() + "]";
        Map<String, String> jobStatus = allAvailableJobs.get(key);
        if (jobStatus != null){
            jobStatus.put(proteinRange, status);
        }else{
            jobStatus = new ConcurrentHashMap<>();
            jobStatus.put(proteinRange, status);
        }
        allAvailableJobs.put(key, jobStatus);
    }

    public void removeFromAllAvailableJobs(StepInstance stepInstance) {
        String key = stepInstance.getStepId();
        String proteinRange = "[" + stepInstance.getBottomProtein() + "-" + stepInstance.getTopProtein() + "]";
        Map<String, String> jobStatus = allAvailableJobs.get(key);
        if (jobStatus != null){
            jobStatus.remove(proteinRange);
            if (jobStatus.isEmpty()) {
                allAvailableJobs.remove(key);
            }
        }
    }


    public void addToSubmittedStepInstances(StepInstance stepInstance) {
        String key = stepInstance.getStepId();
        String proteinRange = "[" + stepInstance.getBottomProtein() + "-" + stepInstance.getTopProtein() + "]";
        Map<String, String> jobStatus = submittedStepInstances.get(key);
        String status = "submitted";

        if (jobStatus != null){
            jobStatus.put(proteinRange, status);
        }else{
            jobStatus = new ConcurrentHashMap<>();
            jobStatus.put(proteinRange, status);
            //Utilities.verboseLog("StepInstanceAdd " + key + ":" +  jobStatus.toString());
        }

        submittedStepInstances.put(key, jobStatus);
        addToAllAvailableJobs(stepInstance, "submitted");
    }

    public void updateSubmittedStepInstances(StepInstance stepInstance) {
        String key = stepInstance.getStepId();
        Utilities.verboseLog("Update submittedStepInstance: " + key + " (" + stepInstance.getStepId() + ")");
        String proteinRange = "[" + stepInstance.getBottomProtein() + "-" + stepInstance.getTopProtein() + "]";
        Map<String, String> jobStatus = submittedStepInstances.get(key);
        String status = "Done";
        if (jobStatus != null){
            jobStatus.put(proteinRange, status);
            submittedStepInstances.put(key, jobStatus);
            removeFromAllAvailableJobs(stepInstance);
        }else{
            LOGGER.warn("Trying to update a step that is not the list - step:" + key +  "["
                    + proteinRange + "] -- " + status);
        }
    }

    public void removeFromSubmittedStepInstances(StepInstance stepInstance) {
        String key = stepInstance.getStepId();
        submittedStepInstances.remove(key);
    }

    public Integer getKey(Long id, String name) {
        String keyString = name + id.toString();
        return keyString.hashCode();
    }

    public void printSubmittedStepInstances() {
        Utilities.verboseLog(" submittedStepInstances:");
        Set ids = submittedStepInstances.keySet();
        Utilities.verboseLog(" submittedStepInstances:" + ids.size());
        //Collections.sort((List<Comparable>) ids);

        for (Object stepInstanceId : ids) {
            Long id = (Long) stepInstanceId;
            System.out.println(id + ":" + submittedStepInstances.get(id));
        }
    }

    public int getSubmittedStepInstancesCount() {
        Utilities.verboseLog(" getSubmittedStepInstancesCounts:");
        int uniqStepCount = 0;
        int stepCount = 0;
        for (Map.Entry<String, Map<String, String>> elem:submittedStepInstances.entrySet()) {
            Map<String, String> jobStatus = (Map<String, String>) elem.getValue();
            stepCount += jobStatus.keySet().size();
            uniqStepCount ++;
        }
        if (stepCount < uniqStepCount){
            Utilities.verboseLog(" Originals: stepCount " + stepCount + " uniq stepCount: " + uniqStepCount);
            stepCount = uniqStepCount;
        }
        Utilities.verboseLog(" getSubmittedStepInstancesCounts: " + stepCount + " uniq: " + uniqStepCount);
        return stepCount;
    }

    /**
     *
     * @return
     */
    public int getSubmittedStepInstancesCountOld() {
        return getSubmittedStepInstancesCount();
    }

    public Set<String> getNonAcknowledgedSubmittedStepInstances() {
        Utilities.verboseLog(" getNonAcknowledgedSubmittedStepInstances:");
        Set ids = submittedStepInstances.entrySet();
        Set<String> nonAcknowledgedStepInstances = new TreeSet<String>();
        Utilities.verboseLog(" submittedStepInstances:" + ids.size());

        for (Object entry : submittedStepInstances.entrySet()) {
            if (!((Map.Entry<Integer, String>) entry).getValue().contains("Done")) {
                nonAcknowledgedStepInstances.add(((Map.Entry<Integer, String>) entry).getValue());
            }
        }
        return nonAcknowledgedStepInstances;
    }

    public Map<String, Integer> getNonAcknowledgedSubmittedStepInstancesCounts() {
        Utilities.verboseLog(" getNonAcknowledgedSubmittedStepInstances:");
        Map<String, Integer>  nonAcknowledgedStepInstances = new HashMap<>();
        for  (Map.Entry<String, Map<String, String>> elem:submittedStepInstances.entrySet()) {
            String key = (String) elem.getKey();
            String status = "Done";
            Map<String, String> jobStatus = (Map<String, String>) elem.getValue();
            int notDoneCount = 0;
            for (Map.Entry<String, String> entryStatus : jobStatus.entrySet()) {
                String value = (String)  entryStatus.getValue();
                if (! value.equals(status)){
                    notDoneCount ++;
                }
            }
            if (notDoneCount > 0) {
                nonAcknowledgedStepInstances.put(key, notDoneCount);
            }
        }
        return nonAcknowledgedStepInstances;
    }

    public void displayNonAcknowledgedSubmittedStepInstances() {
        String nonAcknowledgedSubmittedStepInstances = "[";
        if (Utilities.verboseLog) {
            Utilities.verboseLog("Current Non-Acknowledged Submitted StepInstances ");

            for (Map.Entry<String, Integer> elem:getNonAcknowledgedSubmittedStepInstancesCounts().entrySet()) {
                String jobName = (String) elem.getKey();
                int notDoneCount = (int) elem.getValue();
                if (notDoneCount > 0) {
                    String jobMessage = "#:" + notDoneCount; // + ") (rep:" + status;
                    nonAcknowledgedSubmittedStepInstances += String.format("%s (%s)", jobName, jobMessage)
                            + ", ";
                }
            }
        }
        nonAcknowledgedSubmittedStepInstances += "]";
        System.out.println(nonAcknowledgedSubmittedStepInstances);
    }



    /**
     * Time last message was received
     */
    public void updateLastMessageReceivedTime() {
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
    public void displayMemoryAndRunningJobs() {
        Long now = System.currentTimeMillis();
        Long timeSinceLastMemoryDisplay = now - timeOfLastMemoryDisplay;
        if (timeSinceLastMemoryDisplay > 5 * 1000) {
            displayMemInfo();

            System.out.println(Utilities.getTimeNow() + " Current active Jobs");
            List<String> currentRunningJobs = getRunningJobs();
            for (String runningJob : currentRunningJobs) {
                System.out.println(runningJob);
            }
            timeOfLastMemoryDisplay = System.currentTimeMillis();
        }

    }


    public void displayRunningJobs() {
        if (Utilities.verboseLog) {
            Utilities.verboseLog("Current active Jobs");
            List<String> currentRunningJobs = getRunningJobs();
            Map<String,List<String>> runningJobsMap = new HashMap<>();
            for (String runningJob : currentRunningJobs) {
                String key = runningJob.split(":")[0];
                String proteinRange = runningJob.split(":")[1];
                if (runningJobsMap.containsKey(key)) {
                    runningJobsMap.get(key).add(proteinRange);
                }else{
                    List<String> proteinRanges = new ArrayList<>();
                    proteinRanges.add(proteinRange);
                    runningJobsMap.put(key,proteinRanges);
                }
            }
            for (Map.Entry<String,List<String>> elem : runningJobsMap.entrySet()) {
                String key = (String) elem.getKey();
                List<String> proteinRanges = (List<String>) elem.getValue();
                Collections.sort(proteinRanges);
                System.out.println(String.format("%8s %s %s", "", key, proteinRanges.toString()));
            }
        }
    }

    public List<String> getRunningJobs() {
        synchronized (runningJobs) {
            return Collections.unmodifiableList(new ArrayList<String>(runningJobs));
        }
    }

    public void displayAllAvailableJobs() {
        if (Utilities.verboseLog) {
            Utilities.verboseLog("Current available Jobs");
            for (Map.Entry<String, Map<String, String>> elem:allAvailableJobs.entrySet()) {
                String jobName = (String) elem.getKey();
                String status = "";
                Map<String, String> jobStatus = (Map<String, String>) elem.getValue();
                int jobCount = jobStatus.size();
                for (Map.Entry<String, String> entryStatus : jobStatus.entrySet()) {
                    String key = (String) entryStatus.getKey();
                    String value = (String)  entryStatus.getValue();
                    status = key + "-" + value; // + ",";
                }
                String jobMessage = "#:" + jobCount + ") (rep:" + status;
                System.out.println(String.format("%15s %s (%s)", "", jobName, jobMessage));
            }
        }
    }


    public ConcurrentMap<UUID, WorkerState> getWorkerStateMap() {
        return workerStateMap;
    }

    /**
     * insert or replace worker state
     *
     * @param workerState
     */
    public void updateWorkerStateMap(WorkerState workerState) {
        if (workerStateMap.replace(workerState.getWorkerIdentification(), workerState) == null) {
            workerStateMap.putIfAbsent(workerState.getWorkerIdentification(), workerState);

        }
    }

    /**
     * poll  the statistics broker plugin
     *
     * @param queue
     * @return
     */

    private synchronized boolean pollStatsBroker(Destination queue) {
        statsMessageListener.setDestination(queue);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LOGGER.debug("Setting the destination to " + getQueueName(queue) + " at " + timestamp);
        jmsTemplate.execute(STATS_BROKER_DESTINATION + getQueueName(queue), new ProducerCallbackImpl(statsQueue));
        //wait for a second to receive the message
        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return statsMessageListener.getStats() != null;
    }

    public boolean pollStatsBrokerJobQueue() {

        return pollStatsBroker(jobRequestQueue);
    }

    public boolean pollStatsBrokerHighMemJobQueue() {
        return pollStatsBroker(highMemJobRequestQueue);
    }

    public boolean pollStatsBrokerResponseQueue() {
        return pollStatsBroker(jobResponseQueue);
    }

    public boolean pollStatsBrokerTopic() {
        return pollStatsBroker(workerManagerTopicQueue);
    }

    public String getQueueName(Destination queue) {
        try {
            if (queue != null) {
                return ((Queue) queue).getQueueName();
            }
        } catch (JMSException e) {
            e.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("There is a problem with the queue name " + queue.toString());
            }
        }
        return "*";
    }

    public String getTopicName(Destination topic) {
        try {
            if (topic != null) {
                return ((Topic) topic).getTopicName();
            }
        } catch (JMSException e) {
            e.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("There is a problem with the queue name " + topic.toString());
            }
        }
        return "*";
    }

    public void sendMessage() {
        jmsTemplate.send(jobRequestQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage("test");
            }
        });
    }

    public void sendhighMemMessage() {
        jmsTemplate.send(highMemJobRequestQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage("test2");
            }
        });
    }

    /**
     * poll the topic for statistics
     *
     * @param topic
     * @return
     */
    private boolean pollStatsBrokerTopic(Destination topic) {
        statsMessageListener.setDestination(topic);
        LOGGER.info("Setting the destination to " + getQueueName(topic));
        jmsTemplate.execute(STATS_BROKER_DESTINATION + getTopicName(topic), new ProducerCallbackImpl(statsQueue));
        //wait for a second to receive the message
        try {
            Thread.sleep(1 * 500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return statsMessageListener.getStats() != null;
    }

    /**
     * display queue statistics for debugging
     */
    public void displayQueueStatistics() {
        final boolean requestQueueStatsAvailable = pollStatsBrokerJobQueue();
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
     *
     * @return
     */
    public int getRequestQueueSize() {
        final boolean requestQueueStatsAvailable = pollStatsBrokerJobQueue();
        if (!requestQueueStatsAvailable) {
            LOGGER.warn("JobRequestQueue: not initialised");
            return -99;
        }
        setRequestQueueConsumerCount(statsMessageListener.getConsumers());
        return statsMessageListener.getEnqueueCount() - statsMessageListener.getDispatchCount();
    }

    /**
     * get highmem queuesie
     *
     * @return
     */
    public int getHighMemRequestQueueSize() {
        final boolean requestQueueStatsAvailable = pollStatsBrokerHighMemJobQueue();
        if (!requestQueueStatsAvailable) {
            Utilities.verboseLog(5, "HighMemJobRequestQueue: not initialised");
            return -99;
        }
        return statsMessageListener.getEnqueueCount() - statsMessageListener.getDispatchCount();
    }

    public void displayHighMemoryQueueStatistics() {
        final boolean requestQueueStatsAvailable = pollStatsBrokerHighMemJobQueue();
        if (!requestQueueStatsAvailable) {
            Utilities.verboseLog(5, "JobRequestQueue: not initialised");
        } else {
            Utilities.verboseLog(5, "HighMemoryJobRequestQueue:  " + statsMessageListener.getStats());
        }

    }

    /**
     * set the unfinished jobs
     */
    public void updateStatsUtilJobCounts() {
        pollStatsBrokerResponseQueue();
        int responseDequeueCount = statsMessageListener.getDequeueCount();
        pollStatsBrokerJobQueue();
        int requestEnqueueCount = statsMessageListener.getEnqueueCount();

        //unfinishedJobs = requestEnqueueCount - responseDequeueCount;
        setUnfinishedJobs(requestEnqueueCount - responseDequeueCount);
        setTotalJobs((long) requestEnqueueCount);
        pollStatsBrokerJobQueue();
    }

    /**
     * Display master job progress report based on the number of jobs left to run
     */
    public void displayMasterProgress() {
        Long masterTotalJobs = totalJobs;
        if (unfinishedJobs > 0 && masterTotalJobs > 5.0) {
            Double progress = (double) (masterTotalJobs - unfinishedJobs) / (double) masterTotalJobs;

            boolean displayProgress = false;
            Double actualProgress;
            int changeSinceLastReport = 0;
            boolean displayRemainingJobs = false;
            Long now = System.currentTimeMillis();
            Long timeSinceLastReport = now - progressReportTime;
            if (timeSinceLastReport > 1200000) {
                displayProgress = true;
                progressCounter++;
                if (progressCounter > 3) {
                    displayRemainingJobs = true;
                }
            }

            if (progress > 0.25 && progress < 0.5 && progressCounter < 1) {
                displayProgress = true;
                progressCounter = 1;
            } else if (progress > 0.5 && progress < 0.75 && progressCounter < 2) {
                displayProgress = true;
                progressCounter = 2;
            } else if (progress > 0.75 && progress < 0.9 && progressCounter < 3) {
                displayProgress = true;
                //displayRemainingJobs = true; //TODO debug so remove
                progressCounter = 3;
            } else if (progress > 0.9 && progressCounter < 4) {
                displayProgress = true;
                progressCounter = 4;
            } else if (progress > 0.9 && progressCounter >= 4) {
                changeSinceLastReport = previousUnfinishedJobs - unfinishedJobs;
                if (timeSinceLastReport > 1800000 && changeSinceLastReport > 0) {
                    displayProgress = true;
                    previousUnfinishedJobs = unfinishedJobs;
                    progressCounter++;
                }
            }
            if (forceDisplayProgress) {
                displayProgress = true;
            }
            if (displayProgress) {
                progressReportTime = System.currentTimeMillis();
                // Round down, to avoid confusion with 99.5% being rounded to 100% complete!
                actualProgress = Math.floor(progress * 100);
                System.out.println(Utilities.getTimeNow() + " " + String.format("%.0f%%", actualProgress) + " completed");
                Set<String> nonAckStepInstances = new HashSet<>();

                //for (StepInstance stepinstance: getNonAcknowledgedSubmittedStepInstances()):

                Utilities.verboseLog("NonAcknowledgedSubmittedStepInstances: ");
                displayNonAcknowledgedSubmittedStepInstances();
                displayRunningJobs();
                if (displayRemainingJobs) {
                    displayAllAvailableJobs();
                }

                int connectionCount = 9999; //statsMessageListener.getConsumers();
                changeSinceLastReport = previousUnfinishedJobs - unfinishedJobs;
                if (changeSinceLastReport > 0) {
                    previousUnfinishedJobs = unfinishedJobs;
                }
                previousUnfinishedJobs = unfinishedJobs; // maybe the above conditional is superfluous

                String debugProgressString = " #:t" + masterTotalJobs + " :l" + unfinishedJobs + " change: " + changeSinceLastReport + " :c" + connectionCount;
                Utilities.verboseLog("debugProgressString: " + debugProgressString);
//                LOGGER.debug(statsMessageListener.getStats());
            }
        }
    }


    /**
     * Display worker job progress report based on the number of jobs left to run
     */
    public void displayWorkerProgress() {
        if (progressCounter == 0) {
            progressReportTime = System.currentTimeMillis();
        }
        Long now = System.currentTimeMillis();
        Long timeSinceLastReport = now - progressReportTime;
        float progressPercent = 0;
        Long workerTotalJobs = totalJobs;
        if (workerTotalJobs > 5.0) {
            progressPercent = (workerTotalJobs - unfinishedJobs) * 100 / workerTotalJobs;
        }
        //display every hour 60 * 60 * 1000
        if (timeSinceLastReport > 3600000) {
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
        progressCounter++;
    }

    /**
     * Display final worker job progress report based on the number of jobs left to run
     */
    public void displayFinalWorkerProgress() {
        Long workerTotalJobs = totalJobs;
        int finishedJobs = workerTotalJobs.intValue() - unfinishedJobs;
        System.out.println(Utilities.getTimeNow() + " Completed " + finishedJobs + " of " + workerTotalJobs + " jobs");
        LOGGER.debug(statsMessageListener.getStats());
    }


    /**
     *
     */

    public int getAvailableProcessors() {
        int processors = Runtime.getRuntime().availableProcessors();
        LOGGER.debug(Utilities.getTimeNow() + " Processors available: " + processors);
        return processors;
    }

    public void memoryDisplay() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memBean.getHeapMemoryUsage();
        MemoryUsage nonheap = memBean.getNonHeapMemoryUsage();

        if (systemInfo == null) {
            systemInfo = new SystemInfo();
        }
        System.out.println("SystemInfo \n " + systemInfo.Info());
    }

    public void displaySystemInfo() {
        if (systemInfo == null) {
            systemInfo = new SystemInfo();
        }
        System.out.println(Utilities.getTimeNow() + " " + "SystemInfo \n " + systemInfo.Info());
    }

    public void displayMemInfo() {
        if (systemInfo == null) {
            systemInfo = new SystemInfo();
        }
        System.out.println(Utilities.getTimeNow() + " Stats from the JVM ");
        System.out.println(systemInfo.MemInfo());
        System.out.println(getHeapNonHeapUsage());
        // get virtual memory etc
        String PID = "";
        try {
            PID = Utilities.getPid();
            System.out.println(Utilities.getTimeNow() + " " + Utilities.getSwapMemoryDetailsCLC(PID));
            Utilities.runFreeCmd();
            Utilities.runVmstatCmd();
        } catch (Exception ex) {
            LOGGER.debug("Error in getting process PID" + ex);
            System.out.println(Utilities.getTimeNow() + " Failed to get other memory stats - PID : " + PID + " " + ex);
            ex.printStackTrace();
        }
    }

    /**
     * get memory usage for the JVM
     *
     * @return
     */
    public String getHeapNonHeapUsage() {
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
    public void getJVMMemory() {

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
