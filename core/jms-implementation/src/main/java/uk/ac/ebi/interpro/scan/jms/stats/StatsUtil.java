package uk.ac.ebi.interpro.scan.jms.stats;

import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.sql.Timestamp;
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

    private static boolean stopRemoteQueueJmsContainer = false;

    private AtomicBoolean RemoteQueueContainerStopped = new AtomicBoolean(false);

    private static int tier;

    private int progressCounter = 0;

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
            Thread.sleep(1*500);
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
     *   Display progress report based on the number of jobs left to run
     */
    public void displayProgress(){
        if(unfinishedJobs > 0 && totalJobs > 5){
            Long progress = (totalJobs - unfinishedJobs) / totalJobs;
            if (progress > 0.25 && progress < 0.5 && progressCounter < 1){
                System.out.println("Progress: 25% done" + " #:" + totalJobs + ":" + unfinishedJobs);
                progressCounter = 1;
            }else if  (progress > 0.5 && progress < 0.75 && progressCounter < 2){
                System.out.println("Progress: 50% done" + " #:" + totalJobs + ":" + unfinishedJobs);
                progressCounter = 2;
            }else if  (progress > 0.75 && progress < 0.9 && progressCounter < 3){
                System.out.println("Progress: 75% done" + " #:" + totalJobs + ":" + unfinishedJobs);
                progressCounter = 3;
            }else if  (progress > 0.9 && progressCounter < 4){
                System.out.println("Progress: 90% done" + " #:" + totalJobs + ":" + unfinishedJobs);
                progressCounter = 4;
            }
        }
    }
}
