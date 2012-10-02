package uk.ac.ebi.interpro.scan.jms.stats;

import org.joda.time.DateTime;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Queue;
import java.util.Enumeration;

/**
 * @author Gift Nuka, Phil Jones, Maxim
 * Date: 17/08/12
 * Time: 15:14
 */
public class BrokerStatistics {

    private DateTime timeStamp = new DateTime();

    private Integer dispatchCount;

    private Integer dequeueCount;

    private Integer inflightCount;

    private Integer consumerCount;

    private Integer enqueueCount;

    private Double averageEnqueueTime;

    private String  destinationName;


    /**
     * Constructor that build a statistics object from the MapMessage returned
     * from the ActiveMQ broker.  NOTE = not a general JMS solution.
     * @param mapMessage returned from the ActiveMQ broker
     * @throws javax.jms.JMSException
     */
    public BrokerStatistics(MapMessage mapMessage, Destination destination) throws JMSException {
        if (mapMessage == null){
            throw new IllegalArgumentException("The mapMessage cannot be null");
        }
        if (destination == null){
            throw new IllegalArgumentException("The destination cannot be null");
        }
        destinationName = ((Queue) destination).getQueueName();
        Enumeration keys = mapMessage.getMapNames();
        while (keys.hasMoreElements()) {
            final String key = (String)keys.nextElement();
            if ("dispatchCount".equals(key)){
                dispatchCount = (int)mapMessage.getLong(key);
            }
            else if ("dequeueCount".equals(key)){
                dequeueCount = (int)mapMessage.getLong(key);
            }
            else if ("inflightCount".equals(key)){
                inflightCount = (int)mapMessage.getLong(key);
            }
            else if ("consumerCount".equals(key)){
                consumerCount = (int)mapMessage.getLong(key);
            }
            else if ("enqueueCount".equals(key)){
                enqueueCount = (int)mapMessage.getLong(key);
            }
            else if ("averageEnqueueTime".equals(key)){
                averageEnqueueTime = mapMessage.getDouble(key);
            }
        }

    }

    public DateTime getTimeStamp() {
        return timeStamp;
    }

    public int getDispatchCount() {
        return dispatchCount;
    }

    public int getDequeueCount() {
        return dequeueCount;
    }

    public int getInflightCount() {
        return inflightCount;
    }

    public int getConsumerCount() {
        return consumerCount;
    }

    public int getEnqueueCount() {
        return enqueueCount;
    }

    public double getAverageEnqueueTime() {
        return averageEnqueueTime;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public int expectedCompletionTime(){
        int totalEnqueue = enqueueCount - dispatchCount;
        return (int)(totalEnqueue*averageEnqueueTime/consumerCount);
    }


    @Override
    public String toString() {
        return "BrokerStatistics{" +
                "timeStamp=" + timeStamp +
                ", dispatchCount=" + dispatchCount +
                ", dequeueCount=" + dequeueCount +
                ", inflightCount=" + inflightCount +
                ", consumerCount=" + consumerCount +
                ", enqueueCount=" + enqueueCount +
                ", averageEnqueueTime=" + averageEnqueueTime +
                ", expectedCompletionTime=" + expectedCompletionTime() +
                ", destinationName=" + destinationName +
                '}';
    }
}
