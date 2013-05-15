package uk.ac.ebi.interpro.scan.jms.stats;

import com.sun.management.HotSpotDiagnosticMXBean;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import javax.management.MBeanServer;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private AtomicBoolean RemoteQueueContainerStopped = new AtomicBoolean(false);

    private static int tier;

    private int progressCounter = 0;

    private Long progressReportTime = Long.MAX_VALUE;

    public StatsUtil() {

    }

    public synchronized int getTier() {
        return tier;
    }

    public synchronized  void setTier(int tier) {
        this.tier = tier;
    }

    public synchronized boolean isStopRemoteQueueJmsContainer() {
        return stopRemoteQueueJmsContainer;
    }

    public synchronized void setStopRemoteQueueJmsContainer(boolean stopRemoteQueueJmsContainer) {
        this.stopRemoteQueueJmsContainer = stopRemoteQueueJmsContainer;
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
        this.totalJobs = totalJobs;
    }

    public synchronized int getUnfinishedJobs() {
        return unfinishedJobs;
    }

    public synchronized void setUnfinishedJobs(int unfinishedJobs) {
        this.unfinishedJobs = unfinishedJobs;
    }

    /**
     * poll  the statistics broker plugin
     * @param queue
     * @return
     */

    private boolean  pollStatsBroker(Destination queue){
        statsMessageListener.setDestination(queue);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LOGGER.debug("Setting the destination to "+getQueueName(queue) +" at " +timestamp);
        jmsTemplate.execute(STATS_BROKER_DESTINATION +  getQueueName(queue), new ProducerCallbackImpl(statsQueue));
        //wait for a second to receive the message
        try {
            Thread.sleep(3*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            if(LOGGER.isDebugEnabled()&& queue!=null){
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            if(LOGGER.isDebugEnabled()&& topic!=null){
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return  statsMessageListener.getStats()!=null;
    }


    /**
     *   Display master job progress report based on the number of jobs left to run
     */
    public void displayMasterProgress(){
//        System.out.println("#un:to - " + unfinishedJobs + ":" + totalJobs);
        if(unfinishedJobs > 0 && totalJobs > 5.0){
            Double progress = (double)(totalJobs - unfinishedJobs) / (double) totalJobs;
//            System.out.println(" Progress:  " + progress + ":" + progressCounter + "  ");
            int connectionCount = statsMessageListener.getConsumers();
            boolean displayProgress = false;
            double actualProgress = 0d;
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
            if(displayProgress){
                progressReportTime = System.currentTimeMillis();
                actualProgress = progress * 100;
                System.out.println(Utilities.getTimeNow() + " " + String.format("%.0f%%",actualProgress) + " completed");
                String debugProgressString = " #:t" + totalJobs + ":l" + unfinishedJobs + ":c" + connectionCount;
                LOGGER.debug(statsMessageListener.getStats());
            }
        }
    }



    /**
     *   Display master job progress report based on the number of jobs left to run
     */
    public void displayWorkerProgress(){
        if(progressCounter ==  0){
             progressReportTime = System.currentTimeMillis();
        }
        Long now = System.currentTimeMillis();
        Long timeSinceLastReport = now - progressReportTime;
        float progressPercent = 0;
        if(totalJobs > 5.0){
            progressPercent = (totalJobs - unfinishedJobs) * 100 / totalJobs;
        }
        if(timeSinceLastReport > 1800000){
            int connectionCount = statsMessageListener.getConsumers();
            int changeSinceLastReport = previousUnfinishedJobs - unfinishedJobs;
            int finishedJobs = totalJobs.intValue() - unfinishedJobs;
//            System.out.println(Utilities.getTimeNow() + " " + String.format("%.0f%%",progressPercent)  + " of analyses done");
            System.out.println(Utilities.getTimeNow() + " " + finishedJobs + " of " + totalJobs + " completed");
            String debugProgressString = " #:t" + totalJobs + ":l" + unfinishedJobs + ":c" + connectionCount;
            LOGGER.debug(statsMessageListener.getStats());
            progressReportTime = System.currentTimeMillis();
            previousUnfinishedJobs = unfinishedJobs;
        }

        progressCounter ++;
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
    }

    public void memoryMonitor(){

        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memBean.getHeapMemoryUsage();
        MemoryUsage nonheap = memBean.getNonHeapMemoryUsage();

        // init code
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        HotSpotDiagnosticMXBean diagBean = null;
        try {
            diagBean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            // loop code
            // add some code to figure if we have passed some threshold, then
            LOGGER.debug("Memory diagnostic options: " + diagBean.getDiagnosticOptions().toString());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


//        File heapFile = new File(outputDir, "heap-" + curThreshold + ".hprof");
//        log.info("Dumping heap file " + heapFile.getAbsolutePath());
//        diagBean.dumpHeap(heapFile.getAbsolutePath(), true);
    }
}
