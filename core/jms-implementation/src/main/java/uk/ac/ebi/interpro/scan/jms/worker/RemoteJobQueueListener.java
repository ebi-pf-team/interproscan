package uk.ac.ebi.interpro.scan.jms.worker;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import uk.ac.ebi.interpro.scan.jms.stats.StatsUtil;

import javax.jms.*;

/**
 * This implementation receives responses on the destinationResponseQueue
 * and then propagates them to the super worker or master.
 *
 * @author nuka, scheremetjew
 * @version $Id: ResponseMonitorImpl.java,v 1.1.1.1 2009/10/07 13:37:52 pjones Exp $
 * @since 1.0
 */
public class RemoteJobQueueListener implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(RemoteJobQueueListener.class.getName());

    private JmsTemplate localJmsTemplate;

    private WorkerMessageSender workerMessageSender;

    private Destination jobRequestQueue;

    private StatsUtil statsUtil;

//    private WorkerState workerState;

    private boolean gridThrottle = true;

    private int jobCount = 0;

    long timeFirstMessageReceived = 0;

    private int maxUnfinishedJobs;

    @Required
    public void setGridThrottle(boolean gridThrottle) {
        this.gridThrottle = gridThrottle;
    }

    @Required
    public void setLocalJmsTemplate(JmsTemplate localJmsTemplate) {
        this.localJmsTemplate = localJmsTemplate;
    }

    @Required
    public void setJobRequestQueue(Destination jobRequestQueue) {
        this.jobRequestQueue = jobRequestQueue;
    }

    @Required
    public void setWorkerMessageSender(WorkerMessageSender workerMessageSender) {
        this.workerMessageSender = workerMessageSender;
    }

    @Required
    public void setStatsUtil(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }

    @Required
    public void setMaxUnfinishedJobs(int maxUnfinishedJobs) {
        this.maxUnfinishedJobs = maxUnfinishedJobs;
    }

//    public void setWorkerState(WorkerState workerState) {
//        this.workerState = workerState;
//    }

    @Override
    public void onMessage(final Message message) {
        if(jobCount == 0){
            LOGGER.debug("onMessage: FirstJobReceived  ");
            timeFirstMessageReceived = System.currentTimeMillis();
        }
        jobCount ++;
        if (!(message instanceof ObjectMessage)) {
            LOGGER.error("RemoteQueue Message Listener: Received a message of an unknown type (non-ObjectMessage)");
            try {
                LOGGER.debug("Message type of the unknown type message="+message.getJMSType());
                LOGGER.debug("Message ID of the unknown type message="+message.getJMSMessageID());
            } catch (JMSException e) {
                LOGGER.debug("Message problem: Failed to access message - "+e.toString());
                e.printStackTrace();

            }
        }
//        localJmsTemplate.send(jobRequestQueue, new MessageCreator() {
//            public Message createMessage(Session session) throws JMSException {
//                return message;
//            }
//        });
        //send message
        try {
            workerMessageSender.sendMessage(jobRequestQueue,message, true);
//            workerState.addNewJob(message);
        } catch (JMSException e) {
            LOGGER.debug("Message problem: Failed to access message - "+e.toString());
            e.printStackTrace();
        }  catch (Exception e) {
            LOGGER.debug("Message problem: Failed to access message - "+e.toString());
        }
        //check the size of the queue
        if(gridThrottle){
            checkQueueState();
        }

        LOGGER.debug("Worker: received a message from the remote request queue and forwarded it onto the local jobRequestQueue");
    }

    /**
     * check if the message quota for this worker has been reached and then block for a few seconds
     *
     */
    public void checkQueueState(){
        LOGGER.debug("checkQueueState - Check the state of the local queue depending on the tier we are in ");
        int unfinishedJobs = getUnifinishedJobs(); //statsUtil.getUnfinishedJobs();
        LOGGER.debug("checkQueueState - maxUnfinishedJobs: " + maxUnfinishedJobs + ",  unfinishedJobs: " + unfinishedJobs);
        if(jobCount == 2){
            long now = System.currentTimeMillis();
            if((now - timeFirstMessageReceived) < 10*1000){
                try {
                    LOGGER.debug("First 8 messages ... ");
                    Thread.sleep(15*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        if(statsUtil.isStopRemoteQueueJmsContainer()){
            LOGGER.debug("checkQueueState : isStopRemoteQueueJmsContainer = true");
            return;
        }else{
            boolean stopRemoteQueue = false;
            switch(statsUtil.getTier()){
                case 1:
                    if(unfinishedJobs > maxUnfinishedJobs){
                        stopRemoteQueue = true;
                    }
                    break;
                case 2:
                    if(unfinishedJobs > maxUnfinishedJobs / 2){
                        stopRemoteQueue = true;
                    }
                    break;
                default:
                    if(unfinishedJobs > maxUnfinishedJobs / (Math.pow(2, statsUtil.getTier() - 1 ))){
                        stopRemoteQueue = true;
                    }
            }
            //stop the remoteQueueListener
            if (stopRemoteQueue){
                LOGGER.debug("checkQueueState - Disable remote listener ");
                statsUtil.setStopRemoteQueueJmsContainer(true);
                //wait for some seconds before exiting onMessage
                try {
                    Thread.sleep(15*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    private int  getUnifinishedJobs(){
        statsUtil.pollStatsBrokerResponseQueue();
        int responseDequeueCount =    statsUtil.getStatsMessageListener().getDequeueCount();
        return jobCount - responseDequeueCount;
    }
}
