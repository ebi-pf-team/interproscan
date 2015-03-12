package uk.ac.ebi.interpro.scan.jms.stats;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;

import javax.jms.*;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: nuka
 * Date: 11/09/12
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
public class QueueBrowser {

    public QueueBrowser() {
    }

    public static int getQueueSize(final JmsTemplate jmsTemplate, final Destination queue) {
        int numOnQueue = 0;
        javax.jms.QueueBrowser inQBrowser = jmsTemplate.execute(new SessionCallback<javax.jms.QueueBrowser>() {
            public javax.jms.QueueBrowser doInJms(Session session) throws JMSException {
                return session.createBrowser((Queue) queue);
            }
        }, true);
        Enumeration messagesOnQ = null;
        try {
            messagesOnQ = inQBrowser.getEnumeration();
            while (messagesOnQ.hasMoreElements()) {
                messagesOnQ.nextElement();
                numOnQueue++;
            }
            inQBrowser.close();
        } catch (JMSException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return numOnQueue;
    }

    public static int getQueueSize(final JmsTemplate jmsTemplate, final String  queueName) {
        int numOnQueue = 0;
        javax.jms.QueueBrowser inQBrowser = jmsTemplate.execute(new SessionCallback<javax.jms.QueueBrowser>() {
            public javax.jms.QueueBrowser doInJms(Session session) throws JMSException {
                Queue queue = session.createQueue(queueName);
                return session.createBrowser(queue);
            }
        }, true);
        Enumeration messagesOnQ = null;
        try {
            messagesOnQ = inQBrowser.getEnumeration();
            while (messagesOnQ.hasMoreElements()) {
                messagesOnQ.nextElement();
                numOnQueue++;
            }
            inQBrowser.close();
        } catch (JMSException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return numOnQueue;
    }

    public static String getQueueName(Destination queue) {
        try {
            if(queue!=null){
                return ((Queue) queue).getQueueName();
            }
        } catch (JMSException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "*";
    }
}
