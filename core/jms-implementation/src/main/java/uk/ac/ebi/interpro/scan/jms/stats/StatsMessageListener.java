package uk.ac.ebi.interpro.scan.jms.stats;

import org.apache.log4j.Logger;

import javax.jms.*;

/**
 * This is the main AMQ statistics broker listener
 * On receiving a message for statistics for a specified queue, the listener populates the BrokerStatistics object
 * with the necessary values including the consumer count and number of messages on the queue
 *
 * @author Gift Nuka, Phil Jones
 * Time: 10:27
 * To change this template use File | Settings | File Templates.
 */
public class StatsMessageListener implements MessageListener {
    private static final Logger LOGGER = Logger.getLogger(StatsMessageListener.class.getName());

    private BrokerStatistics stats;

    private Destination destination;


    /**
     * Passes a message to the listener.
     *
     * @param message the message passed to the listener
     */
    public void onMessage(Message message) {
//        LOGGER.debug("Received a message on the stats response queue");
        if (message != null){
            MapMessage mapMessage = (MapMessage) message;
            try {
                stats = new BrokerStatistics(mapMessage, destination);
//                System.out.println("+ stats.toString() = " + stats.toString());
            } catch (JMSException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


    /**
     * check if a new worker is required for the worker or the master.
     *                                                          ount=0, consumerCount=4, enqueueCount=4, averageEnqueueTime=1.5, expectedCompletionTime=0, destinationName=jms.queue.worker_job_request_queue}

     * @param completionTargetMillis - completion target time is dependent on the worker or master and is calculated differently.
     * @return
     */
    public boolean newWorkersRequired(int completionTargetMillis){

        if (stats == null){
            return false;
        }
        LOGGER.debug("+ stats.toString() = " + stats.toString());
        return stats.expectedCompletionTime() > completionTargetMillis;
    }

    public String getStats(){
        if (stats == null){
            LOGGER.debug("No stats are available for this queue");
            return null;
        }
        return stats.toString();
    }

    public boolean  hasConsumers(){
        if (stats == null){
           return false;
        }
        //consumer counter is greater than 1 (invmconsumer)
        return stats.getConsumerCount() > 1;
    }

    public int  getConsumers(){
        if (stats == null){
            return 0;
        }
        //consumer counter is gresater than 1 (invmconsumer)
        return stats.getConsumerCount();
    }

    public  int getQueueSize(){
        if (stats == null){
            return 0;
        }
        return stats.getEnqueueCount() - stats.getDispatchCount();
    }

    public int getEnqueueCount(){
        if (stats == null){
            return 0;
        }
        return stats.getEnqueueCount();
    }

    public int getDequeueCount(){
        if (stats == null){
            return 0;
        }
        return stats.getDequeueCount();
    }

    public int getDispatchCount(){
        if (stats == null){
            return 0;
        }
        return stats.getDispatchCount();
    }

    public void setStats(BrokerStatistics stats) {
        this.stats = stats;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
        setStats(null);
    }
}
