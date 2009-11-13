package uk.ac.ebi.interpro.scan.jms.master;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobDataMap;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import uk.ac.ebi.interpro.scan.jms.SessionHandler;

/**
 * MessageJob is the class for executing jobs submitted by the Quartz scheduler
 * User: maslen
 * Date: 06-Nov-2009
 * Time: 14:10:09
 */

public class MessageJob implements Job{

    public static final String DESTINATION = "destination";
    public static final String MESSAGE_STRING = "message";
    public static final String SESSION_HANDLER = "session_handler";

    public MessageJob() {
    }

    public void execute(JobExecutionContext context)
        throws JobExecutionException {
        //extract data from submitted job -> queue details and message; works just as well with objects too when tested
        JobDataMap data = context.getJobDetail().getJobDataMap();
        String destination = data.getString(DESTINATION);
        String messageString = data.getString(MESSAGE_STRING);
        SessionHandler sessionHandler = (SessionHandler) data.get(SESSION_HANDLER);
        try {
            MessageProducer producer = sessionHandler.getMessageProducer(destination);
            TextMessage message = sessionHandler.createTextMessage(messageString);
            producer.send(message);
            System.out.println("Sent message: " + message.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }           
    }
}